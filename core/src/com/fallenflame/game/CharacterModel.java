package com.fallenflame.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.physics.obstacle.WheelObstacle;
import com.fallenflame.game.util.FilmStrip;
import com.fallenflame.game.util.JsonAssetManager;

import javax.xml.soap.Text;


public abstract class CharacterModel extends WheelObstacle implements ILight {
    // Physics constants
    /** The factor to multiply by the input */
    private float force;
    /** The amount to slow the character down */
    private float damping;
    /** The maximum character speed */
    private float maxspeed;

    /** The current horizontal movement of the character */
    private Vector2 movement = new Vector2();
    /** Whether or not to animate the current frame */
    private boolean animate = false;

    /** How many frames until we can walk again */
    private int walkCool;
    /** The standard number of frames to wait until we can walk again */
    private int walkLimit;

    /** FilmStrip pointer to the texture region */
    //protected FilmStrip filmstrip;

    protected FilmStrip filmstripWalkRight;
    protected FilmStrip filmstripWalkLeft;
    protected FilmStrip filmstripWalkUp;
    protected FilmStrip filmstripWalkDown;

    /** The current animation frame of the avatar */
    private int startFrame;

    /** Cache for internal force calculations */
    private Vector2 forceCache = new Vector2();

    /**
     * Returns the directional movement of this character.
     *
     * This is the result of input times character force.
     *
     * @return the directional movement of this character.
     */
    public Vector2 getMovement() {
        return movement;
    }

    /**
     * Sets the directional movement of this character.
     *
     * This is the result of input times character force.
     *
     * @param value the directional movement of this character.
     */
    public void setMovement(Vector2 value) {
        setMovement(value.x,value.y);
    }

    /**
     * Sets the directional movement of this character.
     *
     * This is the result of input times character force.
     *
     * @param dx the horizontal movement of this character.
     * @param dy the vertical movement of this character.
     */
    public void setMovement(float dx, float dy) {
        movement.set(dx,dy);
    }

    /**
     * Returns how much force to apply to get the character moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the character moving
     */
    public float getForce() {
        return force;
    }

    /**
     * Sets how much force to apply to get the character moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @param value	how much force to apply to get the character moving
     */
    public void setForce(float value) {
        force = value;
    }

    /**
     * Returns how hard the brakes are applied to get a character to stop moving
     *
     * @return how hard the brakes are applied to get a character to stop moving
     */
    public float getDamping() {
        return damping;
    }

    /**
     * Sets how hard the brakes are applied to get a character to stop moving
     *
     * @param value	how hard the brakes are applied to get a character to stop moving
     */
    public void setDamping(float value) {
        damping = value;
    }

    /**
     * Returns the upper limit on character left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on character left-right movement.
     */
    public float getMaxSpeed() {
        return maxspeed;
    }

    /**
     * Sets the upper limit on character left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @param value	the upper limit on character left-right movement.
     */
    public void setMaxSpeed(float value) {
        maxspeed = value;
    }

    /**
     * Returns the current animation frame of this character.
     *
     * @return the current animation frame of this character.
     */
    public float getStartFrame() {
        return startFrame;
    }

    /**
     * Sets the animation frame of this character.
     *
     * @param value	animation frame of this character.
     */
    public void setStartFrame(int value) {
        startFrame = value;
    }

    /**
     * Returns the cooldown limit between walk animations
     *
     * @return the cooldown limit between walk animations
     */
    public int getWalkLimit() {
        return walkLimit;
    }

    /**
     * Sets the cooldown limit between walk animations
     *
     * @param value	the cooldown limit between walk animations
     */
    public void setWalkLimit(int value) {
        walkLimit = value;
    }

    /**
     * Gets light radius for character
     * @return light radius
     */
    public abstract float getLightRadius();

    /**
     * Gets light color for color
     * @return light color
     */
    public abstract Color getLightColor();

    /**
     * Creates a new character with degenerate settings
     *
     * The main purpose of this constructor is to set the initial capsule orientation.
     */
    public CharacterModel() {
        super(0,0,1.0f);
        setFixedRotation(false);
    }

