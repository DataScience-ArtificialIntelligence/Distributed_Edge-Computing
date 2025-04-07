//package com.p2p.server;
//import com.p2p.model.Peer;
//
//import java.rmi.Remote;
//import java.rmi.RemoteException;
//
//public interface PeerRegistryInterface extends Remote {
//    void registerPeer(String peerName, Peer peer) throws RemoteException;
//    Peer getPeer(String peerName) throws RemoteException;
//}
package com.p2p.server;
import com.p2p.model.Peer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PeerRegistryInterface extends Remote {
    void registerPeer(String peerName, Peer peer) throws RemoteException;
    void unregisterPeer(String peerName) throws RemoteException;
    Peer getPeer(String peerName) throws RemoteException;
    List<String> getAvailablePeers() throws RemoteException;
}