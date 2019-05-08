package com.example.hui.happy_of_disappear;

/**
 * Created by hui on 2019/4/30.
 */

public class Location {
    private int col;
    private int row;
    public Location(int row,int col){
        this.col = col;
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void setRow(int row) {
        this.row = row;
    }
}
