package imbacrian.betterwithgenders.api;

import imbacrian.betterwithgenders.physics.BreastPhysics;

public interface GenderData {
	Gender bwg$getGender();
	void bwg$setGender(Gender gender);

	float bwg$getBreastSize();
	void bwg$setBreastSize(float size);

	boolean bwg$isJiggleEnabled();
	void bwg$setJiggleEnabled(boolean value);

	float bwg$getJiggleAmount();
	void bwg$setJiggleAmount(float value);

	float bwg$getJiggleDirection();
	void bwg$setJiggleDirection(float angle);

	float bwg$getBreastSeparation();
	void bwg$setBreastSeparation(float separation);

	boolean bwg$isJiggleWithArmor();
	void bwg$setJiggleWithArmor(boolean value);

	boolean bwg$isIndividualPhysics();
	void bwg$setIndividualPhysics(boolean value);

	/* Client-side animation state of this player's breasts. Never sent over the network. */
	BreastPhysics bwg$getPhysics();

	/* Copies every persistent setting from another holder except physics */
	default void bwg$copyPersistentFrom(GenderData other) {
		if (other == null || other == this) return;
		this.bwg$setGender(other.bwg$getGender());
		this.bwg$setBreastSize(other.bwg$getBreastSize());
		this.bwg$setBreastSeparation(other.bwg$getBreastSeparation());
		this.bwg$setJiggleDirection(other.bwg$getJiggleDirection());
		this.bwg$setJiggleAmount(other.bwg$getJiggleAmount());
		this.bwg$setJiggleEnabled(other.bwg$isJiggleEnabled());
		this.bwg$setJiggleWithArmor(other.bwg$isJiggleWithArmor());
		this.bwg$setIndividualPhysics(other.bwg$isIndividualPhysics());
	}
}
