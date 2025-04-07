//package com.p2p.client;
//import com.p2p.model.Peer;
//import com.p2p.server.PeerRegistryInterface;
//import javax.swing.*;
//import java.awt.*;
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.rmi.Naming;
//import java.rmi.RemoteException;
//import java.rmi.server.UnicastRemoteObject;
//
//public class PeerClient extends UnicastRemoteObject implements Peer {
//    private final String name;
//    private final String directory = "uploads/";
//
//    protected PeerClient(String name) throws RemoteException {
//        this.name = name;
//        File folder = new File(directory);
//        if (!folder.exists()) folder.mkdirs();
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public byte[] downloadFile(String filename) throws RemoteException {
//        try {
//            Path path = Paths.get(directory + filename);
//            return Files.readAllBytes(path);
//        } catch (IOException e) {
//            throw new RemoteException("File not found", e);
//        }
//    }
//
//    public void uploadFile(String filename, byte[] data) throws RemoteException {
//        try {
//            Path path = Paths.get(directory + filename);
//            Files.write(path, data);
//            JOptionPane.showMessageDialog(null, "File " + filename + " uploaded successfully.");
//        } catch (IOException e) {
//            throw new RemoteException("Error saving file", e);
//        }
//    }
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            try {
//                String peerName = args.length > 0 ? args[0] : "Peer1";
//                PeerClient peer = new PeerClient(peerName);
//                Naming.rebind("rmi://localhost/" + peerName, peer);
//                PeerRegistryInterface registry = (PeerRegistryInterface) Naming.lookup("rmi://localhost/PeerRegistry");
//                registry.registerPeer(peerName, peer);
//
//                JFrame frame = new JFrame(peerName + " - P2P File Sharing");
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                frame.setSize(400, 300);
//                frame.setLayout(new GridLayout(3, 1));
//
//                JButton uploadButton = new JButton("Upload File");
//                JButton downloadButton = new JButton("Download File");
//                JButton exitButton = new JButton("Exit");
//
//                uploadButton.addActionListener(e -> {
//                    String targetPeerName = JOptionPane.showInputDialog("Enter peer name to send file to:");
//                    String filename = JOptionPane.showInputDialog("Enter filename:");
//                    try {
//                        Peer targetPeer = registry.getPeer(targetPeerName);
//                        if (targetPeer != null) {
//                            byte[] fileData = Files.readAllBytes(Paths.get("uploads/" + filename));
//                            targetPeer.uploadFile(filename, fileData);
//                        } else {
//                            JOptionPane.showMessageDialog(null, "Peer not found.");
//                        }
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                });
//
//                downloadButton.addActionListener(e -> {
//                    String sourcePeerName = JOptionPane.showInputDialog("Enter peer name to download from:");
//                    String filename = JOptionPane.showInputDialog("Enter filename:");
//                    try {
//                        Peer sourcePeer = registry.getPeer(sourcePeerName);
//                        if (sourcePeer != null) {
//                            byte[] fileData = sourcePeer.downloadFile(filename);
//                            Files.write(Paths.get("downloads/" + filename), fileData);
//                            JOptionPane.showMessageDialog(null, "File downloaded from " + sourcePeerName);
//                        } else {
//                            JOptionPane.showMessageDialog(null, "Peer not found.");
//                        }
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                });
//
//                exitButton.addActionListener(e -> System.exit(0));
//
//                frame.add(uploadButton);
//                frame.add(downloadButton);
//                frame.add(exitButton);
//                frame.setVisible(true);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }
//}
package com.p2p.client;
import com.p2p.model.Peer;
import com.p2p.server.PeerRegistryInterface;
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
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class PeerClient extends UnicastRemoteObject implements Peer {
    private final String name;
    private static final String directory = "uploads/";
    private static final String downloadDir = "downloads/";

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

                JFrame frame = new JFrame(peerName + " - P2P File Sharing");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(500, 400);
                frame.setLayout(new GridLayout(4, 1));

                JButton uploadButton = new JButton("Upload File");
                JButton downloadButton = new JButton("Download File");
                JButton browseButton = new JButton("Browse Local Files");
                JButton exitButton = new JButton("Exit");

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
                        File folder = new File(directory);
                        File[] files = folder.listFiles();
                        if (files == null || files.length == 0) {
                            JOptionPane.showMessageDialog(frame, "No files available in uploads directory");
                            return;
                        }

                        StringBuilder fileList = new StringBuilder("Available files:\n");
                        for (File file : files) {
                            fileList.append("- ").append(file.getName()).append(" (")
                                    .append(file.length() / 1024).append(" KB)\n");
                        }
                        JOptionPane.showMessageDialog(frame, fileList.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                exitButton.addActionListener(e -> {
                    try {
                        // Unregister before exiting
                        registry.unregisterPeer(peerName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    System.exit(0);
                });

                frame.add(uploadButton);
                frame.add(downloadButton);
                frame.add(browseButton);
                frame.add(exitButton);
                frame.setVisible(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
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