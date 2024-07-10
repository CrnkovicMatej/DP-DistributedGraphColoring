package com.project.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        System.out.println("Results of the graph coloring:");
        nodes.forEach(node -> System.out.printf("Node %d has the color %d%n", node.getId(), node.getColor()));
    }

    public void printMisResults() {
        System.out.println("Maximal Independent Set (MIS) results:");
        nodes.stream()
                .filter(node -> node.getInMIS().get())
                .map(Node::getId)
                .forEach(id -> System.out.println("Node " + id + " is in the MIS."));
    }

    public void printColorMisResults() {
        System.out.println("Color Maximal Independent Set (MIS) results:");
        nodes.stream()
                .filter(Node::getOwnSelection)
                .map(Node::getId)
                .forEach(id -> System.out.println("Node " + id + " is in the MIS."));
    }

    public Optional<Node> getNodeById(int id) {
        return nodes.stream()
                .filter(node -> node.getId() == id)
                .findFirst();
    }
}
