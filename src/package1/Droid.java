package package1;

import java.awt.Color;

public class Droid {
    private int row;
    private int col;
    private Color color;

    public Droid(int row, int col, Color color) {
        this.row = row;
        this.col = col;
        this.color = color;
    }

    public int getRow() {
        return row;
    }
    
    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }
    
    public void setCol(int col) {
        this.col = col;
    }

    public Color getColor() {
        return color;
    }

    // Tambahkan metode lain yang mungkin Anda butuhkan untuk droid
}
