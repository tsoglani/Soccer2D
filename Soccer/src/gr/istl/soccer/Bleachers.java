package gr.istl.soccer;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author gaitanesnikos
 */
public class Bleachers extends JPanel { // kerkides

    private ArrayList<Seat> seats;
    private int side;
    protected static final int TEAM_1 = 1;
    protected static final int TEAM_2 = 2;

    public Bleachers(int side) {
        init();
        this.side = side;
        setLayout(new GridLayout(3, 20));
        setBackground(Color.LIGHT_GRAY);
        for (int i = 0; i < 1; i++) {
            addPlayer("");
        }
    }
/**
 *  poianis omadas einai oi funs
 * @return
 */
    protected int getSide() {
        return side;
    }

    /**
     * initialize
     */
    public void init() {

        seats = new ArrayList<Seat>();
        for (int i = 0; i < 60; i++) {// vazw tis outos i allos theseis 
            addSeats(new Seat());
        }
    }

    /**
     * otan mpenei neos xristis ton vazei stin kaini thesi
     */
    public void addPlayer(String name) {
        for (Seat seat : seats) {
            if (!seat.isSitOnIt()) {
                seat.setsItOnIt(true, name);
                break;
            }
        }
    }

    /**
     * bazei tis theseis
     *
     * @param u
     */
    public void addSeats(Seat u) {
        seats.add(u);
        add(u);
    }

    /**
     * an mpei enas xristis stis eidi iparxouses theseis ton petaei mesa
     *
     * @param u
     */
    public void newUserIsOut(Seat u) {
        seats.remove(u);
        remove(u);
    }
/**
 * 
 * @param g
 */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

    }
}
