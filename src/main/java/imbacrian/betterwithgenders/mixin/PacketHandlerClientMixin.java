package imbacrian.betterwithgenders.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.net.packet.PacketEntityTagData;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.net.handler.PacketHandlerClient")
public class PacketHandlerClientMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow
    protected Entity getEntityByID(int i) {
        throw new UnsupportedOperationException();
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
                float jd = packetEntityTagData.tag.containsKey("BWG_JiggleDirection") ? packetEntityTagData.tag.getFloat("BWG_JiggleDirection") : -1.0F;
                float ja = packetEntityTagData.tag.containsKey("BWG_JiggleAmount") ? packetEntityTagData.tag.getFloat("BWG_JiggleAmount") : -1.0F;
                LOGGER.info("BWG: Client received BWG NBT for entity {} gender={} breastSize={} jiggleDirection={} jiggleAmount={}", 
                    packetEntityTagData.entityId, g, b, jd, ja);

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
