# Exercise 2 - Report

In this exercise, I have implemented RESTful APIs to manage users, votes, polls and poll vote options.

I reused the code from exercise 1, and added the following:

- Data models for users, votes, polls and poll vote options
- A manager class that stores the data in hashmaps
- REST controllers for the data models

[Code](../../src/main/java/no/hvl/student/_1/dat250/exercise1)

This is the test scenario I used:

1. Create a new user
2. List all users (-> shows the newly created user)
3. Create another user
4. List all users again (-> shows two users)
5. User 1 creates a new poll
6. List polls (-> shows the new poll)
7. User 2 votes on the poll
8. User 2 changes his vote
9. List votes (-> shows the most recent vote for User 2)
10. Delete the one poll
11. List votes (-> empty)

The tests were implemented as a shell script that runs curl commands to interact with the API.

[Tests](./test.sh)

## Remaining work

The implementation has some limitations and missing logic, and can be better organized.

Not all CRUD-operations were implemented, only the ones that were necessary to complete the proposed test case.

Could be a good idea to implement the API tests in Java.
