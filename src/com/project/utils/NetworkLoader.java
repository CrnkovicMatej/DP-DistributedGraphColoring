package com.project.utils;

import com.project.network.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class NetworkLoader {
    private String nodesFilePath;
    private String linksFilePath;

    public NetworkLoader(String nodesFilePath, String linksFilePath) {
        this.nodesFilePath = nodesFilePath;
        this.linksFilePath = linksFilePath;
    }

    public Network loadTopology() throws IOException {
        Network network = new Network();

        // nodes loading
        try (BufferedReader reader = new BufferedReader(new FileReader(nodesFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals("id")) { // skip over the header line
                    int id = Integer.parseInt(line.trim());
                    Node node = new Node(id);
                    network.addNode(node);
                }
            }
        }

        // links loading
        try (BufferedReader reader = new BufferedReader(new FileReader(linksFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals("source,destination")) { // skip over the header line
                    String[] parts = line.split(",");
                    int sourceId = Integer.parseInt(parts[0].trim());
                    int destinationId = Integer.parseInt(parts[1].trim());

                    Node sourceNode = network.getNodes().stream().filter(n -> n.getId() == sourceId).findFirst().orElse(null);
                    Node destinationNode = network.getNodes().stream().filter(n -> n.getId() == destinationId).findFirst().orElse(null);

                    if (sourceNode != null && destinationNode != null) {
                        network.connectNodes(sourceNode, destinationNode);
                    }
                }
            }
        }

        return network;
    }

    public boolean validateTopology(Network network) {
        return network.isConnected();
    }
}
