package imbacrian.betterwithgenders.render;

import imbacrian.betterwithgenders.BwGenders;
import imbacrian.betterwithgenders.api.Gender;
import imbacrian.betterwithgenders.api.GenderData;
import net.minecraft.client.render.EntityRendererDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Unique;
import org.useless.dragonfly.data.entity.mojang.EntityGeometryMojangData;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

public class MammaryModel {

	public static void apply(StaticEntityModel model, GenderData data, double wobble) {
		boolean shown = data.bwg$getGender() == Gender.FEMALE || data.bwg$getGender() == Gender.OTHER;
		double jiggleRot = data.bwg$isJiggleEnabled() ? wobble * data.bwg$getJiggleAmount() : 0.0;
		applyPair(model, data, shown, jiggleRot, "breastLeft", "breastRight");
		applyPair(model, data, shown, jiggleRot, "jacketBreastLeft", "jacketBreastRight");
	}

	public static void applyArmor(StaticEntityModel model, GenderData data, double wobble) {
		boolean shown = data.bwg$getGender() == Gender.FEMALE || data.bwg$getGender() == Gender.OTHER;
		double jiggleRot = shown && data.bwg$isJiggleEnabled() ? wobble * data.bwg$getJiggleAmount() : 0.0;
		applyPair(model, data, shown, jiggleRot, "armorBreastLeft", "armorBreastRight");
	}

	private static void applyPair(StaticEntityModel model, GenderData data, boolean shown, double jiggleRot, String leftName, String rightName) {
		BoneTransform left = model.getTransform(leftName);
		BoneTransform right = model.getTransform(rightName);
		if (left == null || right == null) return;
		left.visible = shown;
		right.visible = shown;
		if (!shown) return;

		left.scaleZ = data.bwg$getBreastSize();
		right.scaleZ = data.bwg$getBreastSize();

		double baseRot = Math.toRadians(data.bwg$getJiggleDirection());
		left.rotY = baseRot + jiggleRot;
		right.rotY = -baseRot - jiggleRot;

		double separation = data.bwg$getBreastSeparation()/2.0F;
		left.posX = -separation;
		right.posX = separation;
	}
}
