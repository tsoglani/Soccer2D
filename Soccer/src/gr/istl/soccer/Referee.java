package gr.istl.soccer;

import java.awt.Color;


/**
 *
 * @author gaitanesnikos
 */
public class Referee implements Runnable {

   // private Color color;
    private SoccerField field;

    public Referee(SoccerField field) {
        this.field = field;
    }

    /**
     * 
     */
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            if (field.getBall() == null) {
                return;
            }
            if (!field.getRectangle().contains(field.getBall().getX() + 10, field.getBall().getY())) {
                // an i pala paei na vgei eksw apo to gipedo apo tin deksia meria tin metathetw 1 vima aristera
                // field.getBall().getPoint().x--;
            }
            if (!field.getRectangle().contains(field.getBall().getBounds().x, field.getBall().getBounds().y + field.getBall().getBounds().height + 20)) {
                // ani mpala vgainei eksw apo to gipedo  mpos tin katw meria paw tin mpala 1 bima pisw
                field.getBall().getPoint().y -= 4;
            //    System.out.println("-- ball --");

            }
            if (!field.getRectangle().contains(field.getBall().getBounds().x, field.getBall().getBounds().y + field.getBall().getBounds().height - 20)) {
                // ani mpala vgainei eksw apo to gipedo  mpos tin panw meria paw tin mpala 1 bima pisw
                field.getBall().getPoint().y += 4;

            }


        }
    }
}
