package com.fallenflame.game.enemies;

import com.badlogic.gdx.Gdx;
import com.fallenflame.game.LevelModel;
import com.fallenflame.game.PlayerModel;

import java.util.List;

public class AIGhostController extends AIController {

    /**
     * Enumeration to encode the finite state machine.
     */
    private enum FSMState {
        /**
         * The enemy does not have a target
         */
        IDLE,
        /**
         * The enemy is chasing the player
         */
        CHASE,
    }

    // Instance Attributes
    /** The enemy's current state*/
    private FSMState state;
    /** The player*/
    private PlayerModel player;
    /** The enemy being controlled by this AIController */
    private EnemyGhostModel enemy;

    /**
     * Creates an AIController for the enemy with the given id.
     *
     * @param id The unique enemy identifier
     * @param level The game level (for pathfinding)
     * @param enemies The list of enemies
     * @param player The player to target
     */
    public AIGhostController(int id, LevelModel level, List<EnemyModel> enemies, PlayerModel player) {
        super(id, level, enemies);
        this.player = player;
        assert(enemy.getClass() == EnemyGhostModel.class);
        this.enemy = (EnemyGhostModel)super.enemy;
        state = FSMState.CHASE;
    }

    /**
     * Change the state of the enemy using a Finite State Machine.
     */
    protected void changeStateIfApplicable() {
        switch (state) {
            case IDLE:
                if (!isBlockedByLight()) {
                    state = FSMState.CHASE;
                }
                break;

            case CHASE:
                enemy.makeAggressive();
                if (isBlockedByLight()) {
                    state = FSMState.IDLE;
                }
                break;

            default:
                Gdx.app.error("AIGhostController", "Impossible state reached", new IllegalArgumentException());
                assert false;
        }
    }

    /**
     * Mark all desirable tiles to move to.
     *
     * This method implements pathfinding through the use of goal tiles.
     */
    protected void markGoalTiles() {
        switch (state) {
            case IDLE:
                break;

            case CHASE:
                level.setGoal(level.screenToTile(player.getX()), level.screenToTile(player.getY()));
                break;
        }
    }

    /**
     * Returns true if the player's light radius blocks the ghost enemy's progression.
     * This is the case when the player's light radius is greater than it's minimum value, and
     * the ghost is within that radius.
     */
    private boolean isBlockedByLight(){
        return (player.getLightRadius() >= player.getMinLightRadius()) &&
                cartesianDistance(enemy.getX(), player.getX(), enemy.getY(), player.getY()) <= player.getLightRadius();
    }
}
