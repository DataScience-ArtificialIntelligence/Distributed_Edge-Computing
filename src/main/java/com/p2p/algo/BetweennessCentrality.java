package com.p2p.algo;

import java.io.*;
import java.util.*;

public class BetweennessCentrality {
    private Map<Integer, List<Integer>> graph = new HashMap<>();
    private Map<Integer, Double> centrality = new HashMap<>();
    private int nodeCount = 0;
    private int edgeCount = 0;
    private boolean isWeighted = false;

    public BetweennessCentrality(String graphFile) throws IOException {
        loadGraph(graphFile);
        initializeCentrality();
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

                // Check if the graph has weights
                if (parts.length >= 3) {
                    isWeighted = true;
                }

                // Add edges to the graph (we'll ignore weights for basic betweenness)
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

    private void initializeCentrality() {
        for (Integer node : graph.keySet()) {
            centrality.put(node, 0.0);
        }
    }

    public void compute() {
        System.out.println("Starting betweenness centrality computation...");

        // For each node as a source
        for (Integer source : graph.keySet()) {
            // Use BFS to find shortest paths
            Queue<Integer> queue = new LinkedList<>();
            Map<Integer, List<Integer>> predecessors = new HashMap<>();
            Map<Integer, Integer> distance = new HashMap<>();
            Map<Integer, Integer> numShortestPaths = new HashMap<>();

            // Initialize for all nodes
            for (Integer node : graph.keySet()) {
                predecessors.put(node, new ArrayList<>());
                distance.put(node, Integer.MAX_VALUE);
                numShortestPaths.put(node, 0);
            }

            // Set up source node
            distance.put(source, 0);
            numShortestPaths.put(source, 1);
            queue.add(source);

            // BFS to find all shortest paths
            while (!queue.isEmpty()) {
                Integer current = queue.poll();

                for (Integer neighbor : graph.getOrDefault(current, Collections.emptyList())) {
                    // First time seeing this node
                    if (distance.get(neighbor) == Integer.MAX_VALUE) {
                        distance.put(neighbor, distance.get(current) + 1);
                        queue.add(neighbor);
                    }

                    // Found another shortest path to neighbor
                    if (distance.get(neighbor) == distance.get(current) + 1) {
                        numShortestPaths.put(neighbor, numShortestPaths.get(neighbor) + numShortestPaths.get(current));
                        predecessors.get(neighbor).add(current);
                    }
                }
            }

            // Calculate dependencies
            Map<Integer, Double> dependency = new HashMap<>();
            for (Integer node : graph.keySet()) {
                dependency.put(node, 0.0);
            }

            // Process nodes in order of decreasing distance from source
            List<Integer> stack = new ArrayList<>(graph.keySet());
            stack.sort(Comparator.comparing(distance::get).reversed());

            for (Integer node : stack) {
                // Skip source and unreachable nodes
                if (node.equals(source) || distance.get(node) == Integer.MAX_VALUE) {
                    continue;
                }

                for (Integer predecessor : predecessors.get(node)) {
                    double factor = (double) numShortestPaths.get(predecessor) / numShortestPaths.get(node);
                    dependency.put(predecessor, dependency.get(predecessor) + factor * (1 + dependency.get(node)));
                }

                if (!node.equals(source)) {
                    centrality.put(node, centrality.get(node) + dependency.get(node));
                }
            }
        }

        // Normalize by dividing by (n-1)(n-2) for undirected graphs, or (n-1)(n-2)/2 for directed
        // In our case, assume directed graph
        double normFactor = (nodeCount - 1) * (nodeCount - 2);
        if (normFactor > 0) {
            for (Integer node : centrality.keySet()) {
                centrality.put(node, centrality.get(node) / normFactor);
            }
        }

        System.out.println("Betweenness centrality computation completed");
    }

    public Map<Integer, Double> getCentrality() {
        return centrality;
    }

    public List<Map.Entry<Integer, Double>> getTopNodes(int n) {
        List<Map.Entry<Integer, Double>> sortedCentrality = new ArrayList<>(centrality.entrySet());
        sortedCentrality.sort(Map.Entry.<Integer, Double>comparingByValue().reversed());

        return sortedCentrality.subList(0, Math.min(n, sortedCentrality.size()));
    }

    public String getGraphStats() {
        double avgCentrality = centrality.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double maxCentrality = centrality.values().stream().max(Double::compare).orElse(0.0);

        return "Graph Statistics:\n" +
                "- Nodes: " + nodeCount + "\n" +
                "- Edges: " + edgeCount + "\n" +
                "- Average betweenness centrality: " + String.format("%.6f", avgCentrality) + "\n" +
                "- Maximum betweenness centrality: " + String.format("%.6f", maxCentrality) + "\n" +
                "- Is weighted graph: " + isWeighted;
    }
}