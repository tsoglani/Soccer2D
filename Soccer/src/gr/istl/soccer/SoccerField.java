package gr.istl.soccer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author gaitanesnikos
 */
public class SoccerField extends JPanel {

    protected static final Color DARK_GREEN = new Color(30, 153, 67);// new Color(34, 139, 34);
    protected static final Color LIGHT_GREEN = new Color(78, 175, 100);
    private String name;
    private Team team1;
    private Team team2;
    private Ball ball;
    private SoccerFrame frame;
    private Referee referee;
    private boolean isPlayingOnline;
    public static boolean isKickOff = true;
    private weather wether;
    private boolean makeRainingMotion; // gia na ftiaksw to animation tou xioniou
    private ArrayList<Point> rainPoints = new ArrayList<Point>();
    private Timer raining;
    private Timer clock;
    private boolean isSingleGame;
    private Bleachers team1Bleachers;
    private Bleachers team2Bleachers;
    public static final int SOCCER_WIDTH_IN_M = 120;
    public static final int SOCCER_HEIGHT_IN_M = 90;
    public static final float MHKOS_TERMATOS_IN_METERS_Y_AXIS = 7.32f;//
    private final float MEGALH_PERIOXH_IN_METERS_Y_AXIS = 40.32f;//ayto kata mhkos ths grammhs pisw apo to terma
    private final float MEGALH_PERIOXH_IN_METERS_X_AXIS = 16.5f;//
    private final float MIKRH_PERIOXH_IN_METERS_Y_AXIS = 18.32f;//ayto kata mhkos ths grammhs pisw apo to terma
    private final float MIKRH_PERIOXH_IN_METERS_X_AXIS = 5.5f;//
    private final float AKTINA_HMIKYKLIAS_PERIOXHS = 3.0f;

    public SoccerField() {
        referee = new Referee(this);
        setBackground(DARK_GREEN);
        team1 = new Team(1, "a", true, true);
        team2 = new Team(2, "b", false, false);
        setFocusable(true);
        addKeyListener(new KeyboardHandler());
        initializeTimers();
        setLayout(new SoccerLayoutManager());

        new Thread(referee).start();

    }

    public void addPlayer(Player player) {
        switch (player.getTeamId()) {
            case Team.TEAM_1:
                player.setTeam(team1);
                team1.addPLayer(player);
                break;
            case Team.TEAM_2:
                player.setTeam(team2);
                team2.addPLayer(player);
                break;
        }
        add(player);
        player.start();
    }

    public void addBall(Ball ball) {
        this.ball = ball;
        ball.setField(this);
        add(ball);
        ((SoccerLayoutManager) getLayout()).setBall(ball);
        ball.start();
    }

    /**
     * initialize Timer snow, timer clock and start these Timer instances
     */
    private void initializeTimers() {
        raining = new Timer(500, new ActionListener() {// otan teleiwsei o xronos allazw me random points ta "xionia"
            @Override
            public void actionPerformed(ActionEvent e) {
                makeRainingMotion = !makeRainingMotion;
            }
        });
        clock = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        clock.start();
        if (wether == weather.SNOWING) {
            raining.start();
        }

    }

    /**
     * pernw tis diastaseis tou gipedou
     *
     * @return
     */
    public Rectangle getRectangle() {
        return new Rectangle(5, 5, getWidth() - 10, getHeight() - 10);
    }

    /**
     * pernw tis diastaseis twn termatwn analoga me tin omada pou pernaw ws
     * orisma
     *
     * @param team
     * @param width
     * @param height
     * @return
     */
    public Rectangle getRectangleNet(Team team) {
        if (team.isPlayingHome()) {
            return new Rectangle(0, (int) convertYMetersInPixels(SOCCER_HEIGHT_IN_M / 2 - (MHKOS_TERMATOS_IN_METERS_Y_AXIS / 2)), 5, (int) convertYMetersInPixels(MHKOS_TERMATOS_IN_METERS_Y_AXIS));
        } else {
            return new Rectangle((int) convertXMetersInPixels(SOCCER_WIDTH_IN_M) - 5, (int) convertYMetersInPixels(SOCCER_HEIGHT_IN_M / 2 - (MHKOS_TERMATOS_IN_METERS_Y_AXIS / 2)), 5, (int) convertYMetersInPixels(MHKOS_TERMATOS_IN_METERS_Y_AXIS));
        }
    }

