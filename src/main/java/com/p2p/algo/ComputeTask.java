package com.p2p.algo;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;

public class ComputeTask implements Serializable, Callable<Map<Integer, Double>> {
    private Map<Integer, List<Integer>> graphSection;
    private Map<Integer, Double> rankVector;
    private double dampingFactor;
    private int totalNodeCount;

    public ComputeTask(Map<Integer, List<Integer>> graphSection, Map<Integer, Double> rankVector,
                       double dampingFactor, int totalNodeCount) {
        this.graphSection = graphSection;
        this.rankVector = new HashMap<>(rankVector);  // Create a copy
        this.dampingFactor = dampingFactor;
        this.totalNodeCount = totalNodeCount;
    }

    @Override
    public Map<Integer, Double> call() {
        Map<Integer, Double> newRanks = new HashMap<>();

        // Initialize with random jump probability
        for (Integer node : graphSection.keySet()) {
            newRanks.put(node, (1 - dampingFactor) / totalNodeCount);
        }

        // Add contribution from incoming links
        for (Map.Entry<Integer, List<Integer>> entry : graphSection.entrySet()) {
            int sourceNode = entry.getKey();
            List<Integer> outLinks = entry.getValue();

            if (outLinks.size() > 0) {
                double rankToDistribute = rankVector.get(sourceNode) * dampingFactor;
                double rankPerTarget = rankToDistribute / outLinks.size();

                for (Integer targetNode : outLinks) {
                    newRanks.put(targetNode,
                            newRanks.getOrDefault(targetNode, (1 - dampingFactor) / totalNodeCount) + rankPerTarget);
                }
            }
        }

        return newRanks;
    }
}