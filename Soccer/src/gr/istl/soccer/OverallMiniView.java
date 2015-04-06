package gr.istl.soccer;

import static gr.istl.soccer.OverallMiniView.SCALE_PERCENT;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.Timer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author gaitanesnikos
 */
public class OverallMiniView extends JPanel implements ActionListener {

    public static double SCALE_PERCENT = 0.25;

    Timer timer = new Timer(1000, this);
    SoccerField field;
    BufferedImage image;

    public OverallMiniView(SoccerField field) {
        this.field = field;
        setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED, Color.red, new java.awt.Color(255, 255, 255)), javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(204, 204, 255), new java.awt.Color(204, 204, 255))));
        timer.start();
    }

    /**
     *
     * @param g
     */
    @Override
    public void paintComponent(Graphics g) {
        if (image != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            image = new BufferedImage(field.getWidth(), field.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D imgG2d = image.createGraphics();
            field.paint(imgG2d);
            imgG2d.dispose();
            repaint();
        }
    }

    public void resize() {
        setBounds(0, 0, (int) (field.getWidth() * SCALE_PERCENT), (int) (field.getHeight() * SCALE_PERCENT));
    }

}

