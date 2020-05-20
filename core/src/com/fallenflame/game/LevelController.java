package com.fallenflame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.enemies.*;
import com.fallenflame.game.physics.obstacle.Obstacle;
import com.fallenflame.game.util.BGMController;
import com.fallenflame.game.util.JsonAssetManager;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/** Credit to Walker White for some code reused from B2LightsDemo */
public class LevelController implements ContactListener {
    //  MAY NEED THESE:
    /** Number of velocity iterations for the constrain solvers */
    public static final int WORLD_VELOC = 6;
    /** Number of position iterations for the constrain solvers */
    public static final int WORLD_POSIT = 2;

    // Sound constants
    /** Base volume for enemy movement sounds */
    public static final float ENEMY_MOV_BASE_VOL = .2f;
    /** Volume scaling for enemy movement sounds.
     * Must be >0. Lower numbers will lead to faster volume drop-off.
     * Value of 1 means drop-off rate is exactly equivalent to 1/distance */
    public static final float ENEMY_MOVE_VOL_SCL = 2f;
    /** Pitch for enemy movement sounds */
    public static final float ENEMY_MOV_PITCH = 1f;
    /** Base volume for enemy constant sounds */
    public static final float ENEMY_CONS_BASE_VOL = .45f;
    /** Volume scaling for enemy constant sounds.
     * Must be >0. Lower numbers will lead to faster volume drop-off.
     * Value of 1 means drop-off rate is exactly equivalent to 1/distance */
    public static final float ENEMY_CONS_VOL_SCL = 4f;

    /** Threshold value that enemy constant sound gets subtracted by. Filters out
     * quiet noises so every movement noise isn't constantly playing.
     */
    public static final float ENEMY_CONS_VOL_THR = .1f;
    /** Pitch for enemy constant sounds */
    public static final float ENEMY_CONS_PITCH = 1f;
    /** Volume scaling for panning
     * Must be in range [0,1]. 1 is maximum panning, 0 is no panning. */
    public static final float PAN_SCL = .4f;

    /** Volume for player flare sounds */
    public static final float PLAYER_FLARE_VOL = .4f;

    /** Whether or not the level has been populated */
    private boolean populated;
    /** Whether ot not the ghost has been added to the level*/
    private boolean ghostAdded = false;

    // Physics objects for the level
    /** Reference to the player character */
    private PlayerModel player;
    /** Reference to the exit (for collision detection) */
    private ExitModel exit;
    /** Reference to all enemies */
    private List<EnemyModel> enemies;
    /** Reference to all walls */
    private List<WallModel> walls;
    /** Reference to all flares */
    private List<FlareModel> flares;
    /** Reference to all fireballs */
    private List<FireballModel> fireballs;
    /** Reference to all items */
    private List<ItemModel> items;
    /** Reference to continuing player-item contacts */
    private HashSet<ItemModel> itemContacts;
    /** Level Model for AI Pathfinding */
    private LevelModel levelModel;

    // JSON data (for objects created after population)
    /** Flare JSONValue */
    private JsonValue flareJSON;
    /** Fireball JSONValue */
    private JsonValue fireballJSON;
    /** Ghost Enemy JSONValue */
    private JsonValue ghostJSON;

    /** Player starting position (for use by ghost) */
    private float[] startPos;

    /** Whether or not the level is in debug mode (showing off physics) */
    private int debug;
    /** Whether or not the level is in debug 2 mode (unlit area only half dark) */
    private boolean debug2;

    // World Definitions
    /** The Box2D world */
    protected World world;
    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;
    /** The world background */
    protected TextureRegion background;

    // Sneak Bar
    /** The texture used for the powerbar background*/
    protected TextureRegion powerBarLeft;
    /** The texture used for the powerbar background*/
    protected TextureRegion powerBarMiddle;
    /** The texture used for the powerbar background*/
    protected TextureRegion powerBarRight;
    /** The texture used for the powerbar foreground*/
    protected TextureRegion powerBarForeground;
    /** The texture used for the powerbar ghost active indicator*/
    protected TextureRegion powerBarActive;
    /** The texture used for the powerbar ghost inactive indicator */
    protected TextureRegion powerBarInactive;
    /** The maximum power value allowed in the game */
    protected float globalMaxPowerValue;
    /** The offset of the powerbar ghost from the player*/
    protected Vector2 powerBarOffset;
    /** The offset of the powerbar background from the ghost*/
    protected Vector2 powerBarBackgroundOffset;
    /** The offset of the powerbar foreground from the background */
    protected Vector2 powerBarForegroundOffset;

