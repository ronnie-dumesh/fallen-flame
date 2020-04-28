package com.fallenflame.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.controllers.*;
import com.fallenflame.game.util.*;

import com.fallenflame.game.util.JsonAssetManager;

public class LevelSelectMode implements Screen, InputProcessor {

    /** Textures temporarily loaded in so I have something to look at as I set up this class */
    private static final String PLAY_BTN_FILE = "textures/fireplay.png";
    private Texture playButton = new Texture(PLAY_BTN_FILE);
    private static final String BACKGROUND_FILE = "textures/firelevelselect.png";
    private Texture background = new Texture(BACKGROUND_FILE);

    private static final String LEVEL_BTN_FILE = "textures/particle.png";
    private Texture levelButton = new Texture(LEVEL_BTN_FILE);

    /** Position vectors for all the level select buttons */
    private Vector2[] posVec = {new Vector2(50,200),new Vector2(150,300),new Vector2(250,225),new Vector2(350,100),new Vector2(425,250),new Vector2(500,375),new Vector2(550,150),new Vector2(625,300),new Vector2(700,100),new Vector2(750,250)};

    /** Amount to scale the play button */
    private static float BUTTON_SCALE  = 0.75f;

    /** Display font */
    protected BitmapFont displayFont;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;

    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;

    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The current state of whether a level button has been pressed */
    private int   pressState;

    /** The current state of whether any level buttons are being hovered over */
    private int[] hoverState;

    /** Level selected by the player */
    private int levelSelected;


    public LevelSelectMode(GameCanvas canvas)
    {
        this.canvas  = canvas;
        pressState = 0;
        hoverState = new int[posVec.length];
        for (int i = 0; i < posVec.length; i++) {
            hoverState[i] = 0;
        }
    }

    @Override
    public void show() {
        displayFont = JsonAssetManager.getInstance().getEntry("display", BitmapFont.class);
    }

    @Override
    public void render(float delta) {
        canvas.begin();
        canvas.draw(background, 0, 0);
        canvas.drawTextCentered("Level Select", displayFont, 250);
        for (int i = 0; i < posVec.length; i++) {
            if (hoverState[i] != 1) {
                canvas.draw(levelButton, Color.WHITE, levelButton.getWidth() / 2, levelButton.getHeight() / 2,
                        posVec[i].x, posVec[i].y, 0, 1, 1);
            } else {
                canvas.draw(levelButton, Color.ORANGE, levelButton.getWidth() / 2, levelButton.getHeight() / 2,
                        posVec[i].x, posVec[i].y, 0, 1, 1);
            }
            canvas.drawText("" + (i + 1), displayFont, posVec[i].x - 14, posVec[i].y + 80);
        }
        canvas.end();
        // We are are ready, notify our listener
        if (isReady() && listener != null) {
            listener.exitScreen(this, 0);
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);


        heightY = height;
    }

    public void reset() { pressState = 0; }

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

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == 1;
    }

    public void setScreenListener(ScreenListener listener) { this.listener = listener; }

    public int getLevelSelected() {return levelSelected;}

    // PROCESSING PLAYER INPUT

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

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        if (playButton == null || pressState == 1) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        // Level button is a circle.
        float radius = BUTTON_SCALE*scale*levelButton.getWidth()/2.0f;
        for (int i = 0; i < posVec.length; i++) {
            float dist = (screenX-posVec[i].x)*(screenX-posVec[i].x)+(screenY-posVec[i].y)*(screenY-posVec[i].y);
            if (dist < radius*radius) {
                pressState = 1;
                levelSelected = i;
            }
        }
        return false;


    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        // Play button is a circle.
        float radius = BUTTON_SCALE*scale*levelButton.getWidth()/2.0f;
        for (int i = 0; i < posVec.length; i++) {
            float dist = (screenX-posVec[i].x)*(screenX-posVec[i].x)+(screenY-posVec[i].y)*(screenY-posVec[i].y);
            if (dist < radius*radius) {
                hoverState[i] = 1;
            } else {
                hoverState[i] = 0;
            }
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
