# Assignment 3 - Paxos
## How to Run 
To compile the project run the following command from the root directory of the project:
```
make
```

Then, run the following command to start the ConnectionServer:
```
make server
```

To run the nodes with the precondition that the ConnectionServer is running, run the following commands in separate terminals:
```
make m1
make m2 
...
make m3
```

If you want more control, heres how you can run a node and fully configure it:

```
java -cp .:./json.jar Node <nodeName> <TRUE> <Profile>
```
* **nodeName**: The name of the node. Must be unique. Stick to M1-M9
* **TRUE**: Whether the node is a proposer or not. Input TRUE if it is, anything else if it isn't.
* **Profile**: The profile of the node. 
    * **INSTANT** : The node instantly sends and receives messages.
    * **M2** : ~4 second delay before sending messages, 20% chance of dropping messages.
    * **M3** : ~1 second delay before sending messages, 50% chance of dropping messages.
    * **SMALL** : ~0.5 second delay before sending messages, 5% chance of dropping messages.
    * **LARGE**: ~1 second delay before sending messages, 5% chance of dropping messages.

### Warnings / Exceptions 
After an Acceptor has received a DECIDE message, an exception occurs which looks like the following: 
```
...
Exception in thread "main" java.lang.NullPointerException
        at Node.handleDecideMessage(Node.java:365)
        at Node.connect(Node.java:130)
        at Node.main(Node.java:427)
java.io.IOException: Stream closed
        at java.io.BufferedReader.ensureOpen(BufferedReader.java:122)
        at java.io.BufferedReader.ready(BufferedReader.java:444)
        at Node.lambda$connect$0(Node.java:75)
        at java.lang.Thread.run(Thread.java:748)
bash-4.2$ 
```
This doesn't change the functionality of the system however, as the decided value can be seen above the exception. 
It's caused by my threads not exiting safely and is something I would fix if I had more time.

## Design Overview 
The system is comprised of 2 main components: the ConnectionServer, and the Node. The Node contains the Paxos logic and the ConnectionServer is responsible for handling incoming connections and broacasting messages to all nodes. It is important to note though that messages are still functionally unicast as Nodes ignore any messages that are not addressed to them.

### Message Design 
I based my implementation off of the lecture slides and the one described in [Understanding Paxos](https://people.cs.rutgers.edu/~pxk/417/notes/paxos.html)

Messages are generally structured as follows: ```TO:FROM:TYPE:PROPOSALNUMBER:VALUE```

My Node implementation uses the following message types:
- **PREPARE** 
- **PROMISE**
- **PROPOSE**
- **ACCEPT**
- **DECIDE**

Since there are no learners, a Proposer will send a **DECIDE** message to all Nodes via the ConnectionServer once consensus is reached. Upon receiving a decide message, a Node will print out the decided value and exit.

I also opted to not have any rejection messages or NACKs, since I felt they were unncessary as the Proposers already have a timeout mechanism and the Acceptors will ignore any messages that are not addressed to them. Rejection messages would be good for performance however. 

### Storage

The storage for each `Node` is managed through JSON files that record the state relevant to the Paxos protocol. Each node's file contains:

- `name`: Node's identifier.
- `proposer`: Boolean indicating if the node is a proposer.
- `proposal_accepted`: Boolean indicating if a proposal has been accepted.
- `accepted_id`: The highest proposal number accepted.
- `accepted_value`: The value associated with `accepted_id`.
- `max_id`: The highest proposal number seen.

These files are crucial for persisting state across the protocol's execution, allowing for recovery and consistency. The storage is managed by `JSONUtils`, ensuring atomic read/write operations. Before tests or system restarts, the storage should be cleaned to prevent state carry-over. Alternatively, the storage can be manually edited to simulate a node's state (e.g. to simulate a node having already accepted a proposal).

## Testing
To do testing I've created a test suite that runs a series of tests to ensure that the system is working as expected. Each test puts its printouts in separate folders labelled test0, test1, etc.

The suite is comprised of bash scripts which utilise the makefile to run the system. 

**NOTE**: Do not run any processes on port 12345 as this is the port the ConnectionServer uses.

### Running Tests
To run all tests run the following command from the root directory of the project:

```
make clean
make run-tests 
```

Note: It's important to run `make clean` before running `make run-tests` to ensure that no old storage files are used.

The test suite runs the following tests in order:

**Test 0:** This test ensures that consensus is reached when one node proposes 

**Test 1:** This test ensures that consensus is reached when two nodes propose different values at the same time.


