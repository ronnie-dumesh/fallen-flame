package com.fallenflame.game.util;

import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Map;

public class BGMController {
    private static Sound activeBGM;

    private static String currentAssetName;

    private static long activeBGMID = -1;

    private static Map<String, Sound> soundMap = new HashMap<>();

    public static void stopBGM() {
        if (activeBGMID >= 0) activeBGM.stop(activeBGMID);
        activeBGMID = -1;
        currentAssetName = null;
        activeBGM = null;
    }

    public static void stopBGMIfPlaying(String assetName) {
        if (assetName.equals(currentAssetName)) stopBGM();
    }

    private static Sound getFromSoundMap(String assetName) {
        if (soundMap.containsKey(assetName)) return soundMap.get(assetName);
        Sound n = JsonAssetManager.getInstance().getEntry(assetName, Sound.class);
        if (n == null) return null;
        soundMap.put(assetName, n);
        return n;
    }

    public static void startBGM(String assetName, boolean allowFail) {
        if (assetName.equals(currentAssetName)) return;
        if (activeBGM != null) stopBGM();
        activeBGM = getFromSoundMap(assetName);
        if (activeBGM == null && allowFail) {
            return;
        }
        currentAssetName = assetName;
        activeBGMID = -1;
        resumeBGM();
    }

    public static void startBGM(String assetName) {
        startBGM(assetName, false);
    }

    public static void pauseBGM() {
        if (activeBGMID < 0) return;
        activeBGM.pause(activeBGMID);
    }

    public static void resumeBGM() {
        if (activeBGMID >= 0) return;
        activeBGMID = activeBGM.loop(0.06f);
    }
}
