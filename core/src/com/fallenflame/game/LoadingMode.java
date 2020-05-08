package com.fallenflame.game;
/*
 * LoadingMode.java
 *
 * Asset loading is a really tricky problem.  If you have a lot of sound or images,
 * it can take a long time to decompress them and load them into memory.  If you just
 * have code at the start to load all your assets, your game will look like it is hung
 * at the start.
 *
 * The alternative is asynchronous asset loading.  In asynchronous loading, you load a
 * little bit of the assets at a time, but still animate the game while you are loading.
 * This way the player knows the game is not hung, even though he or she cannot do
 * anything until loading is complete. You know those loading screens with the inane tips
 * that want to be helpful?  That is asynchronous loading.
 *
 * This player mode provides a basic loading screen.  While you could adapt it for
 * between level loading, it is currently designed for loading all assets at the
 * start of the game.
 *
 * This class differs slightly from the labs in that the AssetManager is now a
 * singleton and is not constructed by this class.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.fallenflame.game.util.BGMController;
import com.fallenflame.game.util.JsonAssetManager;
import com.fallenflame.game.util.ScreenListener;

/**
 * Class that provides a loading screen for the state of the game.
 *
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class LoadingMode implements Screen, InputProcessor, ControllerListener {
    // Textures necessary to support the loading screen
    private static final String BACKGROUND_FILE = "textures/loading_new.png";
    private static final String MENU_BACKGROUND_FILE = "textures/m_background.png";
    private static final String FIRE_BUDDY_FILE = "textures/firebuddy_light.png";
    private static final String PROGRESS_FILE = "textures/progressbar.png";
    private static final String PLAY_BTN_FILE = "textures/fireplay.png";
    private static final String FONT_FILE = "fonts/chp-fire.ttf";

    /**
     * Background texture for start-up
     */
    private Texture background;
    /**
     * Background for the menu selection screen
     */
    private Texture menu;
    /**
     * Fire buddy to add to background texture for start-up
     */
    private Texture fireBuddy;
    /**
     * Play button to display when done
     */
    private Texture playButton;
    /**
     * Font for the 3 textures
     */
    protected BitmapFont displayFont;
    /**
     * Texture atlas to support a progress bar
     */
    private Texture statusBar;

    // statusBar is a "texture atlas." Break it up into parts.
    /**
     * Left cap to the status background (grey region)
     */
    private TextureRegion statusBkgLeft;
    /**
     * Middle portion of the status background (grey region)
     */
    private TextureRegion statusBkgMiddle;
    /**
     * Right cap to the status background (grey region)
     */
    private TextureRegion statusBkgRight;
    /**
     * Left cap to the status forground (colored region)
     */
    private TextureRegion statusFrgLeft;
    /**
     * Middle portion of the status forground (colored region)
     */
    private TextureRegion statusFrgMiddle;
    /**
     * Right cap to the status forground (colored region)
     */
    private TextureRegion statusFrgRight;
    /**Layout to get the bounds for the rectanlges*/
    /**
     * Start layout
     */
    private GlyphLayout startLayout;
    /**
     * Select layout
     */
    private GlyphLayout selectLayout;
    /**
     * Controls layout
     */
    private GlyphLayout controlLayout;

    Array<MenuText> menuTextArray;
    /**
     * Default budget for asset loader (do nothing but load 60 fps)
     */
    private static int DEFAULT_BUDGET = 15;
    /**
     * Standard window size (for scaling)
     */
    private static int STANDARD_WIDTH = 800;
    /**
     * Standard window height (for scaling)
     */
    private static int STANDARD_HEIGHT = 700;
    /**
     * Ratio of the bar width to the screen
     */
    private static float BAR_WIDTH_RATIO = 0.66f;
    /**
     * Ratio of the bar height to the screen
     */
    private static float BAR_HEIGHT_RATIO = 0.05f;
    /**
     * Text offset
     */
    private float text_offset;
    /**
     * Height of the progress bar
     */
    private static int PROGRESS_HEIGHT = 30;
    /**
     * Width of the rounded cap on left or right
     */
    private static int PROGRESS_CAP = 15;
    /**
     * Width of the middle portion in texture atlas
     */
    private static int PROGRESS_MIDDLE = 200;
    /**
     * Amount to scale the play button
     */
    private static float BUTTON_SCALE = 0.75f;

    /**
     * AssetManager to be loading in the background
     */
    private AssetManager manager;
    /**
     * Reference to GameCanvas created by the root
     */
    private GameCanvas canvas;
    /**
     * Listener that will update the player mode when we are done
     */
    private ScreenListener listener;

    /**
     * The width of the progress bar
     */
    private int width;
    /**
     * The y-coordinate of the center of the progress bar
     */
    private int centerY;
    /**
     * The x-coordinate of the center of the progress bar
     */
    private int centerX;
    /**
     * The height of the canvas window (necessary since sprite origin != screen origin)
     */
    private int heightY;
    /**
     * Scaling factor for when the student changes the resolution.
     */
    private float scale;
    /**
     * The typical color for the font
     */
    private Color normalFontColor;
    /**
     * The color of the font on the menu screen when hovered
     */
    private Color hoveredFontColor;
    /**
     * Current progress (0 to 1) of the asset manager
     */
    private float progress;
    /**
     * The current state of the play button
     */
    private int pressState;
    /**
     * The amount of time to devote to loading assets (as opposed to on screen hints, etc.)
     */
    private int budget;
    /**
     * Whether or not this player mode is still active
     */
    private boolean active;
    /**
     * Things to make the font pretty
     */
    private FreeTypeFontGenerator generator;
    private FreeTypeFontGenerator.FreeTypeFontParameter parameter;

    /** Is the next screen control? */
    public boolean toControl = false;

    /**
     * Returns the budget for the asset loader.
     * <p>
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @return the budget in milliseconds
     */
    public int getBudget() {
        return budget;
    }

    /**
     * Sets the budget for the asset loader.
     * <p>
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param millis the budget in milliseconds
     */
    public void setBudget(int millis) {
        budget = millis;
    }

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == 2;
    }

    /**
     * Creates a LoadingMode with the default budget, size and position.
     *
     * @param manager The AssetManager to load in the background
     */
    public LoadingMode(GameCanvas canvas) {
        this(canvas, DEFAULT_BUDGET);
    }

    /**
     * Creates a LoadingMode with the default size and position.
     * <p>
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param manager The AssetManager to load in the background
     * @param millis  The loading budget in milliseconds
     */
    public LoadingMode(GameCanvas canvas, int millis) {
        this.manager = JsonAssetManager.getInstance();
        this.canvas = canvas;
        budget = millis;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(), canvas.getHeight());

        // Load the next four images immediately.
        playButton = null;
        text_offset = -(canvas.getHeight() / 7);
        background = new Texture(BACKGROUND_FILE);
        menu = new Texture(MENU_BACKGROUND_FILE);
        statusBar = new Texture(PROGRESS_FILE);
        fireBuddy = new Texture(FIRE_BUDDY_FILE);
        normalFontColor = new Color(242, 242, 242, 1);
        hoveredFontColor = new Color(152, 243, 255, 1);
        startLayout = new GlyphLayout();
        selectLayout = new GlyphLayout();
        controlLayout = new GlyphLayout();

        // No progress so far.
        progress = 0;
        pressState = 0;
        active = false;
        menuTextArray = new Array<>();
        // Break up the status bar texture into regions
        statusBkgLeft = new TextureRegion(statusBar, 0, 0, PROGRESS_CAP, PROGRESS_HEIGHT);
        statusBkgRight = new TextureRegion(statusBar, statusBar.getWidth() - PROGRESS_CAP, 0, PROGRESS_CAP, PROGRESS_HEIGHT);
        statusBkgMiddle = new TextureRegion(statusBar, PROGRESS_CAP, 0, PROGRESS_MIDDLE, PROGRESS_HEIGHT);

        int offset = statusBar.getHeight() - PROGRESS_HEIGHT;
        statusFrgLeft = new TextureRegion(statusBar, 0, offset, PROGRESS_CAP, PROGRESS_HEIGHT);
        statusFrgRight = new TextureRegion(statusBar, statusBar.getWidth() - PROGRESS_CAP, offset, PROGRESS_CAP, PROGRESS_HEIGHT);
        statusFrgMiddle = new TextureRegion(statusBar, PROGRESS_CAP, offset, PROGRESS_MIDDLE, PROGRESS_HEIGHT);

        // Let ANY connected controller start the game.
