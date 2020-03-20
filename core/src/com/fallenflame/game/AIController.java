package com.fallenflame.game;

import java.util.*;

public class AIController {
    /**
     * Enumeration to encode the finite state machine.
     */
    private static enum FSMState {
        /** The enemy just spawned */
        SPAWN,
        /** The enemy is patrolling around without a target */
        WANDER,
        /** The enemy has a target, but must get closer */
        CHASE,
        /** The enemy has a target and is attacking it */
        ATTACK,
        /** The enemy sees something that is not the player*/
        INVESTIAGE,
    }

    /** The radius from which a monster will notice a flare and approach it*/
    private static final int FLARE_DETECTION_RADIUS = 1000;
    /** The radius from which a monster can chase a player */
    private static final int CHASE_DIST = 1000;
    /** The radius from which a monster can attack a player */
    private static final int ATTACK_DIST = 1000;
    /** The radius from which an enemy could have considered to have finished its investigation
     * of a flare or of a player's last-known location*/
    private static final int REACHED_INVESTIGATE = 1000;

    // Instance Attributes
    /** The ship being controlled by this AIController */
    private EnemyModel enemy;
    /** The game level; used for pathfinding */
    private LevelModel level;
    /** All enemies; used to prevent collisions */
    private List<EnemyModel> enemies;
    /** The enemy's current state*/
    private FSMState state;
    /** The player*/
    private PlayerModel player;
    /** The flares that the enemy will investigate */
    private List<FlareModel> flares;
    /** The map's walls, used to prevent collisions */
    private List<WallModel> walls;
    /** The ship's next action. */
    private int move; // A ControlCode
    /** The number of ticks since we started this controller */
    private long ticks;

    /**
     * Creates an AIController for the ship with the given id.
     *
     * @param id The unique ship identifier
     * @param level The game level (for pathfinding)
     * @param enemies The list of enemies
     * @param player The player to target
     * @param flares The flares that can attract the enemy
     */
    public AIController(int id, LevelModel level, List<EnemyModel> enemies, PlayerModel player,
                        List<FlareModel> flares) {
        this.enemy = enemies.get(id);
        this.level = level;
        this.enemies = enemies;
        this.player = player;
        this.flares = flares;

        state = FSMState.SPAWN;
        move  = CONTROL_NO_ACTION;
        ticks = 0;
    }

    /**
     * Returns the action selected by this InputController
     *
     * The returned int is a bit-vector of more than one possible input
     * option. This is why we do not use an enumeration of Control Codes;
     * Java does not (nicely) provide bitwise operation support for enums.
     *
     * This function tests the environment and uses the FSM to chose the next
     * action of the enemy. This function SHOULD NOT need to be modified.  It
     * just contains code that drives the functions that you need to implement.
     *
     * @return the action selected by this InputController
     */
    public int getAction() {
        ticks++;

        if ((enemy.getId() + ticks) % 10 == 0) {
            changeStateIfApplicable();

            markGoalTiles();
            move = getMoveAlongPathToGoalTile();
        }

        int action = move;
        return action;
    }

    /**
     * Change the state of the enemy using a Finite State Machine.
     */
    private void changeStateIfApplicable() {
        Random random = new Random();
        int rand_int = random.nextInt(100);
        switch (state) {
            case SPAWN:
                state = FSMState.WANDER;
                break;

            case WANDER:
                if(withinChase()){
                    state = FSMState.CHASE;
                    break;
                }

                for(FlareModel flare : flares){
                    int flareX = flare.getX();
                    int flareY = flare.getY();
                    int flareDistance = cartesianDistance(flareX, enemy.getX(),
                            flareY, enemy.getY());
                    if(flareDistance <= FLARE_DETECTION_RADIUS) {
                        /**TODO MAKE MULTIPLE FLARE SUPPORT FOR PATHFINDING */
                        enemy.setInvestigateX(flareX);
                        enemy.setInvestigateY(flareY);
                        state = FSMState.INVESTIAGE;
                        break;
                    }
                }
                break;

            case CHASE:
                enemy.setActivated(true);
                if(rand_int < 30 || !withinChase()){
                    state = FSMState.INVESTIAGE;
                    enemy.setInvestigateX(flareX);
                    enemy.setInvestigateY(flareY);
                }
                else if(withinAttack()){state = FSMState.ATTACK;}
                break;

            case ATTACK:
                if(rand_int < 30){state = FSMState.WANDER;}
                else if(!withinAttack()){state = FSMState.CHASE;}
                break;

            case INVESTIAGE:
                if(withinChase()){
                    state = FSMState.CHASE;
                }
                else if(investigateReached()){
                    enemy.setActivated(false);
                    state = FSMState.WANDER;
                    enemy.setInvestigateX(null);
                    enemy.setInvestigateY(null);
                }
                break;

            default:
                assert (false);
                state = FSMState.WANDER;
                break;
        }
    }

