// =============================================================================
// Xi Compiler
// GasModel.java
// =============================================================================


package edu.cornell.gdiac.b2lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.obstacle.ObstacleCanvas;
import edu.cornell.gdiac.physics.obstacle.WheelObstacle;
import edu.cornell.gdiac.util.JsonAssetManager;

import java.lang.reflect.Field;

public class GasModel extends WheelObstacle {

    /** Default gas radius. */
    private static final float GAS_RADIUS = 1f; // Might want to change this!

    /** How long a fire can last, in milliseconds. */
    private static final int FIRE_DURATION = 6000;

    /** Is this fire lit? */
    private boolean isLit;

    /** Time when it started burning. **/
    private long startBurn;

    /**Gasoline texture */
    private TextureRegion gasTexture;

    /**Fire texture */
    private TextureRegion fireTexture;

    public GasModel(float x, float y) {
        super(x, y, GAS_RADIUS);
        //setSensor(true);
    }
    public void initialize(JsonValue json) {
        setName(json.name());
        setBodyType(BodyDef.BodyType.DynamicBody);
        setDensity(1.0f);
        setFriction(0.0f);
        //System.out.println(this.body);
        isLit = false;

        //System.out.println(isSensor());

        // Create collision filter (for light penetration)
        Filter contactFilter = new Filter();
        contactFilter.categoryBits = 0b10;
        setFilterData(contactFilter);

        Color debugColor;
        try {
            String cname = json.get("debugcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            debugColor = new Color((Color) field.get(null));
        } catch (Exception e) {
            debugColor = null; // Not defined
        }
        int opacity = json.get("debugopacity").asInt();
        debugColor.mul(opacity / 255.0f);
        setDebugColor(debugColor);

        // Now get the texture from the AssetManager singleton
        String gasKey = json.get("gastexture").asString();
        String fireKey = json.get("firetexture").asString();
        gasTexture = JsonAssetManager.getInstance().getEntry(gasKey, TextureRegion.class);
        fireTexture = JsonAssetManager.getInstance().getEntry(fireKey, TextureRegion.class);
        setTexture(gasTexture);
    }
    /** Is this gas lit? */
    public boolean getLit() {
        return isLit;
    }

    /** Set this gas lit or not. */
    public void setLit(boolean value) {
        if ((isLit && value) || (!isLit && !value)) return;
        if (isLit) {
            throw new Error("Cannot unlight a fire!");
        }
        isLit = true;
        setTexture(fireTexture);
        startBurn = System.currentTimeMillis();
    }

    @Override
    public void draw(ObstacleCanvas canvas) {
        if (isLit && fireTexture != null) {
            canvas.draw(fireTexture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.x, getAngle(), 1, 1);
        } else if (!isLit && gasTexture != null) {
            canvas.draw(gasTexture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.x, getAngle(), 1, 1);
        }
    }

    /** How long till burnout in milliseconds. */
    public int timeToBurnout() {
        if (!isLit) return Integer.MAX_VALUE;
        int timeLeft = FIRE_DURATION - (int) (System.currentTimeMillis() - startBurn);
        return Math.max(timeLeft, 0);
    }
}
