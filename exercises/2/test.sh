#!/bin/bash

set -e

header="Content-Type: application/json"
base_url="http://localhost:8080"

create_user() {
    response=$(curl -X POST -H "$header" -d '{"username":"'$1'"}' $base_url/api/users -w "%{http_code}" -s -o /dev/null)
    if [ "$response" -eq 200 ] || [ "$response" -eq 201 ]; then
        echo "User $1 created successfully"
    else
        echo "Failed to create user. Status code: $response"
        exit 1
    fi
}

# Create a new user
user_1="bob"
create_user $user_1

# List all users (-> shows the newly created user)
curl -X GET -H "$header" $base_url/api/users
echo

# Create another user
user_2="alice"
create_user $user_2

# List all users again (-> shows two users)
curl -X GET -H "$header" $base_url/api/users
echo

# User 1 creates a new poll
response=$(curl -X POST -H "$header" -d '{"question":"What is your favorite color?", "options":["red", "green", "blue"]}' $base_url/api/polls -s)
echo "Create poll response: $response"
poll_id=$(echo $response | jq -r '.id')
if [ -n "$poll_id" ]; then
    echo "Poll created successfully: $poll_id"
else
    echo "Failed to create poll"
    exit 1
fi

# List polls (-> shows the new poll)
response=$(curl -X GET -H "$header" $base_url/api/polls -s )
all_poll_ids=$(echo $response | jq -r '.[].id')
if [[ $all_poll_ids == *$poll_id* ]]; then
    echo "Poll found in list"
else
    echo "Poll not found in list: $all_poll_ids"
    exit 1
fi
# echo "Get polls response: $response"
poll_options=$(echo $response | jq '.[] | select(.id == "'$poll_id'") | .options')
first_option=$(echo $poll_options | jq -r '.[0].id')
second_option=$(echo $poll_options | jq -r '.[1].id')

# User 2 votes on the poll
# TODO: username should be based on auth
response=$(curl -X POST -H "$header" -d "{\"username\": \"$user_2\", \"optionId\":\"$first_option\"}" $base_url/api/polls/$poll_id/votes -s)
vote_id=$(echo $response | jq -r '.id')
echo "Vote created successfully: $vote_id"

# List votes (-> shows the vote for User 2)
response=$(curl -X GET -H "$header" $base_url/api/polls/$poll_id/votes -s)
echo "List votes before modify response: $(echo $response | jq '.[].option.caption')"

# User 2 changes his vote
response=$(curl -X PUT -H "$header" -d "{\"username\": \"$user_2\", \"optionId\":\"$second_option\"}" $base_url/api/polls/$poll_id/votes/$vote_id -s)

# List votes (-> shows the most recent vote for User 2)
response=$(curl -X GET -H "$header" $base_url/api/polls/$poll_id/votes -s)
echo "List votes after modify response: $(echo $response | jq '.[].option.caption')"

# Delete the one poll
curl -X DELETE -H "$header" $base_url/api/polls/$poll_id -s
echo "Poll deleted successfully"

# List votes (-> empty)
response=$(curl -X GET -H "$header" $base_url/api/polls/$poll_id -s)
if [ -z "$response" ]; then
    echo "Poll not found"
else
    echo "Poll still exists"
    exit 1
fi
