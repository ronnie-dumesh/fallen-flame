package com.fallenflame.game;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import static com.fallenflame.game.LevelModel.TILE_SIZE;

public class FogController {
    private ParticleEffectPool fogPool;
    private fogParticle[][] fog;
    private LevelModel levelModel;
    private PlayerModel playerModel; //Needed for light radius
    private int tileGridW;
    private int tileGridH;

    public void initialize(ParticleEffect fogTemplate, LevelModel lm, PlayerModel pm) {
        /*Using a pool doesn't actually help much, as if the number of models is higher than the max it just makes a new
        object. However, it has a slight performance help in terms of reusing objects. 100 is a random value, can be changed*/
        fogPool = new ParticleEffectPool(fogTemplate, 0, 150);
        levelModel = lm;
        playerModel = pm;
        int[] n = levelModel.tileGridSize();
        tileGridW = n[0];
        tileGridH = n[1];
        /*Using a 2D array of an array (called fogParticle) to keep track of which fog particles are complete and need
        * new particles versus which ones do not. This fixes the initial issue of us creating 10,000 fog particles as
        * fog particles were created whether or not the particle around that tile was complete*/
        fog = new fogParticle[tileGridW][tileGridH];
    }

    public void updateFog(Vector2 scale) {
        for (int x = 0; x < tileGridW; x++) {
            for (int y = 0; y < tileGridH; y++) {
                //To prevent drawing on tiles with the player or a wall as well as if its within the light radius
                if (levelModel.hasWall(x, y)) continue;
                boolean withinLight = (Math.pow((Math.pow((x * TILE_SIZE) - (playerModel.getX()), 2) +
                        Math.pow((y * TILE_SIZE) - (playerModel.getY()), 2)), 0.5))
                        <= playerModel.getLightRadius();
                Array<ParticleEffectPool.PooledEffect> fogArr;
                if (withinLight || levelModel.hasPlayer(x, y)) {
                    if (fog[x][y] != null) {
                        fogArr = fog[x][y].fogParticles;
                        for (ParticleEffectPool.PooledEffect effect : fogArr) {
                            if (effect.isComplete()) {
                                effect.free();
                                fogArr.removeValue(effect, true);
                            } else {
                                if (!fog[x][y].durationSet) {
                                    effect.setDuration(50);
                                }
                            }
                        }
                        fog[x][y].durationSet = true;
                    }
                } else {

                    if (fog[x][y] == null) {
                        fog[x][y] = new fogParticle();
                    }
                    fog[x][y].durationSet = false;
                    fogArr = fog[x][y].fogParticles;
                    /*Free up complete fog particles so new ones can hopefully use more of the pool's resources*/
                    for (ParticleEffectPool.PooledEffect effect : fogArr) {
                        if (effect.isComplete()) ;
                        {
                            effect.free();
                            fogArr.removeValue(effect, true);
                        }
                    }
                    /*Only make a new fog particle if we do not have enough particles in the array for that tile*/
                    if (fogArr.size < 2 || (levelModel.hasEnemy(x, y) && fogArr.size < 9)) {
                        ParticleEffectPool.PooledEffect effect = fogPool.obtain();
                        for (int i = 0; i < (1 + (levelModel.hasEnemy(x, y) ? 8 : 1)); i++) {
                            float randomVal = (float) (Math.random() * TILE_SIZE);
                            effect.setPosition((x * TILE_SIZE + randomVal) * scale.x, (y * TILE_SIZE + randomVal) * scale.y);
                            fog[x][y].fogParticles.add(effect);
                        }
                    }
                }
            }
        }
    }

    public void draw(GameCanvas canvas, float delta) {
        canvas.begin();
        canvas.drawFog(fog, delta);
        canvas.end();
    }
/**Inner class to represent the fog on one tile
 *fogparticles: An Array of Pooled Effects. Need to use Array as this list's length will vary as things are added/removed
 * */
    protected class fogParticle {
        //TODO: Figure out a system that doesn't involve making an inner class for the sole purpose of having a 2D array of Arrays
        public Array<ParticleEffectPool.PooledEffect> fogParticles;
        public boolean durationSet;

        /**Creates a new fogParticle with an empty array*/
        public fogParticle(){
            fogParticles = new Array<>();
            durationSet = false;

        }

    }
}

