package com.fallenflame.game.enemies;

import com.badlogic.gdx.utils.JsonValue;

public class EnemyGhostModel extends EnemyModel {
    @Override
    public void initialize(JsonValue json, float[] pos){
        super.initialize(json, pos);
        setSensor(true);
    }

    /** @return True because ghost is always active */
    @Override
    public boolean isActivated() {
        return true;
    }
}
