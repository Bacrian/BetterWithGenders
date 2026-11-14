package imbacrian.betterwithgenders.client;

import imbacrian.betterwithgenders.gui.GuiGenderMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.core.lang.I18n;
import org.lwjgl.input.Keyboard;
import net.minecraft.client.option.GameSettings;

public class Keybindings {
	public static final KeyBinding genderGUI = GameSettings.register(
		new KeyBinding("key.bwgenders.opengui").setDefault(InputDevice.keyboard, Keyboard.KEY_G)
	);

	private static boolean debugKeyWasDown = false;

	public static void checkKeys(Minecraft mc) {
		/* For debugging
		boolean debugKeyDown = Keyboard.isKeyDown(Keyboard.KEY_F8);
		if (debugKeyDown && !debugKeyWasDown && mc.thePlayer != null) BwgDebug.next();
		debugKeyWasDown = debugKeyDown; */

		if (mc.thePlayer != null && genderGUI.isPressed()) {
			if (mc.currentScreen == null) {
				mc.displayScreen(new GuiGenderMenu());
			}
		}
	}
}
