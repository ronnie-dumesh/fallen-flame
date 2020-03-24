package com.fallenflame.game;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.fallenflame.game.physics.obstacle.Obstacle;

import java.util.*;

/** Credit to Walker White for some code reused from B2LightsDemo */
public class LevelController implements ContactListener {
    //  MAY NEED THESE:
//    /** Number of velocity iterations for the constrain solvers */
//    public static final int WORLD_VELOC = 6;
//    /** Number of position iterations for the constrain solvers */
//    public static final int WORLD_POSIT = 2;

    /** Whether or not the level has been populated */
    private boolean populated;

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
    /** Level Model for AI Pathfinding */
    private LevelModel levelModel;
    /** Flare JSONValue */
    private JsonValue flareJSON;

    /** Whether or not the level is in debug more (showing off physics) */
    private boolean debug;

    // World Definitions
    /** The Box2D world */
    protected World world;
    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;

    // Controllers
    /** Light Controller */
    private LightController lightController;
    /** AI Controllers */
    private List<AIController> AIControllers;

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
    public boolean getDebug() { return debug; }

    /**
     * Sets whether this level is currently in debug node
     *
     * If the level is in debug mode, then the physics bodies will all be drawn as
     * wireframes onscreen
     *
     * @param value	whether this level is currently in debug node
     */
    public void setDebug(boolean value) { debug = value; }

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
        debug  = false;
        levelState = LevelState.IN_PROGRESS;
        // Controllers
        lightController = new LightController();
        AIControllers = new LinkedList<>();
        // Models
        walls = new LinkedList<>();
        enemies = new LinkedList<>();
        flares = new LinkedList<>();
        levelModel = new LevelModel();
        // Not yet populated
        populated = false;
    }

    /**
     * Lays out the game geography and enemies from the given JSON file
     *
     * @param levelJson	the JSON tree defining the level
     */
    public void populate(JsonValue levelJson) {
        populated = true;

        float[] pSize = levelJson.get("physicsSize").asFloatArray();
        int[] gSize = levelJson.get("graphicSize").asIntArray();

        world = new World(Vector2.Zero,false);
        bounds = new Rectangle(0,0,pSize[0],pSize[1]);
        scale.x = gSize[0]/pSize[0];
        scale.y = gSize[1]/pSize[1];

        // Compute the FPS
        int[] fps = levelJson.get("fpsRange").asIntArray();
        maxFPS = fps[1]; minFPS = fps[0];
        timeStep = 1.0f/maxFPS;
        maxSteps = 1.0f + maxFPS/minFPS;
        maxTimePerFrame = timeStep*maxSteps;

        // Create player
        player = new PlayerModel();
        player.initialize(levelJson.get("player"));
        player.setDrawScale(scale);
        player.activatePhysics(world);
        // Create Exit
        exit = new ExitModel();
        exit.initialize(levelJson.get("exit"));
        exit.setDrawScale(scale);
        exit.activatePhysics(world);
        for(JsonValue wallJSON : levelJson.get("walls")) {
            WallModel wall = new WallModel();
            wall.initialize(wallJSON);
            wall.setDrawScale(scale);
            wall.activatePhysics(world);
            walls.add(wall);
        }
        for(JsonValue enemyJSON : levelJson.get("enemies")) {
            //TODO #6 INIT enemies --> waiting for AIController to be finished
            EnemyModel enemy = new EnemyModel();
            enemy.initialize(enemyJSON);
            enemy.setDrawScale(scale);
            enemy.activatePhysics(world);
            enemies.add(enemy);
            // AIController controller = new AIController(enemy, levelModel);
            // AIControllers.add(controller);
            //TODO #6
        }
        flareJSON = levelJson.get("flare");

        // Initialize levelModel
        levelModel.initialize(bounds, player, walls, enemies);

        lightController.initialize(player, levelJson.get("lighting"), world, bounds);
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

        for(WallModel wall : walls) {
            wall.deactivatePhysics(world);
            wall.dispose();
            walls.clear();
        }
        for(EnemyModel enemy : enemies) {
            enemy.deactivatePhysics(world);
            enemy.dispose();
            enemies.clear();
        }
        for(FlareModel flare : flares) {
            flare.deactivatePhysics(world);
            flare.dispose();
            flares.clear();
        }
        exit.deactivatePhysics(world);
        exit.dispose();
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
        // Update player (and update levelModel) and exit
        levelModel.removePlayer(player);
        player.update(dt);
        levelModel.placePlayer(player);

        // TODO: Waiting for AI Controller to be finished
//        // Get Enemy Actions
//        Iterator<AIController> ctrlI = AIControllers.iterator();
//        LinkedList<EnemyModel.Action> actions = new LinkedList();
//        while(ctrlI.hasNext()){
//            AIController ctrl = ctrlI.next();
//            actions.add(ctrl.getAction());
//        }
//        // Execute Enemy Actions (and update levelModel)
//        Iterator<EnemyModel> enemyI = enemies.iterator();
//        Iterator<EnemyModel> actionI = enemies.iterator();
//        while(enemyI.hasNext()){
//            EnemyModel enemy = enemyI.next();
//            levelModel.removeEnemy(enemy);
//            enemy.executeAction(actionI.next()); // TODO handle here or in enemyModel?
//            levelModel.placeEnemy(enemy);
//        }

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

        // Update lights
        lightController.updateLights(player, flares, enemies);
    }

    /**
     * Launch a flare from the player towards the mouse position based on preset flareJSON data.
     * (Called by GameEngine)
     *
     * @param mousePosition Position of mouse when flare launched
     */
    public void createFlare(Vector2 mousePosition){
        FlareModel flare = new FlareModel(player.getPosition());
        flare.initialize(flareJSON);
        flare.applyInitialForce(mousePosition.angle(), mousePosition.cpy());
        flares.add(flare);
    }

    /**
     * Moves the player. (Called by GameEngine)
     * @param angle angle player is facing
     * @param tempAngle movement angle of player (to be scaled by player force)
     */
    public void movePlayer(float angle, Vector2 tempAngle) {
        tempAngle.scl(player.getForce());
        player.setMovement(tempAngle.x, tempAngle.y);
        player.setAngle(angle);
        player.applyForce();
    }

    /**
     * Change lightRadius generated from player. (Called by GameEngine)
     * @param lightRadius radius of light around player
     */
    public void lightFromPlayer(float lightRadius) {
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
    public void draw(GameCanvas canvas) {
        canvas.clear();
        canvas.setCameraPosition(player.getPosition());

        // Draw all objects
        canvas.begin();
        player.draw(canvas);
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
        canvas.end();

        lightController.draw();

        // Draw debugging on top of everything.
        if (debug) {
            canvas.beginDebug();
            player.drawDebug(canvas);
            exit.drawDebug(canvas);
            for(WallModel wall : walls) {
                wall.drawDebug(canvas);
            }
            for(EnemyModel enemy : enemies) {
                enemy.drawDebug(canvas);
            }
            for(FlareModel flare : flares) {
                flare.drawDebug(canvas);
            }
            canvas.endDebug();
        }
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
            // Check for loss condition (player runs into enemy)
            if((bd1 == player && bd2 instanceof EnemyModel)
                    || (bd1 instanceof  EnemyModel && bd2 == player)){
                setLevelState(LevelState.LOSS);
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
            // Ensure flare does not collide with player or enemy
            if ((bd1 == player && bd2 instanceof FlareModel)
                    || (bd1 instanceof FlareModel && bd2 == player)
                    || (bd1 instanceof EnemyModel && bd2 instanceof FlareModel)
                    || (bd1 instanceof FlareModel && bd2 instanceof EnemyModel)) {
                contact.setEnabled(false);
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /** Unused ContactListener method */
    public void endContact(Contact contact) {}
    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}

}
