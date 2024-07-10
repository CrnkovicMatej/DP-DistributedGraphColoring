package com.project.network;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Node implements Runnable {
    public int id;
    //public List<Node> neighbors;
    private final Set<Node> neighbors = ConcurrentHashMap.newKeySet();
    public final Set<Node> com_with = ConcurrentHashMap.newKeySet();
    private final Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();

    private final Semaphore messageSemaphore = new Semaphore(0);

    private List<Message> tempMessagesBuffer = new ArrayList<Message>();
    private AtomicBoolean inMIS;
    private AtomicBoolean active;
    public int randomValue;
    private Map<Integer, Integer> receivedColors;
    private int color;
    private boolean colored;
    private String algorithm;
    private int maxColors;

    public boolean isSmallest;

    public Node(int id, String algorithm, int maxColors) {
        this.id = id;
        this.receivedColors = new HashMap<>();
        this.color = id; // 0 means uncolored
        this.colored = false;
        this.algorithm = algorithm;
        this.maxColors = maxColors;
        this.inMIS = new AtomicBoolean(false);
        this.active = new AtomicBoolean(true);
    }
    public boolean isInMIS() {
        return inMIS.get();
    }
    public void setInMIS(boolean inMIS) {
        this.inMIS.set(inMIS);
    }

    // Getter za active
    public boolean isActive() {
        return active.get();
    }

    // Setter za active
    public void setActive(boolean active) {
        this.active.set(active);
    }
    public int getId() {
        return id;
    }

    public Set<Node> getNeighbors() {
        return neighbors;
    }

    public Set<Node> getCom_with() {
        return com_with;
    }

    public void addCommunicator(Node comm)
    {
        if (!com_with.contains(comm)) {
            com_with.add(comm);
        }
    }
    public void removeCommunicator(Node comm)
    {
        com_with.remove(comm);
    }

    public void addNeighbor(Node neighbor) {
        if (!neighbors.contains(neighbor)) {
            neighbors.add(neighbor);
        }
    }

    public List<Message> getMessageBuffer(){
        return tempMessagesBuffer;
    }

    public void setMessageBuffer(List<Message> messages)
    {
        tempMessagesBuffer =  messages;
    }

    public int getColor() {
        return color;
    }

    public void sendMessage(Node target, Message message) {
        if (neighbors.contains(target)) {
            System.out.println("Node " + id + " sending message to " + target.getId() + ": " + message.getContent());
            target.receiveMessage(message);
        }
    }

    public void receiveMessage(Message message) {
        messageQueue.offer(message);
        System.out.println("Node " + id + " has " + messageQueue.size() + " messages in the queue.");
        System.out.println("Node " + id + " received message: " + message.getContent());
        messageSemaphore.release();
    }

    public List<Message> receiveAllMessages() {
        List<Message> messages = new ArrayList<>();
        Set<Node> receivedFrom = new HashSet<>();
        // we assume no more than 1 message per round
        while (receivedFrom.size() < com_with.size()) {
            try {
                messageSemaphore.acquire();

                Message message;
                while ((message = messageQueue.poll()) != null) {
                    messages.add(message);
                    receivedFrom.add(message.getSender());
                    System.out.println("Node " + id + " received message: " + message.getContent());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return messages;
    }

    @Override
    public void run() {
        System.out.println("Node " + id + " started.");
        if ("color".equalsIgnoreCase(algorithm)) {
            System.out.println("Node " + id + " running coloring algorithm.");
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("Node " + id + " is in the thread loop.");
                processMessages();
                runColoringAlgorithm();
                try {
                    Thread.sleep(200); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } else if ("maximal".equalsIgnoreCase(algorithm)) {
            return;
        }
    }

    private synchronized void processMessages() {
        //System.out.println("Node " + id + " starts processing messages. Initial queue size: " + messageQueue.size());
//        while (!messageQueue.isEmpty()) {
//            System.out.println("Node " + id + " has " + messageQueue.size() + " messages left to process.");
//            //Message message = messageQueue.remove(0);
//            // Message message = messageQueue.poll();
//            System.out.println("Node " + id + " processed message: " + message.getContent());
//
//            String[] parts = message.getContent().split(" ");
//            String messageType = parts[0];
//            if ("INIT".equals(messageType) || "COLOR".equals(messageType)) {
//                int neighborId = Integer.parseInt(parts[1]);
//                int neighborColor = Integer.parseInt(parts[2]);
//                receivedColors.put(neighborId, neighborColor);
//                System.out.println("Node " + id + " received color " + neighborColor + " from neighbor " + neighborId);
//            }
//        }
        //System.out.println("Node " + id + " finished processing messages. Queue size now: " + messageQueue.size());
    }

    private void runColoringAlgorithm() {
        System.out.println("Node " + id + " in coloring algorithm.");
        for (int ri = 2; ri <= maxColors; ri++) {
            System.out.println("Node " + id + " in round " + ri);
            if (color == ri) {
                int newColor = 1;
                HashSet<Integer> usedColors = new HashSet<>(receivedColors.values());
                while (usedColors.contains(newColor)) {
                    newColor++;
                }
                color = newColor;
                colored = true;
                //System.out.println("Node " + id + " colored with color: " + color);
            }

            for (Node neighbor : neighbors) {
                Message message = new Message("COLOR " + id + " " + color, this, neighbor);
                sendMessage(neighbor, message);
                System.out.println("Node " + id + " sending COLOR message to " + neighbor.getId());
            }

            for (Node neighbor : neighbors) {
                while (!receivedColors.containsKey(neighbor.getId()) || receivedColors.get(neighbor.getId()) != color) {
                    //System.out.println("Node " + id + " waiting to receive COLOR from " + neighbor.getId());
                    processMessages();
                }
            }
        }

        System.out.println("LETS CHECK COLOR FOR EVERY NODE");
        // Print the color values for each node and its neighbors
        System.out.println("Node " + id + " final color: " + color);
        System.out.println("Node " + id + " neighbors and their colors:");
        for (Node neighbor : neighbors) {
            System.out.println("Neighbor " + neighbor.getId() + " color: " + neighbor.getColor());
        }
    }


    public boolean isColored() {
        return colored;
    }
    public AtomicBoolean getInMIS() {
        return inMIS;
    }

}