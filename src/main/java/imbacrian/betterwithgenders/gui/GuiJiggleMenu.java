package imbacrian.betterwithgenders.gui;

import imbacrian.betterwithgenders.api.GenderData;
import imbacrian.betterwithgenders.api.Gender;
import imbacrian.bapilib.render.PlayerDoll;
import net.minecraft.client.gui.*;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.lang.I18n;
import org.lwjgl.input.Keyboard;

public class GuiJiggleMenu extends Screen {

	private static final float DIRECTION_MIN = 0.0F;
	private static final float DIRECTION_MAX = 35.0F;

	private int guiWidth = 176;
	private int guiHeight = 125;
	int guiLeft;
	int guiTop;

	private SliderElement directionSlider;
	private SliderElement amountSlider;
	private ButtonElement armorToggleButton;

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

		double directionValue = (data.bwg$getJiggleDirection() - DIRECTION_MIN) / (DIRECTION_MAX - DIRECTION_MIN);
		this.directionSlider = new SliderElement(1, this.guiLeft + 10, this.guiTop + 20, 156, 20,
			I18n.getInstance().translateKey("gui.gender.jiggle.direction"), (float) directionValue);
		this.directionSlider.enabled = canJiggle;
		this.buttons.add(this.directionSlider);

		this.amountSlider = new SliderElement(2, this.guiLeft + 10, this.guiTop + 45, 156, 20,
			I18n.getInstance().translateKey("gui.gender.jiggle.amount"), data.bwg$getJiggleAmount());
		this.amountSlider.enabled = canJiggle;
		this.buttons.add(this.amountSlider);

		this.armorToggleButton = new ButtonElement(3, this.guiLeft + 10, this.guiTop + 70, 156, 20,
			this.armorToggleText(data.bwg$isJiggleWithArmor()));
		this.armorToggleButton.enabled = canJiggle;
		this.buttons.add(this.armorToggleButton);

		this.buttons.add(new ButtonElement(0, this.guiLeft + 10, this.guiTop + 95, 156, 20,
			I18n.getInstance().translateKey("gui.done")));

		this.lastDirectionValue = this.directionSlider.sliderValue;
		this.lastAmountValue = this.amountSlider.sliderValue;
	}

	private String armorToggleText(boolean value) {
		return I18n.getInstance().translateKey("gui.gender.jiggle.armor") + ": "
			+ I18n.getInstance().translateKey(value ? "options.on" : "options.off"); // ajustá las claves si usás otras
	}

	@Override
	protected void buttonClicked(ButtonElement button) {
		if (button.id == 0) {
			this.mc.displayScreen(new GuiGenderMenu());
		} else if (button.id == 3) {
			GenderData data = (GenderData) this.mc.thePlayer;
			boolean newValue = !data.bwg$isJiggleWithArmor();
			data.bwg$setJiggleWithArmor(newValue);
			this.armorToggleButton.displayString = this.armorToggleText(newValue);
		}
	}

	@Override
	public void render(int mx, int my, float renderPartialTicks) {
		this.renderBackground();

		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.textureManager.loadTexture("/assets/betterwithgenders/textures/gui/jiggle_container.png").bind();
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, guiWidth, guiHeight);
		this.drawStringCenteredShadow(this.fontRenderer, I18n.getInstance().translateKey("gui.container.jiggle"),
			this.width / 2, this.guiTop + 5, 16777215);

		PlayerDoll.renderPlayerDoll(guiLeft - 47, guiTop + 125, mx, my,renderPartialTicks,50.F);

		super.render(mx, my, renderPartialTicks);

		GenderData data = (GenderData) this.mc.thePlayer;

		if (this.directionSlider.sliderValue != this.lastDirectionValue) {
			float degrees = (float) (DIRECTION_MIN + this.directionSlider.sliderValue * (DIRECTION_MAX - DIRECTION_MIN));
			data.bwg$setJiggleDirection(degrees);
			this.lastDirectionValue = this.directionSlider.sliderValue;
		}

		if (this.amountSlider.sliderValue != this.lastAmountValue) {
			data.bwg$setJiggleAmount((float) this.amountSlider.sliderValue);
			this.lastAmountValue = this.amountSlider.sliderValue;
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
