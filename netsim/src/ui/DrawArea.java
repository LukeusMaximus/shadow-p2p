package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import components.ComponentManager;

public class DrawArea extends JPanel {

    private ComponentManager componentManager = null;
    
    public DrawArea(int width, int height) {
        this.setBounds(0, 0, width, height);
        this.setBackground(new Color(64, 0, 64));
        
        this.addHierarchyBoundsListener(new HierarchyBoundsListener(){

            @Override
            public void ancestorMoved(HierarchyEvent arg0) {
                System.out.println("Moved");
            }

            @Override
            public void ancestorResized(HierarchyEvent arg0) {
                System.out.println("Resized: " + arg0.getChanged().getBounds());
            }
            
        });
    }

    public void drawCalibrationSquares(Graphics g) {
        int w = this.getBounds().width;
        int h = this.getBounds().height;
        int squareSize = 10;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, squareSize, squareSize);
        g.fillRect(0, h - squareSize - 1, squareSize, squareSize);
        g.fillRect(w - squareSize - 1, 0, squareSize, squareSize);
        g.fillRect(w - squareSize - 1, h - squareSize - 1, squareSize, squareSize);
        g.setColor(Color.RED);
        g.drawRect(0, 0, squareSize, squareSize);
        g.drawRect(0, h - squareSize - 1, squareSize, squareSize);
        g.drawRect(w - squareSize - 1, 0, squareSize, squareSize);
        g.drawRect(w - squareSize - 1, h - squareSize - 1, squareSize, squareSize);
    }
    
    public void render() {
        repaint(this.getVisibleRect());
    }

    public void setComponentManager(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawCalibrationSquares(g);
        if(this.componentManager != null) {
            this.componentManager.renderComponents(g, this.getBounds());
        }
    }
    
}
