package imbacrian.betterwithgenders.gui;

import imbacrian.betterwithgenders.api.GenderData;
import imbacrian.betterwithgenders.api.Gender;
import imbacrian.bapilib.render.PlayerDoll;
import net.minecraft.client.gui.*;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.lang.I18n;
import org.lwjgl.input.Keyboard;
import net.minecraft.core.net.packet.PacketCustomPayload;

public class GuiJiggleMenu extends Screen {

	private static final float DIRECTION_MIN = 0.0F;
	private static final float DIRECTION_MAX = 7.5F;

	private int guiWidth = 196;
	private int guiHeight = 125;
	int guiLeft;
	int guiTop;

	private SliderElement directionSlider;
	private SliderElement amountSlider;
	private ButtonElement armorToggleButton;
	private ButtonElement jiggleToggleButton;
	private ButtonElement individualPhysicsButton;

	private double lastDirectionValue;
	private double lastAmountValue;

	@Override
	public void init() {
		super.init();
		this.guiLeft = (this.width - this.guiWidth) / 2;
		this.guiTop = (this.height - this.guiHeight) / 2;
		this.buttons.clear();

		GenderData data = (GenderData) this.mc.thePlayer;
		boolean canJiggle = data.bwg$getGender() != Gender.MALE;
		boolean individualPhysics = data.bwg$isIndividualPhysics();

		double directionValue = (data.bwg$getJiggleDirection() - DIRECTION_MIN) / (DIRECTION_MAX - DIRECTION_MIN);
		this.directionSlider = new SliderElement(1, this.guiLeft + 10, this.guiTop + 20, 85, 20,
			I18n.getInstance().translateKey("gui.gender.jiggle.direction"), (float) directionValue);
		this.directionSlider.enabled = canJiggle && individualPhysics;
		this.buttons.add(this.directionSlider);

		this.amountSlider = new SliderElement(2, this.guiLeft + 100, this.guiTop + 20, 85, 20,
			I18n.getInstance().translateKey("gui.gender.jiggle.amount"), data.bwg$getJiggleAmount());
		this.amountSlider.enabled = canJiggle;
		updateAmountSliderText();
		this.buttons.add(this.amountSlider);

		this.jiggleToggleButton = new ButtonElement(3, this.guiLeft + 10, this.guiTop + 45, 175, 20,
			this.jiggleToggleText(data.bwg$isJiggleEnabled()));
		this.jiggleToggleButton.enabled = canJiggle;
		this.buttons.add(this.jiggleToggleButton);

		this.armorToggleButton = new ButtonElement(4, this.guiLeft + 10, this.guiTop + 70, 175, 20,
			this.armorToggleText(data.bwg$isJiggleWithArmor()));
		this.armorToggleButton.enabled = canJiggle;
		this.buttons.add(this.armorToggleButton);

		this.individualPhysicsButton = new ButtonElement(5, this.guiLeft + 10, this.guiTop + 95, 85, 20,
			this.individualPhysicsText(individualPhysics));
		this.individualPhysicsButton.enabled = canJiggle;
		this.buttons.add(this.individualPhysicsButton);

		this.buttons.add(new ButtonElement(0, this.guiLeft + 100, this.guiTop + 95, 85, 20,
			I18n.getInstance().translateKey("gui.done")));

		this.lastDirectionValue = this.directionSlider.sliderValue;
		this.lastAmountValue = this.amountSlider.sliderValue;
	}

	private String jiggleToggleText(boolean value) {
		return I18n.getInstance().translateKey("gui.gender.jiggle.enable") + ": "
			+ I18n.getInstance().translateKey(value ? "options.on" : "options.off");
	}

	private String armorToggleText(boolean value) {
		return I18n.getInstance().translateKey("gui.gender.jiggle.armor") + ": "
			+ I18n.getInstance().translateKey(value ? "options.on" : "options.off");
	}

	private String individualPhysicsText(boolean value) {
		return I18n.getInstance().translateKey("gui.gender.jiggle.individual") + ": "
			+ I18n.getInstance().translateKey(value ? "options.on" : "options.off");
	}

