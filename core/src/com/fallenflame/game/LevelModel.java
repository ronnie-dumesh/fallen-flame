package com.fallenflame.game;

import com.badlogic.gdx.math.*;
import com.fallenflame.game.physics.obstacle.BoxObstacle;
import com.fallenflame.game.physics.obstacle.WheelObstacle;

import java.util.*;

// TODO: ASSUMES OBSTACLE POSITION IS AT CENTER, NEED TO VERIFY IF TRUE

public class LevelModel {

    /** 2D tile representation of board where TRUE indicates tile is available for movement*/
    private boolean[][] tileGrid;
    /** Size of tiles (tiles are square so is x and y) */
    private int tileSize;
    /** Width of screen */
    private float width;
    /** Height of screen */
    private float height;

    public LevelModel(){ }

    public void initialize(Rectangle bounds, PlayerModel player, List<WallModel> walls, List<EnemyModel> enemies) {
        tileSize = Math.max((int)player.getRadius(),1);
        width = bounds.getWidth();
        height = bounds.getHeight();

        tileGrid = new boolean[(int) width / tileSize][(int) height / tileSize];
        // Initialize grid to true
        for(int x = 0; x < tileGrid.length; x++) {
            for(int y = 0; y < tileGrid[x].length; y++) {
                tileGrid[x][y] = true;
            }
        }
        // Set grid to false where obstacle exists
        setWheelObstacleInGrid(player, false);
        for(EnemyModel e : enemies) {
            setWheelObstacleInGrid(e, false);
        }
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
            x < screenToTile(obs.getX() + obs.getRadius()); x++) {
            for(int y = screenToTile(obs.getY() - obs.getRadius());
                y < screenToTile(obs.getY() + obs.getRadius()); y++) {
                tileGrid[x][y] = b;
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
            x < screenToTile(obs.getX() + obs.getWidth()); x++) {
            for(int y = screenToTile(obs.getY() - obs.getHeight()/2);
                y < screenToTile(obs.getY() + obs.getHeight()); y++) {
                tileGrid[x][y] = b;
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
        return (int)(f / tileSize);
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
        return (float) (n + 0.5f) * tileSize;
    }

    /**
     * Returns whether the input tile is available for movement.
     *
     * @param x Tile x-coor
     * @param y Tile y-coor
     * @return isSafe boolean
     */
    public boolean getSafe(int x, int y) {
        return tileGrid[x][y];
    }
}
