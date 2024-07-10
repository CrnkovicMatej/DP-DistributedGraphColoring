package com.project.network;

public class Message {

    public enum Type { 
        DEFAULT,
        RANDOM,
        SELECTED,
        ELIMINATED,
        INIT,
        COLOR
    }

    public Type type = Type.DEFAULT;
    private final String content;
    private final Node source;
    private final Node destination;
    private int round;

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

    public Message(Type type, String content, int round, Node source, Node destination) {
        this.type = type;
        this.content = content;
        this.source = source;
        this.destination = destination;
        this.round = round;
    }

    public Node getSender(){return source;}
    public String getContent() {
        return content;
    }

    public Type getType() {return type;}

    public int getRound(){return round;}

    public Node getDestination() {
        return destination;
    }
}