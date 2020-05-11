package com.fallenflame.game.enemies;

import com.fallenflame.game.FlareModel;
import com.fallenflame.game.LevelModel;
import com.fallenflame.game.PlayerModel;

import java.util.HashSet;
import java.util.List;

public class AITypeBController extends AIController {

    /**
     * Enumeration to encode the finite state machine.
     */
    private enum FSMState {
        /** The enemy does not have a target */
        IDLE,
        /** The enemy is firing directly at the player or flare */
        DIRECT_FIRE,
        /** The enemy is firing at the player's last known position */
        SUSTAINED_FIRE,
    }

    // Constants
    private static final int SUSTAINED_FIRE_TIME = 2000;

    // Instance Attributes
    /** The enemy's current state*/
    private FSMState state;
    /** The enemy being controlled by this AIController */
    private EnemyTypeBModel enemy;
    /** Flares */
    List<FlareModel> flares;
    /** How long enemy has been in sustained fire */
    private int firingTime;
    /** Behavior only guaranteed in DIRECT_FIRE state
     * True if firing at flare, false if firing at player */
    private boolean firingAtFlare;
    /** Target flare */
    private FlareModel targetFlare;
    /** True if already fired once at flare stuck to wall */
    private boolean firedWall;

    /**
     * Creates an AIController for the enemy with the given id.
     *
     * @param id The unique enemy identifier
     * @param level The game level (for pathfinding)
     * @param enemies The list of enemies
     * @param player The player to target
     */
    public AITypeBController(int id, LevelModel level, List<EnemyModel> enemies,
                             PlayerModel player, List<FlareModel> flares) {
        super(id, level, enemies, player);
        this.player = player;
        this.flares = flares;
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
                // Check for flare targets -- FIRST because flares are prioritized
                for(FlareModel f : flares) {
                    if(withinFlareRange(f) && !f.isStuck()){
                        firingAtFlare = true;
                        firedWall = false;
                        targetFlare = f;
                        enemy.setFiringTarget(f.getX(), f.getY());
                        state = FSMState.DIRECT_FIRE;
                        break;
                    }
                }
                // Check for player target within range
                if(withinPlayerLight()) {
                    firingAtFlare = false;
                    enemy.setFiringTarget(player.getX(), player.getY());
                    state = FSMState.DIRECT_FIRE;
                    break;
                }
                break;
            case DIRECT_FIRE:
                enemy.makeAggressive();
                if(firingAtFlare){
                    enemy.setFiringTarget(targetFlare.getX(), targetFlare.getY());
                    // If flare now out of stuck to wall, stop firing
                    if(targetFlare.isStuck()){
                        if(!firedWall)
                            firedWall = true;
                        else{
                            // if already fired at wall once, stop firing
                            targetFlare = null;
                            state = FSMState.IDLE;
                            break;
                        }
                    }
                }
                else{
                    enemy.setFiringTarget(player.getX(), player.getY());
                    // If player now out of range, switch to sustained fire at last known position
                    if(!withinPlayerLight()) {
                        state = FSMState.SUSTAINED_FIRE;
                        firingTime = 0;
                        break;
                    }
                }
            case SUSTAINED_FIRE:
                enemy.makeAlert();
                // Check for flare targets -- FIRST because flares are prioritized
                for(FlareModel f : flares) {
                    if(withinFlareRange(f) && !f.isStuck()){
                        firingAtFlare = true;
                        targetFlare = f;
                        enemy.setFiringTarget(f.getX(), f.getY());
                        state = FSMState.DIRECT_FIRE;
                        break;
                    }
                }
                // Check for player target within range
                if(withinPlayerLight()) {
                    enemy.setFiringTarget(player.getX(), player.getY());
                    state = FSMState.DIRECT_FIRE;
                    break;
                }
                // Check if sustained fire has ended
                if(firingTime >= SUSTAINED_FIRE_TIME){
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
    protected boolean markGoalTiles() { return false; }

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

}
