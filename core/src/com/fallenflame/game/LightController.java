package com.fallenflame.game;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.physics.lights.LightSource;
import com.fallenflame.game.physics.lights.PointSource;
import com.fallenflame.game.physics.obstacle.Obstacle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LightController {
    private static final int RAYS = 512;
    protected PlayerModel player;
    protected JsonValue lightingConfig;
    protected PointSource playerLight;
    protected Map<FlareModel, PointSource> flareLights;
    protected Map<EnemyModel, PointSource> enemyLights;
    protected OrthographicCamera raycamera;
    protected RayHandler rayhandler;
    public void initialize(PlayerModel player, JsonValue levelLighting, World world, Vector2 bound) {
        this.player = player;
        this.lightingConfig = levelLighting;
        playerLight = createPointLight(player.getLightRadius());
        attachLightTo(playerLight, player);
        this.flareLights = new HashMap<>();
        this.enemyLights = new HashMap<>();
        raycamera = new OrthographicCamera(bound.x, bound.y);
        // TODO: This defo doesn't work. Need testing.
        raycamera.position.set(player.getX(), player.getY());
        raycamera.update();
        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);
        rayhandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        rayhandler.setCombinedMatrix(raycamera);
        rayhandler.setAmbientLight(0, 0, 0, 0);
        rayhandler.setBlur(true);
        rayhandler.setBlurNum(3);
    }
    public void dispose() {
        this.player = null;
        this.lightingConfig = null;
        this.playerLight = null;
        this.flareLights.clear();
        this.flareLights = null;
        this.enemyLights.clear();
        this.enemyLights = null;
    }
    protected void attachLightTo(PointSource l, Obstacle o) {
        l.attachToBody(o.getBody(), l.getX(), l.getY(), l.getDirection());
        l.setActive(true);
    }
    protected PointSource createPointLight(float dist) {
        float[] pos = {0, 0};

        PointSource p = new PointSource(rayhandler, RAYS, Color.WHITE, dist, pos[0], pos[1]);
        p.setSoft(true);

        Filter f = new Filter();
        f.categoryBits = f.maskBits = 0;
        p.setContactFilter(f);
        p.setActive(false);

        return p;
    }
    public void updateLights(PlayerModel player, List<FlareModel> flares, List<EnemyModel> enemies) {
        playerLight.setDistance(player.getLightRadius());
        flareLights.keySet().stream().filter(i -> !flares.contains(i)).forEach(i -> {
            PointSource f = flareLights.get(i);
            f.setActive(false);
            f.dispose();
            flareLights.remove(i);
        });
        flares.stream().filter(i -> !flareLights.containsKey(i)).forEach(i -> {
            PointSource f = createPointLight(i.getLightRadius());
            attachLightTo(f, i.getBody());
            flareLights.put(i, f);
        });
        enemyLights.keySet().stream().filter(i -> !enemies.contains(i) || !i.getActivated()).forEach(i -> {
            PointSource f = enemyLights.get(i);
            f.setActive(false);
            f.dispose();
            enemyLights.remove(i);
        });
        enemies.stream().filter(i -> !enemyLights.containsKey(i)).forEach(i -> {
            PointSource f = createPointLight(i.getLightRadius());
            attachLightTo(f, i.getBody());
            enemyLights.put(i, f);
        });
    }
    public void draw() {
        rayhandler.render();
    }

}
