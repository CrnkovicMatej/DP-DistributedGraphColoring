package com.project.network;

public class Message {

    public enum Type { 
        DEFAULT,
        RANDOM,
        SELECTED,
        ELIMINATED
    }

    public Type type = Type.DEFAULT;
    private final String content;
    private final Node source;
    private final Node destination;

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

    public Node getSender(){return source;}
    public String getContent() {
        return content;
    }

    public Type getType() {return type;}

    public Node getDestination() {
        return destination;
    }
}