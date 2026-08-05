package imbacrian.betterwithgenders.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.core.net.packet.PacketCustomPayload;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import imbacrian.betterwithgenders.api.Gender;
import imbacrian.betterwithgenders.api.GenderData;
import imbacrian.betterwithgenders.api.StatueGenderData;

import imbacrian.bapilib.util.EntitySyncHelper;
import imbacrian.bapilib.util.TilePosHelper;

@Mixin(targets = "net.minecraft.server.net.handler.PacketHandlerServer")
public class PacketHandlerServerMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow
    private net.minecraft.server.MinecraftServer mcServer;

    @Shadow
    private net.minecraft.server.entity.player.PlayerServer playerEntity;

    @Inject(method = "handleCustomPayload", at = @At("TAIL"))
    private void onHandleCustom(PacketCustomPayload packetCustomPayload, CallbackInfo ci) {
		if (packetCustomPayload == null) return;

		try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packetCustomPayload.data))) {
			if ("BWG:Gender".equals(packetCustomPayload.channel)) {
				int entityId = dis.readInt();
				String genderName = dis.readUTF();
				float breastSize = dis.readFloat();
				float breastSeparation = dis.readFloat();
				float jiggleDirection = dis.readFloat();

				if (this.playerEntity instanceof GenderData gd) {
					try {
						gd.bwg$setGender(Gender.valueOf(genderName));
					} catch (IllegalArgumentException iae) {
						LOGGER.warn("BWG: Invalid gender '{}' from client", genderName);
					}
					gd.bwg$setBreastSize(breastSize);
					gd.bwg$setBreastSeparation(breastSeparation);
					gd.bwg$setJiggleDirection(jiggleDirection);

					EntitySyncHelper.syncEntityData(this.mcServer, this.playerEntity);
				}
			} else if ("BWG:Jiggle".equals(packetCustomPayload.channel)) {
				int entityId = dis.readInt();
				float jiggleDirection = dis.readFloat();
				float jiggleAmount = dis.readFloat();
				boolean jiggleEnabled = dis.readBoolean();
				boolean jiggleWithArmor = dis.readBoolean();
				boolean individualPhysics = dis.readBoolean();

				if (this.playerEntity instanceof GenderData gd) {
					gd.bwg$setJiggleDirection(jiggleDirection);
					gd.bwg$setJiggleAmount(jiggleAmount);
					gd.bwg$setJiggleEnabled(jiggleEnabled);
					gd.bwg$setJiggleWithArmor(jiggleWithArmor);
					gd.bwg$setIndividualPhysics(individualPhysics);

					EntitySyncHelper.syncEntityData(this.mcServer, this.playerEntity);
				}

			} else if (packetCustomPayload != null && "BWG:StatueGender".equals(packetCustomPayload.channel)) {
				int x = dis.readInt();
				int y = dis.readInt();
				int z = dis.readInt();
				String genderName = dis.readUTF();
				float breastSize = dis.readFloat();
				LOGGER.info("BWG: Server processed BWG:StatueGender payload pos=({},{},{}) gender={} breastSize={}", x, y, z, genderName, breastSize);

				// Apply to server-side tile entity if possible
				try {
					net.minecraft.core.world.World world = this.playerEntity.world;
					net.minecraft.core.world.pos.TilePos pos = new net.minecraft.core.world.pos.TilePos(x, y, z);
					net.minecraft.core.block.entity.TileEntity te = world.getTileEntity(pos);
					if (te instanceof StatueGenderData sgd) {
						try {
							sgd.bwg$setGender(Gender.valueOf(genderName));
							sgd.bwg$setBreastSize(breastSize);
						} catch (IllegalArgumentException iae) {
							LOGGER.warn("BWG: Invalid gender '{}' from client for statue", genderName);
						}
					}
				} catch (Throwable t) {
					LOGGER.warn("BWG: failed to apply StatueGenderData to tile entity", t);
				}
            }
        } catch (Throwable t) {
            LOGGER.warn("BWG: Exception in PacketHandlerServerMixin.onHandleCustom", t);
        }
    }
}
