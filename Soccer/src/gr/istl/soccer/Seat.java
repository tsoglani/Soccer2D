/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.istl.soccer;

import java.awt.*;
import javax.swing.JPanel;

/**
 * oi xristes pou parakolouthoun ton angona me ta stoixeia tous
 *
 * @author gaitanesnikos
 */
public class Seat extends JPanel {

    private boolean sitOnIt;
    private Image personImage;// an o xristis thelei na valei fotografia 
    private String userName;
    private int age;
    private String message;

    public Seat() {

//        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED, new java.awt.Color(153, 153, 153), new java.awt.Color(255, 255, 255)), javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(204, 204, 255), new java.awt.Color(204, 204, 255))));

    }

    /**
     *
     * @return
     */
    public int getAge() {
        return age;
    }

    /**
     *
     * @param age
     */
    public void setAge(int age) {
        this.age = age;
    }

    public Image getPersonImage() {
        return personImage;
    }

    public void setPersonImage(Image personImage) {
        this.personImage = personImage;
    }

    /**
     *
     * @return
     */
    public boolean isSitOnIt() {
        return sitOnIt;
    }

    public void setsItOnIt(boolean sitOnIt, String userName) {
        this.sitOnIt = sitOnIt;
        this.userName = userName;
    }

    /**
     *
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public void paintComponent(Graphics g) {
        if (!sitOnIt) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g;
        switch (((Bleachers) getParent()).getSide()) {
            case Bleachers.TEAM_1:
                g2d.setColor(Color.YELLOW);
                break;
            case Bleachers.TEAM_2:
                g2d.setColor(Color.red);
                break;
        }

        int radius = Math.min(getWidth(), getHeight());
        g2d.fillOval(getWidth() / 2, getHeight() / 3, radius / 2, radius / 2);
//        Graphics2D g2d = (Graphics2D) g;
//        if (!sitOnIt) {
//            return;
//        }
//        if (personImage != null) {
//            g2d.drawImage(personImage, null, this);
//        } else {
//            g2d.setXORMode(Color.lightGray);
//            g2d.setColor(Color.GRAY);
//            g2d.fillOval(getWidth() / 7, getHeight() / 7, getWidth() - getWidth() / 5, getHeight() - getHeight() / 5);
//            g2d.setColor(Color.WHITE);
//            g2d.fillOval(getWidth() / 5, getHeight() / 3, getWidth() / 5, getHeight() / 6);
//            g2d.fillOval(getWidth() / 2, getHeight() / 3, getWidth() / 5, getHeight() / 6);
//
//            g2d.drawArc(getWidth() / 4, getHeight() - getHeight() / 3, getWidth() / 3, getHeight() / 10, 180, 140);
//            g2d.setFont(new Font(Font.SERIF, Font.ITALIC, 20));
//            g2d.setColor(Color.red);
//            if (userName != null) {
//                g2d.drawString(userName, getWidth()/2,getHeight()- getHeight()/10);
//            }
//
//        }

    }
}
