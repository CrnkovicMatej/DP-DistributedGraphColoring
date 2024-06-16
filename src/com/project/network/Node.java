package com.project.network;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private int id;
    private List<Node> neighbors;

    public Node(int id) {
        this.id = id;
        this.neighbors = new ArrayList<>();
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

    public void sendMessage(Node target, Message message) {
        if (neighbors.contains(target)) {
            target.receiveMessage(message);
        }
    }

    public void receiveMessage(Message message) {
        System.out.println("Node " + id + " received message: " + message.getContent());
    }
}