    /**
     * zwgrafizw to gipedo
     *
     * @param g
     */
    public void drawTheField(Graphics2D g) {
        Color[] bgColors = new Color[]{DARK_GREEN, LIGHT_GREEN};

        //zwgrafise th dixromia apo ta aristera mexri to kentro 
        int color_step = getWidth() / 9;
        for (int i = 0, j = 0; i < getWidth() / 2; i += color_step, j++) {
            if (j % 2 == 0) {
                g.setColor(bgColors[0]);
            } else {
                g.setColor(bgColors[1]);
            }
            g.fillRect(i, 0, color_step, getHeight());
        }
        //zwgrafise apo terma deksia mexri to kentro th dixromia
        for (int i = getWidth(), j = 0; i > getWidth() / 2; i -= color_step, j++) {
            if (j % 2 != 0) {
                g.setColor(bgColors[0]);
            } else {
                g.setColor(bgColors[1]);
            }
            g.fillRect(i + 5, 0, color_step, getHeight());
        }
        g.setColor(Color.white);

        //zografizv tis grammes toy out k korner
        g.drawRect(4, 4, getWidth() - 8, getHeight() - 8);

        //zografizv thn mesaia grammh
        g.drawLine((int) convertXMetersInPixels(SOCCER_WIDTH_IN_M / 2), 0, (int) convertXMetersInPixels(SOCCER_WIDTH_IN_M / 2), (int) convertYMetersInPixels(SOCCER_HEIGHT_IN_M));

        // zografizw to imikikleio
        int radius = Math.min(getWidth() / 5, getHeight() / 5);
        g.drawOval(getWidth() / 2 - radius / 2, (getHeight() - getHeight() / 5) / 2, radius, radius);

        // zografizw tis grames tis megalis perioxis
        g.drawRect(5, (int) convertYMetersInPixels(SOCCER_HEIGHT_IN_M / 2 - (MEGALH_PERIOXH_IN_METERS_Y_AXIS / 2)), (int) convertXMetersInPixels(MEGALH_PERIOXH_IN_METERS_X_AXIS), (int) convertYMetersInPixels(MEGALH_PERIOXH_IN_METERS_Y_AXIS));
        g.drawRect(getWidth() - 5 - (int) convertXMetersInPixels(MEGALH_PERIOXH_IN_METERS_X_AXIS),
                (int) convertYMetersInPixels(SOCCER_HEIGHT_IN_M / 2 - (MEGALH_PERIOXH_IN_METERS_Y_AXIS / 2)),
                (int) convertXMetersInPixels(MEGALH_PERIOXH_IN_METERS_X_AXIS), (int) convertYMetersInPixels(MEGALH_PERIOXH_IN_METERS_Y_AXIS));

        // zografizw tis grames tis mikris perioxis
        g.drawRect(5, (int) convertYMetersInPixels(SOCCER_HEIGHT_IN_M / 2 - (MIKRH_PERIOXH_IN_METERS_Y_AXIS / 2)), (int) convertXMetersInPixels(MIKRH_PERIOXH_IN_METERS_X_AXIS), (int) convertYMetersInPixels(MIKRH_PERIOXH_IN_METERS_Y_AXIS));
        g.drawRect(getWidth() - 5 - (int) convertXMetersInPixels(MIKRH_PERIOXH_IN_METERS_X_AXIS),
                (int) convertYMetersInPixels(SOCCER_HEIGHT_IN_M / 2 - (MIKRH_PERIOXH_IN_METERS_Y_AXIS / 2)),
                (int) convertXMetersInPixels(MIKRH_PERIOXH_IN_METERS_X_AXIS),
                (int) convertYMetersInPixels(MIKRH_PERIOXH_IN_METERS_Y_AXIS));

        // draw the nets
        g.fill(getRectangleNet(team1));

        g.fill(getRectangleNet(team2));


        //    zografizw to imikikleio tis megalis perioxis
        g.drawArc((int) convertXMetersInPixels(MEGALH_PERIOXH_IN_METERS_X_AXIS - AKTINA_HMIKYKLIAS_PERIOXHS) + 5,
                (int) convertYMetersInPixels(SOCCER_HEIGHT_IN_M / 2 - AKTINA_HMIKYKLIAS_PERIOXHS),
                (int) convertXMetersInPixels(2 * AKTINA_HMIKYKLIAS_PERIOXHS),
                (int) convertXMetersInPixels(2 * AKTINA_HMIKYKLIAS_PERIOXHS),
                90, -180);
//        g.drawArc(getWidth() - getWidth() / 4, getHeight() / 3 + getHeight() / 14, getWidth() / 10, getHeight() / 5, 90, 180);
        g.drawArc(getWidth() - (int) convertXMetersInPixels(MEGALH_PERIOXH_IN_METERS_X_AXIS + AKTINA_HMIKYKLIAS_PERIOXHS) - 6,
                (int) convertYMetersInPixels(SOCCER_HEIGHT_IN_M / 2 - AKTINA_HMIKYKLIAS_PERIOXHS),
                (int) convertXMetersInPixels(2 * AKTINA_HMIKYKLIAS_PERIOXHS),
                (int) convertXMetersInPixels(2 * AKTINA_HMIKYKLIAS_PERIOXHS),
                90, 180);

        // draw the nets
        Rectangle terma1NetRect = getRectangleNet(team1);
        int y1 = (int) terma1NetRect.getY();
        int width1 = (int) terma1NetRect.getWidth() + 12;
        int height1 = (int) terma1NetRect.getHeight();

        g.setColor(new Color(170, 170, 170));
        for (int i = y1; i < y1 + height1; i += 5) {
            g.drawLine(5, i, width1, i);
        }
        g.setColor(new Color(190, 190, 190));
        for (int i = 0; i < width1; i += 4) {
            g.drawLine(i, y1, i, y1 + height1);
        }
        g.setColor(Color.BLACK);
        g.drawRect(0, y1, width1, height1);
        //gia na fainonta pio boldarismena ta dokaria kai to front toy termatos metatopizw kata ligo to drawRect
        g.drawRect(0, y1 + 1, width1, height1);
        g.drawRect(0, y1, width1 + 1, height1);


        //draw team's 2 to terma
        Rectangle terma2NetRect = getRectangleNet(team2);
        int y2 = (int) terma2NetRect.getY();
        int width2 = (int) terma2NetRect.getWidth() + 12;
        int height2 = (int) terma2NetRect.getHeight();
        g.setColor(new Color(170, 170, 170));
        for (int i = y2; i < y2 + height2; i += 5) {
            g.drawLine(getWidth() - width2, i, getWidth() + width2, i);
        }
        g.setColor(new Color(190, 190, 190));
        for (int i = getWidth() - width2; i < getWidth(); i += 5) {
            g.drawLine(i, y2, i, y2 + height2);
        }

        g.setColor(Color.BLACK);
        g.drawRect(getWidth() - width2, y2, width2, height2);
        //gia na fainonta pio boldarismena ta dokaria kai to front toy termatos metatopizw kata ligo to drawRect
        g.drawRect(getWidth() - width2 - 1, y2 + 1, width2, height2);
    }

