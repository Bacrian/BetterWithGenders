package imbacrian.betterwithgenders.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.tileentity.TileEntityRendererStatue;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.block.entity.TileEntityStatue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
@Mixin(value = TileEntityRendererStatue.class, remap = false)
public class TileEntityRendererStatueMixin {

	@Inject(method = "renderModel", at = @At(value = "INVOKE", target = "Lorg/useless/dragonfly/models/entity/StaticEntityModel;render()V", shift = At.Shift.BEFORE))
	private void bwg$hideStatueBreasts(TessellatorGeneral tessellator, StaticEntityModel model, TileEntityStatue.Pose pose, CallbackInfo ci) {
		// Hide breast bones on statues - they should not have breasts. My bad, whoops.
		hideBreastBones(model, "breastLeft", "breastRight");
		hideBreastBones(model, "jacketBreastLeft", "jacketBreastRight");
		hideBreastBones(model, "armorBreastLeft", "armorBreastRight");
	}

	private void hideBreastBones(StaticEntityModel model, String leftName, String rightName) {
		var left = model.getTransform(leftName);
		var right = model.getTransform(rightName);
		if (left != null) left.visible = false;
		if (right != null) right.visible = false;
	}
}
