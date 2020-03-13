package com.fallenflame.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.physics.obstacle.BoxObstacle;
import com.fallenflame.game.physics.obstacle.ObstacleCanvas;
import com.fallenflame.game.util.JsonAssetManager;

/**
 * A rectangle shape representing a wall.
 */
public class WallModel extends BoxObstacle {
    /** Texture information. */
    protected PolygonRegion region;

    /** Texture anchor upon region init. */
    protected Vector2 anchor;

    /** Padding (in physics coordinates) to increase the sprite beyond the physics body. */
    protected Vector2 padding;

    /**
     * Constructor with default settings.
     */
    public WallModel() {
        super(0, 0, 1, 1);
        region = null;
        padding = new Vector2();
    }

    /**
     * Initialise support for a tiled texture.
     *
     * This will make sure that the texture is uniform. In other words, moving the this obstacle will not move the
     * texture.
     */
    protected void initRegion() {
        if (texture == null) return;
        float[] scaled = new float[vertices.length];
        for (int i = 0, j = scaled.length; i < j; i++) {
            scaled[i] = (vertices[i] + Math.signum(vertices[i]));
            if (i % 2 == 0)
                scaled[i] = (scaled[i] * padding.x + getX()) * drawScale.x;
            else
                scaled[i] = (scaled[i] * padding.y + getY()) * drawScale.y;
        }
        short[] tris = {0, 1, 3, 3, 2, 1};
        anchor = new Vector2(getX(), getY());
        region = new PolygonRegion(texture, scaled, tris);
    }

    @Override
    protected void resize(float width, float height) {
        super.resize(width, height);
        initRegion();
    }

    @Override
    public void setPosition(Vector2 value) {
        this.setPosition(value.x, value.y);
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        initRegion();
    }

    @Override
    public void setX(float value) {
        super.setX(value);
        initRegion();
    }

    @Override
    public void setY(float value) {
        super.setY(value);
        initRegion();
    }

    @Override
    public void setAngle(float value) {
        throw new UnsupportedOperationException("WallModel cannot be rotated.");
    }

    @Override
    public void setTexture(TextureRegion value) {
        super.setTexture(value);
        initRegion();
    }

    @Override
    public void setDrawScale(Vector2 value) {
        this.setDrawScale(value.x, value.y);
    }

    @Override
    public void setDrawScale(float x, float y) {
        super.setDrawScale(x, y);
        initRegion();
    }

    /**
     * Get the sprite padding for this wall.
     *
     * The sprite padding is a vector indicating the additional width and height of the sprite, beyond the bounds of its
     * physics body. The dimensions are measured in physics scale, not texture scale. So if padding is (0.1, 0.2), the
     * width is 2 * 0.1 units larger, and the height is 2 * 0.2 units higher.
     *
     * This method returns a reference to a new copy of the padding vector. Modify the returned vector has no effect.
     * Use {@code setPadding} to change the padding.
     *
     * @return The sprite padding for this wall.
     */
    public Vector2 getPadding() {
        return padding.cpy();
    }

    /**
     * Set the sprite padding for this wall.
     *
     * The sprite padding is a vector indicating the additional width and height of the sprite, beyond the bounds of its
     * physics body. The dimensions are measured in physics scale, not texture scale. So if padding is (0.1, 0.2), the
     * width is 2 * 0.1 units larger, and the height is 2 * 0.2 units higher.
     *
     * This method does not retain a reference to the parameter.
     *
     * @param padding The sprite padding for this wall.
     */
    public void setPadding(Vector2 padding) {
        this.padding.set(padding);
        initRegion();
    }

    /**
     * Set the sprite padding for this wall.
     *
     * The sprite padding is a vector indicating the additional width and height of the sprite, beyond the bounds of its
     * physics body. The dimensions are measured in physics scale, not texture scale. So if padding is (0.1, 0.2), the
     * width is 2 * 0.1 units larger, and the height is 2 * 0.2 units higher.
     *
     * This method does not retain a reference to the parameter.
     *
     * @param w The horizontal padding for this wall.
     * @param h The vertical padding for this wall.
     */
    public void setPadding(float w, float h) {
        this.padding.set(w, h);
        initRegion();
    }

    public void initialize(JsonValue json) {
        setName(json.name());
        float[] pos = json.get("pos").asFloatArray(),
                size = json.get("size").asFloatArray(),
                pad = json.get("pad").asFloatArray();
        setPosition(pos[0], pos[1]);
        setDimension(size[0], size[1]);
        setPadding(pad[0], pad[1]);

        // TODO: Collision filter

        // TODO: Debug?

        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        setTexture(texture);
    }

    @Override
    public void draw(ObstacleCanvas canvas) {
        if (region != null) {
            canvas.draw(region, Color.WHITE, 0, 0,
                    (getX() - anchor.x) * drawScale.x,
                    (getY() - anchor.y) *  drawScale.y,
                    getAngle(),
                    1, 1);
        }
    }
}
