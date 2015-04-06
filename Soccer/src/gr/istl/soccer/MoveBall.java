/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.istl.soccer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Nikos
 */
public class MoveBall extends JFrame {

    SoccerField fr;
    JButton jButton = new JButton("move");
    JTextField txt1 = new JTextField();
    JTextField txt2 = new JTextField();
    JPanel jPanel = new JPanel();

    public MoveBall(final SoccerField fr) {
        this.fr = fr;
        setSize(400,400);
        setVisible(true);
        jButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fr.getBall().moveToRelativeLocationInMeters(Integer.parseInt(txt1.getText()), Integer.parseInt(txt2.getText()));
            }
        });
        txt1.setColumns(5);
        txt2.setColumns(5);
        add(jPanel);
        jPanel.add(txt1);
        jPanel.add(txt2);

        jPanel.add(jButton);

    }

}
