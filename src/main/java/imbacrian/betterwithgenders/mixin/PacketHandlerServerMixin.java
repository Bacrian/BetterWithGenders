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

@Mixin(targets = "net.minecraft.server.net.handler.PacketHandlerServer")
public class PacketHandlerServerMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow
    private net.minecraft.server.MinecraftServer mcServer;

    @Shadow
    private net.minecraft.server.entity.player.PlayerServer playerEntity;

    @Inject(method = "handleCustomPayload", at = @At("TAIL"))
    private void onHandleCustom(PacketCustomPayload packetCustomPayload, CallbackInfo ci) {
        try {
            if (packetCustomPayload != null && "BWG:Gender".equals(packetCustomPayload.channel)) {
                try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packetCustomPayload.data))) {
                    int entityId = dis.readInt();
                    String genderName = dis.readUTF();
                    float breastSize = dis.readFloat();
					float breastSeparation = dis.readFloat();
					float jiggleDirection = dis.readFloat();
                    LOGGER.info("BWG: Server processed BWG:Gender payload entity={} gender={} breastSize={}, breastSeparation={}, jiggleDirection={}", entityId, genderName, breastSize, breastSeparation, jiggleDirection);

                    // Apply to server-side player entity if possible
                    try {
                        if (this.playerEntity instanceof GenderData gd) {
                            try {
                                gd.bwg$setGender(Gender.valueOf(genderName));
                            } catch (IllegalArgumentException iae) {
                                LOGGER.warn("BWG: Invalid gender '{}' from client", genderName);
                            }
                            gd.bwg$setBreastSize(breastSize);
							gd.bwg$setBreastSeparation(breastSeparation);
							gd.bwg$setJiggleDirection(jiggleDirection);

                            // mark dirty so tracking uses updated data
                            net.minecraft.core.entity.Entity e = (net.minecraft.core.entity.Entity) this.playerEntity;
                            e.additionalDataChanged = true;
                            e.sendAdditionalData = true;
                        }
                    } catch (Throwable t) {
                        LOGGER.warn("BWG: failed to apply GenderData to server playerEntity", t);
                    }

                    // Broadcast entity NBT to tracked players so clients receive updated BWG_* tags
                    try {
                        if (this.mcServer != null && this.playerEntity != null) {
                            // Build tag snapshot and log BWG keys before sending
                            com.mojang.nbt.tags.CompoundTag outTag = new com.mojang.nbt.tags.CompoundTag();
                            try {
                                ((net.minecraft.core.entity.Entity)this.playerEntity).addAdditionalSaveData(outTag);
                            } catch (Throwable _t) {
                                // fallback: create PacketEntityTagData instance
                                try {
                                    outTag = new net.minecraft.core.net.packet.PacketEntityTagData(this.playerEntity).tag;
                                } catch (Throwable __t) {
                                    LOGGER.debug("BWG: unable to build tag snapshot for logging", __t);
                                }
                            }

                            if (outTag != null) {
                                if (outTag.containsKey("BWG_Gender")) {
                                    String g = outTag.getString("BWG_Gender");
                                    float b = outTag.containsKey("BWG_BreastSize") ? outTag.getFloat("BWG_BreastSize") : -1.0F;
									float bs = outTag.containsKey("BWG_BreastSeparation") ? outTag.getFloat("BWG_BreastSeparation") : -1.0F;
                            LOGGER.info("BWG: broadcasting EntityTagData for entity {} (gender={} breastSize={} breastSeparation={} jiggleDirection={} jiggleAmount={}) to tracked players",
                                this.playerEntity.id, g, b ,bs,
                                outTag.containsKey("BWG_JiggleDirection") ? outTag.getFloat("BWG_JiggleDirection") : -1.0F,
                                outTag.containsKey("BWG_JiggleAmount") ? outTag.getFloat("BWG_JiggleAmount") : -1.0F);
                                } else {
                                    LOGGER.info("BWG: broadcasting EntityTagData for entity {} (no BWG_* keys) to tracked players", this.playerEntity.id);
                                }
                            } else {
                                LOGGER.info("BWG: broadcasting EntityTagData for entity {} (tag null) to tracked players", this.playerEntity.id);
                            }

                            this.mcServer.getEntityTracker(this.playerEntity.dimension).sendPacketToTrackedPlayers(
                                this.playerEntity, new net.minecraft.core.net.packet.PacketEntityTagData(this.playerEntity)
                            );
                        }
                    } catch (Throwable t) {
                        LOGGER.warn("BWG: failed to broadcast EntityTagData", t);
                    }
                } catch (IOException e) {
                    LOGGER.warn("BWG: Failed to parse BWG:Gender payload", e);
                }
            } else if (packetCustomPayload != null && "BWG:Jiggle".equals(packetCustomPayload.channel)) {
                try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packetCustomPayload.data))) {
                    int entityId = dis.readInt();
                    float jiggleDirection = dis.readFloat();
                    float jiggleAmount = dis.readFloat();
                    boolean jiggleEnabled = dis.readBoolean();
                    boolean jiggleWithArmor = dis.readBoolean();
                    boolean individualPhysics = dis.readBoolean();
                    LOGGER.info("BWG: Server processed BWG:Jiggle payload entity={} jiggleDirection={} jiggleAmount={} jiggleEnabled={} jiggleWithArmor={} individualPhysics={}",
                        entityId, jiggleDirection, jiggleAmount, jiggleEnabled, jiggleWithArmor, individualPhysics);

                    // Apply to server-side player entity if possible
                    try {
                        if (this.playerEntity instanceof GenderData gd) {
                            gd.bwg$setJiggleDirection(jiggleDirection);
                            gd.bwg$setJiggleAmount(jiggleAmount);
                            gd.bwg$setJiggleEnabled(jiggleEnabled);
                            gd.bwg$setJiggleWithArmor(jiggleWithArmor);
                            gd.bwg$setIndividualPhysics(individualPhysics);

                            // mark dirty so tracking uses updated data
                            net.minecraft.core.entity.Entity e = (net.minecraft.core.entity.Entity) this.playerEntity;
                            e.additionalDataChanged = true;
                            e.sendAdditionalData = true;
                        }
                    } catch (Throwable t) {
                        LOGGER.warn("BWG: failed to apply JiggleData to server playerEntity", t);
                    }

                    // Broadcast entity NBT to tracked players so clients receive updated BWG_* tags
                    try {
                        if (this.mcServer != null && this.playerEntity != null) {
                            this.mcServer.getEntityTracker(this.playerEntity.dimension).sendPacketToTrackedPlayers(
                                this.playerEntity, new net.minecraft.core.net.packet.PacketEntityTagData(this.playerEntity)
                            );
                        }
                    } catch (Throwable t) {
                        LOGGER.warn("BWG: failed to broadcast EntityTagData for jiggle", t);
                    }
                } catch (IOException e) {
                    LOGGER.warn("BWG: Failed to parse BWG:Jiggle payload", e);
                }
            }
        } catch (Throwable t) {
            LOGGER.warn("BWG: Exception in PacketHandlerServerMixin.onHandleCustom", t);
        }
    }
}
