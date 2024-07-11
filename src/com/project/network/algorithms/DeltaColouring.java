package com.project.network.algorithms;

import com.project.network.Message;
import com.project.network.Node;

import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class DeltaColouring implements GraphAlgorithm {
    List<Node> nodes;
    int numThreads;

    int degree;


    public DeltaColouring(List<Node> nodes, int numThreads, int degree){
        this.nodes = nodes;
        this.numThreads = numThreads;
        this.degree = degree;
    }

    @Override
    public void execute()
    {
        System.out.println("Starting the (degree+1)-coloring algorithm");
        ExecutorService executor = newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (Node node : nodes) {
            futures.add(executor.submit(() -> {
                for (Node neighbour : node.getNeighbors()) {
                    node.getMessageHandler().sendMessage(neighbour, new Message(Message.Type.INIT, String.valueOf(node.getColor()), node, neighbour));
                }

            }));

        }
        waitForFutures(futures);

        for (Node node : nodes) {
            futures.add(executor.submit(() -> {
                List<Message> messages = node.getMessageHandler().receiveAllMessages();
                for (Message message : messages) {
                    if (message.getType() == Message.Type.INIT ) {
                        int senderId = message.getSender().getId();
                        int color = Integer.parseInt(message.getContent());
                        node.receivedColors.put(senderId, color);
                    }
                }
            }));
        }
        waitForFutures(futures);


        // Asynchronous rounds for each node
        for (Node node : nodes) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int current_round = degree + 2; current_round <= nodes.size()+1; current_round++) {
                    if (node.getColor() == current_round) {
                        int newColor = findSmallestAvailableColor(node);
                        node.setColor(newColor);
                    }
                    for (Node neighbor : node.getNeighbors()) {
                        node.getMessageHandler().sendMessageAsync(neighbor, new Message(Message.Type.COLOR, String.valueOf(node.getColor()), current_round, node, neighbor));
                    }
                    for (Node neighbor : node.getNeighbors())
                    {
                        node.getMessageHandler().waitForNeighboursColor(neighbor, current_round);
                    }
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        executor.shutdown();
    }

    private void waitForFutures(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        futures.clear();
    }

    private int findSmallestAvailableColor(Node node) {
        Set<Integer> usedColors = new HashSet<>(node.receivedColors.values());
        for (int c = 1; c <= degree + 1; c++) {
            if (!usedColors.contains(c)) {
                return c;
            }
        }
        return -1; // should never happen if degree is correct
    }
}
