package gr.istl.soccer;

import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author george
 */
public class PrintCommandsDialog extends JDialog {

    private static JTextArea txt = new JTextArea();
    private JScrollPane jScrollPane;

    public PrintCommandsDialog() {

        add(new JLabel(), BorderLayout.BEFORE_FIRST_LINE);
        jScrollPane = new JScrollPane();
        jScrollPane.getViewport().add(txt);
        add(jScrollPane);
        jScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());
            }
        });
        txt.setEditable(false);
        setSize(300, 300);
        setVisible(true);
    }

    public static void print(final String textToAppend) {
        txt.append(textToAppend + "\n");
          
//            new Thread(){
//                public void run(){
//                    SoccerFrame.gssc.play("" + textToAppend.replace("0", "")+ ".", GoogleSpeechSynthesisClient2.PLAYBACK_SPEED.FAST);
//                }
//            }.start();
    }
}
