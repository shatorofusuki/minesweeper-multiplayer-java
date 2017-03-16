package logics;

import java.util.Random;

/**
 *
 * @author S
 */
public class Field {

    private Cell[][] field;
    private final int width;
    private final int height;
    private final double level;
    private boolean end;

    /**
     *
     * @param width width of the field
     * @param height height of the field
     * @param level level of the game on a scale from 0 to 1
     */
    public Field(int width, int height, double level) {
        this.width = width;
        this.height = height;
        this.level = level;
        field = new Cell[height][width];
        Random rand = new Random();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                field[i][j] = new Cell(rand.nextDouble() < (level), false);
            }
        }
        setMinesForTheField();
    }

    //<editor-fold defaultstate="collapsed" desc="Properties setters & getters">
    /**
     *
     * @param end if the game has ended
     */
    public void setEnd(boolean end) {
        this.end = end;
    }

    /**
     *
     * @return width of the field
     */
    public int getWidth() {
        return width;
    }

    /**
     *
     * @return height of the field
     */
    public int getHeight() {
        return height;
    }

    //</editor-fold>
    /**
     *
     * @return how many undiscovered bombs left
     */
    public int bombsLeft() {
        int res = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (field[i][j].isBomb() && !field[i][j].isFlagged()) {
                    res++;
                }
            }
        }
        return res;
    }

    /**
     *
     * @return how many bombs there are
     */
    public int bombsAll() {
        int res = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (field[i][j].isBomb()) {
                    res++;
                }
            }
        }
        return res;
    }

    /**
     *
     * @return how many flags are where they should not be
     */
    public int falseFlags() {
        int res = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (!field[i][j].isBomb() && field[i][j].isFlagged()) {
                    res++;
                }
            }
        }
        return res;
    }

    /**
     *
     * @param i as x
     * @param j as y
     */
    public void giveMeNewFieldFor(int i, int j) {
        while (field[i][j].isBomb()) {
            resetField();
        }
    }

    /**
     * basically restarts the game
     */
    public void resetField() {
        System.out.println("Resetting the field");
        Random rand = new Random();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                field[i][j].reset(rand.nextDouble() < (level));
            }
        }

        setMinesForTheField();
    }

    /**
     *
     * @param showMines if the bombs should be shown
     * @param showNumbers if the number of bombs around a certain cell should be
     * displayed
     * @return field as a string for the console
     */
    public String gameView(boolean showMines, boolean showNumbers) {
        String s = "";
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (field[i][j].isBomb() && showMines) {
                    s += "";
                } else if (showNumbers) {
                    s += field[i][j].getMinesAround();
                } else {
                    s += field[i][j];
                }
                s += " ";
            }
            s += "\n";
        }
        return s;
    }

    /**
     *
     * @param i as x
     * @param j as y
     * @return true if it's not a bomb
     */
    public boolean openCell(int i, int j) {
        if (reachedEdge(i, j)) {
            System.out.println("Exceeded limits of the field, try again");
            return true;
        }

        if (field[i][j].isOpened() || field[i][j].isFlagged()) {
            return true;
        }
        if (field[i][j].isBomb()) {
            return false;
        }
        return this.surf(i, j);
    }

    /**
     * not used, console mode only
     *
     * @return field as a string to send over the network
     */
    public String fieldDescriptionForClients() {
        String s = height + ":" + width + "\n";
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                s += j + ":";
                s += i + ":";
                if (field[i][j].isBomb()) {
                    s += "1:";
                } else {
                    s += "0:";
                }
                s += field[i][j].getMinesAround() + ":";
                if (field[i][j].isOpened()) {
                    s += "1:";
                } else {
                    s += "0:";
                }
                if (field[i][j].isFlagged()) {
                    s += "1";
                } else {
                    s += "0";
                }
                s += "\n";
            }
        }
        if (!end) {
            s += "end\n";
        } else {
            s += "finish\n";
        }
        return s;
    }

    /**
     * not used, console mode only
     *
     * @param s restores the state of the field form a string
     */
    public void restoreFieldFromMessage(String s) {
        for (String part : s.split("\n")) {

            if (part.equals("") || (part.length() - part.replaceAll("\\:", "").length()) != 5) {
                continue;
            } else if (part.equals("finish")) {
                setEnd(true);
            } else if (part.equals("end")) {
                setEnd(false);
            }
            //int x = Integer.parseInt(part.split(":")[0]);
            Cell temp = field[Integer.parseInt(part.split(":")[1])][Integer.parseInt(part.split(":")[0])];
            if (Integer.parseInt(part.split(":")[2]) == 1) {
                temp.setBomb(true);
            } else {
                temp.setBomb(false);
            }
            temp.setMinesAround(Integer.parseInt(part.split(":")[3]));
            if (Integer.parseInt(part.split(":")[4]) == 1) {
                temp.setOpened(true);
            } else {
                temp.setOpened(false);
            }
            if (Integer.parseInt(part.split(":")[5]) == 1) {
                temp.setFlagged(true);
            } else {
                temp.setFlagged(false);
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Field generation assistant methods">
    private void setMinesForTheField() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                field[i][j].setMinesAround(findMinesAround(i, j));
            }
        }
    }

    private int findMinesAround(int a, int b) {
        int result = 0;
        for (int i = a - 1; i <= a + 1; i++) {
            for (int j = b - 1; j <= b + 1; j++) {
                if (!reachedEdge(i, j) && !(j == b && i == a) && field[i][j].isBomb()) {
                    result++;
                }
            }
        }
        return result;
    }

    private boolean reachedEdge(int i, int j) {
        return i < 0 || j < 0 || i >= height || j >= width;
    }

    private boolean surf(int i, int j) {
        //System.out.println("Surfing " + i + ";" + j);
        if (reachedEdge(i, j) || field[i][j].isOpened()) {
            return true;
        }

        field[i][j].setOpened(true);

        if (field[i][j].isBomb()) {
            return false;
        }

        if (field[i][j].getMinesAround() > 0) {
            return true;
        } else {
            for (int a = i - 1; a <= i + 1; a++) {
                for (int b = j - 1; b <= j + 1; b++) {
                    if (!(i == a && j == b) && !reachedEdge(a, b)) {
                        this.surf(a, b);
                    }
                }
            }
        }
        return true;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Cell methods">
    /**
     *
     * @param i as x
     * @param j as x
     * @return if the cell is opened
     */
    public boolean isCellOpened(int i, int j) {
        return field[i][j].isOpened();
    }

    /**
     *
     * @param i as x
     * @param j as x
     * @param opened if the cell should be opened
     */
    public void setCellOpened(int i, int j, boolean opened) {
        field[i][j].setOpened(opened);
    }

    /**
     *
     * @param i as x
     * @param j as y
     * @return if the cell is explosive
     */
    public boolean isCellBomb(int i, int j) {
        return field[i][j].isBomb();
    }

    /**
     *
     * @param i as x
     * @param j as y
     * @return true if the cell has a flag
     */
    public boolean isFlagged(int i, int j) {
        return field[i][j].isFlagged();
    }

    /**
     *
     * @param i as x
     * @param j as y
     */
    public void setFlagged(int i, int j) {
        field[i][j].setFlagged(!field[i][j].isFlagged());
    }

    /**
     *
     * @param i as x
     * @param j as y
     * @return cell at x,y as a string
     */
    public String getCellString(int i, int j) {
        return field[i][j].toString();
    }
    //</editor-fold>

}
