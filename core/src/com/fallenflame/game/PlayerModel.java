package com.fallenflame.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.util.JsonAssetManager;

/**
 * Player avatar for the plaform game.
 *
 * Note that the constructor does very little.  The true initialization happens
 * by reading the JSON value.
 */
public class PlayerModel extends CharacterModel {
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
    /** If player is walking (as opposed to sprinting or sneaking) */
    protected boolean walking;

    /**Tint of player light */
    protected Color tint;

    /**Player walk sound */
    protected Sound walkSound;

    /**Player is making walk sound */
    protected boolean playingSound;

    /**
     * Initializes the character via the given JSON value
     *
     * @param json	the JSON subtree defining the player
     */
    public void initialize(JsonValue json, float[] pos) {
        super.initialize(json, pos);
        flareCount = json.get("flarecount").asInt();
        forceWalk = getForce();
        lightRadiusSprint = json.get("sprintlightrad").asInt();
        lightRadiusSneak = json.get("sneaklightrad").asInt();
        minLightRadius = json.get("minlightradius").asInt();
        lightRadius = minLightRadius;
        walking = true;

        float[] tintValues = json.get("tint").asFloatArray();//RGBA
        tint = new Color(tintValues[0], tintValues[1], tintValues[2], tintValues[3]);

        String walkSoundKey = json.get("walksound").asString();
        walkSound = JsonAssetManager.getInstance().getEntry(walkSoundKey, Sound.class);
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

    /**
     * Sets whether player is walking
     * @param b True if walking, False if sprinting or sneaking
     */
    public void setWalking(Boolean b) { walking = b; }

    /**
     * Returns whether player is walking
     * @return True if walking, False if sprinting or sneaking
     */
    public boolean isWalking() { return walking; }

    public void incrementLightRadius(float i) { setLightRadius(lightRadius + i); }

    public boolean isPlayingSound() {return playingSound;}

    public void setPlayingSound(boolean status) {playingSound = status;}

}