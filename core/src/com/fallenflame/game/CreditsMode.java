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

public class CreditsMode implements Screen, InputProcessor {

    private static final String BACKGROUND_FILE = "textures/s_credits_background.png";
    private Texture background = new Texture(BACKGROUND_FILE);

    private static final String PAGE_PREV_FILE = "textures/ls_back.png";
    private Texture pagePrev = new Texture(PAGE_PREV_FILE);


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
    private int hoverState;

    private static final int BACK_BTN_WIDTH = 60;
    private static final int BACK_BTN_HEIGHT = 30;
    private static final int BACK_BTN_X = 10;
    private static final int BACK_BTN_Y = 10;

    /** Level selected by the player */
    private int levelSelected;

    private int numberUnlocked;

    public CreditsMode(GameCanvas canvas)
    {
        this.canvas  = canvas;
        pressState = 0;

        hoverState = 0; // Plus three for back button, next and prev page

    }


    @Override
    public void show() {
        displayFont = JsonAssetManager.getInstance().getEntry("display", BitmapFont.class);
        BGMController.startBGM("menu-music");
    }

    @Override
    public void render(float delta) {
        canvas.beginWithoutCamera();
        canvas.draw(background, 0, 0);
        displayFont.setColor(Color.BLACK);
        displayFont.getData().setScale(.5f);

        displayFont.setColor(hoverState == 1 ? Color.CYAN : Color.WHITE);
        canvas.drawText("Back", displayFont,BACK_BTN_X, heightY - BACK_BTN_Y);
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
        if (levelSelected >= 0) BGMController.stopBGMIfPlaying("menu-music");
        this.pressState = 0;
        hoverState = 0;
    }

    @Override
    public void dispose() {
        if (levelSelected >= 0) BGMController.stopBGMIfPlaying("menu-music");
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
        if (keycode == Input.Keys.ESCAPE) {
            pressState = 1;
            return true;
        }
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

        int origScreenY = screenY;
        // Flip to match graphics coordinates
        screenY = heightY-screenY;
        if (screenX >= BACK_BTN_X && screenX <= BACK_BTN_X + BACK_BTN_WIDTH &&
                origScreenY >= BACK_BTN_Y && origScreenY <= BACK_BTN_Y + BACK_BTN_HEIGHT) {
            pressState = 1;
            levelSelected = -1;
        }
        return false;


    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        int origScreenY = screenY;
        // Flip to match graphics coordinates
        screenY = heightY-screenY;
        hoverState =
                (screenX >= BACK_BTN_X && screenX <= BACK_BTN_X + BACK_BTN_WIDTH &&
                        origScreenY >= BACK_BTN_Y && origScreenY <= BACK_BTN_Y + BACK_BTN_HEIGHT) ? 1 : 0;
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