//        for (Controller controller : Controllers.getControllers()) {
//            controller.addListener(this);
//        }
        active = true;
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        statusBkgLeft = null;
        statusBkgRight = null;
        statusBkgMiddle = null;

        statusFrgLeft = null;
        statusFrgRight = null;
        statusFrgMiddle = null;
        startLayout = null;
        selectLayout = null;
        controlLayout = null;
        background.dispose();
        menu.dispose();
        fireBuddy.dispose();
        if (displayFont != null) {
            displayFont.dispose();
            displayFont = null;
        }
        menuTextArray = null;
        statusBar.dispose();
        background = null;
        menu = null;
        fireBuddy = null;
        statusBar = null;
        if (playButton != null) {
            playButton.dispose();
            playButton = null;
        }
    }

    /**
     * Update the status of this player mode.
     * <p>
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        if (progress >= 1) {
            BGMController.startBGM("menu-music", true);
        }
        if (playButton == null) {
            manager.update(budget);
            this.progress = manager.getProgress();
            if (progress >= 1.0f) {
                this.progress = 1.0f;
                // Right now assets don't load until the user clicks play button.
                // This sends asignal to GDXRoot to load assets.
                listener.exitScreen(this, 420);
                playButton = new Texture(PLAY_BTN_FILE);
                playButton.setFilter(TextureFilter.Linear, TextureFilter.Linear);
                generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_FILE));
                parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = 44;
                parameter.shadowOffsetX = 0;
                parameter.shadowOffsetY = 4;
                parameter.color = Color.WHITE;
                displayFont = generator.generateFont(parameter);
                generator.dispose();
                displayFont.setColor(normalFontColor);
                startLayout.setText(displayFont, "Start");
                selectLayout.setText(displayFont, "Level Select");
                controlLayout.setText(displayFont, "Controls");
                if(menuTextArray.size < 3) {
                    menuTextArray.add(new MenuText(false, "Start", new Rectangle(
                            (canvas.getWidth() - startLayout.width) / 2, (canvas.getHeight() - startLayout.height) / 2,
                            startLayout.width, startLayout.height), 0));
                    menuTextArray.add(new MenuText(false, "Select", new Rectangle(
                            (canvas.getWidth() - selectLayout.width) / 2, (canvas.getHeight() - selectLayout.height) / 2 + text_offset,
                            selectLayout.width, selectLayout.height), 1));
                    menuTextArray.add(new MenuText(false, "Controls", new Rectangle(
                            (canvas.getWidth() - controlLayout.width) / 2, (canvas.getHeight() - controlLayout.height ) / 2 + (text_offset * 2),
                            controlLayout.width, controlLayout.height), 2));
                }
            }
        }
    }

    /**
     * Draw the status of this player mode.
     * <p>
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.beginWithoutCamera();
        if (playButton == null) {
            canvas.draw(background, 0, 0);
            canvas.draw(fireBuddy, centerX / 1.20f, centerY / .15f);
            drawProgress(canvas);
        } else {
            canvas.draw(menu, 0, 0);
            for(MenuText mt: menuTextArray){
                if(mt.isHovered){
                    displayFont.setColor(Color.CYAN);
                }
                else{
                    displayFont.setColor(Color.WHITE);
                }
                canvas.drawTextCentered(mt.text, displayFont, text_offset*mt.offset);
            }
        }
        canvas.end();
    }

    /**
     * Updates the progress bar according to loading progress
     * <p>
     * The progress bar is composed of parts: two rounded caps on the end,
     * and a rectangle in a middle.  We adjust the size of the rectangle in
     * the middle to represent the amount of progress.
     *
     * @param canvas The drawing context
     */
    private void drawProgress(GameCanvas canvas) {
        canvas.draw(statusBkgLeft, Color.WHITE, centerX - width / 2, centerY, scale * PROGRESS_CAP, scale * PROGRESS_HEIGHT);
        canvas.draw(statusBkgRight, Color.WHITE, centerX + width / 2 - scale * PROGRESS_CAP, centerY, scale * PROGRESS_CAP, scale * PROGRESS_HEIGHT);
        canvas.draw(statusBkgMiddle, Color.WHITE, centerX - width / 2 + scale * PROGRESS_CAP, centerY, width - 2 * scale * PROGRESS_CAP, scale * PROGRESS_HEIGHT);

        canvas.draw(statusFrgLeft, Color.WHITE, centerX - width / 2, centerY, scale * PROGRESS_CAP, scale * PROGRESS_HEIGHT);
        if (progress > 0) {
            float span = progress * (width - 2 * scale * PROGRESS_CAP) / 2.0f;
            canvas.draw(statusFrgRight, Color.WHITE, centerX - width / 2 + scale * PROGRESS_CAP + span, centerY, scale * PROGRESS_CAP, scale * PROGRESS_HEIGHT);
            canvas.draw(statusFrgMiddle, Color.WHITE, centerX - width / 2 + scale * PROGRESS_CAP, centerY, span, scale * PROGRESS_HEIGHT);
        } else {
            canvas.draw(statusFrgRight, Color.WHITE, centerX - width / 2 + scale * PROGRESS_CAP, centerY, scale * PROGRESS_CAP, scale * PROGRESS_HEIGHT);
        }
    }

    // ADDITIONAL SCREEN METHODS

    /**
     * Called when the Screen should render itself.
     * <p>
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            // We are are ready, notify our listener
            if (isReady() && listener != null) {
                listener.exitScreen(this, 0);
            }
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

        this.width = (int) (BAR_WIDTH_RATIO * width);
        centerY = (int) (BAR_HEIGHT_RATIO * height);
        centerX = width / 2;
        heightY = height;
    }

    /**
     * Called when the Screen is paused.
     * <p>
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     * <p>
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
        pressState = 0;
        for (MenuText mt : menuTextArray) {
            mt.isHovered = false;
        }
    }

    /**
     * Sets the ScreenListener for this mode
     * <p>
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    // PROCESSING PLAYER INPUT

    /**
     * Called when the screen was touched or a mouse button was pressed.
     * <p>
     * This method checks to see if the play button is available and if the click
     * is in the bounds of the play button.  If so, it signals the that the button
     * has been pressed and is currently down. Any mouse button is accepted.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (displayFont == null || pressState == 2) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY - screenY;
        for(MenuText mt: menuTextArray){
            if(mt.rect.contains(screenX, screenY)){
                pressState = 1;
                break;
            }
        }
        toControl = (menuTextArray.get(2).rect.contains(screenX, screenY));
        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     * <p>
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressState == 1) {
            pressState = 2;
            return false;
        }
        return true;
    }

    /**
     * Called when a button on the Controller was pressed.
     * <p>
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown(Controller controller, int buttonCode) {
        return false;
    }

    /**
     * Called when a button on the Controller was released.
     * <p>
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    // UNSUPPORTED METHODS FROM InputProcessor

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        return true;
    }

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @param keycode the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char character) {
        return true;
    }

    /**
     * Called when a key is released.
     * <p>
     * We allow key commands to start the game this time.
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.N || keycode == Input.Keys.P) {
            pressState = 2;
            return false;
        }
        return true;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        screenY = heightY - screenY;
        if (displayFont != null && menuTextArray.size > 0) //If this isn't null, nor are the layouts
        {
            for(MenuText mt : menuTextArray){
                mt.isHovered = mt.rect.contains(screenX, screenY);
            }
        }
        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param amount the amount of scroll from the wheel
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(int amount) {
        return false;
    }

    /**
     * Called when the mouse or finger was dragged. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    // UNSUPPORTED METHODS FROM ControllerListener

    /**
     * Called when a controller is connected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void connected(Controller controller) {
    }

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected(Controller controller) {
    }

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     * <p>
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode   The axis moved
     * @param value      The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return true;
    }

    /**
     * Called when a POV on the Controller moved. (UNSUPPORTED)
     * <p>
     * The povCode is controller specific. The value is a cardinal direction.
     *
     * @param controller The game controller
     * @param povCode    The POV controller moved
     * @param value      The direction of the POV
     * @return whether to hand the event to other listeners.
     */
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        return true;
    }

    /**
     * Called when an x-slider on the Controller moved. (UNSUPPORTED)
     * <p>
     * The x-slider is controller specific.
     *
     * @param controller The game controller
     * @param sliderCode The slider controller moved
     * @param value      The direction of the slider
     * @return whether to hand the event to other listeners.
     */
    public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
        return true;
    }

    /**
     * Called when a y-slider on the Controller moved. (UNSUPPORTED)
     * <p>
     * The y-slider is controller specific.
     *
     * @param controller The game controller
     * @param sliderCode The slider controller moved
     * @param value      The direction of the slider
     * @return whether to hand the event to other listeners.
     */
    public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
        return true;
    }

    /**
     * Called when an accelerometer value on the Controller changed. (UNSUPPORTED)
     * <p>
     * The accelerometerCode is controller specific. The value is a Vector3 representing
     * the acceleration on a 3-axis accelerometer in m/s^2.
     *
     * @param controller        The game controller
     * @param accelerometerCode The accelerometer adjusted
     * @param value             A vector with the 3-axis acceleration
     * @return whether to hand the event to other listeners.
     */
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return true;
    }

    protected class MenuText {
        protected boolean isHovered;
        protected String text;
        protected Rectangle rect;
        protected float offset;

        public MenuText(boolean ih, String t, Rectangle r, float off) {
            isHovered = ih;
            text = t;
            rect = r;
            offset = off;
        }

    }
}