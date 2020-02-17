package com.stillcoolme.basic.bean;

/**
 * Author: stillcoolme
 * Date: 2019/7/25 19:55
 * Description:
 */
public class Result {
    int id;
    float score;

    public Result() {
    }

    public Result(int id, float score) {
        this.id = id;
        this.score = score;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getScore() {
        return this.score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}

