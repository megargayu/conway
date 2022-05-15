package com.company;

import com.company.HashLife.MacroCell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Grid extends JPanel {
    public final int endX = 10000, endY = 10000;
    public final int SPACING = 15;
    public double zoom = 1;
    public double locX = 0, locY = 0; // Start at center of grid
    public Point startSelect = null, endSelect = null;

    private class MouseControls extends MouseAdapter {
        private Thread dragger;
        private boolean dragging;
        private double multiplier = 1;

        @Override
        public void mousePressed(MouseEvent me) {
            Main.controls.mousePressed(me);
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            Main.controls.mouseReleased(me);
            dragging = false;
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            Main.controls.mouseDragged(me);
            dragging = true;
            if (dragger == null || !dragger.isAlive()) {
                dragger = new Thread(() -> {
                    multiplier = 1;
                    while (dragging) {
                        if (MouseInfo.getPointerInfo().getLocation().getY() < 30)
                            locY += 10 * multiplier / zoom;
                        else if (MouseInfo.getPointerInfo().getLocation().getY() >
                                Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 30)
                            locY -= 10 * multiplier / zoom;
                        else if (MouseInfo.getPointerInfo().getLocation().getX() < 30)
                            locX += 10 * multiplier / zoom;
                        else if (MouseInfo.getPointerInfo().getLocation().getX() >
                                Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 30)
                            locX -= 10 * multiplier / zoom;
                        else break;

                        try {
                            Thread.sleep(100);
                            repaint();
                        } catch (InterruptedException e) { e.printStackTrace(); }

                        if (multiplier < 3)
                            multiplier *= 1.1;
                    }
                });
                dragger.start();
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int ticks = e.getWheelRotation();
            double toZoom = zoom * Math.pow(1.1, -ticks);
            if (toZoom < 1.8 && toZoom > 0.05) {
                zoom = toZoom;
                repaint();
            }
        }
    }
    private class KeyboardControls extends KeyAdapter {
        private final GridEvent gridEvent;
        private int timesPressed = 0;
        private int lastKeyCode;

        public KeyboardControls(GridEvent gridEvent) {
            this.gridEvent = gridEvent;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    handleKeyPress(e.getKeyCode());
                    locX += Math.min(5 + 0.3 * timesPressed, 15);
                    repaint();
                    break;
                case KeyEvent.VK_RIGHT:
                    handleKeyPress(e.getKeyCode());
                    locX -= Math.min(5 + 0.3 * timesPressed, 15);
                    repaint();
                    break;
                case KeyEvent.VK_UP:
                    handleKeyPress(e.getKeyCode());
                    locY += Math.min(5 + 0.3 * timesPressed, 15);
                    repaint();
                    break;
                case KeyEvent.VK_DOWN:
                    handleKeyPress(e.getKeyCode());
                    locY -= Math.min(5 + 0.3 * timesPressed, 15);
                    repaint();
                    break;
                default:
                    gridEvent.keyPressed(e);
                    repaint();
            }
        }

        private void handleKeyPress(int keyCode) {
            if (lastKeyCode != keyCode) {
                lastKeyCode = keyCode;
                timesPressed = 1;
            } else timesPressed++;
        }
    }

    public Grid(GridEvent gridEvent) {
        setBackground(Color.DARK_GRAY);
        setLayout(new DragLayout());

        MouseControls mouseControls = new MouseControls();
        addMouseListener(mouseControls);
        addMouseMotionListener(mouseControls);
        addMouseWheelListener(mouseControls);

        setFocusable(true); // Needed for keyboard controls
        addKeyListener(new KeyboardControls(gridEvent));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        MacroCell cell = Main.hashLifeAlgo.getState().state;
        draw(g2, -cell.size / 2, -cell.size / 2, cell);

        if (startSelect != null && endSelect != null) {
            g2.setColor(new Color(50, 205, 50, 170));
            Point2D.Double startSelectCoord = toCoord(startSelect.x, startSelect.y),
                    endSelectCoord = toCoord(endSelect.x, endSelect.y);
            double x = Math.min(startSelectCoord.getX(), endSelectCoord.getX()),
                    y = Math.min(startSelectCoord.getY(), endSelectCoord.getY()),
                    w = Math.abs(endSelectCoord.getX() - startSelectCoord.getX()),
                    h = Math.abs(endSelectCoord.getY() - startSelectCoord.getY());
            g2.fill(new Rectangle2D.Double(x, y, w, h));
        }

        g2.setStroke(new BasicStroke(0.5f));
        toCoord(0, 0);
        if (zoom > 0.2) {
            for (double x = -(SPACING / 2d) + getWidth() / 2d + locX; x > 0; x -= SPACING) drawLineX(x, g2);
            for (double x = SPACING / 2d + getWidth() / 2d + locX; x < getWidth() / zoom; x += SPACING) drawLineX(x, g2);
            for (double y = -(SPACING / 2d) + getHeight() / 2d + locY; y > 0; y -= SPACING) drawLineY(y, g2);
            for (double y = SPACING / 2d + getHeight() / 2d + locY; y < getHeight() / zoom; y += SPACING) drawLineY(y, g2);
        }
    }

    private void draw(Graphics2D g2, int x, int y, MacroCell cellToDraw) {
        if (cellToDraw.off) return;

        if (cellToDraw.dim == 0) {
            Point2D.Double p = toCoord(x, y);

            // Don't render off-screen
            if (p.getX() + SPACING * zoom <= 0 || p.getX() >= getWidth() ||
                    p.getY() + SPACING * zoom <= 0 || p.getY() >= getHeight())
                return;

            g2.setColor(Color.white);
            g2.fill(new Rectangle2D.Double(p.getX(), p.getY(), SPACING * zoom, SPACING * zoom));
        } else {
            int offset = cellToDraw.size / 2;
            draw(g2, x, y, cellToDraw.quad(0));
            draw(g2, x + offset, y, cellToDraw.quad(1));
            draw(g2, x, y + offset, cellToDraw.quad(2));
            draw(g2, x + offset, y + offset, cellToDraw.quad(3));
        }
    }

    private void drawLineX(double x, Graphics2D g2) {
        g2.setColor(((x + endX / 2d - locX) / SPACING % 10 < 1) ? Color.LIGHT_GRAY : Color.GRAY);
        g2.draw(new Line2D.Double(x * zoom, 0, x * zoom, endY));
    }

    private void drawLineY(double y, Graphics2D g2) {
        g2.setColor(((y + endY / 2d - locY) / SPACING % 10 < 1) ? Color.LIGHT_GRAY : Color.GRAY);
        g2.draw(new Line2D.Double(0, y * zoom, endX, y * zoom));
    }

    public Point getPoint(MouseEvent me) {
        return getPoint(me.getX(), me.getY());
    }

    public Point getPoint(int x, int y) {
        // Convert raw coordinates into coordinates on the grid
        return new Point((int) Math.round((x / zoom - getWidth() / 2d - locX) / SPACING),
                (int) Math.round((y / zoom - getHeight() / 2d - locY) / SPACING));
    }

    public Point2D.Double toCoord(int x, int y) {
        // Convert coordinates on the grid to raw coordinates
        return new Point2D.Double((locX + x * SPACING + getWidth() / 2d - SPACING / 2d) * zoom,
                (locY + y * SPACING + getHeight() / 2d - SPACING / 2d) * zoom);
    }
}
