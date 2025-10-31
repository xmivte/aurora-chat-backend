#!/bin/bash

read -r -p "Choose project type FE/BE: " PROJECT_TYPE
read -r -p "Enter your GitLab project access token: " GITLAB_REPO_ACCESS_TOKEN
read -r -p "Enter the GitLab project ID: " GITLAB_PROJECT_ID

VARIABLE_NAMES=()

if [ "$PROJECT_TYPE" == "FE" ]; then
    VARIABLE_NAMES=("BEARER_TOKEN" "GIT_CLONE_AUTH_FE" "HOST" "TEAM_NAME")
elif [ "$PROJECT_TYPE" == "BE" ]; then
    VARIABLE_NAMES=("BEARER_TOKEN" "GIT_CLONE_AUTH_BE" "HOST" "TEAM_NAME")
else
    echo "Your input parameter <$PROJECT_TYPE> is not supported, it should match one of these cases: FE/BE"
    exit 0
fi

for VARIABLE_NAME in "${VARIABLE_NAMES[@]}"; do
    if [ "$VARIABLE_NAME" == "HOST" ]; then
        read -r -p "Enter the value for '$VARIABLE_NAME' (Ex. teamname.devbstaging.com): " VARIABLE_VALUE
    else
        read -r -p "Enter the value for '$VARIABLE_NAME': " VARIABLE_VALUE
    fi

    curl -sS --request POST \
        --header "PRIVATE-TOKEN: $GITLAB_REPO_ACCESS_TOKEN" \
        "https://gitlab.devbstaging.com/api/v4/projects/$GITLAB_PROJECT_ID/variables" \
        --form "key=$VARIABLE_NAME" --form "value=$VARIABLE_VALUE" | jq .
done

echo "$TEAM_NAME GitLab pipeline variables for $PROJECT_TYPE were created successfully!"