    //flare counter
    /** The texture used for the flarecount when you have available flares */
    protected TextureRegion activeFlareCountTexture;
    /** The texture used for the flarecount when you have unavailable flares */
    protected TextureRegion inactiveFlareCountTexture;
    /** The offset of the leftmost flarecount from the player */
    protected Vector2 flareCountOffset;
    /** The distance between each counter */
    protected float flareCountSplit;

    // Controllers
    private final LightController lightController;
    private final List<AIController> AIControllers;
    private final FogController fogController;
    private final TextController textController;

    // BGM
    private String bgm;

    /** Enum to specify level state */
    public enum LevelState {
        /** Player has reached the exit */
        WIN,
        /** Player has died */
        LOSS,
        /** Player is still playing */
        IN_PROGRESS
    }
    private LevelState levelState;

    // TO FIX THE TIMESTEP
    /** The maximum frames per second setting for this level */
    protected int maxFPS;
    /** The minimum frames per second setting for this level */
    protected int minFPS;
    /** The amount of time in to cover a single animation frame */
    protected float timeStep;
    /** The maximum number of steps allowed before moving physics forward */
    protected float maxSteps;
    /** The maximum amount of time allowed in a frame */
    protected float maxTimePerFrame;
    /** The amount of time that has passed without updating the frame */
    protected float physicsTimeLeft;
    /** FPS of game */
    private float fps;
    /** Number of ticks sense we started this controller (used to limit number of fps updates) */
    private long ticks;

    /**
     * Returns the bounding rectangle for the physics world
     *
     * The size of the rectangle is in physics, coordinates, not screen coordinates
     *
     * @return the bounding rectangle for the physics world
     */
    public Rectangle getBounds() { return bounds; }

    /**
     * Sets the bounding rectangle for the physics world
     *
     * The size of the rectangle is in physics, coordinates, not screen coordinates
     *
     * @value the bounding rectangle for the physics world
     */
    public void setBounds(Rectangle value) { bounds = value; }

    /**
     * Returns the scaling factor to convert physics coordinates to screen coordinates
     *
     * @return the scaling factor to convert physics coordinates to screen coordinates
     */
    public Vector2 getScale() { return scale; }

    /**
     * Returns a reference to the Box2D World
     *
     * @return a reference to the Box2D World
     */
    public World getWorld() { return world; }

    /**
     * Returns a reference to the player
     *
     * @return a reference to the player
     */
    public PlayerModel getPlayer() { return player; }

    /**
     * Returns a reference to the exit
     *
     * @return a reference to the exit
     */
    public ExitModel getExit() { return exit; }

    /**
     * Returns a reference to the enemies
     *
     * @return a reference to the enemies
     */
    public List<EnemyModel> getEnemies() { return enemies; }

    /**
     * Returns a reference to the walls
     *
     * @return a reference to the walls
     */
    public List<WallModel> getWalls() { return walls; }

    /**
     * Returns a reference to the flares
     *
     * @return a reference to the flares
     */
    public List<FlareModel> getFlares() { return flares; }

    /**
     * Returns whether this level is currently in debug node
     *
     * If the level is in debug mode, then the physics bodies will all be drawn as
     * wireframes onscreen
     *
     * @return whether this level is currently in debug node
     */
    public int getDebug() { return debug; }

    /**
     * Returns whether this level is currently in debug node
     *
     * If the level is in debug 2 mode, then unlit area will be half-dark
     *
     * @return whether this level is currently in debug node
     */
    public boolean getDebug2() { return debug2; }

    /**
     * Sets whether this level is currently in debug node
     *
     * If the level is in debug mode, then the physics bodies will all be drawn as
     * wireframes onscreen
     *
     * @param value	whether this level is currently in debug node
     */
    public void setDebug(int value) { debug = value % 3; }

    /**
     * Sets whether this level is currently in debug 2 node
     *
     * If the level is in debug 2 mode, then unlit area will be half-dark
     *
     * @param value	whether this level is currently in debug node
     */
    public void setDebug2(boolean value) { debug2 = value; }

    /**
     * Returns the maximum FPS supported by this level
     *
     * This value is used by the rayhandler to fix the physics timestep.
     *
     * @return the maximum FPS supported by this level
     */
    public int getMaxFPS() { return maxFPS; }

    /**
     * Sets the maximum FPS supported by this level
     *
     * This value is used by the rayhandler to fix the physics timestep.
     *
     * @param value the maximum FPS supported by this level
     */
    public void setMaxFPS(int value) { maxFPS = value; }

    /**
     * Returns the minimum FPS supported by this level
     *
     * This value is used by the rayhandler to fix the physics timestep.
     *
     * @return the minimum FPS supported by this level
     */
    public int getMinFPS() { return minFPS; }

