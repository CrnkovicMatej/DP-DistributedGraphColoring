package com.project.network;

import java.util.ArrayList;
import java.util.List;

public class Node implements Runnable {
    private int id;
    private List<Node> neighbors;
    private List<Message> messageQueue;
    private int color;
    private boolean colored;

    public Node(int id) {
        this.id = id;
        this.neighbors = new ArrayList<>();
        this.messageQueue = new ArrayList<>();
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
        while (true) {
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
        while (!messageQueue.isEmpty()) {
            Message message = messageQueue.remove(0);
            System.out.println("Node " + id + " processed message: " + message.getContent());
        }
    }
}
