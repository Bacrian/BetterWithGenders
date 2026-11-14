package imbacrian.betterwithgenders.physics;

public final class BreastPhysics {

	// ---- spring ----
	private static final float STIFFNESS = 0.26F; // Pull back to rest, per tick^2. Natural period is about 2*pi/sqrt(STIFFNESS) ticks.
	// Fraction of velocity lost each tick.
	private static final float DAMPING = 0.12F;
	private static final float LIMIT = 1.5F;

	// ---- input gains ----
	/** Push per block/tick^2 of vertical acceleration (jumping, landing, stepping up) */
	private static final float GAIN_VERTICAL = 1.6F;
	/** Push per block/tick^2 of acceleration along the facing direction (starting/stopping to run) */
	private static final float GAIN_FORWARD = 2.5F;
	/** Push from the stride animation, scaled by walk animation speed (alternates between breasts) */
	private static final float GAIN_STEP = 0.10F;
	/** Push per degree turned this tick (turning right pushes the breasts toward the player's left) */
	private static final float GAIN_TURN = 0.008F;
	/** One-off push when crouching / standing up */
	private static final float SNEAK_KICK = 0.5F;
	private static final float PUSH_LIMIT = 1.2F;
	/** Cycle of the walk animation: cos(walkAnimPos * STRIDE) */
	private static final float STRIDE = 0.6662F;

	// ---- idle breathing ----
	private static final float BREATH_AMPLITUDE = 0.05F;
	private static final float BREATH_SPEED = 0.09F;

	// Moving farther than this in one tick (squared, in blocks)
	// is a teleport: restart instead of reacting.
	private static final double TELEPORT_DIST_SQ = 16.0;

	private static final class Spring {
		float pos, vel, prev;

		void step(float push) {
			prev = pos;
			vel = (vel + push - STIFFNESS * pos) * (1.0F - DAMPING);
			pos += vel;
			if (pos > LIMIT) { pos = LIMIT; vel = 0; }
			else if (pos < -LIMIT) { pos = -LIMIT; vel = 0; }
		}

		void clear() { pos = vel = prev = 0; }

		float at(float partialTick) { return prev + (pos - prev) * partialTick; }
	}

	private final Spring bounceL = new Spring(), bounceR = new Spring();
	private final Spring swingL = new Spring(), swingR = new Spring();

	private boolean initialized;
	private double lastX, lastY, lastZ;
	private float lastYaw, lastDy, lastForward;
	private boolean wasSneaking;
	private float breath, prevBreath;

	public void update(double x, double y, double z, float bodyYaw, float walkPos, float walkSpeed,
	                   boolean sneaking, int tick, boolean individual) {
		prevBreath = breath;
		breath = BREATH_AMPLITUDE * (float) Math.sin(tick * BREATH_SPEED);

		if (!initialized) {
			snapshot(x, y, z, bodyYaw, 0.0F, 0.0F);
			wasSneaking = sneaking;
			initialized = true;
			return;
		}

		double dx = x - lastX, dy = y - lastY, dz = z - lastZ;
		if (dx * dx + dy * dy + dz * dz > TELEPORT_DIST_SQ) {
			reset();
			snapshot(x, y, z, bodyYaw, 0.0F, 0.0F);
			wasSneaking = sneaking;
			initialized = true;
			return;
		}

		double yaw = Math.toRadians(bodyYaw);
		float forward = (float) (-Math.sin(yaw) * dx + Math.cos(yaw) * dz);
		float turn = wrapDegrees(bodyYaw - lastYaw);
		float verticalAccel = (float) dy - lastDy;
		float forwardAccel = forward - lastForward;
		snapshot(x, y, z, bodyYaw, (float) dy, forward);

		float vertical = clamp(-verticalAccel * GAIN_VERTICAL - forwardAccel * GAIN_FORWARD, -PUSH_LIMIT, PUSH_LIMIT);
		if (sneaking != wasSneaking) {
			wasSneaking = sneaking;
			vertical += SNEAK_KICK;
		}
		float step = GAIN_STEP * clamp(walkSpeed, 0.0F, 1.0F) * (float) Math.cos(walkPos * STRIDE);
		float lateral = clamp(turn * GAIN_TURN, -PUSH_LIMIT, PUSH_LIMIT);

		bounceL.step(vertical + step);
		bounceR.step(vertical + (individual ? -step : step));
		swingL.step(lateral);
		swingR.step(lateral);
	}

	// Manual push, e.g. so the settings menu can show the effect of a slider.
	public void kick(float strength) {
		bounceL.vel += strength;
		bounceR.vel += strength;
		swingL.vel += strength;
		swingR.vel += strength;
	}

	// Forget everything (player stopped being a female/other, jiggle disabled, teleport, etc)
	public void reset() {
		bounceL.clear(); bounceR.clear(); swingL.clear(); swingR.clear();
		breath = prevBreath = 0;
		lastDy = lastForward = 0;
		initialized = false; // next update() takes a fresh snapshot instead of reacting to a stale one
	}

	// Vertical bounce (positive = up), including idle breathing.
	public float bounce(boolean left, float partialTick) {
		float spring = (left ? bounceL : bounceR).at(partialTick);
		return spring + prevBreath + (breath - prevBreath) * partialTick;
	}

	// Sideways displacement from turning. Same direction for both breasts.
	public float swing(boolean left, float partialTick) {
		return (left ? swingL : swingR).at(partialTick);
	}

	private void snapshot(double x, double y, double z, float yaw, float dy, float forward) {
		lastX = x; lastY = y; lastZ = z;
		lastYaw = yaw;
		lastDy = dy;
		lastForward = forward;
	}

	private static float clamp(float v, float min, float max) {
		return v < min ? min : (v > max ? max : v);
	}

	private static float wrapDegrees(float deg) {
		deg %= 360.0F;
		if (deg >= 180.0F) deg -= 360.0F;
		if (deg < -180.0F) deg += 360.0F;
		return deg;
	}
}
