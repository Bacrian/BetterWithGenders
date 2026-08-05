package imbacrian.betterwithgenders.client;

import imbacrian.bapilib.util.KeybindCategoryHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BwgOptions {
	private static final KeybindCategoryHelper helper = new KeybindCategoryHelper();

	public static void init() {
		helper.addCategory("options.category.bwgenders", Keybindings.genderGUI);
	}
}