    /**
     * Mark all desirable tiles to move to.
     *
     * This method implements pathfinding through the use of goal tiles.
     * It searches for all desirable tiles to move to (there may be more than
     * one), and marks each one as a goal. Then, the pathfinding method
     * getMoveAlongPathToGoalTile() moves the ship towards the closest one.
     *
     * POSTCONDITION: There is guaranteed to be at least one goal tile
     * when completed.
     */
    private void markGoalTiles() {
        boolean setGoal = false;
        int playerX = player.getX(), playerY = player.getY();
        int x, y;

        switch (state) {
            case SPAWN:
                break;

            case WANDER:
                Random random = new Random();
                if (enemy.getGoalX() == null || enemy.getGoalY() == null || random.nextInt(100) < 10) {
                    x = random.nextInt(level.getHeight());
                    y = random.nextInt(level.getWidth());
                    if (level.getSafe(x, y)) {
                        enemy.setGoal(x, y);
                        setGoal = true;
                    }

                } else if (level.getSafe(enemy.getGoalX(), enemy.getGoalY())) {
                    setGoal = true;
                }

                break;

            case CHASE:
                /* TODO: Multiple goal support */
//                for (int xOffset = -1; xOffset <= 1; xOffset++) {
//                    for (int yOffset = -1; yOffset <= 1; yOffset++) {
//                        x = xOffset + playerX;
//                        y = yOffset + playerY;
//                        if (level.getSafe(x, y)) {
//                            level.setGoal(x, y);
//                            setGoal = true;
//                        }
//                    }
//                }
                if(level.getSafe(playerX, playerY)){
                    enemy.setGoal(playerX, playerY);
                }
                break;

            case ATTACK:
                if (level.getSafe(playerX, playerY)) {
                    enemy.setGoal(playerX, playerY);
                    setGoal = true;
                }

                break;

            case INVESTIAGE:
//                for (int flareOffsetX = -1; flareOffsetX <= 1; flareOffsetX++) {
//                    for (int flareOffsetY = -1; flareOffsetY <= 1; flareOffsetY++) {
//                        x = enemy.getFlare().getX() + flareOffsetX;
//                        y = enemy.getFlare().getY() + flareOffsetY;
//                        if (level.getSafe(x, y)) {
//                            level.setGoal(x, y);
//                            setGoal = true;
//                        }
//                    }
//                }
                int investX = enemy.getInvestigateX();
                int investY = enemy.getInvestigateY();
                if(level.getSafe(investX, investY)){
                    enemy.setGoal(enemy.investX, investY);
                }
                break;
        }

        if (!setGoal) {
            int sx = enemy.screenToTile(enemy.getX());
            int sy = enemy.screenToTile(enemy.getY());
            enemy.setGoal(sx, sy);
        }
    }

    /**
     * Returns a movement direction that moves towards a goal tile.
     *
     * The value returned should be a control code.  See PlayerController
     * for more information on how to use control codes.
     *
     * @return a movement direction that moves towards a goal tile.
     */
    private int getMoveAlongPathToGoalTile() {
        int startX = level.screenToTile(enemy.getX());
        int startY = level.screenToTile(enemy.getY());
        if (enemy.isGoal(startX, startY)) {
            return CONTROL_NO_ACTION;
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

            if (visited.contains(c)) {
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

        public Coordinate(int x, int y, Coordinate prev){
            this.x = x;
            this.y = y;
            this.prev = prev;
        }

        public int getX() {return x;}
        public int getY() {return y;}
        public Coordinate getPrev() {return prev;}

        /** Determines the correct direction to move to based on the desired goal and current state */
        public int getDirectionFromPrev(){
            if(prev == null){return randAction();}
            if(this.x > prev.x && level.getSafe(this.x + 1, this.y)) {return CONTROL_MOVE_RIGHT;
            } else if(this.x < prev.x && level.getSafe(this.x - 1, this.y)){return CONTROL_MOVE_LEFT;
            } else if(this.y < prev.y && level.getSafe(this.x, this.y-1)){return CONTROL_MOVE_UP;
            } else if (this.y > prev.y && level.getSafe(this.x, this.y + 1)){return CONTROL_MOVE_DOWN;
            } else {return randAction();}
        }

        /** Selects a random cardinal direction to move */
        public int randAction(){
            Random random = new Random();
            int[] arr = {CONTROL_MOVE_DOWN, CONTROL_MOVE_LEFT, CONTROL_MOVE_RIGHT, CONTROL_MOVE_UP};
            return arr[random.nextInt(arr.length)];
        }
    }


    /** Returns whether an enemy is in range to chase a player */
    public boolean withinChase(){
        double distance = cartesianDistance(level.screenToTile(enemy.getX()),
                level.screenToTile(player.getX()),
                level.screenToTile(enemy.getY()),
                level.screenToTile(player.getY()));

        return distance <= CHASE_DIST;
    }

    /** Returns whether an enemy is in range to attack a player */
    public boolean withinAttack(){
        double distance = cartesianDistance(level.screenToTile(enemy.getX()),
                level.screenToTile(player.getX()),
                level.screenToTile(enemy.getY()),
                level.screenToTile(player.getY()));

        return distance <= ATTACK_DIST;
    }

    /** Determines whether the player has reached the coordinates they are investigating */
    public boolean investigateReached(){
        double distance = cartesianDistance(level.screenToTile(enemy.getX()),
                level.screenToTile(enemy.getInvestigateX()),
                level.screenToTile(enemy.getY()),
                level.screenToTile(enemy.getInvestigateY()));
        return distance <= REACHED_INVESTIGATE;
    }

    /** Returns the Cartesian distance of coordinates (x1, y1) and (x2, y2) */
    public double cartesianDistance(int x1, int x2, int y1, int y2){
        return Math.pow((Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)), 0.5);
    }
}