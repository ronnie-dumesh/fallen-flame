package com.fallenflame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.fallenflame.game.util.JsonAssetManager;
import com.fallenflame.game.util.ScreenListener;

import java.util.Arrays;

public class PauseMode implements Screen, InputProcessor {
    private static final String MODAL = "textures/modal.png";
    private final Texture modal = new Texture(MODAL);
    private final GameCanvas canvas;
    private final int[] hoverStates;
    private final Rectangle[] hoverRects;
    private int screenWidth;
    private int screenHeight;
    private BitmapFont displayFont;
    private ScreenListener listener;

    public PauseMode(GameCanvas canvas)
    {
        this.canvas = canvas;
        hoverStates = new int[4];
        hoverRects = new Rectangle[4];
        Arrays.fill(hoverStates, 0);
    }

    public void setScreenListener(ScreenListener listener) { this.listener = listener; }

    @Override
    public void render(float delta) {
        canvas.beginWithoutCamera();
        canvas.draw(new TextureRegion(modal, screenWidth, screenHeight), 0, 0);
        displayFont.getData().setScale(1);
        GlyphLayout gl = new GlyphLayout(displayFont, "Resume");
        float y = screenHeight / 4 * 0.5f;
        displayFont.setColor(hoverStates[0] == 1 ? Color.CYAN : Color.WHITE);
        canvas.drawTextFromCenter("Resume", displayFont, screenWidth / 2, screenHeight - y);
        hoverRects[0] = new Rectangle(screenWidth / 2 - gl.width / 2, y - gl.height / 2, gl.width, gl.height + 10);
        gl = new GlyphLayout(displayFont, "Restart");
        y = screenHeight / 4 * 1.5f;
        displayFont.setColor(hoverStates[1] == 1 ? Color.CYAN : Color.WHITE);
        canvas.drawTextFromCenter("Restart", displayFont, screenWidth / 2, screenHeight - y);
        hoverRects[1] = new Rectangle(screenWidth / 2 - gl.width / 2, y - gl.height / 2, gl.width, gl.height + 10);
        gl = new GlyphLayout(displayFont, "Return to Level Select");
        y = screenHeight / 4 * 2.5f;
        displayFont.setColor(hoverStates[2] == 1 ? Color.CYAN : Color.WHITE);
        canvas.drawTextFromCenter("Return to Level Select", displayFont, screenWidth / 2, screenHeight - y);
        hoverRects[2] = new Rectangle(screenWidth / 2 - gl.width / 2, y - gl.height / 2, gl.width, gl.height + 10);
        gl = new GlyphLayout(displayFont, "Edit Controls");
        y = screenHeight / 4 * 3.5f;
        displayFont.setColor(hoverStates[3] == 1 ? Color.CYAN : Color.WHITE);
        canvas.drawTextFromCenter("Edit Controls", displayFont, screenWidth / 2, screenHeight - y);
        hoverRects[3] = new Rectangle(screenWidth / 2 - gl.width / 2, y - gl.height / 2, gl.width, gl.height + 10);
        canvas.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (Arrays.stream(hoverStates).noneMatch(i -> i == 1)) {
            return false;
        } else {
            for (int i = 0, j = hoverRects.length; i < j; i++) {
                if (hoverRects[i] == null) continue;
                if (hoverRects[i].contains(screenX, screenY)) {
                    listener.exitScreen(this, i);
                }
            }
            return true;
        }
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        for (int i = 0, j = hoverRects.length; i < j; i++) {
            if (hoverRects[i] == null) continue;
            hoverStates[i] = (hoverRects[i].contains(screenX, screenY)) ? 1 : 0;
        }
        return true;
    }

    public void screenshot() {
        if (screenWidth == 0 || screenHeight == 0) {
            screenWidth = Gdx.graphics.getWidth();
            screenHeight = Gdx.graphics.getHeight();
        }
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public void show() {
        displayFont = JsonAssetManager.getInstance().getEntry("display", BitmapFont.class);
    }

    @Override
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
