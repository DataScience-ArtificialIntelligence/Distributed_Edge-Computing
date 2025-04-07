package com.p2p.algo;

import java.io.*;
import java.util.*;

public class HighestIndegreeNode {
    private Map<Integer, List<Integer>> graph = new HashMap<>();
    private Map<Integer, Integer> inDegrees = new HashMap<>();
    private int nodeCount = 0;
    private int edgeCount = 0;

    public HighestIndegreeNode(String graphFile) throws IOException {
        loadGraph(graphFile);
        calculateInDegrees();
    }

    private void loadGraph(String filename) throws IOException {
        // Load graph from file
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            // Skip comment or header lines
            if (line.startsWith("#") || line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 2) {  // At least source and target
                int source = Integer.parseInt(parts[0]);
                int target = Integer.parseInt(parts[1]);

                // Make sure both nodes are in the graph
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

    private void calculateInDegrees() {
        // Initialize in-degrees to zero for all nodes
        for (Integer node : graph.keySet()) {
            inDegrees.put(node, 0);
        }

        // Count incoming edges for each node
        for (Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {
            for (Integer target : entry.getValue()) {
                inDegrees.put(target, inDegrees.get(target) + 1);
            }
        }
    }

    public List<Map.Entry<Integer, Integer>> getTopNodes(int n) {
        List<Map.Entry<Integer, Integer>> sortedDegrees = new ArrayList<>(inDegrees.entrySet());
        sortedDegrees.sort(Map.Entry.<Integer, Integer>comparingByValue().reversed());

        return sortedDegrees.subList(0, Math.min(n, sortedDegrees.size()));
    }

    public Map<Integer, Integer> getInDegrees() {
        return inDegrees;
    }

    public String getGraphStats() {
        double avgInDegree = (double) edgeCount / nodeCount;
        int maxInDegree = inDegrees.values().stream().max(Integer::compare).orElse(0);
        int minInDegree = inDegrees.values().stream().min(Integer::compare).orElse(0);

        return "Graph Statistics:\n" +
                "- Nodes: " + nodeCount + "\n" +
                "- Edges: " + edgeCount + "\n" +
                "- Average in-degree: " + String.format("%.2f", avgInDegree) + "\n" +
                "- Maximum in-degree: " + maxInDegree + "\n" +
                "- Minimum in-degree: " + minInDegree;
    }
}