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

public class GuiGenderMenu extends Screen {

	private int guiWidth = 242;
	private int guiHeight = 140;
	int guiLeft;
	int guiTop;

	private SliderElement breastSlider;
	private double lastBreastValue = -1;
	GenderData data = (GenderData) this.mc.thePlayer;


	@Override
	public void init() {
		super.init();
		this.guiLeft = (this.width - this.guiWidth) / 2;
		this.guiTop = (this.height - this.guiHeight) / 2;

		this.buttons.clear();

		// Gender button on right panel
		this.buttons.add(new ButtonElement(1, this.guiLeft + 135, this.guiTop + 35, 100, 20,
		I18n.getInstance().translateKey("gui.gender.button") + ": "
		+ I18n.getInstance().translateKey("gui.gender." + data.bwg$getGender().name().toLowerCase())));

		//Breasts Slider
		this.breastSlider = new SliderElement(2, this.guiLeft + 135, this.guiTop + 60, 100, 20,
			I18n.getInstance().translateKey("gui.gender.slider"), data.bwg$getBreastSize());
		this.buttons.add(this.breastSlider);
		this.breastSlider.enabled = data.bwg$getGender() != Gender.MALE;
		this.lastBreastValue = data.bwg$getBreastSize();

		// Jiggle Physics Menu
		this.buttons.add(new ButtonElement(3, this.guiLeft + 135, this.guiTop + 85, 100, 20,
			I18n.getInstance().translateKey("gui.gender.jiggle.open")));

		// Save changes
		this.buttons.add(new ButtonElement(0, this.guiLeft + 135, this.guiTop + 110, 100, 20, I18n.getInstance().translateKey("gui.done")));
	}

	@Override
	protected void buttonClicked(ButtonElement button) {
		if (button.id == 0) {
			this.mc.displayScreen(null);
		} else if (button.id == 1) {
			GenderData player = (GenderData) this.mc.thePlayer;
			Gender next = player.bwg$getGender().next();
			player.bwg$setGender(next);

			ButtonElement genderButton = this.buttons.get(0);
			genderButton.displayString = I18n.getInstance().translateKey("gui.gender.button")
				+ ": " + I18n.getInstance().translateKey("gui.gender." + next.name().toLowerCase());

			this.breastSlider.enabled = next != Gender.MALE;
		} else if (button.id == 3) {
		this.mc.displayScreen(new GuiJiggleMenu());
		}
	}

	@Override
	public void render(int mx, int my, float renderPartialTicks) {
		this.renderBackground();

		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.textureManager.loadTexture("/assets/betterwithgenders/textures/gui/gender_container.png").bind();
		this.drawTexturedModalRect(guiLeft,guiTop,0,0,guiWidth,guiHeight);
		this.drawStringCenteredShadow(this.fontRenderer,I18n.getInstance().translateKey("gui.container.gender"),this.width / 2,this.guiTop + 10, 16777215);

		PlayerDoll.renderPlayerDoll(guiLeft + 47, guiTop + 125, mx, my,renderPartialTicks,50.F);

		super.render(mx, my, renderPartialTicks);

		if (this.breastSlider.sliderValue != lastBreastValue) {
			((GenderData) this.mc.thePlayer).bwg$setBreastSize((float) this.breastSlider.sliderValue);
			this.lastBreastValue = this.breastSlider.sliderValue;
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
