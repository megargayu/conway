package com.company.HashLife;

/**
 * A specialized LifeState for the Hashlife algorithm.
 */
public class HashLifeState {

    /**
     * The current state is stored by a simple MacroCell, always simplified.
     */
    public MacroCell state;

    /**
     * @param state to initialize the HashLifeState
     */
    HashLifeState(MacroCell state) {
        this.state = state.simplify();
    }

    /**
     * Initialize the HashLifeState with an array.
     *
     * @param array an int array
     */
    HashLifeState(int[][] array) {
        this.state = Memorization.fromArray(array);
    }

    /**
     * @param other another HashLifeState
     */
    HashLifeState(HashLifeState other) {
        this.state = other.state;
    }

    /**
     * @return a copy of this HashLifeState
     */
    HashLifeState copy() {
        return new HashLifeState(this);
    }

    /**
     * @param x the x coordinate of the cell to get
     * @param y the y coordinate of the cell to get
     * @return the state of the cell
     */
    int getCellAt(int x, int y) {
        return state.getCell(x + state.size/2, y + state.size/2);
    }

    /**
     * @param x the x coordinate of the cell to get
     * @param y the y coordinate of the cell to get
     * @param newState the new state of the cell
     */
    void setCellAt(int x, int y, int newState) {
        state = state.setCell(x + state.size/2, y + state.size/2, newState);
    }

    /**
     * @return an array representing the state of the universe
     */
    int[][] toArray() {
        return state.toArray();
    }

    /**
     * @param steps the number of generations forward to evolve to
     */
    void evolve(int steps) {
        int s = 32 - Integer.numberOfLeadingZeros(steps);
        int n = 1<<s;

        //Make sure we can go as far in the futur as we want
        for(int i = 0; i<=s; i++)
            state = state.borderize().borderize();
        //We are using a binary decomposition as state.result(s) works with powers of two
        while(n > 0) {
            if((steps&n) != 0){
                state = state.result(s).borderize();
            }
            n /= 2;
            s--;
        }

        //Delete unnecessary borders introduced by borderize()
        state = state.simplify();
    }
}