package imbacrian.betterwithgenders.mixin;

import imbacrian.betterwithgenders.api.GenderData;
import imbacrian.betterwithgenders.render.MammaryModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.HumanArmorShape;
import net.minecraft.core.util.helper.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
@Mixin(value = MobRendererPlayer.class, remap = false)
public abstract class MobRendererPlayerMixin extends MobRenderer<Player> {
	protected MobRendererPlayerMixin(float shadowSize) {
		super(shadowSize);
	}

	@Inject(method = "setupAnimations", at = @At("RETURN"))
	private void bwg$applyBreastSize(Player entity, StaticEntityModel model, float partialTick, int layer,
	                                 CallbackInfoReturnable<StaticEntityModel> cir) {
		StaticEntityModel result = cir.getReturnValue();
		if (result == null || layer != 0) return;
		GenderData data = (GenderData) entity;

		double xd = MathHelper.lerp(entity.xdO, entity.xd, partialTick);
		double zd = MathHelper.lerp(entity.zdO, entity.zd, partialTick);
		double vel = -1.0 / (3.0 * Math.hypot(xd, zd) + 1.0) + 1.0;
		double yd = MathHelper.lerp(entity.ydO, entity.yd, partialTick);
		boolean nowOnGround = entity.onGround;

		if (!nowOnGround && yd < 0.0) {
			data.bwg$setPrevFallSpeed(yd);
		}
		if (!data.bwg$isWasOnGround() && nowOnGround) {
			double impactStrength = MathHelper.clamp(-data.bwg$getPrevFallSpeed() * 20.0, 0.0, 1.5);
			data.bwg$setFallImpulse(data.bwg$getFallImpulse() + impactStrength);
		}
		data.bwg$setWasOnGround(nowOnGround);

		double elapsedTicks = (entity.tickCount + partialTick) - data.bwg$getLastImpulseTick();
		double impulse = data.bwg$getFallImpulse() * Math.pow(0.7, elapsedTicks);
		data.bwg$setFallImpulse(impulse);
		data.bwg$setLastImpulseTick(entity.tickCount + partialTick);

		double timer = data.bwg$getJiggleTimer()
			+ (entity.tickCount + partialTick - data.bwg$getJiggleLastRenderTick()) / (30.0 - 29.0 * MathHelper.clamp(vel, 0.0, 1.0));
		data.bwg$setJiggleTimer(timer);
		data.bwg$setJiggleLastRenderTick(entity.tickCount);

		float currentYaw = entity.yRot;
		double turnWobble = data.bwg$getTurnWobble();
		if (data.bwg$isIndividualPhysics()) {
			float yawDiff = MathHelper.wrapDegrees(currentYaw - data.bwg$getPrevYaw());
			if (Math.abs(yawDiff) > 0.5 && Math.abs(yawDiff) < 45.0) {
				turnWobble += yawDiff * 0.08 * data.bwg$getJiggleAmount();
				data.bwg$setPrevYaw(currentYaw);
			}
			turnWobble *= 0.85;
		} else {
			turnWobble = 0.0;
			data.bwg$setPrevYaw(currentYaw);
		}
		data.bwg$setTurnWobble(turnWobble);

		double walkWobble = (Math.sin(timer) * 0.7 + Math.sin(timer * 2.0) * 0.3) * vel;
		double impactWobble = impulse * Math.sin(timer * 1.12);
		double turnWobbleEffect = turnWobble * Math.sin(timer * 0.5);

		data.bwg$setLastWobble(walkWobble);
		data.bwg$setLastImpactWobble(impactWobble);
		data.bwg$setLastTurnWobble(turnWobbleEffect);

		boolean hasChestplate = entity.getItemInArmorSlot(HumanArmorShape.CHEST) != null;
		MammaryModel.apply(result, data, walkWobble, impactWobble, turnWobbleEffect, hasChestplate);
	}
}
