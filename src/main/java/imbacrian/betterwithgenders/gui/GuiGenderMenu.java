package imbacrian.betterwithgenders.gui;

import imbacrian.bapilib.render.PlayerDoll;
import imbacrian.betterwithgenders.api.Gender;
import imbacrian.betterwithgenders.api.GenderData;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.SliderElement;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.lang.I18n;
import org.lwjgl.input.Keyboard;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import net.minecraft.core.net.packet.PacketCustomPayload;

public class GuiGenderMenu extends Screen {

	private int guiWidth = 242;
	private int guiHeight = 140;
	int guiLeft;
	int guiTop;

	private SliderElement breastSlider;
	private SliderElement separationSlider;
	private double lastBreastValue = -1;
	private double lastSeparationValue = -1;



	@Override
	public void init() {
		super.init();
		GenderData data = (GenderData) this.mc.thePlayer;
		this.guiLeft = (this.width - this.guiWidth) / 2;
		this.guiTop = (this.height - this.guiHeight) / 2;

		this.buttons.clear();

		// Gender button on right panel
		this.buttons.add(new ButtonElement(0, this.guiLeft + 135, this.guiTop + 10, 100, 20,
		I18n.getInstance().translateKey("gui.gender.button") + ": "
		+ I18n.getInstance().translateKey("gui.gender." + data.bwg$getGender().name().toLowerCase())));

		// Breasts Slider
		this.breastSlider = new SliderElement(1, this.guiLeft + 135, this.guiTop + 35, 100, 20,
			I18n.getInstance().translateKey("gui.gender.slider"), data.bwg$getBreastSize());
		this.buttons.add(this.breastSlider);
		this.breastSlider.enabled = data.bwg$getGender() != Gender.MALE;
		this.lastBreastValue = data.bwg$getBreastSize();

		// Breast Separation Slider

		this.separationSlider = new SliderElement(2, this.guiLeft + 135, this.guiTop + 60, 100, 20,
			I18n.getInstance().translateKey("gui.gender.separation.slider"), data.bwg$getBreastSeparation());
		this.buttons.add(this.separationSlider);
		this.separationSlider.enabled = data.bwg$getGender() != Gender.MALE;
		this.lastSeparationValue = data.bwg$getBreastSeparation();

		// Jiggle Physics Menu
		this.buttons.add(new ButtonElement(3, this.guiLeft + 135, this.guiTop + 85, 100, 20,
			I18n.getInstance().translateKey("gui.gender.jiggle.open")));

		// Save changes
		this.buttons.add(new ButtonElement(4, this.guiLeft + 135, this.guiTop + 110, 100, 20, I18n.getInstance().translateKey("gui.done")));
	}

	@Override
	protected void buttonClicked(ButtonElement button) {

		if (button.id == 0) {
			GenderData player = (GenderData) this.mc.thePlayer;
			Gender next = player.bwg$getGender().next();
			player.bwg$setGender(next);
			this.sendGenderPacket(next, player.bwg$getBreastSize());

			ButtonElement genderButton = this.buttons.get(0);
			genderButton.displayString = I18n.getInstance().translateKey("gui.gender.button")
				+ ": " + I18n.getInstance().translateKey("gui.gender." + next.name().toLowerCase());

			this.breastSlider.enabled = next != Gender.MALE;
			this.separationSlider.enabled = next != Gender.MALE;
		} else if (button.id == 3) {
		this.mc.displayScreen(new GuiJiggleMenu());
		} else if (button.id == 4) {
			this.mc.displayScreen(null);
		}
	}

	@Override
	public void render(int mx, int my, float renderPartialTicks) {
		this.renderBackground();

		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.textureManager.loadTexture("/assets/betterwithgenders/textures/gui/gender_container.png").bind();
		this.drawTexturedModalRect(guiLeft,guiTop,0,0,guiWidth,guiHeight);
		this.drawStringCenteredShadow(this.fontRenderer,I18n.getInstance().translateKey("gui.container.gender"),(this.width / 2)-72,this.guiTop + 10, 16777215);

		PlayerDoll.renderPlayerDoll(guiLeft + 47, guiTop + 125, mx, my,renderPartialTicks,50.F);

		super.render(mx, my, renderPartialTicks);

		if (this.breastSlider.sliderValue != lastBreastValue) {
			GenderData player = (GenderData) this.mc.thePlayer;
			player.bwg$setBreastSize((float) this.breastSlider.sliderValue);
			this.lastBreastValue = this.breastSlider.sliderValue;
			this.sendGenderPacket(player.bwg$getGender(), player.bwg$getBreastSize());
		}
	}

	private void sendGenderPacket(Gender gender, float breastSize) {
		if (this.mc.getSendQueue() == null) return;
		try {
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
			dos.writeInt(this.mc.thePlayer.id);
			dos.writeUTF(gender.name());
			dos.writeFloat(breastSize);
			dos.flush();
			this.mc.getSendQueue().addToSendQueue(new PacketCustomPayload("BWG:Gender", baos.toByteArray()));
			dos.close();
			baos.close();
		} catch (java.io.IOException e) {
			// ignore. Yup.
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
