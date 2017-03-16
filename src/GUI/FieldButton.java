package GUI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author S
 */
public class FieldButton extends JButton implements ActionListener {

    private MineSweeperGUI parent;

    /**
     *
     * @param x button X axis
     * @param y button Y axis
     * @param parent parental JFrame
     */
    public FieldButton(int x, int y, final MineSweeperGUI parent) {

        this.putClientProperty("x", x);
        this.putClientProperty("y", y);

        if (parent.isServer()) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        System.out.println("Flagging");
                        JButton button = (JButton) e.getSource();
                        int firstIndex = (int) button.getClientProperty("x");
                        int secondIndex = (int) button.getClientProperty("y");
                        parent.getController().setFlag(firstIndex, secondIndex);
                    }
                }
            });
            addActionListener(this);
        }
        this.parent = parent;
    }

    /**
     *
     * @return button x position
     */
    public int get_x() {
        return (int) this.getClientProperty("x");
    }

    /**
     *
     * @return button y position
     */
    public int get_y() {
        return (int) this.getClientProperty("y");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (!parent.getController().isExploded()) {
            JButton button = (JButton) e.getSource();
            int firstIndex = (int) button.getClientProperty("x");
            int secondIndex = (int) button.getClientProperty("y");
            if (!parent.getController().getField().isFlagged(firstIndex, secondIndex)) {
                parent.getController().respondToPress(firstIndex, secondIndex);
            }
        }
    }

    @Override
    public String toString() {
        String s = "";
        if (this.isEnabled()) {
            s += "1";
        } else {
            s += "0";
        }
        s += ":";
        s += this.getText() + ":";
        if (this.getBackground() == Color.RED) {
            s += 1;
        } else if (this.getBackground() == Color.GREEN) {
            s += 2;
        } else {
            s += 0;
        }
        s += ":(" + get_x() + ";" + get_y() + ")";

        return s;
    }

}
