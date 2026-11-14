package imbacrian.betterwithgenders.client;

import imbacrian.betterwithgenders.api.Gender;
import imbacrian.betterwithgenders.api.GenderData;
import imbacrian.betterwithgenders.network.BwgPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

@Environment(EnvType.CLIENT)
public final class BwgClientProfile {
	private static final Logger LOGGER = LoggerFactory.getLogger("betterwithgenders/profile");
	private static final String FILE_NAME = "betterwithgenders-profile.properties";

	/* Snapshot of the persistent GenderData fields. Defaults mirror PlayerGenderMixin. */
	private static final class Data {
		Gender gender = Gender.MALE;
		float breastSize = 0.5F;
		float breastSeparation = 0.0F;
		float jiggleDirection = 0.0F;
		float jiggleAmount = 0.3F;
		boolean jiggleEnabled = true;
		boolean jiggleWithArmor = false;
		boolean individualPhysics = true;

		static Data of(GenderData d) {
			Data out = new Data();
			out.gender = d.bwg$getGender();
			out.breastSize = d.bwg$getBreastSize();
			out.breastSeparation = d.bwg$getBreastSeparation();
			out.jiggleDirection = d.bwg$getJiggleDirection();
			out.jiggleAmount = d.bwg$getJiggleAmount();
			out.jiggleEnabled = d.bwg$isJiggleEnabled();
			out.jiggleWithArmor = d.bwg$isJiggleWithArmor();
			out.individualPhysics = d.bwg$isIndividualPhysics();
			return out;
		}

		void applyTo(GenderData d) {
			d.bwg$setGender(gender);
			d.bwg$setBreastSize(breastSize);
			d.bwg$setBreastSeparation(breastSeparation);
			d.bwg$setJiggleDirection(jiggleDirection);
			d.bwg$setJiggleAmount(jiggleAmount);
			d.bwg$setJiggleEnabled(jiggleEnabled);
			d.bwg$setJiggleWithArmor(jiggleWithArmor);
			d.bwg$setIndividualPhysics(individualPhysics);
		}
	}

	private static Data profile; // null until the user customises something (or a file is found)
	private static boolean loaded = false;
	private static boolean dirty = false;
	private static Player lastPlayer; // a local player instance that's already handled
	private static boolean tickFailed = false;

	private BwgClientProfile() {}

	// The public API

	/* Called from the menus after every change. Updates the global profile (written to disk later). */
	public static void captureFrom(GenderData data) {
		ensureLoaded();
		profile = Data.of(data);
		dirty = true;
	}

	/* Writes the profile to disk if it changed. Cheap to call; menus call it when they close. */
	public static void saveIfDirty() {
		if (!dirty || profile == null) return;
		dirty = false;
		Path file = file();
		Path tmp = file.resolveSibling(FILE_NAME + ".tmp");
		Properties p = new Properties();
		p.setProperty("gender", profile.gender.name());
		p.setProperty("breastSize", Float.toString(profile.breastSize));
		p.setProperty("breastSeparation", Float.toString(profile.breastSeparation));
		p.setProperty("jiggleDirection", Float.toString(profile.jiggleDirection));
		p.setProperty("jiggleAmount", Float.toString(profile.jiggleAmount));
		p.setProperty("jiggleEnabled", Boolean.toString(profile.jiggleEnabled));
		p.setProperty("jiggleWithArmor", Boolean.toString(profile.jiggleWithArmor));
		p.setProperty("individualPhysics", Boolean.toString(profile.individualPhysics));
		try {
			Files.createDirectories(file.getParent());
			try (OutputStream out = Files.newOutputStream(tmp)) {
				p.store(out, "Better With Genders - global profile (delete this file to stop overriding worlds/servers)");
			}
			Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			dirty = true; // try again later
			LOGGER.warn("BWG: could not save {}", file, e);
		}
	}

	public static void tick(Minecraft mc) {
		try {
			Player player = mc.thePlayer;
			if (player == lastPlayer) {
				if (dirty && mc.currentScreen == null) saveIfDirty();
				return;
			}
			Player previous = lastPlayer;
			lastPlayer = player;
			if (player == null || !(player instanceof GenderData gd)) return;

			if (!mc.isMultiplayerWorld()) {
				ensureLoaded();
				if (profile != null) {
					profile.applyTo(gd);
					return;
				}
			}
			if (previous instanceof GenderData prev) {
				gd.bwg$copyPersistentFrom(prev);
			}
		} catch (Throwable t) {
			if (!tickFailed) { // never take the game loop down because of this hook
				tickFailed = true;
				LOGGER.error("BWG: profile tick failed", t);
			}
		}
	}

	/* Server tells what it has stored for us (login / respawn). It always wins in multiplayer. */
	public static void onSelfSync(Minecraft mc, byte[] data) {
		if (!(mc.thePlayer instanceof GenderData gd)) return;
		try {
			BwgPackets.applySelfSync(data, gd);
		} catch (IOException e) {
			LOGGER.warn("BWG: bad {} payload", BwgPackets.CHANNEL_SELF_SYNC, e);
		}
	}

	// Internals

	private static Path file() {
		return FabricLoader.getInstance().getGameDir().resolve("config").resolve(FILE_NAME);
	}

	private static void ensureLoaded() {
		if (loaded) return;
		loaded = true;
		Path file = file();
		if (!Files.isRegularFile(file)) return;
		Properties p = new Properties();
		try (InputStream in = Files.newInputStream(file)) {
			p.load(in);
		} catch (IOException e) {
			LOGGER.warn("BWG: could not read {}", file, e);
			return;
		}
		Data d = new Data();
		try {
			d.gender = Gender.valueOf(p.getProperty("gender", d.gender.name()).trim());
		} catch (IllegalArgumentException ignored) {
			// keep default
		}
		d.breastSize = readFloat(p, "breastSize", d.breastSize);
		d.breastSeparation = readFloat(p, "breastSeparation", d.breastSeparation);
		d.jiggleDirection = readFloat(p, "jiggleDirection", d.jiggleDirection);
		d.jiggleAmount = readFloat(p, "jiggleAmount", d.jiggleAmount);
		d.jiggleEnabled = Boolean.parseBoolean(p.getProperty("jiggleEnabled", Boolean.toString(d.jiggleEnabled)).trim());
		d.jiggleWithArmor = Boolean.parseBoolean(p.getProperty("jiggleWithArmor", Boolean.toString(d.jiggleWithArmor)).trim());
		d.individualPhysics = Boolean.parseBoolean(p.getProperty("individualPhysics", Boolean.toString(d.individualPhysics)).trim());
		profile = d;
	}

	private static float readFloat(Properties p, String key, float fallback) {
		try {
			float v = Float.parseFloat(p.getProperty(key, "").trim());
			return Float.isFinite(v) ? v : fallback;
		} catch (NumberFormatException e) {
			return fallback;
		}
	}
}
