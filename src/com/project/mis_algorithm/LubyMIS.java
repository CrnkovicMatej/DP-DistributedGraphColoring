package com.project.mis_algorithm;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class LubyMIS {

    public static class Node {
        int id;
        List<Node> neighbors;
        AtomicBoolean inMIS;
        AtomicBoolean active;
        int randomValue;
        BlockingQueue<Message> messageQueue;

        Node(int id) {
            this.id = id;
            this.neighbors = new ArrayList<>();
            this.inMIS = new AtomicBoolean(false);
            this.active = new AtomicBoolean(true);
            this.messageQueue = new LinkedBlockingQueue<>();
        }

        void addNeighbor(Node neighbor) {
            neighbors.add(neighbor);
        }

        void reset() {
            inMIS.set(false);
            active.set(true);
        }

        void sendMessage(Message message, Node recipient) {
            try {
                recipient.messageQueue.put(message);
                System.out.println("Node " + this.id + " sends " + message.type + "(" + message.value + ") to Node " + recipient.id);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        List<Message> receiveAllMessages() {
            List<Message> messages = new ArrayList<>();
            messageQueue.drainTo(messages);
            for (Message message : messages) {
                System.out.println("Node " + this.id + " received " + message.type + "(" + message.value + ")");
            }
            return messages;
        }

        public int getId() {
            return id;
        }

        public AtomicBoolean getInMIS() {
            return inMIS;
        }
    }

    static class Message {
        enum Type { RANDOM, SELECTED }
        Type type;
        int value;

        Message(Type type, int value) {
            this.type = type;
            this.value = value;
        }
    }

    public static void lubyMIS(List<Node> nodes, int numThreads) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        Random random = new Random();

        while (true) {
            List<Future<?>> futures = new ArrayList<>();
            AtomicBoolean hasActiveNodes = new AtomicBoolean(false);

            // Round r: Nodes generate random values and send to neighbors
            for (Node node : nodes) {
                if (node.active.get()) {
                    hasActiveNodes.set(true);
                    futures.add(executor.submit(() -> {
                        node.randomValue = random.nextInt(Integer.MAX_VALUE);
                        System.out.println("Node " + node.id + " generated random value: " + node.randomValue);
                        for (Node neighbor : node.neighbors) {
                            if (neighbor.active.get()) {
                                node.sendMessage(new Message(Message.Type.RANDOM, node.randomValue), neighbor);
                            }
                        }
                    }));
                }
            }

            // Wait for round r to complete
            waitForFutures(futures);
            futures.clear();

            // Round r+1: Nodes decide if they join MIS based on neighbor random values
            for (Node node : nodes) {
                if (node.active.get()) {
                    futures.add(executor.submit(() -> {
                        boolean isSmallest = true;
                        List<Message> messages = node.receiveAllMessages();
                        for (Message message : messages) {
                            if (message.type == Message.Type.RANDOM && message.value < node.randomValue) {
                                isSmallest = false;
                                break;
                            }
                        }
                        if (isSmallest) {
                            System.out.println("Node " + node.id + " joins the MIS");
                            node.inMIS.set(true);
                            node.active.set(false);
                            for (Node neighbor : node.neighbors) {
                                if (neighbor.active.get()) {
                                    node.sendMessage(new Message(Message.Type.SELECTED, 1), neighbor);
                                }
                            }
                        } else {
                            for (Node neighbor : node.neighbors) {
                                if (neighbor.active.get()) {
                                    node.sendMessage(new Message(Message.Type.SELECTED, 0), neighbor);
                                }
                            }
                        }
                    }));
                }
            }

            // Wait for round r+1 to complete
            waitForFutures(futures);
            futures.clear();

            // Round r+2: Nodes process elimination messages
            for (Node node : nodes) {
                if (node.active.get()) {
                    futures.add(executor.submit(() -> {
                        boolean neighborInMIS = false;
                        List<Message> messages = node.receiveAllMessages();
                        for (Message message : messages) {
                            if (message.type == Message.Type.SELECTED && message.value == 1) {
                                neighborInMIS = true;
                                break;
                            }
                        }
                        if (neighborInMIS) {
                            System.out.println("Node " + node.id + " deactivates itself due to neighbor in MIS");
                            node.active.set(false);
                        }
                    }));
                }
            }

            // Wait for round r+2 to complete
            waitForFutures(futures);

            if (!hasActiveNodes.get()) {
                break;
            }
        }

        executor.shutdown();
    }

    private static void waitForFutures(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Node> readNodes(String filename) throws IOException {
        List<Node> nodes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            // Skip the header
            br.readLine();
            while ((line = br.readLine()) != null) {
                int id = Integer.parseInt(line.trim());
                nodes.add(new Node(id));
            }
        }
        return nodes;
    }

    public static void readLinks(String filename, List<Node> nodes) throws IOException {
        Map<Integer, Node> nodeMap = new HashMap<>();
        for (Node node : nodes) {
            nodeMap.put(node.id, node);
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            // Skip the header
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int sourceId = Integer.parseInt(parts[0].trim());
                int destinationId = Integer.parseInt(parts[1].trim());
                Node sourceNode = nodeMap.get(sourceId);
                Node destinationNode = nodeMap.get(destinationId);
                sourceNode.addNeighbor(destinationNode);
                destinationNode.addNeighbor(sourceNode); // Add node to undirected graph
            }
        }
    }
}
