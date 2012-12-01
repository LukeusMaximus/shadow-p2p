package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class DrawArea extends JPanel {

    private BufferedImage buffer;
    
    public DrawArea(int width, int height) {
        this.setBounds(0, 0, width, height);
        this.setBackground(new Color(64, 0, 64));
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.addHierarchyBoundsListener(new HierarchyBoundsListener(){

            @Override
            public void ancestorMoved(HierarchyEvent arg0) {
                System.out.println("Moved");
                
            }

            @Override
            public void ancestorResized(HierarchyEvent arg0) {
                System.out.println("Resized");
                Rectangle bounds = arg0.getChanged().getBounds();
                buffer = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);
            }
            
        });
    }
    
    public void drawCalibrationSquares() {
        System.out.println("bounds = " + this.getBounds().toString());
        int w = this.getBounds().width;
        int h = this.getBounds().height;
        int squareSize = 10;
        Graphics g = buffer.getGraphics();
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawCalibrationSquares();
        g.drawImage(buffer, 0, 0, null);
    }
    
    
    
}
