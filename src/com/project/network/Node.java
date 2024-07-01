package com.project.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node implements Runnable {
    private int id;
    private List<Node> neighbors;
    private List<Message> messageQueue;
    private int color;
    private boolean colored;
    private String algorithm;
    private int maxColors;

    public Node(int id, String algorithm, int maxColors) {
        this.id = id;
        this.neighbors = new ArrayList<>();
        this.messageQueue = new ArrayList<>();
        this.color = 0; // 0 means uncolored
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
            target.receiveMessage(message);
        }
    }

    public synchronized void receiveMessage(Message message) {
        messageQueue.add(message);
        System.out.println("Node " + id + " received message: " + message.getContent());
    }

    @Override
    public void run() {
        System.out.println("Node " + id + " started.");
        while (!Thread.currentThread().isInterrupted()) {
            processMessages();
            try {
                Thread.sleep(1000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private synchronized void processMessages() {
        if (messageQueue.isEmpty()) {
            return;
        }

        if ("color".equalsIgnoreCase(algorithm)) {
            runColoringAlgorithm();
        } else if ("maximal".equalsIgnoreCase(algorithm)) {
            runMaximalIndependentSetAlgorithm();
        }
    }

    private void runColoringAlgorithm() {
        Set<Integer> usedColors = new HashSet<>();
        
        while (!messageQueue.isEmpty()) {
            Message message = messageQueue.remove(0);
            System.out.println("Node " + id + " processed message: " + message.getContent());

            String[] parts = message.getContent().split(" ");
            int neighborColor = Integer.parseInt(parts[parts.length - 1]);
            System.out.println("Neighbor color is: " + neighborColor);
            usedColors.add(neighborColor);
        }

        if (!colored) {
            System.out.println("COLORED IS FALSE!");
            int newColor = 1;
            while (usedColors.contains(newColor)) {
                newColor++;
            }
            color = newColor;
            colored = true;

            System.out.println("Node " + id + " colored with color: " + color);

            for (Node neighbor : neighbors) {
                Message message = new Message("Color of node " + id + " is " + color, this, neighbor);
                sendMessage(neighbor, message);
            }
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
