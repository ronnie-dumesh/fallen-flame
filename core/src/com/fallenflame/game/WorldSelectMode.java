package com.fallenflame.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.fallenflame.game.util.BGMController;
import com.fallenflame.game.util.JsonAssetManager;
import com.fallenflame.game.util.ScreenListener;

public class WorldSelectMode implements Screen, InputProcessor {

    private static final String BACKGROUND_FILE = "textures/ls_background.png";
    private Texture background = new Texture(BACKGROUND_FILE);

    private static final String LEVEL_BTN_FILE = "textures/ls_unlocked_level.png";
    private Texture levelButton = new Texture(LEVEL_BTN_FILE);

    /** Position vectors for all the world select buttons */
    private Vector2[] posVecRel = {new Vector2(1f/4f,2f/3f),new Vector2(3f/8f,2f/3f),new Vector2(1f/2f,2f/3f)};
    private Vector2[] posVec;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;

    /** The width of the canvas window */
    private int widthX;

    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;

    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The current state of whether a level button has been pressed */
    private int   pressState;

    /** The current state of whether any level buttons are being hovered over */
    private int[] hoverState;

    public WorldSelectMode(GameCanvas canvas)
    {
        this.canvas  = canvas;
        pressState = 0;
        posVec = new Vector2[posVecRel.length];
        hoverState = new int[posVecRel.length + 1]; // Plus one for back button
        for (int i = 0; i < posVecRel.length; i++) {
            posVec[i] = new Vector2(0f,0f);
            hoverState[i] = 0;
        }
        for (int i = posVecRel.length; i < posVecRel.length + 1; i++) {
            hoverState[i] = 0;
        }
    }

    @Override
    public boolean keyDown(int i) {
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(int i) {
        return false;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float v) {
        canvas.beginWithoutCamera();
        canvas.draw(background, 0, 0);
        canvas.end();
    }

    @Override
    public void resize(int width, int height) {
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        widthX = width;
        heightY = height;
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

    public void setScreenListener(ScreenListener listener) { this.listener = listener; }
}

