package com.project.network;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Node  {
    public int id;
    private final Set<Node> neighbors = ConcurrentHashMap.newKeySet();
    public final Set<Node> com_with = ConcurrentHashMap.newKeySet();
    private final Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();

    private final Semaphore messageSemaphore = new Semaphore(0);

    private List<Message> tempMessagesBuffer = new ArrayList<Message>();
    private AtomicBoolean inMIS;
    private AtomicBoolean active;
    public int randomValue;
    public Map<Integer, Integer> receivedColors;
    public Map<Integer, Boolean> selected;
    private int color;
    public boolean isSmallest;

    public Node(int id) {
        this.id = id;
        this.receivedColors = new HashMap<>();
        this.selected = new HashMap<>();
        this.color = id;
        this.inMIS = new AtomicBoolean(false);
        this.active = new AtomicBoolean(true);
    }

    public void initSelected()
    {
        this.selected = this.getNeighbors().stream()
                .collect(Collectors.toMap(Node::getId, neighbor -> false));
        this.selected.put(this.getId(), false);
    }
    public boolean areAllNeighborsSelectedFalse() {
        return this.getNeighbors().stream()
                .noneMatch(neighbor -> selected.get(neighbor.getId()));
    }

    public void setOwnSelectionTrue() {
        this.selected.put(this.getId(), true);
    }
    public boolean getOwnSelection() {
        return this.selected.get(this.getId());
    }
    public boolean isInMIS() {
        return inMIS.get();
    }
    public void setInMIS(boolean inMIS) {
        this.inMIS.set(inMIS);
    }

    public boolean isActive() {
        return active.get();
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }
    public int getId() {
        return id;
    }

    public Set<Node> getNeighbors() {
        return neighbors;
    }

    public Set<Node> getCom_with() {
        return com_with;
    }

    public void addCommunicator(Node comm)
    {
        if (!com_with.contains(comm)) {
            com_with.add(comm);
        }
    }
    public void removeCommunicator(Node comm)
    {
        com_with.remove(comm);
    }

    public void addNeighbor(Node neighbor) {
        if (!neighbors.contains(neighbor)) {
            neighbors.add(neighbor);
        }
    }

    public List<Message> getMessageBuffer(){
        return tempMessagesBuffer;
    }

    public void setMessageBuffer(List<Message> messages)
    {
        tempMessagesBuffer =  messages;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void sendMessage(Node target, Message message) {
        if (neighbors.contains(target)) {
            System.out.println("Node " + id + " sending message to " + target.getId() + ": " + message.getContent());
            target.receiveMessage(message);
        }
    }

    public CompletableFuture<Void> sendMessageAsync(Node target, Message message) {
        if (neighbors.contains(target)) {
            System.out.println("Node " + id + " sending message to " + target.getId() + ": " + message.getContent());
            return CompletableFuture.runAsync(() -> target.receiveMessage(message));
        } else {
            return CompletableFuture.completedFuture(null); // Return completed future if target is not a neighbor
        }
    }

    public void receiveMessage(Message message) {
        messageQueue.offer(message);
        System.out.println("Node " + id + " has " + messageQueue.size() + " messages in the queue.");
        System.out.println("Node " + id + " received message: " + message.getContent());
        messageSemaphore.release();
    }

    public List<Message> receiveAllMessages() {
        List<Message> messages = new ArrayList<>();
        Map<Node, Integer> receivedCounts = new HashMap<>();

        while (receivedCounts.size() < neighbors.size()) {
            try {
                messageSemaphore.acquire();

                Message message;
                while ((message = messageQueue.poll()) != null) {
                    Node sender = message.getSender();
                    receivedCounts.put(sender, receivedCounts.getOrDefault(sender, 0) + 1);

                    if (receivedCounts.get(sender) == 1) {
                        messages.add(message);
                        System.out.println("Node " + id + " received message: " + message.getContent() + " from Node " + sender.getId());
                    } else {
                        System.out.println("Node " + id + " received duplicate message from Node " + sender.getId() + " and ignored it.");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return messages;
    }

    public List<Message> receiveAllCommMessages() {
        List<Message> messages = new ArrayList<>();
        Map<Node, Integer> receivedCounts = new HashMap<>();

        while (receivedCounts.size() < com_with.size()) {
            try {
                messageSemaphore.acquire();

                Message message;
                while ((message = messageQueue.poll()) != null) {
                    Node sender = message.getSender();
                    receivedCounts.put(sender, receivedCounts.getOrDefault(sender, 0) + 1);

                    if (receivedCounts.get(sender) == 1) {
                        messages.add(message);
                        System.out.println("Node " + id + " received message: " + message.getContent() + " from Node " + sender.getId());
                    } else {
                        System.out.println("Node " + id + " received duplicate message from Node " + sender.getId() + " and ignored it.");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return messages;
    }
    public void waitForNeighboursColor(Node neighbor, int current_round) {

        while (true) {
            try {
                messageSemaphore.acquire();

                Message message;
                boolean foundValidMessage = false;
                while ((message = messageQueue.peek()) != null) {
                    if (message.getSender() == neighbor && message.getRound() == current_round && message.getType() == Message.Type.COLOR) {
                        System.out.println("Node " + id + " received message: " + message.getContent() + " from Node " + neighbor.getId());
                        foundValidMessage = true;
                        this.receivedColors.put(message.getSender().getId(), Integer.parseInt(message.getContent()));
                        break;
                    } else {
                        messageQueue.offer(messageQueue.poll());
                        messageSemaphore.release();
                    }
                }

                if (foundValidMessage) {
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void waitForNeighboursSelected(Node neighbor, int current_round) {

        while (true) {
            try {
                messageSemaphore.acquire();

                Message message;
                boolean foundValidMessage = false;
                while ((message = messageQueue.peek()) != null) {
                    if (message.getSender() == neighbor && message.getRound() == current_round && message.getType() == Message.Type.SELECTED ) {
                        System.out.println("Node " + id + " received message: " + message.getContent() + " from Node " + neighbor.getId());
                        foundValidMessage = true;
                        this.selected.put(message.getSender().getId(), Boolean.parseBoolean(message.getContent()));
                        break;
                    } else {
                        messageQueue.offer(messageQueue.poll());
                        messageSemaphore.release();
                    }
                }

                if (foundValidMessage) {
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public AtomicBoolean getInMIS() {
        return inMIS;
    }

}