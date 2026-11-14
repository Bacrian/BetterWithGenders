package imbacrian.betterwithgenders.mixin;

import imbacrian.betterwithgenders.api.GenderData;
import imbacrian.betterwithgenders.render.MammaryModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.HumanArmorShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
@Mixin(value = MobRendererPlayer.class, remap = false)
public abstract class MobRendererPlayerMixin extends MobRenderer<Player> {
	protected MobRendererPlayerMixin(float shadowSize) {
		super(shadowSize);
	}

	@Inject(method = "setupAnimations", at = @At("RETURN"))
	private void bwg$applyBreasts(Player entity, StaticEntityModel model, float partialTick, int layer,
	                              CallbackInfoReturnable<StaticEntityModel> cir) {
		StaticEntityModel result = cir.getReturnValue();
		if (result == null || layer != 0) return;

		GenderData data = (GenderData) entity;
		boolean hasChestplate = entity.getItemInArmorSlot(HumanArmorShape.CHEST) != null;
		MammaryModel.apply(result, data, partialTick, hasChestplate);
	}
}
