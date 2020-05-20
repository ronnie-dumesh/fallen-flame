package com.fallenflame.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

import java.util.HashMap;
import java.util.Map;

public class InputBindings {
    private static final Preferences prefs = Gdx.app.getPreferences("fallen_flame_key_binding");
    public enum Control {
        GO_UP,
        GO_DOWN,
        GO_LEFT,
        GO_RIGHT,
        RESET_LEVEL,
        SNEAKING,
        SPRINTING,
    }
    private static final Map<Control, Integer> bindings;
    static {
        bindings = new HashMap<>();
        reset(false);
        readFromPrefs();
    }
    public static int getBindingOf(Control c) {
        return bindings.get(c);
    }
    private static boolean allowed(int i) {
        switch (i) {
            case Input.Keys.A:
            case Input.Keys.B:
            case Input.Keys.C:
            case Input.Keys.D:
            case Input.Keys.E:
            case Input.Keys.F:
            case Input.Keys.G:
            case Input.Keys.H:
            case Input.Keys.I:
            case Input.Keys.J:
            case Input.Keys.K:
            case Input.Keys.L:
            case Input.Keys.M:
            case Input.Keys.N:
            case Input.Keys.O:
            case Input.Keys.P:
            case Input.Keys.Q:
            case Input.Keys.R:
            case Input.Keys.S:
            case Input.Keys.T:
            case Input.Keys.U:
            case Input.Keys.V:
            case Input.Keys.W:
            case Input.Keys.X:
            case Input.Keys.Y:
            case Input.Keys.Z:
            case Input.Keys.ALT_LEFT:
            case Input.Keys.ALT_RIGHT:
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.CONTROL_RIGHT:
            case Input.Keys.COMMA:
            case Input.Keys.PERIOD:
            case Input.Keys.SLASH:
            case Input.Keys.BACKSLASH:
            case Input.Keys.LEFT_BRACKET:
            case Input.Keys.RIGHT_BRACKET:
            case Input.Keys.COLON:
            case Input.Keys.UP:
            case Input.Keys.LEFT:
            case Input.Keys.DOWN:
            case Input.Keys.RIGHT:
            case Input.Keys.SPACE:
                return true;
            default:
                return false;
        }
    }
    private static void readFromPrefs() {
        for (Control c : Control.values()) {
            String id = controlToID(c);
            if (prefs.contains(id)) {
                setBindingOf(c, prefs.getInteger(id), false, false, false);
            }
        }
    }
    private static void switchControlIfNeeded(Control c, int i) {
        Control con = null;
        for (Map.Entry<Control, Integer> c2 : bindings.entrySet()) {
            if (c == c2.getKey()) continue;
            if (i == c2.getValue()) {
                con = c2.getKey();
                break;
            }
        }
        if (con != null) {
            setBindingOf(con, getBindingOf(c), false, false);
        }
    }
    public static void reset(boolean resetPrefs) {
        bindings.put(Control.GO_UP, Input.Keys.W);
        bindings.put(Control.GO_DOWN, Input.Keys.S);
        bindings.put(Control.GO_LEFT, Input.Keys.A);
        bindings.put(Control.GO_RIGHT, Input.Keys.D);
        bindings.put(Control.RESET_LEVEL, Input.Keys.R);
        bindings.put(Control.SNEAKING, Input.Keys.CONTROL_LEFT);
        bindings.put(Control.SPRINTING, Input.Keys.SHIFT_LEFT);
        if (resetPrefs) {
            prefs.clear();
            prefs.flush();
        }
    }
    public static void reset() {
        reset(true);
    }
    public static String keyToString(int k) {
        switch(k) {
            case Input.Keys.COMMA: return "Comma";
            case Input.Keys.PERIOD: return "Full Stop/Period";
            case Input.Keys.SLASH: return "Slash";
            case Input.Keys.BACKSLASH: return "Backslash";
            case Input.Keys.LEFT_BRACKET: return "L-Bracket";
            case Input.Keys.RIGHT_BRACKET: return "R-Bracket";
            case Input.Keys.COLON: return "Colon";
            case Input.Keys.ALT_LEFT: {
                try {
                    return System.getProperty("os.name").toLowerCase().contains("mac") ? "L-Option" : "L-Alt";
                } catch (Exception ignored) {
                    return "L-Alt";
                }
            }
            case Input.Keys.ALT_RIGHT: {
                try {
                    return System.getProperty("os.name").toLowerCase().contains("mac") ? "R-Option" : "R-Alt";
                } catch (Exception ignored) {
                    return "R-Alt";
                }
            }
            default:
                return Input.Keys.toString(k);
        }
    }
    private static void setBindingOf(Control c, int i, boolean shortcutCheck, boolean swapCheck, boolean saveToPref) {
        if (!allowed(i)) return;
        if (swapCheck) switchControlIfNeeded(c, i);
        bindings.put(c, i);
        if (shortcutCheck && (c == Control.GO_UP && i == Input.Keys.UP
                && getBindingOf(Control.GO_DOWN) == Input.Keys.S
                && getBindingOf(Control.GO_LEFT) == Input.Keys.A
                && getBindingOf(Control.GO_RIGHT) == Input.Keys.D) ||
                (c == Control.GO_DOWN && i == Input.Keys.DOWN
                        && getBindingOf(Control.GO_UP) == Input.Keys.W
                        && getBindingOf(Control.GO_LEFT) == Input.Keys.A
                        && getBindingOf(Control.GO_RIGHT) == Input.Keys.D) ||
                (c == Control.GO_LEFT && i == Input.Keys.LEFT
                        && getBindingOf(Control.GO_DOWN) == Input.Keys.S
                        && getBindingOf(Control.GO_UP) == Input.Keys.W
                        && getBindingOf(Control.GO_RIGHT) == Input.Keys.D) ||
                (c == Control.GO_RIGHT && i == Input.Keys.RIGHT
                        && getBindingOf(Control.GO_DOWN) == Input.Keys.S
                        && getBindingOf(Control.GO_LEFT) == Input.Keys.A
                        && getBindingOf(Control.GO_UP) == Input.Keys.W)) {
            setBindingOf(Control.GO_UP, Input.Keys.UP, false);
            setBindingOf(Control.GO_LEFT, Input.Keys.LEFT, false);
            setBindingOf(Control.GO_RIGHT, Input.Keys.RIGHT, false);
            setBindingOf(Control.GO_DOWN, Input.Keys.DOWN, false);
        } else if (shortcutCheck && (c == Control.GO_UP && i == Input.Keys.W
                && getBindingOf(Control.GO_DOWN) == Input.Keys.DOWN
                && getBindingOf(Control.GO_LEFT) == Input.Keys.LEFT
                && getBindingOf(Control.GO_RIGHT) == Input.Keys.RIGHT) ||
                (c == Control.GO_DOWN && i == Input.Keys.S
                        && getBindingOf(Control.GO_UP) == Input.Keys.UP
                        && getBindingOf(Control.GO_LEFT) == Input.Keys.LEFT
                        && getBindingOf(Control.GO_RIGHT) == Input.Keys.RIGHT) ||
                (c == Control.GO_LEFT && i == Input.Keys.A
                        && getBindingOf(Control.GO_DOWN) == Input.Keys.DOWN
                        && getBindingOf(Control.GO_UP) == Input.Keys.UP
                        && getBindingOf(Control.GO_RIGHT) == Input.Keys.RIGHT) ||
                (c == Control.GO_RIGHT && i == Input.Keys.D
                        && getBindingOf(Control.GO_DOWN) == Input.Keys.DOWN
                        && getBindingOf(Control.GO_LEFT) == Input.Keys.LEFT
                        && getBindingOf(Control.GO_UP) == Input.Keys.UP)) {
            setBindingOf(Control.GO_UP, Input.Keys.W, false);
            setBindingOf(Control.GO_LEFT, Input.Keys.A, false);
            setBindingOf(Control.GO_RIGHT, Input.Keys.D, false);
            setBindingOf(Control.GO_DOWN, Input.Keys.S, false);
        }
        if (saveToPref) {
            prefs.putInteger(controlToID(c), i);
            prefs.flush();
        }
    }
    private static void setBindingOf(Control c, int i, boolean shortcutCheck, boolean swapCheck) {
        setBindingOf(c, i, shortcutCheck, swapCheck, true);
    }
    private static void setBindingOf(Control c, int i, boolean shortcutCheck) {
        setBindingOf(c, i, shortcutCheck, true);
    }
    public static void setBindingOf(Control c, int i) {
        setBindingOf(c, i, true);
    }
    public static String controlToID(Control c) {
        switch (c) {
            case GO_UP: return "up";
            case GO_DOWN: return "down";
            case GO_LEFT: return "left";
            case GO_RIGHT: return "right";
            case RESET_LEVEL: return "reset";
            case SNEAKING: return "sneak";
            case SPRINTING: return "sprint";
        }
        return null;
    }
    public static String[] controlToIDs(Control c) {
        switch (c) {
            case GO_UP: return new String[]{"up"};
            case GO_DOWN: return new String[]{"down"};
            case GO_LEFT: return new String[]{"left"};
            case GO_RIGHT: return new String[]{"right"};
            case RESET_LEVEL: return new String[]{"reset"};
            case SNEAKING: return new String[]{"sneak"};
            case SPRINTING: return new String[]{"sprint"};
        }
        return null;
    }
    public static Control idToControl(String c) {
        switch (c.toLowerCase()) {
            case "up": return Control.GO_UP;
            case "down": return Control.GO_DOWN;
            case "left": return Control.GO_LEFT;
            case "right": return Control.GO_RIGHT;
            case "reset": return Control.RESET_LEVEL;
            case "sneak": return Control.SNEAKING;
            case "sprint": return Control.SPRINTING;
        }
        return null;
    }
    public static String controlToString(Control c) {
        switch (c) {
            case GO_UP: return "Go up";
            case GO_DOWN: return "Go down";
            case GO_LEFT: return "Go left";
            case GO_RIGHT: return "Go right";
            case RESET_LEVEL: return "Reset level";
            case SNEAKING: return "Sneaking";
            case SPRINTING: return "Sprinting";
        }
        return null;
    }
}
