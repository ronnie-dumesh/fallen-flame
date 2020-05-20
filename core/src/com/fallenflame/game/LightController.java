package com.fallenflame.game;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.enemies.EnemyModel;
import com.fallenflame.game.physics.lights.PointSource;
import com.fallenflame.game.physics.obstacle.Obstacle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * {@code LightController} manages and renders the light effect of the game.
 *
 * It collects information about the player and enemies, and renders light directly to the graphics engine of LibGDX
 * instead of using {@code GameCanvas}.
 */
public class LightController {
    /** Ambient light level */
    public static final float AMBIENT_LIGHT = 0.2f;

    /**
     * Logger for outputting info.
     */
    private static Logger log = Logger.getLogger("LightController");

    /**
     * The amount of rays each light source emits. The more rays the more precise the lights will be, but the more
     * resource it will use.
     */
    private static final int RAYS = 512;

    /**
     * A cached copy of player model.
     */
    protected PlayerModel player;

    /**Float value to increase the actual amount seen*/
    protected float playerLightOffset;
    protected float flareLightOffset;
    /**
     * A cached copy of lighting config.
     */
    protected JsonValue lightingConfig;

    /**
     * The light of the exit. This is to be created by this controller at initialisation phase.
     */
    protected PointSource exitLight;

    /**
     * The light of the player. This is to be created by this controller at initialisation phase.
     */
    protected PointSource playerLight;

    /**
     * A map of lights for all the flares.
     */
    protected Map<FlareModel, PointSource> flareLights;

    /**
     * A map of fireball lights for all the fireballs.
     */
    protected Map<FireballModel, PointSource> fireballLights;

    /**
     * A map of item lights for all the items.
     */
    protected Map<ItemModel, PointSource> itemLights;

    /**
     * A map of lights for all the enemies.
     */
    protected Map<EnemyModel, PointSource> enemyLights;

    /**
     * Camera viewport for the light.
     */
    protected OrthographicCamera raycamera;

    /**
     * Ray handler. This is what draws the light.
     */
    protected RayHandler rayhandler;

    protected Map<PointSource, Float> animateIn;
    protected Map<PointSource, Float> animateOut;
    protected int animateTicks;

    private float targetPlayerRadius;

    protected boolean debug;

    protected float scale;

    protected static final int DEFAULT_ANIMATE_TICKS = 9;

    /**
     * Initialise this controller.
     *
     * @param player The player instance.
     * @param levelLighting The lighting JSON config of this level.
     * @param world The instance of Box2D {@code World}.
     * @param scale Scale for rendering.
     */

    public void initialize(PlayerModel player, ExitModel exit,
                           JsonValue levelLighting, World world, Rectangle bounds, Vector2 scale) {
        animateIn = new HashMap<>();
        animateOut = new HashMap<>();
        animateTicks = levelLighting.has("animateTicks") ? levelLighting.get("animateTicks").asInt() : DEFAULT_ANIMATE_TICKS;

        // Set up camera first.
        raycamera = new OrthographicCamera(
                Gdx.graphics.getWidth() / scale.x,
                Gdx.graphics.getHeight() / scale.y);

        // set up ray handler.
        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);
        rayhandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        rayhandler.setAmbientLight(0, 0, 0, AMBIENT_LIGHT);
        rayhandler.setBlur(true);
        rayhandler.setBlurNum(3);
        updateCamera();

        // Save player and config.
        this.player = player;
        this.lightingConfig = levelLighting;
        playerLightOffset = (player.getLightRadius()/3f);
        flareLightOffset = 0.5f;
        // Create player light.
        playerLight = createPointLight(player.getLightRadius()+playerLightOffset, player.getTextureX(), player.getTextureY());
        targetPlayerRadius = player.getLightRadius();

        // Create exit light.
        exitLight = createPointLight(exit.getLightRadius(), exit.getX(), exit.getY());

