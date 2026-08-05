package imbacrian.betterwithgenders.mixin;

import com.mojang.logging.LogUtils;
import imbacrian.betterwithgenders.gui.GuiStatueGenderMenu;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicStatue;
import net.minecraft.core.block.entity.TileEntityStatue;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemPaintBrush;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.slf4j.Logger;

@Mixin(ItemPaintBrush.class)
public class ItemPaintBrushMixin {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Inject(method = "onUseOnBlock", at = @At("HEAD"), cancellable = true, remap = false)
	private void bwg$onUseOnBlock(ItemStack selfStack, World world, Player player, TilePosc blockPos, Side side, double xHit, double yHit, CallbackInfoReturnable<Boolean> cir) {
		if (player != null && player.isSneaking()) {
			Block<?> block = world.getBlockType(blockPos);

			if (block != null && block.getLogic() instanceof BlockLogicStatue statueLogic) {
				TileEntityStatue statueEntity = statueLogic.getTileEntity(world, blockPos);

				if (statueEntity != null) {
						net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
						mc.displayScreen(new GuiStatueGenderMenu(statueEntity, new TilePos(blockPos)));
					cir.setReturnValue(true);
				}
			}
		}
	}
}