    /**
     * Initializes the character via the given JSON value
     *
     * The JSON value has been parsed and is part of a bigger level file.
     *
     * @param json	the JSON subtree defining the player
     */
    public void initialize(JsonValue json, float[] pos) {
        setName(json.name());
        float radius = json.get("radius").asFloat();
        setPosition(pos[0], pos[1]);
        setRadius(radius);

        // Technically, we should do error checking here.
        // A JSON field might accidentally be missing
        setBodyType(json.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setDensity(json.get("density").asFloat());
        setFriction(json.get("friction").asFloat());
        setRestitution(json.get("restitution").asFloat());
        setForce(json.get("force").asFloat());
        setDamping(json.get("damping").asFloat());
        setMaxSpeed(json.get("maxspeed").asFloat());
        setStartFrame(json.get("startframe").asInt());
        setWalkLimit(json.get("walklimit").asInt());

        // Reflection is best way to convert name to color
//        Color debugColor;
//        try {
//            String cname = json.get("debugcolor").asString().toUpperCase();
//            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
//            debugColor = new Color((Color)field.get(null));
//        } catch (Exception e) {
//            debugColor = null; // Not defined
//        }
//        int opacity = json.get("debugopacity").asInt();
//        debugColor.mul(opacity/255.0f);
//        setDebugColor(debugColor);

        //Now get the texture from the AssetManager singleton
        textureHelper(json);
    }

    /**
     * Intializes the CharacterModel texture using the JSON file
     *
     * The JSON value has been parsed and is part of a bigger level file.
     *
     * @param json	the JSON subtree defining the player has a "texture" key
     *              that has the fields "walk-right", "walk-left", "left",
     *              "right", "up", "down"
     */
    private void textureHelper(JsonValue json){
        JsonValue textureJson = json.get("texture");

        String key = textureJson.get("right").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            filmstripWalkRight = (FilmStrip) texture;
        } catch (Exception e) {
            filmstripWalkRight = null;
        }

        key = textureJson.get("left").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            filmstripWalkLeft = (FilmStrip) texture;
        } catch (Exception e) {
            filmstripWalkLeft = null;
        }

//        key = textureJson.get("up").asString();
//        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
//        try {
//            filmstripWalkUp = (FilmStrip) texture;
//        } catch (Exception e) {
//            filmstripWalkUp = null;
//        }
//
//        key = textureJson.get("down").asString();
//        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
//        try {
//            filmstripWalkDown = (FilmStrip) texture;
//        } catch (Exception e) {
//            filmstripWalkDown = null;
//        }

        //pick default direction
        FilmStrip filmStrip = filmstripWalkRight;
        setTexture(filmStrip);
    }

    /**
     * Applies the force to the body of this character
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!isActive()) {
            return;
        }

        // Only walk or spin if we allow it
        setLinearVelocity(Vector2.Zero);
        setAngularVelocity(0.0f);

        // Apply force for movement
        if (getMovement().len2() > 0f) {
            forceCache.set(getMovement());
            body.applyForce(forceCache,getPosition(),true);
            animate = true;
        } else {
            animate = false;
        }
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        //getAngle has up as 0 radians, down as pi radians, pi/2 is left, -pi/2 is right.
        double angle = getAngle();
        double pi = Math.PI;
        double pi16 = pi / 16;
        if(angle < 0){
            angle = angle + 2 * pi;
        }
//
//        if(angle > 15 * pi16 || angle < 1 * pi16) {
//            filmstrip = filmstripWalkUp;
//        } else if(angle > 1 * pi16 || angle < 7 * pi16) {
//            filmstrip = filmstripWalkLeft;
//        } else if(angle > 7 * pi16 || angle < 9 * pi16) {
//            filmstrip = filmstripWalkDown;
//        } else { // 9 *  pi16 < angle < 15 * pi16
//            filmstrip = filmstripWalkRight;
//        }


        FilmStrip filmstrip;

        if(angle < Math.PI){
            filmstrip = filmstripWalkLeft;
        } else {
            filmstrip = filmstripWalkRight;
        }

        setTexture(filmstrip);

        // Animate if necessary
        if (animate && walkCool == 0) {
            if (filmstrip != null) {
                int next = (filmstrip.getFrame()+1) % filmstrip.getSize();
                filmstrip.setFrame(next);
            }
            walkCool = walkLimit;
        } else if (walkCool > 0) {
            walkCool--;
        } else if (!animate) {
            if (filmstrip != null) {
                filmstrip.setFrame(startFrame);
            }
            walkCool = 0;
        }

        super.update(dt);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,0 ,1.0f,1.0f);
        }
    }
}
