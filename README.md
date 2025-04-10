# Distributed File Transfer with Java RMI and Edge Computing
## Group Members
HITIK ADWANI - 22BDS029  
LAKSHYA BABEL - 222BDS033  
ABHIJIT SINGH - 22BDS054  
SURYANSH AYUSH - 22BDS057

## Overview
This project presents a hybrid distributed system that combines edge computing and cloud infrastructure to efficiently process large-scale graph data. By using Java RMI for peer-to-peer communication, the system enables low-latency file access from the nearest peer, while Spring Boot manages coordination from a central cloud server. Files are partitioned into chunks using Java NIO with memory-mapped buffers, allowing parallel transfer with metadata for integrity and recovery. A Swing-based GUI provides real-time progress updates, and the project is managed using Maven and deployed via Docker containers for cross-platform support. Integrated into the existing Deezer Networks platform, this system is scalable, fault-tolerant, and optimized for real-world distributed graph processing applications.

## Dataset

Network dataset : [Deezer Networks](https://snap.stanford.edu/data/gemsec-Deezer.html).



## Project Setup

To set up this project locally, follow these steps:

### Step 1: Clone the Repository


```bash
git clone https://github.com/DataScience-ArtificialIntelligence/CardioVascular_Disease_Detection.git
```

### Step 2: Build the project
```bash
mvn clean install
``` 

### Step 3: Running the Application
```bash
mvn spring-boot:run
```

### Step 4: Start Peer Clients
```bash
java "-Djava.security.policy=client.policy" -cp "target/p2p-file-sharing-1.0.jar;target/classes" com.p2p.client.PeerClient Peer1
``` 
Replace Peer1 with different peer names for additional clients (e.g., Peer2, Peer3).

For Linux/Mac users, use colons instead of semicolons for the classpath separator:

```bash
java "-Djava.security.policy=client.policy" -cp "target/p2p-file-sharing-1.0.jar:target/classes" com.p2p.client.PeerClient Peer1
```









## Links

[Link to report](https://drive.google.com/file/d/1JpLNH9WYmP9kZZVV6KxdMLz7zsjYI0H_/view?usp=sharing)  
[Link to Presentation](https://docs.google.com/presentation/d/1oXNt0PcFWfBddSBP0KvbKE-uOzl32IeH/edit?usp=sharing&ouid=116995870568451359349&rtpof=true&sd=true)


