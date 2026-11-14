package imbacrian.betterwithgenders.mixin;

import com.mojang.logging.LogUtils;
import imbacrian.betterwithgenders.client.BwgClientProfile;
import imbacrian.betterwithgenders.gui.GuiStatueGenderMenu;
import imbacrian.betterwithgenders.network.BwgPackets;
import imbacrian.betterwithgenders.network.ServerModStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.core.block.entity.TileEntityStatue;
import net.minecraft.core.net.packet.PacketCustomPayload;
import net.minecraft.core.world.pos.TilePos;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

@Mixin(targets = "net.minecraft.client.net.handler.PacketHandlerClient")
public class PacketHandlerClientMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "handleCustomPayload", at = @At("TAIL"), remap = false)
    private void onHandleCustomPayload(PacketCustomPayload packetCustomPayload, CallbackInfo ci) {
        if (packetCustomPayload == null || packetCustomPayload.channel == null || !packetCustomPayload.channel.startsWith("BWG:")) {
            return;
        }

        if (!ServerModStatus.doesServerHaveMod()) {
            ServerModStatus.setServerHasMod(true);
            LOGGER.info("BWG: Server has Better With Genders mod installed");
        }

        Minecraft mc = Minecraft.getMinecraft();

        // Server's stored settings for our own player (login / respawn).
        if (BwgPackets.CHANNEL_SELF_SYNC.equals(packetCustomPayload.channel)) {
            if (packetCustomPayload.data != null) {
                BwgClientProfile.onSelfSync(mc, packetCustomPayload.data);
            }
            return;
        }

        // Handle open statue gender menu packet
        if ("BWG:OpenStatueGenderMenu".equals(packetCustomPayload.channel)) {
            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packetCustomPayload.data))) {
                int x = dis.readInt();
                int y = dis.readInt();
                int z = dis.readInt();

                if (mc.currentWorld != null) {
                    TilePos pos = new TilePos(x, y, z);
                    var tileEntity = mc.currentWorld.getTileEntity(pos);
                    if (tileEntity instanceof TileEntityStatue statueEntity) {
                        mc.displayScreen(new GuiStatueGenderMenu(statueEntity, new TilePos(x, y, z)));
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