	private void updateAmountSliderText() {
		if (this.amountSlider != null) {
			if (this.amountSlider.sliderValue >= 1.0D) {
				this.amountSlider.displayString = I18n.getInstance().translateKey("gui.gender.jiggle.max");
			} else {
				this.amountSlider.displayString = I18n.getInstance().translateKey("gui.gender.jiggle.amount");
			}
		}
	}

	@Override
	protected void buttonClicked(ButtonElement button) {
		if (button.id == 0) {
			this.mc.displayScreen(new GuiGenderMenu());
		} else if (button.id == 3) {
			GenderData data = (GenderData) this.mc.thePlayer;
			boolean newValue = !data.bwg$isJiggleEnabled();
			data.bwg$setJiggleEnabled(newValue);
			this.jiggleToggleButton.displayString = this.jiggleToggleText(newValue);
			this.sendJigglePacket(data);
		} else if (button.id == 4) {
			GenderData data = (GenderData) this.mc.thePlayer;
			boolean newValue = !data.bwg$isJiggleWithArmor();
			data.bwg$setJiggleWithArmor(newValue);
			this.armorToggleButton.displayString = this.armorToggleText(newValue);
			this.sendJigglePacket(data);
		} else if (button.id == 5) {
			GenderData data = (GenderData) this.mc.thePlayer;
			boolean newValue = !data.bwg$isIndividualPhysics();
			data.bwg$setIndividualPhysics(newValue);
			this.individualPhysicsButton.displayString = this.individualPhysicsText(newValue);
			this.directionSlider.enabled = newValue && data.bwg$getGender() != Gender.MALE;
			this.sendJigglePacket(data);
		}
	}

	@Override
	public void render(int mx, int my, float renderPartialTicks) {
		this.renderBackground();

		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.textureManager.loadTexture("/assets/betterwithgenders/textures/gui/jiggle_container.png").bind();
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, guiWidth, guiHeight);
		this.drawStringCenteredShadow(this.fontRenderer, I18n.getInstance().translateKey("gui.container.jiggle"),
			this.width / 2, this.guiTop + 7, 16777215);

		PlayerDoll.renderPlayerDoll(guiLeft - 47, guiTop + 110, mx, my,renderPartialTicks,50.F);

		super.render(mx, my, renderPartialTicks);

		GenderData data = (GenderData) this.mc.thePlayer;

		if (this.directionSlider.sliderValue != this.lastDirectionValue) {
			float degrees = (float) (DIRECTION_MIN + this.directionSlider.sliderValue * (DIRECTION_MAX - DIRECTION_MIN));
			data.bwg$setJiggleDirection(degrees);
			this.lastDirectionValue = this.directionSlider.sliderValue;
			this.sendJigglePacket(data);
		}

		if (this.amountSlider.sliderValue != this.lastAmountValue) {
			data.bwg$setJiggleAmount((float) this.amountSlider.sliderValue);
			this.lastAmountValue = this.amountSlider.sliderValue;
			this.sendJigglePacket(data);
			updateAmountSliderText();
		}
	}

	private void sendJigglePacket(GenderData data) {
		if (this.mc.getSendQueue() == null) return;

		try {
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
			dos.writeInt(this.mc.thePlayer.id);
			dos.writeFloat(data.bwg$getJiggleDirection());
			dos.writeFloat(data.bwg$getJiggleAmount());
			dos.writeBoolean(data.bwg$isJiggleWithArmor());
			dos.writeBoolean(data.bwg$isJiggleEnabled());
			dos.writeBoolean(data.bwg$isIndividualPhysics());
			dos.flush();
			this.mc.getSendQueue().addToSendQueue(new PacketCustomPayload("BWG:Jiggle", baos.toByteArray()));
			dos.close();
			baos.close();
		} catch (java.io.IOException e) {
			// ignore; best-effort
		}
	}

	@Override
	public void keyPressed(char eventCharacter, int eventKey, int mx, int my) {
		if (eventKey == Keyboard.KEY_ESCAPE) {
			this.mc.displayScreen(null);
		} else {
			super.keyPressed(eventCharacter, eventKey, mx, my);
		}
	}
}
