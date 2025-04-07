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
            if (parts.length >= 2) {  // At least source and target
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
                "- Average outgoing edges: " + String.format("%.2f", (edgeCount / (double)nodeCount)) + "\n" +
                "- Damping factor: " + dampingFactor + "\n" +
                "- Iterations: " + iterations;
    }

    // Add methods to configure PageRank parameters
    public void setDampingFactor(double dampingFactor) {
        if (dampingFactor > 0 && dampingFactor < 1) {
            this.dampingFactor = dampingFactor;
        } else {
            System.out.println("Warning: Damping factor must be between 0 and 1. Using default value: " + this.dampingFactor);
        }
    }

    public void setIterations(int iterations) {
        if (iterations > 0) {
            this.iterations = iterations;
        } else {
            System.out.println("Warning: Iterations must be positive. Using default value: " + this.iterations);
        }
    }

    // Add a method to identify sink nodes (nodes with no outgoing links)
    public List<Integer> getSinkNodes() {
        List<Integer> sinkNodes = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {
            if (entry.getValue().isEmpty()) {
                sinkNodes.add(entry.getKey());
            }
        }
        return sinkNodes;
    }

    // Add a method to calculate convergence between iterations
    private double calculateConvergence(Map<Integer, Double> oldRanks, Map<Integer, Double> newRanks) {
        double sum = 0;
        for (Integer node : oldRanks.keySet()) {
            double diff = Math.abs(oldRanks.get(node) - newRanks.get(node));
            sum += diff;
        }
        return sum / oldRanks.size();
    }

    // Add a method to compute PageRank with convergence threshold
    public void computeWithConvergence(double threshold) {
        System.out.println("Starting PageRank computation with convergence threshold: " + threshold);

        int iteration = 0;
        double convergence = 1.0;

        while (convergence > threshold && iteration < 100) { // Maximum 100 iterations as a safety
            Map<Integer, Double> oldRanks = new HashMap<>(ranks);
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

            // Calculate convergence
            convergence = calculateConvergence(oldRanks, newRanks);
            iteration++;

            System.out.println("Iteration " + iteration + ", convergence: " + convergence);
        }

        System.out.println("PageRank computation completed after " + iteration + " iterations");
    }

    // Main method for standalone testing
    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.out.println("Usage: java PageRank <graph-file>");
                return;
            }

            PageRank pageRank = new PageRank(args[0]);
            pageRank.compute();

            List<Map.Entry<Integer, Double>> topNodes = pageRank.getTopNodes(10);
            System.out.println("\nTop 10 nodes by PageRank:");
            for (Map.Entry<Integer, Double> entry : topNodes) {
                System.out.println("Node " + entry.getKey() + ": " + String.format("%.6f", entry.getValue()));
            }

            System.out.println("\n" + pageRank.getGraphStats());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}