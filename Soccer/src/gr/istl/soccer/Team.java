package gr.istl.soccer;

import java.awt.Color;
import java.util.ArrayList;

/**
 *
 * @author gaitanesnikos
 */
public class Team {

    public static final int TEAM_1 = 1;
    public static final int TEAM_2 = 2;
    private ArrayList<Player> playingPlayers; // i vasiki entekada
    private ArrayList<Player> substitution;// oloi oi paixtes tis omadas
    private int teamId;
    private String name;
   // private Color color;
    private String stadium = "KAMP_NOU";
    private int elevenCounter = 0; // metraei tous prwtous 11 pou dilwnw ws tin vasiki m entekada
    private boolean isMyTeam = false; // gia an paizw sti idio pc na mporw  ( if(!isMyTeam )) sto eventHandler (keylistener) na kounisw tous antipalous paixtes
    // me liga logia me ayto mporw na kathorisw ta koumpia tou antipalou (den mporei na einai ta didia me tou paixti mou)
    private boolean isPlayingHome;
    //formation
    private boolean isFollowBySameTeamPLayer; // to xrysimopoiw ston algorythmo gia na akolouthaei enas paixtis mono ayton pou exei tin mpala an einai konta
    private int score = 0;

    /**
     * an i team paizei entos edraas
     * @param isPlayingHome 
     */
    public Team(int teamId, String name, boolean isPlayingHome, boolean isMyTeam) {
        this.teamId = teamId;
        this.name = name;
        this.isPlayingHome = isPlayingHome;
        this.isMyTeam = isMyTeam;
        init();
    }

    /**
     * an  akolouthaei enas paixtis  ayton pou exei tin mpala (gia na min pigenoun poloi)
     * @return 
     */
    public boolean isIsFollowBySameTeamPLayer() {
        return isFollowBySameTeamPLayer;
    }

    /**
     * 
     * @param isFollowBySameTeamPLayer 
     */
    public void setIsFollowBySameTeamPLayer(boolean isFollowBySameTeamPLayer) {
        this.isFollowBySameTeamPLayer = isFollowBySameTeamPLayer;
    }

    /**
     * 
     * @return 
     */
    public boolean isPlayingHome() {
        return isPlayingHome;
    }

    /**
     * 
     * @param isMyStadium 
     */
    public ArrayList<Player> getPlayingPlayers() {
        return playingPlayers;
    }

    /**
     *
     * @return
     */
    public boolean isIsMyTeam() {
        return isMyTeam;
    }

    /**
     *
     * @return
     */
    public int getScore() {
        return score;
    }

    /**
     *
     * @param score
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * vazw tous paixtes stin omada
     * @param name
     * @param field
     * @param stamina
     * @param speed
     * @param id
     */
    public void addPLayer(Player player) {
        // vazw prwta tous 11kadatous paixtes 
        if (elevenCounter < 11) {
            playingPlayers.add(player);
            chooseEachPlayerPosition(player, "4-4-2");
            player.setSelected(true);
            elevenCounter++;
        } else {// kai meta tis allages
            substitution.add(player);
        }
    }

    /**
     * vgazw tis theseis twn paixtwn analoga me tin seira pou mpenoun stin
     * arraylist mou me tous entekadatous paixtes
     *
     * @param player
     */
    private void chooseEachPlayerPosition(Player player, String formation) {
        if (formation.equals("4-4-2")) {
            if (elevenCounter == 0) {
                setPosition(Player.position.GK, player); // no eeerrrraaassssssseeeeeeeeeeeeeeeeeeeeee
            }
            if (elevenCounter == 1) {
                setPosition(Player.position.CL, player);
            }
            if (elevenCounter == 2) {
                setPosition(Player.position.CB1, player);
            }
            if (elevenCounter == 3) {
                setPosition(Player.position.CB2, player);
            }
            if (elevenCounter == 4) {
                setPosition(Player.position.CR, player);
            }
            if (elevenCounter == 5) {
                setPosition(Player.position.ML, player);
            }
            if (elevenCounter == 6) {
                setPosition(Player.position.CM, player);
            }
            if (elevenCounter == 7) {
                setPosition(Player.position.MR, player);
            }
            if (elevenCounter == 8) {
                setPosition(Player.position.CF1, player);
            }
            if (elevenCounter == 9) {
                setPosition(Player.position.SS, player);
            }
            if (elevenCounter == 10) {
                setPosition(Player.position.CF2, player);
            }
        }
    }

    public boolean isHavingTheBall() {
        for (Player player : getPlayingPlayers()) {
            if (player.isHavingTheBall()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param pos
     * @param player 
     */
    public void setPosition(Player.position pos, Player player) {
        player.setPlayAtPosition(pos);

    }

    /**
     *  team formation
     * @param defenders
     * @param midles
     * @param forwards 
     */
    public void formation(int defenders, int midles, int forwards) {
    }

    /**
     * initialize
     */
    public void init() {

        substitution = new ArrayList();
        playingPlayers = new ArrayList<Player>();
    }

    /**
     * pairnw to onoma tis omadas
     * @return
     */
    public String getName() {
        return name;
    }

    public void setTeamId(int id) {
        this.teamId = id;
    }

    public void notCotrolledByTeam() {
        for (Player player : getPlayingPlayers()) {
            player.setHasTheBall(false);
        }
    }
}
