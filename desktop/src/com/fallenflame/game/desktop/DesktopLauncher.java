package com.fallenflame.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.fallenflame.game.GDXRoot;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width  = 800;
		config.height = 600;
		// config.resizable = false; we may want this later
		new LwjglApplication(new GDXRoot(), config);
	}
}
