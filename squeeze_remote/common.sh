#!/usr/bin/env bash

export BGreen='\033[1;32m'
export NC='\033[0m'

export DEFAULT_SSH_USER="root"
export DEFAULT_SSH_PASS="12345"

read_ssh_params() {
    local default_remote="$1"
    local default_user="${2:-$DEFAULT_SSH_USER}"
    local default_pass="${3:-$DEFAULT_SSH_PASS}"

    echo -e "${BGreen}SSH${NC}"

    echo -e "Enter SSH ip. ${BGreen}Press Enter for [$default_remote]${NC}"
    read -p "" remote_input
    remote="${remote_input:-$default_remote}"

    echo -e "Enter SSH username. ${BGreen}Press Enter for [$default_user]:${NC} "
    read -p "" user_input
    username="${user_input:-$default_user}"

    echo -e "Enter SSH password. ${BGreen}Press Enter for [$default_pass]:${NC} "
    read -p "" pass_input
    password="${pass_input:-$default_pass}"

    export remote username password
}
