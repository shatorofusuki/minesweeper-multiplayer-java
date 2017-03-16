package logics;

/**
 *
 * @author S
 */
public class Cell {

    private int minesAround;
    private boolean bomb;
    private boolean flagged;
    private boolean opened;

    /**
     *
     * @param bomb if the cell should be created with a bomb
     * @param opened if the cell should be in an opened state
     */
    public Cell(boolean bomb, boolean opened) {
        this.minesAround = 0;
        this.flagged = false;
        this.bomb = bomb;
        this.opened = opened;
    }

    //<editor-fold defaultstate="collapsed" desc="Properties setters & getters">
    /**
     *
     * @return mines around the cell
     */
    public int getMinesAround() {
        return minesAround;
    }

    /**
     *
     * @param minesAround mines around the cell
     */
    public void setMinesAround(int minesAround) {
        this.minesAround = minesAround;
    }

    /**
     *
     * @return if the cell is explosive
     */
    public boolean isBomb() {
        return bomb;
    }

    /**
     *
     * @param bomb if the cell should contain a bomb
     */
    public void setBomb(boolean bomb) {
        bomb = bomb;
    }

    /**
     *
     * @return if the player has placed a flag on the cell
     */
    public boolean isFlagged() {
        return flagged;
    }

    /**
     *
     * @param flagged if the flag should be raised on the cell
     */
    public void setFlagged(boolean flagged) {
        //System.out.println(flagged);
        if (!opened) {
            this.flagged = flagged;
        }
    }

    /**
     *
     * @return if the cell has been opened
     */
    public boolean isOpened() {
        return opened;
    }

    /**
     *
     * @param opened if the cell should be opened
     */
    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    //</editor-fold>
    /**
     *
     * @param bomb if the cell should become explosive
     */
    public void reset(boolean bomb) {
        opened = false;
        flagged = false;
        this.bomb = bomb;
        minesAround = 0;
    }

    @Override
    public String toString() {
        String result = "";
        if (flagged) {
            result = "✝";
        } else if (!opened) {
            result = "❒";
        } else if (minesAround == 0) {
            result = " ";
        } else {
            result += minesAround;
        }
        return result;
    }

}