    public double convertXMetersInPixels(double xMeters) {
        return (((double) getWidth() / SOCCER_WIDTH_IN_M)) * xMeters;
    }

    public double convertYMetersInPixels(double yMeters) {
        return (((double) getHeight() / SOCCER_HEIGHT_IN_M)) * yMeters;
    }

    /**
     *
     * @param g2d
     */
    public void drawWeather(Graphics2D g2d) {
        g2d.setColor(Color.white);
        if (wether == weather.SNOWING) {
            if (makeRainingMotion) {
                for (Point p : rainPoints) {
                    g2d.drawOval(p.x, p.y, 1, 1);
                }
            } else {
                rainPoints.removeAll(rainPoints);
                for (int i = 0; i < 1000; i++) {
                    int r = (int) (Math.random() * getWidth());
                    int c = (int) (Math.random() * getHeight());
                    rainPoints.add(new Point(r, c));
                    g2d.drawOval(r, c, 1, 1);
                }
                makeRainingMotion = !makeRainingMotion;
            }
        }
    }

    /**
     * to keyListener twn paixtwn other class
     */
    private final class KeyboardHandler implements KeyListener {

        private Player otherTeamSelectedPlayer = null;
        private Player selectedPlayer = null;
        private Team selectedPlayerTeam = null;
        private Team otherPlayerTeam = null;

