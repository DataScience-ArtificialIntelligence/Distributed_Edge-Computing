# Distributed File Transfer with Java RMI and Edge Computing
## Group Members
HITIK ADWANI - 22BDS029  
LAKSHYA BABEL - 222BDS033  
ABHIJIT SINGH - 22BDS054  
SURYANSH AYUSH - 22BDS057

## Overview
This project presents a scalable and efficient peer-to-peer system built with Java RMI, Spring Boot, and Swing. Features like file partitioning, edge computing, and Docker containerization improve performance and portability. The system serves as a reusable service for distributed tasks, with future enhancements planned for security, fault tolerance, and collaborative peer interaction.


## Dataset

Network dataset : [Deezer Networks](https://snap.stanford.edu/data/Deezer.).



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

[Link to report](https://drive.google.com/file/d/1EnGT4l5aFFk4n5-gPtrUEaNzAD8RkBaM/view?usp=drive_link)  


