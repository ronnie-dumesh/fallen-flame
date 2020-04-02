package com.fallenflame.game.enemies;

import com.fallenflame.game.CharacterModel;

public abstract class EnemyModel extends CharacterModel {
    // Active status
    protected boolean activated = false;

    // Constants for the control codes
    // We would normally use an enum here, but Java enums do not bitmask nicely
    /** Do not do anything */
    public static final int CONTROL_NO_ACTION  = 0x00;
    /** Move the ship to the left */
    public static final int CONTROL_MOVE_LEFT  = 0x01;
    /** Move the ship to the right */
    public static final int CONTROL_MOVE_RIGHT = 0x02;
    /** Move the ship to the up */
    public static final int CONTROL_MOVE_UP    = 0x04;
    /** Move the ship to the down */
    public static final int CONTROL_MOVE_DOWN  = 0x08;
    /** Fire the ship weapon */
    public static final int CONTROL_FIRE 	   = 0x10;

    /**
     * Gets enemy's active status
     * @return whether this enemy is activated
     */
    public boolean getActivated() {
        return this.activated;
    }

    /**
     * Sets enemy's active status
     * @param activated
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    /**
     * Gets light radius for enemy. MAY BE OVERWRITTEN BY CHILD for different light behavior
     * @return light radius
     */
    public float getLightRadius() {
        return getActivated() ? 1 : 0;
    }

    /**
     * Executes enemy action
     * @param ctrlCode action for enemy to execute. can be left, right, up, down movement or no action
     * @return true if enemy has moved
     */
    public abstract boolean executeAction(int ctrlCode);
}
