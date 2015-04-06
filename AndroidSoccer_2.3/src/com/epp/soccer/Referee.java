package com.epp.soccer;

import java.util.ArrayList;

import android.graphics.Point;


public class Referee extends Thread {
	private Ball ball;
	private SoccerField field;
	private SoccerActivity context;
	private int time=0;
	
	public Referee(Ball ball, SoccerField field, SoccerActivity context) {
		this.ball = ball;
		this.field = field;
		this.context = context;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			time ++;
			if (ball.getRectangle().intersect(field.getTeamPlaysAwayNets())) {
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {		
                        Ball.isGoal=true;
                        if(time >100){
	                        // rithmizw to anagrafon score
							if (field.getTeam1().isPlayingHome()) {
								field.getTeam1().setScore(field.getTeam1().getScore() + 1);
							} else {
								field.getTeam2().setScore(field.getTeam2().getScore() + 1);
							}
						}
						field.getBall().setX(field.getWidth() / 2);
						field.getBall().setY(field.getHeight() / 2);
						removePlayerMoves();
						startingPlayersPositions();
					}
				});
				try {
					sleep(1500);
					Ball.isGoal=false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// an mpei sto panw terma
			if (ball.getRectangle().intersect(field.getTeamPlaysHomeNets())) {
				ball.setIsGoal(true);
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						 if(time >100){
						if (!field.getTeam1().isPlayingHome()) {
							field.getTeam1().setScore(field.getTeam1().getScore() + 1);
							field.getTeam2().setHaveKickOffBall(true);
							field.getTeam1().setHaveKickOffBall(false);
						} else {
							field.getTeam2().setScore(field.getTeam2().getScore() + 1);
							
							field.getTeam1().setHaveKickOffBall(true);
							field.getTeam2().setHaveKickOffBall(false);
						}
						}
						field.getBall().setX(field.getWidth() / 2);
						field.getBall().setY(field.getHeight() / 2);
						removePlayerMoves();	
						startingPlayersPositions();
					}
				});
				try {
					Thread.sleep(200);
					ball.setIsGoal(false);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public void removePlayerMoves(){
		for(Player p:(Player[])field.getTeam1().getTeamPlayers()){
//			p.getMoves().removeAll(p.getMoves());
			p.setMoves(new ArrayList<Point>());
			
		}
		for(Player p:(Player[])field.getTeam2().getTeamPlayers()){
			p.setMoves(new ArrayList<Point>());
		}
	}
	
	public void startingPlayersPositions(){
		for(Player p:(Player[])field.getTeam1().getTeamPlayers()){
			p.generateStartPositions();
		}
		for(Player p:(Player[])field.getTeam2().getTeamPlayers()){
			p.generateStartPositions();
		}
	}
}
