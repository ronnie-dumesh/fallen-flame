package com.fallenflame.game;

import com.badlogic.gdx.math.Vector2;

public class EnemyModel extends CharacterModel {
    protected boolean activated = false;

    protected Vector2 goal;

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
        this.activated = true;
    }

    /**
     * @return the Vector2 representing the enemy's goal sate
     */
    public Vector2 getGoal() {
        return this.goal.cpy();
    }

    /**
     * @return the y coordinate of the enemy's goal state
     */
    public float getGoalY() {
        return this.goal.y;
    }

    /**
     * @return the x coordinate of the enemy's goal state
     */
    public float getGoalX() {
        return this.goal.x;
    }

    /**
     * Set enemy's goal position
     * @param v Vector representing enemy's goal position
     */
    public void setGoal(Vector2 v) {
        this.goal = v;
    }

    /**
     * Set enemy's goal position
     * @param x x-coor of enemy's goal
     * @param y y-coor of enemy's goal
     */
    public void setGoal(float x, float y) {
        setGoal(new Vector2(x, y));
    }

    /**
     * @param v is a vector with a x and y attributes
     * @return whether given coordinate is equal to the enemy's current goal
     */
    public boolean isGoal(Vector2 v){
        return this.goal.equals(v);
    }

    /**
     * @param x x-coor of position to check
     * @param y y-coor of position to check
     * @return whether given coordinate is equal to the enemy's current goal
     */
    public boolean isGoal(float x, float y){
        return isGoal(new Vector2(x, y));
    }

    /** Sets the goal of the enemy to be equal to its current position */
    public void clearGoal() {
        setGoal(new Vector2(getX(), getY()));
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
                return false;
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
        float angle = tempAngle.angle();
        // Convert to radians with up as 0
        angle = (float)Math.PI*(angle-90.0f)/180.0f;
        setAngle(angle);
        applyForce();
        return true;
    }
}
