package com.fallenflame.game.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

public class EnemyTypeBModel extends EnemyModel{
    /** Cooldown length */
    private int cooldownLength;
    /** Position to sustain fire towards. Player last known location */
    protected Vector2 firingTarget;
    /** The number of frames until we can fire again */
    private int firecool;

    public void initialize(JsonValue json, float[] pos){
        super.initialize(json, pos);
        cooldownLength = json.get("cooldown").asInt();
    }

    /**
     * Set enemy's firing target position
     * @param v Vector representing enemy's firing target position
     */
    public void setFiringTarget(Vector2 v) {
        firingTarget = v;
    }

    /**
     * Set enemy's firing target position
     * @param x x-coor of enemy's target
     * @param y y-coor of enemy's target
     */
    public void setFiringTarget(float x, float y) {
        setFiringTarget(new Vector2(x, y));
    }

    /**
     * Get enemy's firing target
     * @return Vector2 target coordinates
     */
    public Vector2 getFiringTarget() { return firingTarget; }

    /**
     * Returns whether an enemy can fire. Default implementation returns false.
     * For enemies that can fire, override and return true if not in cooldown
     * @return boolean canFire
     */
    public boolean canFire() {
        return firecool <= 0;
    }

    /**@author Walker White
     * Reset or cool down the enemy weapon.
     *
     * If flag is true, the weapon will cool down by one animation frame.  Otherwise
     * it will reset to its maximum cooldown.
     *
     * @param flag whether to cooldown or reset
     */
    public void coolDown(boolean flag) {
        if (flag && firecool > 0) {
            firecool--;
        } else if (!flag) {
            firecool = cooldownLength;
        }
    }
}
