package com.project.network;

public class Message {

    public enum Type { 
        DEFAULT,
        RANDOM,
        SELECTED
    }

    public Type type = Type.DEFAULT;
    private String content;
    private Node source;
    private Node destination;

    public Message(String content, Node source, Node destination) {
        this.content = content;
        this.source = source;
        this.destination = destination;
    }

    public Message(Type type, String content, Node source, Node destination) {
        this.type = type;
        this.content = content;
        this.source = source;
        this.destination = destination;
    }

    public String getContent() {
        return content;
    }

    public Node getSource() {
        return source;
    }

    public Node getDestination() {
        return destination;
    }
}