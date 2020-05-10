package com.fallenflame.game.enemies;

import com.badlogic.gdx.math.Vector2;
import com.fallenflame.game.FlareModel;

public class EnemyTypeAModel extends EnemyModel {

    /** Position to investigate. Player last known location or flare */
    protected Vector2 investigatePosition;

    /** Flare to investigate (null if investigating player last known location) */
    protected FlareModel investigateFlare;

    /**
     * @return the Vector2 representing the position the enemy seeks to investigate
     */
    public Vector2 getInvestigatePosition() {
        if (investigatePosition == null) {
            return null;
        } else {
            return this.investigatePosition.cpy();
        }
    }

    /**
     * @return the y coordinate of the position the enemy seeks to investigate
     */
    public float getInvestigatePositionY() {
        return this.investigatePosition.y;
    }

    /**
     * @return the x coordinate of the enemy's goal state
     */
    public float getInvestigatePositionX() {
        return this.investigatePosition.x;
    }

    /**
     * Set flare enemy is investigating
     * @param f flare to investigate
     */
    public void setInvestigateFlare(FlareModel f) {
        investigateFlare = f;
    }

    /**
     * Get flare enemy is investigating
     * @return flare to investigate
     */
    public FlareModel getInvestigateFlare() { return investigateFlare; }

    /**
     * Whether enemy is investigating flare
     * @return true if enemy is investigating flare
     */
    public boolean isInvestigatingFlare() { return investigateFlare != null; }

    /**
     * Clear enemy flare investigating
     */
    public void clearInvestigateFlare() { investigateFlare = null; }

    /**
     * Set enemy's investigation position
     * @param v Vector representing enemy's investigation position
     */
    public void setInvestigatePosition(Vector2 v) {
        this.investigatePosition = v;
    }

    /**
     * Set enemy's investigation position
     * @param x x-coor of enemy's investigation position
     * @param y y-coor of enemy's investigation position
     */
    public void setInvestigatePosition(float x, float y) {
        setInvestigatePosition(new Vector2(x, y));
    }

}
