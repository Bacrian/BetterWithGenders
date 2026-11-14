package imbacrian.betterwithgenders.mixin;

import imbacrian.betterwithgenders.api.Gender;
import imbacrian.betterwithgenders.api.GenderData;
import imbacrian.betterwithgenders.physics.BreastPhysics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Player.class)
public abstract class PlayerPhysicsTickMixin {

	@Inject(method = "tick", at = @At("RETURN"))
	private void bwg$tickBreastPhysics(CallbackInfo ci) {
		Player player = (Player) (Object) this;
		GenderData data = (GenderData) player;
		BreastPhysics physics = data.bwg$getPhysics();

		if (data.bwg$getGender() == Gender.MALE || !data.bwg$isJiggleEnabled()) {
			physics.reset();
			return;
		}

		physics.update(player.x, player.y, player.z, player.yBodyRot, player.walkAnimPos, player.walkAnimSpeed,
			player.isSneaking(), player.tickCount, data.bwg$isIndividualPhysics());
	}
}
