package imbacrian.betterwithgenders.mixin;

import imbacrian.betterwithgenders.api.StatueGenderData;
import imbacrian.betterwithgenders.render.MammaryModel;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.tileentity.TileEntityRendererStatue;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.entity.TileEntityStatue;
import net.minecraft.core.entity.IArmorWearing;
import net.minecraft.core.enums.HumanArmorShape;
import net.minecraft.core.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Mixin(TileEntityRendererStatue.class)
public abstract class TileEntityRendererStatueMixin {

	@Unique
	private TileEntityStatue bwg$currentStatue;

	@Inject(method = "renderAt", at = @At("HEAD"))
	private void bwg$captureStatue(TessellatorGeneral tessellator, TileEntityStatue statue, ItemStack heldStack, int meta,
	                               Block<?> lowerBlock, TileEntityStatue.Pose pose, IArmorWearing<HumanArmorShape> armorWearer,
	                               double x, double y, double z, CallbackInfo ci) {
		this.bwg$currentStatue = statue;
	}

	@Inject(method = "renderModel", at = @At("HEAD"))
	private void bwg$applyStatueBust(TessellatorGeneral tessellator, StaticEntityModel model, TileEntityStatue.Pose pose, CallbackInfo ci) {
		if (this.bwg$currentStatue instanceof StatueGenderData data) {
			MammaryModel.applyStatue(model, data);
		}
	}
}
