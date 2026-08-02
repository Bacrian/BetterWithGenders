package imbacrian.betterwithgenders.api;

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

	double bwg$getJiggleTimer();
	void bwg$setJiggleTimer(double value);

	long bwg$getJiggleLastRenderTick();
	void bwg$setJiggleLastRenderTick(long value);

	double bwg$getLastWobble();
	void bwg$setLastWobble(double value);

	boolean bwg$isWasOnGround();
	void bwg$setWasOnGround(boolean value);

	double bwg$getPrevFallSpeed();
	void bwg$setPrevFallSpeed(double value);

	double bwg$getFallImpulse();
	void bwg$setFallImpulse(double value);
}
