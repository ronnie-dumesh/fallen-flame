package com.fallenflame.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.fallenflame.game.GDXRoot;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width  = 1260;
		config.height = 720;
		config.title = "Fallen Flame";
		config.resizable = true;
		config.fullscreen = false;
		new LwjglApplication(new GDXRoot(), config);
	}
}
