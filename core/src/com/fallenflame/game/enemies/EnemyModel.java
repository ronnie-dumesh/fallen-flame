package com.fallenflame.game.enemies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.fallenflame.game.CharacterModel;
import com.fallenflame.game.util.JsonAssetManager;

public abstract class EnemyModel extends CharacterModel {

    private enum ActivationStates {
        Calm,
        Alert,
        Aggressive
    }

    private ObjectMap<ActivationStates, Color> stateTints = new ObjectMap<>();

    /**Enemy move sound */
    private Sound moveSound;

    /**Enemy constant sound */
    private Sound constantSound;

    /**ID of enemy move sound*/
    protected long moveSoundID;

    /**ID of enemy constant sound*/
    protected long constantSoundID;

    // Active status
    protected ActivationStates state = ActivationStates.Calm;

    // Constants for the control codes
    // We would normally use an enum here, but Java enums do not bitmask nicely
    /** Do not do anything */
    public static final int CONTROL_NO_ACTION  = 0x00;
    /** Move the enemy to the left */
    public static final int CONTROL_MOVE_LEFT  = 0x01;
    /** Move the enemy to the right */
    public static final int CONTROL_MOVE_RIGHT = 0x02;
    /** Move the enemy to the up */
    public static final int CONTROL_MOVE_UP    = 0x04;
    /** Move the enemy to the down */
    public static final int CONTROL_MOVE_DOWN  = 0x08;
    /** Move the enemy to the down and left */
    public static final int CONTROL_MOVE_DOWN_LEFT = 0x10;
    /** Move the enemy to the down and right */
    public static final int CONTROL_MOVE_DOWN_RIGHT = 0x20;
    /** Move the enemy to the up and left */
    public static final int CONTROL_MOVE_UP_LEFT = 0x40;
    /** Move the enemy to the up and right */
    public static final int CONTROL_MOVE_UP_RIGHT = 0x80;
    /** Command the enemy to shoot */
    public static final int CONTROL_SHOOT = 0x100;

    /**
     * Initializes the enemy via the given JSON value
     *
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the enemy subtree
     *
     * @param json	the JSON subtree defining the enemy
     */
    public void initialize(JsonValue json, float[] pos) {
        super.initialize(json, pos);

        String moveSoundKey = json.get("movesound").asString();
        moveSound = JsonAssetManager.getInstance().getEntry(moveSoundKey, Sound.class);
        moveSoundID = -1;

        String constantSoundKey = json.get("constantsound").asString();
        constantSound = JsonAssetManager.getInstance().getEntry(constantSoundKey, Sound.class);
        constantSoundID = -1;

        for(ActivationStates state : ActivationStates.values()){
            String stateName = state.name().toLowerCase();
            float[] tintValues = json.get("statetints").get(stateName).asFloatArray();//RGBA
            Color tint = new Color(tintValues[0], tintValues[1], tintValues[2], tintValues[3]);
            stateTints.put(state, tint);
        }
    }

    /**
     * @return whether enemy is aggressive
     */
    public boolean isAgressive() {
        return this.state.equals(ActivationStates.Aggressive);
    }

    /**
     * @return whether enemy is alert
     */
    public boolean isAlert() {
        return this.state.equals(ActivationStates.Alert);
    }

    /**
     * @return whether enemy is calm
     */
    public boolean isCalm() {
        return this.state.equals(ActivationStates.Calm);
    }

    /**
     * @return false if the enemy is calm and true otherwise
     */
    public boolean isActivated() {
        return !isCalm();
    }

    /**
     *  Sets the enemy's activation state to calm
     */
    public void makeCalm() {
        this.state = ActivationStates.Calm;
    }

    /**
     *  Sets the enemy's activation state to alert
     */
    public void makeAlert() {
        this.state = ActivationStates.Alert;
    }

    /**
     * Sets the enemy's activation state to aggressive
     */
    public void makeAggressive() {
        this.state = ActivationStates.Aggressive;
    }

    /**
     * Gets light radius for enemy. MAY BE OVERWRITTEN BY CHILD for different light behavior
     * @return light radius
     */
    public float getLightRadius() {
        return isActivated() ? 1.0f : 0.0f;
    }

    /**
     * Gets the tint to color the enemy. MAY BE OVERWRITTEN BY CHILD for different light behavior
     * @return light color
     */
    public Color getLightColor() {
        return stateTints.get(state);
    }

    /**
     * Returns the move sound
     *
     * @return the move sound
     */
    public Sound getMoveSound() {
        return moveSound;
    }

    public long getMoveSoundID() {return moveSoundID;}

    public void setMoveSoundID(long id) {moveSoundID = id;}

    /**
     * Returns the constant sound
     *
     * @return the constant sound
     */
    public Sound getConstantSound() {
        return constantSound;
    }

    public long getConstantSoundID() {return constantSoundID;}

    public void setConstantSoundID(long id) {constantSoundID = id;}

    /**
     * Executes enemy action
     * @param ctrlCode action for enemy to execute. can be left, right, up, down movement or no action
     * @return true if enemy has moved
     */
    public abstract boolean executeAction(int ctrlCode);
}
