package imbacrian.betterwithgenders.render;

import imbacrian.betterwithgenders.api.Gender;
import imbacrian.betterwithgenders.api.GenderData;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

public class MammaryModel {

	public static void apply(StaticEntityModel model, GenderData data, double walkWobble, double impactWobble, double turnWobble, boolean hasChestplate) {
		boolean shown = data.bwg$getGender() == Gender.FEMALE || data.bwg$getGender() == Gender.OTHER;
		boolean suppressMotion = hasChestplate && !data.bwg$isJiggleWithArmor();

		double jiggleY = (data.bwg$isJiggleEnabled() && !suppressMotion) ? (walkWobble + turnWobble) * data.bwg$getJiggleAmount() : 0.0;
		double jiggleX = (data.bwg$isJiggleEnabled() && !suppressMotion) ? impactWobble * data.bwg$getJiggleAmount() : 0.0;

		applyPair(model, data, shown, jiggleY, jiggleX, "breastLeft", "breastRight");
		applyPair(model, data, shown, jiggleY, jiggleX, "jacketBreastLeft", "jacketBreastRight");
	}

	public static void applyArmor(StaticEntityModel model, GenderData data, double walkWobble, double impactWobble, double turnWobble) {
		boolean shown = data.bwg$getGender() == Gender.FEMALE || data.bwg$getGender() == Gender.OTHER;
		double jiggleY = (data.bwg$isJiggleEnabled() && data.bwg$isJiggleWithArmor()) ? (walkWobble + turnWobble) * data.bwg$getJiggleAmount() : 0.0;
		double jiggleX = (data.bwg$isJiggleEnabled() && data.bwg$isJiggleWithArmor()) ? impactWobble * data.bwg$getJiggleAmount() : 0.0;
		applyPair(model, data, shown, jiggleY, jiggleX, "armorBreastLeft", "armorBreastRight");
	}

	public static void applyStatic(StaticEntityModel model, boolean shown, float size, String leftName, String rightName) {
		BoneTransform left = model.getTransform(leftName);
		BoneTransform right = model.getTransform(rightName);
		if (left == null || right == null) return;
		left.visible = shown;
		right.visible = shown;
		if (!shown) return;
		left.scaleZ = size;
		right.scaleZ = size;
	}

	private static void applyPair(StaticEntityModel model, GenderData data, boolean shown, double jiggleY, double jiggleX, String leftName, String rightName) {
		BoneTransform left = model.getTransform(leftName);
		BoneTransform right = model.getTransform(rightName);
		if (left == null || right == null) return;
		left.visible = shown;
		right.visible = shown;
		if (!shown) return;

		left.scaleZ = data.bwg$getBreastSize();
		right.scaleZ = data.bwg$getBreastSize();

		if (data.bwg$isIndividualPhysics()) {
			// Dual physics
			double baseRot = Math.toRadians(data.bwg$getJiggleDirection());

			// Subtle random variation
			double leftRandom = Math.sin((System.currentTimeMillis() / 500.0) + leftName.hashCode()) * 0.02;
			double rightRandom = Math.sin((System.currentTimeMillis() / 500.0) + rightName.hashCode()) * 0.02;

			left.rotY = baseRot + jiggleY + leftRandom;
			right.rotY = -baseRot - jiggleY + rightRandom;

			left.rotX = jiggleX + leftRandom * 0.5;
			right.rotX = jiggleX + rightRandom * 0.5;

			double separation = data.bwg$getBreastSeparation() * 2.0F;
			left.posX = -separation;
			right.posX = separation;
		} else {
			// Unified physics
			left.rotY = jiggleY;
			right.rotY = -jiggleY;

			left.rotX = jiggleX;
			right.rotX = jiggleX;

			left.posX = 0.0F;
			right.posX = 0.0F;
		}
	}
}
