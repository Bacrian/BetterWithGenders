package imbacrian.betterwithgenders.mixin;

import net.minecraft.client.gui.options.components.KeyBindingComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.data.OptionsPages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import imbacrian.betterwithgenders.client.Keybindings;

@Mixin(OptionsPages.class)
public class OptionsPagesMixin {

	@Unique
	private static boolean addedCustomCategory = false;
	@Inject(method = "init", at = @At("RETURN"))
	private static void addModKeybindCategory(CallbackInfo ci) {
		if (!addedCustomCategory) {
			try {
				OptionsCategory genderCategory = new OptionsCategory("options.category.bwgenders");
				genderCategory.withComponent(new KeyBindingComponent(Keybindings.genderGUI));
				OptionsPages.CONTROLS.withComponent(genderCategory);
				addedCustomCategory = true;
			} catch (Exception e) {
				// Yeah, it retries if it fails.
			}
		}
	}
}
