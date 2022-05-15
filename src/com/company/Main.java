package com.company;

import com.company.HashLife.HashLifeAlgo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Main {
    public static JFrame frame;
    public static Grid grid;
    public static HashLifeAlgo hashLifeAlgo;
    public static Controls controls;

    public static void main(String[] args) {
        hashLifeAlgo = new HashLifeAlgo();
        frame = new JFrame("Conway's Game of Life");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        grid = new Grid(new GridEvent() {
            @Override
            public void cellClicked(Point p) {
                hashLifeAlgo.toggleCellAt(p.x, p.y);
                grid.repaint();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE)
                    controls.togglePlaying();
            }
        });
        frame.setContentPane(grid);

        controls = new Controls();
        frame.add(controls);

        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.setVisible(true);
        frame.setResizable(false);
    }
}
