//package com.p2p.server;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;
//
//@SpringBootApplication
//public class P2PServer {
//    public static void main(String[] args) {
//        try {
//            Registry registry = LocateRegistry.createRegistry(1099);
//            PeerRegistry peerRegistry = new PeerRegistry();
//            registry.rebind("PeerRegistry", peerRegistry);
//            System.out.println("Peer-to-Peer Registry running on port 1099...");
//            SpringApplication.run(P2PServer.class, args);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
package com.p2p.server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

@SpringBootApplication
public class P2PServer {
    public static void main(String[] args) {
        try {
            // Get the server's IP address
            String ipAddress = InetAddress.getLocalHost().getHostAddress();

            // Set system properties for RMI
            System.setProperty("java.rmi.server.hostname", ipAddress);
            // Allow remote connections to download classes
            System.setProperty("java.rmi.server.useCodebaseOnly", "false");

            // Create RMI registry
            Registry registry = LocateRegistry.createRegistry(1099);

            // Create and bind the PeerRegistry
            PeerRegistry peerRegistry = new PeerRegistry();
            registry.rebind("PeerRegistry", peerRegistry);

            System.out.println("P2P Registry Server running at " + ipAddress + ":1099");
            System.out.println("RMI URL: rmi://" + ipAddress + "/PeerRegistry");

            // Start Spring Boot application
            SpringApplication.run(P2PServer.class, args);
        } catch (Exception e) {
            System.err.println("Server startup error:");
            e.printStackTrace();
        }
    }
}