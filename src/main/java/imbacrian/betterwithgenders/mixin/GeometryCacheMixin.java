package imbacrian.betterwithgenders.mixin;

import imbacrian.betterwithgenders.BwGenders;
import net.minecraft.client.render.texturepack.TexturePackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.useless.dragonfly.data.entity.mojang.EntityGeometryMojangData;

import java.io.InputStream;

@Mixin(EntityGeometryMojangData.Cache.class)
public abstract class GeometryCacheMixin {

	@Inject(method = "reload", at = @At("TAIL"))
	private static void bwg$forceOwnGeometry(TexturePackList packList, CallbackInfo ci) {
		bwg$loadOwn("/assets/betterwithgenders/models/entity/player/player.geo.json");
		bwg$loadOwn("/assets/betterwithgenders/models/entity/humanoid.geo.json");
	}

	@Unique
	private static void bwg$loadOwn(String path) {
		try (InputStream stream = GeometryCacheMixin.class.getResourceAsStream(path)) {
			if (stream != null) {
				EntityGeometryMojangData.Cache.loadModelsFromResource(stream);
			} else {
				BwGenders.LOGGER.error("Couldn't reload own {} after reload!", path);
			}
		} catch (Exception e) {
			BwGenders.LOGGER.error("Error upon forcing {} load.", path, e);
		}
	}
}
