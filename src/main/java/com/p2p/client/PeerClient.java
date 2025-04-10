package com.p2p.client;
import com.p2p.model.Peer;
import com.p2p.server.PeerRegistryInterface;
import com.p2p.algo.PageRank;
import com.p2p.algo.HighestIndegreeNode;
import com.p2p.algo.BetweennessCentrality;
import com.p2p.algo.ClusteringCoefficient;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

public class PeerClient extends UnicastRemoteObject implements Peer {
    private final String name;
    private static final String directory = "uploads/";
    private static final String downloadDir = "downloads/";
    private Map<Integer, Double> pageRankResults;
    private Map<Integer, Integer> indegreeResults;
    private Map<Integer, Double> betweennessResults;
    private Map<Integer, Double> clusteringResults;

    protected PeerClient(String name) throws RemoteException {
        this.name = name;
        // Create both upload and download directories
        File uploadFolder = new File(directory);
        File downloadFolder = new File(downloadDir);
        if (!uploadFolder.exists()) uploadFolder.mkdirs();
        if (!downloadFolder.exists()) downloadFolder.mkdirs();
    }

    public String getName() throws RemoteException {
        return name;
    }

    public byte[] downloadFile(String filename) throws RemoteException {
        try {
            Path path = Paths.get(directory + filename);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RemoteException("File not found: " + filename, e);
        }
    }

    public void uploadFile(String filename, byte[] data) throws RemoteException {
        try {
            Path path = Paths.get(directory + filename);
            Files.write(path, data);
            JOptionPane.showMessageDialog(null, "File " + filename + " uploaded successfully.");
        } catch (IOException e) {
            throw new RemoteException("Error saving file: " + filename, e);
        }
    }

