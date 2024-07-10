package com.project;

import com.project.network.*;
import com.project.network.algorithms.LubyMis;
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
        if (!loader.validateTopology(network)) {
            System.out.println("The network is not connected.");
            return;
        }

        if ("maximal".equals(algorithm)) { 
            List<Node> nodes = network.getNodes();
            LubyMis alg_luby = new LubyMis(nodes, nodes.size());

            long startTime = System.nanoTime();
            alg_luby.execute();
            long endTime = System.nanoTime();
            long durationInMillis = (endTime - startTime) / 1_000_000;
            System.out.println("Execute method duration: " + durationInMillis + " ms");
            System.out.println("Maximal Independent Set (MIS) results:");
            for (Node node : network.getNodes()) {
                if (node.getInMIS().get()) {
                    System.out.println("Node " + node.getId() + " is in the MIS.");
                }
            }
        } else {

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
 