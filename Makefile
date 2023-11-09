
JCC = javac
JC = java
JFLAGS = -g
CP = -cp .:./json.jar

default: ConnectionServer.class Node.class

ConnectionServer.class: 
	$(JCC) $(JFLAGS) $(CP) ConnectionServer.java

Node.class:
	$(JCC) $(JFLAGS) $(CP) Node.java

server: 
	$(JC) $(CP) ConnectionServer

clean: 	
	$(RM) *.class
	$(RM) *.json
