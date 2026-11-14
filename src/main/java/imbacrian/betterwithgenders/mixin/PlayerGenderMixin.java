package imbacrian.betterwithgenders.mixin;

import com.mojang.nbt.tags.CompoundTag;
import imbacrian.betterwithgenders.api.Gender;
import imbacrian.betterwithgenders.api.GenderData;
import imbacrian.betterwithgenders.physics.BreastPhysics;
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

	// Individual Physics Toggle
	@Unique
	private boolean bwg$individualPhysics = true;
	@Override
	public boolean bwg$isIndividualPhysics() { return this.bwg$individualPhysics; }
	@Override
	public void bwg$setIndividualPhysics(boolean value) { this.bwg$individualPhysics = value; }

	// Animation state. Created lazily so a dedicated server never allocates it.
	@Unique
	private BreastPhysics bwg$physics;
	@Override
	public BreastPhysics bwg$getPhysics() {
		if (this.bwg$physics == null) this.bwg$physics = new BreastPhysics();
		return this.bwg$physics;
	}

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
		// Animation state is deliberately NOT saved/synced: it is simulated locally by every client.
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void bwg$loadGender(CompoundTag tag, CallbackInfo ci) {
		if (tag.containsKey("BWG_Gender")) {
			try {
				this.betterwithgenders$gender = Gender.valueOf(tag.getString("BWG_Gender"));
			} catch (IllegalArgumentException ignored) {
				// Unknown/legacy value in an old save: keep the current gender instead of crashing the load.
			}
			// The tracker only sends the NBT to players that start tracking when this flag is set.
			((net.minecraft.core.entity.Entity) (Object) this).sendAdditionalData = true;
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
	}
}

