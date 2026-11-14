package imbacrian.betterwithgenders.mixin;

import com.mojang.logging.LogUtils;
import imbacrian.bapilib.util.EntitySyncHelper;
import imbacrian.betterwithgenders.api.GenderData;
import imbacrian.betterwithgenders.network.BwgPackets;
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.core.net.packet.PacketEntityTagData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.PlayerList;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	@Unique
	private static final Logger BWG_LOGGER = LogUtils.getLogger();

	@Inject(method = "playerLoggedIn", at = @At("TAIL"))
	private void bwg$syncOnJoin(PlayerServer player, CallbackInfo ci) {
		PlayerList self = (PlayerList) (Object) this;

		for (PlayerServer other : self.playerEntities) {
			if (other == player) continue;

			player.playerNetServerHandler.sendPacket(new PacketEntityTagData(other));
			other.playerNetServerHandler.sendPacket(new PacketEntityTagData(player));
		}

		bwg$sendSelfSync(player);
	}

	@Inject(method = "recreatePlayerEntity", at = @At("RETURN"))
	private void bwg$keepDataOnRespawn(PlayerServer previousPlayer, int dimension, CallbackInfoReturnable<PlayerServer> cir) {
		PlayerServer next = cir.getReturnValue();
		if (next == null || next == previousPlayer) return;
		if (previousPlayer instanceof GenderData from && next instanceof GenderData to) {
			to.bwg$copyPersistentFrom(from);
			// The tracker already spawned the new entity for nearby players before we could copy anything.
			EntitySyncHelper.syncEntityData(MinecraftServer.getInstance(), next);
			bwg$sendSelfSync(next);
		}
	}

	@Unique
	private static void bwg$sendSelfSync(PlayerServer player) {
		if (!(player instanceof GenderData data)) return;
		try {
			player.playerNetServerHandler.sendPacket(
				new PacketCustomPayload(BwgPackets.CHANNEL_SELF_SYNC, BwgPackets.encodeSelfSync(data)));
		} catch (IOException e) {
			BWG_LOGGER.warn("BWG: could not send self sync to {}", player.username, e);
		}
	}
}
