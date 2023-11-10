
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

m1: 
	$(JC) $(CP) Node M1 True INSTANT
m2: 
	$(JC) $(CP) Node M2 True M2
m3: 
	$(JC) $(CP) Node M3 True M3
m4: 
	$(JC) $(CP) Node M4 F INSTANT
m5: 
	$(JC) $(CP) Node M5 F INSTANT
m6: 
	$(JC) $(CP) Node M6 F INSTANT
m7: 
	$(JC) $(CP) Node M7 F INSTANT
m8: 
	$(JC) $(CP) Node M8 F INSTANT
m9: 
	$(JC) $(CP) Node M9 F INSTANT

# Testing 
run-tests: 
	./test0.sh
	./test1.sh
	./test2.sh
	./test3.sh
	./test4.sh
	./test-checker.sh test0
	./test-checker.sh test1
	./test-checker.sh test2
	./test-checker.sh test3
	./test-checker.sh test4
# Cleaning utils 
clean: 	
	$(RM) *.class
	$(RM) *.json

clean-json: 
	$(RM) *.json
