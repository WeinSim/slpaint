package com.weinsim.sutil.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class SWeightedGraph<T> {

    ArrayList<Node<T>> nodes;

    public SWeightedGraph() {
        nodes = new ArrayList<>();
    }

    public SWeightedGraph(Collection<T> elements) {
        nodes = new ArrayList<>();
        for (T t : elements) {
            add(t);
        }
    }

    public void add(T element) {
        if (contains(element)) {
            return;
        }
        Node<T> newNode = new Node<T>(element);
        nodes.add(newNode);
    }

    private Node<T> getNode(T element) {
        for (Node<T> node : nodes) {
            if (node.getElement() == element) {
                return node;
            }
        }
        return null;
    }

    public boolean contains(T element) {
        return getNode(element) != null;
    }

    public void connect1Way(T element1, T element2) {
        Node<T> node1 = getNode(element1);
        Node<T> node2 = getNode(element2);
        if (node1 == null || node2 == null)
            return;
        node1.connect1Way(node2);
    }

    public void connect2Way(T element1, T element2) {
        Node<T> node1 = getNode(element1);
        Node<T> node2 = getNode(element2);
        if (node1 == null || node2 == null)
            return;
        node1.connect2Way(node2);
    }

    public void disconnect1Way(T element1, T element2) {
        Node<T> node1 = getNode(element1);
        Node<T> node2 = getNode(element2);
        if (node1 == null || node2 == null)
            return;
        node1.disconnect1Way(node2);
    }

    public void disconnect2Way(T element1, T element2) {
        Node<T> node1 = getNode(element1);
        Node<T> node2 = getNode(element2);
        if (node1 == null || node2 == null)
            return;
        node1.disconnect2Way(node2);
    }

    public void set1WayWeight(T element1, T element2, double weight) {
        Node<T> node1 = getNode(element1);
        Node<T> node2 = getNode(element2);
        if (node1 == null || node2 == null)
            return;
        node1.set1WayWeight(node2, weight);
    }

    public void set2WayWeight(T element1, T element2, double weight) {
        Node<T> node1 = getNode(element1);
        Node<T> node2 = getNode(element2);
        if (node1 == null || node2 == null)
            return;
        node1.set2WayWeight(node2, weight);
    }

    public HashMap<T, Double> getConnections(T element) {
        Node<T> node = getNode(element);
        HashMap<T, Double> output = new HashMap<>();
        if (node == null)
            return output;
        HashMap<Node<T>, Double> connections = node.getConnections();
        for (Node<T> n : connections.keySet()) {
            output.put(n.getElement(), connections.get(n));
        }
        return output;
    }

    public ArrayList<T> getElements() {
        ArrayList<T> elements = new ArrayList<>();
        for (Node<T> node : nodes) {
            elements.add(node.getElement());
        }
        return elements;
    }
}
