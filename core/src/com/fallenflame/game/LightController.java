package com.fallenflame.game;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.physics.lights.PointSource;
import com.fallenflame.game.physics.obstacle.Obstacle;

import java.util.*;
import java.util.logging.Logger;

/**
 * {@code LightController} manages and renders the light effect of the game.
 *
 * It collects information about the player and enemies, and renders light directly to the graphics engine of LibGDX
 * instead of using {@code GameCanvas}.
 */
public class LightController {
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

    /**
     * Initialise this controller.
     *
     * @param player The player instance.
     * @param levelLighting The lighting JSON config of this level.
     * @param world The instance of Box2D {@code World}.
     * @param bounds The bound of the viewport.
     */
    public void initialize(PlayerModel player, JsonValue levelLighting, World world, Rectangle bounds) {
        dispose();
        
        // Set up camera first.
        raycamera = new OrthographicCamera(bounds.width, bounds.height);

        // set up ray handler.
        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);
        rayhandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        rayhandler.setCombinedMatrix(raycamera);
        rayhandler.setAmbientLight(.5f, .5f, .5f, 0);
        rayhandler.setBlur(true);
        rayhandler.setBlurNum(3);

        // Save player and config.
        this.player = player;
        this.lightingConfig = levelLighting;

        // Create player light.
        playerLight = createPointLight(player.getLightRadius());
        attachLightTo(playerLight, player);

        // Create empty maps for flare and enemy lights.
        this.flareLights = new HashMap<>();
        this.enemyLights = new HashMap<>();
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

    /**
     * Update all lights, call this before {@code draw()}.
     *
     * @param flares A collection of flares.
     * @param enemies A collection of enemies.
     */
    public void updateLights(Collection<FlareModel> flares, Collection<EnemyModel> enemies) {
        raycamera.position.set(player.getX(), player.getY(), 0);
        raycamera.update();
        rayhandler.setCombinedMatrix(raycamera);
        playerLight.setDistance(player.getLightRadius());
        flareLights.keySet().stream().filter(i -> !flares.contains(i)).forEach(i -> {
            PointSource f = flareLights.get(i);
            f.setActive(false);
            f.dispose();
            flareLights.remove(i);
        });
        flares.stream().filter(i -> !flareLights.containsKey(i)).forEach(i -> {
            PointSource f = createPointLight(i.getLightRadius());
            attachLightTo(f, i);
            flareLights.put(i, f);
        });
        // Iterate through all enemy lights and if the enemy is no longer activated or the enemy is gone, remove the light
        Iterator<EnemyModel> iter = enemyLights.keySet().iterator();
        while(iter.hasNext()){
            EnemyModel e = iter.next();
            if(!enemies.contains(e) || !e.getActivated()){
                PointSource f = enemyLights.get(e);
                f.setActive(false);
                f.dispose();
                iter.remove();
            }
        }
        enemies.stream().filter(i -> !enemyLights.containsKey(i)).forEach(i -> {
            PointSource f = createPointLight(i.getLightRadius());
            attachLightTo(f, i);
            enemyLights.put(i, f);
        });
        rayhandler.update();
    }

    /**
     * Update all lights, call this before {@code draw()}.
     *
     * @deprecated Do not use this signature, instead use {@link #updateLights(Collection, Collection)}.
     * @param player Player instance.
     * @param flares List of flares.
     * @param enemies List of enemies.
     */
    public void updateLights(PlayerModel player, List<FlareModel> flares, List<EnemyModel> enemies) {
        updateLights(flares, enemies);
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
