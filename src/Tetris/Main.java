package Tetris;

import Engine2D.*;
import Engine2D.Rectangle;
import UnityMath.Vector2;

import javax.swing.*;
import javax.swing.plaf.IconUIResource;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;

public class Main {
    public static Scene scene;
    public static int size = 20;
    public static int BoardW = 10;
    public static int BoardH = 20;
    public static int WIDTH = BoardW*size+50;
    public static int HEIGHT = BoardH*size+50;
    public static AbstractShape[][] board = new AbstractShape[BoardW][BoardH];
    public static TetrisItem active;
    public static ShapesObject sceneBoard;
    public static boolean game = true;
    public static int score = 0;
    public static int lvl = 1;
    public static int maxlvl = 100;
    public static int delay;
    public static boolean nowButtonPresst = false;
    public static Color Backgraund = Color.DARK_GRAY;
    //public static int swp = 0xff040200;
    public static Color[] ColorSwapArray = new Color[6];
    public static void main(String[] args) {
        System.out.println("Hello world!");

        //ColorSwapArray[0] = new Color(0, 0, 0);
        ColorSwapArray[0] = new Color(0, 0, 255);
        ColorSwapArray[1] = new Color(0, 255, 0);
        ColorSwapArray[2] = new Color(0, 255, 255);
        ColorSwapArray[3] = new Color(255, 0, 0);
        ColorSwapArray[4] = new Color(255, 0, 255);
        ColorSwapArray[5] = new Color(255, 255, 0);
        //ColorSwapArray[7] = new Color(255, 255, 255);

        JFrame frame = new JFrame("Tetris");
        frame.setSize(new Dimension(WIDTH+100, HEIGHT+100));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        scene = new Scene(WIDTH, HEIGHT);
        //scene.setCenterVisible(true);
        //scene.setCoordVisible(true);
        sceneBoard = new ShapesObject("Board", 0);
        scene.add(sceneBoard);
        for(int i = 0; i < board.length; ++i){
            for(int j = 0; j < board[i].length; ++j){
                var space = new Rectangle(size/2, new Vector2(i*size, j*size), Backgraund);
                sceneBoard.add(space);
                board[i][j] = space;
            }
        }
        Scene.camera.position = new Vector2(size*BoardW/2, -size*BoardH/2);

        active = new TetrisItem();
        active = newActive();
        //active.setCenterVisible(true);
        scene.add(active);

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if(!game) return;
                nowButtonPresst = true;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> {
                        if (!isBorder('l')){
                            active.move(new Vector2(-size, 0));
                            scene.repaint();
                        }
                    }
                    case KeyEvent.VK_RIGHT -> {
                        if (!isBorder('r')) {
                            active.move(new Vector2(size, 0));
                            scene.repaint();
                        }
                    }
                    case KeyEvent.VK_DOWN -> {
                        if (!isStop()){
                            active.move(new Vector2(0, -size));
                            scene.repaint();
                        }
                        else {
                            Respaun();
                            Explosion();
                            scene.repaint();
                        }
                    }
                    case KeyEvent.VK_UP -> {
                        Rotation();
                        scene.repaint();
                    }
                }
                nowButtonPresst = false;
            }
        });
        frame.add(scene);
        frame.setVisible(true);

        Toolkit tk = Toolkit.getDefaultToolkit();

        long start_time = 0;
        long end_time;
        long summ_time = 0;
        for(;;) {
            end_time = System.currentTimeMillis();
            summ_time += end_time - start_time;
            delay = 500 - (lvl * 10);
            if (summ_time >= delay) {
                tk.sync();
                if(!nowButtonPresst) {
                    if (game) {
                        if (!isStop()) active.move(new Vector2(0, -size));
                        else {
                            Respaun();
                            Explosion();
                        }
                        scene.repaint();
                    } else {
                        System.out.println("Game Over: " + score);
                        break;
                    }

                    summ_time = 0L;
                }
            }
            start_time = end_time;
        }
    }
    public static void ColorChange(boolean all){
        if(all) {
            for (var it : sceneBoard.body.toArray(new AbstractShape[0])) {
                if (it.id == 1) {
                    it.color = new Color(it.color.getRGB() ^ ColorSwapArray[lvl % ColorSwapArray.length].getRGB());
                }
            }
        }
        for(var it: active.body.toArray(new AbstractShape[0])){
            if(lvl!= 0)it.color = new Color(active.color.getRGB() ^ ColorSwapArray[lvl%ColorSwapArray.length].getRGB());
        }
    }
    public static void Rotation(){
        if(!active.isRotate) return;
        active.setAngZ(90);
        Scene.camera.position = new Vector2(0,0);
        ArrayList<Vector2>newPos = new ArrayList<>();
        for (var shape: active.body){
            var p = shape.getVertices(shape.center);
            Scene.fromSceneCoord(p);
            if(!isBorder2(p)){
                newPos.add(p);
            }
        }
        if(newPos.size() == active.body.size()){
            for (var i = 0; i < newPos.size(); ++i) {
                active.body.get(i).position = new Vector2(newPos.get(i));
            }
        }
        Scene.camera.position = new Vector2(size*BoardW/2, -size*BoardH/2);
        active.setAngZ(0);
    }
    public static void EndGame(){
        game = false;
        scene.remove(active);
        active = null;
    }
    public static void Explosion(){
        int rowCount = 0;
        int countI = 0;
        int lastRow = 0;
        boolean fl = false;
        ArrayDeque<Integer> ExpRow = new ArrayDeque<>();
        for(int j = 0; j < BoardH; ++j){
            countI = 0;
            if(fl) break;
            for(int i = 0; i < BoardW; ++i){
                if(board[i][j].id == 1){
                    countI++;
                }
                if(countI == BoardW){
                    rowCount++;
                    ExpRow.addFirst(j);
                }
            }
            if(countI == 0){
                lastRow = j;
                break;
            }
            countI = 0;
        }
        int oldlvl = lvl;
        if(rowCount > 0) {
            for (var j : ExpRow) {
                for (int i = 0; i < BoardW; ++i) {
                    sceneBoard.body.remove(board[i][j]);
                    var space = new Rectangle(size / 2, new Vector2(i * size, j * size), Backgraund);
                    sceneBoard.add(space);
                    board[i][j] = space;
                    if(rowCount == 4)
                        score += lvl*300;
                    else
                        score += lvl*100;
                }
            }
            if(score >= lvl*10000) {
                if (lvl == maxlvl)
                    System.out.println("Max Level!");
                else {
                    lvl++;
                    System.out.println("Level: " + lvl);
                }
            }
            for(var j: ExpRow){
                for(int jj = j + 1; jj < lastRow; ++jj){
                    for (int ii = 0; ii < BoardW; ++ii) {
                        var tmp = board[ii][jj];
                        sceneBoard.body.remove(board[ii][jj]);
                        var space = new Rectangle(size / 2, new Vector2(ii * size, jj * size), Backgraund);
                        sceneBoard.add(space);
                        board[ii][jj] = space;

                        sceneBoard.body.remove(board[ii][jj-1]);
                        var tmpShape = new Rectangle(size / 2, new Vector2(ii * size, (jj-1) * size), tmp.color);
                        tmpShape.id = tmp.id;
                        sceneBoard.add(tmpShape);
                        tmpShape.id = tmp.id;
                        board[ii][jj-1] = tmpShape;
                    }
                }
                lastRow--;
            }
            //if(oldlvl != lvl)ColorChange(true);
        }
    }
    public static void Respaun(){
        for(var shape : active.body){
            int i = (int)Math.abs(shape.position.x/size);
            int j = (int)Math.abs(shape.position.y/size);
            sceneBoard.body.remove(board[i][j]);
            shape.id = 1;
            sceneBoard.add(shape);
            shape.id = 1;
            board[i][j] = shape;
        }
        scene.remove(active);
        active = newActive();
        //ColorChange(false);
        scene.add(active);
    }
    public static boolean isBorder(char dir){
        for(var shape: active.body){
            int i = (int)Math.abs(shape.position.x/size);
            int j = (int)Math.abs(shape.position.y/size);
            if(dir == 'l' && i == 0 || dir == 'r' && i == BoardW-1)
                return true;
            else if(dir == 'l' && board[i-1][j].id == 1)
                return true;
            else if(dir == 'r' && board[i+1][j].id == 1)
                return true;
        }
        return false;
    }
    public static boolean isBorder2(Vector2 tryPos){
            int i = (int)(tryPos.x/size);
            int j = (int)(tryPos.y/size);
            if(i < 0 || j < 0 || i >= BoardW || j >= BoardH)
                return true;
            else return board[i][j].id == 1;
    }
    public static boolean isStop(){
        for(var shape: active.body){
            int i = (int)Math.abs(shape.position.x/size);
            int j = (int)Math.abs(shape.position.y/size);
            if(j == 0)
                return true;
            else if( board[i][j-1].id == 1)
                return true;
        }

        return false;
    }
    public static TetrisItem newActive(){
        TetrisItem newO = new TetrisItem("Active", 2, true);
        //newO.setAngZ(180);
        Random rnd = new Random();
        int type = rnd.nextInt(8);
        switch (type){
            case 0 ->{
                newO.color = Color.BLUE;
                newO.add(new Rectangle(size/2, new Vector2(0,0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(0,1*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(0,2*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(0,3*size), newO.color));

                newO.name = "Vert";
                newO.move(new Vector2(size*BoardW/2, size*BoardH-4*size));
            }
            case 1 ->{
                newO.color = Color.RED;
                newO.add(new Rectangle(size/2, new Vector2(0*size,0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size,1*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size,0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size,2*size), newO.color));

                newO.name = "!L";
                newO.move(new Vector2(size*BoardW/2, size*BoardH-3*size));
            }
            case 2 ->{
                newO.color = Color.MAGENTA;
                newO.add(new Rectangle(size/2, new Vector2(0*size,0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(0*size,1*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size,0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(0*size,2*size), newO.color));

                newO.name = "L";
                newO.move(new Vector2(size*BoardW/2, size*BoardH-3*size));
            }
            case 3 ->{
                newO.color = Color.GREEN;
                newO.add(new Rectangle(size/2, new Vector2(0*size, 0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(0*size, 1*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size, 0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size, 1*size), newO.color));

                newO.name = "Cube";
                newO.isRotate = false;
                newO.move(new Vector2(size*BoardW/2, size*BoardH-2*size));
            }
            case 4 ->{
                newO.color = Color.YELLOW;
                newO.add(new Rectangle(size/2, new Vector2(0*size, 0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size, 0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size, 1*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(2*size, 1*size), newO.color));

                newO.name = "!Z";
                newO.move(new Vector2(size*BoardW/2, size*BoardH-2*size));
            }
            case 5 ->{
                newO.color = Color.ORANGE;
                newO.add(new Rectangle(size/2, new Vector2(0*size, 1*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size, 1*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size, 0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(2*size, 0*size), newO.color));

                newO.name = "Z";
                newO.move(new Vector2(size*BoardW/2, size*BoardH-2*size));
            }
            case 6 ->{
                newO.color = Color.PINK;
                newO.add(new Rectangle(size/2, new Vector2(0*size, 0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size, 0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size, 1*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(2*size, 0*size), newO.color));

                newO.name = "!T";
                newO.move(new Vector2(size*BoardW/2, size*BoardH-2*size));
            }
            case 7 ->{
                newO.color = Color.CYAN;
                newO.add(new Rectangle(size/2, new Vector2(0*size, 0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size, 0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size, 1*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(2*size, 0*size), newO.color));
                newO.add(new Rectangle(size/2, new Vector2(1*size, -1*size), newO.color));

                newO.name = "My1";
                newO.isRotate = false;
                newO.move(new Vector2(size*BoardW/2, size*BoardH-2*size));
            }
        }
        for(var shape : newO.body){
            int i = (int)Math.abs(shape.position.x/size);
            int j = (int)Math.abs(shape.position.y/size);
            if(board[i][j].id == 1) EndGame();
        }
        var tmp = new Vector2(newO.body.get(1).center);
        newO.center = new Vector2(tmp.add(newO.body.get(1).position));

        return newO;
    }
}