package com.fallenflame.game;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.enemies.EnemyModel;
import com.fallenflame.game.physics.lights.PointSource;
import com.fallenflame.game.physics.obstacle.Obstacle;

import java.util.*;
import java.util.List;
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

    /**
     * A cached copy of lighting config.
     */
    protected JsonValue lightingConfig;

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

    protected boolean debug;

    /**
     * Initialise this controller.
     *
     * @param player The player instance.
     * @param levelLighting The lighting JSON config of this level.
     * @param world The instance of Box2D {@code World}.
     * @param bounds The bound of the viewport.
     */
    public void initialize(PlayerModel player, JsonValue levelLighting, World world, Rectangle bounds) {
        // Set up camera first.
        raycamera = new OrthographicCamera(bounds.width, bounds.height);

        // set up ray handler.
        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);
        rayhandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        rayhandler.setCombinedMatrix(raycamera);
        rayhandler.setAmbientLight(0, 0, 0, AMBIENT_LIGHT);
        rayhandler.setBlur(true);
        rayhandler.setBlurNum(3);

        // Save player and config.
        this.player = player;
        this.lightingConfig = levelLighting;

        // Create player light.
        playerLight = createPointLight(player.getLightRadius());
        attachLightTo(playerLight, player);

        // Create empty maps for flare, fireball and enemy lights.
        this.flareLights = new HashMap<>();
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
        this.flareLights.clear();
        this.flareLights = null;
        this.fireballLights.clear();
        this.fireballLights = null;
        this.enemyLights.clear();
        this.enemyLights = null;
    }

    /**
     * Attach light to an obstacle, and activate the light.
     *
     * @param l The light.
     * @param o The obstacle.
     */
    protected void attachLightTo(PointSource l, Obstacle o) {
        l.attachToBody(o.getBody(), l.getX(), l.getY(), l.getDirection());
        l.setActive(true);
    }

    /**
     * Create a point light.
     *
     * @param dist The distance of the light.
     * @return The {@code PointSource} instance.
     */
    protected PointSource createPointLight(float dist) {
        float[] pos = {0, 0};

        // Create point source.
        PointSource p = new PointSource(rayhandler, RAYS, Color.WHITE, dist, pos[0], pos[1]);
        p.setSoft(true);

        // Set up filter.
        Filter f = new Filter();
        f.categoryBits = f.maskBits = 0;
        p.setContactFilter(f);
        p.setActive(false);

        return p;
    }

    protected <T extends Obstacle & ILight>
    void updateLightsForList(Collection<T> list, Map<T, PointSource> lightMap) {
        // First step: Remove lights of things that are no longer in the list.
        Set<Map.Entry<T, PointSource>> entrySet = lightMap.entrySet();
        entrySet.removeIf(i -> {
            if (!list.contains(i.getKey())) {
                PointSource l = i.getValue();
                l.setActive(false);
                l.remove();
                return true;
            }
            return false;
        });

        // Second step: Update light radii for lights already there.
        for (Map.Entry<T, PointSource> entry : entrySet) {
            entry.getValue().setDistance(entry.getKey().getLightRadius());
            entry.getValue().setColor(entry.getKey().getLightColor());
        }

        // Last step: Create lights for new things in the list.
        list.stream().filter(i -> !lightMap.containsKey(i)).forEach(i -> {
            PointSource f = createPointLight(i.getLightRadius());
            f.setColor(i.getLightColor());
            attachLightTo(f, i);
            lightMap.put(i, f);
        });
    }

    /**
     * Update all lights, call this before {@code draw()}.
     *
     * @param flares A collection of flares.
     * @param enemies A collection of enemies.
     */
    public void updateLights(Collection<FlareModel> flares, Collection<EnemyModel> enemies, Collection<FireballModel> fireballs) {
        // Update debug.
        if (debug) {
            rayhandler.setAmbientLight(.5f, .5f, .5f, 0);
        } else {
            rayhandler.setAmbientLight(0, 0, 0, 0);
        }

        // Update raycamera.
        raycamera.position.set(player.getX(), player.getY(), 0);
        raycamera.update();
        rayhandler.setCombinedMatrix(raycamera);

        // Update player light.
        playerLight.setDistance(player.getLightRadius());

        // Update flare lights.
        updateLightsForList(flares, flareLights);
        updateLightsForList(fireballs, fireballLights);

        // Update enemy lights.
        updateLightsForList(
                enemies.stream().filter(EnemyModel::isActivated).collect(Collectors.toList()),
                enemyLights);

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
