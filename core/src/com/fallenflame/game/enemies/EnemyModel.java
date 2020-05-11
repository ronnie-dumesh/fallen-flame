package com.fallenflame.game.enemies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.fallenflame.game.CharacterModel;
import com.fallenflame.game.GameCanvas;
import com.fallenflame.game.util.JsonAssetManager;

public abstract class EnemyModel extends CharacterModel {

    /**
     * Calm: enemy is not activated (will patrol if subtype pathing)
     * Alert: investigating (either flare or player last known position)
     * Pause: just activated, about to transition to aggressive
     * Aggressive: actively chasing player
     */
    private enum ActivationStates {
        Calm,
        Alert,
        Pause,
        Aggressive
    }

    /** Time left pausing */
    private float pauseTime;
    /** Time enemy will pause before chasing h*/
    private float totalPauseTime;
    /** Exclamation mark texture */
    private TextureRegion exclamationMark;
    /** Exclamation mark origin x value */
    private float excOriginX;
    /** Exclamation mark origin y value */
    private float excOriginY;

    private ObjectMap<ActivationStates, Color> stateTints = new ObjectMap<>();

    /**Enemy active sound (Acquired from https://freesound.org/people/000600/sounds/180015/)*/
    private Sound activeSound;
    /**Enemy constant sound (Acquired from https://freesound.org/people/ecfike/sounds/132865/)*/
    private Sound constantSound;
    /**ID of enemy active sound*/
    protected long activeSoundID;
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
    public static final int CONTROL_FIRE = 0x1000;

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

        String activeSoundKey = json.get("activesound").asString();
        activeSound = JsonAssetManager.getInstance().getEntry(activeSoundKey, Sound.class);
        activeSoundID = -1;

        String constantSoundKey = json.get("constantsound").asString();
        constantSound = JsonAssetManager.getInstance().getEntry(constantSoundKey, Sound.class);
        constantSoundID = -1;

        for(ActivationStates state : ActivationStates.values()){
            String stateName = state.name().toLowerCase();
            float[] tintValues = json.get("statetints").get(stateName).asFloatArray();//RGBA
            Color tint = new Color(tintValues[0], tintValues[1], tintValues[2], tintValues[3]);
            stateTints.put(state, tint);
        }

        // Get the pause time and exclamation mark texture (in an if statement because of an implicit promise that an enemy
        // will have this json field if it is to use PAUSE state and pauseTime)
        if(json.has("totalPauseTime")){
            totalPauseTime = json.get("totalPauseTime").asInt();
            pauseTime = totalPauseTime;
            String key = json.get("exclamationTexture").asString();
            exclamationMark = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
            float offsetX = json.get("exclamationTextureOffset").get("x").asFloat();
            excOriginX = exclamationMark.getRegionWidth()/2.0f + offsetX * drawScale.x;
            float offsetY = json.get("exclamationTextureOffset").get("y").asFloat();
            excOriginY = exclamationMark.getRegionHeight()/2.0f + offsetY * drawScale.x;
        }

    }

    /**
     * @return decrements pausetime and returns true if it is less than 0
     */
    public boolean isFinishedPausing() { return pauseTime-- <= 0; }

    /**
     * Resets pause time for next potential pause
     */
    public void resetPause() { pauseTime = totalPauseTime; }

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
     * Sets the enemy's activation status to paused
     */
    public void makePause(){ this.state = ActivationStates.Pause; }

    /**
     * Gets light radius for enemy. MAY BE OVERWRITTEN BY CHILD for different light behavior
     * @return light radius
     */
    public float getLightRadius() {
        if(state == ActivationStates.Pause)
            return 4.0f; // larger light radius to see exclamation mark
        return isActivated() ? 1.5f : 0.0f;
    }

    /**
     * Gets the tint to color the enemy. MAY BE OVERWRITTEN BY CHILD for different light behavior
     * @return light color
     */
    public Color getLightColor() {
        return new Color(255,255,255,1);
    }

    /**
     * Returns the active sound
     *
     * @return the active sound
     */
    public Sound getActiveSound() {
        return activeSound;
    }

    public long getActiveSoundID() {return activeSoundID;}

    public void setActiveSoundID(long id) {activeSoundID = id;}

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
     * Executes enemy movement action
     * @param ctrlCode action for enemy to execute. can be left, right, up, down movement or no action
     * @return true if enemy has moved
     */
    public boolean executeMovementAction(int ctrlCode){
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

        move(tempAngle);
        return ctrlCode != CONTROL_NO_ACTION; // Return false if no action.
    }

    /**
     * Draws explanation mark over enemy if in "pause" state
     * @param canvas Drawing context
     */
    @Override
    public void draw(GameCanvas canvas) {
        if(state == ActivationStates.Pause){
            // Draw exclamation mark
            canvas.draw(exclamationMark, Color.WHITE, excOriginX, excOriginY,
                    getX()*drawScale.x,getY()*drawScale.y,0 ,1,1);
        }
        super.draw(canvas);
    }
}
