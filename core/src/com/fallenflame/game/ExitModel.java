package com.fallenflame.game;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.fallenflame.game.physics.obstacle.BoxObstacle;
import com.fallenflame.game.util.JsonAssetManager;
import java.lang.reflect.*;

/** Credit to Walker White for code from the B2LightsDemo */
public class ExitModel extends BoxObstacle implements ILight {
    /**Tint of exit light */
    protected Color tint;
    /** Radius of exit light */
    protected float lightRadius;

    /**
     * Gets exit color tint
     * @return light color
     */
    public Color getLightColor() {
        return tint;
    }

    /**
     * Gets exit light radius
     * @return light radius
     */
    public float getLightRadius() {
        return lightRadius;
    }

    /**
     * Create a new ExitModel with degenerate settings
     */
    public ExitModel() {
        super(0,0,1,1);
        setSensor(true);
    }

    /**
     * Initializes the exit door via the given JSON value
     *
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the exit subtree
     *
     * @param json	the JSON subtree defining the dude
     */
    public void initialize(JsonValue json, float[] pos) {
        setName(json.name());
        float[] size = json.get("size").asFloatArray();
        setPosition(pos[0],pos[1]);
        setDimension(size[0],size[1]);

        // Technically, we should do error checking here.
        // A JSON field might accidentally be missing
        setBodyType(json.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setDensity(json.get("density").asFloat());
        setFriction(json.get("friction").asFloat());
        setRestitution(json.get("restitution").asFloat());

        lightRadius = json.get("lightradius").asInt();
        float[] tintValues = json.get("tint").asFloatArray();//RGBA
        tint = new Color(tintValues[0], tintValues[1], tintValues[2], tintValues[3]);

        // Reflection is best way to convert name to color
        Color debugColor;
        try {
            String cname = json.get("debugcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            debugColor = new Color((Color)field.get(null));
        } catch (Exception e) {
            debugColor = null; // Not defined
        }
        int opacity = json.get("debugopacity").asInt();
        debugColor.mul(opacity/255.0f);
        setDebugColor(debugColor);

        // Now get the texture from the AssetManager singleton
        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        setTexture(texture);
    }
}
