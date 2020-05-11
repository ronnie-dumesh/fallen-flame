package com.fallenflame.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.physics.obstacle.ObstacleCanvas;
import com.fallenflame.game.physics.obstacle.WheelObstacle;
import com.fallenflame.game.util.FilmStrip;
import com.fallenflame.game.util.JsonAssetManager;

public class FireballModel extends WheelObstacle implements ILight {
    // Physics constants
    /** The speed with which fireball is thrown */
    private float speed;
    // Graphic constants
    /** FilmStrip pointer to the texture region */
    private FilmStrip filmstrip;
    /** The current animation frame of the flare */
    private int startFrame;
    /**The color to tint the flare */
    private Color tint;
    /** Light Radius */
    private float lightRadius;
    /** Active status (aka mark for deletion) */
    private boolean active;

    /**
     * Get active status
     * @return True if active
     */
    public boolean isActive() { return active; }

    /**
     * Set active status to false
     */
    public void deactivate() { active = false; }

    /**
     * Returns the light radius of this flare.
     *
     * @return the light radius of this flare.
     */
    public float getLightRadius() {
        return lightRadius;
    }

    /**
     * @return the color of the Flare's tint
     */
    public Color getLightColor() {return tint;}

    /**
     * @return fireball speed
     */
    public float getSpeed() { return speed; }

    public FireballModel(Vector2 pos) {
        super(pos.x,pos.y,1.0f);
        this.setSensor(true);
        active = true;
        setFixedRotation(false);
    }

    /**
     * Initializes the fireball via the given JSON value
     *
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the fireball subtree
     *
     * @param json	the JSON subtree defining the dude
     */
    public void initialize(JsonValue json) {
        setRadius(json.get("radius").asFloat());
        setBodyType(json.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setDensity(json.get("density").asFloat());
        setFriction(json.get("friction").asFloat());
        setRestitution(json.get("restitution").asFloat());
        lightRadius = json.get("lightradius").asFloat();
        speed = json.get("speed").asFloat();
        startFrame = json.get("startframe").asInt();
        float[] tintValues = json.get("tint").asFloatArray();//RGBA
        tint = new Color(tintValues[0], tintValues[1], tintValues[2], tintValues[3]);
        // Get the texture from the AssetManager singleton
        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            filmstrip = (FilmStrip)texture;
        } catch (Exception e) {
            filmstrip = null;
        }
        setTexture(texture);
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        //TODO: when we have animated fireball
//        if (filmstrip != null) {
//            int next = (filmstrip.getFrame()+1) % filmstrip.getSize();
//            filmstrip.setFrame(next);
//        }

        super.update(dt);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(ObstacleCanvas canvas) {
        if (texture != null) {
            canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),1.0f,1.0f);
        }
    }
}
