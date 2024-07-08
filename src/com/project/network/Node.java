package com.project.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Node implements Runnable {
    private int id;
    public List<Node> neighbors;
    // private BlockingQueue<Message> messageQueue; // BlockingQueue
    private List<Message> messageQueue;
    AtomicBoolean inMIS;
    AtomicBoolean active;
    int randomValue;
    private Map<Integer, Integer> receivedColors; // Maps neighbor ID to color
    private int color;
    private boolean colored;
    private String algorithm;
    private int maxColors;

    public Node(int id, String algorithm, int maxColors) {
        this.id = id;
        this.neighbors = new ArrayList<>();
        // this.messageQueue = new LinkedBlockingQueue<>(); //new ArrayList<>();
        this.messageQueue = new ArrayList<>();
        this.receivedColors = new HashMap<>();
        this.color = id; // 0 means uncolored
        this.colored = false;
        this.algorithm = algorithm;
        this.maxColors = maxColors;
        this.inMIS = new AtomicBoolean(false);
        this.active = new AtomicBoolean(true);
    }

    public int getId() {
        return id;
    }

    public List<Node> getNeighbors() {
        return neighbors;
    }

    public void addNeighbor(Node neighbor) {
        if (!neighbors.contains(neighbor)) {
            neighbors.add(neighbor);
        }
    }

    public int getColor() {
        return color;
    }

    public synchronized void sendMessage(Node target, Message message) {
        if (neighbors.contains(target)) {
            System.out.println("Node " + id + " sending message to " + target.getId() + ": " + message.getContent());
            target.receiveMessage(message);
        }
    }

    public synchronized void receiveMessage(Message message) {
        messageQueue.add(message);
        System.out.println("usli u receive mess " + messageQueue);

            // messageQueue.offer(message);
        
        System.out.println("Node " + id + " has " + messageQueue.size() + " messages in the queue.");
        System.out.println("Node " + id + " received message: " + message.getContent());
        // processMessages(); // Process messages immediately after receiving
    }

    public List<Message> receiveAllMessages() {
        System.out.println("usli u receive all mesh");
        List<Message> messages = new ArrayList<>();
        
        // messageQueue.drainTo(messages);
        System.out.println("nakon drain" + messageQueue);
        for (Message message : messageQueue) {
            System.out.println("Node " + this.id + " received " + message.type + "(" + message.getContent() + ")");
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
            runMaximalIndependentSetAlgorithm();
        }
    }

    private synchronized void processMessages() {
        //System.out.println("Node " + id + " starts processing messages. Initial queue size: " + messageQueue.size());
        while (!messageQueue.isEmpty()) {
            System.out.println("Node " + id + " has " + messageQueue.size() + " messages left to process.");
            Message message = messageQueue.remove(0);
            // Message message = messageQueue.poll();
            System.out.println("Node " + id + " processed message: " + message.getContent());

            String[] parts = message.getContent().split(" ");
            String messageType = parts[0];
            if ("INIT".equals(messageType) || "COLOR".equals(messageType)) {
                int neighborId = Integer.parseInt(parts[1]);
                int neighborColor = Integer.parseInt(parts[2]);
                receivedColors.put(neighborId, neighborColor);
                System.out.println("Node " + id + " received color " + neighborColor + " from neighbor " + neighborId);
            }
        }
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

    private void runMaximalIndependentSetAlgorithm() {
        if (!colored) {
            boolean allNeighborsUncolored = true;
            for (Node neighbor : neighbors) {
                if (neighbor.isColored()) {
                    allNeighborsUncolored = false;
                    break;
                }
            }

            if (allNeighborsUncolored) {
                color = 1; 
                colored = true;
                System.out.println("Node " + id + " is part of the maximal independent set");

                for (Node neighbor : neighbors) {
                    Message message = new Message("Node " + id + " is in the MIS", this, neighbor);
                    sendMessage(neighbor, message);
                }
            }
        }
    }

    public boolean isColored() {
        return colored;
    }

    public AtomicBoolean getInMIS() {
        return inMIS;
    }

    public static void lubyMIS(List<Node> nodes, int numThreads) {
        System.out.println("unutar misa");
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        Random random = new Random();

        List<Future<?>> futures = new ArrayList<>();

        while (true) {
            // List<Future<?>> futures = new ArrayList<>();
            AtomicBoolean hasActiveNodes = new AtomicBoolean(false);

            // Round r: Nodes generate random values and send to neighbors
            for (Node node : nodes) {
                if (node.active.get()) {
                    hasActiveNodes.set(true);
                    futures.add(executor.submit(() -> {
                        node.randomValue = random.nextInt(Integer.MAX_VALUE);
                        System.out.println("Node " + node.id + " generated random value: " + node.randomValue);
                        for (Node neighbor : node.neighbors) {
                            if (neighbor.active.get()) {
                                node.sendMessage(neighbor, new Message(Message.Type.RANDOM, String.valueOf(node.randomValue), node, neighbor));
                            }
                        }
                    }));
                }
            }
            System.out.println("Gotova runda R");
            // Wait for round r to complete
            waitForFutures(futures);
            // Thread.sleep(2000);
            futures.clear();

            // Round r+1: Nodes decide if they join MIS based on neighbor random values
            for (Node node : nodes) {
                if (node.active.get()) {
                    futures.add(executor.submit(() -> {
                        boolean isSmallest = true;
                        List<Message> messages = node.receiveAllMessages();
                        for (Message message : messages) {
                            System.out.println("vrijednost " + Integer.valueOf(message.getContent()) + "a tip je" + Integer.valueOf(message.getContent()).getClass());
                            if (message.type == Message.Type.RANDOM && Integer.valueOf(message.getContent()) < node.randomValue) {
                                isSmallest = false;
                                break;
                            }
                        }
                        if (isSmallest) {
                            System.out.println("Node " + node.id + " joins the MIS");
                            node.inMIS.set(true);
                            node.active.set(false);
                            for (Node neighbor : node.neighbors) {
                                if (neighbor.active.get()) {
                                    node.sendMessage(neighbor, new Message(Message.Type.SELECTED, "1", node, neighbor));
                                }
                            }
                        } else {
                            for (Node neighbor : node.neighbors) {
                                if (neighbor.active.get()) {
                                    node.sendMessage(neighbor, new Message(Message.Type.SELECTED, "0", node, neighbor));
                                }
                            }
                        }
                    }));
                }
            }
            System.out.println("Gotova runda R+1");

            // Wait for round r+1 to complete
            waitForFutures(futures);
            futures.clear();

            // Round r+2: Nodes process elimination messages
            for (Node node : nodes) {
                if (node.active.get()) {
                    futures.add(executor.submit(() -> {
                        boolean neighborInMIS = false;
                        List<Message> messages = node.receiveAllMessages();
                        for (Message message : messages) {
                            if (message.type == Message.Type.SELECTED && "1".equals(message.getContent())) {
                                neighborInMIS = true;
                                break;
                            }
                        }
                        if (neighborInMIS) {
                            System.out.println("Node " + node.id + " deactivates itself due to neighbor in MIS");
                            node.active.set(false);
                        }
                    }));
                }
            }

            // Wait for round r+2 to complete
            System.out.println("Gotova runda R+2");

            waitForFutures(futures);
            futures.clear();

            if (!hasActiveNodes.get()) {
                break;
            }
        }

        executor.shutdown();
    }

    private static void waitForFutures(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}