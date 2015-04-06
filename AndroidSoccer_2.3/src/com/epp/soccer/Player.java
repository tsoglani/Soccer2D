package com.epp.soccer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * @author gaitanesnikos
 */

public class Player extends ViewGroup implements Runnable {
	private int currentShootPower = 0;
	private int totalWidth, totalHeight;
	private String name;
	private boolean isSelected = true;
	final static int startingSpeed = 7;
	private Team team;
	private int passPower, currentSpeed, shootPower;
	private int topSpeed;
	private boolean goRight, goLeft, goUp, goDown;
	private ArrayList<Point> moves;
	public allPositions playingPosition;
	private SoccerActivity context;
	private SoccerField field;
	private boolean hasTheBall = false;
	private Point nextStation;
	private boolean canGetTheBall = true;
	private boolean isReadyToGivePass;
	private static boolean iswaitingForPass;
	private Rect liteRect;
	private Point currentPointTouchPos = new Point();
	private boolean isReadyToShoot;
	private Thread shootThread;
	public static final int port = 2000;
	private Thread clientThread;
	private Thread thread;

	private boolean isThreadStoping = false;
	double xLocationInMeters, yLocationInMeters, posPixelsX, posPixelsY;
	float posX, posY;
	private Point animationPoint; // allazw to x,y sto draw gia n fainete san
									// animation
	private boolean isMoving = false;
	private boolean isConnectedToServer = false;
	
	private SenderThread st;
	private ReceiverThread rt;
	
