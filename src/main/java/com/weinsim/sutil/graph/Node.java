package com.weinsim.sutil.graph;

import java.util.HashMap;

public class Node<T> {

    private T element;

    private HashMap<Node<T>, Double> connections;

    public Node(T element) {
        this.element = element;
        connections = new HashMap<>();
    }

    public void connect2Way(Node<T> node) {
        set2WayWeight(node, 1.0);
    }

    public void connect1Way(Node<T> node) {
        set1WayWeight(node, 1.0);
    }

    public void disconnect2Way(Node<T> node) {
        set2WayWeight(node, 0.0);
    }

    public void disconnect1Way(Node<T> node) {
        set1WayWeight(node, 0.0);
    }

    public void set2WayWeight(Node<T> node, double weight) {
        if (weight == 0.0 || weight == -0.0) {
            connections.remove(node);
            node.getConnections().remove(this);
        } else {
            connections.put(node, weight);
            node.getConnections().put(this, weight);
        }
    }

    public void set1WayWeight(Node<T> node, double weight) {
        if (weight == 0.0 || weight == -0.0) {
            connections.remove(node);
        } else {
            connections.put(node, weight);
        }
    }

    public boolean isConnected(Node<T> node) {
        return connections.keySet().contains(node);
    }

    public HashMap<Node<T>, Double> getConnections() {
        return connections;
    }

    public double getConnectionWeight(Node<T> node) {
        Double d = connections.get(node);
        return d == null ? 0 : d;
    }

    public T getElement() {
        return element;
    }

    public void setElement(T element) {
        this.element = element;
    }
}