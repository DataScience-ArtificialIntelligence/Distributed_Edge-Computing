package com.p2p.algo;

import java.io.*;
import java.util.*;

public class GraphProcessor {
    private Map<Integer, List<Integer>> graph;
    private int nodeCount;
    private int edgeCount;

    public GraphProcessor(String filename) throws IOException {
        this.graph = new HashMap<>();
        loadGraph(filename);
    }

    private void loadGraph(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        edgeCount = 0;

        while ((line = reader.readLine()) != null) {
            // Skip comment or header lines
            if (line.startsWith("#") || line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 2) {  // At least source and target
                int source = Integer.parseInt(parts[0]);
                int target = Integer.parseInt(parts[1]);

                if (!graph.containsKey(source)) {
                    graph.put(source, new ArrayList<>());
                }
                graph.get(source).add(target);

                // Make sure target is in the graph even if it has no outlinks
                if (!graph.containsKey(target)) {
                    graph.put(target, new ArrayList<>());
                }

                edgeCount++;
            }
        }
        reader.close();

        nodeCount = graph.size();
        System.out.println("Loaded graph with " + nodeCount + " nodes and " + edgeCount + " edges");
    }

    public Map<Integer, List<Integer>> getGraph() {
        return graph;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public double getAverageDegree() {
        return (double) edgeCount / nodeCount;
    }

    // Find nodes with highest in-degree (most incoming links)
    public List<Map.Entry<Integer, Integer>> getTopInDegreeNodes(int n) {
        Map<Integer, Integer> inDegrees = new HashMap<>();

        // Initialize all nodes with in-degree 0
        for (Integer node : graph.keySet()) {
            inDegrees.put(node, 0);
        }

        // Count incoming links
        for (Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {
            List<Integer> outLinks = entry.getValue();
            for (Integer target : outLinks) {
                inDegrees.put(target, inDegrees.getOrDefault(target, 0) + 1);
            }
        }

        // Sort by in-degree
        List<Map.Entry<Integer, Integer>> sortedDegrees = new ArrayList<>(inDegrees.entrySet());
        sortedDegrees.sort(Map.Entry.<Integer, Integer>comparingByValue().reversed());

        return sortedDegrees.subList(0, Math.min(n, sortedDegrees.size()));
    }

    // Find nodes with highest out-degree (most outgoing links)
    public List<Map.Entry<Integer, Integer>> getTopOutDegreeNodes(int n) {
        Map<Integer, Integer> outDegrees = new HashMap<>();

        // Count outgoing links
        for (Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {
            Integer node = entry.getKey();
            List<Integer> outLinks = entry.getValue();
            outDegrees.put(node, outLinks.size());
        }

        // Sort by out-degree
        List<Map.Entry<Integer, Integer>> sortedDegrees = new ArrayList<>(outDegrees.entrySet());
        sortedDegrees.sort(Map.Entry.<Integer, Integer>comparingByValue().reversed());

        return sortedDegrees.subList(0, Math.min(n, sortedDegrees.size()));
    }

    public String getGraphStats() {
        return "Graph Statistics:\n" +
                "- Nodes: " + nodeCount + "\n" +
                "- Edges: " + edgeCount + "\n" +
                "- Average outgoing edges: " + String.format("%.2f", getAverageDegree());
    }
}