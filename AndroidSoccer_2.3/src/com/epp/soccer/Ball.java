package com.epp.soccer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class Ball extends ViewGroup implements Runnable {

	private Paint paint;
	private boolean isControlledByPlayer;// an i mpala elengxete apo ton paixti
	private boolean isGivingPass;// an dinete pasa
	private boolean isTakingAShoot;// an ginete shoot
	private boolean isTakingAShootByKeeper;
	private int power;// i dinami pou askeite stin mpala
	private SoccerActivity context;
	private double passX, passY, shootX, shootY;
	private SoccerField field;
	private boolean savedByKeeper; // ama tin mpala tinn apokrouisei o keeper
	public static boolean isGoal = false; // an mpei goal den mpori na tin ksorisei o goalkeeper i na ginei allh energeia
	private float posX, posY;
	private boolean isMoving = false;
	double posMetersX, posMetersY;
	private Thread clientThread;
private Player playerHasBall=null;

	private SenderThread st;
	private ReceiverThread rt;
	
	public Ball(SoccerActivity context, SoccerField field) {
		super(context);
		this.field = field;
		this.context = context;

		setWillNotDraw(false);
		new Thread(){
			public void run(){
				try {
					Socket socket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);					
					Ball.this.st = new Ball.SenderThread(socket);
                    st.start();
                    Ball.this.rt = new Ball.ReceiverThread(socket);
                    rt.start();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	
	private void setBallPositionToPlayer(String id){
		Player p=getPlayer(id);
		p.setSelected(true);
		setPlayerHasBall(p);
		p.setHasTheBall(true);
		field.postInvalidate();
		//setMoving(true);
		
		postInvalidate();
	//setControlledByPlayer(true);
	}
	
	private Player getPlayer(String id){
		Player player =null;
		for(Player p:(Player[])getField().getTeam1().getTeamPlayers()){
			if(p.getID().equals(id)){
				player=p;
			}
		}
		for(Player p:(Player[])getField().getTeam2().getTeamPlayers()){
			if(p.getID().equals(id)){
				player=p;
			}
		}
		return player;
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
        	if(!isControlledByPlayer){
        	outputWriter.println("ball|$|"+ msg);//ball|$|msg
        }else{
          	outputWriter.println("ball|$|"+playerHasBall.getID()+"##"+ msg);//ball|$|msg     	
        }}
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
                    if(server_msg.contains("Goal")){
                    	if(getField()==null){
    						continue;
    					}
    					getField().getTeam1().setScore(getField().getTeam1().getScore() + 1);
    					getField().getTeam2().setHaveKickOffBall(true);
    					getField().getTeam1().setHaveKickOffBall(false);
    					getField().getBall().setX(getField().getWidth() / 2);
    					getField().getBall().setY(getField().getHeight() / 2);
    					removePlayerMoves();	
    					startingPlayersPositions();
    					continue;
                    }
                    if (!server_msg.contains("Ball")) {
    					continue;
    				}
					if (server_msg.contains("##")) {
						setBallPositionToPlayer(server_msg.split("##")[0]);
						// Log.e("setBallPositionToPlayer",server_msg.split("##")[0]);
					} else {
						setControlledByPlayer(false);
						moveToRelativeLocationInMeters(findPosY(server_msg),
								findPosX(server_msg));
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
		// System.out.println("positionX   =================   "+newMsg);
		positionX = Double.parseDouble(newMsg);
		return positionX;
	}

	public double findPosY(String msg) {
		double positionY = 0;
		String newMsg = msg.toString();
		int start = newMsg.indexOf("positionY= ");
		int end = newMsg.length();
		newMsg = newMsg.substring(start, end);
		// System.out.println(newMsg);
		newMsg = newMsg.replace("positionY= ", "");
		positionY = Double.parseDouble(newMsg);

		return positionY;
	}
	
	public void removePlayerMoves(){
		if(getField()==null){
			return;
		}
		for(Player p:(Player[])getField().getTeam1().getTeamPlayers()){
//			p.getMoves().removeAll(p.getMoves());
			p.setMoves(new ArrayList<Point>());
		}
		for(Player p:(Player[])getField().getTeam2().getTeamPlayers()){
//			p.getMoves().removeAll(p.getMoves());
			p.setMoves(new ArrayList<Point>());
		}
	}
	public void startingPlayersPositions(){
		if(getField()==null){
			return;
		}
		for(Player p:(Player[])getField().getTeam1().getTeamPlayers()){
			p.generateStartPositions();
		}
		for(Player p:(Player[])getField().getTeam2().getTeamPlayers()){
			p.generateStartPositions();
		}

	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		for (int i = 0; i < getChildCount(); i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() == GONE) {
				continue;
			}
			child.layout(l, t, r, l);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		paint = new Paint();
		paint.setColor(Color.WHITE);
		canvas.drawCircle(5, 5, 5, paint);
	}

	/**
	 * arxizei to thread
	 */
	public void start() {
		new Thread(this).start();
	}

	/**
	 * kaleite gia na soutarei o paixtis
	 * 
	 * @param player
	 */
	public void shootKeeper(Player player) {
		if (isTakingAShootByKeeper) {
			return;
		}
		isTakingAShootByKeeper = true; // dilwnw oti o paixtis tha soutarei
		power = 20;// pairnw tin dinami tou sout tou paixti
		if (player.getTeam().isPlayingHome()) {// ama skorarw pros ta katw
			shootX = field.getTeamPlaysAwayNets().right
					- field.getTeamPlaysAwayNets().width() / 2;
			shootY = field.getTeamPlaysAwayNets().top
					+ field.getTeamPlaysAwayNets().height() / 8;
		} else {
			shootX = field.getTeamPlaysHomeNets().right
					+ field.getTeamPlaysAwayNets().width() / 2;
			shootY = field.getTeamPlaysHomeNets().bottom
					+ field.getTeamPlaysAwayNets().height() / 2;
		}
		new Thread(this).start(); // arxizei to thread tou sout
		
	}

	public void shoot(Player player, double shootX, double shootY) {
		if (isTakingAShoot) {
			return;
		}
		isTakingAShoot = true; // dilwnw oti o paixtis tha soutarei
		power = player.getCurrentShootPower();// pairnw tin dinami tou sout tou paixti
		// exw ta simeia tou touchEvent pou dixnoun idi pros to terma arra den xreiazomai to Rect twn net
		this.shootX = shootX;
		this.shootY = shootY;
		new Thread(this).start(); // arxizei to thread tou sout
	}

	public void pass(double playerPosX, double playerPosY, int power) {
		isGivingPass = true;
		passX = playerPosX;
		passY = playerPosY;
		this.power = power;
		// passThread=
		new Thread(this).start();
		// passThread.start();
	}

	;

	@Override
	public void run() {
		if (isGivingPass && !isGoal) {
			// power=(int)(power*((double)field.getLayoutParams().width/context.getFrameLayout().getWidth()));
			while (power >= 0) {
				try {
					setMoving(true);
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				power--;
				//passX+=getWidth();
				//passY+=getHeight();
				context.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (isGoal) {
							setMoving(false);
							return;
						}
						if (field.getRectangle().contains( (int)(getX()+passX),(int)getY()/2)) {
							setX((int)(posX + passX/5));
						}else{
							return;
						}
						if (field.getRectangle().contains( (int)(getX()),(int)(getY()+passY/2))) {
							setY((int)(posY + passY/5));
						
						}else return;
					//	Log.e("passx  = "+Double.toString(passX),"passy = "+Double.toString(passY));
						
						//Log.e("getx = "+Double.toString(getX()),"gety = "+Double.toString(getY()));

					}
				});

				if (isControlledByPlayer || isGoal) {
					if (isGoal) {
						isGivingPass = false;
					}
					setMoving(false);
					fireBallMovedEvent();
					break;
				}
				fireBallMovedEvent();
			}
		//	fireBallMovedEvent();
			setMoving(false);
			isGivingPass = false;
		}

		if (isTakingAShoot || isTakingAShootByKeeper) {
			power += power * 2;
			while (power >= 0 && !isGoal) {
				try {
					setMoving(true);
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (savedByKeeper
						|| getRectangle().intersect(
								field.getTeamPlaysAwayNets()) || isGoal) {
					isGoal = false;
					if (isGoal) {
						isTakingAShoot = false;
					}
					fireBallMovedEvent();
					break;
				}
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (isGoal) {
							setMoving(false);
							return;
						}
						if (shootX > getX()) {
							setX(getX() + 2);
						}
						if (shootX < getX()) {
							setX(getX() - 2);
						}
						if (shootY > getY()) {
							setY(getY() + 5);
						}
						if (shootY < getY()) {
							setY(getY() - 5);
						}

					}
				});
				fireBallMovedEvent();
				power--;
				Log.e("shoot","sutsaadsa");
			}
			fireBallMovedEvent();
			isTakingAShoot = false;
			isTakingAShootByKeeper = false;

		}
		setMoving(false);
	}

	public Rect getRectangle() {
		return new Rect((int) getX(), (int) getY(), (int) getX() + getWidth(),
				(int) getY() + getHeight());
	}

	public boolean isControlledByPlayer() {
		return isControlledByPlayer;
	}

	public void setControlledByPlayer(boolean isControlledByPlayer) {
		this.isControlledByPlayer = isControlledByPlayer;
	}

	public boolean isSavedByKeeper() {
		return savedByKeeper;
	}

	public void setSavedByKeeper(boolean savedByKeeper) {
		this.savedByKeeper = savedByKeeper;
	}

	public boolean isTakingAShoot() {
		return isTakingAShoot;
	}

	public void setIsGoal(boolean isGoal) {
		this.isGoal = isGoal;
	}

	public boolean isTakingAShootByKeeper() {
		return isTakingAShootByKeeper;
	}

	public void setTakingAShootByKeeper(boolean isTakingAShootByKeeper) {
		this.isTakingAShootByKeeper = isTakingAShootByKeeper;
	}

	public void moveToRelativeLocationInMeters(double x, double y) {
		posMetersX = x;
		posMetersY = y;
		
		if (x > SoccerField.SOCCER_FIELD_X_WIDTH_IN_M) {
			x = SoccerField.SOCCER_FIELD_X_WIDTH_IN_M;
		}
		if (y > SoccerField.SOCCER_FIELD_Y_HEIGHT_IN_M) {
			y = SoccerField.SOCCER_FIELD_Y_HEIGHT_IN_M;
		}
		
//		context.runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				setX(field.getWidth()-(float) (x * ((double)field.getWidth() / SoccerField.SOCCER_FIELD_X_WIDTH_IN_M)));
//				setY((float) (y * ((double)field.getHeight() / SoccerField.SOCCER_FIELD_Y_HEIGHT_IN_M)));
				try {
					setX(field.getWidth()-(float) (posMetersX * ((double)field.getWidth() / SoccerField.SOCCER_FIELD_X_WIDTH_IN_M)));
						setY((float) (posMetersY * ((double)field.getHeight() / SoccerField.SOCCER_FIELD_Y_HEIGHT_IN_M)));
				} catch(Exception ex){}
//			}
//		});
	}

