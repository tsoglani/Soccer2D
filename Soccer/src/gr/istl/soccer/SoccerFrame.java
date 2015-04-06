package gr.istl.soccer;

import javax.swing.JFrame;

/**
 *
 * @author gaitanesnikos
 */
public class SoccerFrame extends JFrame {
    public static final int HEIGHT = 400;
    public static final int WIDTH = 600;
    
    public SoccerFrame() {
        add(new SoccerFieldContainer());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocation(400, 300);
        setVisible(true);
    }

    public static void main(String[] args) {        
        new SoccerFrame();
        new PrintCommandsDialog();
       
    }
}
