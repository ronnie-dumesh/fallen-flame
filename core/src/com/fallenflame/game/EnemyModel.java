package com.fallenflame.game;

import com.badlogic.gdx.math.Vector2;

public class EnemyModel extends CharacterModel {
    protected boolean activated = false;

    protected Vector2 investigatePosition;

    /**
     * Control Codes to encode actions
     */
    public static final int CONTROL_NO_ACTION = 0x00;
    public static final int CONTROL_MOVE_LEFT = 0X01;
    

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
     * Gets light radius for enemy
     * @return light radius
     */
    public float getLightRadius() {
        return getActivated() ? 1 : 0;
    }

    /**
     * Executes enemy action
     * @param action for enemy to execute. can be left, right, up, down movement or no action
     * @return true if enemy has moved
     */
    public boolean executeAction(AIController.Action action) {
        Vector2 tempAngle = new Vector2(); // x: -1 = left, 1 = right, 0 = still; y: -1 = down, 1 = up, 0 = still
        switch(action){
            case NO_ACTION:
                break; // Do not return false immediately, because then the previous movement will not be cleared.
            case LEFT:
                tempAngle.set(-1,0);
                break;
            case RIGHT:
                tempAngle.set(1,0);
                break;
            case UP:
                tempAngle.set(0,1);
                break;
            case DOWN:
                tempAngle.set(0,-1);
                break;
            default:
                System.out.println("invalid enemy action");
                assert false;
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
        return action != AIController.Action.NO_ACTION; // Return false if no action.
    }
}
