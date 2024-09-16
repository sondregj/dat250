package no.hvl.student._1.dat250.exercise1;

import com.fasterxml.jackson.annotation.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/votes")
public class VoteController {

    @Autowired
    private PollManager pollManager;

    @GetMapping
    public List<Vote> getAllVotes() {
        return pollManager.getAllVotes();
    }

    @GetMapping("/{id}")
    public Vote getVote(@PathVariable String id) {
        return pollManager.getVote(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vote createVote(@RequestBody Vote vote) {
        pollManager.addVote(vote);
        return vote;
    }
}
