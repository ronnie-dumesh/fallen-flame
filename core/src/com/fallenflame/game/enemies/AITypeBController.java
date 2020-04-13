package com.fallenflame.game.enemies;

import com.fallenflame.game.LevelModel;
import com.fallenflame.game.PlayerModel;

import java.util.List;

public class AITypeBController extends AIController {

    /**
     * Enumeration to encode the finite state machine.
     */
    private enum FSMState {
        /** The enemy does not have a target */
        IDLE,
        /** The enemy is firing directly at the player */
        DIRECT_FIRE,
        /** The enemy is firing at the player's last known position */
        SUSTAINED_FIRE,
    }

    // Constants
    private static final int SUSTAINED_FIRE_TIME = 2000;

    // Instance Attributes
    /** The enemy's current state*/
    private FSMState state;
    /** The player*/
    private PlayerModel player;
    /** The enemy being controlled by this AIController */
    private EnemyTypeBModel enemy;
    /** How long enemy has been in sustained fire */
    private int firingTime;

    /**
     * Creates an AIController for the enemy with the given id.
     *
     * @param id The unique enemy identifier
     * @param level The game level (for pathfinding)
     * @param enemies The list of enemies
     * @param player The player to target
     */
    public AITypeBController(int id, LevelModel level, List<EnemyModel> enemies, PlayerModel player) {
        super(id, level, enemies);
        this.player = player;
        assert(enemy.getClass() == EnemyTypeBModel.class);
        this.enemy = (EnemyTypeBModel)super.enemy;
        state = FSMState.IDLE;
    }

    /**
     * Change the state of the enemy using a Finite State Machine.
     */
    protected void changeStateIfApplicable() {
        switch(state) {
            case IDLE:
                enemy.makeCalm();
                // Check for player target within range
                if(withinRange()) {
                    enemy.setFiringTarget(player.getX(), player.getY());
                    state = FSMState.DIRECT_FIRE;
                    break;
                }
                break;
            case DIRECT_FIRE:
                enemy.makeAggressive();
                enemy.setFiringTarget(player.getX(), player.getY());
                // If player now out of range, switch to sustained fire at last known position
                if(!withinRange()) {
                    state = FSMState.SUSTAINED_FIRE;
                    firingTime = 0;
                    break;
                }
                break;
            case SUSTAINED_FIRE:
                enemy.makeAlert();
                // Check for player target within range
                if(withinRange()) {
                    state = FSMState.DIRECT_FIRE;
                    break;
                }
                // Check if sustained fire has ended
                else if(firingTime >= SUSTAINED_FIRE_TIME){
                    state = FSMState.IDLE;
                    break;
                }
                firingTime++;
        }
    }

    /**
     * Mark all desirable tiles to move to.
     *
     * This method implements pathfinding through the use of goal tiles.
     */
    protected void markGoalTiles() { }

    /**
     * Get enemy movement toward goal
     *
     * @return a movement direction that moves towards a goal tile or NO_ACTION.
     */
    protected int getMoveAlongPathToGoalTile() { return EnemyModel.CONTROL_NO_ACTION; }

    /**
     * Return firing action code if enemy is firing
     */
    @Override
    protected int getOtherAction() {
        if(state == FSMState.DIRECT_FIRE || state == FSMState.SUSTAINED_FIRE)
            return EnemyModel.CONTROL_FIRE;
        return EnemyModel.CONTROL_NO_ACTION;
    }

    /** Returns whether an enemy is in range to chase a player */
    private boolean withinRange(){
        double distance = cartesianDistance(enemy.getX(),player.getX(),enemy.getY(),player.getY());
        return distance <= player.getLightRadius();
    }
}
