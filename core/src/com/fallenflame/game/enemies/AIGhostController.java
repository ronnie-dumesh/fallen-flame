package com.fallenflame.game.enemies;

import com.badlogic.gdx.Gdx;
import com.fallenflame.game.LevelModel;
import com.fallenflame.game.PlayerModel;

import java.util.List;

public class AIGhostController extends AIController {

    /** Margin of error for movement (to prevent overly finicky movement) */
    private static float MARGIN = .2f;

    // Instance Attributes
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
        super(id, level, enemies, player);
        this.player = player;
        assert(enemy.getClass() == EnemyGhostModel.class);
        this.enemy = (EnemyGhostModel)super.enemy;
    }

    /**
     * Change the state of the enemy using a Finite State Machine. Ghost is always in chase mode
     */
    protected void changeStateIfApplicable() { }

    /**
     * Ghost enemy always moves directly towards player because it can move through walls
     * @return ctrlCode to move towards player
     */
    @Override
    protected int getMoveAlongPathToGoalTile() {
        // We need to move right
        if(player.getPosition().x - enemy.getPosition().x - MARGIN > 0) {
            // Move up
            if(player.getPosition().y - enemy.getPosition().y - MARGIN > 0)
                return EnemyModel.CONTROL_MOVE_UP_RIGHT;
            // Move down
            else if(player.getPosition().y - enemy.getPosition().y + MARGIN < 0)
                return EnemyModel.CONTROL_MOVE_DOWN_RIGHT;
            // No lateral movement
            else
                return EnemyModel.CONTROL_MOVE_RIGHT;
        }
        // Move left
        else if(player.getPosition().x - enemy.getPosition().x + MARGIN < 0)  {
            // Move up
            if(player.getPosition().y - enemy.getPosition().y - MARGIN > 0)
                return EnemyModel.CONTROL_MOVE_UP_LEFT;
            // Move down
            else if(player.getPosition().y - enemy.getPosition().y + MARGIN < 0)
                return EnemyModel.CONTROL_MOVE_DOWN_LEFT;
            // No lateral movement
            else
                return EnemyModel.CONTROL_MOVE_LEFT;
        }
        // No horizontal movement
        else {
            // Move up
            if(player.getPosition().y - enemy.getPosition().y - MARGIN > 0)
                return EnemyModel.CONTROL_MOVE_UP;
            // Move down
            else if(player.getPosition().y - enemy.getPosition().y + MARGIN < 0)
                return EnemyModel.CONTROL_MOVE_DOWN;
            // No lateral movement
            else
                return EnemyModel.CONTROL_NO_ACTION;
        }
    }

    /**
     * Mark all desirable tiles to move to.
     *
     * This method implements pathfinding through the use of goal tiles.
     */
    protected void markGoalTiles() {
        // Would be required if we switch to normal pathfinding:
        // level.setGoal(level.screenToTile(player.getX()), level.screenToTile(player.getY()));
    }
}
