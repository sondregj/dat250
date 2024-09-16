// CreateUserComponent, CreatePollComponent, VoteComponent

import { useState, useEffect } from "react";

interface CreateUserProps {
  setUsername: (username: string) => void;
}
export function CreateUser({ setUsername }: CreateUserProps): JSX.Element {
  const createUser = async (event: React.FormEvent) => {
    event.preventDefault();
    const username = (event.target as HTMLFormElement).username.value;
    const response = await fetch("http://localhost:8080/api/users", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ username }),
    });
    if (!response.ok) {
      console.error("Failed to create user");
      return;
    }
    const data = await response.json();
    console.log(data);
    setUsername(username);
  };
  return (
    <div>
      <h2>Create User</h2>
      <form onSubmit={createUser}>
        <label htmlFor="username">Username</label>
        <input type="text" id="username" name="username" />
        <br />
        <button type="submit">Create User</button>
      </form>
    </div>
  );
}

// interface CreatePollInput {
//   id: string;
//   question: string;
//   options: string[];
// }

export function CreatePoll(): JSX.Element {
  const [options, setOptions] = useState<string[]>([""]);

  const createPoll = async (event: React.FormEvent) => {
    event.preventDefault();
    const question = (event.target as HTMLFormElement).question.value;
    const options = Array.from(
      (event.target as HTMLFormElement).querySelectorAll("input[name^=option]"),
    ).map((input) => input.value);

    const response = await fetch("http://localhost:8080/api/polls", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ question, options }),
    });
    const data = await response.json();
    console.log(data);
    // clear
    setOptions([""]);
  };

  return (
    <div style={{ display: "block" }}>
      <h2>Create Poll</h2>

      <form onSubmit={createPoll}>
        <label htmlFor="question">Question</label>
        <input type="text" id="question" name="question" />

        <div>
          <label htmlFor="options">Options</label>
          {options.map((option, index) => (
            <div>
              <input
                key={index}
                type="text"
                id={`option-${index}`}
                name={`option-${index}`}
                value={option}
                onChange={(event) => {
                  const newOptions = [...options];
                  newOptions[index] = event.target.value;
                  setOptions(newOptions);
                }}
              />
              <button
                type="button"
                onClick={() => {
                  const newOptions = [...options];
                  newOptions.splice(index, 1);
                  setOptions(newOptions);
                }}
              >
                Remove
              </button>
            </div>
          ))}
          <button
            type="button"
            onClick={() => {
              setOptions([...options, ""]);
            }}
          >
            Add Option
          </button>
        </div>

        <button type="submit">Create Poll</button>
      </form>
    </div>
  );
}

interface Poll {
  id: string;
  question: string;
  options: Array<{
    id: string;
    caption: string;
    presentationOrder: number;
  }>;
}

interface Vote {
  id: string;
  pollId: string;
  option: {
    id: string;
    caption: string;
    presentationOrder: number;
  };
}

export function Vote({ username }: { username: string }): JSX.Element {
  const [polls, setPolls] = useState<Poll[]>([]);
  const [votes, setVotes] = useState<{ [key: string]: Vote[] }>({});
  const fetchPolls = async () => {
    const response = await fetch("http://localhost:8080/api/polls");
    const data = await response.json();
    setPolls(data);
  };
  useEffect(() => {
    fetchPolls();
  }, []);
  const fetchPollVotes = async (pollId: string) => {
    const response = await fetch(
      "http://localhost:8080/api/polls/" + pollId + "/votes",
    );
    const data = await response.json();
    console.log("Votes", data);
    setVotes((votes) => ({ ...votes, [pollId]: data }));
  };
  useEffect(() => {
    polls.forEach((poll) => {
      fetchPollVotes(poll.id);
    });
    console.log("Polls changed");
  }, [polls]);

  console.log("votes", votes);
  const vote = async (pollId: string, optionId: string, voteId?: string) => {
    const response = await fetch(
      "http://localhost:8080/api/polls/" + pollId + "/votes",
      {
        method: voteId ? "PUT" : "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ username, optionId, voteId }),
      },
    );
    const data = await response.json();
    console.log("Vote", data);
  };

  return (
    <div>
      <h2>Vote</h2>
      <button onClick={fetchPolls}>Fetch Polls</button>

      {polls.map((poll) => (
        <div>
          <h3>Question: {poll.question}</h3>
          <ul>
            {poll.options
              .sort((a, b) => a.presentationOrder - b.presentationOrder)
              .map((option) => (
                <li key={option.id}>
                  {option.caption}
                  {votes[poll.id] && (
                    <span>
                      {" "}
                      -{" "}
                      {
                        votes[poll.id]?.filter(
                          (vote) =>
                            (typeof vote.option === "string" &&
                              vote.option === option.id) ||
                            vote.option.id === option.id,
                        ).length
                      }
                    </span>
                  )}
                  <button
                    onClick={() =>
                      vote(
                        poll.id,
                        option.id,
                        // TODO: make sure it is the users own vote
                        // votes[poll.id]?.length > 0
                        //   ? typeof votes[poll.id][0].option === "string"
                        //     ? votes[poll.id][0].option
                        //     : votes[poll.id][0].option.id
                        //   : undefined,
                      )
                    }
                  >
                    Vote
                  </button>
                </li>
              ))}
          </ul>
        </div>
      ))}
    </div>
  );
}
