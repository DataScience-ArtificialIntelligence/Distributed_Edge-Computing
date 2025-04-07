// PageRank.java - Modified for your graph.txt format
package com.p2p.algo;

import java.io.*;
import java.util.*;

public class PageRank {
    private Map<Integer, List<Integer>> graph = new HashMap<>();
    private Map<Integer, Double> ranks = new HashMap<>();
    private double dampingFactor = 0.85;
    private int iterations = 10;
    private int nodeCount = 0;
    private int edgeCount = 0;

    public PageRank(String graphFile) throws IOException {
        loadGraph(graphFile);
        initializeRanks();
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
            if (parts.length >= 3) {  // Format: source target weight
                int source = Integer.parseInt(parts[0]);
                int target = Integer.parseInt(parts[1]);
                // We'll ignore the weight for basic PageRank

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

    private void initializeRanks() {
        double initialRank = 1.0 / nodeCount;
        for (Integer node : graph.keySet()) {
            ranks.put(node, initialRank);
        }
    }

    public void compute() {
        System.out.println("Starting PageRank computation for " + iterations + " iterations...");

        for (int i = 0; i < iterations; i++) {
            Map<Integer, Double> newRanks = new HashMap<>();

            // Initialize with random jump probability
            for (Integer node : graph.keySet()) {
                newRanks.put(node, (1 - dampingFactor) / nodeCount);
            }

            // Add contribution from incoming links
            for (Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {
                int sourceNode = entry.getKey();
                List<Integer> outLinks = entry.getValue();

                if (outLinks.size() > 0) {
                    double rankToDistribute = ranks.get(sourceNode) * dampingFactor;
                    double rankPerTarget = rankToDistribute / outLinks.size();

                    for (Integer targetNode : outLinks) {
                        Double currentRank = newRanks.get(targetNode);
                        if (currentRank != null) {
                            newRanks.put(targetNode, currentRank + rankPerTarget);
                        }
                    }
                }
            }

            // Update ranks
            ranks = newRanks;

            if (i % 2 == 0) {
                System.out.println("Completed iteration " + (i+1) + " of " + iterations);
            }
        }

        System.out.println("PageRank computation completed");
    }

    public Map<Integer, Double> getRanks() {
        return ranks;
    }

    public List<Map.Entry<Integer, Double>> getTopNodes(int n) {
        List<Map.Entry<Integer, Double>> sortedRanks = new ArrayList<>(ranks.entrySet());
        sortedRanks.sort(Map.Entry.<Integer, Double>comparingByValue().reversed());

        return sortedRanks.subList(0, Math.min(n, sortedRanks.size()));
    }

    // Add a method to get graph statistics
    public String getGraphStats() {
        return "Graph Statistics:\n" +
                "- Nodes: " + nodeCount + "\n" +
                "- Edges: " + edgeCount + "\n" +
                "- Average outgoing edges: " + (edgeCount / (double)nodeCount);
    }
}