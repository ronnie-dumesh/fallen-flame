package com.fallenflame.game;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.fallenflame.game.util.*;

/**
 * Player avatar for the plaform game.
 *
 * Note that the constructor does very little.  The true initialization happens
 * by reading the JSON value.
 */
public class PlayerModel extends CharacterModel {
    /** Radius of player's light */
    protected float lightRadius = 0;

    /**
     * Initializes the player via the given JSON value
     *
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the player subtree
     *
     * @param json	the JSON subtree defining the player
     */
    public void initialize(JsonValue json) {
        super.initialize(json);
        // Enemy specific initialization
        // Now get the texture from the AssetManager singleton
        String key = getDefaultTexture(); // TODO: should get from JSON? --> json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            filmstrip = (FilmStrip)texture;
        } catch (Exception e) {
            filmstrip = null;
        }
        setTexture(texture);
    }

    /** Return player default texture */
    protected String getDefaultTexture() {
        return "player";
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
    public void incrementLightRadius(float i) {
        lightRadius = Math.max(lightRadius + i, 0);
    }
}