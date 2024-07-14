package com.project;

import com.project.network.*;
import com.project.network.algorithms.ColorMis;
import com.project.network.algorithms.DeltaColouring;
import com.project.network.algorithms.GraphAlgorithm;
import com.project.network.algorithms.LubyMis;
import com.project.utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.io.IOException;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 3) {
            System.out.println("Usage: java com.project.Main <nodes_file_path> <links_file_path> <algorithm> ");
            return;
        }

        String nodesFilePath = args[0];
        String linksFilePath = args[1];
        String algorithm = args[2];

        NetworkLoader loader = new NetworkLoader(nodesFilePath, linksFilePath);
        Network network;

        try {
            network = loader.loadTopology();
        } catch (IOException e) {
            System.out.println("Error loading network topology: " + e.getMessage());
            return;
        }
        if (!loader.validateTopology(network)) {
            System.out.println("The provided network is not connected. Please make sure that the graph you've entered via nodes and links files has at least" +
                    " one path between each of its nodes.");
            return;
        }

        List<Node> nodes = network.getNodes();
        GraphAlgorithm graphAlgorithm;

        if ("maximal".equalsIgnoreCase(algorithm)) {
            graphAlgorithm = new LubyMis(nodes, nodes.size());
            executeAlgorithm(graphAlgorithm);
            network.printMisResults();
        } else if ("color".equalsIgnoreCase(algorithm)) {
            int degree = network.getNetworkDegree();
            graphAlgorithm = new DeltaColouring(nodes, nodes.size(), degree);
            executeAlgorithm(graphAlgorithm);
            network.printColors();
        } else if("colorMIS".equalsIgnoreCase(algorithm)){
            graphAlgorithm = new ColorMis(nodes, nodes.size(), nodes.size());
            executeAlgorithm(graphAlgorithm);
            network.printColorMisResults();
        } else {
            System.out.println("Unknown algorithm specified.");
        }
    }

    private static void executeAlgorithm(GraphAlgorithm algorithm) {
        long startTime = System.nanoTime();
        algorithm.execute();
        long endTime = System.nanoTime();
        long durationInMillis = (endTime - startTime) / 1_000_000;
        System.out.println("Execute method duration: " + durationInMillis + " ms");
    }
}
 