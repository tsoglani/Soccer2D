package gr.istl.soccer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;


/**
 *
 * @author gaitanesnikos
 */
public class SoccerLayoutManager implements LayoutManager {

    private Ball myBall;
    
    public SoccerLayoutManager() {
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        comp.setVisible(false);
        comp = null;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return new Dimension(600, 400);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(500, 300);
    }
    
    /**
     * xrisimopoieite otan allazei megethos to conteiner kai stin ekinisi tis
     * earmogis
     *
     * @param parent
     */
    @Override
    public void layoutContainer(Container parent) {
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component component = parent.getComponent(i);
            if (component instanceof Ball) {
                Ball ball = (Ball) component;
               
                Point point = ball.refresh();
                ball.setBounds(point.x, point.y, Ball.BALL_WIDTH, Ball.BALL_HEIGHT);
            } else if (component instanceof Player) {
                Player player = (Player) component;
                Point point = player.refresh();
                player.setBounds(point.x, point.y, Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
            } else if (component instanceof OverallMiniView) {
                OverallMiniView radar = (OverallMiniView) component;
                if (myBall == null) {
                    return;
                }
                radar.setBounds(0, 0, OverallMiniView.WIDTH, OverallMiniView.HEIGHT);
            }
        }
    }

    protected void setBall(Ball ball) {
        this.myBall = ball;
    }
}
