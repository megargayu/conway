package com.company.HashLife;

import java.util.Arrays;

/**
 * An implementation of the interface LifeAlgo for the Hashlife algorithm.
 */
public class HashLifeAlgo {

    public enum Status {
        OFF(0),
        ON(1);

        private final int value;
        Status(int value) {
            this.value = value;
        }
        int getValue() {
            return value;
        }
    }

    /**
     * The current state of the algorithm, represented by a HashLifeState.
     */
    HashLifeState s;

    public HashLifeAlgo() {
        s = new HashLifeState(new int[0][0]);
    }

    public HashLifeAlgo(int[][] grid) {
        s = new HashLifeState(grid);
    }

    public HashLifeAlgo(HashLifeState state) {
        s = new HashLifeState(state);
    }

    public void setState(HashLifeState state) {
        s = state.copy();
    }

    public HashLifeState getState() {
        return s.copy();
    }

    public void loadFromArray(int[][] array) {
        s = new HashLifeState(array);
    }

    public int[][] saveToArray() {
        return s.toArray();
    }

    public void setCellAt(int x, int y, Status status) {
        setCellAt(x, y, status.getValue());
    }

    public void setCellAt(int x, int y, int status) {
        s.setCellAt(y, x, status); // For some reason, these values are flipped around
    }

    public int toggleCellAt(int x, int y) {
        int state = s.getCellAt(y, x); // For some reason, these values are flipped around
        setCellAt(x, y, 1 - state);
        return state;
    }

    public int getCellAt(int x, int y) {
        return s.getCellAt(y, x); // For some reason, these values are flipped around
    }

    public void evolve() {
        evolve(1);
    }

    public void evolve(int steps) {
        s.evolve(steps);
    }

    public void print() {
        // For testing
        int[][] arr = saveToArray();
        for (int[] ints: arr) {
            for (int val: ints)
                if (val == Status.ON.getValue()) System.out.print("O  ");
                else                             System.out.print("X  ");
            System.out.println();
        }
    }

    public boolean isEmpty() {
        return s.state.off;
    }

    public static void main(String[] args) {
        HashLifeAlgo algo = new HashLifeAlgo();
        System.out.println(Arrays.deepToString(algo.saveToArray()));
        System.out.println(algo.s.state.off);
        algo.setCellAt(0, 0, Status.ON);
        algo.setCellAt(1, 0, Status.ON);
        algo.setCellAt(-1, 0, Status.ON);
        System.out.println(algo.s.state.off);
        algo.evolve();
        System.out.println(Arrays.deepToString(algo.saveToArray()));
        algo.print();
    }
}