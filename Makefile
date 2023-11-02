
JC = javac
CP = -cp

default: ConnectionServer.class CouncilMember.class 

ConnectionServer.class: 
	$(JC) $(CP) . ConnectionServer.java

CouncilMember.class:
	$(JC) $(CP) . CouncilMember.java

node: 
	java $(CP) . CouncilMember

server: 
	java $(CP) . ConnectionServer

clean: 	
	$(RM) *.class