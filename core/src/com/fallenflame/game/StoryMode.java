package com.fallenflame.game;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.fallenflame.game.util.BGMController;
import com.fallenflame.game.util.JsonAssetManager;
import com.fallenflame.game.util.ScreenListener;

import java.util.HashMap;


public class StoryMode implements Screen, InputProcessor {
    private static final String BACKGROUND_FILE = "textures/s_credits_background.png";
    private Texture background = new Texture(BACKGROUND_FILE);

    private static final String PAGE_PREV_FILE = "textures/ls_back.png";
    private Texture pagePrev = new Texture(PAGE_PREV_FILE);

    private static final String PAGE_NEXT_FILE = "textures/ls_forward.png";
    private Texture pageNext = new Texture(PAGE_NEXT_FILE);

    private static final String INTRO_1 = "textures/intro_page1.png";
    private static final String INTRO_2 = "textures/intro_page2.png";
    private static final String INTRO_3 = "textures/intro_page3.png";
    private static final String VOLCANO_1 = "textures/volcano_page1.png";

    /**
     * Display font
     */
    protected BitmapFont displayFont;

    /**
     * Reference to GameCanvas created by the root
     */
    private GameCanvas canvas;

    public HashMap<Integer, Story> storyTextures;

    /**
     * Listener that will update the player mode when we are done
     */
    private ScreenListener listener;

    /**
     * Standard window size (for scaling)
     */
    private static int STANDARD_WIDTH = 800;
    /**
     * Standard window height (for scaling)
     */
    private static int STANDARD_HEIGHT = 700;

    /**
     * The width of the canvas window
     */
    private int widthX;

    /**
     * The height of the canvas window (necessary since sprite origin != screen origin)
     */
    private int heightY;

    /**
     * Scaling factor for when the student changes the resolution.
     */
    private float scale;

    /**
     * The current state of whether a level button has been pressed
     */
    private int pressState;
    public int storySelected;
    private int page;
    /**
     * The current state of whether any level buttons are being hovered over
     */
    private int[] hoverState;
    /**
     * Position vectors for the next page and prev page buttons
     */
    private Vector2[] nextPrevRel = {new Vector2(1f / 8f, 1f / 2f), new Vector2(7f / 8f, 1f / 2f)};
    private Vector2[] nextPrev;



    public StoryMode(GameCanvas canvas) {
        this.canvas = canvas;
        pressState = 0;
        hoverState = new int[3]; // Plus three for back button, next and prev page
        storyTextures = new HashMap<>();
        nextPrev = new Vector2[nextPrevRel.length];
        nextPrev[0] = new Vector2(canvas.getWidth()/8, canvas.getHeight()/8);
        nextPrev[1] = new Vector2(canvas.getWidth()-(canvas.getWidth()/8), canvas.getHeight()/8);
        for (int i = 0; i < 2; i++) {
            hoverState[i] = 0;
        }
        storySelected = 0;
        page = 0;
        storyTextures.put(0, new Story(new Texture[]{new Texture(INTRO_1), new Texture(INTRO_2), new Texture(INTRO_3)}));
        storyTextures.put(2, new Story(new Texture[]{new Texture(VOLCANO_1)}));

    }


    @Override
    public void show() {
        displayFont = JsonAssetManager.getInstance().getEntry("display", BitmapFont.class);
        BGMController.startBGM("menu-music");
    }

    @Override
    public void render(float delta) {
        canvas.beginWithoutCamera();
        background = storyTextures.get(storySelected).stories[page];
        canvas.draw(background, 0, 0);
        displayFont.setColor(Color.BLACK);
        displayFont.getData().setScale(.5f);
        canvas.draw(pageNext, hoverState[0] == 1 ? Color.CYAN : Color.WHITE, pageNext.getWidth() / 2, pageNext.getHeight() / 2,
                nextPrev[1].x, nextPrev[1].y, 0, 1, 1);
        if(page != 0){
            canvas.draw(pagePrev, hoverState[1] == 1 ? Color.CYAN : Color.WHITE, pagePrev.getWidth() / 2, pagePrev.getHeight() / 2,
                    nextPrev[0].x, nextPrev[0].y, 0, 1, 1);
        }
        canvas.end();
        // We are are ready, notify our listener
        if (isReady() && listener != null) {
            listener.exitScreen(this, 0);
        }
    }

    /**
     * Called when the Screen is resized.
     * <p>
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float) width) / STANDARD_WIDTH;
        float sy = ((float) height) / STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        widthX = width;
        heightY = height;


    }

    public void reset() {
        pressState = 0;
        storySelected = 0;
        page = 0;

    }

    @Override
    public void pause() {
        if(storySelected == 0)BGMController.stopBGMIfPlaying("menu-music");
        this.pressState = 0;
        for (int i = 0, j = hoverState.length; i < j; i++ ) {
            hoverState[i] = 0;
        }

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        this.pressState = 0;

    }

    @Override
    public void dispose() { if(storySelected == 0) BGMController.stopBGMIfPlaying("menu-music"); }


    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == 1;
    }

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }


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

        int origScreenY = screenY;
        // Flip to match graphics coordinates
        screenY = heightY - screenY;

      float  w = scale * pageNext.getWidth() / 2.0f;
        float h = scale * pageNext.getHeight() / 2.0f;

        for (int i = 0; i < nextPrev.length; i++) {
            if ((Math.pow(screenX - nextPrev[i].x, 2) / (w * w)) + (Math.pow(screenY - nextPrev[i].y, 2) / (h * h)) <= 1) {
                if (i == 0) {
                    if (page > 0) {
                        page--;
                    }
                } else {
                    if ((page + 1) < storyTextures.get(storySelected).stories.length) {
                        page++;
                    }
                    else{
                        pressState = 1;
                    }
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
        int origScreenY = screenY;
        // Flip to match graphics coordinates
        screenY = heightY - screenY;
        float w = scale*pageNext.getWidth()/2.0f;
        float h = scale*pageNext.getHeight()/2.0f;

        for (int i = 0; i < nextPrev.length; i++) {
            if ((Math.pow(screenX - nextPrev[i].x, 2) / (w * w)) + (Math.pow(screenY - nextPrev[i].y, 2) / (h * h)) <= 1) {
                if (i == 0) {
                    hoverState[0] = 1;
                } else {
                    hoverState[1] = 1;
                }
            }
        }
        return false;
    }
    @Override
    public boolean scrolled(int amount) {
        return false;
    }

   protected class Story{
        Texture[] stories;
        public Story(Texture[] arr){
            stories = arr;
        }
    }
}
