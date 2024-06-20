package com.project;

import com.project.network.*;
import com.project.utils.*;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java com.project.Main <nodes-file-path> <links-file-path>");
            return;
        }

        String nodesFilePath = args[0];
        String linksFilePath = args[1];
        NetworkLoader loader = new NetworkLoader(nodesFilePath, linksFilePath);

        try {
            Network network = loader.loadTopology();

            if (loader.validateTopology(network)) {
                System.out.println("The network is connected.");

                // primitive example
                // Simulate message sending
                Node node1 = network.getNodes().get(0);
                Node node2 = network.getNodes().get(1);

                Message message = new Message("Hello, World!", node1, node2);
                node1.sendMessage(node2, message);
            } else {
                System.out.println("The network is not connected.");
            }
        } catch (IOException e) {
            System.err.println("Failed to load topology: " + e.getMessage());
        }
    }
}