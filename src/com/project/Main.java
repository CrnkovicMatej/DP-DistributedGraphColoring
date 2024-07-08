package com.project;

import com.project.network.*;
import com.project.utils.*;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 3) {
            System.out.println("Usage: java com.project.Main <nodes_file_path> <links_file_path> <algorithm> [<maxColors>]");
            return;
        }

        String nodesFilePath = args[0];
        String linksFilePath = args[1];
        String algorithm = args[2];
        int maxColors = args.length >= 4 ? Integer.parseInt(args[3]) : Integer.MAX_VALUE;

        NetworkLoader loader = new NetworkLoader(nodesFilePath, linksFilePath);
        Network network;

        try {
            network = loader.loadTopology();
        } catch (IOException e) {
            System.out.println("Error loading network topology: " + e.getMessage());
            return;
        }

        if ("maximal".equals(algorithm)) { 

                List<Node> nodes = new ArrayList<>();
                for (Node node : network.getNodes()) {
                    Node newNode = new Node(node.getId(), algorithm, 0);
                    newNode.neighbors.addAll(node.getNeighbors());
                    network.getNodes().set(network.getNodes().indexOf(node), newNode);
                    nodes.add(newNode);
                }
                // Execute Luby's algorithm
                Node.lubyMIS(nodes, 4);

                // Print the result
                System.out.println("Maximal Independent Set (MIS) results:");
                for (Node node : nodes) {
                    if (node.getInMIS().get()) {
                        System.out.println("Node " + node.getId() + " is in the MIS.");
                    }
                }
        } else {

            if (!loader.validateTopology(network)) {
                System.out.println("The network is not fully connected.");
                return;
            }

            for (Node node : network.getNodes()) {
                // Reinitialize nodes with the algorithm and maxColors
                Node newNode = new Node(node.getId(), algorithm, maxColors);
                newNode.getNeighbors().addAll(node.getNeighbors());
                network.getNodes().set(network.getNodes().indexOf(node), newNode);
            }

            network.start();
            System.out.println("Network has started.");
        }
    }
}
 