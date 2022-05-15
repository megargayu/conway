package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controls extends JPanel {
    private static final Runnable errorSound =
            (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.exclamation");
    private ToolBtn currentTool;

    private abstract class RegularButton extends FancyButton {
        public RegularButton(ImageIcon icon) {
            super(icon);
        }

        abstract public void toggle();
    }
    private abstract class ToolBtn extends FancyButton {
        private boolean activated;
        private final Color defaultColorOrig = defaultColor;
        public ToolBtn(ImageIcon icon) {
            super(icon);
            on();

            for (ToolBtn btn : toolBtns) {
                if (btn == this) continue;
                if (btn.activated) {
                    off();
                    currentTool = btn;
                }
            }

            addActionListener(e -> {
                if (!activated) {
                    on();
                    for (ToolBtn btn : toolBtns) {
                        if (btn == this) continue;
                        btn.off();
                    }
                }

                activated = !activated;
            });
        }

        public final void off() {
            activated = false;
            defaultColor = defaultColorOrig;
            setBackground(defaultColorOrig);
            onToolChange();
        }

        public final void on() {
            activated = true;
            currentTool = this;
            defaultColor = Color.LIGHT_GRAY;
            setBackground(Color.LIGHT_GRAY);
        }

        public abstract void mousePressed(MouseEvent me);
        public abstract void mouseDragged(MouseEvent me);
        public abstract void mouseReleased(MouseEvent me);
        public void onToolChange() {}
    }

    private class PlayBtn extends RegularButton {
        public ScheduledExecutorService player;
        private boolean playing = false;
        public PlayBtn() {
            super(new ImageIcon(PlayBtn.class.getResource("Media/Play.png")));
            addActionListener(e -> toggle());
        }

        @Override
        public void toggle() {
            if (Main.hashLifeAlgo.isEmpty()) {
                errorSound.run();
                return;
            }
            toggleIcon();
            if (!playing) play();
            playing = !playing;
        }

        public void play() {
            player = Executors.newScheduledThreadPool(1);
            player.scheduleAtFixedRate(() -> {
                Main.hashLifeAlgo.evolve();
                Main.frame.repaint();
                if (!playing) player.shutdown();
                if (Main.hashLifeAlgo.isEmpty()) {
                    toggleIcon();
                    playing = !playing;
                    player.shutdown();
                }
            }, 0, 20, TimeUnit.MILLISECONDS);
        }

        private void toggleIcon() {
            changeIcon(new ImageIcon(PlayBtn.class.getResource((!playing) ? "Media/Stop.png" : "Media/Play.png")));
        }
    }
    private class DrawBtn extends ToolBtn {
        private boolean wasPlaying;
        private int statusToDraw;
        private Point lastPoint;

        public DrawBtn() {
            super(new ImageIcon(DrawBtn.class.getResource("Media/Pencil.png")));
        }

        @Override
        public void mousePressed(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me)) {
                Point point = Main.grid.getPoint(me);
                if (wasPlaying = playBtn.playing) togglePausing();
                if (point.x >= -(Main.grid.endX / 2d) && point.y >= -(Main.grid.endY / 2)) {
                    statusToDraw = Main.hashLifeAlgo.getCellAt(point.x, point.y) == 0 ? 1 : 0;
                    Main.hashLifeAlgo.setCellAt(point.x, point.y, statusToDraw);
                    Main.grid.repaint();
                }
                lastPoint = point;
            }
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me)) {
                Point point = Main.grid.getPoint(me);
                if (point.x >= -(Main.grid.endX / 2d) && point.y >= -(Main.grid.endY / 2d)) {
                    // Thanks to https://stackoverflow.com/a/33557436 for linear interpolation
                    int dx = point.x - lastPoint.x, dy = point.y - lastPoint.y;
                    double a;
                    if (Math.abs(dx) >= Math.abs(dy)) {
                        a = dy / ((double) dx);
                        for (int i = 0; Math.abs(i) < Math.abs(dx); i += Math.signum(dx))
                            Main.hashLifeAlgo.setCellAt(lastPoint.x + i, (int) (lastPoint.y + i * a), statusToDraw);
                    } else {
                        a = dx / ((double) dy);
                        for (int i = 0; Math.abs(i) < Math.abs(dy); i += Math.signum(dy))
                            Main.hashLifeAlgo.setCellAt((int) (lastPoint.x + i * a), lastPoint.y + i, statusToDraw);
                    }
                    Main.hashLifeAlgo.setCellAt(point.x, point.y, statusToDraw);
                    lastPoint = point;
                    Main.grid.repaint();
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me)) {
                Point point = Main.grid.getPoint(me);
                if (wasPlaying) togglePausing();
                if (point.x >= -(Main.grid.endX / 2) && point.y >= -(Main.grid.endY / 2)) {
                    Main.hashLifeAlgo.setCellAt(point.x, point.y, statusToDraw);
                    Main.grid.repaint();
                }
                lastPoint = point;
            }
        }
    }
    private class MoveBtn extends ToolBtn {
        private int mouseX, mouseY;
        private Point2D.Double lastLoc;

        public MoveBtn() {
            super(new ImageIcon(MoveBtn.class.getResource("Media/Drag.png")));
            lastLoc = new Point2D.Double(Main.grid.locX, Main.grid.locY);
        }

        @Override
        public void mousePressed(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me)) {
                mouseX = me.getX();
                mouseY = me.getY();
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me)) {
                lastLoc = new Point2D.Double(Main.grid.locX, Main.grid.locY);
            }
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            if (SwingUtilities.isLeftMouseButton(me)) {
                me.translatePoint(me.getComponent().getLocation().x - mouseX,
                        me.getComponent().getLocation().y - mouseY);
                Main.grid.locX = me.getX() / Main.grid.zoom + lastLoc.x;
                Main.grid.locY = me.getY() / Main.grid.zoom + lastLoc.y;
                Main.grid.repaint();
            }
        }
    }
    private class SelectBtn extends ToolBtn {
        public SelectBtn() {
            super(new ImageIcon(MoveBtn.class.getResource("Media/Select.png")));
        }

        @Override
        public void mousePressed(MouseEvent me) {
            Main.grid.startSelect = Main.grid.getPoint(me);
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            Main.grid.endSelect = Main.grid.getPoint(me);
            Main.grid.repaint();
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            Main.grid.endSelect = Main.grid.getPoint(me);
            Main.grid.repaint();
        }
    }

    private final PlayBtn playBtn = new PlayBtn();
    private final List<ToolBtn> toolBtns = new ArrayList<>();

    public Controls() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        setLayout(new GridLayout(1, 3, 5, 1));

        add(playBtn);
        toolBtns.add(new DrawBtn());
        toolBtns.add(new MoveBtn());
        toolBtns.add(new SelectBtn());
        for (ToolBtn toolBtn : toolBtns) add(toolBtn);
    }

    public void mousePressed(MouseEvent me) {
        currentTool.mousePressed(me);
    }

    public void mouseDragged(MouseEvent me) {
        currentTool.mouseDragged(me);
    }

    public void mouseReleased(MouseEvent me) {
        currentTool.mouseReleased(me);
    }

    public void togglePausing() {
        // Toggle without changing icon
        if (Main.hashLifeAlgo.isEmpty()) {
            errorSound.run();
            return;
        }

        if (!playBtn.playing) playBtn.play();
        playBtn.playing = !playBtn.playing;
    }

    public void togglePlaying() {
        playBtn.toggle();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.scale(1, 1);
        g2.setColor(Color.decode("#696969"));
        g2.fill(new RoundRectangle2D.Double(-10, 6, getWidth() + 10, getHeight() - 12, 5, 5));
    }
}