	public Player(SoccerActivity context, SoccerField field, Team team, String name, int passPower, int speed, int shootPower) {
		super(context);
		liteRect = getLiteRectangle();
		this.field = field;
		this.team = team;
		this.name = name;
		this.context = context;
		this.passPower = passPower;
		this.topSpeed = speed;
		currentSpeed = startingSpeed;
		this.shootPower = shootPower;
		initialize(context);
		nextStation = new Point();
		setWillNotDraw(false);
		thread = new Thread(this);
		
		new Thread(){
			public void run(){
				try {
					Socket socket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
					st = new Player.SenderThread(socket);
	                st.start();
	                rt = new Player.ReceiverThread(socket);
	                rt.start();
					isConnectedToServer = true;
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
            outputWriter.println(Player.this.getID());
            while (Player.this.team.isMyTeam()) {
                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void send(String msg) {
            outputWriter.println(Player.this.getID()+"|$|"+ msg);
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
                if(!team.isMyTeam()&&playingPosition!=Player.allPositions.GK){
                	setSelected(true);
                }    
//                    jTextArea1.append(server_msg);
                    if(server_msg.contains("player")){                       
                        String playerId = findId(server_msg);
                        if(getID().equals(playerId)){
                        	setHasTheBall(false);
                        	boolean findIfSelected = findIfSelected(server_msg);
                            if (isSelected != findIfSelected) {
                                setSelected(findIfSelected);
                            }
                        	moveToRelativeLocationInMeters( SoccerField.SOCCER_FIELD_X_WIDTH_IN_M-findPosY(server_msg),findPosX(server_msg)+5);
                        	
                        }
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    /**
	 * seperate the id from the original msg
	 * 
	 * @param msg
	 */
	private String findId(String msg) {
		String newMsg = msg;
		try {
			int start = 0;
			int end = msg.indexOf("_");
			newMsg = msg.substring(start, end);
			newMsg = newMsg.replace("player :", "");
		} catch (Exception e) {

		}
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
		double newPosY = Double.parseDouble(newMsg);
		return newPosY;
	}

	/**
	 * 
	 * @param context
	 */
	private void initialize(final SoccerActivity context) {
		moves = new ArrayList<Point>();
		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				

				currentPointTouchPos.x = (int) event.getX();
				currentPointTouchPos.y = (int) event.getY();

				if (!isReadyToGivePass) {// an exei patithei gia na dosw pasa tote akyrwnete i entoli

					if (!getTeam().isMyTeam()) {// ama patisw se paixti alis omadas paw pros ta ekei kai na apistrepsw
						Team otherTeam;
						if (getTeam() == field.getTeam1()) {
							otherTeam = field.getTeam2();
						} else {
							otherTeam = field.getTeam1();
						}

//						otherTeam.getSelectedPlayer().getMoves().removeAll(otherTeam.getSelectedPlayer().getMoves());
						otherTeam.getSelectedPlayer().setMoves(new ArrayList<Point>());
						otherTeam.getSelectedPlayer().nextStation.x = (int) (event.getX() + getX());
						otherTeam.getSelectedPlayer().nextStation.y = (int) (event.getY() + getY());
						return false;
					}
					if (!getTeam().getHasTheBall() && playingPosition != allPositions.GK) {
						setSelected(true);
					}
					if (getRectangle().contains((int) (getX() + event.getX()), (int) (event.getY() + getY()))) {
						moves.removeAll(moves);
						moves.add(new Point((int) (getX() + event.getX()) - getWidth() / 3, (int) (event.getY() + getY() - getHeight() / 3)));
						return false;
					}

					addPlayerPoints((int) event.getX() + (int) getX() - getWidth() / 3, (int) event.getY() + (int) getY() - getHeight() / 3);
				} else {
					Team playerTeam;
					Rect teamsNet;
					if (getTeam() == field.getTeam1()) {
						playerTeam = field.getTeam1();
					} else {
						playerTeam = field.getTeam2();
					}
					if (playerTeam.isPlayingHome()) {// ama eimai o apo panw vazw goal pros ta katw
						teamsNet = field.getTeamPlaysAwayNets();
					} else {
						teamsNet = field.getTeamPlaysHomeNets();
					}
					if (teamsNet.contains((int) (getX() + event.getX()),
							(int) (getY() + event.getY()))) {
						isReadyToShoot = true;
					} else {
						isReadyToShoot = false;
						shootThread = null;
						currentShootPower = 0;
					}
					if (event.getAction() == MotionEvent.ACTION_UP) {
						if (isReadyToShoot) {
							isReadyToShoot = false;
							shootAlgorythm(event.getX() + getX(), event.getY() + getY());
							setIswaitingForPass(false);
							return false;
						}
						setReadyToGivePass(false);
						if (getRectangle().contains((int) (event.getX() + getX()), (int) (event.getY() + getY() - getHeight() / 10))) {
							setIswaitingForPass(false);// katw taytoxrona to isReadyToGivePass = false
							return false;
						} else {
							for (Player p : (Player[]) getTeam() .getTeamPlayers()) {
								if (p.getRectangle().contains((int) (getX() + event.getX()), (int) (event.getY() + getY())) && p != Player.this) {
									passAlgorythm(p);
									return false;
								}
							}
						}
						passAlgorythm((getX() + event.getX()), (event.getY() + getY()));
						setIswaitingForPass(false);

					}
				}
				return false;
			}
		});

		setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (!hasTheBall && getTeam().isMyTeam()) {
					passAlgorythm(Player.this);
					return false;
				}
				if (!Player.this.getLiteRectangle().contains(currentPointTouchPos.x, currentPointTouchPos.y)) {
					return false;
				}
				moves.removeAll(moves);
				setReadyToGivePass(true);
				return false;
			}
		});

	}

	public Point getCurrentPointTouchPos() {
		return currentPointTouchPos;
	}

	
	public void moveToRelativeLocationRotated(double x, double y){
		moveToRelativeLocationInMeters( SoccerField.SOCCER_FIELD_X_WIDTH_IN_M-y,x+5);
		
	}
	
	public void generateStartPositions() {
		if (SoccerField.isKickOff == false) {
			SoccerField.isKickOff = true;
			new Thread() {
				public void run() {
					try {
						sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					SoccerField.isKickOff = false;
				}
			}.start();
		}
		if (getTeam().isPlayingHome()) {
			 if (playingPosition == Player.allPositions.GK) {
				 moveToRelativeLocationRotated(0, 45);
	            }
	           
	            if (playingPosition == Player.allPositions.CB1) {//3
	            	moveToRelativeLocationRotated(20, 35);
	            }
	            if (playingPosition == Player.allPositions.CB2) {//4
	            	moveToRelativeLocationRotated(20, 55);
	            }
	            if (playingPosition == Player.allPositions.CL) {//2
	            	moveToRelativeLocationRotated(20, 15);
	            }
	            if (playingPosition == Player.allPositions.CR) {//5
	            	moveToRelativeLocationRotated(20, 75);
	            }
	            if (playingPosition == Player.allPositions.ML) {//6
	            	moveToRelativeLocationRotated(40, 25);
	            }
	            if (playingPosition == Player.allPositions.CM) {//7
	            	moveToRelativeLocationRotated(40, 45);
	            }
	            if (playingPosition == Player.allPositions.MR) {//8
	            	moveToRelativeLocationRotated(40, 65);
	            }
	            if (playingPosition == Player.allPositions.CF2) {//11
	            	moveToRelativeLocationRotated(57, 50);
	            }
	            if (playingPosition == Player.allPositions.SS) {//10
	            	moveToRelativeLocationRotated(50, 45);
	            }
	            if (playingPosition == Player.allPositions.CF1) {//9
	            	moveToRelativeLocationRotated(57, 40);
	            }

		}
			 else {
				 if (playingPosition == Player.allPositions.GK) {//1
					 moveToRelativeLocationRotated(117, 45);
		            }
		            if (playingPosition == Player.allPositions.CB1) {//3
		            	moveToRelativeLocationRotated(100, 35);
		            }
		            if (playingPosition == Player.allPositions.CB2) {//4
		            	moveToRelativeLocationRotated(100, 55);
		            }
		            if (playingPosition == Player.allPositions.CR) {//5
		            	moveToRelativeLocationRotated(100, 75);
		            }
		            if (playingPosition == Player.allPositions.CL) {//2
		            	moveToRelativeLocationRotated(100, 15);  
		            }
		            if (playingPosition == Player.allPositions.ML) {//6
		            	moveToRelativeLocationRotated(80, 25);
		            }
		            if (playingPosition == Player.allPositions.CM) {//7
		            	moveToRelativeLocationRotated(80, 45);
		            }
		            if (playingPosition == Player.allPositions.MR) {//8
		            	moveToRelativeLocationRotated(80, 65);
		            }
		            if (playingPosition == Player.allPositions.CF2) {//11
		            	moveToRelativeLocationRotated(65, 50);
		            }
		            if (playingPosition == Player.allPositions.SS) {//10
		            	moveToRelativeLocationRotated(70, 45);
		            }
		            if (playingPosition == Player.allPositions.CF1) {//9
		            	moveToRelativeLocationRotated(65, 40);
		            }
		}
		nextStation.x = (int) getX();
		nextStation.y = (int) getY();
		pixelsToMeters();
		firePlayerMovedEvent();
	}

	/**
	 * 
	 * @return
	 */

	/**
	 * 
	 * @param x
	 * @param y
	 */
	public void motionPlayer(int x, int y) {
		if (getRectangle().contains(x, y)) {
			moves.removeAll(moves);
		}

	}

	/**
	 * convert pixel to metter
	 */
	public void pixelsToMeters() {
		if ((field.getWidth() <= 0) || (field.getHeight() <= 0)) {
			// return;
		}
		xLocationInMeters = SoccerField.SOCCER_FIELD_X_WIDTH_IN_M * ((getX()) / field.getWidth());
		yLocationInMeters = SoccerField.SOCCER_FIELD_Y_HEIGHT_IN_M * ((getY()) / field.getHeight());
	}

	public void moveToRelativeLocationInMeters(double xMeters, double yMeters) {

		xLocationInMeters = yMeters;
		yLocationInMeters = xMeters;
		try {
			moves.removeAll(moves);
			
			if (yMeters > SoccerField.SOCCER_FIELD_Y_HEIGHT_IN_M) {
				yMeters = SoccerField.SOCCER_FIELD_Y_HEIGHT_IN_M;
			}
			if (xMeters >  SoccerField.SOCCER_FIELD_X_WIDTH_IN_M) {
				xMeters = SoccerField.SOCCER_FIELD_X_WIDTH_IN_M;
			}
			
			setX((float) (xMeters * (field.getWidth() / SoccerField.SOCCER_FIELD_X_WIDTH_IN_M)));
			setY((float) (yMeters * (field.getHeight() / SoccerField.SOCCER_FIELD_Y_HEIGHT_IN_M)));
			nextStation = new Point();
			nextStation.x = (int) (float) (yMeters * field.getWidth() / SoccerField.SOCCER_FIELD_X_WIDTH_IN_M);
			nextStation.y = (int) (yMeters * field.getHeight() / SoccerField.SOCCER_FIELD_Y_HEIGHT_IN_M);
			moves.add(nextStation);
			context.runOnUiThread(new Thread() {
				public void run() {
					field.setBackgroundColor(Color.GREEN);
					
					field.invalidate();
					invalidate();
					field.postInvalidate();
					postInvalidate();
					field.setBackgroundColor(Color.GREEN);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
	}

	public void pixelsFromMeters(float x, float y) {

		setX((float) (field.getWidth() * SoccerField.SOCCER_FIELD_Y_HEIGHT_IN_M / x));
		setY((float) (field.getHeight() * SoccerField.SOCCER_FIELD_X_WIDTH_IN_M / y));
		moves.removeAll(moves);
	}

	/**
	 * convert meters to pixel
	 */
	public void metersToPixels() {
		if ((field.getWidth() <= 0) || (field.getHeight() <= 0) || xLocationInMeters <= 0) {
			return;
		}
		posPixelsX = xLocationInMeters * field.getWidth() / SoccerField.SOCCER_FIELD_X_WIDTH_IN_M;
		posPixelsY = yLocationInMeters * field.getHeight() / SoccerField.SOCCER_FIELD_Y_HEIGHT_IN_M;

	}

	/**
     * 
     */
	public void shootAlgorythm() {
		setHasTheBall(false);
		canGetTheBall = false;
		field.getBall().shootKeeper(this);
		isMoving = true;
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
					isMoving = false;
					canGetTheBall = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
     * 
     */
	public void shootAlgorythm(double shootX, double shootY) {
		setHasTheBall(false);
		canGetTheBall = false;
		field.getBall().shoot(this, shootX, shootY);
		isMoving = true;
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
					isMoving = false;
					canGetTheBall = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void passAlgorythm(Player playerToSendBall) {
		final Player playerHavingBall = getTeam().gePlayerWhoHasTheBall();
		if (playerHavingBall == null) {
			return;
		}
		playerHavingBall.setHasTheBall(false);
		playerHavingBall.canGetTheBall = false;
		field.getBall().pass(playerToSendBall.getX(), playerToSendBall.getY(),
				playerToSendBall.passPower);
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
					playerHavingBall.canGetTheBall = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	public void passAlgorythm(double x, double y) {
		final Player playerHavingBall = getTeam().gePlayerWhoHasTheBall();
		if (playerHavingBall == null) {
			return;
		}
		playerHavingBall.setHasTheBall(false);
		playerHavingBall.canGetTheBall = false;
		field.getBall().pass(x, y, passPower);
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
					playerHavingBall.canGetTheBall = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
     * 
     */
	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		for (int i = 0; i < getChildCount(); i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() == GONE) {
				continue;
			}
			else {
				child.layout(0, 0, arg3, arg4 / 10);
			}
		}

	}

	/**
     *    
     */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint();
		if (isSelected && getTeam().isMyTeam()) {
			paint.setColor(getTeam().getClr());
            canvas.drawCircle(15, 15, 15, paint);//commented by vellis :draws the focus?? 
			paint.setColor(Color.GREEN);
			canvas.drawCircle(15, 15, 14, paint);
		}else{
			if (isSelected && !getTeam().isMyTeam()){
				paint.setColor(Color.RED);
				canvas.drawCircle(15, 15, 14, paint);	
				paint.setColor(Color.GREEN);
				canvas.drawCircle(15, 15, 14, paint);
		}
		}

		if (isReadyToGivePass) {
			paint.setColor(getTeam().getClr());
			canvas.drawCircle(15, 15, 17, paint);
			paint.setColor(Color.GREEN);
			canvas.drawCircle(15, 15, 16, paint);
			paint.setColor(getTeam().getClr());
			paint.setColor(getTeam().getClr());
			canvas.drawCircle(15, 15, 15, paint);
			paint.setColor(Color.GREEN);
			canvas.drawCircle(15, 15, 14, paint);
			canvas.drawCircle(15, 15, 13, paint);
			paint.setColor(Color.GREEN);
			canvas.drawCircle(15, 15, 12, paint);
			paint.setColor(getTeam().getClr());
			canvas.drawCircle(15, 15, 11, paint);
			paint.setColor(Color.GREEN);
			canvas.drawCircle(15, 15, 10, paint);
			paint.setColor(getTeam().getClr());
			canvas.drawCircle(15, 15, 9, paint);
			paint.setColor(Color.GREEN);
			canvas.drawCircle(15, 15, 8, paint);

		}
		if (isReadyToShoot) {
			drawShoot(canvas, paint);
		}
		paint.setColor(getTeam().getTeamColor());
		canvas.drawCircle(15, 15, 10, paint);
		paint.setColor(getTeam().getClr());
		paint.setTextSize(10);
		canvas.drawText(name, 0, 40, paint);
	}

	private void drawShoot(Canvas canvas, Paint paint) {
		paint.setColor(Color.WHITE);
		canvas.drawArc(new RectF(0, 0, 30, 40), 180, currentShootPower * 9, true, paint);
		paint.setColor(Color.GREEN);
		canvas.drawArc(new RectF(3, 5, 25, 35), 180, currentShootPower * 9, true, paint);

		if (shootThread != null && !shootThread.isAlive() || shootThread == null) {
			shootThread = new Thread() {
				public void run() {
					while (currentShootPower < shootPower) {
						try {
							Thread.sleep(70);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						currentShootPower++;
						Log.e("powerrr ", Integer.toString(currentShootPower));
					}
				}
			};
			shootThread.start();
		}
	}

	private boolean checkifArraysPositionEqualsWithPlayerPos(int length) {
		if (getX() <= moves.get(0).x + length && getX() >= moves.get(0).x - length && getY() <= moves.get(0).y + length && getY() >= moves.get(0).y - length) {
			return true;
		}
		return false;

	}

	/**
     * 
     */
	@Override
	public void run() {
		final int distanse = 2;
		while (true) {
			if (isThreadStoping) {
				isThreadStoping = false;
				break;
			}
			if (isSelected) {
				setHasTheBall(false);
			}
			if (isSelected &&  field.getBall().getRectangle().intersect(getRectangle())
					&& canGetTheBall) {
				setHasTheBall(true);

			}
			if (isHavingTheBall() && isMoving&&getTeam().isMyTeam()) {
				field.getBall().fireBallMovedEvent();
				// fireBallMovedEvent();
			}

			if (playingPosition == allPositions.GK && team.isMyTeam()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if ( team.gePlayerWhoHasTheBall() == null && !field.getBall().isTakingAShoot() && !field.getBall().isTakingAShootByKeeper() && !field.getBall().isGivingPass()) {
					field.getBall().setMoving(false);
				}
			}
		
			if (moves != null) {
				if (!moves.isEmpty() && moves.size() >= 0) {
					
					try{
					
					nextStation.x = moves.get(0).x;
					nextStation.y = moves.get(0).y;
					if (checkifArraysPositionEqualsWithPlayerPos(getWidth())) {
						moves.remove(0);
						this.isMoving = true;
						if (this.hasTheBall) {
							field.getBall().setMoving(true);
						}
					}
					
					}catch(Exception ex){}
					
					
				} else {
					this.isMoving = false;
				}
			}

			try {
				if (playingPosition == allPositions.GK) {
					Thread.sleep(60 - currentSpeed * 2);
				} else {
					Thread.sleep(70 - currentSpeed * 2);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			
			
			context.runOnUiThread(new Runnable() {

				public void run() {// if(isSelected){
					setGoRight(false);
					setGoLeft(false);
					setGoUp(false);
					setGoDown(false);
					if (playingPosition != allPositions.GK) {
						int startXx = getWidth() / 4, startYy = getHeight() / 4;
						int goToyY = startYy, goToxX = startXx;
						if (getX() > nextStation.x && field.getRectangle().contains((int) getX() - distanse, (int) getY()) && team.isMyTeam()) {
							setX(getX() - distanse);
							goToxX = -getWidth() / 4;
							setGoLeft(true);
							isMoving = true;
						} else {
							setGoLeft(false);

						}

						if (getX() < nextStation.x && field.getRectangle().contains((int) getX() + distanse + getWidth()/3, (int) getY()) && team.isMyTeam()) { // go
							isMoving = true; // right
							setX(getX() + distanse);
							goToxX = getWidth() / 2;
							setGoRight(true);
						} else {
							setGoRight(false);

						}

						if (getY() > nextStation.y
								&& field.getRectangle().contains((int) getX(),
										(int) getY() - distanse)
								&& team.isMyTeam()) {// go up
							isMoving = true;
							setY(getY() - distanse);
							setGoUp(true);
							goToyY -= getHeight() / 3;
						} else {
							setGoUp(false);

						}

						if (getY() < nextStation.y
								&& field.getRectangle().contains(
										(int) getX(),
										(int) getY() + distanse + getHeight()
												/ 2 + getHeight() / 7)
								&& team.isMyTeam()) {// go
							// down
							setY(getY() + distanse);
							setGoDown(true);
							isMoving = true;
							goToyY = getHeight() / 2;
						} else {
							setGoDown(false);

						}
						int length = getWidth();
						if ((goToyY == startYy && goToxX == startXx)
								|| (!isGoingUp()
										&& (nextStation.y > getY() - length / 2 && nextStation.y < getY()
												+ length / 3) && (nextStation.x > getX()
										- length / 3 && nextStation.x < getX()
										+ length / 3))) {
							isMoving = false;
						}

						// pixelsToMeters();
						// metersToPixels();
						
					//	if(!getTeam().isMyTeam()){
							
						//	Log.e("goes here","go here");
						//}
						if (hasTheBall&& !field.getBall().getRectangle().intersect(field.getTeamPlaysAwayNets())|| 
								hasTheBall&& !field.getBall().getRectangle().intersect(field.getTeamPlaysHomeNets())) {
							
							
							///katoxi mpalas
							if(!team.isMyTeam()){
								Log.e("Katoxi mpalas stin antipali  omada ","");
								
							}
							field.getBall().setX(getX() + getWidth() / 5);
							field.getBall().setY(getY() + getHeight() / 5);
							// field.getBall().setMoving(true);
						}
						
						
						
						
						
						
						
						
						if (field.getBall().getRectangle()
								.intersect(field.getTeamPlaysAwayNets())
								|| field.getBall()
										.getRectangle()
										.intersect(field.getTeamPlaysHomeNets())) {
							// ama perasei i mpala to terma tote o paixtis
							// xanei
							// tin mpala ( stin periptwsei pou mpei mazi me
							// tin
							// mpala sto terma )
							loosesBall();
						}

					}
					if (isMoving && team.isMyTeam()) {
						movingAlgo();
					}

				}
			});

			if (playingPosition == allPositions.GK) {
				context.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						keepersAlgorythm();
					}
				});
			}

			// refreshLayout();

			if (!hasTheBall) {
				canGetTheBall();
			}

			if (hasTheBall && team.isMyTeam()) {
				context.getShootButton().setOnTouchListener(shootTouchHandler);
				context.getPassButton().setOnTouchListener(passTouchHandler);
			}
			if (isMoving) {
				context.runOnUiThread(new Thread() {
					public void run() {
						field.invalidate();
						invalidate();
					}

				});

			}

		}
	}

	public void canGetTheBall() {
		if (field.getBall().getRectangle().intersect(getRectangle())
				&& canGetTheBall && !getTeam().getHasTheBall()
				&& !field.getBall().isTakingAShoot()
				&& !field.getBall().isTakingAShootByKeeper()) {
			setHasTheBall(true);
			setSelected(true);

			context.getSeekBar().setMax(topSpeed);
		}

	}
	
	public int getCurrentSpeed(){
		return currentSpeed;
	}
	private OnTouchListener shootTouchHandler = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// System.out.println("use shootTouchHandler");
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				isReadyToShoot = true;
				System.out.println("ACTION_DOWN");
				new Thread() {
					public void run() {
						while (currentShootPower < shootPower) {
							try {
								Thread.sleep(70);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							currentShootPower++;
							Log.e("powerrr ",
									Integer.toString(currentShootPower));
						}
					}
				}.start();

			}

			if (event.getAction() == MotionEvent.ACTION_UP) {
				System.out.println("ACTION_UP");
				isReadyToShoot = false;
				if (getTeam().isMyTeam()) {
					if (!getTeam().getHasTheBall()) {
						context.getShootButton().setOnClickListener(null);
						return false;
					}

					if (getTeam().isPlayingHome()) {

						currentShootPower = currentShootPower;
						Player.this.shootAlgorythm(
								field.getLayoutParams().width / 2,
								field.getLayoutParams().height - getHeight()
										/ 2);
					} else {
						currentShootPower = currentShootPower;
						Player.this.shootAlgorythm(
								field.getLayoutParams().width / 2,
								+getHeight() / 2);

					}
					currentShootPower = 0;
				}

			}
			return false;
		}
	};

	private OnTouchListener passTouchHandler = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// System.out.println("view.x ="+ v.getX());

			if (!getTeam().getHasTheBall()) {
				// context.getPassButton().setOnTouchListener(null);
				new Thread() {
					public void run() {
						try {
							Thread.sleep(500);
							context.getPassButton().defaultJoysticLocation();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				}.start();

				return false;
			}

		context.getPassButton().setPressPoint(
					new Point(context.getPassButton().getWidth() / 2, context
							.getPassButton().getHeight() / 2));
			context.getPassButton().setJoysticLocation(event.getX(),
					event.getY());
			// if(context.getPassButton().getPressPoint()==null){
		//	context.getPassButton().setPressPoint(
			//		new Point(context.getPassButton().getWidth() / 2, context
				//			.getPassButton().getHeight() / 2));
			// }else{
			double posX = 0, posY = 0;

			posX =  event.getX()-context.getPassButton().getWidth()/2;
			posY =  event.getY()-context.getPassButton().getHeight()/2;
		
			
			
			
			Player.this.passAlgorythm(posX, posY);
			// context.getPassButton().defaultJoysticLocation();
			// context.getPassButton().setPressPoint(new
			// Point(context.getPassButton().getWidth()/2,context.getPassButton().getHeight()/2));
			// }

			return false;
		}

	};

	private OnClickListener shootHandler = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (getTeam().isMyTeam()) {
				if (!getTeam().getHasTheBall()) {
					context.getShootButton().setOnClickListener(null);
					return;
				}

				if (getTeam().isPlayingHome()) {

					currentShootPower = Player.this.shootPower;
					Player.this.shootAlgorythm(
							field.getLayoutParams().width / 2,
							field.getLayoutParams().height);
				} else {
					currentShootPower = Player.this.shootPower;
					Player.this.shootAlgorythm(
							field.getLayoutParams().width / 2, 0);

				}
			}
		}
	};

	/*
	 * 
	 * private OnClickListener passHandler = new OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { if (getTeam().isMyTeam()) { if
	 * (!getTeam().getHasTheBall()) {
	 * context.getPassButton().setOnClickListener(null); return; } double
	 * posX=0; double posY=0; if (isGoingRight()) {
	 * posX=context.getFrameLayout().getWidth(); } else if (isGoingLeft()){
	 * posX=0; }else if (!isGoingLeft()&&!isGoingRight()){ posX=getX(); }
	 * 
	 * if (isGoingDown()) { posY=context.getFrameLayout().getHeight(); } else if
	 * (isGoingUp()){ posY=0; }else if (!isGoingDown()&&!isGoingUp()){
	 * posY=getY(); }
	 * 
	 * 
	 * passAlgorythm(posX,posY);
	 * 
	 * } } };
	 */

	public void keepersAlgorythm() {
		if (getTeam().isPlayingHome()) { // ama einai sto panw terma o keeper
			if (!getRectangle().intersect(field.getTeamPlaysHomeNets())) {
				// an den einai sto terma na paei
				if (getY() > field.getTeamPlaysHomeNets().bottom - 10) {
					setY(getY() - 1);
				} else {
					setY(getY() + 1);
				}
				if (getX() < field.getTeamPlaysHomeNets().centerX()) {
					setX(getX() + 1);
				} else if (getX() > field.getTeamPlaysHomeNets().centerX()) {
					setX(getX() - 1);
				}
			} else {
				// an einai sto terma na akolouthei tin mpala
				if (getX() < field.getBall().getX()) {
					setX(getX() + 2);
				} else {
					setX(getX() - 2);
				}

				if (getRectangle().intersect(field.getBall().getRectangle())) {
					field.getBall().shootKeeper(this);
					loosesBall();
				}
			}

		} else { // ama elengxei to katw terma

			if (!getRectangle().intersect(field.getTeamPlaysAwayNets())) {
				// an den einai sto terma na paei
				if (getY() > field.getTeamPlaysAwayNets().top - 20) {
					setY(getY() - 1);
				} else {
					setY(getY() + 1);
				}
				if (getX() < field.getTeamPlaysAwayNets().centerX()) {
					setX(getX() + 1);
				} else if (getX() > field.getTeamPlaysAwayNets().centerX()) {
					setX(getX() - 1);
				}
			} else {
				// an einai sto terma na akolouthei tin mpala
				if (getX() < field.getBall().getX()) {
					setX(getX() + 2);
					// if(!field.getTeamPlaysAwayNets().intersect(getRectangle())){
					// // ama vgei apo to terma paei pisw , to xrysimopoiw mono
					// sto katw terma
					// setX(getX()-2);
					// }
				} else {
					setX(getX() - 2);
					// if(!field.getTeamPlaysAwayNets().intersect(getRectangle())){
					// // ama vgei apo to terma paei pisw , to xrysimopoiw mono
					// sto katw terma
					// setX(getX()+2);
					// }
				}

				if (getRectangle().intersect(field.getBall().getRectangle())) {
					field.getBall().shootKeeper(this);
					loosesBall();
				}
			}

		}

	}

	public void loosesBall() {
		new Thread() {

			public void run() {
				Player p = null;
				try {
					if (getTeam().gePlayerWhoHasTheBall() != null) {
						if (getTeam().gePlayerWhoHasTheBall() != null) {
							p = getTeam().gePlayerWhoHasTheBall();
							p.setHasTheBall(false);
							p.canGetTheBall = false;
							// field.getBall().setMoving(false);
						}
					}
					Thread.sleep(500);
					if (p != null) {
						p.canGetTheBall = true;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
     * 	
     */
	public void startThread() {
		thread.start();
	}

	

	/**
     * 
     */
	public void pauseThread() {
		thread.suspend();
	}

	/**
     * 
     */
	public void resumeThread() {
		thread.resume();
	}

	/**
	 * 
	 * @return
	 */
	public int getPlayerWidth() {
		return totalWidth;
	}

	/**
	 * 
	 * @param width
	 */
	public void setWidth(int width) {
		this.totalWidth = width;
	}

	/**
	 * 
	 * @return
	 */
	public int getPlayerHeight() {
		return totalHeight;
	}

	/**
	 * 
	 * @param height
	 */
	public void setHeight(int height) {
		this.totalHeight = height;
	}

	/**
     * 
     */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * only one player for each team can be selected
	 */
	public void setSelected(boolean isSelected) {
		if (!isSelected || playingPosition == allPositions.GK) {
			return;
		}

		for (Player player : (Player[]) getTeam().getTeamPlayers()) {
			if (player!=null ) {
				player.isSelected = false;
				player.postInvalidate();
			}

		}

		this.isSelected = isSelected;
		this.postInvalidate();
	}

	/**
	 * 
	 * @author gaitanesnikos
	 * 
	 */
	public static enum allPositions {

		GK, CB1, CB2, CB, CR, CL, MR, ML, CM, MC1, MC2, CF, CF1, CF2, SS
	}

	/**
	 * 
	 * @return
	 */
	public Rect getRectangle() {
		return new Rect((int) getX(), (int) getY(), (int) getX() + getWidth()
				- 30, (int) getY() + getHeight() - 40);
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @param team
	 */
	public void setTeam(Team team) {
		this.team = team;
	}

	/**
	 * 
	 * @return
	 */
	public int getTotalSpeed() {
		return topSpeed;
	}

	/**
	 * 
	 * @param currentSpeed
	 */
	public void setCurrentSpeed(int currentSpeed) {
		if (currentSpeed < 7) {
			currentSpeed = 7;
		}
		if (currentSpeed > 20) {
			currentSpeed = 20;
		}
		this.currentSpeed = currentSpeed;
		

	}

	/**
	 * 
	 * @return
	 */
	public int getCurrentShootPower() {
		return currentShootPower;
	}

	/**
	 * 
	 * @param shootPower
	 */
	public void setCurrentShootPowerShootPower(int currentShootPower) {
		this.currentShootPower = currentShootPower;
	}

	/**
	 * 
	 * @return
	 */
	public Point getPoint() {
		return nextStation;
	}

	/**
	 * 
	 * @param point
	 */
	public void setPoint(Point point) {
		this.nextStation = point;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isHavingTheBall() {
		return hasTheBall;
	}

	/**
	 * 
	 * @return
	 */
	// private int getSeekValue() {
	// if (seekBar.getProgress() > 7) {
	// return seekBar.getProgress();
	// } else {
	// return 7;
	// }
	// }

	public void addPlayerPoints(int x, int y) {
		Point p = new Point();
		p.x = x;
		p.y = y;
		moves.add(p);
	}

	/**
	 * only one player of all the teams has the ball
	 * 
	 * @param hasTheBall
	 */
	public void setHasTheBall(boolean hasTheBall) {

		//if (!team.isMyTeam()) {
			//return;

		//}

		for (Player player : (Player[]) field.getTeam1().getTeamPlayers()) {
			if ( hasTheBall) {
				player.hasTheBall = false;
			}
		}
		for (Player player : (Player[]) field.getTeam2().getTeamPlayers()) {
			if (hasTheBall) {
				player.hasTheBall = false;
			}
		}
		if (hasTheBall) {
			field.getBall().setControlledByPlayer(true);
			field.getBall().setPlayerHasBall(this);
			this.field.getBall().setMoving(true);
		} else {
			field.getBall().setControlledByPlayer(false);
		}

		
		this.hasTheBall = hasTheBall;
	}

	public allPositions getPosition() {
		return playingPosition;
	}

	public void setPosition(allPositions position) {
		this.playingPosition = position;
	}

	public boolean isReadyToGivePass() {
		return isReadyToGivePass;
	}

	public void setReadyToGivePass(boolean isReadyToGivePass) {
		if (isReadyToGivePass) {
			for (Player p : (Player[]) getTeam().getTeamPlayers()) {

				p.isReadyToGivePass = false;
			}
			iswaitingForPass = true;
		}

		this.isReadyToGivePass = isReadyToGivePass;
	}

	public void setMoves(ArrayList<Point> moves) {
		this.moves = moves;
	}

	public Point[] getMoves() {
		return moves.toArray(new Point[moves.size()]);
	}

	private Rect getLiteRectangle() {

		return new Rect(5, 5, 30, 30);
	}// left index: 1 type: int
		// top index: 2 type: int

	// right index: 3 type: int
	// local: bottom index: 4 type: int

	public void setIswaitingForPass(boolean iswaitingForPass) {
		if (!iswaitingForPass) {

			for (Player p : (Player[]) getTeam().getTeamPlayers()) {
				if (p.isReadyToGivePass) {
					p.isReadyToGivePass = false;
				}
			}
		}
		Player.iswaitingForPass = iswaitingForPass;
	}

	public allPositions getPlayingPosition() {
		return playingPosition;
	}

	public void setPlayingPosition(allPositions playingPosition) {
		this.playingPosition = playingPosition;
	}

	public void addPlayerSelectionListener() {
	}

	public void removePlayerSelectionListener() {
	}

	public void addPlayerMovedListener() {
	}

	public void removePlayerMovedListener() {
	}

	private void firePlayerSelectionEvent(boolean isPlayerSelected) {
		// System.out.println("selected: " + isPlayerSelected + ", " +
		// this.getName());
	}

	private void firePlayerMovedEvent(double x, double y) {
		// System.out.println("moved at posoition: " + x + ", " + y);
	}

	public String getID() {
		return getTeam().getName() + "#" + getName();
	}

	public void setX(float x) {
		try {
			setPosX(x);
			context.runOnUiThread(new Thread() {
				public void run() {
					layout((int) posX, (int) posY, (int) posX + getWidth(),
							(int) posY + getHeight());
				}

			});

		} catch (Exception e) {
			Log.e(e.getMessage(), e.getMessage());

		}

	
			field.repaint();
			// repaint();
		

	}

	public void setY(float y) {
		setPosY(y);
		try {

			context.runOnUiThread(new Thread() {
				public void run() {
					layout((int) posX, (int) posY, (int) posX + getWidth(),
							(int) posY + getHeight());
					
						field.repaint();
						// repaint();
					
				}

			});

		} catch (Exception e) {
			Log.e(e.getMessage(), e.getMessage());

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

	public void setGoUp(boolean goUp) {
		if (goUp) {

			setGoDown(false);

		} else {

		}
		this.goUp = goUp;
	}

	public void setGoDown(boolean goDown) {
		if (goDown) {

			setGoUp(false);
			// isMoving=true;
		} else {
			// isMoving=false;
		}
		this.goDown = goDown;
	}

	public void setGoRight(boolean goRight) {
		if (goRight) {
			setGoLeft(false);

		} else {

		}
		this.goRight = goRight;
	}

	public void setGoLeft(boolean goLeft) {
		if (goLeft) {

			setGoRight(false);

		} else {

		}
		this.goLeft = goLeft;
	}

	public boolean isGoingUp() {

		return goUp;
	}

	public boolean isGoingDown() {

		return goDown;
	}

	public boolean isGoingRight() {

		return goRight;
	}

	public boolean isGoingLeft() {

		return goLeft;
	}

	public void movingAlgo() {
		try {
			if (st == null || rt==null) {
				return;
			}

			firePlayerMovedEvent();

			pixelsToMeters();

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	private void firePlayerMovedEvent() {
		if (isConnectedToServer && Player.this.team.isMyTeam()) {
			// \Log.e("KKKO", "SENDING!!!!!!!!!!!!!!!!!!!"+ getID() +
			// "_positionX = " +( yLocationInMeters)+ "_positionY = " +
			// (SoccerField.SOCCER_FIELD_X_WIDTH_IN_M - xLocationInMeters-5));
			st.send("player :"
					+ getID()
					+ "_selected = "
					+ isSelected
					+ "_positionX = "
					+ (yLocationInMeters)
					+ "_positionY = "
					+ (SoccerField.SOCCER_FIELD_X_WIDTH_IN_M
							- xLocationInMeters - 5));
		}
	}

	public void repaint() {
		context.runOnUiThread(new Thread() {
			public void run() {
				invalidate();
				postInvalidate();
			}
		});
	}
}