        /**
         *
         */
        @Override
        public void keyTyped(KeyEvent e) {
        }

        /**
         *
         * @param e
         */
        @Override
        public void keyPressed(KeyEvent e) {
            if (!isIsPlayingOnline() && !isIsSingleGame()) { // ama den einai online 
                // kai an den paizw mono paixnidi  i otherPlayerTeam einai i omada tou antipalou(ama i omada tou paixti einai i team1 tote i otherPlayerTeam eiai i team2...)
                if (getTeam1().isIsMyTeam()) {
                    selectedPlayerTeam = team1;
                    otherPlayerTeam = team2;
                } else {
                    selectedPlayerTeam = team2;
                    otherPlayerTeam = team1;
                }

                for (Player p : selectedPlayerTeam.getPlayingPlayers()) {
                    if (p.isIsSelected()) {
                        selectedPlayer = p;
                    }
                }

                for (Player p : otherPlayerTeam.getPlayingPlayers()) {
                    if (p.isIsSelected()) {
                        otherTeamSelectedPlayer = p;// pairnw ton paixti pou einai selected stin otherPlayerTeam kai ton vazw ston otherTeamSelectedPlayer
                    }
                }

                if (otherPlayerTeam != null) {
                    if (e.getKeyCode() == KeyEvent.VK_M) {
                        otherTeamSelectedPlayer.setIsDownPressed(true);
                    }
                    if (e.getKeyCode() == KeyEvent.VK_I) {
                        otherTeamSelectedPlayer.setIsUpPressed(true);
                    }
                    if (e.getKeyCode() == KeyEvent.VK_L) {
                        otherTeamSelectedPlayer.setIsRightPressed(true);
                    }
                    if (e.getKeyCode() == KeyEvent.VK_J) {
                        otherTeamSelectedPlayer.setIsLeftPressed(true);
                    }

                    if (e.getKeyCode() == KeyEvent.VK_O) {
                        if (!otherTeamSelectedPlayer.isHavingTheBall()) {
                            otherTeamSelectedPlayer.setWantToChangeSelectedPlayer(true);
                            otherTeamSelectedPlayer.setIsLeftPressed(false);
                            otherTeamSelectedPlayer.setIsRightPressed(false);
                            otherTeamSelectedPlayer.setIsUpPressed(false);
                            otherTeamSelectedPlayer.setIsDownPressed(false);
                        }
                    }
                    if (e.getKeyCode() == KeyEvent.VK_U) {
                        otherTeamSelectedPlayer.setCurrentSpeed(otherTeamSelectedPlayer.getMaxSpeed());
                    }
                    if (e.getKeyCode() == KeyEvent.VK_P) {
                        if (otherTeamSelectedPlayer.isHavingTheBall()) {
                            otherTeamSelectedPlayer.setTakingAShoot(true);
                            if (otherTeamSelectedPlayer.getTime() == null) {
                                otherTeamSelectedPlayer.setTime(otherTeamSelectedPlayer.new powerShootThread());
                                ((Thread) otherTeamSelectedPlayer.getTime()).start();
                            }
                        }
                    }
                    if (e.getKeyCode() == KeyEvent.VK_K) {
                        if (getBall() != null && (otherTeamSelectedPlayer.getField().getBall().isControlledByPlayer() && otherTeamSelectedPlayer.getField().getBall().getWhoPLayerHasTheBall() == otherTeamSelectedPlayer)) {
                            otherTeamSelectedPlayer.getField().getBall().setGivePass(true);
                        }
                    }
                }
            }

            // gia n paiksw dilpo sto idio pc mporw edw n rythmisw pliktra apo ta opoia tha elengxonte oi paixtes p den einai dikoi mou

            if (otherPlayerTeam == null || otherPlayerTeam != selectedPlayer.getTeam()) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    selectedPlayer.setIsDownPressed(true);

                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    selectedPlayer.setIsUpPressed(true);
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    selectedPlayer.setIsRightPressed(true);
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    selectedPlayer.setIsLeftPressed(true);
                }
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    if (selectedPlayer.isHavingTheBall()) {
                        selectedPlayer.setTakingAShoot(true);
                        if (selectedPlayer.getTime() == null) {
                            selectedPlayer.setTime(selectedPlayer.new powerShootThread());
                            selectedPlayer.getTime().start();
                        }
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_W) {
                    if (selectedPlayer.getCurrentSpeed() != selectedPlayer.getMaxSpeed()) {
                        selectedPlayer.setCurrentSpeed(selectedPlayer.getMaxSpeed());
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_Q) {
                    if (!selectedPlayer.isHavingTheBall()) {
                        selectedPlayer.setWantToChangeSelectedPlayer(true);
                        selectedPlayer.setIsLeftPressed(false);
                        selectedPlayer.setIsRightPressed(false);
                        selectedPlayer.setIsUpPressed(false);
                        selectedPlayer.setIsDownPressed(false);
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    if (selectedPlayer.getField().getBall().isControlledByPlayer() && selectedPlayer.getField().getBall().getWhoPLayerHasTheBall() != null && selectedPlayer.getField().getBall().getWhoPLayerHasTheBall() == selectedPlayer) {
                        selectedPlayer.getField().getBall().setGivePass(true);
                    } else if (selectedPlayer.getField().getBall().isControlledByPlayer() && selectedPlayer.getField().getBall().getWhoPLayerHasTheBall() != null && selectedPlayer.getField().getBall().getWhoPLayerHasTheBall() != selectedPlayer) {
                        selectedPlayer.setIsPressing(true);
                    }
                }
                repaint();
                revalidate();
            }
            repaint();
        }

