package gr.istl.soccer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicProgressBarUI;

/**
 *
 * @author gaitanesnikos
 */
public class Player extends JPanel implements Runnable {

    public static enum position {

        GK, CB, CB1, CB2, CR, CL, CM, CM1, CM2, MR, ML, SS, CF, CF1, CF2
    };
    private position playAtPosition;
    private int timePlays = 1; // gia na elenksw poso exei paiksei o paixths kai na emfanizete sto progressbar i analogi timh
    static final int NORMAL_SPEED = 7;
    private powerShootThread time;
    private JProgressBar staminaProgressBar;
    // images with motion
    private Image[] imagesUp;
    private Image[] imagesDown;
    private Image[] imagesRight;
    private Image[] imagesLeft;
    private Image[] playerCurentImages; // pinakas me eikones gia animation
    private Image currentImage;
    ////////////////////
    private int teamId;
    private Team team;
    private SoccerField field;
    private boolean takingAShoot;
    private boolean isUpPressed;
    private boolean isDownPressed;
    private boolean isRightPressed;
    private boolean isLeftPressed;
    private String playerName;
    private boolean wantToChangeSelectedPlayer;
    private boolean isSelected;
    private int powerBarPower = 6; // metraw tin dinami pou vazw sto sout kai sxediazw analoga
    private boolean hasYellowCard; // an exei kitrini karta
    private int stamina; // i antoxi tou player
    private int maxSpeed;
    private int playerId;// arithmos fanelas
    private int currentSpeed;// to speed tou paixti
    private boolean hasTheBall;
    private int shootPower = 20;
    private Thread thread;
    private int keepingSkils = 15;
    private Timer changeFocus;
    private boolean canGetTheBall = true;
    private int indexToChangeImages;
    private boolean isPressing;        // an presarei o paixtis gia na pare tin mpala (an dn tin exei  i omada tou )
    private ArrayList<Point> movesNonSelected = new ArrayList<Point>();
    private Point point;// to simeio pou vriskete o player me vasi to arxiko parathiro
    private double xLocationInMeters, yLocationInMeters;
    private boolean isPlayerMoving;  // ama o paixtis kineite ginetai true 
    private boolean isRunning;
    private boolean isMoving;
    public static final int PLAYER_WIDTH = 50;
    public static final int PLAYER_HEIGHT = 50;
    private SenderThread st;
    private ReceiverThread rt;