        // Create empty maps for flare, fireball and enemy lights.
        this.flareLights = new HashMap<>();
        this.itemLights = new HashMap<>();
        this.fireballLights = new HashMap<>();
        this.enemyLights = new HashMap<>();
    }

    public boolean getDebug() {
        return debug;
    }

    public void setDebug(boolean value) {
        debug = value;
    }

    /**
     * Dispose this controller. Do not use this controller after it is disposed or unexpected behaviour may happen.
     */
    public void dispose() {
        if (this.player == null) return;
        this.player = null;
        this.lightingConfig = null;
        this.playerLight = null;
        this.exitLight = null;
        this.flareLights.clear();
        this.flareLights = null;
        this.itemLights.clear();
        this.itemLights = null;
        this.fireballLights.clear();
        this.fireballLights = null;
        this.enemyLights.clear();
        this.enemyLights = null;
    }

    /**
     * Create a point light.
     *
     * @param dist The distance of the light.
     *
     * @return The {@code PointSource} instance.
     */
    protected PointSource createPointLight(float dist, float x, float y) {
        // Create point source.
        PointSource p = new PointSource(rayhandler, RAYS, Color.WHITE, dist, x, y);
        p.setSoft(true);

        // Set up filter.
        Filter f = new Filter();
        f.categoryBits = f.maskBits = 0;
        p.setContactFilter(f);
        p.setActive(true);

        return p;
    }

    protected <T extends Obstacle & ILight>
    void updateLightsForList(Collection<T> list, Map<T, PointSource> lightMap) {
        // First step: Remove lights of things that are no longer in the list.
        Set<Map.Entry<T, PointSource>> entrySet = lightMap.entrySet();
        entrySet.removeIf(i -> {
            if (!list.contains(i.getKey())) {
                PointSource l = i.getValue();
                animateIn.remove(l);
                animateOut.put(l, 1f);
                return true;
            }
            return false;
        });

        // Second step: Update light radii for lights already there.
        for (Map.Entry<T, PointSource> entry : entrySet) {
           flareLightOffset = (entry.getValue().getDistance()/2.5f);
            entry.getValue().setDistance(entry.getKey().getLightRadius()+flareLightOffset);
            entry.getValue().setColor(entry.getKey().getLightColor());
            entry.getValue().setPosition(entry.getKey().getPosition());
        }

        // Last step: Create lights for new things in the list.
        list.stream().filter(i -> !lightMap.containsKey(i)).forEach(i -> {
            PointSource f = createPointLight(i.getLightRadius()+flareLightOffset, i.getX(), i.getY());
            f.setColor(i.getLightColor());
            lightMap.put(i, f);
            animateIn.put(f, 0f);
        });
    }

    protected void doAnimation() {
        float i = 1f / animateTicks;
        for (Map.Entry<PointSource, Float> e : animateIn.entrySet()) {
            e.getKey().setDistance(e.getValue() * e.getKey().getDistance());
            animateIn.put(e.getKey(), e.getValue() + i);
        }
        animateIn.values().removeIf((e) -> e >= 1);
        for (Map.Entry<PointSource, Float> e : animateOut.entrySet()) {
            e.getKey().setDistance(e.getKey().getDistance() / e.getValue() * (e.getValue() - i));
            if (e.getValue() <= i) {
                e.getKey().setActive(false);
                e.getKey().dispose();
                animateOut.remove(e.getKey());
                continue;
            }
            animateOut.put(e.getKey(), e.getValue() - i);
        }
        animateOut.entrySet().removeIf((e) -> {
            if (e.getValue() <= i) {
                e.getKey().setActive(false);
                e.getKey().dispose();
                return true;
            }
            return false;
        });
       float pLightCurrDist = playerLight.getDistance();
       if (pLightCurrDist + playerLightOffset != targetPlayerRadius) {
           playerLightOffset = (player.getLightRadius()/3.0f);
           playerLightOffset = playerLightOffset + (player.isSprinting() ? 1.0f : 0f);
           if (Math.abs((pLightCurrDist+playerLightOffset) - targetPlayerRadius) < 0.05) {
               playerLight.setDistance(targetPlayerRadius+playerLightOffset);
           } else if (pLightCurrDist+playerLightOffset < targetPlayerRadius) {
               playerLight.setDistance(pLightCurrDist + playerLightOffset + (targetPlayerRadius  - pLightCurrDist) * 0.5f);
           } else {
               playerLight.setDistance(pLightCurrDist + playerLightOffset - (pLightCurrDist  - targetPlayerRadius) * 0.5f);
           }
       }
    }

    private void updateCamera() {
        if (player != null) raycamera.position.set(player.getX(), player.getY(), 0);
        raycamera.update();
        rayhandler.setCombinedMatrix(raycamera);
    }

    /**
     * Update all lights, call this before {@code draw()}.
     *
     * @param flares A collection of flares.
     * @param enemies A collection of enemies.
     */
    public void updateLights(Collection<FlareModel> flares, Collection<EnemyModel> enemies,
                             Collection<FireballModel> fireballs, Collection<ItemModel> items) {
        // Update debug.
        if (debug) {
            rayhandler.setAmbientLight(.5f, .5f, .5f, 0);
        } else {
            rayhandler.setAmbientLight(0, 0, 0, 0);
        }

        updateCamera();

        // Update player light.
        targetPlayerRadius = player.getLightRadius();
        playerLight.setPosition(player.getTextureX(), player.getTextureY());

        // Update flare lights.
        updateLightsForList(flares, flareLights);
        updateLightsForList(fireballs, fireballLights);
        updateLightsForList(items, itemLights);

        // Update enemy lights.
        updateLightsForList(
                enemies.stream().filter(EnemyModel::isActivated).collect(Collectors.toList()),
                enemyLights);

        doAnimation();
        rayhandler.update();
    }

    /**
     * Draw the light effect on the screen. This function directly draw to the screen, and it does not use
     * {@code GameCanvas}.
     */
    public void draw() {
        if (rayhandler == null) {
            log.warning("Rayhandler is not initialised so draw() has no effect. " +
                    "Have you initialised this controller yet? Or have you disposed it already?");
            return;
        }
        rayhandler.render();
    }

}
