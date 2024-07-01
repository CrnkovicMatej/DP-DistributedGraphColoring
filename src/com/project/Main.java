package com.project;

import com.project.network.Network;
import com.project.network.Node;
import com.project.network.Message;
import com.project.utils.NetworkLoader;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java com.project.Main <nodes_file_path> <links_file_path>");
            return;
        }

        String nodesFilePath = args[0];
        String linksFilePath = args[1];

        NetworkLoader loader = new NetworkLoader(nodesFilePath, linksFilePath);
        Network network;

        try {
            network = loader.loadTopology();
        } catch (IOException e) {
            System.out.println("Error loading network topology: " + e.getMessage());
            return;
        }

        if (!loader.validateTopology(network)) {
            System.out.println("The network is not fully connected.");
            return;
        }

        network.start();
        System.out.println("Network has started.");

        for (Node node : network.getNodes()) {
            for (Node neighbor : node.getNeighbors()) {
                Message message = new Message("Color of node " + node.getId() + " is " + node.getColor(), node, neighbor);
                node.sendMessage(neighbor, message);
            }
        }
    }
}
