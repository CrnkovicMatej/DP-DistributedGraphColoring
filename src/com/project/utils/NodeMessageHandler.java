package com.project.utils;

import com.project.network.Message;
import com.project.network.Node;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

public class NodeMessageHandler {
    private final Node node;
    private final Semaphore messageSemaphore;
    private final Queue<Message> messageQueue;

    public NodeMessageHandler(Node node, Semaphore messageSemaphore, Queue<Message> messageQueue) {
        this.node = node;
        this.messageSemaphore = messageSemaphore;
        this.messageQueue = messageQueue;
    }

    public void flush()
    {
        messageQueue.clear();
    }

    public void sendMessage(Node target, Message message) {
        if (node.getNeighbors().contains(target)) {
            target.getMessageHandler().receiveMessage(message);
        }
    }

    public CompletableFuture<Void> sendMessageAsync(Node target, Message message) {
        if (node.getNeighbors().contains(target)) {
            return CompletableFuture.runAsync(() -> target.getMessageHandler().receiveMessage(message));
        } else {
            return CompletableFuture.completedFuture(null); // Return completed future if target is not a neighbor
        }
    }

    public void receiveMessage(Message message) {
        messageQueue.offer(message);
        messageSemaphore.release();
    }
    public List<Message> receiveAllMessages() {
        List<Message> messages = new ArrayList<>();
        Map<Node, Integer> receivedCounts = new HashMap<>();
        Set<Node> neighbors = node.getNeighbors();

        while (receivedCounts.size() < neighbors.size()) {
            try {
                messageSemaphore.acquire();
                processMessages(messages, receivedCounts, neighbors);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return messages;
    }

    private void processMessages(List<Message> messages, Map<Node, Integer> receivedCounts, Set<Node> neighbors) {
        Message message;
        while ((message = messageQueue.poll()) != null) {
            Node sender = message.getSender();
            receivedCounts.put(sender, receivedCounts.getOrDefault(sender, 0) + 1);

            if (receivedCounts.get(sender) == 1) {
                messages.add(message);
            } else {
            }
        }
    }

    public void waitForNeighboursMessage(Node neighbor, int current_round, Message.Type messageType, BiConsumer<Message, Node> messageProcessor) {
        while (true) {
            try {
                if (processMessageQueue(neighbor, current_round, messageType, messageProcessor)) {
                    messageSemaphore.acquire();
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private boolean processMessageQueue(Node neighbor, int current_round, Message.Type messageType, BiConsumer<Message, Node> messageProcessor) {
        Message message;
        while ((message = messageQueue.peek()) != null) {
            if (message.getSender() == neighbor && message.getRound() == current_round && message.getType() == messageType) {
                messageProcessor.accept(message, neighbor);
                return true;
            } else {
                messageQueue.offer(messageQueue.poll());
                messageSemaphore.release();
            }
        }
        return false;
    }

    public void waitForNeighboursRandom(Node neighbor) {
        waitForNeighboursMessage(neighbor, 0, Message.Type.RANDOM, (message, sender) -> {
            node.randomValues.put(sender.getId(), Integer.parseInt(message.getContent()));
        });
    }

    public void waitForNeighboursColor(Node neighbor, int current_round) {
        waitForNeighboursMessage(neighbor, current_round, Message.Type.COLOR, (message, sender) -> {
            node.receivedColors.put(sender.getId(), Integer.parseInt(message.getContent()));
        });
    }

    public void waitForNeighboursSelected(Node neighbor, int current_round) {
        waitForNeighboursMessage(neighbor, current_round, Message.Type.SELECTED, (message, sender) -> {
            node.selected.put(sender.getId(), Boolean.parseBoolean(message.getContent()));
        });
    }

    public void waitForNeighboursEliminated(Node neighbor) {
        waitForNeighboursMessage(neighbor, 0, Message.Type.ELIMINATED, (message, sender) -> {
            node.eliminated.put(sender.getId(), Boolean.parseBoolean(message.getContent()));
        });
    }
}
