package com.fallenflame.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.util.FilmStrip;
import com.fallenflame.game.util.JsonAssetManager;

/**
 * Player avatar for the plaform game.
 *
 * Note that the constructor does very little.  The true initialization happens
 * by reading the JSON value.
 */
public class PlayerModel extends CharacterModel {
    /** Player movement types enum */
    private enum MovementState {
        WALK,
        SNEAK,
        SPRINT
    }
    /** How the player is currently moving */
    private MovementState move;

    /** Number of flares the player can have on the screen at once */
    private int flareCount;
    /** Player's force when moving at standard walk speed */
    protected float forceWalk;
    /** Radius of player's light */
    protected float lightRadius;
    protected float minLightRadius;
    protected float lightRadiusSaved;
    protected float lightRadiusSprint;
    protected float lightRadiusSneak;
    /** Player sneak left (once hits 0, a ghost is deployed on the map) Sneakval must be greater than or equal to 0 */
    protected int sneakVal;

    /**Tint of player light */
    protected Color tint;

    /**Player walk sound */
    protected Sound walkSound;

    /**Player is making walk sound */
    protected boolean playingSound;

    /** Fire buddy filmstrip*/
    private FilmStrip fireBuddyFilmstrip;
    private FilmStrip fireBuddyLeft;
    private FilmStrip fireBuddyRight;
    private FilmStrip fireBuddyUp;
    private FilmStrip fireBuddyDown;

    /** Origin of fire buddy when drawing not in sneak mode */
    protected Vector2 fireBuddyOrigin;

    /** Origin of drawing for fire buddy when player is in sneak mode */
    protected Vector2 fireBuddySneak;

    /**
     * Initializes the character via the given JSON value
     *
     * @param json	the JSON subtree defining the player
     */
    public void initialize(JsonValue json, float[] pos, int startSneakVal) {
        super.initialize(json, pos);
        flareCount = json.get("flarecount").asInt();
        forceWalk = getForce();
        lightRadiusSprint = json.get("sprintlightrad").asInt();
        lightRadiusSneak = json.get("sneaklightrad").asInt();
        minLightRadius = json.get("minlightradius").asInt();
        sneakVal = startSneakVal;
        lightRadius = minLightRadius;
        move = MovementState.WALK;

        float[] tintValues = json.get("tint").asFloatArray();//RGBA
        tint = new Color(tintValues[0], tintValues[1], tintValues[2], tintValues[3]);

        String walkSoundKey = json.get("walksound").asString();
        walkSound = JsonAssetManager.getInstance().getEntry(walkSoundKey, Sound.class);
    }

    public void initialize(JsonValue json, float[] pos) {
        initialize(json, pos, json.get("defaultStartSneakVal").asInt());
    }

    @Override
    public void initializeTextures(JsonValue json){
        super.initializeTextures(json);

        JsonValue firebuddy = json.get("firebuddy");

        JsonValue textureJson = firebuddy.get("texture");

        String key = textureJson.get("left").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            fireBuddyLeft = (FilmStrip) texture;
        } catch (Exception e) {
            fireBuddyLeft = null;
        }

