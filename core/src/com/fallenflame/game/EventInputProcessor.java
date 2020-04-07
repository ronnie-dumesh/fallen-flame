package com.fallenflame.game;

import com.badlogic.gdx.InputProcessor;
import com.fallenflame.game.GameEngine;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;

/** Controller to handle event-based inputs from the game. This is in a separate file from InputController, as
 * InputController handles polling events (or events that occur on an update
 * This is also separate from the LoadingMode InputProcessor to prevent LoadingMode from needing GameEngine or having to
 * call a function from GDXRoot (therefore causing a cycle)
 * Additionally, it splits the features well, and makes it easier to make a distinction between event-driven inputs
 * that LoadingMode needs versus event-driven events that the main game needs*/

public class EventInputProcessor implements InputProcessor {
   private GameEngine gameEngine;

   public EventInputProcessor(GameEngine g){
       gameEngine = g;
   }
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
        if(!gameEngine.isScreenActive()){
            return true;
        }
       if(amount == 1){
           gameEngine.lightFromPlayer(-1.0f);
       }
       if(amount == -1){
           gameEngine.lightFromPlayer(1.0f);
       }

       return true;
    }
}
