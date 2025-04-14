package com.crio.qcontest.entities;

public class User {
    private final Long id;
    private final String name;
    private Integer score;
    private static long idCounter = 1;

    public User(String name) {
        this.id = idCounter++;
        this.name = name;
        this.score = 1500;
    }

    public User(Long id, User other){
        this.id = id;
        this.name = other.name;
        this.score = other.score;
    }

    public String getName() {
        return name;
    }

    public Integer getScore() {
        return score;
    }

    public Long getId() {
        return id;
    }

    public void setScore(Integer score){
        this.score = score;
    }

    @Override
    public String toString() {
        return "User [id=" + id + "]";
    }  
}