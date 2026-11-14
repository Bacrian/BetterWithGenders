package imbacrian.betterwithgenders.render;

import imbacrian.betterwithgenders.api.Gender;
import imbacrian.betterwithgenders.api.GenderData;
import imbacrian.betterwithgenders.api.StatueGenderData;
import imbacrian.betterwithgenders.client.BwgDebug;
import imbacrian.betterwithgenders.physics.BreastPhysics;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

public class MammaryModel {

	private static final double DEPTH_MIN = 0.5;

	private static final double SURFACE_Z = -2.0;
	//Per layer, as written in the .geo.json
	private static final double[] LAYER_BODY = {-1.6, -2.8};
	private static final double[] LAYER_JACKET = {-1.6, -3.05};
	private static final double[] LAYER_ARMOR = {-1.8, -3.0};

	private static double[] layer(String boneName) {
		if (boneName.startsWith("armor")) return LAYER_ARMOR;
		if (boneName.startsWith("jacket")) return LAYER_JACKET;
		return LAYER_BODY;
	}

	/** Droop at size 1.0 (the free end swings forward by this much). Scales with the size slider */
	private static final double DROOP_MAX = Math.toRadians(22.0);
	/** rotX sign that swings the bottom of the block FORWARD (positive rotX = top forward, like a head pitch) */
	private static final double DROOP_SIGN = -1.0;
	/** Model pixels (1/16 block) moved up/down per unit of spring displacement at jiggle amount 1.0. 0 = none */
	private static final double BOUNCE_PIXELS = 0.0;
	/** Radians of tilt (pitch) per unit of bounce at jiggle amount 1.0 */
	private static final double TILT_PER_BOUNCE = 0.30;
	/** Radians of sideways swing per unit of lateral spring displacement at jiggle amount 1.0 */
	private static final double SWING_ROT_PER_UNIT = 0.10;
	/** Extra length along Y per unit of bounce at jiggle amount 1.0 (0.12 = 12 %) */
	private static final double STRETCH_PER_BOUNCE = 0.18;
	// HARD CEILINGS, approached smoothly (tanh) and never exceeded, whatever the physics produce.
	private static final double MAX_SHIFT_Y = 0.5;

	private static final double MAX_TILT = 0.12;
	private static final double MAX_SWING_ROT = 0.06;
	private static final double MAX_STRETCH = 0.12;
	private static final double SWING_SIGN = 1.0;

	public static void apply(StaticEntityModel model, GenderData data, float partialTick, boolean hasChestplate) {
		boolean shown = data.bwg$getGender() == Gender.FEMALE || data.bwg$getGender() == Gender.OTHER;
		boolean moving = data.bwg$isJiggleEnabled() && (!hasChestplate || data.bwg$isJiggleWithArmor());
		BreastPhysics physics = moving ? data.bwg$getPhysics() : null;

		applyPair(model, data, shown, physics, partialTick, "breastLeft", "breastRight");
		applyPair(model, data, shown, physics, partialTick, "jacketBreastLeft", "jacketBreastRight");
	}

	public static void applyArmor(StaticEntityModel model, GenderData data, float partialTick) {
		boolean shown = data.bwg$getGender() == Gender.FEMALE || data.bwg$getGender() == Gender.OTHER;
		BreastPhysics physics = (data.bwg$isJiggleEnabled() && data.bwg$isJiggleWithArmor()) ? data.bwg$getPhysics() : null;
		applyPair(model, data, shown, physics, partialTick, "armorBreastLeft", "armorBreastRight");
	}

