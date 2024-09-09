package no.hvl.student._1.dat250.exercise1;

import com.fasterxml.jackson.annotation.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/polls")
public class PollController {

    @Autowired
    private PollManager pollManager;

    @GetMapping
    public List<Poll> getAllPolls() {
        return pollManager.getAllPolls();
    }

    @GetMapping("/{id}")
    public Poll getPoll(@PathVariable String id) {
        return pollManager.getPoll(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Poll createPoll(@RequestBody CreatePoll createPoll) {
        Poll poll = new Poll();
        User user = pollManager.getUser(createPoll.username);
        poll.setCreator(user);
        poll.setQuestion(createPoll.question);
        Set<VoteOption> options = new HashSet<>();
        for (String option : createPoll.options) {
            VoteOption voteOption = new VoteOption();
            voteOption.setCaption(option);
            voteOption.setPresentationOrder(createPoll.options.indexOf(option));
            voteOption.setPoll(poll);
            options.add(voteOption);
        }
        poll.setOptions(options);
        pollManager.addPoll(poll);
        for (VoteOption voteOption : options) {
            pollManager.addPollVoteOption(poll.getId(), voteOption);
        }
        return poll;
    }

    @PutMapping("/{id}")
    public Poll updatePoll(@PathVariable String id, @RequestBody Poll poll) {
        poll.setId(id);
        pollManager.addPoll(poll);
        return poll;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePoll(@PathVariable String id) {
        pollManager.removePoll(id);
    }

    @GetMapping("/{id}/options")
    @ResponseStatus(HttpStatus.OK)
    public Set<VoteOption> getOptions(@PathVariable String id) {
        return pollManager.getPollVoteOptions(id);
    }

    @PostMapping("/{id}/options")
    @ResponseStatus(HttpStatus.CREATED)
    public void addOption(
        @PathVariable String id,
        @RequestBody VoteOption option
    ) {
        pollManager.addPollVoteOption(id, option);
    }

    @GetMapping("/{id}/votes")
    @ResponseStatus(HttpStatus.OK)
    public Set<Vote> getVotes(@PathVariable String id) {
        return pollManager.getPollVotes(id);
    }

    @PostMapping("/{id}/votes")
    @ResponseStatus(HttpStatus.CREATED)
    public Vote addVote(@PathVariable String id, @RequestBody UserVote vote) {
        // TODO: Check if user has already voted
        Poll poll = pollManager.getPoll(id);

        VoteOption selected = null;
        for (VoteOption v : poll.getOptions()) {
            if (v.getId().equals(vote.optionId)) {
                selected = pollManager.getVoteOption(vote.optionId);
                break;
            }
        }
        if (selected == null) {
            throw new IllegalArgumentException("Invalid option");
        }

        Vote newVote = new Vote();

        User user = pollManager.getUser(vote.username);
        newVote.setUser(user);
        newVote.setOption(selected);
        pollManager.addVote(newVote);
        return newVote;
    }

    @PutMapping("/{id}/votes/{voteId}")
    @ResponseStatus(HttpStatus.OK)
    public Vote updateVote(
        @PathVariable String id,
        @PathVariable String voteId,
        @RequestBody UserVote vote
    ) {
        Vote currentVote = pollManager.getVote(voteId);
        if (vote == null) {
            throw new IllegalArgumentException("Vote not found");
        }
        if (!currentVote.getUser().getUsername().equals(vote.username)) {
            throw new IllegalArgumentException("Vote does not belong to user");
        }
        currentVote.setOption(pollManager.getVoteOption(vote.optionId));
        return currentVote;
    }
}

class CreatePoll {

    public String username;
    public String question;
    public List<String> options;
}

class UserVote {

    public String username;
    public String optionId;
}