        /**
         *
         * @param e
         */
        @Override
        public void keyReleased(KeyEvent e) {

            if (!selectedPlayer.getField().isIsPlayingOnline() && otherTeamSelectedPlayer != null && !selectedPlayer.getField().isIsSingleGame()) {// ama den paizei diplo mporw na allaksw tin thesi tou antipalou apo to idio plikrologio

                if (e.getKeyCode() == KeyEvent.VK_M) {

                    otherTeamSelectedPlayer.setIsDownPressed(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_I) {
                    otherTeamSelectedPlayer.setIsUpPressed(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_L) {
                    otherTeamSelectedPlayer.setIsRightPressed(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_J) {
                    otherTeamSelectedPlayer.setIsLeftPressed(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_P) {
//                    System.out.println("shotPower : " + otherTeamSelectedPlayer.getPowerBarPower());
                    if (otherTeamSelectedPlayer != null) {
                        if (otherTeamSelectedPlayer.getField().getBall() != null) {

                            if (selectedPlayer.getField().getBall().isControlledByPlayer() && otherTeamSelectedPlayer.isHavingTheBall() && otherTeamSelectedPlayer.isTakingAShoot()) {
                                otherTeamSelectedPlayer.getField().getBall().shoot(otherTeamSelectedPlayer.getTeam().isPlayingHome(), false, otherTeamSelectedPlayer.getPowerBarPower());
                                if (otherTeamSelectedPlayer.getTime() != null) {
                                    otherTeamSelectedPlayer.getTime().stop();
                                    otherTeamSelectedPlayer.setTime(null);
                                }
                            }
                        }
                        otherTeamSelectedPlayer.setTakingAShoot(false);
                    }

                    otherTeamSelectedPlayer.setPowerBarPower(6);
                }
                if (e.getKeyCode() == KeyEvent.VK_U) {

                    otherTeamSelectedPlayer.setCurrentSpeed(Player.NORMAL_SPEED);
                }
            }



            if ((otherPlayerTeam == null || otherPlayerTeam != selectedPlayer.getTeam()) && selectedPlayer != null) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    selectedPlayer.setIsDownPressed(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    selectedPlayer.setIsUpPressed(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    selectedPlayer.setIsRightPressed(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    selectedPlayer.setIsLeftPressed(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    if (getBall() != null && getBall().isControlledByPlayer() && selectedPlayer.isHavingTheBall() && selectedPlayer.isTakingAShoot()) {
                        //       System.out.println("shotPower : " + powerBarPower);
                        getBall().shoot(selectedPlayer.getTeam().isPlayingHome(), false, selectedPlayer.getPowerBarPower());
                        if (selectedPlayer.getTime() != null) {
                            selectedPlayer.getTime().stop();
                            selectedPlayer.setTime(null);

                        }

                        selectedPlayer.setTakingAShoot(false);
                    }

                    selectedPlayer.setPowerBarPower(6);

                }
                if (e.getKeyCode() == KeyEvent.VK_W) {

                    selectedPlayer.setCurrentSpeed(Player.NORMAL_SPEED);
                }
                if (e.getKeyCode() == KeyEvent.VK_S) {

                    selectedPlayer.setIsPressing(false);
                }


            }
            revalidate();
            repaint();
        }
    }

    /**
     * zwgafizw to scor
     *
     * @param g2d
     */
    public void drawScore(Graphics2D g2d) {
        if (team1.isIsMyTeam()) {
            g2d.setColor(Color.yellow);
        } else {
            g2d.setColor(Color.red);
        }



        if (team1.isPlayingHome()) {
            g2d.drawString(Integer.toString(team1.getScore()), getWidth() / 2 - getWidth() / 20, getHeight() / 5);
        } else {
            g2d.drawString(Integer.toString(team1.getScore()), getWidth() / 2 + getWidth() / 20, getHeight() / 5);
        }



        if (team2.isIsMyTeam()) {
            g2d.setColor(Color.yellow);
        } else {
            g2d.setColor(Color.red);
        }
        if (team2.isPlayingHome()) {

            g2d.drawString(Integer.toString(team2.getScore()), getWidth() / 2 - getWidth() / 20, getHeight() / 5);

        } else {
            g2d.drawString(Integer.toString(team2.getScore()), getWidth() / 2 + getWidth() / 20, getHeight() / 5);
        }


    }

    /**
     *
     * @param g
     */
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        drawTheField(g2d);
        drawWeather(g2d);
        drawScore(g2d);
     
//g2d.draw(ball.getRectangle());

//        for (Player p : team1.getPlayingPlayers()) {
//            g2d.draw(p.getRectangle());
//            if (p.getPlayAtPosition() == Player.position.GK) {
//                g2d.fill(p.getKeeperRectangle());
//            }
//            g2d.draw(this.getRectangleNet(team2, p.getStartingWidth(), p.getStartingHeight()));
//        }
//
//        for (Player p : team2.getPlayingPlayers()) {
//          //  g2d.draw(p.getRectangle());
//         //   g2d.draw(this.getRectangleNet(team1, p.getStartingWidth(), p.getStartingHeight()));
//            if (p.getPlayAtPosition() == Player.position.GK) {
//                g2d.fill(p.getKeeperRectangle());
//               
//            }
//        }
//g2d.draw(ball.getRectangle());



    }

    /**
     *
     * @return
     */
    public boolean isIsPlayingOnline() {
        return isPlayingOnline;
    }

    /**
     *
     * @return
     */
    public boolean isIsSingleGame() {
        return isSingleGame;
    }

    /**
     *
     * @param isSingleGame
     */
    public void setIsSingleGame(boolean isSingleGame) {
        this.isSingleGame = isSingleGame;
    }

    /**
     * an paizw mesa apo diktyo .. kanei aftomata kai to single game=false
     *
     * @param isPlayingOnline
     */
    public void setIPlayingOnline(boolean isPlayingOnline) {
        if (isPlayingOnline) {
            setIsSingleGame(false);
        }
        this.isPlayingOnline = isPlayingOnline;
    }

    /**
     *
     * @return
     */
    public Team getTeam1() {
        return team1;
    }

    /**
     *
     * @param team1
     */
    public void setTeam1(Team team1) {
        this.team1 = team1;
    }

    /**
     *
     * @return
     */
    public Team getTeam2() {
        return team2;
    }

    public SoccerFrame getFrame() {

        return frame;
    }

    /**
     *
     * @param team2
     */
    public void setTeam2(Team team2) {
        this.team2 = team2;
    }

    /**
     *
     * @return
     */
    public Ball getBall() {
        return ball;
    }

    /**
     * rithmizw ton kairo pou tha yparxei sto gipedo
     */
    private enum weather {

        CLEAR, SNOWING
    }

    //PUBLIC API
    /**
     *
     * @param playerId
     */
    public void setSelectedPlayer(String playerId) {
        for (int i = 0; i < getComponentCount(); i++) {
            if (getComponent(i) instanceof Player && ((Player) getComponent(i)).getId().equals(playerId)) {
                ((Player) getComponent(i)).setSelected(true);//automatically deselects same team's previous player.
            }
        }
    }

    /**
     *
     * @param playerId
     * @return
     */
    public Player getPlayer(String playerId) {
        for (int i = 0; i < getComponentCount(); i++) {
            if (getComponent(i) instanceof Player && ((Player) getComponent(i)).getId().equals(playerId)) {
                return ((Player) getComponent(i));
            }
        }
        return null;
    }

    //public void movePlayer(String playerId, double x, double y) {
    //    getPlayer(playerId).moveToRelativeLocationInMeters(x, y);
    //}
    //  public void moveBall(double x, double y) {
    //   getBall().moveToRelativeLocationInMeters(x, y);
    // }
    public void setFloorControl(boolean hasFloor) {
        if (hasFloor) {
            addKeyListener(new KeyboardHandler());
        } else {
            removeKeyListener(getKeyListeners()[0]);
        }
    }

    public static BufferedImage getScreenShot(Component component) {
        BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
        // call the Component's paint method, using
        // the Graphics object of the image.
        component.paint(image.getGraphics());
        return image;
    }

    public void addPlayerSelectionListener() {
        //
    }

    public void removePlayerSelectionListener() {
        //
    }

    public void addPlayerMovedListener() {
        //
    }

    public void removePlayerMovedListener() {
        //
    }

    public void addBallListener() {
        //
    }

    public void removeBallListener() {
        //
    }
    
    public BufferedImage getFieldImage() {
        int width = OverallMiniView.WIDTH;
        int height = OverallMiniView.HEIGHT;
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        paintComponent(g);
        return bi;
    }

    /**
     *
     * @param g
     * @param width
     * @param height
     */
    private void paintRadarComponent(Graphics g, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        drawCostumField(g2d, height, width);
    }

    /**
     * zwgrafizw to gipedo se costum diastaseis
     *
     * @param g
     */
    private void drawCostumField(Graphics2D g, int width, int height) {
        Color[] bgColors = new Color[]{DARK_GREEN, LIGHT_GREEN};


        //zwgrafise th dixromia apo ta aristera mexri to kentro
        int color_step = width / 9;
        for (int i = 0, j = 0; i < width / 2; i += color_step, j++) {
            if (j % 2 == 0) {
                g.setColor(bgColors[0]);
            } else {
                g.setColor(bgColors[1]);
            }
            g.fillRect(i, 0, color_step, height);
        }
        //zwgrafise apo terma deksia mexri to kentro th dixromia
        for (int i = width, j = 0; i > width / 2; i -= color_step, j++) {
            if (j % 2 != 0) {
                g.setColor(bgColors[0]);
            } else {
                g.setColor(bgColors[1]);
            }
            g.fillRect(i + 5, 0, color_step, height);
        }



        g.setColor(Color.white);

        g.drawRect(width / 100, height / 7, width - width / 40, height - height / 25);//<<!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! aspro border ghpedoy.. na ginei mh karfwto wste na paizei kai me to zoom..

        //zografizv thn mesaia grammh
        g.drawLine(width / 2, 0, width / 2, height);
        // zografizw to imikikleio
        int radius = Math.min(width / 5, height / 5);
        g.drawOval(width / 2 - radius / 2, (height - height / 5) / 2, radius, radius);

        // zografizw tis grames tis megalis perioxis
        g.drawRect(width / 100, height / 4, width / 5, height / 2);
        g.drawRect(width - width / 5, height / 4, width / 5 - width / 65, height / 2);

        // zografizw tis grames tis mikris perioxis
        g.drawRect(width / 100, height / 3, width / 9, height / 3);
        g.drawRect(width - width / 9, height / 3, width / 9 - width / 65, height / 3);




        // draw the nets
        //   g.fill(getRectangleNet(team1, width, height));
        //  g.fill(getRectangleNet(team2,width, height));




        //    zografizw to imikikleio tis megalis perioxis
        g.drawArc(width - width / 4, height / 3 + height / 14, width / 10, height / 5, 90, 180);
        g.drawArc(width / 6 - width / 55 + width / 100, height / 3 + height / 13, width / 10, height / 5, 90, -180);

        // draw the nets
        Rectangle terma1NetRect = getRectangleNet(team1);
        int y1 = (int) terma1NetRect.getY() - 5;
        int width1 = (int) terma1NetRect.getWidth();
        int height1 = (int) terma1NetRect.getHeight();
        g.setColor(new Color(170, 170, 170));
        for (int i = y1; i < y1 + height1; i += 5) {
            g.drawLine(0, i, width1, i);
        }
        g.setColor(new Color(190, 190, 190));
        for (int i = 0; i < width1; i += 4) {
            g.drawLine(i, y1, i, y1 + height1);
        }
        g.setColor(Color.BLACK);
        g.drawRect(0, y1, width1, height1);
        //gia na fainonta pio boldarismena ta dokaria kai to front toy termatos metatopizw kata ligo to drawRect
        g.drawRect(0, y1 + 1, width1, height1);
        g.drawRect(0, y1, width1 + 1, height1);

        //draw team's 2 to terma
        Rectangle terma2NetRect = getRectangleNet(team2);
        int y2 = (int) terma2NetRect.getY() - 5;
        int width2 = (int) terma2NetRect.getWidth();
        int height2 = (int) terma2NetRect.getHeight();
        g.setColor(new Color(170, 170, 170));
        for (int i = y2; i < y2 + height2; i += 5) {
            g.drawLine(width - width2, i, width + width2, i);
        }
        g.setColor(new Color(190, 190, 190));
        for (int i = width - width2; i < width; i += 5) {
            g.drawLine(i, y2, i, y2 + height2);
        }

        g.setColor(Color.BLACK);
        g.drawRect(width - width2, y2, width2, height2);
        //gia na fainonta pio boldarismena ta dokaria kai to front toy termatos metatopizw kata ligo to drawRect
        g.drawRect(width - width2 - 1, y2 + 1, width2, height2);
    }

    public void setFrame(SoccerFrame frame) {
        this.frame = frame;
    }

    public String toString() {
        return "field";
    }
}
