package com.fallenflame.game;

import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.physics.obstacle.BoxObstacle;
import com.fallenflame.game.physics.obstacle.WheelObstacle;
import com.fallenflame.game.util.JsonAssetManager;

public class TreeModel extends BoxObstacle implements IWallLike {

    private float offsetX, offsetY;

    /**
     * Constructor with default settings.
     */
    public TreeModel() {
        super(0, 0, 1, 1);
    }

    public void initialize(JsonValue globalJson, JsonValue levelJson) {
        setName(globalJson.name());
        float[] pos = levelJson.get("pos").asFloatArray();
        float[] size = globalJson.get("size").asFloatArray();


        setDimension(size[0], size[1]);
        setBodyType(globalJson.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setPosition(pos[0], pos[1]);

        // Get default texture
        String key = globalJson.get("texture").asString();;
        if(levelJson.has("texture"))
            levelJson.get("texture").asString(); // Get specific texture if available
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        setTexture(texture);

        float[] offsets = globalJson.get("offset").asFloatArray();
        offsetX = offsets[0];
        offsetY = offsets[1];
    }

    @Override
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            canvas.draw(texture,
                    getX() * drawScale.x - texture.getRegionWidth() / 2f + offsetX,
                    getY() * drawScale.y - texture.getRegionHeight() / 2f + offsetY);
        }
    }
}
