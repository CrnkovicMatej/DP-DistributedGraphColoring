package com.project.network.algorithms;

import com.project.network.Message;
import com.project.network.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class ColorMis implements GraphAlgorithm {
    List<Node> nodes;
    int numThreads;

    int maxColors;

    public ColorMis(List<Node> nodes, int numThreads, int maxColors){
        this.nodes = nodes;
        this.numThreads = numThreads;
        this.maxColors = maxColors;
    }

    @Override
    public void execute() {
        System.out.println("Starting the m-coloring to MIS algorithm");
        nodes.forEach(Node::initSelected);

        ExecutorService executor = newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();
        // Asynchronous rounds for each node
        for (Node node : nodes) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int current_round = 1; current_round <= maxColors; current_round++) {
                    if (node.getColor() == current_round && node.areAllNeighborsSelectedFalse()) {
                        node.setOwnSelectionTrue();
                    }
                    for (Node neighbor : node.getNeighbors()) {
                        node.getMessageHandler().sendMessageAsync(neighbor, new Message(Message.Type.SELECTED, String.valueOf(node.getOwnSelection()), current_round, node, neighbor));
                    }
                    for (Node neighbor : node.getNeighbors())
                    {
                        node.getMessageHandler().waitForNeighboursSelected(neighbor, current_round);
                    }
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
    }
}
