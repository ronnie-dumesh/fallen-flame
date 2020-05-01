package com.fallenflame.game.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.FlareModel;
import com.fallenflame.game.LevelModel;
import com.fallenflame.game.PlayerModel;
import com.fallenflame.game.enemies.EnemyModel;

import java.util.*;

/**
 * This class is the AI Controller for all moving enemies.
 * Subtype Default: enemy stands still when idle
 * Subtype Pathing: enemy follows a path when idle
 */
public class AITypeAController extends AIController {
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
    private static final int REACHED_INVESTIGATE = 2;

    // Instance Attributes
    /** The enemy's current state*/
    private FSMState state;
    /** The enemy being controlled by this AIController */
    private EnemyTypeAModel enemy;
    /** The flares in the world */
    private List<FlareModel> flares;
    /** Pathing step coordinates - Null for enemies not of subtype Pathing */
    private Vector2[] pathCoors;
    /** Current point in path */
    private int pathPoint;

    /**
     * Creates an AIController for the enemy with the given id.
     *
     * @param id The unique enemy identifier
     * @param level The game level (for pathfinding)
     * @param enemies The list of enemies
     * @param player The player to target
     * @param flares The flares that may attract the enemy
     */
    public AITypeAController(int id, LevelModel level, List<EnemyModel> enemies, PlayerModel player,
                        List<FlareModel> flares) {
        super(id, level, enemies, player);
        assert(enemy.getClass() == EnemyTypeAModel.class);
        this.enemy = (EnemyTypeAModel)super.enemy;
        this.flares = flares;
        pathCoors = null;
        state = FSMState.IDLE;
    }

    /**
     * Creates an AIController for an enemy of subtype pathing.
     *
     * @param id The unique enemy identifier
     * @param level The game level (for pathfinding)
     * @param enemies The list of enemies
     * @param player The player to target
     * @param flares The flares that may attract the enemy
     */
    public AITypeAController(int id, LevelModel level, List<EnemyModel> enemies, PlayerModel player,
                             List<FlareModel> flares, JsonValue pathCoorsJSON) {
        super(id, level, enemies, player);
        this.player = player;
        assert(enemy.getClass() == EnemyTypeAModel.class);
        this.enemy = (EnemyTypeAModel)super.enemy;
        this.flares = flares;
        state = FSMState.IDLE;
        // unpack pathing coordinates
        Vector2[] pathCoors = new Vector2[pathCoorsJSON.size];
        for(int i=0; i<pathCoorsJSON.size; i++) {
            int[] coor = pathCoorsJSON.get(i).asIntArray();
            pathCoors[i] = new Vector2(coor[0], coor[1]);
        }
        this.pathCoors = pathCoors;
        // initialize pathing
        pathPoint = 0;
        enemy.setInvestigatePosition(pathCoors[pathPoint]);
    }

    /**
     * Change the state of the enemy using a Finite State Machine.
     */
    protected void changeStateIfApplicable() {
        switch(state) {
            case IDLE:
                enemy.setSneaking(); // walk slower when pathing
                enemy.makeCalm();
                // Check for flares in range
                checkFlares();
                // Check for player in range
                if(withinPlayerLight()){
                    state = FSMState.CHASE;
                    break;
                }
                // If enemy is of subtype pathing
                if(pathCoors != null) {
                    // update investigation position
                    if(investigateReached()) {
                        pathPoint = (pathPoint + 1) % pathCoors.length;
                        enemy.setInvestigatePosition(pathCoors[pathPoint]);
                    }
                }
                break;

            case CHASE:
                enemy.setWalking(); // walk normally when chasing
                enemy.makeAggressive();
                // Check for flares in range <-- we check this here because the enemy can be "distracted" by flares
                checkFlares();
                // If no longer in player light, go to last known position
                if(!withinPlayerLight()){
                    state = FSMState.INVESTIGATE;
                    enemy.setInvestigatePosition(new Vector2(player.getX(), player.getY()));
                }
                break;

            case INVESTIGATE:
                enemy.setWalking(); // walk normally when investigating
                enemy.makeAlert();
                assert enemy.getInvestigatePosition() != null;
                // Check if investigating flare
                if(enemy.isInvestigatingFlare()){
                    // Update investigation position for moving flare
                    enemy.setInvestigatePosition(enemy.getInvestigateFlare().getX(), enemy.getInvestigateFlare().getY());
                }
                // Only check for player in range iff enemy is not investigating flare
                else if(withinPlayerLight()){
                    state = FSMState.CHASE;
                    enemy.clearInvestigateFlare();
                    break;
                }
                
                // if flare died, or we reached investigation position and it wasn't a flare stop
                if((enemy.isInvestigatingFlare() && (enemy.getInvestigateFlare().timeToBurnout() <= 0)) ||
                        (investigateReached() && !enemy.isInvestigatingFlare())){
                    enemy.setInvestigatePosition(null);
                    enemy.clearInvestigateFlare();
                    state = FSMState.IDLE;
                    // If enemy is of subtype pathing
                    if(pathCoors != null){
                        enemy.setInvestigatePosition(pathCoors[pathPoint]);
                    }
                }
                break;

            default:
                assert false;
        }
    }

    /**
     * Helper function for checking for flares and investigating those that are within range
     */
    private void checkFlares(){
        // Check for flares in range
        for(FlareModel f : flares){
            // If flare found, chase flare
            if(withinFlareRange(f)){
                state = FSMState.INVESTIGATE;
                enemy.setInvestigatePosition(new Vector2(f.getX(), f.getY()));
                enemy.setInvestigateFlare(f);
                break;
            }
        }
    }

    /**
     * Mark all desirable tiles to move to.
     *
     * This method implements pathfinding through the use of goal tiles.
     */
    protected void markGoalTiles() {
        switch(state) {
            case IDLE:
                // If enemy is of subtype pathing
                if(pathCoors != null){
                    level.setGoal(level.screenToTile(enemy.getInvestigatePositionX()),
                            level.screenToTile(enemy.getInvestigatePositionY()));
                }
                break; // no goal tile

            case CHASE:
                level.setGoal(level.screenToTile(player.getX()), level.screenToTile(player.getY()));
                break;

            case INVESTIGATE:
                level.setGoal(level.screenToTile(enemy.getInvestigatePositionX()),
                        level.screenToTile(enemy.getInvestigatePositionY()));
                break;

            default:
                Gdx.app.error("AITypeAController", "Impossible state reached", new IllegalArgumentException());
                assert false;
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
}
