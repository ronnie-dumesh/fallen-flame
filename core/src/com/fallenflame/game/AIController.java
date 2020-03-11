package com.fallenflame.game;

public class AIController implements InputController {
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
        /** Go to player's last known position */
        LAST_KNOWN
    }

//    // Constants for chase algorithms
//    /** How close a target must be for us to chase it */
//    private static final int CHASE_DIST  = 9;
//    /** How close a target must be for us to attack it */
//    private static final int ATTACK_DIST = 4;

    // Instance Attributes
    /** The ship being controlled by this AIController */
    private EnemyModel enemy;
    /** The game board; used for pathfinding */
    private Board board;
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
     * @param board The game board (for pathfinding)
     * @param ships The list of ships (for targetting)
     */
	public AIController(int id, Board board, List<EnemyModel> enemies, PlayerModel player,
                        List<FlareModel> flares, List<WallModel> walls) {
        this.enemy = enemies.get(id);
        this.board = board;
        this.enemies = enemies;
        this.player = player;
        this.flares = flares;
        this.walls = walls;

        state = FSMState.SPAWN;
        move  = CONTROL_NO_ACTION;
        ticks = 0;

        // Select an initial target
        target = null;
        selectTarget();
    }

    /**
     * Returns the action selected by this InputController
     *
     * The returned int is a bit-vector of more than one possible input
     * option. This is why we do not use an enumeration of Control Codes;
     * Java does not (nicely) provide bitwise operation support for enums.
     *
     * This function tests the environment and uses the FSM to chose the next
     * action of the ship. This function SHOULD NOT need to be modified.  It
     * just contains code that drives the functions that you need to implement.
     *
     * @return the action selected by this InputController
     */
    public int getAction() {
        // Increment the number of ticks.
        //System.out.println(state);
        ticks++;

        // Do not need to rework ourselves every frame. Just every 10 ticks.
        if ((ship.getId() + ticks) % 10 == 0) {
            // Process the FSM
            changeStateIfApplicable();

            // Pathfinding
            markGoalTiles();
            move = getMoveAlongPathToGoalTile();
        }

        int action = move;
        return action;
    }

    /**
     * Change the state of the ship.
     *
     * A Finite State Machine (FSM) is just a collection of rules that,
     * given a current state, and given certain observations about the
     * environment, chooses a new state. For example, if we are currently
     * in the ATTACK state, we may want to switch to the CHASE state if the
     * target gets out of range.
     */
    private void changeStateIfApplicable() {
        Random random = new Random();
        int rand_int = random.nextInt(100);
        switch (state) {
            case SPAWN:
                state = FSMState.WANDER;
                break;

            case WANDER:
                selectTarget();

                int distance = cartesianDistance(player.getX(), player.getY(),
                                                    enemy.getX(), enemy.getY());
                if(distance <= player.getRadius()){
                    state = FSMState.CHASE;
                    break;
                }

                for(FlareModel flare : flares){
                    distance = cartesianDistance(flare.getX(), flare.getY(),
                                                    enemy.getX(), enemy.getY());
                    /**TODO MAKE CONSTANT FOR DISTANCE FROM FLARE
                     * like FLARE_DETECTION_RADIUS*/
                    if(distance <= 100) {
                        /**TODO MAKE MULTIPLE FLARE SUPPORT FOR PATHFINDING */
                        state = FSMState.INVESTIAGE;
                        break;
                    }
                }
                break;

            case CHASE:
                if(target == null || rand_int < 30 || !withinChase(ship, target)){state = FSMState.WANDER;}
                else if(withinAttack(ship, target)){state = FSMState.ATTACK;}
                break;

            case ATTACK:
                if(target == null || rand_int < 30){state = FSMState.WANDER;}
                else if(!withinAttack(ship, target)){state = FSMState.CHASE;}
                break;

            case INVESTIAGE:
                if()

            default:
                assert (false);
                state = FSMState.WANDER;
                break;
        }
    }

    /**
     * Acquire a target to attack (and put it in field target).
     *
     * Insert your checking and target selection code here. Note that this
     * code does not need to reassign <c>target</c> every single time it is
     * called. Like all other methods, make sure it works with any number
     * of players (between 0 and 32 players will be checked). Also, it is a
     * good idea to make sure the ship does not target itself or an
     * already-fallen (e.g. inactive) ship.
     */
    private void selectTarget() {
        Random random = new Random();
        ArrayList<Ship> targetArray = new ArrayList<>();

        if(target == null || !target.isAlive() || !target.isActive()){target=null;}
        if(target == null || !withinChase(ship, target)){
            target = null;
            for(Ship enemyShip : fleet) {
                if (!ship.equals(enemyShip) && enemyShip.isAlive() && withinChase(ship, enemyShip)) {
                    targetArray.add(enemyShip);
                }
            }
            if(targetArray.size() > 0) {
                target = targetArray.get(random.nextInt(targetArray.size()));
            } else {
                target = null;
            }
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
        // Clear out previous pathfinding data.
        board.clearMarks();
        boolean setGoal = false; // Until we find a goal

        // Add initialization code as necessary
        //#region PUT YOUR CODE HERE
        int targetX = 0, targetY = 0, x = 0, y = 0;
        if(target != null){
            targetX = board.screenToBoard(target.getX());
            targetY = board.screenToBoard(target.getY());
        }

        //#endregion

        switch (state) {
            case SPAWN: // Do not pre-empt with FSMState in a case
                // insert code here to mark tiles (if any) that spawning ships
                // want to go to, and set setGoal to true if we marked any.
                // Ships in the spawning state will immediately move to another
                // state, so there is no need for goal tiles here.

                //#region PUT YOUR CODE HERE
                break;
            //#endregion

            case WANDER: // Do not pre-empt with FSMState in a case
                // Insert code to mark tiles that will cause us to move around;
                // set setGoal to true if we marked any tiles.
                // NOTE: this case must work even if the ship has no target
                // (and changeStateIfApplicable should make sure we are never
                // in a state that won't work at the time)

                //#region PUT YOUR CODE HERE
                Random random = new Random();
                if(wanderGoalTileY == null || wanderGoalTileX == null || random.nextInt(100) < 10) {
                    x = random.nextInt(board.getHeight());
                    y = random.nextInt(board.getWidth());
                    if (board.isSafeAt(x, y) && board.inBounds(x, y)) {
                        wanderGoalTileX = x;
                        wanderGoalTileY = y;
                        board.setGoal(x, y);
                        setGoal = true;
                    }
                } else if (board.isSafeAt(wanderGoalTileX, wanderGoalTileY) &&
                        board.inBounds(wanderGoalTileX, wanderGoalTileY)){
                    board.setGoal(wanderGoalTileX, wanderGoalTileY);
                    setGoal = true;
                }

                //#endregion */
                break;

            case CHASE: // Do not pre-empt with FSMState in a case
                // Insert code to mark tiles that will cause us to chase the target;
                // set setGoal to true if we marked any tiles.

                for(int xOffset = -1; xOffset <= 1; xOffset++){
                    for(int yOffset = -1; yOffset <=1; yOffset++) {
                        x = xOffset + targetX;
                        y = yOffset + targetY;
                        if (board.isSafeAt(x, y) && board.inBounds(x, y)) {
                            board.setGoal(x, y);
                            setGoal = true;
                        }
                    }
                }

                //#region PUT YOUR CODE HERE

                //#endregion
                break;

            case ATTACK: // Do not pre-empty with FSMState in a case
                // Insert code here to mark tiles we can attack from, (see
                // canShootTargetFrom); set setGoal to true if we marked any tiles.
                //#region PUT YOUR CODE HERE
                //#endregion
                if (board.isSafeAt(targetX, targetY) && board.inBounds(targetX, targetY)) {
                    board.setGoal(targetX, targetY);
                    setGoal = true;
                }

                break;
        }

        // If we have no goals, mark current position as a goal
        // so we do not spend time looking for nothing:
        if (!setGoal) {
            int sx = board.screenToBoard(ship.getX());
            int sy = board.screenToBoard(ship.getY());
            board.setGoal(sx, sy);
        }
    }

    /**
     * Returns a movement direction that moves towards a goal tile.
     *
     * This is one of the longest parts of the assignment. Implement
     * breadth-first search (from 2110) to find the best goal tile
     * to move to. However, just return the movement direction for
     * the next step, not the entire path.
     *
     * The value returned should be a control code.  See PlayerController
     * for more information on how to use control codes.
     *
     * @return a movement direction that moves towards a goal tile.
     */
    private int getMoveAlongPathToGoalTile() {
        //#region PUT YOUR CODE HERE
        int startX = board.screenToBoard(ship.getX());
        int startY = board.screenToBoard(ship.getY());
        if(board.isGoal(startX, startY)){return CONTROL_NO_ACTION;}
        Coordinate start = new Coordinate(startX, startY, null);
        Coordinate c = start;

        Queue<Coordinate> queue = new LinkedList<>();
        queue.add(c);

        while(!queue.isEmpty()){

            c = queue.poll();
            if(board.isGoal(c.getX(), c.getY())){break;}

            if(!board.isVisited(c.getX(), c.getY())){
                board.setVisited(c.getX(), c.getY());

                for(int yOffSet = -1; yOffSet<=1; yOffSet++){
                    for(int xOffSet = -1; xOffSet<=1; xOffSet++){
                        int x = c.getX() + xOffSet;
                        int y = c.getY() + yOffSet;
                        if(board.inBounds(x, y) && board.isSafeAt(x, y)){
                            queue.add(new Coordinate(x, y, c));
                        }
                    }
                }
            }
        }
        while(c.getPrev().getPrev() != null){
            c = c.getPrev();
        }

        //if(c == null){return CONTROL_NO_ACTION;}
        return c.getDirectionFromPrev();
        //#endregion*/
        //return CONTROL_NO_ACTION;
    }

    // Add any auxiliary methods or data structures here
    //#region PUT YOUR CODE HERE
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

        public int getDirectionFromPrev(){
            if(prev == null){return randAction();}
            if(this.x > prev.x && board.isSafeAt(this.x + 1, this.y)) {return CONTROL_MOVE_RIGHT;
            } else if(this.x < prev.x && board.isSafeAt(this.x - 1, this.y)){return CONTROL_MOVE_LEFT;
            } else if(this.y < prev.y && board.isSafeAt(this.x, this.y-1)){return CONTROL_MOVE_UP;
            } else if (this.y > prev.y && board.isSafeAt(this.x, this.y + 1)){return CONTROL_MOVE_DOWN;
            } else {return randAction();}
        }

        public int randAction(){
            Random random = new Random();
            int[] arr = {CONTROL_MOVE_DOWN, CONTROL_MOVE_LEFT, CONTROL_MOVE_RIGHT, CONTROL_MOVE_UP};
            return arr[random.nextInt(arr.length)];
        }
    }

    public boolean withinChase(Ship myShip, Ship enemyShip){
        double distance = cartesianDistance(board.screenToBoard(myShip.getX()),
                board.screenToBoard(enemyShip.getX()),
                board.screenToBoard(myShip.getY()),
                board.screenToBoard(enemyShip.getY()));

        return distance <= CHASE_DIST;
    }

    public boolean withinAttack(Ship myShip, Ship enemyShip){
        double distance = cartesianDistance(board.screenToBoard(myShip.getX()),
                board.screenToBoard(enemyShip.getX()),
                board.screenToBoard(myShip.getY()),
                board.screenToBoard(enemyShip.getY()));

        return distance <= ATTACK_DIST;
    }

    public double cartesianDistance(int x1, int x2, int y1, int y2){
        return Math.pow((Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)), 0.5);
    }
}
