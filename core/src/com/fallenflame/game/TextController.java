package com.fallenflame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.util.JsonAssetManager;

import java.util.*;

public class TextController {
    private Map<Rectangle, String> texts;
    private List<String> nextMessage = null;
    private float nextMessageAlpha;
    private List<String> prevMessage = null;
    private Rectangle nextMessageRect;
    private float prevMessageAlpha;
    private BitmapFont displayFont;
    private int screenWidth;
//    private int screenHeight;

    public void initialize(JsonValue jsonValue) {
        if (jsonValue == null) return;
        texts = new HashMap<>();
        for (JsonValue textJson : jsonValue) {
            float[] pos = textJson.get("pos").asFloatArray();
            float[] size = textJson.get("size").asFloatArray();
            Rectangle r = new Rectangle(pos[0] - size[0] / 2 , pos[1] - size[1] / 2, size[0], size[1]);
            texts.put(r, textJson.get("text").asString());
        }
        displayFont = JsonAssetManager.getInstance().getEntry("display", BitmapFont.class);
        screenWidth = Gdx.graphics.getWidth();
        prevMessageAlpha = 1;
        nextMessageAlpha = 0;
//        screenHeight = Gdx.graphics.getHeight();
    }

    public void dispose() {
        if (texts != null) {
            texts.clear();
            texts = null;
        }
        if (nextMessage != null) {
            nextMessage.clear();
            nextMessage = null;
        }
        nextMessageRect = null;
    }

    public void update(PlayerModel player) {
        if (texts == null) return;
        nextMessageAlpha = Math.min(nextMessageAlpha + .05f, 1);
        prevMessageAlpha = Math.max(prevMessageAlpha - .05f, 0);
        List<String> current = nextMessage;
        Rectangle currentRect = nextMessageRect;
        nextMessage = null;
        nextMessageRect = null;
        for (Map.Entry<Rectangle, String> ele : texts.entrySet()) {
            if (ele.getKey().contains(player.getX(), player.getY())) {
                if (currentRect == ele.getKey()) {
                    nextMessage = current;
                    nextMessageRect = currentRect;
                } else {
                    prevMessage = current;
                    nextMessage = splitLines(ele.getValue());
                    nextMessageRect = ele.getKey();
                    prevMessageAlpha = nextMessageAlpha;
                    nextMessageAlpha = 0;
                }
            }
        }
        if (nextMessage == null && current != null) {
            prevMessage = current;
            prevMessageAlpha = nextMessageAlpha;
            nextMessageRect = null;
            nextMessage = null;
            nextMessageAlpha = 0;
        }
        if (prevMessage != null && prevMessageAlpha < 0.0001f) {
            prevMessage.clear();
            prevMessage = null;
        }
    }

    private void renderText(GameCanvas canvas, Iterable<String> msg, float alpha) {
        if (msg != null) {
            displayFont.getData().setScale(.5f);
            int i = 0;
            displayFont.setColor(new Color(1,1,1, alpha));
            for (String str : msg) {
                canvas.drawTextFromCenter(str, displayFont, screenWidth / 2, 120 - i * 30);
                i++;
            }
            displayFont.getData().setScale(1);
        }
    }

    public void draw(GameCanvas canvas) {
        if (texts == null || (prevMessage == null && nextMessage == null)) return;
        canvas.beginWithoutCamera();
        renderText(canvas, prevMessage, prevMessageAlpha);
        renderText(canvas, nextMessage, nextMessageAlpha);
        canvas.end();
    }

    private List<String> splitLines(String msg) {
        String[] texts = msg.trim().split(" ");
        LinkedList<String> result = new LinkedList<>();
        result.add("");
        int start = 0;
        int end = 0;
        int all = texts.length;
        displayFont.getData().setScale(.5f);
        while (true) {
            end++;
            if (end > all) {
                displayFont.getData().setScale(1);
                return result;
            }
            String[] currentLine = Arrays.copyOfRange(texts, start, end);
            String cl = String.join(" ", currentLine);
            float pw = new GlyphLayout(displayFont, cl).width;
            if (pw < screenWidth - 20) {
                result.removeLast();
                result.addLast(cl);
            } else {
                start = end = end - 1;
                result.addLast("");
            }
        }
    }
}
