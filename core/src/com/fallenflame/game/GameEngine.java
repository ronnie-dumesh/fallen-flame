package com.fallenflame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.fallenflame.game.util.InputBindings;
import com.fallenflame.game.util.JsonAssetManager;
import com.fallenflame.game.util.ScreenListener;

import java.util.Arrays;

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

    // Sound constants
    /** Player walk volume */
    private static final float PLAYER_WALK_VOL = .3f;

    /**Boolean to determine if debug keys do anything.
     * Note: MUST BE FALSE WHEN MAKING A JAR! */
    private static final boolean ALLOW_DEBUG = false;

    private static final String SAVE_PATH = "savedata/save.json";
    private LevelSave[] levelSaves;
    private LevelSelectMode levelSelect;

    private Json json;
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
    /** The smaller version of displayFont, to be used for the win/lose screen*/
    protected BitmapFont menuOptionsFont;
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
    /**Textures for win/lose border. Win border is for the last level in the game*/
    private TextureRegion border;
    private TextureRegion winBorder;
    /**Information to keep track of where the user is hovered/where they are clicked */
    private Rectangle[] hoverRects;
    private int[] hoverStates;
    private GlyphLayout gl;
    private boolean retrySelected;
    private boolean menuSelected;
    //To make the smaller font for the win/lose screen.
    private FreeTypeFontGenerator generator;
    private FreeTypeFontGenerator.FreeTypeFontParameter parameter;
    //Fog-related parameters
    /**ParticleEffect that will be used as a template for the ParticleEffectPool. This is in GameEngine because it needs
     * to load in the .p file, and file loading is done here*/
    private ParticleEffect fogTemplate;

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
    /** Whether 1-9 were pressed. */
    private boolean[] numsPressed = new boolean[9];
    private boolean[] numsPrevious = new boolean[9];
    /** How much did we move horizontally? */
    private float horizontal;
    /** How much did we move vertically? */
    private float vertical;
    /** The ID of the last level played. */
    private int lastLevelPlayed;

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
        if(Gdx.files.local("savedata/save.json").exists()){
            saveJson = jsonReader.parse(Gdx.files.local("savedata/save.json"));
            levelSaves = json.readValue(LevelSave[].class, saveJson);
        }
        else {
            // If local save file doesn't exist (like when jar is first opened), create it from internal template
            saveJson = jsonReader.parse(Gdx.files.internal("jsons/save.json"));
            FileHandle file = Gdx.files.local(SAVE_PATH);
            JsonValue.PrettyPrintSettings settings = new JsonValue.PrettyPrintSettings();
            settings.outputType = JsonWriter.OutputType.json;
            levelSaves = json.readValue(LevelSave[].class, saveJson);
            file.writeString(json.prettyPrint(levelSaves, settings), false);
        }
        // Read save data from local save JSON file
        globalJson = jsonReader.parse(Gdx.files.internal("jsons/global.json"));
        fogTemplate = new ParticleEffect();
        fogTemplate.load(Gdx.files.internal("effects/fog2.p"), Gdx.files.internal("textures"));

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
        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/chp-fire.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 44;
        parameter.shadowOffsetX = 0;
        parameter.shadowOffsetY = 4;
        parameter.color = Color.WHITE;
        menuOptionsFont = generator.generateFont(parameter);
        generator.dispose();
        border = JsonAssetManager.getInstance().getEntry("border", TextureRegion.class);
        winBorder = border = JsonAssetManager.getInstance().getEntry("winborder", TextureRegion.class);
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

    /** Get saveJson */
    public LevelSave[] getLevelSaves() { return levelSaves; }

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
    public GameEngine(LevelSelectMode levelSelect) {
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
        json = new Json();
        this.levelSelect = levelSelect;
        hoverStates = new int[2];
        hoverRects = new Rectangle[2];
        gl = new GlyphLayout();
    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        level.dispose();
        fogTemplate.dispose();
        level  = null;
        canvas = null;
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the level and creates a new one. It will
     * reread from the JSON file, allowing us to make changes on the fly.
     */
    public void reset(int lid) {
        if (lid < 0 || lid >= saveJson.size) return;

        lastLevelPlayed = lid;

        level.dispose();
        level = new LevelController();

        isSuccess = false;
        isFailed = false;
        prevFailed = false;
        prevSuccess = false;
        menuSelected = false;
        retrySelected = false;
         countdown = -1;
         hoverStates = new int[2];
         hoverRects = new Rectangle[2];


        // Reload the json each time
        String currentLevelPath = "jsons/" + saveJson.get(lid).getString("path"); // Currently just gets first level
        levelJson = jsonReader.parse(Gdx.files.internal(currentLevelPath));
        level.populate(levelJson, globalJson, fogTemplate);
        level.setLevelState(LevelController.LevelState.IN_PROGRESS);
        level.getWorld().setContactListener(level);
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the level and creates a new one. It will
     * reread from the JSON file, allowing us to make changes on the fly.
     */
    public void reset() {
        reset(lastLevelPlayed);
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

        if (ALLOW_DEBUG && debugPressed && !debugPrevious) {
            level.setDebug(level.getDebug() + 1);
        }
        if (ALLOW_DEBUG && debug2Pressed && !debug2Previous) {
            level.setDebug2(!level.getDebug2());
        }
        if (resetPressed && !resetPrevious) {
            reset();
        }
        for (int i = 0, j = numsPressed.length; i < j; i++) {
            if (numsPressed[i] && !numsPrevious[i]) {
                reset(i);
            }
        }
        if (exitPressed && !exitPrevious) {
            listener.exitScreen(this, 0);
            return false;
        }
      else if (countdown == 0) {
            if(isSuccess && lastLevelPlayed +1 < saveJson.size)
                reset(lastLevelPlayed+1);
            else if(isFailed){
                if(retrySelected){
                    reset(lastLevelPlayed);
                }
                else if(menuSelected){
                    listener.exitScreen(this, 1);
                }
            }
            else{
                listener.exitScreen(this, 1);
            }
        }

        return true;
    }

    private Vector2 moveAngle = new Vector2();
    /**
     * The core gameplay loop of this world. This checks if the level has ended
     * @param delta Number of seconds since last animation frame
     */
    public void update(float delta) {
        // If the player won or lost, don't update
        if(prevSuccess || prevFailed) return;
        if(level.getPlayer().isDying()){
            level.update(delta);
            return;
        }

        if (flarePressed && !flarePrevious) {
            level.createFlare(getMousePosition(), getScreenDimensions());
        }
        // Rotate the avatar to face the direction of movement
        moveAngle.set(horizontal,vertical);
        if (moveAngle.len2() > 0.0f) {
            if (!level.getPlayer().isPlayingSound()) {
                level.getPlayer().getWalkSound().loop(PLAYER_WALK_VOL);
                level.getPlayer().setPlayingSound(true);
            }
        } else {
            level.getPlayer().getWalkSound().stop();
            level.getPlayer().setPlayingSound(false);
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
        level.getPlayer().move(moveAngle);
        level.update(delta);
        // Get new victory state
        isSuccess = level.getLevelState() == LevelController.LevelState.WIN || prevSuccess;
        isFailed = level.getLevelState() == LevelController.LevelState.LOSS || prevFailed;
        // If new win, mark level complete in save json and ensure next level is unlocked
        if(isSuccess && !prevSuccess) {
            // Update save data
            levelSaves[lastLevelPlayed].completed = true;
            if(lastLevelPlayed + 1 < levelSaves.length){
                levelSaves[lastLevelPlayed + 1].unlocked = true;
            }
            // Write save data to local save JSON file
            JsonValue.PrettyPrintSettings settings = new JsonValue.PrettyPrintSettings();
            settings.outputType = JsonWriter.OutputType.json;
            FileHandle file = Gdx.files.local(SAVE_PATH);
            file.writeString(json.prettyPrint(levelSaves, settings), false);
            // Update level select
            levelSelect.resetNumberUnlocked();
        }
        // If new win or loss, start countdown
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
            displayFont.setColor(Color.CYAN);
            canvas.beginWithoutCamera(); // DO NOT SCALE
            canvas.draw(lastLevelPlayed+1 > saveJson.size ? winBorder : border, canvas.getWidth()/2-(border.getRegionWidth()/2), canvas.getHeight()/2-(border.getRegionHeight()/2));
            String titleText = lastLevelPlayed +1 < saveJson.size ? "Success!" : "You Won the Game!";
            canvas.drawTextCentered(titleText, displayFont, +border.getRegionHeight()/6);
            menuOptionsFont.setColor(hoverStates[0] == 1 ? Color.CYAN : Color.WHITE);
            canvas.drawTextCentered("Continue", menuOptionsFont, -border.getRegionHeight()/6);
            gl.setText(menuOptionsFont, "Continue");
            hoverRects[0] = new Rectangle(
                    (canvas.getWidth() - gl.width) / 2, (canvas.getHeight() - gl.height) / 2 -border.getRegionHeight()/6,
                    gl.width, gl.height);
            canvas.end();
            level.stopAllSounds();
        } else if (isFailed) {
            canvas.beginWithoutCamera(); // DO NOT SCALE
            canvas.draw(border, canvas.getWidth()/2-(border.getRegionWidth()/2), canvas.getHeight()/2-(border.getRegionHeight()/2));
            canvas.drawTextCentered("Game Over!", displayFont, border.getRegionHeight()/6);
            menuOptionsFont.setColor(hoverStates[0] == 1 ? Color.CYAN : Color.WHITE);
            gl.setText(menuOptionsFont, "Retry");
            canvas.drawText("Retry", menuOptionsFont, (canvas.getWidth()/2-gl.width/2)-border.getRegionWidth()/6,
                    (canvas.getHeight()/2 + gl.height/2)-border.getRegionHeight()/6);
            hoverRects[0] = new Rectangle(
                    ((canvas.getWidth()/2-gl.width/2)-border.getRegionWidth()/6), (canvas.getHeight() - gl.height) / 2 -border.getRegionHeight()/6,
                    gl.width, gl.height);
            menuOptionsFont.setColor(hoverStates[1] == 1 ? Color.CYAN : Color.WHITE);
            gl.setText(menuOptionsFont, "Menu");
            canvas.drawText("Menu", menuOptionsFont, (canvas.getWidth()/2-gl.width/2)+border.getRegionWidth()/6,
                    (canvas.getHeight()/2 + gl.height/2)-border.getRegionHeight()/6);
            hoverRects[1] = new Rectangle(
                    ((canvas.getWidth()/2-gl.width/2)+border.getRegionWidth()/6), (canvas.getHeight() - gl.height) / 2 -border.getRegionHeight()/6,
                    gl.width, gl.height);
            canvas.end();
            level.stopAllSounds();
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
     * Get screen dimensions
     * @return Vector2 screen dimensions
     */
    public Vector2 getScreenDimensions(){
        return new Vector2(canvas.getWidth(),canvas.getHeight());
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
        level.stopAllSounds();
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
        numsPrevious = Arrays.copyOf(numsPressed, numsPressed.length);

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
        resetPressed = (Gdx.input.isKeyPressed(InputBindings.getBindingOf(InputBindings.Control.RESET_LEVEL)));
        debugPressed = (Gdx.input.isKeyPressed(Input.Keys.G));
        debug2Pressed = (Gdx.input.isKeyPressed(Input.Keys.E));
        exitPressed  = (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));
        flarePressed  = (Gdx.input.isButtonPressed(Input.Buttons.LEFT));
        sprintPressed = (Gdx.input.isKeyPressed(InputBindings.getBindingOf(InputBindings.Control.SPRINTING)));
        sneakPressed = (Gdx.input.isKeyPressed(InputBindings.getBindingOf(InputBindings.Control.SNEAKING)));
        numsPressed[0] = (Gdx.input.isKeyPressed(Input.Keys.NUM_1) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_1));
        numsPressed[1] = (Gdx.input.isKeyPressed(Input.Keys.NUM_2) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_2));
        numsPressed[2] = (Gdx.input.isKeyPressed(Input.Keys.NUM_3) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_3));
        numsPressed[3] = (Gdx.input.isKeyPressed(Input.Keys.NUM_4) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_4));
        numsPressed[4] = (Gdx.input.isKeyPressed(Input.Keys.NUM_5) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_5));
        numsPressed[5] = (Gdx.input.isKeyPressed(Input.Keys.NUM_6) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_6));
        numsPressed[6] = (Gdx.input.isKeyPressed(Input.Keys.NUM_7) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_7));
        numsPressed[7] = (Gdx.input.isKeyPressed(Input.Keys.NUM_8) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_8));
        numsPressed[8] = (Gdx.input.isKeyPressed(Input.Keys.NUM_9) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_9));

        // Directional controls
        horizontal = 0.0f;
        if (Gdx.input.isKeyPressed(InputBindings.getBindingOf(InputBindings.Control.GO_RIGHT))) {
            horizontal += 1.0f;
        }
        if (Gdx.input.isKeyPressed(InputBindings.getBindingOf(InputBindings.Control.GO_LEFT))) {
            horizontal -= 1.0f;
        }
        vertical = 0.0f;
        if (Gdx.input.isKeyPressed(InputBindings.getBindingOf(InputBindings.Control.GO_UP))) {
            vertical += 1.0f;
        }
        if (Gdx.input.isKeyPressed(InputBindings.getBindingOf(InputBindings.Control.GO_DOWN))) {
            vertical -= 1.0f;
        }

        //#region mouse wheel alternative
        if(Gdx.input.isKeyPressed(InputBindings.getBindingOf(InputBindings.Control.INCREASE_LIGHT))){
            level.lightFromPlayer(0.5f);
        }
        if(Gdx.input.isKeyPressed(InputBindings.getBindingOf(InputBindings.Control.DECREASE_LIGHT))){
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
        if(!isSuccess && !isFailed){return false;}
        if (Arrays.stream(hoverStates).noneMatch(i -> i == 1)) {
            return false;
        } else {
            for (int i = 0, j = hoverRects.length; i < j; i++) {
                if (hoverRects[i] == null) continue;
                if (hoverRects[i].contains(x, (canvas.getHeight() - y))) {
                    retrySelected = isFailed && i == 0;
                    menuSelected = isFailed && i == 1;
                    countdown = 0;
                }
            }
            return true;
        }


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
    public boolean mouseMoved (int x, int y){
        if(!isSuccess && !isFailed){return false;}
        for (int i = 0, j = hoverRects.length; i < j; i++) {
            if (hoverRects[i] == null) continue;
            hoverStates[i] = (hoverRects[i].contains(x, (canvas.getHeight()-y))) ? 1 : 0;
        }
        return true;
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

class LevelSave {
    protected String name;
    protected boolean unlocked;
    protected boolean completed;
    protected String path;
}

