package com.fallenflame.game;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.fallenflame.game.enemies.EnemyModel;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class FogController {
    private ParticleEffectPool fogPool;
    private fogParticle[][] fog;
    private LevelModel levelModel;
    private PlayerModel playerModel; //Needed for light radius
    private List<FlareModel> flareModels;//Needed for flare light radius
    private List<EnemyModel> enemyModels; //Needed to determine if an enemy is agressive.
    private int tileGridW;
    private int tileGridH;
    private final int NUM_FOG_ENEMIES = 12;
    private final int NUM_FOG_SHOOTER = 3;
    private float tileSize;
    private final int NUM_FOG_NORMAL = 1;
    private final float NUM_FOG_AROUND_ENEMIES = 11.0f;
    private static Logger log = Logger.getLogger("FogController");

    private final int[] DIRECTIONS = {1, -1};
    public void initialize(ParticleEffect fogTemplate, LevelModel lm, PlayerModel pm, List<FlareModel> fm, List<EnemyModel> em) {
        /*Using a pool doesn't actually help much, as if the number of models is higher than the max it just makes a new
        object. However, it has a slight performance help in terms of reusing objects. 100 is a random value, can be changed*/
        fogPool = new ParticleEffectPool(fogTemplate, 50, 500);
        levelModel = lm;
        playerModel = pm;
        flareModels = fm;
        enemyModels = em;
        int[] n = levelModel.tileGridSize();
        tileGridW = n[0];
        tileGridH = n[1];
        /*Using a 2D array of an array (called fogParticle) to keep track of which fog particles are complete and need
        * new particles versus which ones do not. This fixes the initial issue of us creating 10,000 fog particles as
        * fog particles were created whether or not the particle around that tile was complete*/
        fog = new fogParticle[tileGridW][tileGridH];
        tileSize = levelModel.getTileSize();
    }

    public void updateFogAndDraw(GameCanvas canvas, Vector2 scale, float delta) {
        // Cache values locally so we don't have to do expensive calculations each loop.
        float px = playerModel.getX(), py = playerModel.getY(), lightRadius = playerModel.getLightRadius();
        // Camera pos:
        Vector3 cameraPos = canvas.getCamera().position;
        // These are the ratio to translate camera pos to tile pos.
        float ratioX = scale.x * tileSize, ratioY = scale.y * tileSize;
        // Bounds of the camera in tile units. Could be out of bounds on tile map! (e.g. lowX could be -3)
        int lowX = (int) Math.floor((cameraPos.x - canvas.getWidth() / 2f) / ratioX),
                highX = (int) Math.floor((cameraPos.x + canvas.getWidth() / 2f) / ratioX),
                lowY = (int) Math.floor((cameraPos.y - canvas.getHeight() / 2f) / ratioY),
                highY = (int) Math.floor((cameraPos.y + canvas.getHeight() / 2f) / ratioY);
        for (int x = 0; x < tileGridW; x++) {
            for (int y = 0; y < tileGridH; y++) {
                // If this tile is not in camera, clear its content.
                if (x < lowX || x >= highX || y < lowY || y >= highY) {
                    if (fog[x][y] != null) {

                        for (ParticleEffectPool.PooledEffect effect : fog[x][y].fogParticles) {
                            effect.setDuration(0);
                            if (effect.isComplete()) {
                                effect.free();
                                fog[x][y].fogParticles.removeValue(effect, true);
                                //This will just remove it from drawing again, it will not automatically remove particles
                            }
                        }
                    }
                    continue;
                }
                //To prevent drawing on tiles with the player or a wall as well as if its within the light radius
                if (levelModel.hasWall(x, y)) continue;

                //0.25 accounts for the aligning of the light to show the player's face instead of just the feet.
                boolean withinLight = (Math.pow((Math.pow((x * tileSize) - (playerModel.getX()), 2) +
                        Math.pow((y * tileSize) - (playerModel.getY() + 0.25), 2)), 0.5))
                        <= playerModel.getLightRadius()-(playerModel.getLightRadius()/4);

                Array<ParticleEffectPool.PooledEffect> fogArr;
                if (withinLight || levelModel.hasPlayer(x, y)) {
                    if (fog[x][y] != null) {
                        fogArr = fog[x][y].fogParticles;
                        for (ParticleEffectPool.PooledEffect effect : fogArr) {
                            effect.setDuration(0);
                            effect.free();
                            fogArr.removeValue(effect, true);
                        }
                    }
                } else {
                    Iterator<FlareModel> iterator = flareModels.iterator();
                    while (iterator.hasNext() && !withinLight) {
                        FlareModel flare = iterator.next();
                        withinLight = (Math.pow((Math.pow((x * tileSize) - (flare.getX()), 2) +
                                Math.pow((y * tileSize) - (flare.getY()), 2)), 0.5))
                                <= flare.getLightRadius()- flare.getLightRadius()/4;
                    }
                    if (withinLight) {
                        if (fog[x][y] != null) {
                            fogArr = fog[x][y].fogParticles;
                            for (ParticleEffectPool.PooledEffect effect : fogArr) {
                                effect.setDuration(0);
                                effect.free();
                                fogArr.removeValue(effect, true);
                            }
                        }
                    } else {
                        Iterator<EnemyModel> enemyIterator = enemyModels.iterator();
                        while (enemyIterator.hasNext() && !withinLight) {
                            EnemyModel enemy = enemyIterator.next();
                            withinLight = (enemy.isActivated()) && (levelModel.screenToTile(enemy.getX()) == x) &&
                                    (levelModel.screenToTile(enemy.getY()) == y);
                        }
                        if (withinLight) {
                            if (fog[x][y] != null) {
                                fogArr = fog[x][y].fogParticles;
                                for (ParticleEffectPool.PooledEffect effect : fogArr) {
                                    effect.setDuration(0);
                                    effect.free();
                                    fogArr.removeValue(effect, true);
                                    //This will just remove it from drawing again, it will not automatically remove particles
                                }
                            }
                        } else {
                            if (fog[x][y] == null) {
                                fog[x][y] = new fogParticle();
                            }
                            fogArr = fog[x][y].fogParticles;
                        if ((fog[x][y].enemies != null || fogArr.size > NUM_FOG_NORMAL)  && !levelModel.hasEnemy(x, y)) {
                                for (ParticleEffectPool.PooledEffect effect : fogArr) {
                                    effect.setDuration(0);
                                    effect.free();
                                    fogArr.removeValue(effect, true);
                                }
                                fog[x][y].enemies = null;
                                ParticleEffectPool.PooledEffect effect = fogPool.obtain();
                                effect.reset();
                            effect.setPosition((levelModel.tileToScreen(x) * scale.x), levelModel.tileToScreen(y) * scale.y);
                            fog[x][y].fogParticles.add(effect);
                            }
                            /*Only make a new fog particle if we do not have enough particles in the array for that tile*/
                            if (fogArr.size < NUM_FOG_NORMAL || levelModel.hasEnemy(x, y) && fogArr.size < ( levelModel.hasLessFog(x,y) ? NUM_FOG_SHOOTER : NUM_FOG_ENEMIES)) {
                                for (int i = 0; i < ((levelModel.hasEnemy(x, y) ? levelModel.hasLessFog(x, y) ? NUM_FOG_SHOOTER : NUM_FOG_ENEMIES : NUM_FOG_NORMAL)); i++) {
                                    ParticleEffectPool.PooledEffect effect = fogPool.obtain();
                                    effect.reset();
                                    float incX = levelModel.hasEnemy(x, y) ? (float) ((Math.random() - 0.5) * NUM_FOG_AROUND_ENEMIES) : 0;
                                    float incY = levelModel.hasEnemy(x, y) ? (float) ((Math.random() - 0.5) * NUM_FOG_AROUND_ENEMIES) : 0;
                                    float randomVal = levelModel.hasEnemy(x, y) ? 6.0f : 1.0f;
                                    float randomX = levelModel.hasEnemy(x, y) ? (float) (((Math.random() - 0.5f)*randomVal))*tileSize : 0;
                                    float randomY = levelModel.hasEnemy(x, y) ? (float) (((Math.random() - 0.5f)*randomVal))*tileSize : 0;
                                    effect.setPosition((levelModel.tileToScreen((int) ((x + incX))) + randomX) * scale.x, levelModel.tileToScreen((int) ((y+incY + randomY))) * scale.y);
                                    fog[x][y].fogParticles.add(effect);
                                }
                            }
                        }
                    }
                }
            }
        }
        canvas.begin();
        canvas.drawFog(fog, delta);
        canvas.end();
    }
/**Inner class to represent the fog on one tile
 *fogparticles: An Array of Pooled Effects. Need to use Array as this list's length will vary as things are added/removed
 * */
    protected class fogParticle {
        protected Array<ParticleEffectPool.PooledEffect> fogParticles;
        protected EnemyModel enemies;

        /**Creates a new fogParticle with an empty array*/
        public fogParticle(){
            fogParticles = new Array<>();
            enemies = null;
        }

    }
}