	public static void applyStatic(StaticEntityModel model, boolean shown, float size, String leftName, String rightName) {
		BoneTransform left = model.getTransform(leftName);
		BoneTransform right = model.getTransform(rightName);
		if (left == null || right == null) return;
		left.visible = shown;
		right.visible = shown;
		if (!shown) return;
		left.scaleZ = right.scaleZ = depthScale(size);
		left.posZ = right.posZ = depthAnchorShift(depthScale(size), leftName);
		BoneTransform droopL = model.getTransform(leftName + "Droop");
		BoneTransform droopR = model.getTransform(rightName + "Droop");
		double swing = DROOP_MAX * size;
		double lever = frontLever(depthScale(size), leftName);
		if (droopL != null) {
			droopL.rotX = DROOP_SIGN * swing;
			droopL.posY = -lever * Math.sin(swing);
			droopL.posZ = -lever * (1.0 - Math.cos(swing));
		}
		if (droopR != null) {
			droopR.rotX = DROOP_SIGN * swing;
			droopR.posY = -lever * Math.sin(swing);
			droopR.posZ = -lever * (1.0 - Math.cos(swing));
		}
	}

	public static void applyStatue(StaticEntityModel model, StatueGenderData data) {
		boolean shown = data.bwg$getGender() == Gender.FEMALE || data.bwg$getGender() == Gender.OTHER;
		applyStatic(model, shown, data.bwg$getBreastSize(), "breastLeft", "breastRight");
		applyStatic(model, shown, data.bwg$getBreastSize(), "jacketBreastLeft", "jacketBreastRight");
		applyStatic(model, shown, data.bwg$getBreastSize(), "armorBreastLeft", "armorBreastRight");
	}

	// physics are null when there must be no motion (disabled, or covered by armor that suppresses it) */
	private static void applyPair(StaticEntityModel model, GenderData data, boolean shown, BreastPhysics physics,
	                              float partialTick, String leftName, String rightName) {
		BoneTransform left = model.getTransform(leftName);
		BoneTransform right = model.getTransform(rightName);
		if (left == null || right == null) return;
		left.visible = shown;
		right.visible = shown;
		if (!shown) return;

		double amount = data.bwg$getJiggleAmount();
		double size = data.bwg$getBreastSize();
		double bounceL = 0, bounceR = 0, swingL = 0, swingR = 0;
		if (physics != null) {
			bounceL = physics.bounce(true, partialTick) * amount;
			bounceR = physics.bounce(false, partialTick) * amount;
			swingL = physics.swing(true, partialTick) * amount * SWING_SIGN;
			swingR = physics.swing(false, partialTick) * amount * SWING_SIGN;
		}
		if (!data.bwg$isIndividualPhysics()) { // unified: the left spring drives both
			bounceR = bounceL;
			swingR = swingL;
		}

		double baseRot = data.bwg$isIndividualPhysics() ? Math.toRadians(data.bwg$getJiggleDirection()) : 0.0;
		double separation = data.bwg$isIndividualPhysics() ? data.bwg$getBreastSeparation() * 2.0F : 0.0;

		applySide(model.getTransform(leftName + "Mount"), model.getTransform(leftName + "Droop"), left,
			baseRot, -separation, bounceL, swingL, size, leftName);
		applySide(model.getTransform(rightName + "Mount"), model.getTransform(rightName + "Droop"), right,
			-baseRot, separation, bounceR, swingR, size, rightName);
	}

