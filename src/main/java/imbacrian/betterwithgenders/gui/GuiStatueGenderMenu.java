package imbacrian.betterwithgenders.gui;

import imbacrian.bapilib.render.PlayerDoll;
import imbacrian.betterwithgenders.api.Gender;
import imbacrian.betterwithgenders.api.StatueGenderData;
import imbacrian.betterwithgenders.network.ServerModStatus;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.SliderElement;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.block.entity.TileEntityStatue;
import net.minecraft.core.world.pos.TilePos;
import org.lwjgl.input.Keyboard;
import net.minecraft.core.net.packet.PacketCustomPayload;

import static turniplabs.halplibe.HalpLibe.LOGGER;

public class GuiStatueGenderMenu extends Screen {

	private static final float BREAST_MIN = 0.0F;
	private static final float BREAST_MAX = 1.0F;

	private int guiWidth = 150;
	private int guiHeight = 110;
	int guiLeft;
	int guiTop;

	private SliderElement breastSlider;
	private double lastBreastValue = -1;

	private TileEntityStatue statue;
	private TilePos statuePos;

	public GuiStatueGenderMenu(TileEntityStatue statue, TilePos statuePos) {
		this.statue = statue;
		this.statuePos = statuePos;
	}

	@Override
	public void init() {
		super.init();
		LOGGER.info("BWG: Statue Gender Menu Opened!");
		StatueGenderData data = (StatueGenderData) this.statue;
		this.guiLeft = (this.width - this.guiWidth) / 2;
		this.guiTop = (this.height - this.guiHeight) / 2;

		this.buttons.clear();

		// Gender button
		this.buttons.add(new ButtonElement(0, this.guiLeft + 10, this.guiTop + 20, 130, 20,
			I18n.getInstance().translateKey("gui.gender.button") + ": "
			+ I18n.getInstance().translateKey("gui.gender." + data.bwg$getGender().name().toLowerCase())));

		// Breasts Slider
		this.breastSlider = new SliderElement(1, this.guiLeft + 10, this.guiTop + 45, 130, 20,
			I18n.getInstance().translateKey("gui.gender.slider"), data.bwg$getBreastSize());
		this.buttons.add(this.breastSlider);
		this.breastSlider.enabled = data.bwg$getGender() != Gender.MALE;
		this.lastBreastValue = data.bwg$getBreastSize();

		// Save changes
		this.buttons.add(new ButtonElement(2, this.guiLeft + 10, this.guiTop + 70, 130, 20, I18n.getInstance().translateKey("gui.done")));
	}

	@Override
	protected void buttonClicked(ButtonElement button) {
		StatueGenderData data = (StatueGenderData) this.statue;

		if (button.id == 0) {
			Gender next = data.bwg$getGender().next();
			data.bwg$setGender(next);
			this.sendStatueGenderPacket(next, data.bwg$getBreastSize());

			ButtonElement genderButton = this.buttons.get(0);
			genderButton.displayString = I18n.getInstance().translateKey("gui.gender.button")
				+ ": " + I18n.getInstance().translateKey("gui.gender." + next.name().toLowerCase());

			this.breastSlider.enabled = next != Gender.MALE;
		} else if (button.id == 2) {
			this.mc.displayScreen(null);
		}
	}

	@Override
	public void render(int mx, int my, float renderPartialTicks) {
		this.renderBackground();

		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.textureManager.loadTexture("/assets/betterwithgenders/textures/gui/statue_gender_container.png").bind();
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, guiWidth, guiHeight);
		this.drawStringCenteredShadow(this.fontRenderer, I18n.getInstance().translateKey("gui.container.statue_gender"), (this.width / 2), this.guiTop + 7, 16777215);

		super.render(mx, my, renderPartialTicks);

		StatueGenderData data = (StatueGenderData) this.statue;

		if (this.breastSlider.sliderValue != lastBreastValue) {
			float breastSize = (float) (BREAST_MIN + this.breastSlider.sliderValue * (BREAST_MAX - BREAST_MIN));
			data.bwg$setBreastSize(breastSize);
			this.lastBreastValue = this.breastSlider.sliderValue;
			this.sendStatueGenderPacket(data.bwg$getGender(), breastSize);
		}
	}

	private void sendStatueGenderPacket(Gender gender, float breastSize) {
		if (this.mc.getSendQueue() == null) return;
		try {
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
			dos.writeInt(statuePos.x);
			dos.writeInt(statuePos.y);
			dos.writeInt(statuePos.z);
			dos.writeUTF(gender.name());
			dos.writeFloat(breastSize);
			dos.flush();
			this.mc.getSendQueue().addToSendQueue(new PacketCustomPayload("BWG:StatueGender", baos.toByteArray()));
			dos.close();
			baos.close();
		} catch (java.io.IOException e) {
			// ignore
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
