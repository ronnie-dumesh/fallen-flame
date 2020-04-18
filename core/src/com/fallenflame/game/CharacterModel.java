package com.fallenflame.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.physics.obstacle.WheelObstacle;
import com.fallenflame.game.util.FilmStrip;
import com.fallenflame.game.util.JsonAssetManager;

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

    /** FilmStrip pointers to the texture regions */
    protected FilmStrip filmstripWalkRight;
    protected FilmStrip filmstripWalkLeft;
    protected FilmStrip filmstripWalkUp;
    protected FilmStrip filmstripWalkDown;

    /** Offset of textures from Physics Body */
    protected Vector2 textureOffset = new Vector2();

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
     * @return the offset of the texture from the physics body
     * as a Vector 2
     */
    public Vector2 getTextureOffset() {
        return new Vector2(textureOffset);
    }

    /**
     * Sets the offset of the texture from the physics body
     * as a Vector 2
     *
     * @param x the offset of the texture on the x-axis
     * @param y the offset of the texture on the y-axis
     */
    public void setTextureOffset(float x , float y){
        textureOffset = new Vector2(x, y);
    }

    /**
     * @param m and n are CharacterModel objects
     * @return the absolute distance from CharacterModel m to CharacterModel n
     */
    public float getDistanceBetween(CharacterModel n) {
        float dx = this.getX() - n.getX();
        float dy = this.getY() - n.getY();
        float dist = (float) Math.pow((Math.pow(dx , 2) + Math.pow(dy, 2)), 0.5);
        return dist;
    }

    /**
     * getAngleBetween(m, n) returns the angle at which CharacterModel n
     * is at relative to the origin CharacterModel m. The method
     * returns the angle in radians in the range [0, 2pi) as if
     * viewed on a unit circle. If n is directly right of m, the angle
     * is 0
     *
     * @param m and n are CharacterModel objects
     * @return angle from CharacterModel m to CharacterModel n in radians
     */
    public float getAngleBetween(CharacterModel n){
        float adj = n.getX() - this.getX();
        float opp = n.getY() - this.getY();
        float hyp = getDistanceBetween(n);

        double ratio, angle;

        //n is located in Quadrant I or II
        if(opp > 0) {
            ratio = (double) adj / hyp;
            angle = Math.acos(ratio);

        } else { //Quadrant III or IV
            ratio = (double) -1 * adj / hyp;
            angle = Math.acos(ratio) + Math.PI;
        }

        return (float) angle;
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
        setTextureOffset(json.get("textureoffset").get("x").asFloat(),
                        json.get("textureoffset").get("y").asFloat());

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

        key = textureJson.get("up").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            filmstripWalkUp = (FilmStrip) texture;
        } catch (Exception e) {
            filmstripWalkUp = null;
        }

        key = textureJson.get("down").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            filmstripWalkDown = (FilmStrip) texture;
        } catch (Exception e) {
            filmstripWalkDown = null;
        }

        //pick default direction
        FilmStrip filmstrip = filmstripWalkRight;
        setTexture(filmstrip, textureOffset.x, textureOffset.y);
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
        if(angle < 0) angle = angle + 2 * Math.PI;
        int angle100 = (int) (angle * 100);


        FilmStrip filmstrip;

        if(angle100 == 0){
            filmstrip = filmstripWalkUp;
        } else if (angle100 > 0 && angle100 < 314){
            filmstrip =filmstripWalkLeft;
        } else if (angle100 == 314){
            filmstrip = filmstripWalkDown;
        } else {
            filmstrip = filmstripWalkRight;
        }

        setTexture(filmstrip, textureOffset.x, textureOffset.y);

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
     * @return The X coordinate of the center of texture of the CharacterModel
     */
    @Override
    public float getX(){
        return super.getX() + textureOffset.x / 2.0f;
    }

    /**
     * @return The Y coordinate of the center of texture of the CharacterModel
     */
    @Override
    public float getY(){
        return super.getY() + textureOffset.y / 2.0f;
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
