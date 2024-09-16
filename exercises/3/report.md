# Experiment 3 - Report

In this experiment, I have implemented a simple web application in React that communicates with the API created in experiment 2.

The app first prompts the user for a username (state is not persisted currently), and then displays a page where the user can create a new poll, list all polls, and vote on a poll.

See the code [here](../../polls)

## Further work

The polls are not automatically refetched after a poll is created.

The votes API currently does not return the user who created the vote, meaning it is not possible to make sure an existing vote by a user is updated instead of a new one being created. There are also no constraints preventing a single user from creating many votes. In this version, votes are only added, not updated, although the API supports it.

The styling can also be improved.