    /**
     * Sets the minimum FPS supported by this level
     *
     * This value is used by the rayhandler to fix the physics timestep.
     *
     * @param value the minimum FPS supported by this level
     */
    public void setMinFPS(int value) { minFPS = value; }

    /**
     * Gets the current state of the level.
     *
     * This value is used by GameEngine to know when player has lost or won.
     */
    public LevelState getLevelState() { return levelState; }

    /**
     * Sets the current state of the level.
     *
     * This value is used by GameEngine to know when player has lost or won.
     *
     * @param state Desired levelState
     */
    public void setLevelState(LevelState state) { levelState = state; }

    /**
     * Creates a new LevelModel
     *
     * The level is empty and there is no active physics world.  You must read
     * the JSON file to initialize the level
     */
    public LevelController() {
        // World
        world  = null;
        bounds = new Rectangle(0,0,1,1);
        scale = new Vector2(1,1);
        debug  = 0;
        debug2 = false;
        levelState = LevelState.IN_PROGRESS;
        // Controllers
        lightController = new LightController();
        AIControllers = new LinkedList<>();
        fogController = new FogController();
        textController = new TextController();
        // Models
        walls = new LinkedList<>();
        enemies = new LinkedList<>();
        flares = new LinkedList<>();
        fireballs = new LinkedList<>();
        levelModel = new LevelModel();
        // Not yet populated
        populated = false;

    }

    /**
     * Lays out the game geography and enemies from the given JSON file
     *
     * @param levelJson	the JSON tree defining the level
     */
    public void populate(JsonValue levelJson, JsonValue globalJson, ParticleEffect fogTemplate) {
        populated = true;

        float[] pSize = levelJson.get("physicsSize").asFloatArray();

        world = new World(Vector2.Zero,false);
        bounds = new Rectangle(0,0,pSize[0],pSize[1]);
        scale.x = scale.y = 50;

        String key = globalJson.get("background").get("texture").asString();
        if (levelJson.get("background").has("texture"))
            levelJson.get("background").get("texture").asString(); // Get specific texture if available
        background = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);


