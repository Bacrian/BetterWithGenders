package imbacrian.betterwithgenders.mixin;

import com.mojang.nbt.tags.CompoundTag;
import imbacrian.betterwithgenders.api.Gender;
import imbacrian.betterwithgenders.api.StatueGenderData;
import net.minecraft.core.block.entity.TileEntityStatue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityStatue.class)
public class TileEntityStatueMixin implements StatueGenderData {
    @Unique
    private Gender bwg$gender = Gender.MALE;
    @Unique
    private float bwg$breastSize = 0.0F;

    @Override
    public Gender bwg$getGender() {
        return bwg$gender;
    }

    @Override
    public void bwg$setGender(Gender gender) {
        bwg$gender = gender;
    }

    @Override
    public float bwg$getBreastSize() {
        return bwg$breastSize;
    }

    @Override
    public void bwg$setBreastSize(float size) {
        bwg$breastSize = size;
    }

    @Inject(method = "writeAdditionalData", at = @At("TAIL"))
    private void bwg$writeAdditionalData(CompoundTag compoundTag, CallbackInfo ci) {
        compoundTag.putString("BWG_Gender", bwg$gender.name());
        compoundTag.putFloat("BWG_BreastSize", bwg$breastSize);
    }

    @Inject(method = "readAdditionalData", at = @At("TAIL"))
    private void bwg$readAdditionalData(CompoundTag compoundTag, CallbackInfo ci) {
        if (compoundTag.containsKey("BWG_Gender")) {
            try {
                bwg$gender = Gender.valueOf(compoundTag.getString("BWG_Gender"));
            } catch (IllegalArgumentException e) {
                bwg$gender = Gender.FEMALE;
            }
        }
        if (compoundTag.containsKey("BWG_BreastSize")) {
            bwg$breastSize = compoundTag.getFloat("BWG_BreastSize");
        }
    }
}
