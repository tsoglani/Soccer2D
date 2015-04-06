package gr.istl.soccer;

/**
 *
 * @author george
 */
public class Test {

    Player[] players = new Player[10];

    Test() {
        addPlayer(new Player());
        addPlayer(new Player());
        addPlayer(new Player());
        addPlayer(new Player());
        for (Player p : players) {
            System.out.println("" + p);
        }

    }

    private void addPlayer(Player player) {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) {
                players[i] = player;
                break;
            }
        }
    }

    public static void main(String[] args) {
        new Test();
    }

    class Player {
    }
}
