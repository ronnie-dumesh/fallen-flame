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
    /** Character movement types enum */
    protected enum MovementState {
        WALK,
        SNEAK,
        SPRINT
    }
    /** How the player is currently moving */
    private MovementState move;
    // Character Speeds
    /** Player walk speed */
    private float walkSpeed;
    /** Player sprint speed */
    private float sprintSpeed;
    /** Player sneak speed */
    private float sneakSpeed;

    /** The current movement of the character */
    private Vector2 movement = new Vector2();
    /** Whether or not to animate the current frame */
    protected boolean animate = false;

    /** How many frames until we can walk again */
    protected float walkCool;
    /** The standard number of frames to wait until we can walk again */
    protected float walkLimit;

    /** FilmStrip pointers to the texture regions */
    protected FilmStrip filmstrip;
    protected FilmStrip filmstripWalkRight;
    protected FilmStrip filmstripWalkLeft;
    protected FilmStrip filmstripWalkUp;
    protected FilmStrip filmstripWalkDown;

    /** Offset of textures from Physics Body */
    protected Vector2 textureOffset = new Vector2();

    /** The current animation frame of the avatar */
    protected int startFrame;

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

    /**
     * Return True if player is sprinting
     * @return True if sprinting, False if sneaking or walking
     */
    public boolean isSprinting() { return move == MovementState.SPRINT; }

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
     * Returns the static speed set for player given current movement state
     * @return float speed
     */
    public float getSpeed(){
        switch(move){
            case SNEAK:
                return sneakSpeed;
            case SPRINT:
                return sprintSpeed;
            case WALK:
                return walkSpeed;
        }
        return -1;
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
    public float getWalkLimit() {
        return walkLimit;
    }

    /**
     * Sets the cooldown limit between walk animations
     *
     * @param value	the cooldown limit between walk animations
     */
    public void setWalkLimit(float value) {
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
     * @param n and n are CharacterModel objects
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
     * @param n and n are CharacterModel objects
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
        setStartFrame(json.get("startframe").asInt());
        setWalkLimit(json.get("walklimit").asFloat());
        setTextureOffset(json.get("textureoffset").get("x").asFloat(),
                json.get("textureoffset").get("y").asFloat());

        walkSpeed = json.get("walkspeed").asFloat();
        sprintSpeed = json.get("sprintspeed").asFloat();
        sneakSpeed = json.get("sneakspeed").asFloat();
        move = MovementState.WALK;
    }

    /**
     * Intializes the CharacterModel textures using the JSON file
     *
     * The JSON value has been parsed and is part of a bigger level file.
     *
     * @param json	the JSON subtree defining the player has a "texture" key
     *              that has the fields "walk-right", "walk-left", "left",
     *              "right", "up", "down"
     */
    public void initializeTextures(JsonValue json){
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
        filmstrip = filmstripWalkRight;
        setTexture(filmstrip, textureOffset.x, textureOffset.y);
    }

    public void move(Vector2 moveAngle) {
        if (moveAngle.isZero()){
            animate = false;
            setLinearVelocity(new Vector2());
            return;
        }

        animate = true;
        // Set character angle facing
        float angle = 0;
        angle = moveAngle.angle();
        // Convert to radians with up as 0
        angle = (float)Math.PI*(angle-90.0f)/180.0f;
        setAngle(angle);

        setLinearVelocity(moveAngle.setLength(getSpeed()));
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        //float walkCoolMultiplier = (dt/(1/60));
        float walkCoolMultiplier = 60 / (1 / dt);
        //getAngle has up as 0 radians, down as pi radians, pi/2 is left, -pi/2 is right.
        double angle = getAngle();
        if(angle < 0) angle = angle + 2 * Math.PI;
        int angle100 = (int) (angle * 100);

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
        if (animate && walkCool <= 0) {
            if (filmstrip != null) {
                int next = (filmstrip.getFrame()+1) % filmstrip.getSize();
                filmstrip.setFrame(next);
            }
            walkCool = walkLimit;
        } else if (walkCool > 0) {
            walkCool -= walkCoolMultiplier;
        } else if (!animate) {
            if (filmstrip != null) {
                filmstrip.setFrame(startFrame);
            }
            walkCool = 0.0f;
        }

        super.update(dt);
    }

    /**
     * @return The X coordinate of the center of texture of the CharacterModel
     */
    public float getTextureX(){
        return super.getX() - textureOffset.x;
    }

    /**
     * @return The Y coordinate of the center of texture of the CharacterModel
     */
    public float getTextureY(){
        return super.getY() - textureOffset.y;
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
