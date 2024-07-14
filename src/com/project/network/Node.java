package com.project.network;

import com.project.utils.NodeMessageHandler;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Node  {
    public int id;
    private final Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();
    private final Semaphore messageSemaphore = new Semaphore(0);
    private final Set<Node> neighbors = ConcurrentHashMap.newKeySet();
    private final NodeMessageHandler messageHandler;

    public final Set<Node> com_with = ConcurrentHashMap.newKeySet();
    private AtomicBoolean inMIS;
    private AtomicBoolean active;
    public int randomValue;
    public Map<Integer, Integer> receivedColors;
    public Map<Integer, Boolean> selected;
    public Map<Integer, Integer> randomValues;
    public Map<Integer, Boolean> eliminated;
    private int color;
    public boolean isSmallest;
    

    public Node(int id) {
        this.id = id;
        this.receivedColors = new HashMap<>();
        this.selected = new HashMap<>();
        this.randomValues = new HashMap<>();
        this.eliminated = new HashMap<>();
        this.color = id;
        this.inMIS = new AtomicBoolean(false);
        this.active = new AtomicBoolean(true);
        this.messageHandler = new NodeMessageHandler(this, messageSemaphore, messageQueue);
    }

    // getters and setters
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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public NodeMessageHandler getMessageHandler() {
        return messageHandler;
    }

    public AtomicBoolean getInMIS() {
        return inMIS;
    }

    public void setRandomValue(int newRandom) { randomValue = newRandom; }

    public void initSelected()
    {
        this.selected = this.getNeighbors().stream()
                .collect(Collectors.toMap(Node::getId, neighbor -> false));
        this.selected.put(this.getId(), false);
    }

    public void initEliminated()
    {
        this.eliminated = this.getNeighbors().stream()
                .collect(Collectors.toMap(Node::getId, neighbor -> false));
        this.eliminated.put(this.getId(), false);
    }
    public boolean noneOfAllNeighborsSelected() {
        return this.getNeighbors().stream()
                .noneMatch(neighbor -> selected.get(neighbor.getId()));
    }

    public void setOwnSelectionTrue() {
        this.selected.put(this.getId(), true);
    }
    public boolean getOwnSelection() {
        return this.selected.get(this.getId());
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

    public void removeEliminatedNeighbors() {
        com_with.removeIf(communicator -> eliminated.getOrDefault(communicator.getId(), false));
    }

    public void addNeighbor(Node neighbor) {
        if (!neighbors.contains(neighbor)) {
            neighbors.add(neighbor);
        }
    }
    

}