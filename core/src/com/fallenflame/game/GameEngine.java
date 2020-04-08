package com.fallenflame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.util.JsonAssetManager;
import com.fallenflame.game.util.ScreenListener;

/**
 * This class' purpose is explained in detail in the Architecture specifciation.
 * Credit to @author: Professor White for code used in this class
 */

public class GameEngine implements Screen, InputProcessor {
    /**Enum to determine if we are loading or not */
    protected enum AssetState {
        /** No assets loaded */
        EMPTY,
        /** Still loading assets */
        LOADING,
        /** Assets are complete */
        COMPLETE
    }

    /** Exit code for quitting the game */
    public static final int EXIT_QUIT = 0;

    /** How long the game should countdown */
    public static final int COUNTDOWN_TIME = 80;

    private JsonReader jsonReader;
    /** The JSON asset directory */
    private JsonValue assetJson;
    /** The JSON save directory. This will be used to determine what level to */
    private JsonValue saveJson;
    /** The JSON defining the level model */
    private JsonValue levelJson;
    /** Global JSON defining objects */
    private JsonValue globalJson;
    /**What actually keeps track of the assetState. Initially set to empty, as no resources will be in at that point*/
    private AssetState currentAssetState = AssetState.EMPTY;
    /** The font for giving messages to the player */
    protected BitmapFont displayFont;
    /** The font for giving messages when in debug mode */
    protected BitmapFont debugFont;

    /**Main game canvas*/
    protected GameCanvas canvas;
    /** Listener that will update the player mode */
    private ScreenListener listener;
    /** Reference to the level controller */
    protected LevelController level;

    /**Boolean to keep track if the player won the level*/
    private boolean isSuccess;
    /** Boolean to prevent countdown from becoming infinite */
    private boolean prevSuccess;
    /**Boolean to keep track if the player died*/
    private boolean isFailed;
    /**Boolean to keep track if the player had died */
    private boolean prevFailed;
    /**Boolean to keep track if the player paused the game*/
    private boolean isPaused;
    /**Boolean to keep track if the screen is active*/
    private boolean isScreenActive;
    /**Rectangle canvasBounds to keep track of the current canvas size for drawing purposes */
    private Rectangle canvasBounds;
    /** Countdown active for winning or losing */
    private int countdown;

    /** User Input Management Fields */
    /** Whether the reset button was pressed. */
    private boolean resetPressed;
    private boolean resetPrevious;
    /** Whether the debug toggle was pressed. */
    private boolean debugPressed;
    private boolean debugPrevious;
    /** Whether the debug2 toggle was pressed. */
    private boolean debug2Pressed;
    private boolean debug2Previous;
    /** Whether the exit button was pressed. */
    private boolean exitPressed;
    private boolean exitPrevious;
    /** Whether the flare button was pressed. */
    private boolean flarePressed;
    private boolean flarePrevious;
    /** Whether the sprint button was pressed. */
    private boolean sprintPressed;
    private boolean sprintPrevious;
    /** Whether the sneak button was pressed. */
    private boolean sneakPressed;
    private boolean sneakPrevious;
    /** How much did we move horizontally? */
    private float horizontal;
    /** How much did we move vertically? */
    private float vertical;


    /**
     * Preloads the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @author Professor White
     */
    public void preLoadContent() {
        if (currentAssetState != AssetState.EMPTY) {
            return;
        }

        currentAssetState = AssetState.LOADING;

        jsonReader = new JsonReader();
        assetJson = jsonReader.parse(Gdx.files.internal("jsons/assets.json"));
        saveJson = jsonReader.parse(Gdx.files.internal("jsons/save.json"));
        globalJson = jsonReader.parse(Gdx.files.internal("jsons/global.json"));

        JsonAssetManager.getInstance().loadDirectory(assetJson);
    }
    /**
     * Load the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @author: Professor White
     */
    public void loadContent() {
        if (currentAssetState != AssetState.LOADING) {
            return;
        }

        JsonAssetManager.getInstance().allocateDirectory();
        displayFont = JsonAssetManager.getInstance().getEntry("display", BitmapFont.class);
        debugFont = JsonAssetManager.getInstance().getEntry("debug", BitmapFont.class);
        currentAssetState = AssetState.COMPLETE;
    }

    /**
     * Unloads the assets for this game.
     *
     * This method erases the static variables.  It also deletes the associated textures
     * from the asset manager. If no assets are loaded, this method does nothing.
     *
     * @author: Professor White
     */
    public void unloadContent() {
        JsonAssetManager.getInstance().unloadDirectory();
        JsonAssetManager.clearInstance();
    }

    /**Getters and setters*/
    /**Return true if the level is complete
     * @return: boolean that is true if the level is completed*/
    public boolean isSuccess(){return isSuccess;}

    /**Return true if the level has failed
     * @return: boolean that is true if the level is failed*/
    public boolean isFailed(){return isFailed;}

    /**Return true if the level has paused
     * @return: boolean that is true if the level is failed*/
    public boolean isPaused(){return isPaused;}

    /**Set if the level has been paused
     * @param: boolean isPaused that is true if the level is completed*/

