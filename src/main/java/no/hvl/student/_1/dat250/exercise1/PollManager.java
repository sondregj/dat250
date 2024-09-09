package no.hvl.student._1.dat250.exercise1;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PollManager {

    private Map<String, Poll> polls = new HashMap<>();
    private Map<String, User> users = new HashMap<>();
    private Map<String, Vote> votes = new HashMap<>();
    private Map<String, VoteOption> voteOptions = new HashMap<>();

    public void addPoll(Poll poll) {
        String id = UUID.randomUUID().toString();
        poll.setId(id);
        polls.put(id, poll);

        for (VoteOption option : poll.getOptions()) {
            String optionId = UUID.randomUUID().toString();
            option.setId(optionId);
            option.setPoll(poll);
            voteOptions.put(optionId, option);
        }
    }

    public Poll getPoll(String id) {
        return polls.get(id);
    }

    public void removePoll(String id) {
        Poll poll = polls.get(id);
        if (poll == null) {
            throw new IllegalArgumentException("Poll not found");
        }
        for (Vote vote : this.getAllVotes()) {
            Poll votePoll = vote.getPoll();
            if (votePoll != null && votePoll.getId().equals(id)) {
                this.removeVote(vote.getId());
            }
        }
        // for (VoteOption option : poll.getOptions()) {
        //     this.removePollVoteOption(option.getId());
        // }
        polls.remove(id);
    }

    public void addPollVoteOption(String pollId, VoteOption voteOption) {
        Poll poll = polls.get(pollId);
        if (poll == null) {
            throw new IllegalArgumentException("Poll not found");
        }
        Set<VoteOption> options = poll.getOptions();
        options.add(voteOption);
        voteOption.setPoll(poll);
        String optionId = UUID.randomUUID().toString();
        voteOption.setId(optionId);
        voteOptions.put(optionId, voteOption);
        poll.setOptions(options);
    }

    public Set<VoteOption> getPollVoteOptions(String id) {
        Poll poll = polls.get(id);
        if (poll == null) {
            throw new IllegalArgumentException("Poll not found");
        }
        return poll.getOptions();
    }

    public void removePollVoteOption(String id) {
        VoteOption option = voteOptions.remove(id);
        if (option != null && option.getPoll() != null) {
            option.getPoll().getOptions().remove(option);
        }
    }

    public Set<Vote> getPollVotes(String id) {
        Poll poll = polls.get(id);
        if (poll == null) {
            throw new IllegalArgumentException("Poll not found");
        }
        return this.getAllVotes()
            .stream()
            .filter(vote -> vote.getPoll().getId().equals(id))
            .collect(Collectors.toSet());
    }

    public void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    public User getUser(String id) {
        return users.get(id);
    }

    public void removeUser(String id) {
        users.remove(id);
    }

    public String addVote(Vote vote) {
        String id = UUID.randomUUID().toString();
        vote.setId(id);
        votes.put(id, vote);
        if (vote.getUser() != null) {
            vote.getUser().getVotes().add(vote);
        }
        return id;
    }

    public Vote getVote(String id) {
        return votes.get(id);
    }

    public VoteOption getVoteOption(String id) {
        return voteOptions.get(id);
    }

    public void removeVote(String id) {
        Vote vote = votes.remove(id);
        if (vote != null && vote.getUser() != null) {
            vote.getUser().getVotes().remove(vote);
        }
    }

    public List<Poll> getAllPolls() {
        return new ArrayList<>(polls.values());
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public List<Vote> getAllVotes() {
        return new ArrayList<>(votes.values());
    }
}

class User {

    private String username;
    private String email;

    @JsonManagedReference("user-polls")
    private List<Poll> createdPolls;

    @JsonManagedReference("user-votes")
    private List<Vote> votes;

    public User() {
        this.createdPolls = new ArrayList<>();
        this.votes = new ArrayList<>();
    }

    public User(String username, String email) {
        this();
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Poll> getCreatedPolls() {
        return createdPolls;
    }

    public void setCreatedPolls(List<Poll> createdPolls) {
        this.createdPolls = createdPolls;
    }

    public List<Vote> getVotes() {
        return votes;
    }

    public void setVotes(List<Vote> votes) {
        this.votes = votes;
    }
}

@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
class Poll {

    private String id;

    private String question;

    @JsonManagedReference("poll-options")
    private Set<VoteOption> options;

    @JsonBackReference("user-polls")
    private User creator;

    private Instant closesAt;

    public Poll() {}

    public Poll(String question, Set<VoteOption> options, User creator) {
        this.question = question;
        this.options = options;
        this.creator = creator;
        if (creator != null && creator.getCreatedPolls() != null) {
            creator.getCreatedPolls().add(this);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getClosesAt() {
        return closesAt;
    }

    public void setClosesAt(Instant closesAt) {
        this.closesAt = closesAt;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Set<VoteOption> getOptions() {
        return options;
    }

    public void setOptions(Set<VoteOption> options) {
        this.options = options;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
        if (
            creator != null &&
            creator.getCreatedPolls() != null &&
            !creator.getCreatedPolls().contains(this)
        ) {
            creator.getCreatedPolls().add(this);
        }
    }
}

@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
class Vote {

    private String id;

    @JsonBackReference("user-votes")
    private User user;

    private VoteOption option;

    public Vote(User user, VoteOption option) {
        this.user = user;
        this.option = option;
        if (user != null && user.getVotes() != null) {
            user.getVotes().add(this);
        }
    }

    public Vote() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (
            user != null &&
            user.getVotes() != null &&
            !user.getVotes().contains(this)
        ) {
            user.getVotes().add(this);
        }
    }

    public Poll getPoll() {
        return option != null ? option.getPoll() : null;
    }

    public VoteOption getOption() {
        return option;
    }

    public void setOption(VoteOption option) {
        this.option = option;
    }
}

@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
class VoteOption {

    private String id;

    @JsonBackReference("poll-options")
    private Poll poll;

    private String caption;
    private int presentationOrder;

    public VoteOption() {}

    public VoteOption(int presentationOrder, String caption) {
        this.presentationOrder = presentationOrder;
        this.caption = caption;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public Poll getPoll() {
        return poll;
    }

    public int getPresentationOrder() {
        return presentationOrder;
    }

    public void setPresentationOrder(int presentationOrder) {
        this.presentationOrder = presentationOrder;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
