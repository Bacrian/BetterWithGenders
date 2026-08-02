package imbacrian.betterwithgenders.mixin;

import com.mojang.nbt.tags.CompoundTag;
import imbacrian.betterwithgenders.api.Gender;
import imbacrian.betterwithgenders.api.GenderData;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerGenderMixin implements GenderData {

	// Breast Handling ~~ ts so funny twin
	//Breast Size
	@Unique
	private float bwg$breastSize = 0.5F;
	@Override
	public float bwg$getBreastSize() {
		return this.bwg$breastSize;
	}
	@Override
	public void bwg$setBreastSize(float size) {
		this.bwg$breastSize = size;
	}

	// Jiggle Direction
	@Unique
	private float bwg$jiggleDirection = 0;
	@Override
	public float bwg$getJiggleDirection() {
		return this.bwg$jiggleDirection;
	}
	@Override
	public void bwg$setJiggleDirection(float angle) {
		this.bwg$jiggleDirection = angle;
	}

	// Breast Separation
	@Unique
	private float bwg$breastSeparation = 0;
	@Override
	public float bwg$getBreastSeparation() {
		return this.bwg$breastSeparation;
	}
	@Override
	public void bwg$setBreastSeparation(float separation) {
		this.bwg$breastSeparation = separation;
	}

	// Jiggle Amount
	@Unique
	private float bwg$jiggleAmount = 0.3F;
	@Override
	public float bwg$getJiggleAmount() {
		return this.bwg$jiggleAmount;
	}
	@Override
	public void bwg$setJiggleAmount(float value) {
		this.bwg$jiggleAmount = value;
	}

	//Jiggling enabled
	@Unique
	private boolean bwg$jiggleEnabled = true;
	@Override
	public boolean bwg$isJiggleEnabled() {
		return this.bwg$jiggleEnabled;
	}
	@Override
	public void bwg$setJiggleEnabled(boolean value) {
		this.bwg$jiggleEnabled = value;
	}

	//Jiggling w/ armor enabled
	@Unique
	private boolean bwg$jiggleWithArmor = false;
	@Override
	public boolean bwg$isJiggleWithArmor() {
		return this.bwg$jiggleWithArmor;
	}
	@Override
	public void bwg$setJiggleWithArmor(boolean value) {
		this.bwg$jiggleWithArmor = value;
	}

	//Jiggle Timer
	@Unique
	private double bwg$jiggleTimer = 0.0;
	@Unique
	private long bwg$jiggleLastRenderTick = 0;
	@Unique
	private double bwg$lastWobble = 0;

	@Override
	public double bwg$getJiggleTimer() { return this.bwg$jiggleTimer; }
	@Override
	public void bwg$setJiggleTimer(double value) { this.bwg$jiggleTimer = value; }

	@Override
	public long bwg$getJiggleLastRenderTick() { return this.bwg$jiggleLastRenderTick; }
	@Override
	public void bwg$setJiggleLastRenderTick(long value) { this.bwg$jiggleLastRenderTick = value; }

	@Override
	public double bwg$getLastWobble() { return this.bwg$lastWobble; }
	@Override
	public void bwg$setLastWobble(double value) { this.bwg$lastWobble = value; }
	//Yes, I'm aware it's long af.

	// Checking for ground
	@Unique
	private boolean bwg$wasOnGround = true;
	@Override
	public boolean bwg$isWasOnGround() { return this.bwg$wasOnGround; }
	@Override
	public void bwg$setWasOnGround(boolean value) { this.bwg$wasOnGround = value; }

	// Fall Speed
	@Unique
	private double bwg$prevFallSpeed = 0.0;
	@Override
	public double bwg$getPrevFallSpeed() { return this.bwg$prevFallSpeed; }
	@Override
	public void bwg$setPrevFallSpeed(double value) { this.bwg$prevFallSpeed = value; }

	// Fall Impulse

	@Unique
	private double bwg$fallImpulse = 0.0;
	@Override
	public double bwg$getFallImpulse() { return this.bwg$fallImpulse; }
	@Override
	public void bwg$setFallImpulse(double value) { this.bwg$fallImpulse = value; }

	// Impulse Tick
	@Unique
	private double bwg$lastImpulseTick = 0.0;

	@Override
	public double bwg$getLastImpulseTick() {
		return this.bwg$lastImpulseTick;
	}
	@Override
	public void bwg$setLastImpulseTick(double value) {
		this.bwg$lastImpulseTick = value;
	}

	// Previous Yaw for turn detection
	@Unique
	private float bwg$prevYaw = 0.0F;
	@Override
	public float bwg$getPrevYaw() { return this.bwg$prevYaw; }
	@Override
	public void bwg$setPrevYaw(float value) { this.bwg$prevYaw = value; }

	// Turn Wobble
	@Unique
	private double bwg$turnWobble = 0.0;
	@Override
	public double bwg$getTurnWobble() { return this.bwg$turnWobble; }
	@Override
	public void bwg$setTurnWobble(double value) { this.bwg$turnWobble = value; }

	// Individual Physics Toggle
	@Unique
	private boolean bwg$individualPhysics = true;
	@Override
	public boolean bwg$isIndividualPhysics() { return this.bwg$individualPhysics; }
	@Override
	public void bwg$setIndividualPhysics(boolean value) { this.bwg$individualPhysics = value; }

	// Wobble handlers

	@Unique
	private double bwg$lastImpactWobble = 0.0;
	@Unique
	private double bwg$lastTurnWobble = 0.0;

	@Override
	public double bwg$getLastImpactWobble() { return this.bwg$lastImpactWobble; }
	@Override
	public void bwg$setLastImpactWobble(double value) { this.bwg$lastImpactWobble = value; }

	@Override
	public double bwg$getLastTurnWobble() { return this.bwg$lastTurnWobble; }
	@Override
	public void bwg$setLastTurnWobble(double value) { this.bwg$lastTurnWobble = value; }

	//Gender handling
	@Unique
	private Gender betterwithgenders$gender = Gender.MALE;

	@Override
	public Gender bwg$getGender() {
		return this.betterwithgenders$gender;
	}

	@Override
	public void bwg$setGender(Gender g) {
		this.betterwithgenders$gender = g;
		// mark entity additional data dirty so server-side tracking can send NBT updates
		net.minecraft.core.entity.Entity entity = (net.minecraft.core.entity.Entity)(Object)this;
		entity.additionalDataChanged = true;
		entity.sendAdditionalData = true;
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void bwg$saveGender(CompoundTag tag, CallbackInfo ci) {
		tag.putString("BWG_Gender", this.betterwithgenders$gender.name());
		tag.putFloat("BWG_BreastSize", this.bwg$breastSize);
		tag.putFloat("BWG_BreastSeparation", this.bwg$breastSeparation);
		tag.putFloat("BWG_JiggleDirection", this.bwg$jiggleDirection);
		tag.putFloat("BWG_JiggleAmount", this.bwg$jiggleAmount);
		tag.putBoolean("BWG_JiggleEnabled", this.bwg$jiggleEnabled);
		tag.putBoolean("BWG_JiggleWithArmor", this.bwg$jiggleWithArmor);
		tag.putBoolean("BWG_IndividualPhysics", this.bwg$individualPhysics);
		tag.putDouble("BWG_JiggleTimer", this.bwg$jiggleTimer);
		tag.putDouble("BWG_FallImpulse", this.bwg$fallImpulse);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void bwg$loadGender(CompoundTag tag, CallbackInfo ci) {
		if (tag.containsKey("BWG_Gender")) {
			this.betterwithgenders$gender = Gender.valueOf(tag.getString("BWG_Gender"));
		}
		if (tag.containsKey("BWG_BreastSize")) {
			this.bwg$breastSize = tag.getFloat("BWG_BreastSize");
		}
		if (tag.containsKey("BWG_BreastSeparation")) {
			this.bwg$breastSeparation = tag.getFloat("BWG_BreastSeparation");
		}
		if (tag.containsKey("BWG_JiggleDirection")) {
			this.bwg$jiggleDirection = tag.getFloat("BWG_JiggleDirection");
		}
		if (tag.containsKey("BWG_JiggleAmount")) {
			this.bwg$jiggleAmount = tag.getFloat("BWG_JiggleAmount");
		}
		if (tag.containsKey("BWG_JiggleEnabled")) {
			this.bwg$jiggleEnabled = tag.getBoolean("BWG_JiggleEnabled");
		}
		if (tag.containsKey("BWG_JiggleWithArmor")) {
			this.bwg$jiggleWithArmor = tag.getBoolean("BWG_JiggleWithArmor");
		}
		if (tag.containsKey("BWG_IndividualPhysics")) {
			this.bwg$individualPhysics = tag.getBoolean("BWG_IndividualPhysics");
		}
		if (tag.containsKey("BWG_JiggleTimer")) {
			this.bwg$jiggleTimer = tag.getDouble("BWG_JiggleTimer");
		}
		if (tag.containsKey("BWG_FallImpulse")) {
			this.bwg$fallImpulse = tag.getDouble("BWG_FallImpulse");
		}
	}
}

