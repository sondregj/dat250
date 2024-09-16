import { useState } from "react";
import "./App.css";
import { CreatePoll, CreateUser, Vote } from "./Polls";

function App() {
  const [username, setUsername] = useState("");
  return (
    <>
      <h1>Polls</h1>
      {username === "" ? (
        <CreateUser setUsername={setUsername} />
      ) : (
        <>
          <p>Welcome, {username}!</p>
          <CreatePoll />
          <Vote username={username} />
        </>
      )}
    </>
  );
}

export default App;
