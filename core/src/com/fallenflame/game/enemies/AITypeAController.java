package com.fallenflame.game.enemies;

import com.badlogic.gdx.math.Vector2;
import com.fallenflame.game.FlareModel;
import com.fallenflame.game.LevelModel;
import com.fallenflame.game.PlayerModel;
import com.fallenflame.game.enemies.EnemyModel;

import java.util.*;

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
    private static final int REACHED_INVESTIGATE = 1;

    // Instance Attributes
    /** The enemy's current state*/
    private FSMState state;
    /** The player*/
    private PlayerModel player;
    /** The enemy being controlled by this AIController */
    private EnemyTypeAModel enemy;
    /** The flares in the world */
    private List<FlareModel> flares;

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
        super(id, level, enemies);
        this.player = player;
        assert(enemy.getClass() == EnemyTypeAModel.class);
        this.enemy = (EnemyTypeAModel)super.enemy;
        this.flares = flares;
        state = FSMState.IDLE;
    }

    /**
     * Change the state of the enemy using a Finite State Machine.
     */
    protected void changeStateIfApplicable() {
        switch(state) {
            case IDLE:
                enemy.makeCalm();
                // Check for player in range
                if(withinChase()){
                    state = FSMState.CHASE;
                    break;
                }
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
                break;

            case CHASE:
                enemy.makeAggressive();

                if(!withinChase()){
                    state = FSMState.INVESTIGATE;
                    enemy.setInvestigatePosition(new Vector2(player.getX(), player.getY()));
                }
                break;

            case INVESTIGATE:
                enemy.makeAlert();
                assert enemy.getInvestigatePosition() != null;
                // Check for player in range
                if(withinChase()){
                    state = FSMState.CHASE;
                    enemy.clearInvestigateFlare();
                    break;
                }
                // Check if investigating flare
                if(enemy.isInvestigatingFlare()){
                    // Update investigation position for moving flare
                    enemy.setInvestigatePosition(enemy.getInvestigateFlare().getX(), enemy.getInvestigateFlare().getY());
                }
                // If we reached investigation position AND we were chasing player OR we were chasing a flare that
                // has stopped moving OR we are chasing a flare that has burned out then we can clear and move on
                if(investigateReached() && (!enemy.isInvestigatingFlare() || enemy.getInvestigateFlare().isStuck()
                                || enemy.getInvestigateFlare().timeToBurnout() <= 0)){
                    enemy.setInvestigatePosition(null);
                    enemy.clearInvestigateFlare();
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
    protected void markGoalTiles() {
        switch(state) {
            case IDLE:
                break; // no goal tile

            case CHASE:
                level.setGoal(level.screenToTile(player.getX()), level.screenToTile(player.getY()));
                break;

            case INVESTIGATE:
                level.setGoal(level.screenToTile(enemy.getInvestigatePositionX()),
                        level.screenToTile(enemy.getInvestigatePositionY()));
                break;

            default:
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

    /** Returns whether an enemy is in range to chase a player */
    private boolean withinChase(){
        double distance = cartesianDistance(enemy.getX(),player.getX(),enemy.getY(),player.getY());
        return distance <= player.getLightRadius();
    }

    /** Returns whether an enemy is in range to chase a player */
    private boolean withinFlareRange(FlareModel f){
        double distance = cartesianDistance(enemy.getX(),f.getX(),enemy.getY(),f.getY());
        return distance <= f.getLightRadius();
    }
}
