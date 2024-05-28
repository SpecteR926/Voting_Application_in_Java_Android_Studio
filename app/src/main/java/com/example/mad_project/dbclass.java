package com.example.mad_project;

public class dbclass {
    Boolean voted;
    String party;

    public dbclass(String party, Boolean voted) {
        this.party = party;
        this.voted = voted;
    }

    public dbclass() {

    }

    public String getVoted() {
        return party;
    }

    public void setVoted(Boolean voted) {
        this.voted = voted;
    }
}