        JsonValue powerBarJSON = globalJson.get("powerbar");
        globalMaxPowerValue = powerBarJSON.get("maxsneak").asFloat();
        key = powerBarJSON.get("texture").get("meterleft").asString();
        powerBarLeft = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        key = powerBarJSON.get("texture").get("metermiddle").asString();
        powerBarMiddle = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        key = powerBarJSON.get("texture").get("meterright").asString();
        powerBarRight = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        key = powerBarJSON.get("texture").get("meterforeground").asString();
        powerBarForeground = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        key = powerBarJSON.get("texture").get("active").asString();
        powerBarActive = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        key = powerBarJSON.get("texture").get("inactive").asString();
        powerBarInactive = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);

        JsonValue textureOffsets = powerBarJSON.get("textureoffsets");
        powerBarOffset = new Vector2 (textureOffsets.get("ghostfromplayer").get("x").asFloat(),
                                         textureOffsets.get("ghostfromplayer").get("y").asFloat());
        powerBarBackgroundOffset =   new Vector2 (textureOffsets.get("backgroundfromghost").get("x").asFloat(),
                                                    textureOffsets.get("backgroundfromghost").get("y").asFloat());
        powerBarForegroundOffset =  new Vector2 (textureOffsets.get("foregroundfrombackground").get("x").asFloat(),
                                                    textureOffsets.get("foregroundfrombackground").get("y").asFloat());

        JsonValue flareCountJSON = globalJson.get("flarecount");
        key = flareCountJSON.get("texture").get("active").asString();
        activeFlareCountTexture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        key = flareCountJSON.get("texture").get("inactive").asString();
        inactiveFlareCountTexture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        flareCountSplit = flareCountJSON.get("flare-split").asFloat();
        flareCountOffset = new Vector2 (flareCountJSON.get("textureoffset").get("x").asFloat(),
              flareCountJSON.get("textureoffset").get("y").asFloat());

        // Compute the FPS
        int[] fps = levelJson.get("fpsRange").asIntArray();
        maxFPS = fps[1]; minFPS = fps[0];
        timeStep = 1.0f/maxFPS;
        maxSteps = 1.0f + maxFPS/minFPS;
        maxTimePerFrame = timeStep*maxSteps;

        // Create player
        player = new PlayerModel();
        player.setDrawScale(scale);
        player.initialize(globalJson.get("player"), levelJson);
        player.initializeTextures(globalJson.get("player"));
        player.activatePhysics(world);
        assert inBounds(player);
        startPos = levelJson.get("playerpos").asFloatArray();
        // Create Exit
        exit = new ExitModel();
        exit.initialize(globalJson.get("exit"), levelJson.get("exitpos").asFloatArray());
        exit.setDrawScale(scale);
        exit.activatePhysics(world);
        assert inBounds(exit);
        // Create Walls
        for(JsonValue wallJSON : levelJson.get("walls")) {
            WallModel wall = new WallModel();

            if(wallJSON.get("texture").asString().equals("wall-side")) {
                wall.initialize(globalJson.get("wall-side"), wallJSON);
            } else {
                wall.initialize(globalJson.get("wall-top"), wallJSON);
            }

            wall.setDrawScale(scale);
            wall.activatePhysics(world);
            walls.add(wall);
            assert inBounds(wall);
        }
        // Create enemies
        int enemyID = 0;
        JsonValue globalEnemies = globalJson.get("enemies");
        for(JsonValue enemyJSON : levelJson.get("enemies")) {
            String enemyType = enemyJSON.get("enemytype").asString();
            // Initialize Enemy Model
            EnemyModel enemy;
            if(enemyType.equals("typeA")) {
                enemy = new EnemyTypeAModel();
            }
            else if(enemyType.equals("typeB")){
                enemy = new EnemyTypeBModel();
            }
            else {
                Gdx.app.error("LevelController", "Enemy type without model", new IllegalArgumentException());
                return;
            }
            enemy.setDrawScale(scale);
            enemy.initialize(globalEnemies.get(enemyType), enemyJSON.get("enemypos").asFloatArray());
            enemy.initializeTextures(globalEnemies.get(enemyType));
            enemy.setConstantSoundID(enemy.getConstantSound().loop(0, ENEMY_CONS_PITCH, 0));
            enemy.activatePhysics(world);
            enemies.add(enemy);
            // Initialize AIController
            if(enemyType.equals("typeA")) {
                // If subtype pathing, give pathCoors as input as well
                if(enemyJSON.has("subtype") && enemyJSON.get("subtype").asString().equals("pathing"))
                    AIControllers.add(new AITypeAController(enemyID, levelModel, enemies, player, flares, enemyJSON.get("pathCoors")));
                else
                    AIControllers.add(new AITypeAController(enemyID, levelModel, enemies, player, flares));
            }
            else if(enemyType.equals("typeB")) {
                AIControllers.add(new AITypeBController(enemyID, levelModel, enemies, player, flares));
            }
            else{
                Gdx.app.error("LevelController", "Enemy type without AIController", new IllegalArgumentException());
                return;
            }

            enemyID++;
            assert inBounds(enemy);
        }
        // Prepare flare, fireball, and ghost jsons
        flareJSON = globalJson.get("flare");
        fireballJSON = globalJson.get("fireball");
        ghostJSON = globalEnemies.get("ghost");

        // Create items (if any exist)
        items = new LinkedList();
        itemContacts = new HashSet();
        if(levelJson.has("items")){
            JsonValue globalItemJson = globalJson.get("items");
            for(JsonValue levelItemJson : levelJson.get("items")){
                ItemModel item = new ItemModel(levelItemJson.get("itemPos").asFloatArray());
                item.initialize(globalItemJson, levelItemJson.get("itemType").asString());
                item.setDrawScale(scale);
                item.activatePhysics(world);
                assert inBounds(item);
                items.add(item);
            }
        }

        textController.initialize(levelJson.has("texts") ? levelJson.get("texts") : null);

        // Set background music
        bgm = levelJson.has("bgm") ? levelJson.get("bgm").asString() : null;

        // Initialize levelModel, lightController, and fogController
        levelModel.initialize(bounds, walls, enemies);
        lightController.initialize(player, exit, levelJson.get("lighting"), world, bounds, scale);
        fogController.initialize(fogTemplate, levelModel, player, flares, enemies);

    }

    /**
     * Disposes of all resources for this model.
     *
     * Because of all the heavy weight physics stuff, this method is absolutely
     * necessary whenever we reset a level.
     */
    public void dispose() {
        if(!populated)
            return;

        lightController.dispose();
        textController.dispose();

        for(WallModel wall : walls) {
            wall.deactivatePhysics(world);
            wall.dispose();
        }
        walls.clear();
        for(EnemyModel enemy : enemies) {
            enemy.getConstantSound().stop();
            enemy.getActiveSound().stop();
            enemy.deactivatePhysics(world);
            enemy.dispose();
        }
        enemies.clear();
        for(FlareModel flare : flares) {
            flare.deactivatePhysics(world);
            flare.dispose();
        }
        flares.clear();
        for(FireballModel fireball : fireballs) {
            fireball.deactivatePhysics(world);
            fireball.dispose();
        }
        fireballs.clear();
        for(ItemModel item : items) {
            item.deactivatePhysics(world);
            item.dispose();
        }
        items.clear();
        exit.deactivatePhysics(world);
        exit.dispose();
        player.getWalkSound().stop();
        player.setPlayingSound(false);
        player.deactivatePhysics(world);
        player.dispose();

        if (world != null) {
            world.dispose();
            world = null;
        }
        populated = false;
    }

    /**
     * Returns true if the object is in bounds.
     *
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     *
     * @return true if the object is in bounds.
     */
    private boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
        boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
        return horiz && vert;
    }

    /**
     * Updates all of the models in the level.
     *
     * This is borderline controller functionality.  However, we have to do this because
     * of how tightly coupled everything is.
     *
     * @param dt the time passed since the last frame
     */
    public void update(float dt) {
        // If the player is alive, update the Box2D world.
        // If updating fails for whatever reason (which it should never)
        // just give up already.
        if(player.isAlive() && !fixedStep(dt)) return;

        // Update player. This is always necessary even if dying cos it
        // updates the texture.
        player.update(dt);

        // Update text controller (even if dead), this allows the text to finish the animation.
        textController.update(player);

        // If dead, mark dead.
        if (player.isDead()) setLevelState(LevelState.LOSS);

        // If dying or dead, that's it. Don't update anything else.
        // (such as light, fog, enemies, etc)
        if (player.isDead() || player.isDying()) return;

        assert inBounds(player);

        // Decrement power value if player is sneaking or sprinting
        if((player.isSneaking() || player.isSprinting()) && player.isAlive()){
            if(player.getPowerVal() > 0){
                player.decPowerVal();
            }
            // Add ghost enemy if player has used all their power
            else if(player.getPowerVal() == 0 && ghostAdded == false) {
                addGhost();
                ghostAdded = true;
            }
        }

        // Get Enemy Actions
        Iterator<AIController> ctrlI = AIControllers.iterator();
        LinkedList<Integer> ctrlCodes = new LinkedList();
        while(ctrlI.hasNext()){
            AIController ctrl = ctrlI.next();
            ctrlCodes.add(ctrl.getAction());
        }
        // Execute Enemy Actions
        Iterator<EnemyModel> enemyI = enemies.iterator();
        Iterator<Integer> actionI = ctrlCodes.iterator();
        while(enemyI.hasNext()){
            EnemyModel enemy = enemyI.next();
            int action = actionI.next();
            enemy.executeMovementAction(action);
            // Check if enemy is firing, for now only supports EnemyTypeBModel. TODO: Will need to rework if more firing enemies
            boolean firing = (action & EnemyModel.CONTROL_FIRE) != 0;
            if (enemy.getClass() == EnemyTypeBModel.class) {
                if(firing && ((EnemyTypeBModel) enemy).canFire()) {
                    fireWeapon((EnemyTypeBModel) enemy);
                } else {
                    ((EnemyTypeBModel) enemy).coolDown(true);
                }
            }
            enemy.update(dt);
            // Play enemy sounds
            float pan = (enemy.getX() - player.getX()) * PAN_SCL;
            if (enemy.isActivated() && (enemy.getActiveSoundID() == -1)) {
                //start sound
                enemy.setActiveSoundID(enemy.getActiveSound().loop(ENEMY_MOV_BASE_VOL, ENEMY_MOV_PITCH, pan));
            } else if (!enemy.isActivated()) {
                //end sound
                enemy.getActiveSound().stop();
                enemy.setActiveSoundID(-1);
            } else {
                //modify sound
                enemy.getActiveSound().setPan(enemy.getActiveSoundID(), pan, ENEMY_MOV_BASE_VOL * ((1/enemy.getDistanceBetween(player) * ENEMY_MOVE_VOL_SCL)));
            }
            enemy.getConstantSound().setPan(enemy.getConstantSoundID(), pan, ENEMY_CONS_BASE_VOL * ((1/enemy.getDistanceBetween(player) * ENEMY_CONS_VOL_SCL)));
            assert inBounds(enemy);
        }

        // Update flares
        Iterator<FlareModel> i = flares.iterator();
        while(i.hasNext()){
            FlareModel flare = i.next();
            if(!(Float.compare(flare.timeToBurnout(), 0.0f) > 0)){
                flare.deactivatePhysics(world);
                flare.dispose();
                i.remove();
            }
            else {
                flare.update(dt);
            }
        }
        // Remove old fireballs
        Iterator<FireballModel> ii = fireballs.iterator();
        while(ii.hasNext()){
            FireballModel f = ii.next();
            if(!f.isActive()){
                f.deactivatePhysics(world);
                f.dispose();
                ii.remove();
            }
        }

        // Check for contact items' usability
        Iterator<ItemModel> i3 = itemContacts.iterator();
        while(i3.hasNext()){
            ItemModel item = i3.next();
            // If item is a flare try to increment flare count (will return false if player is at max)
            if(item.isFlare() && player.incFlareCount()) {
                item.deactivate();
                i3.remove();
            }
        }
        // Remove old items
        Iterator<ItemModel> iii = items.iterator();
        while(iii.hasNext()){
            ItemModel it = iii.next();
            if(!it.isActive()){
                it.deactivatePhysics(world);
                it.dispose();
                iii.remove();
            }
        }

        // Update background music
        if (player.getPowerVal() > 0 || !ghostJSON.has("bgm") || ghostJSON.get("bgm").asString().equals("")) {
            if (bgm != null && !bgm.equals("")) {
                BGMController.startBGM(bgm);
            } else {
                BGMController.stopBGM();
            }
        } else {
            BGMController.startBGM(ghostJSON.get("bgm").asString());
        }

        // Update level model.
        levelModel.update(player, enemies);

        // Update lights
        lightController.updateLights(flares, enemies, fireballs, items);
    }


    public void stopAllSounds(){
        player.getWalkSound().stop();
        player.setPlayingSound(false);
        for(EnemyModel e : enemies){
            e.getConstantSound().stop();
            e.getActiveSound().stop();
        }
    }

    /**
     * Fixes the physics frame rate to be in sync with the animation framerate
     *
     * http://gafferongames.com/game-physics/fix-your-timestep/
     *
     * @param dt the time passed since the last frame
     */
    private boolean fixedStep(float dt) {
        if (world == null) return false;

        physicsTimeLeft += dt;
        if (physicsTimeLeft > maxTimePerFrame) {
            physicsTimeLeft = maxTimePerFrame;
        }

        boolean stepped = false;
        while (physicsTimeLeft >= timeStep) {
            world.step(timeStep, WORLD_VELOC, WORLD_POSIT);
            physicsTimeLeft -= timeStep;
            stepped = true;
        }
        return stepped;
    }

    /**
     * Adds the ghost enemy
     */
    public void addGhost() {
        // Create ghost model
        EnemyModel ghost = new EnemyGhostModel();
        ghost.initialize(ghostJSON, startPos);
        ghost.initializeTextures(ghostJSON);
        ghost.setConstantSoundID(ghost.getConstantSound().loop(0, ENEMY_CONS_PITCH, 0));
        ghost.setDrawScale(scale);
        ghost.activatePhysics(world);
        enemies.add(ghost);
        // Create ghost controller
        AIControllers.add(new AIGhostController(enemies.size()-1, levelModel, enemies, player));
    }

    /**
     * Launch a flare from the player towards the mouse position based on preset flareJSON data, or does nothing if the
     * player has already created the max number of flares.
     * (Called by GameEngine)
     *
     * @param mousePosition Position of mouse when flare launched
     */
    public void createFlare(Vector2 mousePosition, Vector2 screenDimensions){
        if (player.getFlareCount() > 0) {
            player.throwFlare();
            FlareModel flare = new FlareModel(player.getFireBuddyPosition());
            flare.setDrawScale(scale);
            flare.initialize(flareJSON);
            flare.activatePhysics(world);
            Vector2 centerScreenPosition = new Vector2((screenDimensions.x) / 2, (screenDimensions.y) / 2);
            Vector2 posDif = new Vector2(mousePosition.x - centerScreenPosition.x, mousePosition.y - centerScreenPosition.y);
            float angleRad = posDif.angleRad(new Vector2(1, 0));
            Vector2 force = (new Vector2(flare.getInitialForce(), 0)).rotateRad(angleRad);
            flare.applyInitialForce(angleRad, force);
            flare.getShotSound().play(PLAYER_FLARE_VOL);
            flares.add(flare);
            assert inBounds(flare);
            player.decFlareCount();
        }
    }

    /**
     * Fires a bullet from an enemy
     */
    public void fireWeapon(EnemyTypeBModel enemy) {
        Vector2 enemyPos = enemy.getPosition();
        FireballModel fireball = new FireballModel(enemyPos);
        fireball.setDrawScale(scale);
        fireball.initialize(fireballJSON);
        fireball.activatePhysics(world);
        Vector2 posDif = new Vector2(enemy.getFiringTarget().x - enemyPos.x, enemy.getFiringTarget().y- enemyPos.y);
        posDif.nor();  // Normalize vector
        posDif.setLength(fireball.getSpeed());
        fireball.setLinearVelocity(posDif);
        fireballs.add(fireball);
        enemy.coolDown(false);
        assert inBounds(fireball);
    }

    /**
     * Change the player's movement to sprint
     * Store current light radius in lightRadiusSaved and change light radius to lightRadiusSprint
     * (Called by GameEngine)
     */
    public void makeSprint(){
        player.setLightRadiusSaved(player.getLightRadius());
        player.setLightRadius(player.getLightRadiusSprint());
        player.setSprinting();
    }

    /**
     * Change the player's movement to walk
     * Set to walk and restore light radius to what it was before sprinting, which is in lightRadiusSaved
     * (Called by GameEngine)
     */
    public void makeWalk(){
        player.setLightRadius(player.getLightRadiusSaved());
        player.setWalking();
    }

    /**
     * Change the player's movement to sneak
     * Store current light radius in lightRadiusSaved and change light radius to lightRadiusSneak
     * (Called by GameEngine)
     */
    public void makeSneak(){
        player.setLightRadiusSaved(player.getLightRadius());
        player.setLightRadiusSneak();
        player.setSneaking();
    }

    /**
     * Change lightRadius generated from player. (Called by GameEngine)
     * @param lightRadius radius of light around player
     */
    public void lightFromPlayer(float lightRadius) {
        if(player.isWalking())
            player.incrementLightRadius(lightRadius);
    }

    /**
     * Draws the level to the given game canvas
     *
     * If debug mode is true, it will outline all physics bodies as wireframes. Otherwise
     * it will only draw the sprite representations.
     *
     * @param canvas	the drawing context
     */
    public void draw(GameCanvas canvas, float delta, BitmapFont displayFont) {
        canvas.clear();
        canvas.setCameraPosition(player.getPosition().x * scale.x, player.getPosition().y * scale.y);

        canvas.begin();
        //draw background
        if (background != null) {
            canvas.draw(background, Color.WHITE, 0,0,
                    bounds.width * scale.x, bounds.height * scale.y);
        }

        // Draw all objects
        exit.draw(canvas);
        for(WallModel wall : walls) {
            wall.draw(canvas);
        }
        for(EnemyModel enemy : enemies) {
            enemy.draw(canvas);
        }
        for(FlareModel flare : flares) {
            flare.draw(canvas);
        }
        for(FireballModel fireball : fireballs){
            fireball.draw(canvas);
        }
        for(ItemModel item : items) {
            item.draw(canvas);
        }
        player.draw(canvas);
        canvas.end();

        lightController.setDebug(debug2);
        lightController.draw();
        fogController.updateFogAndDraw(canvas, scale, delta);

        drawPowerMeter(canvas);
        drawFlares(canvas);
        textController.draw(canvas);

        // Draw debugging on top of everything.
        if (debug == 1) {
            canvas.beginDebug();
            player.drawDebug(canvas);
            exit.drawDebug(canvas);
            for(WallModel wall : walls) {
                wall.drawDebug(canvas);
            }
            for(FlareModel flare : flares) {
                flare.drawDebug(canvas);
            }
            for(EnemyModel enemy : enemies) {
                enemy.drawDebug(canvas);
            }
            for(FireballModel fireball: fireballs){
                fireball.drawDebug(canvas);
            }
            for(ItemModel item : items) {
                item.drawDebug(canvas);
            }
            canvas.endDebug();
            if(ticks % 10 == 0){
                fps = 1/delta;
            }
            displayFont.setColor(Color.CYAN);
            canvas.begin();
            canvas.drawText(Float.toString(fps), displayFont, 0, canvas.getHeight()/2);
            canvas.end();
            ticks++;
        } else if (debug == 2) {
            canvas.beginDebugFilled();
            levelModel.drawDebug(canvas, scale);
            canvas.endDebug();
        }
    }

    /**
     * Draws the number of flares the player has left,
     * a helper method for LevelController.draw()
     *
     * PlayerModel player must not be null
     *
     * @param canvas the drawing context
     */
    private void drawFlares(GameCanvas canvas) {
        canvas.begin();

        float ox = scale.x * (player.getX() + flareCountOffset.x);
        float oy = scale.y * (player.getY() + flareCountOffset.y);

        float flareWidth = activeFlareCountTexture.getRegionWidth() + flareCountSplit * scale.x;

        if (activeFlareCountTexture != null && inactiveFlareCountTexture != null) {
            int flaresUsed = player.getMaxFlareCount() - player.getFlareCount();

            for(int i = flaresUsed; i < player.getMaxFlareCount(); i++){
                float activeFlareX = ox - i * flareWidth;
                canvas.draw(activeFlareCountTexture, activeFlareX, oy);
            }
            for(int j = 0; j < flaresUsed; j++){
                float inactiveFlareX = ox - j * flareWidth;
                canvas.draw(inactiveFlareCountTexture, inactiveFlareX, oy);
            }
        }

        canvas.end();
    }

    /**
     * Draws power meter, a helper method for LevelController.draw()
     *
     * PlayerModel player and Vector2 scale must not be null
     *
     * @param canvas the drawing context
     */
    private void drawPowerMeter(GameCanvas canvas){
        canvas.begin();

        if(powerBarLeft == null && powerBarRight == null && powerBarMiddle == null
                && powerBarActive == null && powerBarInactive == null && powerBarForeground == null){
            canvas.end();
            return;
        }

        float ox = scale.x * (player.getX() + powerBarOffset.x);
        float oy = scale.y * (player.getY() + powerBarOffset.y);

        float oxLeft = ox + powerBarBackgroundOffset.x * scale.x;
        float oyLeft = oy + powerBarBackgroundOffset.y * scale.y;
        canvas.draw(powerBarLeft, oxLeft, oyLeft);

        float oxMiddle = oxLeft + powerBarLeft.getRegionWidth();
        float oyMiddle = oyLeft;
        float middleWidth = powerBarMiddle.getRegionWidth() * (player.getMaxPowerVal() / globalMaxPowerValue);
        canvas.draw(powerBarMiddle, Color.WHITE, oxMiddle, oyMiddle, middleWidth, powerBarMiddle.getRegionHeight());

        float oxRight = oxMiddle + middleWidth;
        float oyRight = oyMiddle;
        canvas.draw(powerBarRight, oxRight, oyRight);

        if(player.getPowerVal() > 0) {
            canvas.draw(powerBarInactive, ox, oy);

            float oxFront = oxMiddle + powerBarForegroundOffset.x * scale.x;
            float oyFront = oyMiddle + powerBarForegroundOffset.y * scale.y;
            float percentFilled = player.getPowerVal() / globalMaxPowerValue;
            float barLength = percentFilled * powerBarMiddle.getRegionWidth();
            float barHeight = powerBarForeground.getRegionHeight();

            //white chosen as dummy color (no tint)
            canvas.draw(powerBarForeground, Color.WHITE, oxFront, oyFront, barLength, barHeight);

        } else {
            canvas.draw(powerBarActive, ox, oy);
        }

        canvas.end();
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We handle
     * most collisions here
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

            // Check for win condition
            if ((bd1 == player && bd2 == exit  )
                    || (bd1 == exit && bd2 == player)) {
            setLevelState(levelState.WIN);
                return;
            }
            // Check for loss condition 1 (player runs into enemy)
            if((bd1 == player && bd2 instanceof EnemyModel)
                    || (bd1 instanceof  EnemyModel && bd2 == player)){
                player.die();
                return;
            }
            // Check if flare collides with wall and if so stop it
            if((bd1 instanceof FlareModel && bd2 instanceof WallModel
                    || bd1 instanceof  WallModel && bd2 instanceof FlareModel)) {
                if(bd1 instanceof FlareModel)
                    ((FlareModel) bd1).stopMovement();
                else
                    ((FlareModel) bd2).stopMovement();
            }
            // Check for loss condition 2 (fireball hits player)
            if((bd1 instanceof FireballModel && bd2 instanceof PlayerModel
                    || bd1 instanceof  PlayerModel && bd2 instanceof FireballModel)) {
                player.die();
                return;
            }
            // Check for fireball-wall collision and if so remove fireball
            if((bd1 instanceof FireballModel && bd2 instanceof WallModel
                    || bd1 instanceof  WallModel && bd2 instanceof FireballModel)) {
                if(bd1 instanceof FireballModel){
                    ((FireballModel) bd1).deactivate();
                }
                else{
                    ((FireballModel) bd2).deactivate();
                }
            }
            // Check for item pick-up
            if((bd1 instanceof ItemModel && bd2 instanceof PlayerModel
                    || bd1 instanceof PlayerModel && bd2 instanceof ItemModel)) {
                // Ensure bd1 is item
                if(bd2 instanceof ItemModel) {
                    Obstacle temp = bd2;
                    bd2 = bd1;
                    bd1 = temp;
                }
                // Add contact to be handled later (not handled here, so we can handle potentially
                // after beginContact is finished)
                itemContacts.add((ItemModel)bd1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /** Unused ContactListener method */
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();
        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();
        Obstacle bd1 = (Obstacle)body1.getUserData();
        Obstacle bd2 = (Obstacle)body2.getUserData();

        // Check if need to remove contact from itemContacts
        if(bd1 instanceof ItemModel || bd2 instanceof ItemModel) {
            // Ensure bd1 is item
            if (bd2 instanceof ItemModel) {
                Obstacle temp = bd2;
                bd2 = bd1;
                bd1 = temp;
            }
            if(itemContacts.contains(bd1))
                itemContacts.remove(bd1);
        }

    }
    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}

}

