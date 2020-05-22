package com.fallenflame.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.physics.obstacle.BoxObstacle;
import com.fallenflame.game.util.JsonAssetManager;

/** Model for cosmetic extras */
public class ExtraModel extends BoxObstacle {

    /**
     * Create a new ExtraModel with degenerate settings
     */
    public ExtraModel(float[] pos) {
        super(pos[0],pos[1], 1, 1);
        setBodyType(BodyDef.BodyType.StaticBody);
    }

    /**
     * Initializes the exit door via the given JSON value and type
     */
    public void initialize(JsonValue globalJson, String type) {
        // Get global JSON for specific type
        JsonValue globalItemJson = globalJson.get(type);
        float[] size = globalItemJson.get("size").asFloatArray();
        setDimension(size[0],size[1]);
        setDensity(globalItemJson.get("density").asFloat());
        setFriction(globalItemJson.get("friction").asFloat());
        setRestitution(globalItemJson.get("restitution").asFloat());

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
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.x,getAngle(),1,1);
        }
    }
}
