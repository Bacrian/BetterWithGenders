package imbacrian.betterwithgenders.mixin;

import imbacrian.betterwithgenders.api.CustomBodyData;
import imbacrian.betterwithgenders.api.GenderData;
import imbacrian.betterwithgenders.render.MammaryModel;
import net.minecraft.client.render.entity.MobRendererBiped;
import net.minecraft.core.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Mixin(MobRendererBiped.class)
public abstract class MobRendererBipedMixin<T extends Mob> {

	@Inject(method = "setupAnimations", at = @At("RETURN"))
	private void bwg$applyGenericBust(T entity, StaticEntityModel model, float partialTick, int layer,
	                                  CallbackInfoReturnable<StaticEntityModel> cir) {
		if (layer != 0) return;
		if (entity instanceof GenderData) return;

		StaticEntityModel result = cir.getReturnValue();
		if (result == null) return;

		if (entity instanceof CustomBodyData custom) {
			MammaryModel.applyStatic(result, custom.bwg$hasCustomBust(), custom.bwg$getCustomBustSize(), "breastLeft", "breastRight");
		} else {
			MammaryModel.applyStatic(result, false, 0F, "breastLeft", "breastRight"); // ni GenderData ni CustomBodyData: ocultar siempre
		}
	}
}
