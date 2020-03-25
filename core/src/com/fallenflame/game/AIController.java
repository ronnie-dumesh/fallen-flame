package com.fallenflame.game;

import com.badlogic.gdx.math.Vector2;

import java.util.*;

public class AIController {
    /**
     * Enumeration to encode the finite state machine.
     */
    private enum FSMState {
        /** The enemy does not have a target */
        IDLE,
        /** The enemy has a target, but must get closer */
        CHASE,
        /** The enemy has a target and is attacking it */
        ATTACK,
        /** The enemy sees something that is not the player*/
        INVESTIGATE,
    }

    /**
     * Enumeration to encode actions
     */
    private enum Action {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        NO_ACTION
    }

    // Constants
    /** The radius from which an enemy will notice a flare and approach it*/
    private static final int FLARE_DETECTION_RADIUS = 1000;
    /** The radius from which an enemy can chase a player */
    private static final int CHASE_DIST = 1000;
    /** The radius from which an enemy can attack a player */
    private static final int ATTACK_DIST = 1000;
    /** The radius from which an enemy could have considered to have finished its investigation
     * of a flare or of a player's last-known location*/
    private static final int REACHED_INVESTIGATE = 1000;

    // Instance Attributes
    /** The enemy being controlled by this AIController */
    private EnemyModel enemy;
    /** The game level; used for pathfinding */
    private LevelModel level;

    /** The enemy's current state*/
    private FSMState state;
    /** The player*/
    private PlayerModel player;
    /** The flares that the enemy will investigate */
    private List<FlareModel> flares;
    /** The flares that have been investigated by the enemy */
    private HashSet<FlareModel> investigatedFlares;
    /** The last-known position of the player of an encountered flare*/
    private Vector2 investigationPosition;
    /** The enemy's next move */
    private Action move; // A ControlCode
    /** The number of ticks since we started this controller */
    private long ticks;
    /** A random-number generator for use within the class */
    private Random random;
    /** A randomID to stagger the amount of processing of each enemy per frame */
    private int randomID;

    /**
     * Creates an AIController for the enemy with the given id.
     *
     * @param id The unique enemy identifier
     * @param level The game level (for pathfinding)
     * @param enemies The list of enemies
     * @param player The player to target
     * @param flares The flares that can attract the enemy
     */
    public AIController(int id, LevelModel level, List<EnemyModel> enemies, PlayerModel player,
                        List<FlareModel> flares) {
        this.enemy = enemies.get(id);
        this.level = level;
        this.player = player;
        this.flares = flares;

        state = FSMState.IDLE;
        move  = Action.NO_ACTION;
        ticks = 0;

        random = new Random(id);
        randomID = random.nextInt();
    }

    /**
     * Returns the action selected by this InputController
     *
     * @return the action selected by this InputController
     */
    public Action getAction() {
        ticks++;

        if ((randomID + ticks) % 10 == 0) {
            changeStateIfApplicable();

            markGoalTiles();
            move = getMoveAlongPathToGoalTile();
        }

        Action action = move;
        return action;
    }

    /**
     * Change the state of the enemy using a Finite State Machine.
     */
    private void changeStateIfApplicable() {
        int rand_int = random.nextInt(100);
        switch (state) {
            case IDLE:
                if(withinChase()){
                    state = FSMState.CHASE;
                    break;
                }

                FlareModel closestFlare = null; //Safety for if closestFlare is not null for some-reason.
                //This is to prevent an enemy from getting stuck between two flares
                int closestFlareDistance = FLARE_DETECTION_RADIUS;
                for(FlareModel flare : flares){
                    double flareDistance = cartesianDistance(
                            level.screenToTile(flare.getX()),
                            level.screenToTile(enemy.getX()),
                            level.screenToTile(flare.getY()),
                            level.screenToTile(enemy.getY())
                    );

                    if(flareDistance <= closestFlareDistance && !investigatedFlares.contains(flare)) {
                        closestFlare = flare;
                        closestFlareDistance = (int) flareDistance;
                    }
                }

                if(closestFlare != null){
                    investigatedFlares.add(closestFlare);
                    investigationPosition = new Vector2(closestFlare.getX(), closestFlare.getY());
                    state = FSMState.INVESTIGATE;
                }

                break;

            case CHASE:
                enemy.setActivated(true);

                //TODO: consider random chance of quitting chase
                if(!withinChase()){
                    state = FSMState.INVESTIGATE;
                    investigationPosition = new Vector2(player.getX(), player.getY());
                }

                else if(withinAttack()){
                    state = FSMState.ATTACK;
                }

                break;

            case ATTACK:
                if(!withinAttack()){
                    state = FSMState.CHASE;
                }

                break;

            case INVESTIGATE: //investigation position must not be null
                assert investigationPosition != null;
                if(withinChase()){
                    state = FSMState.CHASE;
                    investigationPosition = null;
                }

                else if(investigateReached()){
                    investigationPosition = null;
                    enemy.setActivated(false);
                    state = FSMState.IDLE;
                }

                break;

            default:
                assert (false);
                state = FSMState.IDLE;
                break;
        }
    }

