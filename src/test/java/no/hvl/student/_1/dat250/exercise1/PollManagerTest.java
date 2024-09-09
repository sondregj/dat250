package no.hvl.student._1.dat250.exercise1;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PollManagerTest {

    private PollManager pollManager;

    @BeforeEach
    void setUp() {
        pollManager = new PollManager();
    }

    @Test
    void testPollManagerSequence() {
        // Create a new user
        User user1 = new User();
        user1.setUsername("bob");
        user1.setEmail("bob@example.com");
        pollManager.addUser(user1);

        // List all users (-> shows the newly created user)
        List<User> users = pollManager.getAllUsers();
        assertEquals(1, users.size());
        assertEquals("bob", users.get(0).getUsername());

        // Create another user
        User user2 = new User();
        user2.setUsername("alice");
        user2.setEmail("alice@example.com");
        pollManager.addUser(user2);

        // List all users again (-> shows two users)
        users = pollManager.getAllUsers();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("bob")));
        assertTrue(
            users.stream().anyMatch(u -> u.getUsername().equals("alice"))
        );

        // User 1 creates a new poll
        List<String> options = Arrays.asList("Red", "Green", "Blue");
        for (int i = 0; i < options.size(); i++) {
            VoteOption voteOption = new VoteOption();
            voteOption.setCaption(options.get(i));
            voteOption.setPresentationOrder(i);
            // pollManager.addPollVoteOption(voteOption);
        }
        Poll poll = new Poll();
        poll.setQuestion("Favorite color?");
        // poll.setOptions(new HashSet<>(options));
        poll.setCreator(user1);
        pollManager.addPoll(poll);

        // List polls (-> shows the new poll)
        List<Poll> polls = pollManager.getAllPolls();
        assertEquals(1, polls.size());
        assertEquals("Favorite color?", polls.get(0).getQuestion());

        // User 2 votes on the poll
        Vote vote1 = new Vote();
        vote1.setId("1");
        vote1.setPoll(poll);
        vote1.setUser(user2);
        // vote1.setSelectedOption("Red");
        // vote1.setVoteDate(new Date());
        pollManager.addVote(vote1);

        // User 2 changes his vote
        Vote vote2 = new Vote();
        vote2.setId("2");
        vote2.setPoll(poll);
        vote2.setUser(user2);
        pollManager.addVote(vote2);

        // List votes (-> shows the most recent vote for User 2)
        List<Vote> votes = pollManager.getAllVotes();
        // assertEquals(2, votes.size());
        // Vote latestVote = votes
        //     .stream()
        //     .filter(v -> v.getUser().getUsername().equals(user2.getUsername()))
        //     .max((v1, v2) -> v1.getVoteDate().compareTo(v2.getVoteDate()))
        //     .orElse(null);
        // assertNotNull(latestVote);
        // assertEquals("Blue", latestVote.getSelectedOption());

        // Delete the one poll
        pollManager.removePoll(poll.getId());

        // List votes (-> empty)
        votes = pollManager.getAllVotes();
        assertTrue(votes.isEmpty());
    }
}
