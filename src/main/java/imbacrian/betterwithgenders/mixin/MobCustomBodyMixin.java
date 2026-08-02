package imbacrian.betterwithgenders.mixin;

import com.mojang.nbt.tags.CompoundTag;
import imbacrian.betterwithgenders.api.CustomBodyData;
import net.minecraft.core.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobCustomBodyMixin implements CustomBodyData {
	@Unique
	private boolean bwg$hasCustomBust = false;
	@Unique
	private float bwg$customBustSize = 0.5F;

	@Override
	public boolean bwg$hasCustomBust() { return this.bwg$hasCustomBust; }
	@Override
	public void bwg$setHasCustomBust(boolean value) { this.bwg$hasCustomBust = value; }
	@Override
	public float bwg$getCustomBustSize() { return this.bwg$customBustSize; }
	@Override
	public void bwg$setCustomBustSize(float value) { this.bwg$customBustSize = value; }

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void bwg$saveCustomBust(CompoundTag tag, CallbackInfo ci) {
		tag.putBoolean("BWG_HasCustomBust", this.bwg$hasCustomBust);
		tag.putFloat("BWG_CustomBustSize", this.bwg$customBustSize);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void bwg$loadCustomBust(CompoundTag tag, CallbackInfo ci) {
		if (tag.containsKey("BWG_HasCustomBust")) this.bwg$hasCustomBust = tag.getBoolean("BWG_HasCustomBust");
		if (tag.containsKey("BWG_CustomBustSize")) this.bwg$customBustSize = tag.getFloat("BWG_CustomBustSize");
	}
}