    public void setIsPaused(boolean isPaused){
        this.isPaused = isPaused;
    }

    /**Return true if that screen is currently active
     * @return: boolean that is true if the level is failed*/
    public boolean isScreenActive(){return isScreenActive;}

    /**Set if the screen is currently active
     * @param: boolean isScreenActive that is true if the level is completed*/

    public void setIsScreenActive(boolean isScreenActive){
        this.isScreenActive = isScreenActive;
    }

    /** Return the current GameCanvas. This GameCanvas will be used across all models and views
     * @return GameCanvas Canvas
     */
    public GameCanvas getCanvas(){return canvas;}

    /** Set the new GameCanvas*/
    public void setCanvas(GameCanvas canvas){this.canvas = canvas;}

    /**
     * Creates a new game world
     *
     * The physics bounds and drawing scale are now stored in the levelController and
     * defined by the appropriate JSON file.
     *
     * Game does not start out as active or paused, but rather in loading
     *
     * Source: Professor White
     */
    public GameEngine() {
        jsonReader = new JsonReader();
        level = new LevelController();
        isSuccess = false;
        isFailed = false;
        prevFailed = false;
        prevSuccess = false;
        isScreenActive = false;
        isPaused = false;
        canvasBounds = new Rectangle();
        countdown = -1;
    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        level.dispose();
        level  = null;
        canvas = null;
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the level and creates a new one. It will
     * reread from the JSON file, allowing us to make changes on the fly.
     */
    public void reset() {
        level.dispose();
        level = new LevelController();

        isSuccess = false;
        isFailed = false;
        prevFailed = false;
        prevSuccess = false;
         countdown = -1;

        // Reload the json each time
        String currentLevelPath = "jsons/" + saveJson.get("levels").get(0).getString("path"); // Currently just gets first level
        levelJson = jsonReader.parse(Gdx.files.internal(currentLevelPath));
        level.populate(levelJson, globalJson);
        level.setLevelState(LevelController.LevelState.IN_PROGRESS);
        level.getWorld().setContactListener(level);
    }

    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt Number of seconds since last animation frame
     *
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        readInput();
        if (listener == null) {
            return true;
        }

        if (debugPressed && !debugPrevious) {
            level.setDebug(level.getDebug() + 1);
        }
        if (debug2Pressed && !debug2Previous) {
            level.setDebug2(!level.getDebug2());
        }
        if (resetPressed && !resetPrevious) {
            reset();
        }
        if (exitPressed && !exitPrevious) {
            listener.exitScreen(this, EXIT_QUIT);
            return false;
        }
        //If countdown is > -1, then the player must have won or lost. Either continue to show the win condition message
        //Or reset, if the countdown is up.
        else if (countdown > 0) {
            countdown--;
        } else if (countdown == 0) {
            reset();
        }

        return true;
    }

    private Vector2 tempAngle = new Vector2();
    /**
     * The core gameplay loop of this world. This checks if the level has ended
     * @param delta Number of seconds since last animation frame
     */
    public void update(float delta) {
        if (flarePressed && !flarePrevious) {
            level.createFlare(getMousePosition());
        }
        // Rotate the avatar to face the direction of movement
        tempAngle.set(horizontal,vertical);
        tempAngle.setLength(1); // Fix diagonal too-fast issue.
        float angle = 0;
        if (tempAngle.len2() > 0.0f) {
            angle = tempAngle.angle();
            // Convert to radians with up as 0
            angle = (float)Math.PI*(angle-90.0f)/180.0f;
        }
        if (sprintPressed && !sprintPrevious) {
            // If player just started sprinting
            level.makeSprint();
        } else if (!sprintPressed && sprintPrevious) {
            // If player just stopped sprinting
            level.makeWalk();
        }
        if (sneakPressed && !sneakPrevious) {
            // If player just started sneaking
            level.makeSneak();
        } else if (!sneakPressed && sneakPrevious) {
            // If player just stopped sneaking
            level.makeWalk();
        }
        level.movePlayer(angle, tempAngle);
        level.update(delta);
        isSuccess = level.getLevelState() == LevelController.LevelState.WIN;
        isFailed = level.getLevelState() == LevelController.LevelState.LOSS;
        if((isSuccess && !prevSuccess) || (isFailed && !prevFailed)){
            countdown = COUNTDOWN_TIME;
        }
        prevSuccess = isSuccess;
        prevFailed = isFailed;
    }

