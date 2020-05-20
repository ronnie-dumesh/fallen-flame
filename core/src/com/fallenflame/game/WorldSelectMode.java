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

    private static final String BACKGROUND_FILE = "textures/ws_background.png";
    private Texture background = new Texture(BACKGROUND_FILE);

    private static final String BACK_BUTTON = "textures/ws_back.png";
    private Texture back = new Texture(BACK_BUTTON);

    private static final String LEVEL_BTN_FILE = "textures/ls_unlocked_level.png";
    private Texture levelButton = new Texture(LEVEL_BTN_FILE);

    private static final String CAVE_COLORED = "textures/cave-colored.png";
    private Texture caveColored = new Texture(CAVE_COLORED);
    private static final String CAVE_HOVER = "textures/cave-hover.png";
    private Texture caveHover = new Texture(CAVE_HOVER);
    private static final String CAVE_UNCOLORED = "textures/cave-uncolored.png";
    private Texture caveUncolored = new Texture(CAVE_UNCOLORED);

    private static final String FOREST_COLORED = "textures/forest-colored.png";
    private Texture forestColored = new Texture(FOREST_COLORED);
    private static final String FOREST_HOVER = "textures/forest-hover.png";
    private Texture forestHover = new Texture(FOREST_HOVER);
    private static final String FOREST_UNCOLORED = "textures/forest-uncolored.png";
    private Texture forestUncolored = new Texture(FOREST_UNCOLORED);

    private static final String VOLCANO_COLORED = "textures/volcano-colored.png";
    private Texture volcanoColored = new Texture(VOLCANO_COLORED);
    private static final String VOLCANO_HOVER = "textures/volcano-hover.png";
    private Texture volcanoHover = new Texture(VOLCANO_HOVER);
    private static final String VOLCANO_UNCOLORED = "textures/volcano-uncolored.png";
    private Texture volcanoUncolored = new Texture(VOLCANO_UNCOLORED);

    private static final String STEPS_CAVE = "textures/steps-to-cave.png";
    private Texture stepsCave = new Texture(STEPS_CAVE);
    private static final String STEPS_FOREST = "textures/steps-to-forest.png";
    private Texture stepsForest = new Texture(STEPS_FOREST);
    private static final String STEPS_VOLCANO = "textures/steps-to-volcano.png";
    private Texture stepsVolcano = new Texture(STEPS_VOLCANO);

    /** Position vectors for all the world select buttons */
    private Vector2[] posVecRel = {new Vector2(1f/2f,1f/4f),new Vector2(3f/4f,9f/20f),new Vector2(2f/5f,5f/8f)};
    private Vector2[] posVec;

    /** Position vectors for all the steps */
    private Vector2[] posVecRelSteps = {new Vector2(3f/10f,1f/6f),new Vector2(7f/10f,1f/4f),new Vector2(13f/20f,5f/8f)};
    private Vector2[] posVecSteps;

    /** Vectors for texture types */

    private Texture[] coloredTextures = {caveColored,forestColored,volcanoColored};
    private Texture[] hoverTextures = {caveHover,forestHover,volcanoHover};
    private Texture[] uncoloredTextures = {caveUncolored,forestUncolored,volcanoUncolored};
    private Texture[] stepsTextures = {stepsCave,stepsForest,stepsVolcano};

    /** Scale for items on the world select map */
    private float mapScale = .8f;

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

    private static final int BACK_BTN_WIDTH = 60;
    private static final int BACK_BTN_HEIGHT = 30;
    private static final int BACK_BTN_X = 10;
    private static final int BACK_BTN_Y = 10;

    /** World selected by the player */
    private int worldSelected;

    public WorldSelectMode(GameCanvas canvas)
    {
        this.canvas  = canvas;
        pressState = 0;
        posVec = new Vector2[posVecRel.length];
        posVecSteps = new Vector2[posVecRelSteps.length];
        hoverState = new int[posVecRel.length + 1]; // Plus one for back button
        for (int i = 0; i < posVecRel.length; i++) {
            posVec[i] = new Vector2(0f,0f);
            hoverState[i] = 0;
        }
        for (int i = 0; i < posVecRelSteps.length; i++) {
            posVecSteps[i] = new Vector2(0f,0f);
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
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        if (pressState == 1) {
            return true;
        }

        int origScreenY = screenY;
        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        float w = scale*levelButton.getWidth()/2.0f;
        float h = scale*levelButton.getHeight()/2.0f;

        for (int i = 0; i < posVec.length; i++) {
            if ((Math.pow(screenX-posVec[i].x,2) / (w*w)) + (Math.pow(screenY-posVec[i].y,2) / (h*h)) <= 1) {
                if(true) { //EVENTUALLY CHANGE THIS TO LOGIC FOR IF WORLD IS UNLOCKED
                    pressState = 1;
                    worldSelected = i;
                }
            }
        }

        if (screenX >= BACK_BTN_X && screenX <= BACK_BTN_X + BACK_BTN_WIDTH &&
                origScreenY >= BACK_BTN_Y && origScreenY <= BACK_BTN_Y + BACK_BTN_HEIGHT) {
            pressState = 1;
            worldSelected = -1;
        }

        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {

        int origScreenY = screenY;
        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        for (int i = 0; i < posVec.length; i++) {
            float w = scale*coloredTextures[i].getWidth()/2.0f;
            float h = scale*coloredTextures[i].getHeight()/2.0f;
            if ((Math.pow(screenX-posVec[i].x,2) / (w*w)) + (Math.pow(screenY-posVec[i].y,2) / (h*h)) <= 1) {
                hoverState[i] = 1;
            } else {
                hoverState[i] = 0;
            }
        }

        return false;
    }

    @Override
    public boolean scrolled(int i) {
        return false;
    }

    @Override
    public void show() {
        displayFont = JsonAssetManager.getInstance().getEntry("display", BitmapFont.class);
    }

    @Override
    public void render(float v) {
        canvas.beginWithoutCamera();
        canvas.draw(background, 0, 0);
        for (int i = 0; i < posVec.length; i++) {
            if (hoverState[i] != 1) {
                canvas.draw(hoverTextures[i], Color.WHITE, hoverTextures[i].getWidth() / 2, hoverTextures[i].getHeight() / 2,
                        posVec[i].x, posVec[i].y, 0, mapScale, mapScale);
            } else {
                canvas.draw(coloredTextures[i], Color.WHITE, coloredTextures[i].getWidth() / 2, coloredTextures[i].getHeight() / 2,
                        posVec[i].x, posVec[i].y, 0, mapScale, mapScale);
            }
        }
        for (int i = 0; i < posVecSteps.length; i++) {
                canvas.draw(stepsTextures[i], Color.WHITE, stepsTextures[i].getWidth() / 2, stepsTextures[i].getHeight() / 2,
                        posVecSteps[i].x, posVecSteps[i].y, 0, mapScale, mapScale);
        }
        displayFont.getData().setScale(.5f);
        displayFont.setColor(hoverState[posVec.length] == 1 ? Color.CYAN : Color.WHITE);
        canvas.drawText("Back", displayFont,BACK_BTN_X, heightY - BACK_BTN_Y);
        displayFont.setColor(Color.WHITE);
        displayFont.getData().setScale(1f);
        canvas.end();

        // We are are ready, notify our listener
        if (isReady() && listener != null) {
            listener.exitScreen(this, 0);
        }
    }

    @Override
    public void resize(int width, int height) {
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        widthX = width;
        heightY = height;

        for (int i = 0; i < posVecRel.length; i++) {
            posVec[i] = new Vector2(posVecRel[i].x * widthX,posVecRel[i].y * heightY);
        }
        for (int i = 0; i < posVecRelSteps.length; i++) {
            posVecSteps[i] = new Vector2(posVecRelSteps[i].x * widthX,posVecRelSteps[i].y * heightY);
        }
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

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == 1;
    }

    public void setScreenListener(ScreenListener listener) { this.listener = listener; }

    public int getWorldSelected() {return worldSelected;}
}