	private static void applySide(BoneTransform mount, BoneTransform droop, BoneTransform breast, double baseRot,
	                              double baseShift, double bounce, double swing, double size, String boneName) {
		double shiftY = softLimit(bounce * BOUNCE_PIXELS, MAX_SHIFT_Y);
		double tilt = softLimit(bounce * TILT_PER_BOUNCE, MAX_TILT * size);
		// Rotating by +theta around Y moves a point at the front of the chest toward -X, hence the minus.
		double yaw = softLimit(-swing * SWING_ROT_PER_UNIT, MAX_SWING_ROT * size);

		double droopAngle = DROOP_SIGN * DROOP_MAX * size;

		// Block bone: scale only. Squash & stretch hangs from the top edge; the width never grows (the breasts already
		// sit on the torso's outer edge) and the depth never exceeds the size slider.
		double stretchY = 1.0 + softLimit(bounce * STRETCH_PER_BOUNCE, MAX_STRETCH);

		// F8 test poses: fixed, exaggerated values to see where each bone pivots (BwgDebug).
		// You actually should never have one of these activated in regular playthrough
		/* switch (BwgDebug.pose) {
			case 1: tilt = 0; yaw = 0; stretchY = 1.0; break;
			case 2: tilt = 0.5; yaw = 0; stretchY = 1.0; droopAngle = 0; break;
			case 3: tilt = -0.5; yaw = 0; stretchY = 1.0; droopAngle = 0; break;
			case 4: tilt = 0; yaw = 0.5; stretchY = 1.0; droopAngle = 0; break;
			case 5: tilt = 0; yaw = -0.5; stretchY = 1.0; droopAngle = 0; break;
			case 6: tilt = 0; yaw = 0; stretchY = 1.0; droopAngle = 0; break;
			case 7: tilt = 0; yaw = 0; stretchY = 1.5; droopAngle = 0; break;
			case 8: tilt = 0; yaw = 0; stretchY = 1.0; droopAngle = 0.5; break;
			case 9: tilt = 0; yaw = 0; stretchY = 1.0; droopAngle = -0.5; break;
			default: break;
		} */

		double squeezeX = Math.min(1.0, Math.pow(stretchY, -0.25));
		breast.scaleY = stretchY;
		breast.scaleX = squeezeX;
		double depth = depthScale(size);
		breast.scaleZ = Math.min(depth, depth / (stretchY * squeezeX));
		double anchorShift = depthAnchorShift(breast.scaleZ, boneName);

		if ("breastLeft".equals(boneName)) {
			BwgDebug.lastValues = String.format(java.util.Locale.ROOT,
				"size=%.2f droop=%.1fdeg tilt=%.3frad yaw=%.3frad stretchY=%.3f scaleX=%.3f scaleY=%.3f scaleZ=%.3f posZ=%.3f pose=%d",
				size, Math.toDegrees(droopAngle), tilt, yaw, stretchY, breast.scaleX, breast.scaleY, breast.scaleZ, anchorShift, BwgDebug.pose);
		}

		if (mount != null && droop != null) {
			breast.rotX = 0.0;
			breast.rotY = 0.0;
			breast.posX = 0.0;
			breast.posY = 0.0;
			breast.posZ = anchorShift;

			droop.rotX = droopAngle;
			droop.rotY = baseRot;
			droop.posX = baseShift;
			// Keep the top-front corner in place (rotate about it, not about the pivot behind it)
			double forwardSwing = droopAngle / DROOP_SIGN; // forward swing of the bottom of the block, radians
			double lever = frontLever(depth, boneName);
			droop.posY = -lever * Math.sin(forwardSwing);
			droop.posZ = -lever * (1.0 - Math.cos(forwardSwing));

			mount.posY = shiftY;
			mount.rotX = tilt;
			mount.rotY = yaw;
		} else {
			// Old model without the helper bones: everything on the block (can shear when scaled)
			breast.rotX = droopAngle + tilt;
			breast.rotY = baseRot + yaw;
			breast.posX = baseShift;
			breast.posY = shiftY;
			breast.posZ = anchorShift;
		}
	}

	/** Depth scale for a given size slider value: DEPTH_MIN at size 0, 1.0 at size 1 */
	private static double depthScale(double size) {
		return DEPTH_MIN + (1.0 - DEPTH_MIN) * Math.max(0.0, Math.min(1.0, size));
	}

	/** z offset that makes a depth scale about the block's pivot behave like a scale about the chest surface */
	private static double depthAnchorShift(double depthScale, String boneName) {
		return (1.0 - depthScale) * (SURFACE_Z - layer(boneName)[0]);
	}

	private static double frontLever(double depthScale, String boneName) {
		double[] l = layer(boneName);
		return (l[0] - SURFACE_Z) + depthScale * (SURFACE_Z - l[1]);
	}

	private static double softLimit(double value, double max) {
		if (max < 1.0E-6) return 0.0; // size 0: nothing to move
		return max * Math.tanh(value / max);
	}
}
