package imbacrian.betterwithgenders.client;

import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Test poses, pressing F8 in game to step through them; each one
   applies a fixed, exaggerated transform without physics to every breast,
   so a screenshot shows exactly how the game treats the transforms. Pose 0 is normal play. */
public final class BwgDebug {
	private static final Logger LOGGER = LoggerFactory.getLogger("betterwithgenders/debug");

	public static final String[] POSES = {
		"0 off (normal physics and droop)",
		"1 droop only (physics frozen, droop = -22 deg x size on the Droop bone)",
		"2 Mount rotX +0.5 rad (28.6 deg), nothing else",
		"3 Mount rotX -0.5 rad (28.6 deg), nothing else",
		"4 Mount rotY +0.5 rad, nothing else",
		"5 Mount rotY -0.5 rad, nothing else",
		"6 everything zero (box straight, no droop)",
		"7 block scaleY x1.5, nothing else",
		"8 Droop rotX +0.5 rad, nothing else",
		"9 Droop rotX -0.5 rad, nothing else"
	};

	public static int pose = 0;

	public static volatile String lastValues = "(no breast rendered yet)";

	private BwgDebug() {}

	public static void next() {
		pose = (pose + 1) % POSES.length;
		LOGGER.info("BWG debug pose: {}", POSES[pose]);
		System.out.println("[BWG] debug pose " + POSES[pose]);
		try {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc != null && mc.hudIngame != null) mc.hudIngame.addChatMessage("[BWG] pose " + POSES[pose]);
		} catch (Throwable ignored) {
			// the label is only a convenience
		}
	}
}
