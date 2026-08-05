package imbacrian.betterwithgenders.mixin;

import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.PlayerList;
import net.minecraft.core.net.packet.PacketEntityTagData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

	@Inject(method = "playerLoggedIn", at = @At("TAIL"))
	private void bwg$syncOnJoin(PlayerServer player, CallbackInfo ci) {
		PlayerList self = (PlayerList) (Object) this;

		for (PlayerServer other : self.playerEntities) {
			if (other == player) continue;

			player.playerNetServerHandler.sendPacket(new PacketEntityTagData(other));
			other.playerNetServerHandler.sendPacket(new PacketEntityTagData(player));
		}
	}
}
