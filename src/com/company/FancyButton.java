package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FancyButton extends JButton {
    public Color defaultColor = Color.decode("#696969");
    public Color hoverColor = Color.decode("#B2B2B2");

    public FancyButton(ImageIcon icon) {
        super(new ImageIcon(icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
        setBackground(defaultColor);
        setBorderPainted(false);
        setBorder(null);
        setFocusable(false);
        setPreferredSize(new Dimension(35, 35));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(defaultColor);
            }
        });
    }

    public void changeIcon(ImageIcon icon) {
        setIcon(new ImageIcon(icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
    }
}
