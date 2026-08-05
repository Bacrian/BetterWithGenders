package imbacrian.betterwithgenders.mixin;

import com.mojang.logging.LogUtils;
import imbacrian.betterwithgenders.gui.GuiStatueGenderMenu;
import imbacrian.betterwithgenders.network.ServerModStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.core.block.entity.TileEntityStatue;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.net.packet.PacketEntityTagData;
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.core.world.pos.TilePos;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

@Mixin(targets = "net.minecraft.client.net.handler.PacketHandlerClient")
public class PacketHandlerClientMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow
    protected Entity getEntityByID(int i) {
        throw new UnsupportedOperationException();
    }

    @Inject(method = "handleCustomPayload", at = @At("TAIL"), remap = false)
    private void onHandleCustomPayload(PacketCustomPayload packetCustomPayload, CallbackInfo ci) {
        if (packetCustomPayload != null && (packetCustomPayload.channel.startsWith("BWG:"))) {
            ServerModStatus.setServerHasMod(true);
            LOGGER.info("BWG: Server has Better With Genders mod installed");

            // Handle open statue gender menu packet
            if ("BWG:OpenStatueGenderMenu".equals(packetCustomPayload.channel)) {
                try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packetCustomPayload.data))) {
                    int x = dis.readInt();
                    int y = dis.readInt();
                    int z = dis.readInt();

                    LOGGER.info("BWG: Received open statue menu packet for {},{},{}", x, y, z);

                    Minecraft mc = Minecraft.getMinecraft();
                    if (mc.currentWorld != null) {
						TilePos pos = new TilePos(x, y, z);
                        var tileEntity = mc.currentWorld.getTileEntity(pos);
                        if (tileEntity instanceof TileEntityStatue statueEntity) {
                            LOGGER.info("BWG: Opening statue gender menu");
                            mc.displayScreen(new GuiStatueGenderMenu(statueEntity, new net.minecraft.core.world.pos.TilePos(x, y, z)));
                        } else {
                            LOGGER.warn("BWG: Tile entity at {},{},{} is not a statue", x, y, z);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("BWG: Failed to parse BWG:OpenStatueGenderMenu payload", e);
                }
            }
        }
    }

    @Inject(method = "handleEntityTagData", at = @At("TAIL"))
    private void onHandleEntityTagData(PacketEntityTagData packetEntityTagData, CallbackInfo ci) {
        try {
            Entity entity = this.getEntityByID(packetEntityTagData.entityId);
            if (entity == null) {
                LOGGER.debug("BWG: handleEntityTagData - entity {} not found on client", packetEntityTagData.entityId);
                return;
            }

            if (packetEntityTagData.tag != null && packetEntityTagData.tag.containsKey("BWG_Gender")) {
                String g = packetEntityTagData.tag.getString("BWG_Gender");
                float b = packetEntityTagData.tag.containsKey("BWG_BreastSize") ? packetEntityTagData.tag.getFloat("BWG_BreastSize") : -1.0F;
				float bs = packetEntityTagData.tag.containsKey("BWG_BreastSeparation") ? packetEntityTagData.tag.getFloat("BWG_BreastSeparation") : -1.0F;
                float jd = packetEntityTagData.tag.containsKey("BWG_JiggleDirection") ? packetEntityTagData.tag.getFloat("BWG_JiggleDirection") : -1.0F;
                float ja = packetEntityTagData.tag.containsKey("BWG_JiggleAmount") ? packetEntityTagData.tag.getFloat("BWG_JiggleAmount") : -1.0F;
                LOGGER.info("BWG: Client received BWG NBT for entity {} gender={} breastSize={} breastSeparation={} jiggleDirection={} jiggleAmount={}",
                    packetEntityTagData.entityId, g, b, bs, jd, ja);

                // Apply NBT to the entity (calls readAdditionalSaveData) so mixin fields update
                try {
                    java.lang.reflect.Method read = entity.getClass().getMethod("readAdditionalSaveData", packetEntityTagData.tag.getClass());
                    if (read != null) {
                        read.setAccessible(true);
                        read.invoke(entity, packetEntityTagData.tag);
                        LOGGER.info("BWG: Applied NBT to entity {} via readAdditionalSaveData", packetEntityTagData.entityId);
                    }
                } catch (Exception ex) {
                    try {
                        java.lang.reflect.Method read2 = entity.getClass().getDeclaredMethod("readAdditionalSaveData", packetEntityTagData.tag.getClass());
                        read2.setAccessible(true);
                        read2.invoke(entity, packetEntityTagData.tag);
                        LOGGER.info("BWG: Applied NBT to entity {} via declared readAdditionalSaveData", packetEntityTagData.entityId);
                    } catch (Exception e) {
                        LOGGER.debug("BWG: readAdditionalSaveData failed for entity {}: {}", packetEntityTagData.entityId, e.toString());
                    }
                }

            } else {
                LOGGER.info("BWG: Client received EntityTagData for entity {} without BWG_Gender", packetEntityTagData.entityId);
            }

            // Try to invoke protected setupScale() to refresh entity size dependent state
            try {
                java.lang.reflect.Method m = entity.getClass().getMethod("setupScale");
                if (m != null) {
                    m.setAccessible(true);
                    m.invoke(entity);
                    LOGGER.info("BWG: setupScale invoked for entity {}", packetEntityTagData.entityId);
                }
            } catch (NoSuchMethodException e) {
                // fallback: try declared method on superclass chain
                try {
                    java.lang.reflect.Method m2 = entity.getClass().getDeclaredMethod("setupScale");
                    m2.setAccessible(true);
                    m2.invoke(entity);
                    LOGGER.info("BWG: setupScale invoked via declared method for entity {}", packetEntityTagData.entityId);
                } catch (Exception ex) {
                    LOGGER.debug("BWG: setupScale reflection failed for entity {}: {}", packetEntityTagData.entityId, ex.toString());
                }
            } catch (Exception ex) {
                LOGGER.debug("BWG: error invoking setupScale for entity {}: {}", packetEntityTagData.entityId, ex.toString());
            }

            // Try to refresh dimensions (recalculate bounding box/size dependent state)
            try {
                java.lang.reflect.Method rd = entity.getClass().getMethod("refreshDimensions");
                rd.setAccessible(true);
                rd.invoke(entity);
                LOGGER.info("BWG: refreshDimensions invoked for entity {}", packetEntityTagData.entityId);
            } catch (Exception ex) {
                LOGGER.debug("BWG: refreshDimensions failed for entity {}: {}", packetEntityTagData.entityId, ex.toString());
            }

            LOGGER.debug("BWG: client re-render attempts complete for entity {}", packetEntityTagData.entityId);
        } catch (Exception e) {
            LOGGER.warn("BWG: Exception in PacketHandlerClientMixin.handleEntityTagData", e);
        }
    }
}
