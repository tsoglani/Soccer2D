package gr.istl.soccer;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JLabel;

/**
 *
 * @author gaitanesnikos
 */
public class Ball extends JLabel implements Runnable {

    private Point point; // point of ball visual
    private int speed;// speed of ball
    private boolean controlledByPlayer = false;// αμα εχει καποιος παιχτης την μπαλα , αν ναι τον ακολουθει
    private Player ballPLayer;
    private boolean givePass;// an dinw passa
    private boolean takeAShoot;// an kanw shoot
    private SoccerField field;//
    boolean goRightOnShoot = false;
    boolean controlledByCeeper;// to stop shoot
    boolean controlledByKeeper;
    private double xLocationInMeters, yLocationInMeters;
    private boolean isMoving;
    private SenderThread st;
    private ReceiverThread rt;
    public static final int BALL_WIDTH = 5;
    public static final int BALL_HEIGHT = 5;

    /**
     *
     * @param field
     */
    public Ball() {
        point = new Point();

        new Thread() {
            public void run() {
                try {
                    Socket socket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
                    st = new Ball.SenderThread(socket);
                    st.start();
                    rt = new Ball.ReceiverThread(socket);
                    rt.start();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    class SenderThread extends Thread {

        Socket socket;
        PrintWriter outputWriter;

        SenderThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                outputWriter = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            outputWriter.println("ball");
            while (true) {
                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void send(String msg) {

            if (isControlledByPlayer()) {
                outputWriter.println("ball|$|" + ballPLayer.getId() + "##" + msg);//ball|$|msg

            } else {
                outputWriter.println("ball|$|" + msg);//ball|$|msg
            }

        }
    }

    class ReceiverThread extends Thread {

        Socket socket;
        BufferedReader in;

        ReceiverThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true) {//receive server msg
                    String server_msg = in.readLine();//clean msg without sender's ip
//                    jTextArea1.append(server_msg);
                    if (!server_msg.contains("Ball")) {
                        return;
                    }
                    if (server_msg.contains("##")) {
                        setBallPositionToPlayer(server_msg.split("##")[0]);
                    } else {
                        setWhoPLayerHasTheBall(null);
                        moveToRelativeLocationInMeters(findPosX(server_msg), findPosY(server_msg));
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public double findPosX(String msg) {
        double positionX = 0;
        String newMsg;
        newMsg = msg.replace("Ball :positionX= ", "");
        int end = newMsg.indexOf("positionY");
        newMsg = newMsg.substring(0, end);
        //System.out.println("positionX   =================   "+newMsg);
        positionX = Double.parseDouble(newMsg);
        return positionX;
    }

    public double findPosY(String msg) {
        double positionY = 0;
        String newMsg = msg.toString();
        int start = newMsg.indexOf("positionY= ");
        int end = newMsg.length();
        newMsg = newMsg.substring(start, end);
        //  System.out.println(newMsg);
        newMsg = newMsg.replace("positionY= ", "");
        positionY = Double.parseDouble(newMsg);

        return positionY;
    }

    protected void setField(SoccerField field) {
        this.field = field;
    }

    private boolean updateField() {
        try {
            field = (SoccerField) this.getParent();
            point.x = field.getWidth() / 2;
            point.y = field.getHeight() / 2;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     *
     */
    @Override
    public void run() {
        while (!updateField()) {
        }
        while (true) {

            setIsMoving(false);
            try {
                Thread.sleep(50);
                if (takeAShoot) {
                    shootAlgorithm();
                }

                if (ballPLayer != null) {
                    if (isControlledByPlayer() && givePass) {
                        pass(20, ballPLayer.isIsUpPressed(), ballPLayer.isIsDownPressed(), ballPLayer.isIsRightPressed(), ballPLayer.isIsLeftPressed());
                    }
                }
                if (field.getRectangleNet(field.getTeam1()).intersects(getRectangle())) {
                    System.out.println("goaaaaaalll for team 2");
                    PrintCommandsDialog.print("Goal for team " + field.getTeam1().getName());
                    canselAllPlayerMoves();
                    if (!field.getTeam2().isPlayingHome()) {
                        field.getTeam2().setScore(field.getTeam2().getScore() + 1);
                    } else {
                        field.getTeam1().setScore(field.getTeam1().getScore() + 1);
                    }

                    point.x = field.getWidth() / 2;
                    point.y = field.getHeight() / 2;
                    for (Player p : field.getTeam1().getPlayingPlayers()) {
                        p.kickOffPositions();
                    }
                    for (Player p : field.getTeam2().getPlayingPlayers()) {
                        p.kickOffPositions();
                    }
                    kickOffPosition();
                    fireBallMovedEvent(true);
                }
                if (field.getRectangleNet(field.getTeam2()).intersects(getRectangle())) {
                    System.out.println("goaaaaaalll for team 1");
                    PrintCommandsDialog.print("Goal for team " + field.getTeam2().getName());
                    canselAllPlayerMoves();
                    if (field.getTeam1().isPlayingHome()) {
                        field.getTeam1().setScore(field.getTeam1().getScore() + 1);
                    } else {
                        field.getTeam2().setScore(field.getTeam2().getScore() + 1);
                    }
                    field.repaint();
                    point.x = field.getWidth() / 2;
                    point.y = field.getHeight() / 2;
                    for (Player p : field.getTeam1().getPlayingPlayers()) {
                        p.kickOffPositions();

                    }
                    for (Player p : field.getTeam2().getPlayingPlayers()) {
                        p.kickOffPositions();
                    }
                    kickOffPosition();
                    fireBallMovedEvent(true);
                    sendGoal();
                }
                //    System.out.println(isMoving);
                if (isMoving) {
                    fireBallMovedEvent(false);
                }

                repaint();
                validate();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void kickOffPosition() {
        moveToRelativeLocationInMeters(SoccerField.SOCCER_WIDTH_IN_M / 2, SoccerField.SOCCER_HEIGHT_IN_M / 2);
    }

    /**
     * kaleite apo ton player gia shoot
     *
     * @param goWhere
     * @param controlledByKeeper
     * @param power
     */
    public void shoot(boolean goWhere, boolean controlledByKeeper, int power) {
//        if (field.getWidth() > Player.startingWidth) {
//            speed = (int) (1.5 * power * (field.getWidth() / Player.startingWidth));
//        } else {
        speed = (int) (1.5 * power);
//        }
        goRightOnShoot = goWhere;
        controlledByCeeper = controlledByKeeper;
        takeAShoot = true;
    }

    public void shootAlgorithm() throws InterruptedException {
        if (!controlledByPlayer) {
            takeAShoot = false;
            return;
        }
        PrintCommandsDialog.print("o paixtis  " + getWhoPLayerHasTheBall().getName() + " is taking a shot ");

        //controlledByCeeper = false;
        //  System.out.println("ready to take a shot");
        setIsMoving(true);
        //     System.out.println("taking a fucking shot");
        takeAShoot = true;

        while (speed > 0) {

            //System.out.println("runningggggggg");
            controlledByPlayer = false;// gia na min mporei enas paixtis na parei thn mpala kathos ginete to shoot
            Thread.sleep(speed * 4 - speed * 3);
            if (goRightOnShoot) {// ama pazw entos kai kanw sout i mpala paei aristera aliws deksia to pairnw apo to isMyTeam()  boolean tis klasis Team
                if (point.x + speed + 2 < field.getWidth() - 5) {
                    point.x += speed + 2;
                } else if (point.x + 12 < field.getWidth() - 5) {
                    point.x += 12;
                } else if (point.x + 6 < field.getWidth() - 5) {
                    point.x += 6;
                } else if (point.x + 2 < field.getWidth() - 5) {
                    point.x += 2;
                } else {

                    break;
                }

            } else {
                if (point.x - speed - 2 > 0) {
                    point.x -= speed - 2;
                } else if (point.x - 12 > 0) {
                    point.x -= 12;
                } else if (point.x - 6 > 0) {
                    point.x -= 6;
                } else if (point.x - 2 > 0) {
                    point.x -= 2;
                } else {
                    break;
                }
            }

            if (point.y > field.getHeight() / 2) { // ama to ipsos tis pragmatikis thesi tis mpalas mesa sto gipedo einai > tou misou tou ypsouis tou pragmatikou gipedou 
                //  System.out.println(field.getRealHeightPx());
                point.y -= 3;
            } else {

                point.y += 3;

            }

            updateXAxisPoint();
            updateYAxisPoint();
            field.repaint();
            field.revalidate();
            speed--;
            if (controlledByKeeper) {
                break;
            }
            fireBallMovedEvent(true);
        }
        setControlledByPlayer(false); // i mpala den akolouthei kanena paixth
        takeAShoot = false;

        setControlledByPlayer(false);
        ballPLayer = null;

    }

    public void sendGoal() {
        st.send("Goal");
    }

    /**
     *
     * @return
     */
    public Rectangle showVisualPlayers() {
        return new Rectangle(point.x - field.getWidth() / 4 / 2, point.y - field.getHeight() / 2 / 2, field.getWidth() / 4, field.getHeight() / 2);
    }

    /**
     *
     * @return
     */
    @Override
    public Dimension getPreferredSize() {
        return super.getPreferredSize();
    }

    /**
     * FIXED - NOT TESTED YET
     *
     * @return
     */
    public Point getLocationInPixels() { //  metatrepei to point se screenPoint
        return point;
    }

    /**
     * kaleite kai dinei passa o paixtis
     *
     * @param power
     * @param goesToYminus
     * @param goesToYPlus
     * @param goesToXplus
     * @param goesToXMinus
     */
    public void pass(int power, boolean goesToYminus, boolean goesToYPlus, boolean goesToXplus, boolean goesToXMinus) {
        if (power > 20) {
            power = 20;

        }

        if (isControlledByPlayer() && givePass) {// algorithmos gia pasas
            PrintCommandsDialog.print("player  " + getWhoPLayerHasTheBall().getName() + " is passing ");
            givePass = true;

            setIsMoving(true);
            //  oldPlayer=ballPLayer;
            // ballPLayer = null;
            setControlledByPlayer(false);
//            System.out.println("pass");
            power = power * 3;
            while (power >= 0) {
                try {

                    Thread.sleep(11);
                    if (ballPLayer != null) {
                        if (isControlledByPlayer()) {
                            break;
                        }
                    }

                    if (goesToYminus) {
                        if (field.getRectangle().contains(getBounds().x, getBounds().y - getRectangle().height)) {
                            point.y -= 3;
                        }

                        setIsMoving(true);
                    }
                    if (goesToYPlus) {
                        if (field.getRectangle().contains(getBounds().x, getBounds().y + getRectangle().height + 3 * 3)) {
                            point.y += 3;
                        }

                        setIsMoving(true);
                    }
                    if (goesToXplus) {
                        if (field.getWidth() > point.x + field.getWidth() / 40) {
                            point.x += 3;
                        }

                        setIsMoving(true);
                    }
                    if (goesToXMinus) {
                        if (point.x > field.getWidth() / 40) {
                            point.x -= 3;
                        }

                        setIsMoving(true);
                    }
//                    if (!goesToYPlus) { // an dinw pasa kai vgainei eksw apo to gipedo i mpala mpos tin panw meria paw tin mpala 1 bima pisw
//                        if (!field.getRectangle().contains(point.x, point.y + getRectangle().height + 3 * 3)) {
//                            point.y -= 3;
//                        }
//                    }

                    // 
                    power--;
                    updateXAxisPoint();
                    updateYAxisPoint();
                    field.repaint();
                    field.revalidate();
                    fireBallMovedEvent(true);
                } /**
                 * algorithm gia passa
                 *
                 *
                 */
                catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

        }
        field.repaint();
        field.revalidate();

        givePass = false;

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.fillOval(0, 0, 5, 5); // zografizw tin mpala
        g2d.setColor(Color.white);
        g2d.drawLine(3, 3, 4, 4);

    }

    /**
     *
     * @param controlledByCeeper
     */
    public void setControlledByKeeper(boolean controlledByCeeper) {
        this.controlledByCeeper = controlledByCeeper;
    }

    /**
     *
     * @return
     */
    public boolean isTakeAShoot() {
        return takeAShoot;
    }

    /**
     *
     * @param takeAShoot
     */
    public void setTakeAShoot(boolean takeAShoot) {
        this.takeAShoot = takeAShoot;
    }

    /**
     *
     * @return
     */
    public boolean isGivePass() {
        return givePass;
    }

    /**
     *
     * @param givePass
     */
    public void setGivePass(boolean givePass) {
        this.givePass = givePass;
    }

    /**
     *
     * @return
     */
    public Player getWhoPLayerHasTheBall() {
        return ballPLayer;
    }

    private void setBallPositionToPlayer(String id) {
        System.out.println("set ball position to player by socket");
        Player p = getPlayer(id);
        setWhoPLayerHasTheBall(p);
        p.setHasTheBall(true);
        setControlledByPlayer(true);
        this.moveToRelativeLocationInMeters(p.getxLocationInMeters() + 0.5, p.getyLocationInMeters() + 1);
        //setControlledByPlayer(true);
    }

    private Player getPlayer(String id) {
        Player player = null;
        for (Player p : getField().getTeam1().getPlayingPlayers()) {
            if (p.getId().equals(id)) {
                player = p;
            }
        }
        for (Player p : getField().getTeam2().getPlayingPlayers()) {
            if (p.getId().equals(id)) {
                player = p;
            }
        }
        return player;
    }

    /**
     *
     * @param ballPLayer
     */
    public void setWhoPLayerHasTheBall(Player ballPLayer) {
        this.ballPLayer = ballPLayer;
    }

    /**
     *
     */
    public void start() {
        new Thread(this).start();
    }

    /**
     *
     * @return
     */
    public boolean isControlledByPlayer() {
        return controlledByPlayer;
    }

    /**
     *
     * @param controlledByPlayer
     */
    public void setControlledByPlayer(boolean controlledByPlayer) {

        this.controlledByPlayer = controlledByPlayer;

    }

    /**
     *
     * @return
     */
    public Point getPoint() {
        return point;
    }

    /**
     *
     * @return
     */
    public Rectangle getRectangle() {
        return new Rectangle(point.x, point.y, 5, 5);
    }

    /**
     *
     * @param point
     */
    public void setPoint(Point point) {
        this.point = point;
    }

    /**
     *
     * @param isMoving
     */
    public void setIsMoving(boolean isMoving) {
        this.isMoving = isMoving;

    }

    /**
     *
     * @param x
     * @param y
     */
    public void moveToRelativeLocationInMeters(double xMeters, double yMeters) {
        xLocationInMeters = xMeters;
        yLocationInMeters = yMeters;

        refresh();
        getParent().validate();
    }

    public Point refresh() {
        int widthFieldPixels = field.getWidth();//800
        int heightFieldPixels = field.getHeight();//600

        double w_One_Meter_Pixels = (double) widthFieldPixels / SoccerField.SOCCER_WIDTH_IN_M;// 1metroInPixels = 800px/120m
        double h_One_Meter_Pixels = (double) heightFieldPixels / SoccerField.SOCCER_HEIGHT_IN_M;//1metroInPixels = 600px/90m
        point.x = (int) (xLocationInMeters * w_One_Meter_Pixels);
        point.y = (int) (yLocationInMeters * h_One_Meter_Pixels);
//        repaint();
        return point;
    }

    /**
     * gia na min akirothoun oi kiniseis pou mporei na exei dwsei o user me to drag and drop xrysimopoieite kyriws gia otan mpenei kapoio goal
     */
    private void canselAllPlayerMoves() {
        Team team1 = field.getTeam1();
        for (int i = 0; i < team1.getPlayingPlayers().size(); i++) {
            team1.getPlayingPlayers().get(i).getMovesNonSelected().removeAll(team1.getPlayingPlayers().get(i).getMovesNonSelected());
            //  team1.getPlayingPlayers().get(i).setMovesNonSelected(new ArrayList<Point>());
        }
        Team team2 = field.getTeam2();
        for (int i = 0; i < team2.getPlayingPlayers().size(); i++) {
            team2.getPlayingPlayers().get(i).getMovesNonSelected().removeAll(team2.getPlayingPlayers().get(i).getMovesNonSelected());
            //  team2.getPlayingPlayers().get(i).setMovesNonSelected(new ArrayList<Point>());

        }

    }

    private void updateXAxisPoint() {
        double w_One_Meter_Pixels = (double) field.getWidth() / SoccerField.SOCCER_WIDTH_IN_M;// 1metroInPixels = 800px/120m
        xLocationInMeters = point.x / w_One_Meter_Pixels;
    }

    private void updateYAxisPoint() {
        double h_One_Meter_Pixels = (double) field.getHeight() / SoccerField.SOCCER_HEIGHT_IN_M;// 1metroInPixels = 800px/120m
        yLocationInMeters = point.y / h_One_Meter_Pixels;
    }

    public void addBallListener() {
    }

    public void removeBallListener() {
    }

    public void fireBallMovedEvent(boolean localMode) {
        //System.out.println("ball moved!!!!");
        if (localMode) {
            st.send("Ball :positionX= " + xLocationInMeters + "positionY= " + yLocationInMeters);
        } else {
            Player ballHolder = getWhoPLayerHasTheBall();
            if (ballHolder != null) {
                if (ballHolder.isIsRightPressed()) {
                    st.send("Ball :positionX= " + (xLocationInMeters + 1.5) + "positionY= " + (yLocationInMeters + 1.7));
                } else if (ballHolder.isIsLeftPressed()) {
                    st.send("Ball :positionX= " + (xLocationInMeters + 0.4) + "positionY= " + (yLocationInMeters + 1.7));
                } else if (ballHolder.isIsUpPressed()) {
                    st.send("Ball :positionX= " + (xLocationInMeters + 0.5) + "positionY= " + (yLocationInMeters + 1.2));
                } else if (ballHolder.isIsDownPressed()) {
                    st.send("Ball :positionX= " + (xLocationInMeters + 0.9) + "positionY= " + (yLocationInMeters + 2.4));
                }
            }
        }

//        client.setPosX(meterFromPixelsX);
//        client.setPosY(meterFromPixelsY);
//        client.setSending(true);
    }

    public SoccerField getField() {
        return field;
    }
}
