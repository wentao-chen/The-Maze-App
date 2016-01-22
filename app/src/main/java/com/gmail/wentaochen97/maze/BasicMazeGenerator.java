package com.gmail.wentaochen97.maze;

import java.util.Random;

/**
 * Created by Wentao-Admin on 2016-01-07.
 */
public abstract class BasicMazeGenerator implements MazeGenerator {

    private Integer desiredPlayers = 1;
    private int[] startX = null;
    private int[] startY = null;
    private int[] finishX = null;
    private int[] finishY = null;
    private Long randomSeed = null;

    public Integer getDesiredPlayers() {
        return this.desiredPlayers;
    }

    public void setDesiredPlayers(Integer desiredPlayers) {
        this.desiredPlayers = desiredPlayers;
    }

    public int getPlayers() {
        return startX == null ? 0 : startX.length;
    }

    public void setPlayers(Integer players) {
        setDesiredPlayers(null);
        if (players != null) {
            startX = new int[players];
            startY = new int[players];
            finishX = new int[players];
            finishY = new int[players];
        } else {
            startX = null;
            startY = null;
            finishX = null;
            finishY = null;
        }
    }

    int[] getStartX() {
        return this.startX;
    }

    public int getStartX(int i) {
        return this.startX[i];
    }

    int[] getStartY() {
        return this.startY;
    }

    public int getStartY(int i) {
        return this.startY[i];
    }

    public synchronized void setStart(int i, int startX, int startY) {
        this.startX[i] = startX;
        this.startY[i] = startY;
    }

    int[] getFinishX() {
        return this.finishX;
    }

    public int getFinishX(int i) {
        return this.finishX[i];
    }

    int[] getFinishY() {
        return this.finishY;
    }

    public int getFinishY(int i) {
        return this.finishY[i];
    }

    public synchronized void setFinish(int i, int finishX, int finishY) {
        this.finishX[i] = finishX;
        this.finishY[i] = finishY;
    }

    public Long getSeed() {
        return this.randomSeed;
    }

    public void setRandomSeed() {
        setSeed(new Random().nextLong());
    }

    public synchronized void setSeed(Long seed) {
        this.randomSeed = seed;
    }
}