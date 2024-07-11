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
                    node.randomValue = random.nextInt(Integer.MAX_VALUE);
                    System.out.println("Čvor " + node.id + " generirao slučajnu vrijednost: " + node.randomValue);
                    for (Node communication : node.getCom_with()) {
                        node.sendMessage(communication, new Message(Message.Type.RANDOM, String.valueOf(node.randomValue), node, communication));
                    }
                    for (Node communication : node.getCom_with())
                    {
                        node.waitForNeighboursRandom(communication);
                    }
                }));
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
                        System.out.println("Čvor " + node.id + " pridružuje se MIS");
                        node.setInMIS(true);
                        node.setActive(false);
                        for (Node neighbor : node.getCom_with()) {
                            node.sendMessage(neighbor, new Message(Message.Type.SELECTED, "true", node, neighbor));
                        }
                        return;
                    } else {
                        for (Node neighbor : node.getCom_with()) {
                            node.sendMessage(neighbor, new Message(Message.Type.SELECTED, "false", node, neighbor));
                        }
                    }
                    for (Node communication : node.getCom_with())
                    {
                        node.waitForNeighboursSelected(communication,0);
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
                node.sendMessage(neighbor, new Message(Message.Type.ELIMINATED, messageContent, node, neighbor));
            }
        }
    }
    private void deactivateNode(Node node) {
        System.out.println("Čvor " + node.getId() + " deaktivira se zbog susjeda u MIS");
        node.setActive(false);
        sendEliminationMessages(node, "true");
    }
    private void waitForNeighboursEliminationMsg(Node node) {
        for (Node neighbor : node.getCom_with()) {
            if (neighbor.isActive()) {
                node.waitForNeighboursEliminated(neighbor);
            }
        }
    }

    private void joinMIS(Node node) {
        System.out.println("Čvor " + node.getId() + " pridružuje se MIS jer su mu komunikacijski kanali prazni");
        node.setInMIS(true);
        node.setActive(false);
    }
}