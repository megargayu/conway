package com.company;

import java.awt.*;
import java.awt.event.KeyEvent;

public abstract class GridEvent {
    public void cellClicked(Point p) {} // Runs when a cell is clicked on the grid
    public void keyPressed(KeyEvent e) {} // Returns a key when it is pressed
}