        key = textureJson.get("right").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            fireBuddyRight = (FilmStrip) texture;
        } catch (Exception e) {
            fireBuddyRight = null;
        }

        key = textureJson.get("down").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            fireBuddyDown = (FilmStrip) texture;
        } catch (Exception e) {
            fireBuddyDown = null;
        }

        key = textureJson.get("up").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            fireBuddyUp = (FilmStrip) texture;
        } catch (Exception e) {
            fireBuddyUp = null;
        }

        //pick default direction
        fireBuddyFilmstrip = fireBuddyRight;

        float offsetX = firebuddy.get("textureoffset").get("x").asFloat();
        float offsetY = firebuddy.get("textureoffset").get("y").asFloat();
        //set fire buddy origin;
        setFireBuddyOrigin (((TextureRegion)fireBuddyFilmstrip).getRegionWidth()/2.0f + offsetX * drawScale.x,
                                ((TextureRegion)fireBuddyFilmstrip).getRegionHeight()/2.0f + offsetY * drawScale.y);

        offsetX = firebuddy.get("sneaktextureoffset").get("x").asFloat();
        offsetY = firebuddy.get("sneaktextureoffset").get("y").asFloat();
        //set fire buddy sneak origin
        setFireBuddySneak(((TextureRegion)fireBuddyFilmstrip).getRegionWidth()/2.0f + offsetX * drawScale.x,
                ((TextureRegion)fireBuddyFilmstrip).getRegionHeight()/2.0f + offsetY * drawScale.y);
    }

    /**
     * @return the origin of the firebuddy texture
     * as a Vector 2
     */
    protected Vector2 getFireBuddyOrigin() { return new Vector2(fireBuddyOrigin); }

    /**
     * @return the origin of the firebuddy texture
     * along the x-axis
     */
    protected float getFireBuddyOriginX() { return fireBuddyOrigin.x; }

    /**
     * @return the origin of the firebuddy texture
     * along the y-axis
     */
    protected float getFireBuddyOriginY() {return fireBuddyOrigin.y; }

    /**
     * Sets the origin of the fire buddy to be (x, y)
     * The y-axis is positive downwards, and the x-axis is positive rightwards
     *
     * @param x the offset of the texture on the x-axis
     * @param y the offset of the texture on the y-axis
     */
    protected void setFireBuddyOrigin(float x, float y){
        fireBuddyOrigin = new Vector2(x, y);
    }

    /**
     * @return the origin of the firebuddy texture when sneaking
     * as a Vector 2
     */
    protected Vector2 getFireBuddySneak() { return new Vector2(fireBuddySneak); }

    /**
     * @return the origin of the firebuddy texture when sneaking
     * along the x-axis
     */
    protected float getFireBuddySneakX() { return fireBuddySneak.x; }

    /**
     * @return the origin of the firebuddy texture when sneaking
     * along the y-axis
     */
    protected float getFireBuddySneakY() {return fireBuddySneak.y; }

    /**
     * Sets the origin of the fire buddy to be (x, y) when sneaking
     * The y-axis is positive downwards, and the x-axis is positive rightwards
     *
     * @param x the offset of the texture on the x-axis
     * @param y the offset of the texture on the y-axis
     */
    protected void setFireBuddySneak(float x, float y){
        fireBuddySneak = new Vector2(x, y);
    }


    /**
     * Returns the minimum light radius the player can have
     *
     * @return minimum light radius
     */
    public float getMinLightRadius() { return minLightRadius; }

    /**
     * Returns the number of flares the player can have on the screen at once
     *
     * @return the number of flares the player can have on the screen at once
     */
    public int getFlareCount() {
        return flareCount;
    }

    /**
     * Gets player light radius
     * @return light radius
     */
    public float getLightRadius() {
        return lightRadius;
    }

    /** Get amount of sneak updates left for player */
    public int getSneakVal() { return sneakVal; }

    /** Decrement sneak value by 1 (for 1 update of sneaking) */
    public void decSneakVal() { sneakVal--; }

    /**
     * Gets player color tint
     * @return light color
     */
    public Color getLightColor() {
        return tint;
    }

    /**
     * Sets player light radius (does not include sneak speed)
     * @param r light radius
     */
    public void setLightRadius(float r) {
        lightRadius = Math.max(r, minLightRadius);
    }

    /**
     * Sets player to sneak light radius (not reachable by scrolling)
     */
    public void setLightRadiusSneak() { lightRadius = lightRadiusSneak; }

    /**
     * Increments light radius by i (can be positive or negative) ensuring lightRadius is never less than 0.
     * @param i value to increment radius by
     */

    /**
     * Gets player force for sneaking
     * @return player force for sneaking
     */
    public float getForceSneak() {
        return getForceWalk()/2;
    }

    /**
     * Gets player force for sprinting
     * @return player force for sprinting
     */
    public float getForceSprint() {
        return getForceWalk()*2;
    }

    /**
     * Gets player force for walking
     * @return player force for walking
     */
    public float getForceWalk() {
        return forceWalk;
    }

    /**
     * Returns the walk sound
     *
     * @return the walk sound
     */
    public Sound getWalkSound() {
        return walkSound;
    }

    /**
     * Gets player light radius when not sprinting or sneaking
     * @return light radius
     */
    public float getLightRadiusSaved() {
        return lightRadiusSaved;
    }

    /**
     * Sets player light radius when not sprinting or sneaking
     * @param r light radius
     */
    public void setLightRadiusSaved(float r) {
        lightRadiusSaved = r;
    }

    /**
     * Gets player light radius when sprinting
     * @return light radius
     */
    public float getLightRadiusSprint() {
        return lightRadiusSprint;
    }

    /** Sets player as walking */
    public void setWalking() { move = MovementState.WALK; }
    /** Sets player as sneaking */
    public void setSneaking() { move = MovementState.SNEAK; }
    /** Sets player as sprinting */
    public void setSprinting() { move = MovementState.SPRINT; }

    /**
     * Returns whether player is walking
     * @return True if walking, False if sprinting or sneaking
     */
    public boolean isWalking() { return move == MovementState.WALK; }

    /**
     * Return True if player is sneaking
     * @return True if sneaking, False if sprinting or walking
     */
    public boolean isSneaking() { return move == MovementState.SNEAK; }

    public void incrementLightRadius(float i) { setLightRadius(lightRadius + i); }

    public boolean isPlayingSound() {return playingSound;}

    public void setPlayingSound(boolean status) {playingSound = status;}

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt Number of seconds since last animation frame
     */
    @Override
    public void update(float dt) {
        //getAngle has up as 0 radians, down as pi radians, pi/2 is left, -pi/2 is right.
        double angle = getAngle();
        if(angle < 0) angle = angle + 2 * Math.PI;
        int angle100 = (int) (angle * 100);

        if(angle100 == 0){
            fireBuddyFilmstrip = fireBuddyUp;
        } else if (angle100 > 0 && angle100 < 314){
            fireBuddyFilmstrip = fireBuddyLeft;
        } else if (angle100 == 314){
            fireBuddyFilmstrip = fireBuddyDown;
        } else {
            fireBuddyFilmstrip = fireBuddyRight;
        }

        // Animate if necessary
        // Do not change values of walkCool and animate, to be done in parent.
        if (animate && walkCool == 0 && fireBuddyFilmstrip != null ) {
            int next = (fireBuddyFilmstrip.getFrame()+1) % fireBuddyFilmstrip.getSize();
            fireBuddyFilmstrip.setFrame(next);
        } else if (!animate && fireBuddyFilmstrip != null) {
            fireBuddyFilmstrip.setFrame(startFrame);
        }
        super.update(dt);
    }

    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        if (fireBuddyFilmstrip != null) {
            canvas.draw(fireBuddyFilmstrip, Color.WHITE,
                    isSneaking() ? getFireBuddySneakX() : getFireBuddyOriginX(),
                    isSneaking() ? getFireBuddySneakY() : getFireBuddyOriginY(),
                    getX()*drawScale.x,getY()*drawScale.y,0 ,1,1);
        }
    }
}