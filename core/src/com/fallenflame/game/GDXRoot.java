package com.fallenflame.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import com.fallenflame.game.util.ScreenListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter.
 * There must be some undocumented OpenGL code in setScreen.
 *
 * This class differs slightly from the labs in that the AssetManager is now a
 * singleton and is not constructed by this class.
 *
 * Author: Walker M. White
 * Version: 3/2/2016
 */
/**Unofficial core root of the game (excluding DesktopLauncher).
 * @author: Walker M. White */
public class GDXRoot extends Game implements ScreenListener {
	/** Drawing context to display graphics */
	private GameCanvas canvas;
	/** Drawing context to display level select */
	private GameCanvas levelCanvas;
	/** Asset Loading Screen. What will show  */
	private LoadingMode loading;
	/** Asset Control Screen. What will show  */
	private ControlMode control;
	/** Player mode for the the game */
	private GameEngine engine;
	/** World select for the game */
	private WorldSelectMode worldSelect;
	/** Level select for the game */
	private LevelSelectMode levelSelect;
	/** Pause for the game */
	private PauseMode pauseMode;
	private Transition transition;


	/**
	 * Creates a new game from the configuration settings.
	 */
	public GDXRoot() {}

	/**
	 * Called when the Application is first created.
	 *
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas  = new GameCanvas();
		levelCanvas = new GameCanvas();
		loading = new LoadingMode(canvas,1);
		control = new ControlMode(levelCanvas);
		worldSelect = new WorldSelectMode(levelCanvas);
		levelSelect = new LevelSelectMode(levelCanvas);
		pauseMode = new PauseMode(levelCanvas);
		engine = new GameEngine(levelSelect);
		InputMultiplexer multiplexer = new InputMultiplexer(); //Allows for multiple InputProcessors
		//Multiplexer is an ordered list, so when an event occurs, it'll check loadingMode first, and then GameEngine
		multiplexer.addProcessor(loading);
		multiplexer.addProcessor(levelSelect);
		multiplexer.addProcessor(engine);
		Gdx.input.setInputProcessor(multiplexer);
		transition = new Transition();

		// Initialize the three game worlds
		engine.preLoadContent();
		loading.setScreenListener(this);
		levelSelect.initialize(engine.getLevelSaves());
		setScreen(loading, false);
	}

	/**
	 * Called when the Application is destroyed.
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		setScreen(null);
		engine.unloadContent();
		engine.dispose();
		canvas.dispose();
		levelCanvas.dispose();
		control.dispose();
		canvas = null;
		transition.dispose();

		// Unload all of the resources
		super.dispose();
	}

	@Override
	public void render () {
		if (screen != null) screen.render(Gdx.graphics.getDeltaTime());
		transition.draw();
	}

	/**
	 * Called when the Application is resized.
	 *
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		if (engine != null) {
			engine.resize(width,height);
		}
		if(loading != null){
			loading.resize(width, height);
		}
		// Canvas knows the size, but not that it changed
		canvas.resize();
		super.resize(width,height);
	}

	@Override
	public void setScreen (Screen screen) {
		this.setScreen(screen, true);
	}

	public void setScreen (Screen screen, boolean doTransition) {
		if (doTransition) transition.screenshot();
		super.setScreen(screen);
	}

	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == loading) {
			if (exitCode == 420) {
				engine.loadContent();
			} else {
				if (loading.toControl) {
					Gdx.input.setInputProcessor(control);
					control.setScreenListener(this);
					setScreen(control);
				} else {
					Gdx.input.setInputProcessor(worldSelect);
					worldSelect.setScreenListener(this);
					setScreen(worldSelect);
				}
			}

//			loading.dispose();
//			loading = null;
		} else if (screen == worldSelect) {
			if (worldSelect.getWorldSelected() >= 0) {
				levelSelect.setWorldSelected(worldSelect.getWorldSelected());
				Gdx.input.setInputProcessor(levelSelect);
				levelSelect.setScreenListener(this);
				setScreen(levelSelect);
			} else { // World select = -1 means go back.
				Gdx.input.setInputProcessor(loading);
				loading.setScreenListener(this);
				loading.setScreenListener(this);
				setScreen(loading);
			}
		} else if (screen == levelSelect) {
			if (levelSelect.getLevelSelected() >= 0) {
				Gdx.input.setInputProcessor(engine);
				engine.setScreenListener(this);
				engine.setCanvas(canvas);
				engine.reset(levelSelect.getLevelSelected());
				engine.resume();
				setScreen(engine);
			} else { // Level select = -1 means go back.
				Gdx.input.setInputProcessor(loading);
				loading.setScreenListener(this);
				loading.setScreenListener(this);
				setScreen(loading);
			}
		} else if (screen == engine) {
			if(exitCode == 1){
				Gdx.input.setInputProcessor(levelSelect);
				levelSelect.setScreenListener(this);
				setScreen(levelSelect);
				engine.pause();
			}
			else {
				Gdx.input.setInputProcessor(pauseMode);
				pauseMode.setScreenListener(this);
				pauseMode.screenshot();
				setScreen(pauseMode);
				engine.pause();
			}
//			levelSelect.reset();
		} else if (screen == pauseMode) {
			switch (exitCode) {
				case 0:
					Gdx.input.setInputProcessor(engine);
					engine.setScreenListener(this);
					setScreen(engine);
					engine.resume();
					break;
				case 1:
					Gdx.input.setInputProcessor(engine);
					engine.setScreenListener(this);
					engine.reset();
					setScreen(engine);
					engine.resume();
					break;
				case 2:
					Gdx.input.setInputProcessor(levelSelect);
					levelSelect.setScreenListener(this);
					setScreen(levelSelect);
					engine.pause();
					break;
				case 3:
					Gdx.input.setInputProcessor(control);
					control.setScreenListener(this);
					control.screenshot();
					setScreen(control);
					engine.pause();
					break;
			}
		} else if (screen == control) {
			if (control.hasScreenshot()) {
				Gdx.input.setInputProcessor(pauseMode);
				pauseMode.setScreenListener(this);
				setScreen(pauseMode);
				engine.pause();
			} else {
				Gdx.input.setInputProcessor(loading);
				loading.setScreenListener(this);
				loading.setScreenListener(this);
				setScreen(loading);
			}
		} else if (exitCode == engine.EXIT_QUIT) {
			// We quit the main application
			Gdx.app.exit();
		}
	}

}

/**
 * Transition adds a fade-in fade-out effect when switching between screens. It renders to the screen directly and does
 * NOT use GameCanvas.
 */
class Transition {
	/** A map of screenshots to alpha value. A screenshot is a texture region of the previous screen. */
	private Map<TextureRegion, Float> screenshots = new HashMap<>();

