package imbacrian.betterwithgenders.mixin;

import imbacrian.betterwithgenders.client.Keybindings;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "runTick", at = @At("HEAD"))
	private void checkMyKeys(CallbackInfo ci) {
		Minecraft minecraft = (Minecraft) (Object) this;
		Keybindings.checkKeys(minecraft);
	}
}
