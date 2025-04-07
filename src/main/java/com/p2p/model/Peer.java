package com.p2p.model;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Peer extends Remote {
    String getName() throws RemoteException;
    byte[] downloadFile(String filename) throws RemoteException;
    void uploadFile(String filename, byte[] data) throws RemoteException;

    void runPageRank(String graphFile) throws RemoteException;
    void runHighestIndegree(String graphFile) throws RemoteException;
    void runBetweennessCentrality(String graphFile) throws RemoteException;
    void runClusteringCoefficient(String graphFile) throws RemoteException;
}