	/** The sprite batch to draw on. */
	private SpriteBatch spriteBatch;
	Transition() {
		spriteBatch = new SpriteBatch();
	}

	/**
	 * Dispose this instance.
	 */
	void dispose() {
		spriteBatch.dispose();
		screenshots.clear();
		screenshots = null;
		spriteBatch = null;
	}

	/**
	 * Draw screenshots if necessary. Call this after the main rendering logic.
	 */
	void draw() {
		// If no screenshots, skip.
		if (screenshots.isEmpty()) return;

		// Make sure blend is enabled.
		boolean glBlendEnabled = Gdx.gl.glIsEnabled(GL20.GL_BLEND);
		if (!glBlendEnabled) Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		spriteBatch.begin();
		Color c = spriteBatch.getColor();

		for (Iterator<Map.Entry<TextureRegion, Float>> it = screenshots.entrySet().iterator(); it.hasNext();) {
			Map.Entry<TextureRegion, Float> item = it.next();
			// If alpha <= 0, remove it.
			if (item.getValue() <= 0) {
				it.remove();
				continue;
			}
			// Set colour to enable alpha.
			spriteBatch.setColor(c.r, c.g, c.b, item.getValue());
			spriteBatch.draw(item.getKey(), 0, 0);

			item.setValue(item.getValue() - .05f);
		}

		spriteBatch.end();

		// Disable blend if it wasn't enabled before.
		if (!glBlendEnabled) Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	/**
	 * Take a screenshot of the current screen so that the next time draw is called a fade out effect will happen. Call
	 * this right before screen-switching.
	 */
	void screenshot() {
		screenshots.put(ScreenUtils.getFrameBufferTexture(), 1f);
	}
}