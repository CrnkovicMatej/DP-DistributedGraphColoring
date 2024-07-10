package com.project.network.algorithms;

import com.project.network.Message;
import com.project.network.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class LubyMis implements GraphAlgorithm {
    List<Node> nodes;
    int numThreads;

    public LubyMis(List<Node> nodes, int numThreads){
        this.nodes = nodes;
        this.numThreads = numThreads;
    }

    @Override
    public void execute()
    {
        System.out.println("Pokretanje Luby MIS algoritma");
        ExecutorService executor = newFixedThreadPool(numThreads);
        Random random = new Random();

        List<Future<?>> futures = new ArrayList<>();

        while (true) {
            AtomicBoolean hasActiveNodes = new AtomicBoolean(false);

            initNodesForExecution();

            // Round r: Nodes generate random values and send to neighbors
            generateRandomValuesAndSendMessages(executor, random, futures, hasActiveNodes);

            // Process received random values
            processReceivedRandomValues(executor, futures);

            //end of round r
            waitForFutures(futures);

            // Round r+1: Nodes decide if they join MIS based on neighbor random values
            decideMISMembership(executor, futures);

            recieveSelectedMessages(executor, futures);

            // end of round r+1
            waitForFutures(futures);

            // Round r+2: Nodes process elimination messages
            processSelectedNeighbours(executor, futures);

            recieveEliminatedMessages(executor, futures);

            processEliminationMessages(executor, futures);

            // end of round r+2
            waitForFutures(futures);

            if (!hasActiveNodes.get()) {
                break;
            }
        }

        executor.shutdown();
    }

    private void initNodesForExecution()
    {
        nodes.forEach(node -> node.getNeighbors().forEach(node::addCommunicator));
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

    private void generateRandomValuesAndSendMessages(ExecutorService executor, Random random, List<Future<?>> futures, AtomicBoolean hasActiveNodes) {
        for (Node node : nodes) {
            if (node.isActive()) {
                hasActiveNodes.set(true);
                futures.add(executor.submit(() -> {
                    node.randomValue = random.nextInt(Integer.MAX_VALUE);
                    System.out.println("Čvor " + node.id + " generirao slučajnu vrijednost: " + node.randomValue);
                    for (Node communication : node.getCom_with()) {
                        node.sendMessage(communication, new Message(Message.Type.RANDOM, String.valueOf(node.randomValue), node, communication));
                    }
                }));
            }
        }
    }

    private void processReceivedRandomValues(ExecutorService executor, List<Future<?>> futures) {
        for (Node node : nodes) {
            if (node.isActive()) {
                futures.add(executor.submit(() -> {
                    node.isSmallest = true;
                    List<Message> messages = node.receiveAllCommMessages();
                    for (Message message : messages) {
                        System.out.println("Vrijednost: " + Integer.valueOf(message.getContent()) + ", tip: " + message.getType());
                        if (message.getType() == Message.Type.RANDOM && Integer.valueOf(message.getContent()) < node.randomValue) {
                            node.isSmallest = false;
                            break;
                        }
                    }
                }));
            }
        }
    }

    private void decideMISMembership(ExecutorService executor, List<Future<?>> futures) {
        for (Node node : nodes) {
            if (node.isActive()) {
                futures.add(executor.submit(() -> {
                    if (node.isSmallest) {
                        System.out.println("Čvor " + node.id + " pridružuje se MIS");
                        node.setInMIS(true);
                        node.setActive(false);
                        for (Node neighbor : node.getCom_with()) {
                            node.sendMessage(neighbor, new Message(Message.Type.SELECTED, "1", node, neighbor));
                        }
                    } else {
                        for (Node neighbor : node.getCom_with()) {
                            node.sendMessage(neighbor, new Message(Message.Type.SELECTED, "0", node, neighbor));
                        }
                    }
                }));
            }
        }
    }

    private void recieveSelectedMessages(ExecutorService executor, List<Future<?>> futures) {
        for (Node node : nodes) {
            if (node.isActive()) {
                futures.add(executor.submit(() -> {
                    node.setMessageBuffer(node.receiveAllCommMessages());
                }));
            }
        }
    }

    private void processSelectedNeighbours(ExecutorService executor, List<Future<?>> futures) {
        for (Node node : nodes) {
            if (node.isActive()) {
                futures.add(executor.submit(() -> {
                    for (Message message : node.getMessageBuffer()) {
                        if (message.getType() == Message.Type.SELECTED && message.getContent().equals("1")) {
                            System.out.println("Čvor " + node.id + " deaktivira se zbog susjeda u MIS");
                            node.setActive(false);
                            for (Node neighbor : node.getCom_with()) {
                                node.sendMessage(neighbor, new Message(Message.Type.ELIMINATED, "1", node, neighbor));
                            }
                            break;
                        }
                    }
                    if(node.isActive()){
                        for (Node neighbor : node.getCom_with()) {
                            node.sendMessage(neighbor, new Message(Message.Type.ELIMINATED, "0", node, neighbor));
                        }
                    }
                }));
            }
        }
        waitForFutures(futures);
    }

    private void recieveEliminatedMessages(ExecutorService executor, List<Future<?>> futures) {
        for (Node node : nodes) {
            if (node.isActive()) {
                futures.add(executor.submit(() -> {
                    node.setMessageBuffer(node.receiveAllCommMessages());
                }));
            }
        }
    }

    private void processEliminationMessages(ExecutorService executor, List<Future<?>> futures) {
        for (Node node : nodes) {
            if (node.isActive()) {
                futures.add(executor.submit(() -> {
                    List<Message> messages = node.getMessageBuffer();
                    for (Message message : messages) {
                        if (message.getType() == Message.Type.ELIMINATED && "1".equals(message.getContent())) {
                            node.removeCommunicator(message.getSender());
                        }
                    }
                    if(node.getCom_with().isEmpty())
                    {
                        node.setInMIS(true);
                        node.setActive(false);
                    }
                }));
            }
        }
    }
}