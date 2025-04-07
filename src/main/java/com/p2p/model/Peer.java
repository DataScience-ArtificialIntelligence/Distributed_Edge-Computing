package com.p2p.model;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Peer extends Remote {
    String getName() throws RemoteException;
    byte[] downloadFile(String filename) throws RemoteException;
    void uploadFile(String filename, byte[] data) throws RemoteException;
}