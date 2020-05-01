package com.fallenflame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.physics.obstacle.BoxObstacle;
import com.fallenflame.game.physics.obstacle.ObstacleCanvas;
import com.fallenflame.game.physics.obstacle.WheelObstacle;
import com.fallenflame.game.util.JsonAssetManager;

public class ItemModel extends WheelObstacle implements ILight  {
    /** Item type. For now only items are pick-up flares*/
    private enum ItemType {
        FLARE,
    }
    /** Item type */
    private ItemType type;
    /**Tint of item light */
    private Color tint;
    /** Radius of item light */
    private float lightRadius;
    /** Active status (picked up or not) */
    private boolean active;

    /**
     * Gets item color tint
     * @return light color
     */
    public Color getLightColor() {
        return tint;
    }

    /**
     * Gets item light radius
     * @return light radius
     */
    public float getLightRadius() {
        return lightRadius;
    }

    /**
     * Returns true if item is a flare item
     */
    public boolean isFlare() { return type == ItemType.FLARE; }

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
     * Create a new ItemModel with degenerate settings
     */
    public ItemModel(float[] pos) {
        super(pos[0],pos[1],1.0f);
        setFixedRotation(true);
        active = true;
        setSensor(true);
    }

    /**
     * Initializes the item door via the given JSON value, position, itemType
     *
     * @param globalJson	the JSON subtree defining global item data
     * @param type          string for item type
     */
    public void initialize(JsonValue globalJson, String type){
        switch (type){
            case "flare":
                this.type = ItemType.FLARE;
                break;
            default:
                Gdx.app.error("ItemModel", "Illegal item type", new IllegalArgumentException());
        }
        // Get global JSON for specific type
        JsonValue globalItemJson = globalJson.get(type);
        float radius = globalItemJson.get("radius").asFloat();
        setRadius(radius);
        setBodyType(globalItemJson.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setDensity(globalItemJson.get("density").asFloat());
        setFriction(globalItemJson.get("friction").asFloat());
        setRestitution(globalItemJson.get("restitution").asFloat());

        // Light data
        lightRadius = globalItemJson.get("lightradius").asInt();
        float[] tintValues = globalItemJson.get("tint").asFloatArray();//RGBA
        tint = new Color(tintValues[0], tintValues[1], tintValues[2], tintValues[3]);

        // Now get the texture from the AssetManager singleton
        String key = globalItemJson.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        setTexture(texture);
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
