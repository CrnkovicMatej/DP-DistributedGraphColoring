package com.project.network.algorithms;

import com.project.network.Message;
import com.project.network.Node;
import com.project.utils.NodeMessageHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
        System.out.println("Starting the Luby maximal independent set algorithm");
        ExecutorService executor = newFixedThreadPool(numThreads);
        Random random = new Random();

        List<Future<?>> futures = new ArrayList<>();

        initNodesForExecution();
        AtomicBoolean hasActiveNodes = new AtomicBoolean(false);
        while (true) {
            hasActiveNodes.set(false);
            // Round r: Nodes generate random values and send to neighbors
            processFirstRound(executor, random, futures, hasActiveNodes);

            //end of round r
            waitForFutures(futures);

            // Round r+1: Nodes decide if they join MIS based on neighbor random values
            processSecondRound(executor, futures);


            // end of round r+1
            waitForFutures(futures);

            // Round r+2: Nodes process elimination messages
            processThirdRound(executor, futures);


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
        nodes.forEach(Node::initEliminated);
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

    private void processFirstRound(ExecutorService executor, Random random, List<Future<?>> futures, AtomicBoolean hasActiveNodes) {
        for (Node node : nodes) {
            if (node.isActive()) {
                hasActiveNodes.set(true);
                futures.add(executor.submit(() -> {
                    try {
                        int randomValue = random.nextInt(Integer.MAX_VALUE);
                        node.setRandomValue(randomValue);
                        Set<Node> communications = node.getCom_with();
                        NodeMessageHandler messageHandler = node.getMessageHandler();
                        for (Node communication : communications) {
                            messageHandler.sendMessage(communication, new Message(Message.Type.RANDOM, String.valueOf(randomValue), node, communication));
                        }
                        node.randomValues.clear();
                        for (Node communication : communications) {
                            messageHandler.waitForNeighboursRandom(communication);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }}));
            }
        }
    }

    private void processSecondRound(ExecutorService executor, List<Future<?>> futures) {
        for (Node node : nodes) {
            if (node.isActive()) {
                futures.add(executor.submit(() -> {
                    node.isSmallest = true;
                    for (int value : node.randomValues.values()) {
                        if (node.randomValue >= value) {
                            node.isSmallest = false;
                            break;
                        }
                    }
                    if (node.isSmallest) {
                        node.setInMIS(true);
                        node.setActive(false);
                        for (Node neighbor : node.getCom_with()) {
                            node.getMessageHandler().sendMessage(neighbor, new Message(Message.Type.SELECTED, "true", node, neighbor));
                        }
                        System.out.println("Process " + node.getId() + " joins MIS because it has smallest random number.");
                        return;
                    } else {
                        for (Node neighbor : node.getCom_with()) {
                            node.getMessageHandler().sendMessage(neighbor, new Message(Message.Type.SELECTED, "false", node, neighbor));
                        }
                    }
                    for (Node communication : node.getCom_with())
                    {
                        node.getMessageHandler().waitForNeighboursSelected(communication,0);
                    }
                }));
            }
        }
    }
    private void processThirdRound(ExecutorService executor, List<Future<?>> futures) {
        for (Node node : nodes) {
            if (node.isActive()) {
                futures.add(executor.submit(() -> {
                    handleNodeActivity(node);
                }));
            }
        }
    }

    private void handleNodeActivity(Node node) {
        boolean noNeighboursSelected = node.getCom_with().stream()
                .noneMatch(neighbor -> node.selected.get(neighbor.getId()));

        if (noNeighboursSelected) {
            sendEliminationMessages(node, "false");
        } else {
            deactivateNode(node);
            return;
        }

        waitForNeighboursEliminationMsg(node);
        node.removeEliminatedNeighbors();

        if (node.getCom_with().isEmpty()) {
            joinMIS(node);
        }
    }
    private void sendEliminationMessages(Node node, String messageContent) {
        for (Node neighbor : node.getCom_with()) {
            if (neighbor.isActive()) {
                node.getMessageHandler().sendMessage(neighbor, new Message(Message.Type.ELIMINATED, messageContent, node, neighbor));
            }
        }
    }
    private void deactivateNode(Node node) {
        System.out.println("Process " + node.getId() + " deactivates because one of the neighbours is in MIS.");
        node.setActive(false);
        sendEliminationMessages(node, "true");
    }
    private void waitForNeighboursEliminationMsg(Node node) {
        for (Node neighbor : node.getCom_with()) {
            node.getMessageHandler().waitForNeighboursEliminated(neighbor);
        }
    }

    private void joinMIS(Node node) {
        System.out.println("Process " + node.getId() + " joins MIS because it no longer has active communicators.");
        node.setInMIS(true);
        node.setActive(false);
    }
}