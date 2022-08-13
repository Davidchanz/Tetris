package Tetris;

import Engine2D.ShapesObject;

import java.awt.*;

public class TetrisItem extends ShapesObject {
    public boolean isRotate;
    public Color color;

    TetrisItem(){
        super();
        this.isRotate = true;
    }
    TetrisItem(String name, int id, boolean rotate){
        super(name, id);
        this.isRotate = rotate;
    }
}