//	public void moveToRelativeLocationInMeters(double x, double y, double getWidth, double getHeight) {
//		posMetersX = x;
//		posMetersY = y;
//		SetXY((float) (x * getWidth / SoccerField.SOCCER_FIELD_Y_HEIGHT_IN_M), (float) (y * getHeight / SoccerField.SOCCER_FIELD_X_WIDTH_IN_M));
//
//	}
//
//	public void SetXY(float x, float y) {
//		setX((float) (x));
////		setY((float) (y));
//	}

	public void pixelsToMeters() {
		if ((field.getWidth() <= 0) || (field.getHeight() <= 0)) {
			// return;
		}
		posMetersX = SoccerField.SOCCER_FIELD_X_WIDTH_IN_M * ((getX()) / field.getWidth());
		posMetersY = SoccerField.SOCCER_FIELD_Y_HEIGHT_IN_M * ((getY()) / field.getHeight());
	}

	public void addBallListener() {
	}

	public void removeBallListener() {
	}

	public void fireBallMovedEvent() {
		if (st == null) {
			return;
		}
		Team team = null;
		if (field.getTeam1().isMyTeam()) {
			team = field.getTeam1();
		} else {
			team = field.getTeam2();
		}

		if (!isTakingAShoot && !isGivingPass && !team.getHasTheBall()) {
			return;
		}
		pixelsToMeters();
		st.send("Ball :positionX= " + (posMetersY) + "positionY= " + (SoccerField.SOCCER_FIELD_X_WIDTH_IN_M-posMetersX ));
	}

	public void setX(float x) {
		this.posX =x;
		try {
			context.runOnUiThread(new Thread(){
				public void run(){
					layout((int) posX, (int) posY, (int) posX + getWidth(), (int) posY + getHeight());
							
				}
			});
			// this.setTranslationX(this.getTranslationX()+x);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setY(float y) {
		this.posY = y;
		try {
			// this.setTranslationY(this.getTranslationY()+y);
			context.runOnUiThread(new Thread(){public void run(){
				
					layout((int) posX, (int) posY, (int) posX + getWidth(), (int) posY + getHeight());
			}});
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public float getX() {
		return posX;
	}

	public float getY() {
		return posY;
	}

	public void setPosY(float y) {
		this.posY = y;
	}

	public void setPosX(float x) {
		this.posX = x;
	}

	public boolean isMoving() {
		return isMoving;
	}
	
	public Player getPlayerHasBall() {
		return playerHasBall;
	}


	public void setPlayerHasBall(Player playerHasBall) {
		this.playerHasBall = playerHasBall;
	}


	public void setMoving(boolean isMoving) {	
		if(isMoving){
			fireBallMovedEvent();
		}
		this.isMoving = isMoving;
	}

	public boolean isGivingPass() {
		return isGivingPass;
	}

	public SoccerField getField() {
		return field;
	}
	
}
