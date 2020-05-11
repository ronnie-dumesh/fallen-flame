package com.fallenflame.game.enemies;

import com.fallenflame.game.FlareModel;
import com.fallenflame.game.LevelModel;
import com.fallenflame.game.PlayerModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public abstract class AIController {

    // Instance Attributes
    /** The enemy being controlled by this AIController */
    protected EnemyModel enemy;
    /** THe player */
    protected PlayerModel player;
    /** The game level; used for pathfinding */
    protected LevelModel level;
    /** The enemy's next action --> control code */
    private int move;
    /** The number of ticks since we started this controller */
    private long ticks;
    /** A randomID to stagger the amount of processing of each enemy per frame */
    private int randomID;

    /**
     * Creates an AIController for the enemy with the given id.
     *
     * @param id The unique enemy identifier
     * @param level The game level (for pathfinding)
     * @param enemies The list of enemies
     */
    public AIController(int id, LevelModel level, List<EnemyModel> enemies, PlayerModel player) {
        this.enemy = enemies.get(id);
        this.level = level;
        this.player = player;
        move  = EnemyModel.CONTROL_NO_ACTION;
        ticks = 0;

        Random random = new Random(id);
        randomID = random.nextInt();
    }

    /**
     * Returns the action selected by this InputController
     *
     * @return the action selected by this InputController
     */
    public int getAction(){
        ticks++;

        if ((randomID + ticks) % 5 == 0) {
            // Clear LevelModel for processing
            level.clearAllTiles();
            // Process the FSM
            changeStateIfApplicable();
            // Pathfinding
            if(markGoalTiles())
                move = getMoveAlongPathToGoalTile();
        }

        int action = move;

        action |= getOtherAction();

        return action;
    }

    /**
     * Returns action codes for actions besides movement.
     * Defaults to 0 (no other action) unless extending class overwrites this function.
     * @return int control code
     */
    protected int getOtherAction() { return 0; }

    /**
     * Change the state of the enemy using a Finite State Machine.
     */
    protected abstract void changeStateIfApplicable();

    /**
     * Mark all desirable tiles to move to.
     * This method implements pathfinding through the use of goal tiles.
     * @return false if no tiles marked
     */
    protected abstract boolean markGoalTiles();

    /**
     * Get enemy movement toward goal
     *
     * @return a movement direction that moves towards a goal tile or NO_ACTION.
     */
    protected int getMoveAlongPathToGoalTile() {
        int startX = level.screenToTile(enemy.getX());
        int startY = level.screenToTile(enemy.getY());

        if(!level.isSafe(startX, startY))
            System.out.println("start not safe");

        // Initialize queue with movement options
        Queue<TileIndex> queue = new LinkedList<TileIndex>();
        if(level.isSafe(startX+1, startY)){
            queue.add(new TileIndex(startX+1, startY, EnemyModel.CONTROL_MOVE_RIGHT));}
        if(level.isSafe(startX, startY+1))
            queue.add(new TileIndex(startX, startY+1, EnemyModel.CONTROL_MOVE_UP));
        if(level.isSafe(startX-1, startY))
            queue.add(new TileIndex(startX-1, startY, EnemyModel.CONTROL_MOVE_LEFT));
        if(level.isSafe(startX, startY-1))
            queue.add(new TileIndex(startX, startY-1, EnemyModel.CONTROL_MOVE_DOWN));
        if(level.isSafe(startX-1, startY-1))
            queue.add(new TileIndex(startX-1, startY-1, EnemyModel.CONTROL_MOVE_DOWN_LEFT));
        if(level.isSafe(startX+1, startY-1))
            queue.add(new TileIndex(startX+1, startY-1, EnemyModel.CONTROL_MOVE_DOWN_RIGHT));
        if(level.isSafe(startX-1, startY+1))
            queue.add(new TileIndex(startX-1, startY+1, EnemyModel.CONTROL_MOVE_UP_LEFT));
        if(level.isSafe(startX+1, startY+1))
            queue.add(new TileIndex(startX+1, startY+1, EnemyModel.CONTROL_MOVE_UP_RIGHT));

        while(queue.peek() != null){
            TileIndex curr = queue.poll();
            // Already visited
            if(level.isVisited(curr.x, curr.y))
                continue;
            // Set visited
            level.setVisited(curr.x, curr.y);
            // Find goal
            if(level.isGoal(curr.x, curr.y))
                return curr.ctrlCode;

            // Push all valid movements to queue (with current action because that is the first move from start location
            // that has shortest path to this point)
            if(level.isSafe(curr.x+1, curr.y))
                queue.add(new TileIndex(curr.x+1, curr.y, curr.ctrlCode));
            if(level.isSafe(curr.x, curr.y+1))
                queue.add(new TileIndex(curr.x, curr.y+1, curr.ctrlCode));
            if(level.isSafe(curr.x-1, curr.y))
                queue.add(new TileIndex(curr.x-1, curr.y, curr.ctrlCode));
            if(level.isSafe(curr.x, curr.y-1))
                queue.add(new TileIndex(curr.x, curr.y-1, curr.ctrlCode));

            if(level.isSafe(curr.x-1, curr.y-1))
                queue.add(new TileIndex(curr.x-1, curr.y-1, curr.ctrlCode));
            if(level.isSafe(curr.x+1, curr.y-1))
                queue.add(new TileIndex(curr.x+1, curr.y-1, curr.ctrlCode));
            if(level.isSafe(curr.x-1, curr.y+1))
                queue.add(new TileIndex(curr.x-1, curr.y+1, curr.ctrlCode));
            if(level.isSafe(curr.x+1, curr.y+1))
                queue.add(new TileIndex(curr.x+1, curr.y+1, curr.ctrlCode));
        }
        //System.out.println("Goal not acquired");
        return EnemyModel.CONTROL_NO_ACTION;
    }

    /** Tile Index Object for queue in path finder
     * 	Holds an action attribute that indicates best starting action to get to the current x,y tile index
     */
    protected class TileIndex {
        int x; int y; int ctrlCode;
        public TileIndex(int x, int y) {
            this.x = x; this.y = y;
        }
        public TileIndex(int x, int y, int ctrlCode) {
            this.x = x; this.y = y; this.ctrlCode = ctrlCode;
        }
    }

    /** Returns whether an enemy is in the player's light radius */
    protected boolean withinPlayerLight(){
        double distance = cartesianDistance(enemy.getTextureX(),player.getTextureX(),enemy.getTextureY(),player.getTextureY());
        return distance <= player.getLightRadius();
    }

    /** Returns whether an enemy is in range to chase a player */
    protected boolean withinFlareRange(FlareModel f){
        double distance = cartesianDistance(enemy.getX(),f.getX(),enemy.getY(),f.getY());
        return distance <= f.getLightRadius();
    }

    /**
     * @param x1 the x coordinate of the first point
     * @param x2 the x coordinate of the second point
     * @param y1 the y coordinate of the first point
     * @param y2 the y coordinate of the second point
     * @return The cartesian distance between the points
     */
    protected double cartesianDistance(float x1, float x2, float y1, float y2){
        return Math.pow((Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)), 0.5);
    }
}
