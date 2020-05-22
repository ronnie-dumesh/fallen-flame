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
    /** Player Life types enum */
    protected enum LifeState {
        ALIVE,
        DYING,
        DEAD,
        WINNING,
        WON
    }
    private LifeState life;

    /** Number of flares the player has left */
    private int flareCount;

    /** Radius of player's light */
    protected float lightRadius;
    protected float lightRadiusWalk;
    protected float lightRadiusSprint;
    protected float lightRadiusSneak;
    /** Player sneak and spring left (once hits 0, a ghost is deployed on the map)
     *  powerVal must be greater than or equal to 0 */
    protected float powerVal;
    /** Max sneak and spring player can have at a given level */
    protected float maxPowerVal;
    /** Static resource decrease rates */
    private float sprintDecRate;
    private float sneakDecRate;

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
    private FilmStrip fireBuddyThrow;
    private FilmStrip fireBuddyWin;

    /** Offset of firebuddy texture from center of player in meters */
    protected Vector2 fireBuddyTextureOffset;

    /** Origin of fire buddy when drawing not in sneak mode */
    protected Vector2 fireBuddyOrigin;

    /** Origin of drawing for fire buddy when player is in sneak mode */
    protected Vector2 fireBuddySneak;

    /** Whether or not the fire buddy is throwing a flare */
    protected boolean throwing;

    /** Filmstrip of player death */
    private FilmStrip deathFilmstripRight;
    private FilmStrip deathFilmstripLeft;

    /** Frames until player finishes dying and is considered dead */
    private int deathDelay;

    /** Frames until player finishes dying and is considered dead */
    private int winDelay;

    /**
     * Initializes the character via the given JSON value
     *
     * @param globalJson	the JSON subtree defining global player data
     * @param levelJson     the JSON subtree defining level data
     */
    public void initialize(JsonValue globalJson, JsonValue levelJson) {
        super.initialize(globalJson, levelJson.get("playerpos").asFloatArray());
        // Global json data
        lightRadiusSprint = globalJson.get("sprintlightrad").asInt();
        lightRadiusSneak = globalJson.get("sneaklightrad").asInt();
        lightRadiusWalk = globalJson.get("walklightrad").asInt();
        lightRadius = lightRadiusWalk;
        float[] tintValues = globalJson.get("tint").asFloatArray();//RGBA
        tint = new Color(tintValues[0], tintValues[1], tintValues[2], tintValues[3]);
        sneakDecRate = globalJson.get("sneakDecRate").asFloat();
        sprintDecRate = globalJson.get("sprintDecRate").asFloat();

        // Level json data
        flareCount = levelJson.has("startFlareCount") ?
                levelJson.get("startFlareCount").asInt() : globalJson.get("standardflarecount").asInt();
        powerVal = levelJson.has("startSneakVal") ?
                levelJson.get("startSneakVal").asFloat() : globalJson.get("defaultStartSneakVal").asInt();
        maxPowerVal = powerVal;

        String walkSoundKey = globalJson.get("walksound").asString();
        walkSound = JsonAssetManager.getInstance().getEntry(walkSoundKey, Sound.class);

        life = LifeState.ALIVE;

        throwing = false;

        deathDelay = globalJson.get("deathdelay").asInt();

        winDelay = globalJson.get("windelay").asInt();
    }

    @Override
    public void initializeTextures(JsonValue json){
        super.initializeTextures(json);

        JsonValue textureJson = json.get("texture");

        String key = textureJson.get("death-left").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            deathFilmstripLeft = (FilmStrip) texture;
            deathFilmstripLeft.setFrame(0); //reset filmstrips in cases where the player dies and the level resets
        } catch (Exception e){
            deathFilmstripLeft = null;
        }

        key = textureJson.get("death-right").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            deathFilmstripRight = (FilmStrip) texture;
            deathFilmstripRight.setFrame(0); //reset filmstrips in cases where the player dies and the level resets
        } catch (Exception e){
            deathFilmstripRight = null;
        }

        JsonValue firebuddy = json.get("firebuddy");
        textureJson = firebuddy.get("texture");

        key = textureJson.get("left").asString();
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

        key = textureJson.get("throw").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            fireBuddyThrow = (FilmStrip) texture;
        } catch (Exception e) {
            fireBuddyThrow = null;
        }

        key = textureJson.get("win").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            fireBuddyWin = (FilmStrip) texture;
            fireBuddyWin.setFrame(0); //reset filmstrips in cases where the player wins
        } catch (Exception e) {
            fireBuddyWin = null;
        }

        //pick default direction
        fireBuddyFilmstrip = fireBuddyRight;

        float offsetX = firebuddy.get("textureoffset").get("x").asFloat();
        float offsetY = firebuddy.get("textureoffset").get("y").asFloat();
        //set fire buddy origin;
        fireBuddyTextureOffset = new Vector2(offsetX, offsetY);

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
     * @return the origin of the firebuddy texture in pixels
     * along the y-axis
     */
    protected float getFireBuddyOriginY() {return fireBuddyOrigin.y; }

    /**
     * Sets the origin of the fire buddy to be offset (x, y) relative to the player
     * The y-axis is positive downwards, and the x-axis is positive rightwards
     *
     * @param x the offset of the texture on the x-axis
     * @param y the offset of the texture on the y-axis
     */
    protected void setFireBuddyOrigin(float x, float y){
        fireBuddyOrigin = new Vector2(x, y);
    }

    /**
     * @return the origin of the firebuddy texture when sneaking in pixels
     * as a Vector 2
     */
    protected Vector2 getFireBuddySneak() { return new Vector2(fireBuddySneak); }

    /**
     * Get the position of the firebuddy on the screen in meters
     */
    public Vector2 getFireBuddyPosition() { return new Vector2(
            getPosition().x + (getFireBuddyOriginX() + fireBuddyFilmstrip.getRegionWidth()) / drawScale.x / 2.0f,
            getPosition().y - (getFireBuddyOriginY() / drawScale.y) + fireBuddyFilmstrip.getRegionHeight() / drawScale.y / 2.0f);
    }

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
     * Returns the number of flares the player has left
     *
     * @return the number of flares the player has left
     */
    public int getFlareCount() { return flareCount; }

    /**
     * Decrement flare count (for firing a flare)
     */
    public void decFlareCount() { flareCount--; }

    /**
     * Increment flare count (for picking up a flare)
     */
    public void incFlareCount() {
        flareCount++;
    }

    /**
     * Gets player light radius
     * @return light radius
     */
    public float getLightRadius() {
        return lightRadius;
    }

    /** Get amount of sneak and spring updates left for player */
    public float getPowerVal() { return powerVal; }

    /** Get maximum amount of sneak and spring updates left for player on this level*/
    public float getMaxPowerVal() { return maxPowerVal; }

    /** Decrement resource value by 2 for sprinting (for 1 update of sprinting) */
    public void decPowerValSprint() {
        powerVal -= sprintDecRate;
    }

    /** Decrement resource value by 1 for sneaking */
    public void decPowerValSneak() {
        powerVal -= sneakDecRate;
    }

    /**
     * Gets player color tint
     * @return light color
     */
    public Color getLightColor() {
        return tint;
    }

    /**
     * Sets player to sneak light radius (not reachable by scrolling)
     */
    public void setLightRadiusSneak() { lightRadius = lightRadiusSneak; }

    /**
     * Sets player to sprint light radius
     */
    public void setLightRadiusSprint() { lightRadius = lightRadiusSprint; }


    /**
     * Sets player to walk light radius
     */
    public void setLightRadiusWalk() { lightRadius = lightRadiusWalk; }

    /**
     * Make the firebuddy begin the flare throwing animation.
     */
    public void throwFlare() { throwing = true; }

    /**
     * Sets the player's life state to dying. Once the dying animation
     * concludes the player is then set as dead.
     */
    public void die() { life = LifeState.DYING; }

    /**
     * Sets the player's life state to winning and increase the player light radius.
     * Once the winning animation concludes the player is then set as won.
     */
    public void win() { life = LifeState.WINNING; }

    /**
     * Returns whether the player is dead.
     * @return true if the player is dead and false if player is alive, dying, winning, or has won
     */
    public boolean isDead() { return life == LifeState.DEAD; }

    /**
     * Returns whether the player is dying
     * @return true if the player is dying and false if player is alive, dead, winning, or has won
     */
    public boolean isDying() { return life == LifeState.DYING; }

    /**
     * Returns whether the player is alive
     * @return true if the player is alive and false if player is dead, dying, winning, or has won
     */
    public boolean isAlive() { return life == LifeState.ALIVE;}

    /**
     * Returns whether the player is winning
     * @return true if the player is winning and false if player is alive, dead, dying, or has won
     */
    public boolean isWinning() { return life == LifeState.WINNING; }

    /**
     * Returns whether the player has won
     * @return true if the player has won and false if the player is alive, dead, dying, or winning
     */
    public boolean hasWon() { return life == LifeState.WON; }

    /**
     * Returns the walk sound
     *
     * @return the walk sound
     */
    public Sound getWalkSound() {
        return walkSound;
    }

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

        if(isDying()) {
            if (angle100 > 0 && angle100 < 314 && deathFilmstripLeft != null) {
                filmstrip = deathFilmstripLeft;
            } else if (deathFilmstripRight != null) { //angle between pi and 2pi/0 (inclusive)
                filmstrip = deathFilmstripRight;
            }

            setTexture(filmstrip, textureOffset.x, textureOffset.y);

            int frame = filmstrip.getFrame();
            if (walkCool == 0 && frame < filmstrip.getSize() - 1) {
                walkCool = walkLimit;
                filmstrip.setFrame(frame + 1);
            } else if (walkCool > 0) {
                walkCool--;
            } else if (deathDelay <= 0){
                life = LifeState.DEAD;
            } else if (frame == filmstrip.getSize() - 1) {
                deathDelay--;
            }
        } else if (isWinning()) {
            if (walkCool <= 0){
                walkCool = walkLimit;
            } else if (walkCool > 0) {
                walkCool = walkCool - 2;
            }
        } else if (isAlive()) super.update(dt);

        if (isAlive() || isWinning())  animateFireBuddy(angle100);
    }

    /**
     * A helper method for drawing the fire buddy
     * @param angle100 the angle which the player is facing rounded down to the nearest int
     */
    protected void animateFireBuddy(int angle100){
        if(isWinning()){
            fireBuddyFilmstrip = fireBuddyWin;

            int frame = fireBuddyFilmstrip.getFrame();
            if(walkCool == 0 && frame < fireBuddyFilmstrip.getSize() - 1) {
                fireBuddyFilmstrip.setFrame(frame + 1);
            } else if (winDelay <= 0){
                life = LifeState.WON;
            } else if (frame == fireBuddyFilmstrip.getSize() - 1){
                winDelay--;
            }
        }

        else if(!throwing) {
            if (angle100 == 0) {
                fireBuddyFilmstrip = fireBuddyUp;
            } else if (angle100 > 0 && angle100 < 314) {
                fireBuddyFilmstrip = fireBuddyLeft;
            } else if (angle100 == 314) {
                fireBuddyFilmstrip = fireBuddyDown;
            } else {
                fireBuddyFilmstrip = fireBuddyRight;
            }

            // Animate if necessary
            // Do not change values of walkCool and animate, to be done in PlayerModel.update();
            if (animate && walkCool == 0 && fireBuddyFilmstrip != null) {
                int next = (fireBuddyFilmstrip.getFrame() + 1) % fireBuddyFilmstrip.getSize();
                fireBuddyFilmstrip.setFrame(next);
            } else if (!animate && fireBuddyFilmstrip != null) {
                fireBuddyFilmstrip.setFrame(startFrame);
            }
        } else {
            fireBuddyFilmstrip = fireBuddyThrow;

            // Do not change values of walkCool and animate, to be done in PlayerModel.update();
            int frame = fireBuddyFilmstrip.getFrame();
            if (walkCool == 0 && frame < fireBuddyFilmstrip.getSize() - 1) {
                walkCool = walkLimit;
                fireBuddyFilmstrip.setFrame(frame + 1);
            } else if (frame == fireBuddyFilmstrip.getSize() - 1){
                throwing = false;
                fireBuddyFilmstrip.setFrame(0);
            }
        }
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