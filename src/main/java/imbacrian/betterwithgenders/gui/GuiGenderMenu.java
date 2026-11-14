package imbacrian.betterwithgenders.gui;

import imbacrian.bapilib.render.PlayerDoll;
import imbacrian.betterwithgenders.client.BwgClientProfile;
import imbacrian.betterwithgenders.network.BwgPackets;
import imbacrian.betterwithgenders.api.Gender;
import imbacrian.betterwithgenders.api.GenderData;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.SliderElement;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.lang.I18n;
import org.lwjgl.input.Keyboard;
import net.minecraft.core.net.packet.PacketCustomPayload;

public class GuiGenderMenu extends Screen {

	private static final float SEPARATION_MIN = 0.0F;
	private static final float SEPARATION_MAX = 0.075F;

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
		this.buttons.add(new ButtonElement(0, this.guiLeft + 100, this.guiTop + 10, 130, 20,
		I18n.getInstance().translateKey("gui.gender.button") + ": "
		+ I18n.getInstance().translateKey("gui.gender." + data.bwg$getGender().name().toLowerCase())));

		// Breasts Slider
		this.breastSlider = new SliderElement(1, this.guiLeft + 100, this.guiTop + 35, 130, 20,
			I18n.getInstance().translateKey("gui.gender.slider"), data.bwg$getBreastSize());
		this.buttons.add(this.breastSlider);
		this.breastSlider.enabled = data.bwg$getGender() != Gender.MALE;
		this.lastBreastValue = data.bwg$getBreastSize();

		// Breast Separation Slider
		// The slider works in 0..1, the stored value in SEPARATION_MIN..SEPARATION_MAX (same conversion as render()).
		// Passing the raw value made the slider look reset every time the menu was reopened.
		double separationNormalized = (data.bwg$getBreastSeparation() - SEPARATION_MIN) / (SEPARATION_MAX - SEPARATION_MIN);
		separationNormalized = Math.max(0.0D, Math.min(1.0D, separationNormalized));
		this.separationSlider = new SliderElement(2, this.guiLeft + 100, this.guiTop + 60, 130, 20,
			I18n.getInstance().translateKey("gui.gender.separation.slider"), (float) separationNormalized);
		this.buttons.add(this.separationSlider);
		this.separationSlider.enabled = data.bwg$getGender() != Gender.MALE && data.bwg$isIndividualPhysics();
		this.lastSeparationValue = this.separationSlider.sliderValue;

		// Jiggle Physics Menu
		this.buttons.add(new ButtonElement(3, this.guiLeft + 100, this.guiTop + 85, 130, 20,
			I18n.getInstance().translateKey("gui.gender.jiggle.open")));

		// Save changes
		this.buttons.add(new ButtonElement(4, this.guiLeft + 100, this.guiTop + 110, 130, 20, I18n.getInstance().translateKey("gui.done")));
	}

	@Override
	protected void buttonClicked(ButtonElement button) {

		if (button.id == 0) {
			GenderData player = (GenderData) this.mc.thePlayer;
			Gender next = player.bwg$getGender().next();
			player.bwg$setGender(next);
			this.sendGenderPacket();

			ButtonElement genderButton = this.buttons.get(0);
			genderButton.displayString = I18n.getInstance().translateKey("gui.gender.button")
				+ ": " + I18n.getInstance().translateKey("gui.gender." + next.name().toLowerCase());

			this.breastSlider.enabled = next != Gender.MALE;
			this.separationSlider.enabled = next != Gender.MALE && player.bwg$isIndividualPhysics();
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

		GenderData data = (GenderData) this.mc.thePlayer;

		if (this.breastSlider.sliderValue != lastBreastValue) {

			data.bwg$setBreastSize((float) this.breastSlider.sliderValue);
			this.lastBreastValue = this.breastSlider.sliderValue;
			this.sendGenderPacket();
		}

		if (this.separationSlider.sliderValue != lastSeparationValue) {
			float separation = (float) (SEPARATION_MIN + this.separationSlider.sliderValue * (SEPARATION_MAX - SEPARATION_MIN));
			data.bwg$setBreastSeparation(separation);
			this.lastSeparationValue = this.separationSlider.sliderValue;
			this.sendGenderPacket();
		}
	}

	private void sendGenderPacket() {
		GenderData data = (GenderData) this.mc.thePlayer;
		// Always remember the change globally, even in singleplayer (no send queue there).
		BwgClientProfile.captureFrom(data);
		if (this.mc.getSendQueue() == null) return;
		try {
			this.mc.getSendQueue().addToSendQueue(new PacketCustomPayload(BwgPackets.CHANNEL_GENDER,
				BwgPackets.encodeGender(this.mc.thePlayer.id, data)));
		} catch (java.io.IOException e) {
			// best-effort
		}
	}

	@Override
	public void removed() {
		super.removed();
		BwgClientProfile.saveIfDirty();
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
