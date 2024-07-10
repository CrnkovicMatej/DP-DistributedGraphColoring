package com.project.utils;

import com.project.network.Node;
import com.project.network.Network;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

public class NetworkLoader {
    private String nodesFilePath;
    private String linksFilePath;

    public NetworkLoader(String nodesFilePath, String linksFilePath) {
        this.nodesFilePath = nodesFilePath;
        this.linksFilePath = linksFilePath;
    }

    public Network loadTopology() throws IOException {
        Network network = new Network();
        loadNodes(network);
        loadLinks(network);
        return network;
    }

    private void loadNodes(Network network) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(nodesFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals("id")) {
                    int id = Integer.parseInt(line.trim());
                    Node node = new Node(id);
                    network.addNode(node);
                }
            }
        }
    }

    private void loadLinks(Network network) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(linksFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals("source,destination")) {
                    String[] parts = line.split(",");
                    int sourceId = Integer.parseInt(parts[0].trim());
                    int destinationId = Integer.parseInt(parts[1].trim());

                    Optional<Node> sourceNode = network.getNodeById(sourceId);
                    Optional<Node> destinationNode = network.getNodeById(destinationId);

                    if (sourceNode.isPresent() && destinationNode.isPresent()) {
                        network.connectNodes(sourceNode.get(), destinationNode.get());
                    }
                }
            }
        }
    }
    public boolean validateTopology(Network network) {
        return network.isConnected();
    }
}
