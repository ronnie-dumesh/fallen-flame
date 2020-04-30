package com.fallenflame.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.fallenflame.game.util.*;

import com.fallenflame.game.util.JsonAssetManager;

public class LevelSelectMode implements Screen, InputProcessor {

    private static final String BACKGROUND_FILE = "textures/ls_background.png";
    private Texture background = new Texture(BACKGROUND_FILE);

    private static final String LEVEL_BTN_FILE = "textures/ls_unlocked_level.png";
    private Texture levelButton = new Texture(LEVEL_BTN_FILE);

    /** Position vectors for all the level select buttons */
    private Vector2[] posVecRel = {new Vector2(1f/4f,2f/3f),new Vector2(3f/8f,2f/3f),new Vector2(1f/2f,2f/3f),new Vector2(5f/8f,2f/3f),new Vector2(3f/4f,2f/3f),new Vector2(1f/4f,1f/3f),new Vector2(3f/8f,1f/3f),new Vector2(1f/2f,1f/3f),new Vector2(5f/8f,1f/3f),new Vector2(3f/4f,1f/3f)};
    private Vector2[] posVec;

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

    /** Level selected by the player */
    private int levelSelected;


    public LevelSelectMode(GameCanvas canvas)
    {
        this.canvas  = canvas;
        pressState = 0;
        posVec = new Vector2[posVecRel.length];
        hoverState = new int[posVecRel.length];
        for (int i = 0; i < posVecRel.length; i++) {
            posVec[i] = new Vector2(0f,0f);
            hoverState[i] = 0;
        }
    }

    @Override
    public void show() {
        displayFont = JsonAssetManager.getInstance().getEntry("display", BitmapFont.class);
        BGMController.startBGM("menu-music");
    }

    @Override
    public void render(float delta) {
        canvas.begin();
        canvas.draw(background, 0, 0);
        displayFont.setColor(Color.BLACK);
        displayFont.getData().setScale(.5f);
        for (int i = 0; i < posVec.length; i++) {
            if (hoverState[i] != 1) {
                canvas.draw(levelButton, Color.WHITE, levelButton.getWidth() / 2, levelButton.getHeight() / 2,
                        posVec[i].x, posVec[i].y, 0, 1, 1);
            } else {
                canvas.draw(levelButton, Color.valueOf("98F3FF"), levelButton.getWidth() / 2, levelButton.getHeight() / 2,
                        posVec[i].x, posVec[i].y, 0, 1, 1);
            }
            canvas.drawTextFromCenter("" + (i + 1), displayFont, posVec[i].x, posVec[i].y - levelButton.getHeight()/5);
        }
        displayFont.setColor(Color.WHITE);
        displayFont.getData().setScale(1f);
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

        widthX = width;
        heightY = height;

        for (int i = 0; i < posVecRel.length; i++) {
            posVec[i] = new Vector2(posVecRel[i].x * widthX,posVecRel[i].y * heightY);
        }
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
        BGMController.stopBGMIfPlaying("menu-music");
    }

    @Override
    public void dispose() {
        BGMController.stopBGMIfPlaying("menu-music");
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

        if (pressState == 1) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        float w = scale*levelButton.getWidth()/2.0f;
        float h = scale*levelButton.getHeight()/2.0f;

        for (int i = 0; i < posVec.length; i++) {
            if ((Math.pow(screenX-posVec[i].x,2) / (w*w)) + (Math.pow(screenY-posVec[i].y,2) / (h*h)) <= 1) {
                //TODO: temporary disable of levels 6-10
                if(i < 5) {
                    pressState = 1;
                    levelSelected = i;
                }
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

        float w = scale*levelButton.getWidth()/2.0f;
        float h = scale*levelButton.getHeight()/2.0f;

        for (int i = 0; i < posVec.length; i++) {
            if ((Math.pow(screenX-posVec[i].x,2) / (w*w)) + (Math.pow(screenY-posVec[i].y,2) / (h*h)) <= 1) {
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
