package com.epp.soccer;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

public class SoccerField extends ViewGroup implements Runnable {
	private Team team1;
	private Team team2;
	private Rect teamPlaysHomeNets;
	private Rect teamPlaysAwayNets;
	private Paint paint;
	private Rect teamPlayesHomeArea, teamPlayesHomeSmallArea;
	private Rect teamPlayesAwayArea, teamPlayesAwaySmallArea;
	private Ball ball;
	private Referee referee;
	private SoccerActivity soccerActivity;
	private Thread viewPortThread;

	private float posX, posY;
	private boolean firstTime = true;
	private float seekbarPosX, seekbarPosY;
	public static int realDistanceX=700 , realDistanceY =1300;
	public static boolean isKickOff = false;
	public static final int SOCCER_FIELD_Y_HEIGHT_IN_M = 120, SOCCER_FIELD_X_WIDTH_IN_M = 90;

	
	public SoccerField(SoccerActivity soccerActivity) {
		super(soccerActivity);
		this.soccerActivity = soccerActivity;
		teamPlaysHomeNets = new Rect(getWidth() / 2 - getWidth() / 10, 0, getWidth() / 2 + getWidth() / 10, getHeight() / 30);
		teamPlaysAwayNets = new Rect(getWidth() / 2 - getWidth() / 10, getHeight() - getHeight() / 30, getWidth() / 2 + getWidth() / 10, getHeight());
		team1 = new Team(soccerActivity, this, true, false, "b");
		team2 = new Team(soccerActivity, this, false, true, "a");
		realDistanceX=(int)(soccerActivity.getWindow().getWindowManager().getDefaultDisplay().getWidth()*1.4);
		realDistanceY=(int)(soccerActivity.getWindow().getWindowManager().getDefaultDisplay().getHeight()*1.4);
		
		team1.setHaveKickOffBall(true);
		team2.setHaveKickOffBall(false);
		ball = new Ball(soccerActivity, this);
		referee = new Referee(ball, this, soccerActivity);
		referee.start();
		viewPortThread = new Thread(this);

		
		
		startCamera();
		ball.start();
		setWillNotDraw(false);

		addListener();
		setLayoutParams(new LayoutParams(realDistanceX, realDistanceY)); 
	}

//	public static final double maxMetersY = SOCCER_FIELD_Y_HEIGHT_IN_M, maxMetersX = SOCCER_FIELD_X_WIDTH_IN_M + (int) ((int) SOCCER_FIELD_X_WIDTH_IN_M / 4);

	public void startCamera() {
		viewPortThread.start();
	}
	
