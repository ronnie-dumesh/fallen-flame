package com.fallenflame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import java.util.*;

public class AIController {
    /**
     * Enumeration to encode the finite state machine.
     */
    private enum FSMState {
        /** The enemy does not have a target */
        IDLE,
        /** The enemy is chasing the player */
        CHASE,
        /** The enemy is moving towards the player's last known location or a flare */
        INVESTIGATE,
    }

    // Constants
    /** The radius from which an enemy could have considered to have finished its investigation
     * of a flare or of a player's last-known location*/
    private static final int REACHED_INVESTIGATE = 1;

    // Instance Attributes
    /** The enemy being controlled by this AIController */
    private EnemyModel enemy;
    /** The game level; used for pathfinding */
    private LevelModel level;
    /** The enemy's current state*/
    private FSMState state;
    /** The player*/
    private PlayerModel player;
    /** The enemy's next move */
    private EnemyModel.Action action;
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
     * @param player The player to target
     * @param flares The flares that may attract the enemy
     */
    public AIController(int id, LevelModel level, List<EnemyModel> enemies, PlayerModel player,
                        List<FlareModel> flares) {
        this.enemy = enemies.get(id);
        this.level = level;
        this.player = player;
        // this.flares = flares;
        action = EnemyModel.Action.NO_ACTION;
        state = FSMState.IDLE;
        // action = Action.NO_ACTION;
        ticks = 0;

        Random random = new Random(id);
        randomID = random.nextInt();
    }

    /**
     * Returns the action selected by this InputController
     *
     * @return the action selected by this InputController
     */
    public EnemyModel.Action getAction() {
        ticks++;

        if ((randomID + ticks) % 5 == 0) {
            level.clearAllTiles();
            changeStateIfApplicable();
            markGoalTiles();
            chooseAction();
        }

        return action;
    }

    /**
     * Change the state of the enemy using a Finite State Machine.
     */
    private void changeStateIfApplicable() {
        switch(state) {
            case IDLE:
                enemy.setActivated(false);
                if(withinChase()){
                    state = FSMState.CHASE;
                    break;
                }
                break;

            case CHASE:
                enemy.setActivated(true);

                if(!withinChase()){
                    state = FSMState.INVESTIGATE;
                    enemy.setInvestigatePosition(new Vector2(player.getX(), player.getY()));
                }
                break;

            case INVESTIGATE:
                assert enemy.getInvestigatePosition() != null;
                if(withinChase()){
                    state = FSMState.CHASE;
                    enemy.setInvestigatePosition(null);
                }

                else if(investigateReached()){
                    enemy.setInvestigatePosition(null);
                    enemy.setActivated(false);
                    state = FSMState.IDLE;
                }
                break;

            default:
                assert false;
        }
    }

    /**
     * Mark all desirable tiles to move to.
     *
     * This method implements pathfinding through the use of goal tiles.
     */
    private void markGoalTiles() {
        switch(state) {
            case IDLE:
                break; // no goal tile

            case CHASE:
                level.setGoal(level.screenToTile(player.getX()), level.screenToTile(player.getY()));
                //System.out.println("Goal chase: " + level.screenToTile(player.getX()) + ", " + level.screenToTile(player.getY()));
                break;

            case INVESTIGATE:
                level.setGoal(level.screenToTile(enemy.getInvestigatePositionX()),
                        level.screenToTile(enemy.getInvestigatePositionY()));
                //System.out.println("Goal inv: " + level.screenToTile(enemy.getInvestigatePositionX()) + "," + level.screenToTile(enemy.getInvestigatePositionY()));
                break;

            default:
                assert false;
        }
    }

    /**
     * Determines action based on enemy state and goal tiles and saves action to `action` variable
     */
    private void chooseAction() {
        if(state == FSMState.IDLE)
            action = EnemyModel.Action.NO_ACTION;
        else
            action = getMoveAlongPathToGoalTile();
    }

    /**
     * Get enemy movement toward goal
     *
     * @return a movement direction that moves towards a goal tile or NO_ACTION.
     */
    private EnemyModel.Action getMoveAlongPathToGoalTile() {
        int startX = level.screenToTile(enemy.getX());
        int startY = level.screenToTile(enemy.getY());

        if(!level.isSafe(startX, startY))
            System.out.println("start not safe");

        // Initialize queue with movement options
        Queue<TileIndex> queue = new LinkedList<TileIndex>();
        if(level.isSafe(startX+1, startY)){
            queue.add(new TileIndex(startX+1, startY, EnemyModel.Action.RIGHT));}
        if(level.isSafe(startX, startY+1))
            queue.add(new TileIndex(startX, startY+1, EnemyModel.Action.UP));
        if(level.isSafe(startX-1, startY))
            queue.add(new TileIndex(startX-1, startY, EnemyModel.Action.LEFT));
        if(level.isSafe(startX, startY-1))
            queue.add(new TileIndex(startX, startY-1, EnemyModel.Action.DOWN));


        while(queue.peek() != null){
            TileIndex curr = queue.poll();
            // Already visited
            if(level.isVisited(curr.x, curr.y))
                continue;
            // Set visited
            level.setVisited(curr.x, curr.y);
            // Find goal
            if(level.isGoal(curr.x, curr.y))
                return curr.a;

            // Push all valid movements to queue (with current action because that is the first move from start location
            // that has shortest path to this point)
            if(level.isSafe(curr.x+1, curr.y))
                queue.add(new TileIndex(curr.x+1, curr.y, curr.a));
            if(level.isSafe(curr.x, curr.y+1))
                queue.add(new TileIndex(curr.x, curr.y+1, curr.a));
            if(level.isSafe(curr.x-1, curr.y))
                queue.add(new TileIndex(curr.x-1, curr.y, curr.a));
            if(level.isSafe(curr.x, curr.y-1))
                queue.add(new TileIndex(curr.x, curr.y-1, curr.a));
        }
        //System.out.println("Goal not acquired");
        return EnemyModel.Action.NO_ACTION;
    }

    /** Tile Index Object for queue in path finder
     * 	Holds an action attribute that indicates best starting action to get to the current x,y tile index
     */
    private class TileIndex {
        int x; int y; EnemyModel.Action a;
        public TileIndex(int x, int y) {
            this.x = x; this.y = y;
        }
        public TileIndex(int x, int y, EnemyModel.Action a) {
            this.x = x; this.y = y; this.a = a;
        }
    }

    /** Determines whether the player has reached the coordinates they are investigating */
    private boolean investigateReached(){
        double distance = cartesianDistance(level.screenToTile(enemy.getX()),
                level.screenToTile(enemy.getInvestigatePositionX()),
                level.screenToTile(enemy.getY()),
                level.screenToTile(enemy.getInvestigatePositionY()));
        return distance <= REACHED_INVESTIGATE;
    }

    /** Returns whether an enemy is in range to chase a player */
    private boolean withinChase(){
        double distance = cartesianDistance(enemy.getX(),player.getX(),enemy.getY(),player.getY());
        return distance <= player.getLightRadius();
    }


    /**
     * @param x1 the x coordinate of the first point
     * @param x2 the x coordinate of the second point
     * @param y1 the y coordinate of the first point
     * @param y2 the y coordinate of the second point
     * @return The cartesian distance between the points
     */
    private double cartesianDistance(float x1, float x2, float y1, float y2){
        return Math.pow((Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)), 0.5);
    }
}
