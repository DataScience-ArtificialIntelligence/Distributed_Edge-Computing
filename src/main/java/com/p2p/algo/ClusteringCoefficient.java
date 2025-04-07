package com.p2p.algo;

import java.io.*;
import java.util.*;

public class ClusteringCoefficient {
    private Map<Integer, List<Integer>> graph = new HashMap<>();
    private Map<Integer, Double> coefficients = new HashMap<>();
    private int nodeCount = 0;
    private int edgeCount = 0;
    private double globalCoefficient = 0.0;

    public ClusteringCoefficient(String graphFile) throws IOException {
        loadGraph(graphFile);
        initializeCoefficients();
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

                // Add edges to the graph
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

    private void initializeCoefficients() {
        for (Integer node : graph.keySet()) {
            coefficients.put(node, 0.0);
        }
    }

    public void compute() {
        System.out.println("Starting clustering coefficient computation...");

        // Calculate local clustering coefficient for each node
        for (Integer node : graph.keySet()) {
            List<Integer> neighbors = graph.get(node);
            int neighborCount = neighbors.size();

            if (neighborCount < 2) {
                // No triangles possible with fewer than 2 neighbors
                coefficients.put(node, 0.0);
                continue;
            }

            // Count connected pairs among neighbors
            int triangleCount = 0;

            for (int i = 0; i < neighborCount; i++) {
                Integer neighbor1 = neighbors.get(i);
                List<Integer> neighbor1Neighbors = graph.getOrDefault(neighbor1, Collections.emptyList());

                for (int j = i + 1; j < neighborCount; j++) {
                    Integer neighbor2 = neighbors.get(j);

                    // Check if neighbor2 is in neighbor1's neighbors
                    if (neighbor1Neighbors.contains(neighbor2)) {
                        triangleCount++;
                    }
                }
            }

            // Calculate local clustering coefficient
            int maxPossibleTriangles = neighborCount * (neighborCount - 1) / 2;
            double localCC = maxPossibleTriangles > 0 ? (double) triangleCount / maxPossibleTriangles : 0.0;
            coefficients.put(node, localCC);
        }

        // Calculate global clustering coefficient (average of all local coefficients)
        globalCoefficient = coefficients.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        System.out.println("Clustering coefficient computation completed");
    }

    public Map<Integer, Double> getCoefficients() {
        return coefficients;
    }

    public double getGlobalCoefficient() {
        return globalCoefficient;
    }

    public List<Map.Entry<Integer, Double>> getTopNodes(int n) {
        List<Map.Entry<Integer, Double>> sortedCoefficients = new ArrayList<>(coefficients.entrySet());
        sortedCoefficients.sort(Map.Entry.<Integer, Double>comparingByValue().reversed());

        return sortedCoefficients.subList(0, Math.min(n, sortedCoefficients.size()));
    }

    public String getGraphStats() {
        double maxCC = coefficients.values().stream().max(Double::compare).orElse(0.0);
        double minCC = coefficients.values().stream().min(Double::compare).orElse(0.0);

        return "Graph Statistics:\n" +
                "- Nodes: " + nodeCount + "\n" +
                "- Edges: " + edgeCount + "\n" +
                "- Global clustering coefficient: " + String.format("%.6f", globalCoefficient) + "\n" +
                "- Maximum local clustering coefficient: " + String.format("%.6f", maxCC) + "\n" +
                "- Minimum local clustering coefficient: " + String.format("%.6f", minCC);
    }
}