	/**
     * 
     */
	@Override
	public void run() {// metakinisi ' cameras '
		//if (viewPortThread.isAlive()) {
		//	return;
		//}
		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			soccerActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (soccerActivity.getFrameLayout().getWidth() / 2 > getX() + ball.getX() && getX() + 1 <= 10) {
						setX(getX() + 1);
						} else if (soccerActivity.getFrameLayout().getWidth() / 2 < getX() + ball.getX() && getX() - 1 >= soccerActivity.getFrameLayout().getWidth() - getWidth() - 10) {
						setX(getX() - 1);
					}
					if (soccerActivity.getFrameLayout().getHeight() / 2 > getY() + ball.getY() && getY() + 1 <= 10) {
						setY(getY() + 1);
																											// akoma kai an den allazei i camera tha metakiniotane kai tha vriskotan ektos wriwn me sinepeia 
																											// na min emfanizete
					} else if (soccerActivity.getFrameLayout().getHeight() / 2 < getY() + ball.getY()
							&& getY() - 1 >= soccerActivity.getFrameLayout().getHeight() - getHeight() - 10) {
						setY(getY() - 1);
																											// camera tha metakiniotane kai tha vriskotan ektos wriwn me sinepeia na min emfanizete
					}
				}
			});
		}
	}

	/**
     * 
     */
	private void addListener() {
		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Team userTeam;
				if (team1.isMyTeam()) {
					userTeam = team1;
				} else {
					userTeam = team2;
				}
				Player userPlayer = userTeam.getSelectedPlayer();
				if (userPlayer == null) {
					return false;
				}
				if (!userPlayer.isReadyToGivePass()) {// an exei patithei gia na dosw pasa tote akyrwnete i entoli
					if (!userPlayer.getTeam().isMyTeam()) {
						return false;
					}
					if (!userPlayer.getTeam().getHasTheBall() && userPlayer.getPlayingPosition() != Player.allPositions.GK) {

						
						Log.e("Selected player ", ".."+userPlayer.getID());
						
						
						userPlayer.setSelected(true);
					}
//					userPlayer.getMoves().removeAll(userPlayer.getMoves());
					userPlayer.setMoves(new ArrayList<Point>());
					userPlayer.addPlayerPoints((int) event.getX() - userPlayer.getWidth() / 3, (int) event.getY() - userPlayer.getHeight() / 3);
				}
				return false;
			}
		});

		

	}

	/**
     * 
     */
	public void generateTeams() {
		team2.addPlayerToTeam("01", 20, 20, 20);
		team2.addPlayerToTeam("02", 20, 20, 20);
		team2.addPlayerToTeam("03", 20, 20, 20);
		team2.addPlayerToTeam("04", 20, 20, 20);
		team2.addPlayerToTeam("05", 20, 20, 20);
		team2.addPlayerToTeam("06", 20, 20, 20);
		team2.addPlayerToTeam("07", 20, 20, 20);
		team2.addPlayerToTeam("08", 20, 20, 20);
		team2.addPlayerToTeam("09", 20, 20, 20);
		team2.addPlayerToTeam("10", 20, 20, 20);
		team2.addPlayerToTeam("11", 20, 20, 20);

		team1.addPlayerToTeam("01", 20, 20, 20);
		team1.addPlayerToTeam("02", 20, 20, 20);
		team1.addPlayerToTeam("03", 20, 20, 20);
		team1.addPlayerToTeam("04", 20, 20, 20);
		team1.addPlayerToTeam("05", 20, 20, 20);
		team1.addPlayerToTeam("06", 20, 20, 20);
		team1.addPlayerToTeam("07", 20, 20, 20);
		team1.addPlayerToTeam("08", 20, 20, 20);
		team1.addPlayerToTeam("09", 20, 20, 20);
		team1.addPlayerToTeam("10", 20, 20, 20);
		team1.addPlayerToTeam("11", 20, 20, 20);
	}

	/**
     * 
     */
	public void addplayersAndBallIntoField() {
		for (Player player : (Player[]) team1.getTeamPlayers()) {
			addView(player);
			player.startThread();
		}
		for (Player player : (Player[]) team2.getTeamPlayers()) {
			addView(player);
			player.startThread();
		}
		addView(ball);
	}

	/**
 * 
 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (!firstTime) {
			return;
		}

		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (child.getVisibility() == GONE) {
				continue;
			} else if (child instanceof Player) {
				Player player = (Player) child;
				player.layout((int) player.getX(), (int) player.getY(), (int) player.getX() + 60, (int) player.getY() + 60);
				player.generateStartPositions();

			} else if (child instanceof SeekBar) {
				child.layout(0, b - 10, soccerActivity.getFrameLayout().getWidth(), b);

			} else if (child instanceof Ball) {
				Ball ball = (Ball) child;
				ball.layout((int) ball.getX(), (int) ball.getY(), (int) ball.getX() + 10, (int) ball.getY() + 10);

				ball.moveToRelativeLocationInMeters(SOCCER_FIELD_X_WIDTH_IN_M / 2, SOCCER_FIELD_Y_HEIGHT_IN_M / 2);
			//	ball.moveToRelativeLocationInMeters(SOCCER_FIELD_X_WIDTH_IN_M -4, SOCCER_FIELD_Y_HEIGHT_IN_M -4);
			}
		}
		if (firstTime) {
			firstTime = false;
		}
	}

	/**
     * 
     */
	@SuppressLint({ "DrawAllocation", "DrawAllocation", "DrawAllocation", "DrawAllocation" })
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		teamPlaysHomeNets = new Rect(getWidth() / 2 - getWidth() / 10, 0, getWidth() / 2 + getWidth() / 10, getHeight() / 30);
		teamPlaysAwayNets = new Rect(getWidth() / 2 - getWidth() / 10, getHeight() - getHeight() / 30, getWidth() / 2 + getWidth() / 10, getHeight());
		teamPlayesHomeArea = new Rect(getWidth() / 4, getHeight() / 100, getWidth() / 2 + getWidth() / 4 + getWidth() / 25, getHeight() / 5);
		teamPlayesAwayArea = new Rect(getWidth() / 4, getHeight() - getHeight() / 5, getWidth() / 2 + getWidth() / 4 + getWidth() / 25, getHeight());

		int standarDistWidth = getWidth() / 14;
		int standarDistHeight = getWidth() / 7;
		teamPlayesHomeSmallArea = new Rect(getWidth() / 4 + standarDistWidth, getHeight() / 100, getWidth() / 2 + getWidth() / 4 + getWidth() / 25 - standarDistWidth, getHeight() / 5 - standarDistHeight);
		teamPlayesAwaySmallArea = new Rect(getWidth() / 4 + standarDistWidth, getHeight() - getHeight() / 5 + standarDistHeight, getWidth() / 2 + getWidth() / 4 + getWidth() / 25 - standarDistWidth, getHeight());

		paint = new Paint();
		// getWidth() / 5, 0, getWidth() / 3+getWidth() / 2, getHeight() / 4

		paint.setColor(Color.GREEN);
		canvas.drawPaint(paint);
		paint.setColor(Color.BLACK);

		// zografizw to imikiklio
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 10,
				paint);
		paint.setColor(Color.GREEN);
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 10 - 3,
				paint);
		paint.setColor(Color.BLACK);

		// zografizw tin mesea grami
		canvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2, paint);

		// zografizw toys kuklous stin perioxi
		int radius = Math.min(getWidth() / 10, getHeight() / 10);
		canvas.drawCircle(getWidth() / 2, teamPlayesHomeArea.bottom, radius,
				paint);
		canvas.drawCircle(getWidth() / 2, teamPlayesAwayArea.top, radius, paint);

		paint.setColor(Color.GREEN);
		canvas.drawCircle(getWidth() / 2, teamPlayesHomeArea.bottom,
				radius - 2, paint);
		canvas.drawCircle(getWidth() / 2, teamPlayesAwayArea.top, radius - 2,
				paint);

		// zografizw megales perioxes
		paint.setColor(Color.BLACK);
		canvas.drawRect(teamPlayesAwayArea, paint);
		canvas.drawRect(teamPlayesHomeArea, paint);
		paint.setColor(Color.GREEN);
		canvas.drawRect(teamPlayesHomeArea.left + 2, teamPlayesHomeArea.top,
				teamPlayesHomeArea.right - 2, teamPlayesHomeArea.bottom - 2,
				paint);
		canvas.drawRect(teamPlayesAwayArea.left + 2,
				teamPlayesAwayArea.top + 2, teamPlayesAwayArea.right - 2,
				teamPlayesAwayArea.bottom, paint);

		// zwgrafizw mikres perioxes
		paint.setColor(Color.BLACK);
		canvas.drawRect(teamPlayesAwaySmallArea, paint);
		canvas.drawRect(teamPlayesHomeSmallArea, paint);
		paint.setColor(Color.GREEN);
		canvas.drawRect(teamPlayesHomeSmallArea.left + 2,
				teamPlayesHomeSmallArea.top, teamPlayesHomeSmallArea.right - 2,
				teamPlayesHomeSmallArea.bottom - 2, paint);
		canvas.drawRect(teamPlayesAwaySmallArea.left + 2,
				teamPlayesAwaySmallArea.top + 2,
				teamPlayesAwaySmallArea.right - 2,
				teamPlayesAwaySmallArea.bottom, paint);

		// zografizw ta termata
		paint.setColor(Color.BLACK);
		canvas.drawRect(teamPlaysHomeNets, paint);
		canvas.drawRect(teamPlaysAwayNets, paint);

		// zografizw to scor me ta xromata tis kathe omadas
		if (team1.isMyTeam()) {
			paint.setColor(team1.getTeamColor());
			canvas.drawText(Integer.toString(team1.getScore()), getWidth() / 2,
					getHeight() / 2 - getHeight() / 10, paint);
			canvas.drawText(Integer.toString(team1.getFansNumber()) + " FANS ",
					getWidth() / 2, getHeight() / 2 - getHeight() / 10 + 20,
					paint);
			paint.setColor(team2.getTeamColor());
			canvas.drawText(Integer.toString(team2.getScore()), getWidth() / 2,
					getHeight() / 2 + getHeight() / 10, paint);
			canvas.drawText(Integer.toString(team2.getFansNumber()) + " FANS ",
					getWidth() / 2, getHeight() / 2 + getHeight() / 10 - 20,
					paint);
		} else {
			paint.setColor(team2.getTeamColor());
			canvas.drawText(Integer.toString(team2.getScore()), getWidth() / 2,
					getHeight() / 2 - getHeight() / 10, paint);
			canvas.drawText(Integer.toString(team2.getFansNumber()) + " FANS ",
					getWidth() / 2, getHeight() / 2 - getHeight() / 10 + 20,
					paint);
			paint.setColor(team1.getTeamColor());
			canvas.drawText(Integer.toString(team1.getScore()), getWidth() / 2,
					getHeight() / 2 + getHeight() / 10, paint);
			canvas.drawText(Integer.toString(team1.getFansNumber()) + " FANS ",
					getWidth() / 2, getHeight() / 2 + getHeight() / 10 - 20,
					paint);
		}

		Team userTeam;
		if (team1.isMyTeam()) {
			userTeam = team1;
		} else {
			userTeam = team2;
		}

		// zograizw tin poreia tis passas
		try {

			for (Player p : (Player[]) userTeam.getTeamPlayers()) {
				if (p.isReadyToGivePass()) {
					paint.setColor(Color.MAGENTA);
					canvas.drawLine((int) (p.getX() + p.getWidth() / 3), (int) (p.getY() + p.getHeight() / 3), (int) (p.getCurrentPointTouchPos().x + p.getX()), (int) (p.getCurrentPointTouchPos().y + p.getY()), paint);
				}
			}

			// zograizw tin poreia tou paixti
			for (Player p : (Player[]) userTeam.getTeamPlayers()) {
				if (!(p.getMoves().length==0)) {
					Point previousPoint = null;
					for (Point point : p.getMoves()) {
						if (previousPoint == null) {
							previousPoint = point;
						}
						canvas.drawLine(previousPoint.x, previousPoint.y, point.x, point.y, paint);
						previousPoint = point;
					}
				}
			}
		} catch (ConcurrentModificationException e) {
			e.printStackTrace();
		}
	}

	private void updateSeekBar(float x, float y) {
		// seekBar.setX(x);
		// seekBar.setY(y);

	}



	public void stopThread() {
		try {
			viewPortThread.stop();
		} catch (Exception e) {
		}
	}

	/**
	 * 
	 * @return
	 */
	public Rect getRectangle() {
		return new Rect(0, 0, getWidth(), getHeight());
	}

	/**
	 * 
	 * @return
	 */
	public Rect getTeamPlaysHomeNets() {
		return teamPlaysHomeNets;
	}

	/**
	 * 
	 * @param teamPlaysHomeNets
	 */
	public void setTeamPlaysHomeNets(Rect teamPlaysHomeNets) {
		this.teamPlaysHomeNets = teamPlaysHomeNets;
	}

	/**
	 * 
	 * @return
	 */
	public Rect getTeamPlaysAwayNets() {
		return teamPlaysAwayNets;
	}

	/**
	 * 
	 * @param teamPlaysAwayNets
	 */
	public void setTeamPlaysAwayNets(Rect teamPlaysAwayNets) {
		this.teamPlaysAwayNets = teamPlaysAwayNets;
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

	public Referee getReferee() {
		return referee;
	}

	public void setReferee(Referee referee) {
		this.referee = referee;
	}


	public Player getPlayer(String playerId) {

		for (int i = 0; i < getChildCount(); i++) {
			if (getChildAt(i) instanceof Player
					&& ((Player) getChildAt(i)).getID().equals(playerId)) {
				return ((Player) getChildAt(i));
			}
		}
		return null;
	}

	public void movePlayer(String playerId, double x, double y) {
		getPlayer(playerId).moveToRelativeLocationInMeters(x, y);

	}

	public void moveBall(double x, double y) {
		getBall().moveToRelativeLocationInMeters(x, y);
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

	public void setX(float x) {
		setPosX(x);
		layout((int) posX, (int) posY, (int) posX + getWidth(), (int) posY
				+ getHeight());

	}

	public void setY(float y) {
		setPosY(y);
		layout((int) posX, (int) posY, (int) posX + getWidth(), (int) posY
				+ getHeight());

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

	public void repaint() {
		soccerActivity.runOnUiThread(new Thread() {
			public void run() {

				postInvalidate();
				invalidate();
			}
		});
	}

}