    /**
     * Draw the physics objects to the canvas
     *
     * For simple worlds, this method is enough by itself.  It will need
     * to be overriden if the world needs fancy backgrounds or the like.
     *
     * The method draws all objects in the order that they were added.
     *
     * @param delta  Number of seconds since last animation frame
     */
    public void draw(float delta) {
        canvas.clear();

        level.draw(canvas, delta, debugFont);

        // Final message
        if (isSuccess) {
            displayFont.setColor(Color.YELLOW);
            canvas.begin(); // DO NOT SCALE
            canvas.drawText("VICTORY!", displayFont, 0, canvas.getHeight());
            canvas.end();
        } else if (isFailed) {
            displayFont.setColor(Color.RED);
            canvas.begin(); // DO NOT SCALE
            canvas.drawTextCentered("YOU DIED!", displayFont, 0.0f);
            canvas.end();
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
    public void resize(int width, int height)
        {
            canvasBounds.set(0,0,width,height);
        }


    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (isScreenActive) {
            if (preUpdate(delta)) {
                update(delta);
            }
            draw(delta);
        }
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
       isPaused = true;
    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        isPaused = false;
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     * @author: Professor White
     */
    public void show() {
        // Useless if called in outside animation loop
        isScreenActive = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     * @author: Professor White
     */
    public void hide() {
        // Useless if called in outside animation loop
        isScreenActive = false;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    /************************ POLLING INPUT HANDLING ************************/

    /**
     * Get mouse position
     * @return Vector2 mouse position
     */
    public Vector2 getMousePosition(){
        return new Vector2(Gdx.input.getX(), Gdx.input.getY());
    }

    /**
     * Reads the input for the player and converts the result into game logic.
     */
    public void readInput() {
        // Copy state from last animation frame
        // Helps us ignore buttons that are held down
        resetPrevious  = resetPressed;
        debugPrevious  = debugPressed;
        debug2Previous = debug2Pressed;
        exitPrevious = exitPressed;
        flarePrevious = flarePressed;
        sprintPrevious = sprintPressed;
        sneakPrevious = sneakPressed;

        readKeyboard();
    }

    /**
     * Reads input from the keyboard.
     *
     * This controller reads from the keyboard regardless of whether or not an X-Box
     * controller is connected.  However, if a controller is connected, this method
     * gives priority to the X-Box controller.
     *
     */
    private void readKeyboard() {
        // Give priority to gamepad results
        resetPressed = (Gdx.input.isKeyPressed(Input.Keys.R));
        debugPressed = (Gdx.input.isKeyPressed(Input.Keys.G));
        debug2Pressed = (Gdx.input.isKeyPressed(Input.Keys.E));
        exitPressed  = (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));
        flarePressed  = (Gdx.input.isButtonPressed(Input.Buttons.LEFT));
        sprintPressed = (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT));
        sneakPressed = (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT));

        // Directional controls
        horizontal = 0.0f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            horizontal += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            horizontal -= 1.0f;
        }
        vertical = 0.0f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            vertical += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            vertical -= 1.0f;
        }

        //#region mouse wheel alternative
        if(Gdx.input.isKeyPressed(Input.Keys.PERIOD)){
            level.lightFromPlayer(0.5f);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.COMMA)){
            level.lightFromPlayer(-0.5f);
        }
        //#endregion
    }

    /************************ EVENT-BASED INPUT HANDLING ************************/

    /**Below are functions that are required to be implemented by InputProcessor. All return false if unused to indicate
     * that the event was not handled by this inputProcessor. The functions that return false in this file return true
     * in the LoadingMode inputProcessor to ensure that the even is handled (either LoadingMode does something
     * or just returns true). This has to be used because mouse scrolling can only be done with InputProcessor.
     * Gdx.Input does not have any functions to handle mouse scrolling and this should not take too much time,
     * even with the events*/

    /**What happens when a key is pressed
     * @param keycode representing what key was pressed
     * @return boolean saying if the event was handled*/
    public boolean keyDown (int keycode) {
        return false;
    }

    /**What happens when a key is released
     * @param keycode representing what key was released
     * @return boolean saying if the event was handled*/
    public boolean keyUp (int keycode) {
        return false;
    }


    /**What happens when a character is typed
     * @param character representing what character was typed
     * @return boolean saying if the event was handled*/
    public boolean keyTyped (char character) {
        return false;
    }


    /**What happens when a screen is touched (for phone) or mouse
     * @param pointer, button representing what where and what was touched
     * @return boolean saying if the event was handled*/
    public boolean touchDown (int x, int y, int pointer, int button) {
        return false;
    }

    /**What happens when a screen is released (for phone) or mouse
     * @param pointer, button representing what where and what was released
     * @return boolean saying if the event was handled*/
    public boolean touchUp (int x, int y, int pointer, int button) {
        return false;
    }

    /**What happens when a screen is dragged with a finger (for phone) or mouse
     * @param pointer, button representing what where and what was dragged
     * @return boolean saying if the event was handled*/
    public boolean touchDragged (int x, int y, int pointer) {
        return false;
    }

    /**What happens when the mouse is moved
     * @param x, y representing where the mouse moved
     * @return boolean saying if the event was handled*/
    public boolean mouseMoved (int x, int y) {
        return false;
    }

    /**What happens when the mouse is scrolling. Should take O(1).
     * @param amount representing if the wheel scrolled down (1) or up (-1). Can only be those two values.
     * @return boolean saying if the event was handled*/
    public boolean scrolled (int amount) {
        if(!isScreenActive()){
            return true;
        }
        if(amount == 1){
            level.lightFromPlayer(-1.0f);
        }
        if(amount == -1){
            level.lightFromPlayer(1.0f);
        }

        return true;
    }

}