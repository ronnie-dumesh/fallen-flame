package com.fallenflame.game;

import com.badlogic.gdx.utils.JsonValue;

/**
 * Player avatar for the plaform game.
 *
 * Note that the constructor does very little.  The true initialization happens
 * by reading the JSON value.
 */
public class PlayerModel extends CharacterModel {
    /** Radius of player's light */
    protected float lightRadius = 0;

    public void initialize(JsonValue json) {
        super.initialize(json);
        setFlareCount(json.get("flarecount").asInt());
    }

    /**
     * Gets player light radius
     * @return light radius
     */
    public float getLightRadius() {
        return lightRadius;
    }

    /**
     * Sets player light radius
     * @param r light radius
     */
    public void setLightRadius(float r) {
        lightRadius = r;
    }

    /**
     * Increments light radius by i (can be positive or negative) ensuring lightRadius is never less than 0.
     * @param i value to increment radius by
     */
    public void incrementLightRadius(float i) { lightRadius = Math.max(lightRadius + i, 0); }
}