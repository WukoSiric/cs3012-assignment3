
JC = javac
CP = -cp

default: ConnectionServer.class Node.class 

ConnectionServer.class: 
	$(JC) $(CP) . ConnectionServer.java

Node.class:
	$(JC) $(CP) . Node.java

node: 
	java $(CP) . Node

server: 
	java $(CP) . ConnectionServer

clean: 	
	$(RM) *.class