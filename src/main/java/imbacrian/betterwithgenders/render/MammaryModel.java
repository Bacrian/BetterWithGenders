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
		applyPair(model, shown, data.bwg$getBreastSize(), data.bwg$getJiggleDirection(),
			data.bwg$isJiggleEnabled() ? wobble * data.bwg$getJiggleAmount() : 0.0,
			"breastLeft", "breastRight");
		applyPair(model, shown, data.bwg$getBreastSize(), data.bwg$getJiggleDirection(),
			data.bwg$isJiggleEnabled() ? wobble * data.bwg$getJiggleAmount() : 0.0,
			"jacketBreastLeft", "jacketBreastRight");
	}

	public static void applyArmor(StaticEntityModel model, GenderData data, double wobble) {
		boolean shown = data.bwg$getGender() == Gender.FEMALE || data.bwg$getGender() == Gender.OTHER;
		double rot = shown && data.bwg$isJiggleEnabled() ? wobble * data.bwg$getJiggleAmount() : 0.0;
		applyPair(model, shown, data.bwg$getBreastSize(), data.bwg$getJiggleDirection(), rot,
			"armorBreastLeft", "armorBreastRight");
	}

	private static void applyPair(StaticEntityModel model, boolean shown, float size, float direction, double wobble, String leftName, String rightName) {
		BoneTransform left = model.getTransform(leftName);
		BoneTransform right = model.getTransform(rightName);
		if (left == null || right == null) return;
		left.visible = shown;
		right.visible = shown;
		if (!shown) return;

		left.scaleZ = size;
		right.scaleZ = size;

		double baseRot = Math.toRadians(direction);
		double jiggleRot = wobble * 0.5;
		double finalRot = baseRot + jiggleRot;
		left.rotY = finalRot/18; left.posX = -finalRot/2; left.posY = -finalRot/10; left.posZ = -finalRot/20;
		right.rotY = -finalRot/18; right.posX = finalRot/2; right.posY = -finalRot/10; right.posZ = -finalRot/20;
	}
}
