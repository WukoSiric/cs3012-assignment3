# Assignment 3 - Paxos

## How to Run 

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

