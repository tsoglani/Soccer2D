package gr.istl.soccer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import static java.awt.image.ImageObserver.WIDTH;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 *
 * @author george
 */
public class SoccerFieldContainer extends JPanel {

    JLayeredPane layeredPane;
    OverallMiniView radar;
    SoccerField field;

    public SoccerFieldContainer() {
        setLayout(new BorderLayout());

        Bleachers team1Bleachers = new Bleachers(Bleachers.TEAM_1);
        Bleachers team2Bleachers = new Bleachers(Bleachers.TEAM_2);
        add(team1Bleachers, BorderLayout.NORTH);
        add(team2Bleachers, BorderLayout.SOUTH);

        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        field = new SoccerField();
        field.setFrame((SoccerFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this));
        radar = new OverallMiniView(field);

        layeredPane.add(field, new Integer(0), 0);
        layeredPane.add(radar, new Integer(1), 0);

        layeredPane.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                field.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
                radar.resize();
            }

        });
        field.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ALT) {
                    radar.setVisible(!radar.isVisible());
                }
            }

        });

        //first team
        field.addPlayer(new Player(1, 1, "01", 20, 20));
        field.addPlayer(new Player(1, 2, "02", 20, 20));
        field.addPlayer(new Player(1, 3, "03", 20, 20));
        field.addPlayer(new Player(1, 4, "04", 20, 20));
        field.addPlayer(new Player(1, 5, "05", 20, 20));
        field.addPlayer(new Player(1, 6, "06", 20, 20));
        field.addPlayer(new Player(1, 7, "07", 20, 20));
        field.addPlayer(new Player(1, 8, "08", 20, 20));
        field.addPlayer(new Player(1, 9, "09", 20, 20));
        field.addPlayer(new Player(1, 10, "10", 20, 20));
        field.addPlayer(new Player(1, 11, "11", 20, 20));
//
//        //2nd team
        field.addPlayer(new Player(2, 1, "01", 20, 20));
        field.addPlayer(new Player(2, 2, "02", 20, 20));
        field.addPlayer(new Player(2, 3, "03", 20, 20));
        field.addPlayer(new Player(2, 4, "04", 20, 20));
        field.addPlayer(new Player(2, 5, "05", 20, 20));
        field.addPlayer(new Player(2, 6, "06", 20, 20));
        field.addPlayer(new Player(2, 7, "07", 20, 20));
        field.addPlayer(new Player(2, 8, "08", 20, 20));
        field.addPlayer(new Player(2, 9, "09", 20, 20));
        field.addPlayer(new Player(2, 10, "10", 20, 20));
        field.addPlayer(new Player(2, 11, "11", 20, 20));

        field.addBall(new Ball());
        field.getBall().moveToRelativeLocationInMeters(SoccerField.SOCCER_WIDTH_IN_M / 2, SoccerField.SOCCER_HEIGHT_IN_M / 2);
new MoveBall(field);

//        new Timer(20, new ActionListener() {
//            double[] pos2 = new double[]{13.0, 2.0};
//            double[] pos3 = new double[]{16.0, 2.0};
//            double[] pos4 = new double[]{23.0, 2.0};
//            double[] pos5 = new double[]{28.0, 2.0};
//            double[] pos6 = new double[]{33.0, 2.0};
//            double[] pos7 = new double[]{46.0, 2.0};
//            double[] pos8 = new double[]{59.0, 2.0};
//            double[] pos9 = new double[]{61.0, 2.0};
//            double[] pos10 = new double[]{74.0, 2.0};
//            double[] pos11 = new double[]{88.0, 2.0};
//            double[] pos12 = new double[]{90.0, 2.0};
//            double[] pos13 = new double[]{100.0, 2.0};
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                k++;
////                field.getPlayer("a#02").moveToRelativeLocationInMeters(pos2[0], pos2[1]+k);
////                field.getPlayer("a#03").moveToRelativeLocationInMeters(pos3[0], pos3[1]+k);
////                field.getPlayer("a#04").moveToRelativeLocationInMeters(pos4[0], pos4[1]+k);
////                field.getPlayer("a#05").moveToRelativeLocationInMeters(pos5[0], pos5[1]+k);
////                field.getPlayer("a#06").moveToRelativeLocationInMeters(pos6[0], pos6[1]+k);
////                field.getPlayer("a#07").moveToRelativeLocationInMeters(pos7[0], pos7[1]+k);
////
////                field.getPlayer("b#02").moveToRelativeLocationInMeters(pos8[0], pos8[1]+k);
////                field.getPlayer("b#03").moveToRelativeLocationInMeters(pos9[0], pos9[1]+k);
////                field.getPlayer("b#04").moveToRelativeLocationInMeters(pos10[0], pos10[1]+k);
////                field.getPlayer("b#05").moveToRelativeLocationInMeters(pos11[0], pos11[1]+k);
////                field.getPlayer("b#06").moveToRelativeLocationInMeters(pos12[0], pos12[1]+k);
////                field.getPlayer("b#07").moveToRelativeLocationInMeters(pos13[0], pos13[1]+k);
//            }
//        }).start();

    }
//    int k;
}