    /**
     * Mark all desirable tiles to move to.
     *
     * This method implements pathfinding through the use of goal tiles.
     *
     * POSTCONDITION: There is guaranteed to be at least one goal tile
     * when completed.
     */
    private void markGoalTiles() {
        boolean setGoal = false;
        float playerX = player.getX(), playerY = player.getY();
        int sx, sy;

        switch (state) {
            case IDLE:
                float x, y;
                if (enemy.getGoal() == null || random.nextInt(100) < 10) {
                    x = random.nextFloat() * level.getHeight();
                    y = random.nextFloat() * level.getWidth();
                    enemy.setGoal(x, y);
                    setGoal = true;
                }
                break;

            case CHASE:
                enemy.setGoal(playerX, playerY);
                setGoal = true;
                break;

            case ATTACK:
                enemy.setGoal(playerX, playerY);
                setGoal = true;
                break;

            case INVESTIGATE: //investigationPosition must not be null
                float investX = investigationPosition.x;
                float investY = investigationPosition.y;
                enemy.setGoal(investX, investY);
                break;
        }

        if (!setGoal) {
            enemy.clearGoal();
        }
    }

    /**
     * Returns a movement direction that moves towards a goal tile or NO_ACTION.
     *
     * The value returned should be a control code.  See PlayerController
     * for more information on how to use control codes.
     *
     * @return a movement direction that moves towards a goal tile or NO_ACTION.
     */
    private Action getMoveAlongPathToGoalTile() {
        int startX = level.screenToTile(enemy.getX());
        int startY = level.screenToTile(enemy.getY());
        if (enemy.isGoal(startX, startY)) {
            return Action.NO_ACTION;
        }
        Coordinate start = new Coordinate(startX, startY, null);
        Coordinate c = start;

        Queue<Coordinate> queue = new LinkedList<>();
        queue.add(c);

        HashSet<Coordinate> visited = new HashSet<>();

        while (!queue.isEmpty()) {

            c = queue.poll();
            if (enemy.isGoal(c.getX(), c.getY())) {
                break;
            }

            if (!visited.contains(c)) {
                visited.add(c);

                for (int yOffSet = -1; yOffSet <= 1; yOffSet++) {
                    for (int xOffSet = -1; xOffSet <= 1; xOffSet++) {
                        int x = c.getX() + xOffSet;
                        int y = c.getY() + yOffSet;
                        if (level.getSafe(x, y)) {
                            queue.add(new Coordinate(x, y, c));
                        }
                    }
                }
            }
        }
        while (c.getPrev().getPrev() != null) {
            c = c.getPrev();
        }

        return c.getDirectionFromPrev();
    }


    private class Coordinate {
        private int x;
        private int y;
        private Coordinate prev;

        private Coordinate(int x, int y, Coordinate prev){
            this.x = x;
            this.y = y;
            this.prev = prev;
        }

        private int getX() {return x;}
        private int getY() {return y;}
        private Coordinate getPrev() {return prev;}

        /** Determines the correct direction to move to based on the desired goal and current state */
        private Action getDirectionFromPrev(){
            if(prev == null){return randAction();}
            if(this.x > prev.x && level.getSafe(this.x + 1, this.y)) {return Action.RIGHT;
            } else if(this.x < prev.x && level.getSafe(this.x - 1, this.y)){return Action.LEFT;
            } else if(this.y < prev.y && level.getSafe(this.x, this.y-1)){return Action.UP;
            } else if (this.y > prev.y && level.getSafe(this.x, this.y + 1)){return Action.DOWN;
            } else {return randAction();}
        }

        /** Selects a random cardinal direction to move */
        private Action randAction(){
            Action[] arr = {Action.DOWN, Action.UP, Action.LEFT, Action.RIGHT};
            return arr[random.nextInt(arr.length)];
        }
    }


    /** Returns whether an enemy is in range to chase a player */
    private boolean withinChase(){
        double distance = cartesianDistance(level.screenToTile(enemy.getX()),
                level.screenToTile(player.getX()),
                level.screenToTile(enemy.getY()),
                level.screenToTile(player.getY()));

        return distance <= player.getLightRadius();
    }

    /** Returns whether an enemy is in range to attack a player */
    private boolean withinAttack(){
        double distance = cartesianDistance(level.screenToTile(enemy.getX()),
                level.screenToTile(player.getX()),
                level.screenToTile(enemy.getY()),
                level.screenToTile(player.getY()));

        return distance <= ATTACK_DIST;
    }

    /** Determines whether the player has reached the coordinates they are investigating */
    private boolean investigateReached(){
        double distance = cartesianDistance(level.screenToTile(enemy.getX()),
                level.screenToTile(enemy.getGoalX()),
                level.screenToTile(enemy.getY()),
                level.screenToTile(enemy.getGoalY()));
        return distance <= REACHED_INVESTIGATE;
    }

    /**
     * @param x1 the x coordinate of the first point
     * @param x2 the x coordinate of the second point
     * @param y1 the y coordinate of the first point
     * @param y2 the y coordinate of the second point
     * @return The cartesian distance between the points
     */
    private double cartesianDistance(int x1, int x2, int y1, int y2){
        return Math.pow((Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)), 0.5);
    }
}