    @Override
    public void runPageRank(String graphFile) throws RemoteException {
        try {
            long startTime = System.currentTimeMillis();

            // Show a "processing" dialog
            JDialog processingDialog = new JDialog();
            processingDialog.setTitle("Processing");
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            JLabel statusLabel = new JLabel("Loading graph and computing PageRank...");
            processingDialog.setLayout(new BorderLayout());
            processingDialog.add(statusLabel, BorderLayout.NORTH);
            processingDialog.add(progressBar, BorderLayout.CENTER);
            processingDialog.setSize(300, 100);
            processingDialog.setLocationRelativeTo(null);
            processingDialog.setVisible(true);

            // Create a worker thread to avoid freezing the UI
            SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() throws Exception {
                    publish("Loading graph from file...");
                    PageRank pageRank = new PageRank(directory + graphFile);

                    publish("Computing PageRank (this may take a few minutes)...");
                    pageRank.compute();

                    pageRankResults = pageRank.getRanks();

                    publish("Sorting results...");
                    List<Map.Entry<Integer, Double>> topNodes = pageRank.getTopNodes(20);

                    StringBuilder results = new StringBuilder();
                    results.append(pageRank.getGraphStats()).append("\n\n");
                    results.append("Top 20 PageRank Results:\n");
                    for (Map.Entry<Integer, Double> entry : topNodes) {
                        results.append("Node ").append(entry.getKey())
                                .append(": ").append(String.format("%.6f", entry.getValue()))
                                .append("\n");
                    }

                    // Add execution time
                    long endTime = System.currentTimeMillis();
                    results.append("\nExecution time: ").append((endTime - startTime) / 1000.0).append(" seconds");

                    final String resultText = results.toString();

                    SwingUtilities.invokeLater(() -> {
                        processingDialog.dispose();
                        JTextArea textArea = new JTextArea(resultText);
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        scrollPane.setPreferredSize(new Dimension(500, 400));
                        JOptionPane.showMessageDialog(null, scrollPane, "PageRank Results", JOptionPane.INFORMATION_MESSAGE);
                    });

                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    if (!chunks.isEmpty()) {
                        statusLabel.setText(chunks.get(chunks.size() - 1));
                    }
                }
            };

            worker.execute();

        } catch (Exception e) {
            throw new RemoteException("Error running PageRank", e);
        }
    }

    @Override
    public void runHighestIndegree(String graphFile) throws RemoteException {
        try {
            long startTime = System.currentTimeMillis();

            // Show a "processing" dialog
            JDialog processingDialog = new JDialog();
            processingDialog.setTitle("Processing");
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            JLabel statusLabel = new JLabel("Loading graph and computing Highest Indegree...");
            processingDialog.setLayout(new BorderLayout());
            processingDialog.add(statusLabel, BorderLayout.NORTH);
            processingDialog.add(progressBar, BorderLayout.CENTER);
            processingDialog.setSize(300, 100);
            processingDialog.setLocationRelativeTo(null);
            processingDialog.setVisible(true);

            // Create a worker thread to avoid freezing the UI
            SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() throws Exception {
                    publish("Loading graph from file...");
                    HighestIndegreeNode indegree = new HighestIndegreeNode(directory + graphFile);

                    publish("Calculating in-degrees...");
                    indegreeResults = indegree.getInDegrees();

                    publish("Sorting results...");
                    List<Map.Entry<Integer, Integer>> topNodes = indegree.getTopNodes(20);

                    StringBuilder results = new StringBuilder();
                    results.append(indegree.getGraphStats()).append("\n\n");
                    results.append("Top 20 Nodes by In-degree:\n");
                    for (Map.Entry<Integer, Integer> entry : topNodes) {
                        results.append("Node ").append(entry.getKey())
                                .append(": ").append(entry.getValue())
                                .append(" incoming links\n");
                    }

                    // Add execution time
                    long endTime = System.currentTimeMillis();
                    results.append("\nExecution time: ").append((endTime - startTime) / 1000.0).append(" seconds");

                    final String resultText = results.toString();

                    SwingUtilities.invokeLater(() -> {
                        processingDialog.dispose();
                        JTextArea textArea = new JTextArea(resultText);
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        scrollPane.setPreferredSize(new Dimension(500, 400));
                        JOptionPane.showMessageDialog(null, scrollPane, "Highest In-degree Results", JOptionPane.INFORMATION_MESSAGE);
                    });

                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    if (!chunks.isEmpty()) {
                        statusLabel.setText(chunks.get(chunks.size() - 1));
                    }
                }
            };

            worker.execute();

        } catch (Exception e) {
            throw new RemoteException("Error calculating highest in-degree", e);
        }
    }

    @Override
    public void runBetweennessCentrality(String graphFile) throws RemoteException {
        try {
            long startTime = System.currentTimeMillis();

            // Show a "processing" dialog
            JDialog processingDialog = new JDialog();
            processingDialog.setTitle("Processing");
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            JLabel statusLabel = new JLabel("Loading graph and computing Betweenness Centrality...");
            processingDialog.setLayout(new BorderLayout());
            processingDialog.add(statusLabel, BorderLayout.NORTH);
            processingDialog.add(progressBar, BorderLayout.CENTER);
            processingDialog.setSize(300, 100);
            processingDialog.setLocationRelativeTo(null);
            processingDialog.setVisible(true);

            // Create a worker thread to avoid freezing the UI
            SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() throws Exception {
                    publish("Loading graph from file...");
                    BetweennessCentrality betweenness = new BetweennessCentrality(directory + graphFile);

                    publish("Computing betweenness centrality (this may take a few minutes)...");
                    betweenness.compute();

                    betweennessResults = betweenness.getCentrality();

                    publish("Sorting results...");
                    List<Map.Entry<Integer, Double>> topNodes = betweenness.getTopNodes(20);

                    StringBuilder results = new StringBuilder();
                    results.append(betweenness.getGraphStats()).append("\n\n");
                    results.append("Top 20 Nodes by Betweenness Centrality:\n");
                    for (Map.Entry<Integer, Double> entry : topNodes) {
                        results.append("Node ").append(entry.getKey())
                                .append(": ").append(String.format("%.6f", entry.getValue()))
                                .append("\n");
                    }

                    // Add execution time
                    long endTime = System.currentTimeMillis();
                    results.append("\nExecution time: ").append((endTime - startTime) / 1000.0).append(" seconds");

                    final String resultText = results.toString();

                    SwingUtilities.invokeLater(() -> {
                        processingDialog.dispose();
                        JTextArea textArea = new JTextArea(resultText);
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        scrollPane.setPreferredSize(new Dimension(500, 400));
                        JOptionPane.showMessageDialog(null, scrollPane, "Betweenness Centrality Results", JOptionPane.INFORMATION_MESSAGE);
                    });

                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    if (!chunks.isEmpty()) {
                        statusLabel.setText(chunks.get(chunks.size() - 1));
                    }
                }
            };

            worker.execute();

        } catch (Exception e) {
            throw new RemoteException("Error calculating betweenness centrality", e);
        }
    }

    @Override
    public void runClusteringCoefficient(String graphFile) throws RemoteException {
        try {
            long startTime = System.currentTimeMillis();

            // Show a "processing" dialog
            JDialog processingDialog = new JDialog();
            processingDialog.setTitle("Processing");
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            JLabel statusLabel = new JLabel("Loading graph and computing Clustering Coefficients...");
            processingDialog.setLayout(new BorderLayout());
            processingDialog.add(statusLabel, BorderLayout.NORTH);
            processingDialog.add(progressBar, BorderLayout.CENTER);
            processingDialog.setSize(300, 100);
            processingDialog.setLocationRelativeTo(null);
            processingDialog.setVisible(true);

            // Create a worker thread to avoid freezing the UI
            SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() throws Exception {
                    publish("Loading graph from file...");
                    ClusteringCoefficient clustering = new ClusteringCoefficient(directory + graphFile);

                    publish("Computing clustering coefficients...");
                    clustering.compute();

                    clusteringResults = clustering.getCoefficients();

                    publish("Sorting results...");
                    List<Map.Entry<Integer, Double>> topNodes = clustering.getTopNodes(20);

                    StringBuilder results = new StringBuilder();
                    results.append(clustering.getGraphStats()).append("\n\n");
                    results.append("Top 20 Nodes by Clustering Coefficient:\n");
                    for (Map.Entry<Integer, Double> entry : topNodes) {
                        results.append("Node ").append(entry.getKey())
                                .append(": ").append(String.format("%.6f", entry.getValue()))
                                .append("\n");
                    }

                    // Add execution time
                    long endTime = System.currentTimeMillis();
                    results.append("\nExecution time: ").append((endTime - startTime) / 1000.0).append(" seconds");

                    final String resultText = results.toString();

                    SwingUtilities.invokeLater(() -> {
                        processingDialog.dispose();
                        JTextArea textArea = new JTextArea(resultText);
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        scrollPane.setPreferredSize(new Dimension(500, 400));
                        JOptionPane.showMessageDialog(null, scrollPane, "Clustering Coefficient Results", JOptionPane.INFORMATION_MESSAGE);
                    });

                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    if (!chunks.isEmpty()) {
                        statusLabel.setText(chunks.get(chunks.size() - 1));
                    }
                }
            };

            worker.execute();

        } catch (Exception e) {
            throw new RemoteException("Error calculating clustering coefficients", e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system properties for RMI
                System.setProperty("java.rmi.server.hostname", getLocalIPAddress());
                // Enable RMI client connection through firewall
                System.setProperty("java.rmi.server.useCodebaseOnly", "false");

                String peerName = args.length > 0 ? args[0] : "Peer1";
                PeerClient peer = new PeerClient(peerName);

                // Get server address from user or use default
                String serverAddress = JOptionPane.showInputDialog(
                        "Enter server IP address:",
                        "localhost");
                if (serverAddress == null || serverAddress.trim().isEmpty()) {
                    serverAddress = "localhost";
                }

                // Bind this peer to RMI registry
                String bindAddress = "rmi://" + getLocalIPAddress() + "/" + peerName;
                Naming.rebind(bindAddress, peer);
                System.out.println("Peer bound at: " + bindAddress);

                // Look up the registry
                String registryAddress = "rmi://" + serverAddress + "/PeerRegistry";
                PeerRegistryInterface registry = (PeerRegistryInterface) Naming.lookup(registryAddress);
                registry.registerPeer(peerName, peer);
                System.out.println("Registered with server at: " + registryAddress);

                // Create the main frame
                JFrame frame = new JFrame(peerName + " - P2P File Sharing and Graph Analysis");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(600, 400);

                // Use a more organized layout
                JPanel mainPanel = new JPanel(new BorderLayout());

                // File sharing panel
                JPanel fileSharingPanel = new JPanel(new GridLayout(3, 1, 5, 5));
                fileSharingPanel.setBorder(BorderFactory.createTitledBorder("File Sharing"));

                JButton uploadButton = new JButton("Upload File to Peer");
                JButton downloadButton = new JButton("Download File from Peer");
                JButton browseButton = new JButton("Browse Local Files");

                fileSharingPanel.add(uploadButton);
                fileSharingPanel.add(downloadButton);
                fileSharingPanel.add(browseButton);

                // Graph analysis panel
                JPanel graphAnalysisPanel = new JPanel(new GridLayout(4, 1, 5, 5));
                graphAnalysisPanel.setBorder(BorderFactory.createTitledBorder("Graph Analysis"));

                JButton pageRankButton = new JButton("Run PageRank");
                JButton indegreeButton = new JButton("Run Highest Indegree");
                JButton betweennessButton = new JButton("Run Betweenness Centrality");
                JButton clusteringButton = new JButton("Run Clustering Coefficient");

                graphAnalysisPanel.add(pageRankButton);
                graphAnalysisPanel.add(indegreeButton);
                graphAnalysisPanel.add(betweennessButton);
                graphAnalysisPanel.add(clusteringButton);

                // Control panel
                JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JButton exitButton = new JButton("Exit");
                controlPanel.add(exitButton);

                // Add panels to main panel
                JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
                centerPanel.add(fileSharingPanel);
                centerPanel.add(graphAnalysisPanel);

                mainPanel.add(centerPanel, BorderLayout.CENTER);
                mainPanel.add(controlPanel, BorderLayout.SOUTH);

                // Add padding around the panels
                mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                frame.add(mainPanel);

                // Button action listeners for file sharing
                uploadButton.addActionListener(e -> {
                    String targetPeerName = JOptionPane.showInputDialog("Enter peer name to send file to:");
                    if (targetPeerName == null || targetPeerName.trim().isEmpty()) return;

                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                    int result = fileChooser.showOpenDialog(frame);

                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        try {
                            Peer targetPeer = registry.getPeer(targetPeerName);
                            if (targetPeer != null) {
                                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                                targetPeer.uploadFile(selectedFile.getName(), fileData);
                                JOptionPane.showMessageDialog(frame, "File sent to " + targetPeerName);
                            } else {
                                JOptionPane.showMessageDialog(frame, "Peer not found: " + targetPeerName);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                });

                downloadButton.addActionListener(e -> {
                    String sourcePeerName = JOptionPane.showInputDialog("Enter peer name to download from:");
                    if (sourcePeerName == null || sourcePeerName.trim().isEmpty()) return;

                    String filename = JOptionPane.showInputDialog("Enter filename:");
                    if (filename == null || filename.trim().isEmpty()) return;

                    try {
                        Peer sourcePeer = registry.getPeer(sourcePeerName);
                        if (sourcePeer != null) {
                            byte[] fileData = sourcePeer.downloadFile(filename);
                            Files.write(Paths.get(downloadDir + filename), fileData);
                            JOptionPane.showMessageDialog(frame, "File downloaded from " + sourcePeerName);
                        } else {
                            JOptionPane.showMessageDialog(frame, "Peer not found: " + sourcePeerName);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });

                browseButton.addActionListener(e -> {
                    try {
                        File uploadFolder = new File(directory);
                        File downloadFolder = new File(downloadDir);

                        JTabbedPane tabbedPane = new JTabbedPane();

                        // Uploads tab
                        JPanel uploadsPanel = new JPanel();
                        uploadsPanel.setLayout(new BoxLayout(uploadsPanel, BoxLayout.Y_AXIS));
                        File[] uploadFiles = uploadFolder.listFiles();
                        if (uploadFiles == null || uploadFiles.length == 0) {
                            uploadsPanel.add(new JLabel("No files available in uploads directory"));
                        } else {
                            JList<String> fileList = new JList<>(getFileInfoList(uploadFiles));
                            JScrollPane scrollPane = new JScrollPane(fileList);
                            scrollPane.setPreferredSize(new Dimension(400, 200));
                            uploadsPanel.add(scrollPane);
                        }

                        // Downloads tab
                        JPanel downloadsPanel = new JPanel();
                        downloadsPanel.setLayout(new BoxLayout(downloadsPanel, BoxLayout.Y_AXIS));
                        File[] downloadFiles = downloadFolder.listFiles();
                        if (downloadFiles == null || downloadFiles.length == 0) {
                            downloadsPanel.add(new JLabel("No files available in downloads directory"));
                        } else {
                            JList<String> fileList = new JList<>(getFileInfoList(downloadFiles));
                            JScrollPane scrollPane = new JScrollPane(fileList);
                            scrollPane.setPreferredSize(new Dimension(400, 200));
                            downloadsPanel.add(scrollPane);
                        }

                        tabbedPane.addTab("Uploads", uploadsPanel);
                        tabbedPane.addTab("Downloads", downloadsPanel);

                        JOptionPane.showMessageDialog(frame, tabbedPane, "File Browser", JOptionPane.PLAIN_MESSAGE);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Error browsing files: " + ex.getMessage());
                    }
                });

                // Button action listeners for graph analysis
                pageRankButton.addActionListener(e -> {
                    promptForGraphFileAndRun(frame, peer::runPageRank, "PageRank");
                });

                indegreeButton.addActionListener(e -> {
                    promptForGraphFileAndRun(frame, peer::runHighestIndegree, "Highest Indegree");
                });

                betweennessButton.addActionListener(e -> {
                    promptForGraphFileAndRun(frame, peer::runBetweennessCentrality, "Betweenness Centrality");
                });

                clusteringButton.addActionListener(e -> {
                    promptForGraphFileAndRun(frame, peer::runClusteringCoefficient, "Clustering Coefficient");
                });

                exitButton.addActionListener(e -> {
                    try {
                        // Unregister before exiting
                        registry.unregisterPeer(peerName);
                        System.out.println("Unregistered peer: " + peerName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    System.exit(0);
                });

                frame.setVisible(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Helper method to get file info for JList
    private static String[] getFileInfoList(File[] files) {
        String[] fileInfoList = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileInfoList[i] = files[i].getName() + " (" + (files[i].length() / 1024) + " KB)";
        }
        return fileInfoList;
    }

    // Helper method to prompt for graph file and run algorithm
    private static void promptForGraphFileAndRun(JFrame parent, GraphAlgorithmRunner runner, String algorithmName) {
        try {
            // Let user choose from available files or enter filename
            File folder = new File(directory);
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt") ||
                    name.toLowerCase().endsWith(".csv") ||
                    name.toLowerCase().endsWith(".graph"));

            String graphFile;
            if (files != null && files.length > 0) {
                String[] fileOptions = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    fileOptions[i] = files[i].getName();
                }

                graphFile = (String) JOptionPane.showInputDialog(
                        parent,
                        "Select graph file for " + algorithmName + ":",
                        "Select Graph File",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        fileOptions,
                        fileOptions[0]
                );
            } else {
                graphFile = JOptionPane.showInputDialog(
                        parent,
                        "Enter graph filename for " + algorithmName + ":\n(No graph files found in uploads directory)",
                        "Enter Graph File",
                        JOptionPane.QUESTION_MESSAGE
                );
            }

            if (graphFile != null && !graphFile.trim().isEmpty()) {
                runner.run(graphFile);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Error running " + algorithmName + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Functional interface for graph algorithm runners
    @FunctionalInterface
    private interface GraphAlgorithmRunner {
        void run(String graphFile) throws RemoteException;
    }

    // Helper method to get the local IP address
    private static String getLocalIPAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return "localhost";
        }
    }
}