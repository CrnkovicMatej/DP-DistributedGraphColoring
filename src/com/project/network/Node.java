package com.project.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Node implements Runnable {
    private int id;
    private List<Node> neighbors;
    private List<Message> messageQueue;
    private Map<Integer, Integer> receivedColors; // Maps neighbor ID to color
    private int color;
    private boolean colored;
    private String algorithm;
    private int maxColors;

    public Node(int id, String algorithm, int maxColors) {
        this.id = id;
        this.neighbors = new ArrayList<>();
        this.messageQueue = new ArrayList<>();
        this.receivedColors = new HashMap<>();
        this.color = id; // 0 means uncolored
        this.colored = false;
        this.algorithm = algorithm;
        this.maxColors = maxColors;
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
        System.out.println("Node " + id + " has " + messageQueue.size() + " messages in the queue.");
        System.out.println("Node " + id + " received message: " + message.getContent());
        processMessages(); // Process messages immediately after receiving
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
}
