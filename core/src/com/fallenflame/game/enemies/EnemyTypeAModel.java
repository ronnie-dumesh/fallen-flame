package com.fallenflame.game.enemies;

import com.badlogic.gdx.math.Vector2;
import com.fallenflame.game.CharacterModel;
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
     * @param x x-coor of enemy's goal
     * @param y y-coor of enemy's goal
     */
    public void setInvestigatePosition(float x, float y) {
        setInvestigatePosition(new Vector2(x, y));
    }

    /**
     * Executes enemy action
     * @param ctrlCode action for enemy to execute. can be left, right, up, down movement or no action
     * @return true if enemy has moved
     */
    public boolean executeAction(int ctrlCode) {
        // Determine how we are moving.
        boolean movingLeft  = (ctrlCode & CONTROL_MOVE_LEFT) != 0;
        boolean movingRight = (ctrlCode & CONTROL_MOVE_RIGHT) != 0;
        boolean movingUp    = (ctrlCode & CONTROL_MOVE_UP) != 0;
        boolean movingDown  = (ctrlCode & CONTROL_MOVE_DOWN) != 0;
        boolean movingDownLeft = (ctrlCode & CONTROL_MOVE_DOWN_LEFT) != 0;
        boolean movingDownRight = (ctrlCode & CONTROL_MOVE_DOWN_RIGHT) != 0;
        boolean movingUpLeft = (ctrlCode & CONTROL_MOVE_UP_LEFT) != 0;
        boolean movingUpRight = (ctrlCode & CONTROL_MOVE_UP_RIGHT) != 0;

        Vector2 tempAngle = new Vector2(); // x: - = left, + = right, 0 = still; y: - = down, + = up, 0 = still
        if(movingLeft) {
            tempAngle.set(-1, 0);
        } else if(movingRight) {
            tempAngle.set(1,0);
        } else if(movingUp) {
            tempAngle.set(0,1);
        } else if(movingDown) {
            tempAngle.set(0,-1);
        } else if(movingDownLeft) {
            tempAngle.set(-0.707f, -0.707f);
        } else if(movingDownRight) {
            tempAngle.set(0.707f, -0.707f);
        } else if(movingUpLeft) {
            tempAngle.set(-0.707f, .707f);
        } else if(movingUpRight) {
            tempAngle.set(0.707f, 0.707f);
        }

        tempAngle.scl(getForce());
        setMovement(tempAngle.x, tempAngle.y);
        // Only set angle if our temp angle is not 0. If temp angle is 0 then it means no movement, in which case leave
        // the current facing angle of the enemy as-is.
        if (!tempAngle.isZero()) {
            float angle = tempAngle.angle();
            // Convert to radians with up as 0
            angle = (float) Math.PI * (angle - 90.0f) / 180.0f;
            setAngle(angle);
        }
        applyForce();
        return ctrlCode != CONTROL_NO_ACTION; // Return false if no action.
    }
}
