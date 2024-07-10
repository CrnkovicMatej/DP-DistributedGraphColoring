package com.project.network;

import java.util.ArrayList;
import java.util.List;

public class Network {
    private List<Node> nodes;

    public Network() {
        this.nodes = new ArrayList<>();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void addNode(Node node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
        }
    }

    public void connectNodes(Node node1, Node node2) {
        if (nodes.contains(node1) && nodes.contains(node2)) {
            node1.addNeighbor(node2);
            node2.addNeighbor(node1);
        }
    }

    public void start() {
        for (Node node : nodes) {
            Thread thread = new Thread(node);
            System.out.println("Starting thread for node " + node.getId());
            thread.start();
        }
    }

    public int getNetworkDegree()
    {
        return nodes.stream()
                .mapToInt(node -> node.getNeighbors().size())
                .max()
                .orElse(0);
    }


    public boolean isConnected() {
        if (nodes.isEmpty()) {
            return false;
        }

        boolean[] visited = new boolean[nodes.size()];
        dfs(nodes.get(0), visited);

        for (boolean nodeVisited : visited) {
            if (!nodeVisited) {
                return false;
            }
        }
        return true;
    }

    private void dfs(Node node, boolean[] visited) {
        // due to algo implementations Node ID's start with 1
        visited[node.getId()-1] = true;
        for (Node neighbor : node.getNeighbors()) {
            if (!visited[neighbor.getId()-1]) {
                dfs(neighbor, visited);
            }
        }
    }

    public void printColors() {
        for (Node node : nodes) {
            System.out.println("Node " + node.getId() + " ima boju " + node.getColor());
        }
    }

    public void printMisResults() {
        System.out.println("Maximal Independent Set (MIS) results:");
        for (Node node : nodes) {
            if (node.getInMIS().get()) {
                System.out.println("Node " + node.getId() + " is in the MIS.");
            }
        }
    }

    public void printColorMisResults() {
        System.out.println("Color Maximal Independent Set (MIS) results:");
        nodes.stream()
                .filter(Node::getOwnSelection)
                .map(Node::getId)
                .forEach(id -> System.out.println("Node " + id + " is in the MIS."));
    }
}