    public Player(int teamId, int playerId, String playerName, int stamina, int maxSpeed) {
        this.teamId = teamId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.stamina = stamina;
        this.maxSpeed = maxSpeed;
        currentSpeed = NORMAL_SPEED;
        if (stamina > 20) {
            this.stamina = 20;
        }
        if (maxSpeed > 20) {
            this.maxSpeed = 20;
        }

        new Thread() {
            public void run() {
                try {
                    Socket socket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
                    st = new Player.SenderThread(socket);
                    st.start();
                    rt = new Player.ReceiverThread(socket);
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
            outputWriter.println(Player.this.getId());
            while (team.isIsMyTeam()) {
                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void send(String msg) {
            if (outputWriter == null) {
                return;
            }
            outputWriter.println(Player.this.getId() + "|$|" + msg);//a#11|$|msg
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
//                    System.out.println("received msg from server= " + server_msg);
//                    jTextArea1.append(server_msg);
                    if (server_msg.contains("player")) {
                        if (Player.this.getId().equalsIgnoreCase(findId(server_msg))) {
                            setHasTheBall(false);
                            boolean findIfSelected = findIfSelected(server_msg);
                            if (isSelected != findIfSelected) {
                                setSelected(findIfSelected);
                            }
//                            moveToRelativeLocationInMeters(findPosX(server_msg), SoccerField.maxMetersY - findPosY(server_msg) - 1.5);
                            moveToRelativeLocationInMeters(findPosX(server_msg), findPosY(server_msg));
                        }
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private String findId(String msg) {
        int start = 0;
        int end = msg.indexOf("_");
        String newMsg = msg.substring(start, end);
        newMsg = newMsg.replace("player :", "");
        //System.out.println(newMsg);
        return newMsg;
    }

    private boolean findIfSelected(String msg) {
        int start = msg.indexOf("_selected = ");
        int end = msg.indexOf("_", start + 1);
        String newMsg = msg.substring(start, end);
        newMsg = newMsg.replace("_selected = ", "");
        boolean selected = Boolean.parseBoolean(newMsg);
        return selected;
    }

    private double findPosX(String msg) {
        int start = msg.indexOf("_positionX = ");
        int end = msg.indexOf("_", start + 1);
        String newMsg = msg.substring(start, end);
        newMsg = newMsg.replace("_positionX = ", "");
        double newPosX = Double.parseDouble(newMsg);
        // System.out.println(newMsg);
        return newPosX;
    }

    private double findPosY(String msg) {
        int start = msg.lastIndexOf("_");
        int end = msg.length();
        String newMsg = msg.substring(start, end);
        newMsg = newMsg.replace("_positionY = ", "");
        // System.out.println(newMsg);
        double newPosY = Double.parseDouble(newMsg);
        return newPosY;
    }

    protected void setTeam(Team team) {
        this.team = team;
    }

    /*
     * arxikopoiw ta stigmiotypa mou
     */
    private void initComponents() {
        staminaProgressBar = new JProgressBar();
        imagesRight = new Image[3];
        imagesLeft = new Image[3];
        imagesUp = new Image[3];
        imagesDown = new Image[3];
        playerCurentImages = new Image[3];
        point = new Point();
        putImagesToArrays();
        if (getTeam().isPlayingHome()) { // ama paizei entos exei proepilegmeni eikona stin arxi(koitaei deksia)
            currentImage = imagesRight[0];
        } else {// ama paizei ektos exei proepilegmeni eikona stin arxi( aristera)
            currentImage = imagesLeft[0];
        }
        staminaProgressBar.setUI(new StaminaProgressBarUI());
        staminaProgressBar.setSize(22, 3);
        staminaProgressBar.setLocation(7, -1);
        staminaProgressBar.setMinimum(0);
        staminaProgressBar.setMaximum(20);
        staminaProgressBar.setForeground(Color.GREEN);
        addListeners();

        setLayout(null);
        if (playAtPosition != position.GK) {
            add(staminaProgressBar);
        }
    }

    /**
     * vazw tous listeners pou xreiazomai ston paixti
     */
    private void addListeners() {
        addMouseMotionListener(new Listener());
        addMouseListener(new Listener());
    }

    /**
     * FIXED - NOT TESTED YET metatopizw ton paixti sto sigekrimenw point simfwna me 2 double pou antiprosopevoun ta pragmatika metra
     *
     * @param x
     * @param y
     */
    public void moveToRelativeLocationInMeters(double xMeters, double yMeters) {

        if (!team.isIsMyTeam() && playAtPosition != position.GK) {
            PrintCommandsDialog.print("player   " + getName() + "  moved");
        }

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

    protected void kickOffPositions() {
        new Thread() {
            public void run() {
                try {
                    SoccerField.isKickOff = true;
                    sleep(300);
                    SoccerField.isKickOff = false;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
        if (getTeam().isPlayingHome()) {
            if (playAtPosition == position.GK) {
                moveToRelativeLocationInMeters(0, 45);
            }
            if (playAtPosition == position.CL) {//2
                moveToRelativeLocationInMeters(20, 15);
            }
            if (playAtPosition == position.CB1) {//3
                moveToRelativeLocationInMeters(20, 35);
            }
            if (playAtPosition == position.CB2) {//4
                moveToRelativeLocationInMeters(20, 55);
            }
            if (playAtPosition == position.CR) {//5
                moveToRelativeLocationInMeters(20, 75);
            }
            if (playAtPosition == position.ML) {//6
                moveToRelativeLocationInMeters(40, 25);
            }
            if (playAtPosition == position.CM) {//7
                moveToRelativeLocationInMeters(40, 45);
            }
            if (playAtPosition == position.MR) {//8
                moveToRelativeLocationInMeters(40, 65);
            }
            if (playAtPosition == position.CF2) {//11
                moveToRelativeLocationInMeters(57, 50);
            }
            if (playAtPosition == position.SS) {//10
                moveToRelativeLocationInMeters(50, 45);
            }
            if (playAtPosition == position.CF1) {//9
                moveToRelativeLocationInMeters(57, 40);
            }
        }
        firePlayerMovedEvent(xLocationInMeters, yLocationInMeters);
    }

    /**
     * bazw stous pinakes me image tis eikones me ta motion pou tha xrysimopoihsw gia animation (tis pernaw meta sto currentImage analoga poy einai to opoio
     * emfanizw stin eikona m)
     */
    public void putImagesToArrays() {// vazw stous pinakes tis eikones
        String whichSite = null;
        if (!getTeam().isIsMyTeam()) {
            whichSite = "First";
        } else {
            whichSite = "Second";
        }
        for (int i = 0; i < playerCurentImages.length; i++) {
            imagesUp[i] = getToolkit().getImage("imagesSoccer/" + whichSite + "PlayerRun/" + whichSite + "PlayerUp" + i + ".png");
            imagesDown[i] = getToolkit().getImage("imagesSoccer/" + whichSite + "PlayerRun/" + whichSite + "PlayerBack" + i + ".png");
            imagesRight[i] = getToolkit().getImage("imagesSoccer/" + whichSite + "PlayerRun/" + whichSite + "PlayerRight" + i + ".png");
            imagesLeft[i] = getToolkit().getImage("imagesSoccer/" + whichSite + "PlayerRun/" + whichSite + "PlayerLeft" + i + ".png");
        }
    }

    /**
     * to thread tou kathe player
     */
    @Override
    public void run() {
        while (!updateField()) {
        }
        kickOffPositions();
        while (true) {
            if (field != null && field.getBall() != null && !field.getBall().isControlledByPlayer() && !field.getBall().isGivePass()) {
                field.getBall().setControlledByPlayer(false);
                //  System.out.println("false");
            }
            isPlayerMoving = false;

            try {
                if (hasTheBall) {
                    setSelected(true);
                }
                if (playAtPosition != position.GK) {
                    Thread.sleep(70 - currentSpeed * 2);
                } else {// an einai o keeper exei diaforetiko xrono antidrasi
                    Thread.sleep(45 - keepingSkils * 2);
                }
                if (isSelected) {
                    if (isPressing && !hasTheBall) {
                        // presarei ton paixti pou exei tin mpala an den tin exei o idios 
                        press();
                    }
                    if (wantToChangeSelectedPlayer) { // ama thelw na allaksw paixth 
                        wantToChangeSelectedPlayer = false;
                        changeSelectedPlayer();
                    }
                    indexToChangeImages = (indexToChangeImages + 1) % 3;
                }
                keeperAlgorithm();
                if (playAtPosition == position.GK) {
                    continue;
                }
                readyTostealBall();// enengxei an i ali omada exei tin mpala , kai an torectangle tou paixti simpiptei me ayto tis mpalas
                staminaProgressBar.setValue(stamina);
                changeImage(indexToChangeImages);
                if (playAtPosition != position.GK) {
                    runAlgorithm();
                }
                if ((timePlays % 500) == 0) { // rithmizw to progressbar simfona me to stamina tou kathe paixth

                    if (maxSpeed > NORMAL_SPEED) {
                        maxSpeed--; // otan kourastei ligo meiwnete i taxythta tou
                        stamina -= 1;
                    }
//                    System.out.print("Player: " + getName());
//                    SysteSystem.outm.out.print(" _new stamina: " + stamina);
//                    System.out.println(" _new speed : " + maxSpeed);
                }
                field.repaint();
                field.revalidate();
                setIsMoving(isPlayerMoving);
                if (isPlayerMoving) {
                    if (hasTheBall) {
                        field.getBall().setIsMoving(true);
                    }
                }
                //if(isSelected&&team.isIsMyTeam())//p-----------------
                //System.out.println(isRunning);//p-----------------
                if (isRunning) {
//                    System.out.println("in running-------------------------");
//                    if (contollRunningClient == null || (contollRunningClient != null && !contollRunningClient.isAlive())) {
//                        contollRunningClient = new Thread() {
//                            public void run() {
//                                try {
//                                    Thread.sleep(20);
                    firePlayerMovedEvent(xLocationInMeters, yLocationInMeters);
//                                } catch (InterruptedException ex) {
//                                    ex.printStackTrace();
//                                }
//                            }
//                        };
                    //  contollRunningClient.start();
                    //}

                }
                if (isPlayerMoving) {
                    timePlays++; // auksanw ton xrono pou paizei ( pou ton xeirizomai ) gia na meiwsw analoga
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     *
     * @param index gia allagi eikonas kai psevdesthisi motion
     */
    public void changeImage(int index) {

        if (isUpPressed) {
            currentImage = imagesUp[index];
        }
        if (isDownPressed) {

            currentImage = imagesDown[index];
        }
        if (isRightPressed) {
            currentImage = imagesRight[index];
        }
        if (isLeftPressed) {

            if (isSelected && getTeam().isIsMyTeam() && index == 1) {
                index = 2;
            }
            currentImage = imagesLeft[index];
        }

    }

    /**
     * oi algorithmoi olwn ton paixtwn
     */
    private void runAlgorithm() {
        if (!field.isFocusOwner()) {
            return;
        }
        try {
            if (isSelected) {
                selectedPlayerAlgorythm();
                ballFollowPlayer();
                // System.out.println(hasTheBall);

            } else {
                nonSelectedPlayerAlgorithm();
                changePlayerWhilePass();
                // followAnotherTeamMate();

            }

            if (!getRectangle2().intersects(field.getBall().getRectangle()) && hasTheBall) {
                field.getBall().setControlledByPlayer(false);
                hasTheBall = false;

            }
        } catch (Exception e) {
        }

        field.repaint();
        field.validate();

    }

    /**
     * to rectangle tou kathe paixti ( to periblhma tou)
     *
     * @return
     */
    public Rectangle getRectangle() {
        return new Rectangle(point.x, point.y, 29, 24);
    }

    public Rectangle getRectangle2() {
        return new Rectangle(point.x, point.y, 35, 35);
    }

    /**
     * to rectangle tou keeper einai diaforetiko , xreiazete pio megalo gia na pianei k tpt
     *
     * @return
     */
    public Rectangle getKeeperRectangle() {
        if (team.isPlayingHome()) {
            return new Rectangle(point.x, point.y, 30, 25);
            // return new Rectangle(5, 
            //                (int)field.convertYMetersInPixels(SoccerField.SOCCER_HEIGHT_IN_M/2-(SoccerField.MHKOS_TERMATOS_IN_METERS_Y_AXIS/2)), 
            //              5, (int)field.convertYMetersInPixels(SoccerField.MHKOS_TERMATOS_IN_METERS_Y_AXIS));
        } else {
            return new Rectangle(point.x + getWidth() / 4, point.y, 30, 25);
            // return new Rectangle((int)field.convertXMetersInPixels(SOCCER_WIDTH_IN_M)-5, 
            //        (int)field.convertYMetersInPixels(SOCCER_HEIGHT_IN_M/2-(SoccerField.MHKOS_TERMATOS_IN_METERS_Y_AXIS/2)), 
            //        5, 
            //        (int)field.convertYMetersInPixels(SoccerField.MHKOS_TERMATOS_IN_METERS_Y_AXIS));
        }
    }

    /**
     * allazei paixti ama doso pasa ston paixti pou pernei tin mpala ( an perasei apo to rectangle tou mesa)
     */
    public void changePlayerWhilePass() { // otan dinw pasa i mpala na mporei na stamataei se ena paixti an pernaei dipla tou 
        if (playAtPosition == position.GK) {
            return;
        }
        if (field.getBall() == null) {
            return;
        }
        if (getRectangle().intersects(field.getBall().getRectangle()) && !field.getBall().isControlledByPlayer()) {// an i mpala pernaei apo ton paixti kai den tin exei kapoios tote o paixtis ginete o selected
            setSelected(true);
            setIsLeftPressed(false);
            setIsRightPressed(false);
            setIsUpPressed(false);
            setIsDownPressed(false);

        }
    }

    /**
     * an o paixtis den exei tin mpala presarei ton allo paixti pou tin exei( akolouthei tin mpala )
     */
    public void press() {
        indexToChangeImages = (indexToChangeImages + 1) % 3;
        if (point.x > field.getBall().getPoint().x) {
            point.x -= 2;
            currentImage = imagesLeft[indexToChangeImages];

        } else if (point.x < field.getBall().getPoint().x) {
            point.x += 2;
            currentImage = imagesRight[indexToChangeImages];
        }
        if (point.y > field.getBall().getPoint().y) {
            point.y -= 2;
            currentImage = imagesUp[indexToChangeImages];
        } else if (point.y < field.getBall().getPoint().y) {
            point.y += 2;
            currentImage = imagesDown[indexToChangeImages];
        }

    }

    /**
     * gia na akolouthei i mpala ton paixti
     */
    public void ballFollowPlayer() { // otan o user exei tin mpala  (isControlledByPlayer )  ,
        //i mpala kineite analogws mazi tou ,an dn tin exei kapoios mporei na tin apoktisei opoiosdipote apla pernwntas panw apo tin mpala
        if (field.getBall() == null) {
            return;
        }

        //   field.getBall().setControlledByPlayer(false);
        //setHasTheBall(false);
        if (getRectangle().intersects(field.getBall().getRectangle()) && !field.getBall().isTakeAShoot() && canGetTheBall) {
            if (field.getBall().isGivePass() && field.getBall().getWhoPLayerHasTheBall() != null && field.getBall().getWhoPLayerHasTheBall() == this) {
                return;
            }
            field.getBall().setControlledByPlayer(true);
            setHasTheBall(true);

        } else if (getRectangle().intersects(field.getBall().getRectangle())) {
            field.getBall().setControlledByPlayer(false);
            setHasTheBall(false);
            return;
        }

        if (field.getBall().isControlledByPlayer() && isSelected) {
            if (field.getBall().getWhoPLayerHasTheBall() != null && getTeam() != field.getBall().getWhoPLayerHasTheBall().getTeam()) {
                return;
            }

            if (isRightPressed) {
                field.getBall().moveToRelativeLocationInMeters(xLocationInMeters + 1.5, yLocationInMeters + 1.7);
            } else if (isLeftPressed) {
                field.getBall().moveToRelativeLocationInMeters(xLocationInMeters + 0.4, yLocationInMeters + 1.7);
            } else if (isUpPressed) {
                field.getBall().moveToRelativeLocationInMeters(xLocationInMeters + 0.5, yLocationInMeters + 1.2);
            } else if (isDownPressed) {
                field.getBall().moveToRelativeLocationInMeters(xLocationInMeters + 0.9, yLocationInMeters + 2.4);
            }
            field.getBall().setWhoPLayerHasTheBall(this); // lew stin mpala oti o sigekrimenos pextis exei tin katoxi

        }
    }

    public double getxLocationInMeters() {
        return xLocationInMeters;
    }

    public void setxLocationInMeters(double xLocationInMeters) {
        this.xLocationInMeters = xLocationInMeters;
    }

    public double getyLocationInMeters() {
        return yLocationInMeters;
    }

    public void setyLocationInMeters(double yLocationInMeters) {
        this.yLocationInMeters = yLocationInMeters;
    }

    protected void updateXAxisPoint() {
        double w_One_Meter_Pixels = (double) field.getWidth() / SoccerField.SOCCER_WIDTH_IN_M;// 1metroInPixels = 800px/120m
        xLocationInMeters = point.x / w_One_Meter_Pixels;
    }

    protected void updateYAxisPoint() {
        double h_One_Meter_Pixels = (double) field.getHeight() / SoccerField.SOCCER_HEIGHT_IN_M;// 1metroInPixels = 800px/120m
        yLocationInMeters = point.y / h_One_Meter_Pixels;
    }

    /**
     * o algorithmos gia ton paixti pou elengxw
     */
    private void selectedPlayerAlgorythm() {
        if (isDownPressed) {
            isPlayerMoving = true;
            if (field.getRectangle().contains(point.x, point.y + 3 + getRectangle().width)) {
                point.y += field.getWidth() / 300;
            }
        }
        if (isUpPressed) {
            if (field.getRectangle().contains(point.x, point.y - 3)) {
                point.y -= field.getWidth() / 300;
            }
            isPlayerMoving = true;
        }
        if (isLeftPressed) {
            if (field.getRectangle().contains(point.x - 3, point.y)) {
                point.x -= field.getWidth() / 300;
            }

            isPlayerMoving = true;
        }

        if (isRightPressed) {

            if (field.getRectangle().contains(point.x + 3 + getRectangle().width, point.y)) {
                point.x += field.getWidth() / 300;
            }
            isPlayerMoving = true;
        }
        updateXAxisPoint();
        updateYAxisPoint();
    }

    /**
     * o algorithmos gia paixtes pou dn exw alla einai stin idia omada , me drag and drop tous metakinw thesi
     *
     */
    public void nonSelectedPlayerAlgorithm() {
        if (!movesNonSelected.isEmpty()) {
            isPlayerMoving = true;
            indexToChangeImages = (indexToChangeImages + 1) % 3;
            Point nextStation = movesNonSelected.get(0);
            if (point.x > nextStation.x) {
                point.x--;
                currentImage = imagesLeft[indexToChangeImages];
            }
            if (point.x < nextStation.x) {
                point.x++;
                currentImage = imagesRight[indexToChangeImages];
            }
            if (point.y > nextStation.y) {
                point.y--;
                currentImage = imagesUp[indexToChangeImages];
            }
            if (point.y < nextStation.y) {
                point.y++;
                currentImage = imagesDown[indexToChangeImages];
            }
            if (point.x == nextStation.x && point.y == nextStation.y) {//point.x == nextStation.x&&
                movesNonSelected.remove(0);
            }

        } else {
            isPlayerMoving = false;
        }

    }

    /**
     * akolouthane tous paixtes tis omadas tous an exoun tin mpala , mono enas paixtis akolouthaei to arxiko
     */
    private void followAnotherTeamMate() {//o algorithmos gia olous tous paiztes ektos tous selected ( na akolouthane ton paixti tis omadas tous gia pasa)
        if (field.getBall() == null) {
            return;
        }
        if (field.getBall().getWhoPLayerHasTheBall() == null || isSelected || getTeam().isIsFollowBySameTeamPLayer()) {  // a
            return;
        }
        for (Player p : getTeam().getPlayingPlayers()) {
            if (!p.isSelected) {

                continue;
            }

            if (getRectanglePlayerWithBallToFollowTeamMates().intersects(p.getRectanglePlayerWithBallToFollowTeamMates())) {
                Thread thread = null;
                while ((field.getBall().getWhoPLayerHasTheBall() != null && getTeam() != field.getBall().getWhoPLayerHasTheBall().getTeam()) || !isSelected || field.getBall().getWhoPLayerHasTheBall() != null) {
                    indexToChangeImages = (indexToChangeImages + 1) % 3;

                    getTeam().setIsFollowBySameTeamPLayer(true);

                    if (!getRectanglePlayerWithBallToFollowTeamMates().intersects(p.getRectanglePlayerWithBallToFollowTeamMates())) {

                        if (point.x > p.getRectanglePlayerWithBallToFollowTeamMates().getX() && p.getRectanglePlayerWithBallToFollowTeamMates().getWidth() != 0) {
                            point.x--;

                            currentImage = imagesLeft[indexToChangeImages];
                        }
                        if (point.x < p.getRectanglePlayerWithBallToFollowTeamMates().getX() && p.getRectanglePlayerWithBallToFollowTeamMates().getWidth() != 0) {
                            point.x++;
                            currentImage = imagesRight[indexToChangeImages];
                        }
                        if (point.y > p.getRectanglePlayerWithBallToFollowTeamMates().getY() && p.getRectanglePlayerWithBallToFollowTeamMates().getHeight() != 0) {
                            point.y--;
                            currentImage = imagesUp[indexToChangeImages];
                        }
                        if (point.y < p.getRectanglePlayerWithBallToFollowTeamMates().getY() && p.getRectanglePlayerWithBallToFollowTeamMates().getHeight() != 0) {
                            point.y++;
                            currentImage = imagesDown[indexToChangeImages];
                        }
                        if (field.getBall().getWhoPLayerHasTheBall() == null || !field.getBall().isControlledByPlayer()) {
                            break;
                        }
                        updateXAxisPoint();
                        updateYAxisPoint();
                        field.validate();
                        field.repaint();

                    }
                    if (thread == null || (thread != null && !thread.isAlive())) {
                        thread = new Thread() {
                            public void run() {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }

//                                System.out.println("follow anoher team mate ");
                                firePlayerMovedEvent(xLocationInMeters, yLocationInMeters);
                            }
                        };
                        thread.start();
                    }

                }

                getTeam().setIsFollowBySameTeamPLayer(false);
            }

        }
    }

    /**
     *
     * @return proepilegmeno megethos panel
     */
    /**
     * xrysimopoieite gia na paei enas paixtis apo oli tin omada ston paixti pou exei tin mpala (intersect) gia na parei pasa
     *
     * @return
     */
    public Rectangle getRectanglePlayerWithBallToFollowTeamMates() {
        if (field == null || field.getBall() == null || (field.getBall().getWhoPLayerHasTheBall() == null || (field.getBall().getWhoPLayerHasTheBall() != null && !field.getBall().isControlledByPlayer()) || (field.getBall().getWhoPLayerHasTheBall() != null && field.getBall().getWhoPLayerHasTheBall().getTeam() != getTeam()))) {
            return new Rectangle(point.x, point.y, 0, 0);
        } else {
            return new Rectangle(point.x, point.y, 70, 70);
        }
    }

    /**
     * allazw ton paixti mou ( ginete mesa apo to runnable player sto run )
     */
    public void changeSelectedPlayer() {
        Player nearestPlayer = null;
        Collections.shuffle(getTeam().getPlayingPlayers());// gia na exw mia tyxaia allagi paixti an den vrethei kapoios kontina
        for (Player player : getTeam().getPlayingPlayers()) { //pernw tin lista me tous simpextes m (pou paizoun stin 11da)
            if (player.isIsSelected() || player.playAtPosition == position.GK) {// ektos goalkeeper
                continue;// otan allazw paixth den pairnw ayton pou eixa prin , me tin continue prospernaei aytin tin epanalipsi(paei ston epomeno paixti)
            }
            if (nearestPlayer == null) {
                nearestPlayer = player;// o kontinoteros paixtis einai arxika o prwtos tin listas 
            } else if (player.field.getBall().showVisualPlayers().intersects(player.getRectangle())) {// ama exw kapoion allo paixth sto paidio tis mpalas ton elengxw
                nearestPlayer = player;
                break;
            }
        }
        if (nearestPlayer != null) {
            nearestPlayer.setSelected(true);
        }
    }

    /**
     * klevw tin mpala an einai antipalos
     */
    public void readyTostealBall() {// enengxei an i ali omada exei tin mpala , kai an torectangle tou paixti simpiptei me ayto tis mpalas
        if (field.getBall() == null) {
            return;
        }
        if (field.getBall().getWhoPLayerHasTheBall() != null) {
            if (getRectangle().intersects(field.getBall().getRectangle()) && getTeam() != field.getBall().getWhoPLayerHasTheBall().getTeam() && field.getBall().getWhoPLayerHasTheBall() != this) {
                //  field.getBall().getWhoPLayerHasTheBall().sleepPlayerToLoseBall();// 'koimizei' ton paixti pou xanei thn mpala gia na apomakrinthei o allos  
                new Thread() {
                    public void run() {
                        try {
                            canGetTheBall = false;
                            Thread.sleep(1000);
                            canGetTheBall = true;
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }

                    }
                }.start();
                setSelected(true);
                field.getBall().setWhoPLayerHasTheBall(this);
                setHasTheBall(true);
            }
        }

    }

    /**
     * o algorithmos tou termatofilaka
     */
    public void keeperAlgorithm() {
//        if (field.getBall() == null) {
//            return;
//        }
        if (playAtPosition != position.GK) {
            return;
        }
        // mini algorithmos gia na paei sto terma an den einai 
        if (!field.getRectangleNet(team).intersects(getKeeperRectangle())) {
            int extra = 0;
            if (!team.isPlayingHome()) {
                extra = getWidth() / 2;
            }
            if (field.getRectangleNet(team).x > getKeeperRectangle().x + extra) {
                point.x++;
            } else if (field.getRectangleNet(team).x < getKeeperRectangle().x) {
                point.x--;
            }
            updateXAxisPoint();
            if (field.getRectangleNet(team).y > getKeeperRectangle().y) {
                point.y++;
            } else if (field.getRectangleNet(team).y < getKeeperRectangle().y) {
                point.y--;
            }
        } else { /* na akolouthei thn mpala */

            if (field.getBall().getPoint().y > getKeeperRectangle().y) {

                point.y++;
            } else if (field.getBall().getPoint().y < getKeeperRectangle().y) {
                point.y--;
            }
        }
        updateYAxisPoint();

        if (field.getBall() != null) {
            // na diwksei tin mpala
            if (getKeeperRectangle().intersects(field.getBall().getRectangle()) || getRectangle().intersects(field.getBall().getRectangle()) || getRectangle().contains(field.getBall().getRectangle())) {// ama perasei i mpala apo mesa tou ginete selected kanei sout kai allazei aytomata 
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                field.getBall().shoot(getTeam().isIsMyTeam(), true, 20);
            }
        }
    }

    /**
     * thread pou anevazei tin dinami tou paixti mou oso exei patimeno to pliktro gia sout
     */
    public class powerShootThread extends Thread {

        public void run() {
            while (powerBarPower < shootPower) {
                try {

                    sleep(100);
                    powerBarPower++;

                } catch (InterruptedException ex) {
                    //  ex.printStackTrace();
                }
            }
        }
    }

    /**
     *
     * @param g kaleite me tin repaint() kai zografizei to omponent
     */
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        if (isSelected) {
            if (getTeam().isPlayingHome()) {
                g2d.setColor(Color.yellow);
            } //  g2d.drawLine(0, 25, 20, 25);
            else {
                g2d.setColor(Color.red);
            }
            int radius = Math.min(getWidth(), getHeight()) - 10;
            g2d.drawOval(0, 0, radius, radius);
            g2d.drawOval(5, 5, radius - 10, radius - 10);
        }
        g2d.setColor(Color.blue);
        if (takingAShoot) {
            drawShoot2(g2d);
        }
        g2d.setColor(Color.black);

        g2d.drawImage(currentImage, 10, 5, this);

        g2d.setFont(new Font(Font.SERIF, Font.BOLD, 10));
        g2d.drawString(playerName, 5, 30);

    }

    /**
     *
     * @param g2d zografizw tin kampili katw apo ton paixti i kampili dixnei tin dinami tou sout
     */
    public void drawShoot2(Graphics2D g2d) {
        g2d.fillArc(0, 0, 40, 50, 180, -powerBarPower * 9);
        g2d.setColor(field.DARK_GREEN);
        g2d.fillArc(2, 5, 33, 45, 180, -powerBarPower * 9);

    }

    /**
     *
     * @param g2d zografizw tin dinami tou sout me mia eytheia
     */
    public void drawShoot(Graphics2D g2d) {
        g2d.fillRect(5, 25, powerBarPower, 5);

    }

    /**
     *
     * @return
     */
    public position getPlayAtPosition() {
        return playAtPosition;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    /**
     *
     * @param playAtPosition
     */
    public void setPlayAtPosition(position playAtPosition) {
        this.playAtPosition = playAtPosition;

    }

    /**
     *
     * @return
     */
    public boolean isHavingTheBall() {
        return hasTheBall;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(50, 50);

    }

    /**
     *
     * @param hasTheBall mono enas mporei na exei tin mpala (ama tin eixe kapoios alos ginete false)
     */
    public void setHasTheBall(boolean hasTheBall) { // mono 1as paixtis exei tin mpala kai apo tis 2 omades
        if (!hasTheBall) {
            return;
        }

        setSelected(true);

        for (Player player : getTeam().getPlayingPlayers()) {
            if (player != this) {
                player.hasTheBall = false;
            }
        }

        Team otherTeam;
        if (getTeam() == field.getTeam1()) {
            otherTeam = field.getTeam2();
        } else {
            otherTeam = field.getTeam1();
        }
        for (Player player : otherTeam.getPlayingPlayers()) {

            player.hasTheBall = false;

        }
        this.hasTheBall = hasTheBall;
    }

    /**
     *
     * @return
     */
//    public int getStartingHeight() {
//        return startingHeight;
//    }
    /**
     *
     * @return
     */
//    public int getStartingWidth() {
//        return startingWidth;
//    }
    /**
     *
     * @return
     */
    public Timer getChangeFocus() {
        return changeFocus;
    }

    /**
     *
     * @param changeFocus
     */
    public void setChangeFocus(Timer changeFocus) {
        this.changeFocus = changeFocus;
    }

    /**
     *
     * @return
     */
    public boolean isIsPressing() {
        return isPressing;
    }

    /**
     *
     * @return
     */
    public int getPowerBarPower() {
        return powerBarPower;
    }

    /**
     *
     * @param powerBarPower
     */
    public void setPowerBarPower(int powerBarPower) {
        this.powerBarPower = powerBarPower;
    }

    /**
     *
     * @param isPressing
     */
    public void setIsPressing(boolean isPressing) {
        this.isPressing = isPressing;
    }

    /**
     *
     * @return
     */
    public boolean isTakingAShoot() {
        return takingAShoot;
    }

    /**
     *
     * @param takingAShoot
     */
    public void setTakingAShoot(boolean takingAShoot) {
        this.takingAShoot = takingAShoot;
    }

    /**
     *
     * @return
     */
    public powerShootThread getTime() {
        return time;
    }

    /**
     *
     * @param time
     */
    public void setTime(powerShootThread time) {
        this.time = time;
    }

    /**
     *
     * @return
     */
    public int getTimePlays() {
        return timePlays;
    }

    /**
     *
     * @param timePlays
     */
    public void setTimePlays(int timePlays) {
        this.timePlays = timePlays;
    }

    /**
     *
     * @return
     */
    public boolean isWantToChangeSelectedPlayer() {
        return wantToChangeSelectedPlayer;
    }

    /**
     *
     * @param wantToChangeSelectedPlayer
     */
    public void setWantToChangeSelectedPlayer(boolean wantToChangeSelectedPlayer) {
        this.wantToChangeSelectedPlayer = wantToChangeSelectedPlayer;
    }

    /**
     *
     * @return
     */
    public Team getTeam() {
        return team;
    }

    /**
     *
     * @param shootPower
     */
    public void setShootPower(int shootPower) {
        this.shootPower = shootPower;
    }

    /**
     * emfanizei pote o paix
     *
     * @param isMoving
     */
    private void setIsMoving(boolean isMoving) {
        //this.isPlayerMoving == false arg==true
        //this.isPlayerMoving == true arg==true 
        //this.isPlayerMoving == true arg ==false

        if (isMoving == false && this.isMoving == true) {
//            System.out.println("o paixtis  " + playerName + "  tis omadas " + Integer.toString(teamId) + " just stopped ");
            PrintCommandsDialog.print("player " + getName() + " just stopped ");
            isRunning = false;
        }
        if (isMoving == true && this.isMoving == false) {
            isRunning = true;
//            System.out.println("o paixtis  " + playerName + "  tis omadas " + Integer.toString(teamId) + " just started ");
            PrintCommandsDialog.print("player " + getName() + " just started ");
            //   System.out.println("x = " + this.meterFromPixelsX + "  y = " + this.meterFromPixelsY);
        }

        this.isMoving = isMoving;
    }

    /**
     *
     * @return
     */
    public boolean isHasYellowCard() {
        return hasYellowCard;
    }

    /**
     *
     * @param hasYellowCard
     */
    public void setHasYellowCard(boolean hasYellowCard) {
        this.hasYellowCard = hasYellowCard;
    }

    /**
     *
     * @return
     */
    public boolean isIsSelected() {

        return isSelected;
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return playerName;
    }

    /**
     *
     * @return
     */
    private position gePlayAtPosition() {

        return playAtPosition;
    }

    /**
     *
     * @param playAtPosition
     */
    private void setPlayAtPosotion(position playAtPosition) {
        this.playAtPosition = playAtPosition;
    }

    /**
     *
     * @param isSelected kanei olous toys alous paixtes stin idia omada selected=false
     */
    public void setSelected(boolean isSelected) { // mono enas player einai epilegmenos kathe fora
        if (!this.isSelected && isSelected && !SoccerField.isKickOff) {
            gainFocus();
        }

        if (!isSelected || playAtPosition == position.GK) {
            return;
        }
        movesNonSelected.removeAll(movesNonSelected);//ama exw ena paixti afaireite i kinisi pou tou exw prosxediasei ( an tou exw valei )
        for (Player player : getTeam().getPlayingPlayers()) {

            if (player.isSelected && isSelected && player != this && !SoccerField.isKickOff) {
                PrintCommandsDialog.print("player :   " + player.getName() + "   lost focus");
            }
            player.isSelected = false;

        }
        this.isSelected = isSelected;
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
     * @param point
     */
    public void setPoint(Point point) {
        this.point = point;
    }

    /**
     *
     * @return
     */
    public int getCurrentSpeed() {
        return currentSpeed;
    }

    /**
     *
     * @param currentSpeed
     */
    public void setCurrentSpeed(int currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    /**
     *
     * @return
     */
    public boolean isIsDownPressed() {
        return isDownPressed;
    }

    /**
     *
     * @param isDownPressed
     */
    public void setIsDownPressed(boolean isDownPressed) {
        this.isDownPressed = isDownPressed;
    }

    public SoccerField getField() {
        return field;
    }

    //public void setField(Field field) {
    //    this.field = field;
    //    initComponents();
    // }
    public boolean updateField() {
        try {
            field = (SoccerField) getParent();
            initComponents();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     *
     * @return
     */
    public boolean isIsLeftPressed() {
        return isLeftPressed;
    }

    /**
     *
     * @param isLeftPressed
     */
    public void setIsLeftPressed(boolean isLeftPressed) {
        this.isLeftPressed = isLeftPressed;
    }

    /**
     *
     * @return
     */
    public boolean isIsRightPressed() {
        return isRightPressed;
    }

    /**
     *
     * @param isRightPressed
     */
    public void setIsRightPressed(boolean isRightPressed) {
        this.isRightPressed = isRightPressed;
    }

    /**
     *
     * @return
     */
    public boolean isIsUpPressed() {
        return isUpPressed;
    }

    /**
     *
     * @param isUpPressed
     */
    public void setIsUpPressed(boolean isUpPressed) {
        this.isUpPressed = isUpPressed;
    }

    /**
     *
     */
    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    /**
     * stamataei to thread( i class kanei implements to runnable)
     */
    public void suspend() {
        thread.suspend();
    }

    /**
     * sinexizei to thread
     */
    public void resume() {
        thread.resume();
    }

    /**
     *
     */
    private class Listener extends MouseAdapter {

        @Override
        public void mouseDragged(MouseEvent me) {
//            if (isSelected || hasTheBall || !getTeam().isIsMyTeam() || !getTeam().isHavingTheBall()) {//||!field.isIsPlayingOnline()
//                return;
//            }
//            if (!(field.getRectangle().y < (me.getY() + getY()) * startingHeight / (double) field.getRealHeightPx())) {
//                return;
//            }
//            if (point.y + me.getY() > startingHeight) {
//
//                return;
//            }
//            if (!(field.getRectangle().x < (me.getX() + getX()) * startingWidth / (double) field.getRealWidthPx())) {
//                return;
//            }
//            if (point.x + me.getX() > startingWidth) {
//
//                return;
//            }
//            movesNonSelected.add(new Point((int) ((me.getX() + getX() - 20) * startingWidth / (double) field.getRealWidthPx()), (int) ((me.getY() + getY() - 10) * startingHeight / (double) field.getRealHeightPx())));
        }

        /**
         *
         * @param me
         */
        @Override
        public void mouseClicked(MouseEvent me) {
            if (isSelected || hasTheBall || !getTeam().isIsMyTeam()) {
                return;
            }
            if (!getTeam().isHavingTheBall()) {
                setSelected(true);
            }
            movesNonSelected.removeAll(movesNonSelected);
        }
    }

    /**
     *
     * @return
     */
    public ArrayList<Point> getMovesNonSelected() {
        return movesNonSelected;
    }

    /**
     *
     * @param movesNonSelected
     */
    public void setMovesNonSelected(ArrayList<Point> movesNonSelected) {
        this.movesNonSelected = movesNonSelected;
    }

    private static final class StaminaProgressBarUI extends BasicProgressBarUI {

        @Override
        public void paint(Graphics g, JComponent c) {
            g.setColor(Color.RED);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            super.paint(g, c);
        }
    }

    public void addPlayerSelectionListener() {
    }

    public void removePlayerSelectionListener() {
    }

    public void addPlayerMovedListener() {
    }

    public void removePlayerMovedListener() {
    }

    private void gainFocus() {
        //   System.out.println("selected: " + isPlayerSelected + ", " + this.getName());
        PrintCommandsDialog.print("player :   " + getName() + "    is now selected ");
    }

    private void firePlayerMovedEvent(double x, double y) {
        // System.out.println("moved at posoition: " + x + ", " + y);
        try {
            if (!SoccerField.isKickOff && !getTeam().isIsMyTeam()) {
                return; // an thelwm na paiksw sto idio PC svinw to return 
            }
            if (st == null || getId() == null) {
                return;
            }
            st.send("player :" + getId() + "_selected = " + isSelected + "_positionX = " + x + "_positionY = " + y);
//            client.setPosX(x);
//            client.setPosY(y);
//            client.setId(getId());
//            client.setIsSending(true);
            setHasTheBall(false);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getTeamId() {
        return teamId;
    }

    public String getId() {
        return getTeam().getName() + "#" + getName();
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
