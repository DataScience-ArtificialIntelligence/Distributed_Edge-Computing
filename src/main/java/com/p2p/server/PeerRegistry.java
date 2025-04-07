//package com.p2p.server;
//import com.p2p.model.Peer;
//import java.rmi.RemoteException;
//import java.rmi.server.UnicastRemoteObject;
//import java.util.HashMap;
//import java.util.Map;
//
//public class PeerRegistry extends UnicastRemoteObject implements PeerRegistryInterface {
//    private final Map<String, Peer> peers = new HashMap<>();
//
//    public PeerRegistry() throws RemoteException {}
//
//    public void registerPeer(String name, Peer peer) throws RemoteException {
//        peers.put(name, peer);
//        System.out.println("Registered peer: " + name);
//    }
//
//    public Peer getPeer(String name) throws RemoteException {
//        return peers.get(name);
//    }
//}
package com.p2p.server;
import com.p2p.model.Peer;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeerRegistry extends UnicastRemoteObject implements PeerRegistryInterface {
    private final Map<String, Peer> peers = new HashMap<>();

    public PeerRegistry() throws RemoteException {}

    @Override
    public void registerPeer(String name, Peer peer) throws RemoteException {
        peers.put(name, peer);
        System.out.println("Registered peer: " + name);
    }

    @Override
    public void unregisterPeer(String name) throws RemoteException {
        if (peers.remove(name) != null) {
            System.out.println("Unregistered peer: " + name);
        }
    }

    @Override
    public Peer getPeer(String name) throws RemoteException {
        return peers.get(name);
    }

    @Override
    public List<String> getAvailablePeers() throws RemoteException {
        return new ArrayList<>(peers.keySet());
    }
}