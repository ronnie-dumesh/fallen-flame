package com.fallenflame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.fallenflame.game.physics.obstacle.BoxObstacle;
import com.fallenflame.game.physics.obstacle.WheelObstacle;

import java.util.*;

public class LevelModel {

    public static class Tile {
        /** Is this a goal tiles */
        public boolean goal = false;
        /** Has this tile been visited by pathfinding? */
        public boolean visited = false;
        /** Is this tile safe */
        public boolean safe = true;
    }


    /** 2D tile representation of board where TRUE indicates tile is available for movement*/
    private Tile[][] tileGrid;
    /** Constant tile size (tiles are square so this is x and y) */
    private static final float TILE_SIZE = .4f;
    /** Width of screen */
    private float width;
    /** Height of screen */
    private float height;


    public LevelModel(){ }

    public void initialize(Rectangle bounds, List<WallModel> walls, List<EnemyModel> enemies) {
        width = bounds.getWidth();
        height = bounds.getHeight();

        tileGrid = new Tile[(int) Math.ceil(width / TILE_SIZE)][(int) Math.ceil(height / TILE_SIZE)];
        for(int x = 0; x < tileGrid.length; x++){
            for(int y = 0; y < tileGrid[0].length; y++){
                tileGrid[x][y] = new Tile();
                tileGrid[x][y].safe = true;
            }
        }
        // Set grid to false where obstacle exists
        // TODO: place enemies?
        for(WallModel w : walls) {
            setBoxObstacleInGrid(w, false);
        }
    }

    /**
     * @return the width of the screen
     */
    public float getWidth() {return this.width;}

    /**
     * @return the height of the screen
     */
    public float getHeight() {return this.height;}

    /**
     * Sets tiles previously covered by player as available
     * @param player
     */
    public void removePlayer(PlayerModel player) { setWheelObstacleInGrid(player, true); }

    /**
     * Sets tiles currently covered by player as unavailable
     * @param player
     */
    public void placePlayer(PlayerModel player) { setWheelObstacleInGrid(player, false); }

    /**
     * Sets tiles previously covered by enemy as available
     * @param enemy
     */
    public void removeEnemy(EnemyModel enemy) { setWheelObstacleInGrid(enemy, true); }

    /**
     * Sets tiles currently covered by enemy as unavailable
     * @param enemy
     */
    public void placeEnemy(EnemyModel enemy) { setWheelObstacleInGrid(enemy, false); }

    /**
     * Set tiles currently covered by WheelObstacle obs to boolean b
     * @param obs Wheel obstacle
     * @param b Boolean value
     */
    public void setWheelObstacleInGrid(WheelObstacle obs, boolean b) {
        for(int x = screenToTile(obs.getX() - obs.getRadius());
            x <= screenToTile(obs.getX() + obs.getRadius()); x++) {
            for(int y = screenToTile(obs.getY() - obs.getRadius());
                y <= screenToTile(obs.getY() + obs.getRadius()); y++) {
                if (!inBounds(x, y)) continue;
                tileGrid[x][y].safe = b;
            }
        }
    }

    /**
     * Set tiles currently covered by BoxObstacle obs to boolean b
     * @param obs Wheel obstacle
     * @param b Boolean value
     */
    public void setBoxObstacleInGrid(BoxObstacle obs, boolean b) {
        for(int x = screenToTile(obs.getX() - obs.getWidth()/2);
            x <= screenToTile(obs.getX() + obs.getWidth()/2); x++) {
            for(int y = screenToTile(obs.getY() - obs.getHeight()/2);
                y <= screenToTile(obs.getY() + obs.getHeight()/2); y++) {
                if (!inBounds(x, y)) continue;
                tileGrid[x][y].safe = b;
            }
        }
    }

    /**
     * Returns the tile cell index for a screen position.
     *
     * While all positions are 2-dimensional, the dimensions to
     * the board are symmetric. This allows us to use the same
     * method to convert an x coordinate or a y coordinate to
     * a cell index.
     *
     * @param f Screen position coordinate
     *
     * @return the tile cell index for a screen position.
     */
    public int screenToTile(float f) {
        return (int)(f / TILE_SIZE);
    }

    /**
     * Returns the screen position coordinate for a tile cell index.
     *
     * While all positions are 2-dimensional, the dimensions to
     * the board are symmetric. This allows us to use the same
     * method to convert an x coordinate or a y coordinate to
     * a cell index.
     *
     * @param n Tile cell index
     *
     * @return the screen position coordinate for a tile cell index.
     */
    public float tileToScreen(int n) {
        return (float) (n + 0.5f) * TILE_SIZE;
    }

    /**
     * Returns true if the given position is a valid tile
     *
     * It does not return if the tile is safe or not
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     * @return true if the given position is a valid tile
     */
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < tileGrid.length && y < tileGrid[0].length;
    }

    /**
     * Returns whether the input tile is available for movement.
     *
     * @param x Tile x-coor
     * @param y Tile y-coor
     * @return isSafe boolean
     */
    public boolean isSafe(int x, int y) {
        //TODO: should return inBounds(x, y) and whether Tile.safe is true.
        //TODO: Tile.safe is false when it's an obstacle on it and true otherwise.
        return (inBounds(x,y) && tileGrid[x][y].safe) || tileGrid[x][y].goal;
    } //TODO: temporary change

    /**
     * Returns true if the tile has been visited.
     *
     * A tile position that is not on the board will return false.
     * Precondition: tile in bounds
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     * @return true if the tile is a goal.
     */
    public boolean isVisited(int x, int y){
        if (!inBounds(x,y)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return false;
        }
        return tileGrid[x][y].visited;
    }

    /**
     * Marks a tile as visited.
     *
     * A marked tile will return true for isVisited(), until a call to clearAllTiles().
     * A tile position that is not on the board will raise an error
     * Precondition: tile in bounds
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setVisited(int x, int y){
        if (!inBounds(x,y)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return;
        }
        tileGrid[x][y].visited = true;
    }

    /**
     * Returns true if the tile is a goal.
     *
     * A tile position that is not on the board will return false.
     * Precondition: tile in bounds
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     *
     * @return true if the tile is a goal.
     */
    public boolean isGoal(int x, int y){
        if (!inBounds(x,y)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return false;
        }
        return tileGrid[x][y].goal;
    }

    /**
     * Marks a tile as a goal.
     *
     * A marked tile will return true for isGoal(), until a call to clearAllTiles().
     * A tile position that is not on the board will raise an error
     * Precondition: tile in bounds
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    public void setGoal(int x, int y){
        if (!inBounds(x,y)) {
            Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
            return;
        }
        tileGrid[x][y].goal = true;
    }

    /**
     * Set the goal and visited of each tile to false
     */
    public void clearAllTiles() {
        for (int x = 0; x < tileGrid.length; x++) {
            for (int y = 0; y < tileGrid[0].length; y++) {
                tileGrid[x][y].goal = false;
                tileGrid[x][y].visited = false;
            }
        }
    }

    public void drawDebug(GameCanvas canvas, Vector2 drawScale) {
        for (int x = 0; x < tileGrid.length; x++) {
            for (int y = 0; y < tileGrid[0].length; y++) {
                canvas.drawGrid(x, y, tileGrid[x][y], drawScale, TILE_SIZE);
            }
        }
    }
}
