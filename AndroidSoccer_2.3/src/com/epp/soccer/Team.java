package com.epp.soccer;

import android.graphics.Color;

public class Team {
	private int fansNumber; // oi fan poy parakolouthoun to game
	private SoccerActivity contex;
	private Player[] teamPlayers;
	private Player[] subtitutionPlayers;
	private boolean isMyTeam, isPlayingHome;
	private SoccerField field;
	private int score = 0;
	private int countPlayers;
	private int clr;
	private int teamColor;
	private String name;
	private  boolean haveKickOffBall=true;
	
	public Team(SoccerActivity contex, SoccerField field, boolean isMyTeam, boolean isPlayingHome, String name) {
		this.contex = contex;
		this.field = field;
		this.isMyTeam = isMyTeam;
		this.name = name;
		this.isPlayingHome = isPlayingHome;
		if (isMyTeam) {
			clr = Color.MAGENTA;
			teamColor = Color.BLUE;
		} else {
			clr = Color.RED;
			teamColor = Color.GRAY;
		}
		teamPlayers = new Player[11];
		subtitutionPlayers = new Player[7];
	}

	private void addPlayer(Player player){
		for(int i=0; i<teamPlayers.length; i++){
			if(teamPlayers[i] == null){
				teamPlayers[i] = player;
				break;
			}
		}
	}
	
	private void addSubstitutePlayer(Player player){
		for(int i=0; i<subtitutionPlayers.length; i++){
			if(subtitutionPlayers[i] == null){
				subtitutionPlayers[i] = player;
				break;
			}
		}
	}
	
	public void addPlayerToTeam(String name, int acceleration, int speed, int shootPower) {
		Player player = new Player(contex, field, this, name, acceleration, speed, shootPower);
		if (countPlayers < 11) {
			chooseEachPlayerPosition(player, countPlayers++, "4-4-2");
			addPlayer(player);
			player.setSelected(true);
		} else {
			addSubstitutePlayer(player);
		}
	}

	private void chooseEachPlayerPosition(Player player, int elevenCounter,
			String formation) {
		if (formation.equals("4-4-2")) {
			if (elevenCounter == 0) {
				setPosition(Player.allPositions.GK, player);
			}
			if (elevenCounter == 1) {
				setPosition(Player.allPositions.CB1, player);
			}
			if (elevenCounter == 2) {
				setPosition(Player.allPositions.CB2, player);
			}
			if (elevenCounter == 3) {
				setPosition(Player.allPositions.CL, player);
			}
			if (elevenCounter == 4) {
				setPosition(Player.allPositions.CR, player);
			}
			if (elevenCounter == 5) {
				setPosition(Player.allPositions.ML, player);
			}
			if (elevenCounter == 6) {
				setPosition(Player.allPositions.CM, player);
			}
			if (elevenCounter == 7) {
				setPosition(Player.allPositions.MR, player);
			}
			if (elevenCounter == 8) {
				setPosition(Player.allPositions.CF1, player);
			}
			if (elevenCounter == 9) {
				setPosition(Player.allPositions.SS, player);
			}

			if (elevenCounter == 10) {
				setPosition(Player.allPositions.CF2, player);
			}
		}
	}

	public void setPosition(Player.allPositions pos, Player player) {
		player.setPosition(pos);
		if (pos != Player.allPositions.GK) {
			player.setCurrentSpeed(Player.startingSpeed);
		}
	}
	
	public int getTeamColor() {
		return teamColor;
	}

	public void setTeamColor(int teamColor) {
		this.teamColor = teamColor;
	}

	public Player[] getTeamPlayers() {
		return teamPlayers;
	}

	public boolean isMyTeam() {
		return isMyTeam;
	}

	public void setMyTeam(boolean isMyTeam) {
		this.isMyTeam = isMyTeam;
	}

	public boolean isPlayingHome() {
		return isPlayingHome;
	}

	public void setPlayingHome(boolean isPlayingHome) {
		this.isPlayingHome = isPlayingHome;
	}

	public Player gePlayerWhoHasTheBall() {
		Player player = null;
		for (Player p : teamPlayers) {
			if (p.isHavingTheBall()) {
				player = p;
			}
		}
		return player;
	}

	public boolean getHasTheBall() {
		for (Player p : teamPlayers) {
			if (p.isHavingTheBall()) {
				return true;
			}
		}
		return false;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public boolean isHaveKickOffBall() {
		return haveKickOffBall;
	}

	public void setHaveKickOffBall(boolean haveKickOffBall) {
		this.haveKickOffBall = haveKickOffBall;
	}

	public int getClr() {
		return clr;
	}

	public Player getSelectedPlayer() {
		Player player = null;
		for (Player p : teamPlayers) {
			if (p.isSelected()) {
				player = p;
			}
		}
		return player;
	}

	public void setClr(int clr) {
		this.clr = clr;
	}

	public String getName() {
		return name;
	}

	public void setFansNumber(int fansNumber) {
		this.fansNumber = fansNumber;
	}

	public int getFansNumber() {
		return this.fansNumber;
	}

	public void notCotrolledByTeam() {
		for (Player player : teamPlayers) {
			player.setHasTheBall(false);
		}
	}
}
