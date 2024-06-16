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
        visited[node.getId()] = true;
        for (Node neighbor : node.getNeighbors()) {
            if (!visited[neighbor.getId()]) {
                dfs(neighbor, visited);
            }
        }
    }
}