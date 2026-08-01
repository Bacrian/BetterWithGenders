package imbacrian.betterwithgenders.mixin;

import imbacrian.betterwithgenders.api.GenderData;
import imbacrian.betterwithgenders.render.MammaryModel;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.IArmorWearing;
import net.minecraft.core.enums.HumanArmorShape;
import net.minecraft.client.render.entity.MobRendererBipedArmored;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Mixin(MobRendererBipedArmored.class)
public abstract class MobRendererBipedArmoredMixin<T extends Mob & IArmorWearing<HumanArmorShape>> {

	@Inject(method = "setupAnimations", at = @At("RETURN"))
	private void bwg$applyArmorBreastSize(T entity, StaticEntityModel model, float partialTick, int layer,
	                                      CallbackInfoReturnable<StaticEntityModel> cir) {
		if (layer != 2) return;
		if (!(entity instanceof GenderData data)) return;

		StaticEntityModel result = cir.getReturnValue();
		if (result == null) return;

		double wobble = data.bwg$isJiggleWithArmor() ? data.bwg$getLastWobble() : 0.0;
		MammaryModel.applyArmor(result, data, wobble);
	}
}
