/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import java.awt.event.*;
import javax.swing.JButton;

/**
 *
 * @author S
 */
public class RestartButton extends JButton implements ActionListener {

    private MineSweeperGUI msgui;

    /**
     *
     * @param msgui points to the gui
     */
    public RestartButton(MineSweeperGUI msgui) {
        super();
        this.setText("Restart");
        this.msgui = msgui;
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Asked for restart");
        msgui.getController().restart();
    }

}
