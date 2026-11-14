package imbacrian.betterwithgenders.network;

import imbacrian.betterwithgenders.api.Gender;
import imbacrian.betterwithgenders.api.GenderData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class BwgPackets {
	public static final String CHANNEL_GENDER = "BWG:Gender";
	public static final String CHANNEL_JIGGLE = "BWG:Jiggle";

	// Server -> owning client: the server's stored settings for that player (login / respawn)
	public static final String CHANNEL_SELF_SYNC = "BWG:SelfSync";

	private static final byte SELF_SYNC_VERSION = 1;

	private BwgPackets() {}

	// Client -> server. entityId, gender, breastSize, breastSeparation, jiggleDirection
	public static byte[] encodeGender(int entityId, GenderData d) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (DataOutputStream dos = new DataOutputStream(baos)) {
			dos.writeInt(entityId);
			dos.writeUTF(d.bwg$getGender().name());
			dos.writeFloat(d.bwg$getBreastSize());
			dos.writeFloat(d.bwg$getBreastSeparation());
			dos.writeFloat(d.bwg$getJiggleDirection());
		}
		return baos.toByteArray();
	}

	// Client -> server. entityId, direction, amount, ENABLED, WITH_ARMOR, individualPhysics (this order!)
	public static byte[] encodeJiggle(int entityId, GenderData d) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (DataOutputStream dos = new DataOutputStream(baos)) {
			dos.writeInt(entityId);
			dos.writeFloat(d.bwg$getJiggleDirection());
			dos.writeFloat(d.bwg$getJiggleAmount());
			dos.writeBoolean(d.bwg$isJiggleEnabled());
			dos.writeBoolean(d.bwg$isJiggleWithArmor());
			dos.writeBoolean(d.bwg$isIndividualPhysics());
		}
		return baos.toByteArray();
	}

	// Server -> client. Every persistent setting in one payload
	public static byte[] encodeSelfSync(GenderData d) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (DataOutputStream dos = new DataOutputStream(baos)) {
			dos.writeByte(SELF_SYNC_VERSION);
			dos.writeUTF(d.bwg$getGender().name());
			dos.writeFloat(d.bwg$getBreastSize());
			dos.writeFloat(d.bwg$getBreastSeparation());
			dos.writeFloat(d.bwg$getJiggleDirection());
			dos.writeFloat(d.bwg$getJiggleAmount());
			dos.writeBoolean(d.bwg$isJiggleEnabled());
			dos.writeBoolean(d.bwg$isJiggleWithArmor());
			dos.writeBoolean(d.bwg$isIndividualPhysics());
		}
		return baos.toByteArray();
	}

	// false if the payload has an unknown version and was ignored.
	public static boolean applySelfSync(byte[] data, GenderData target) throws IOException {
		try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
			if (dis.readByte() != SELF_SYNC_VERSION) return false;
			String genderName = dis.readUTF();
			float breastSize = dis.readFloat();
			float breastSeparation = dis.readFloat();
			float jiggleDirection = dis.readFloat();
			float jiggleAmount = dis.readFloat();
			boolean jiggleEnabled = dis.readBoolean();
			boolean jiggleWithArmor = dis.readBoolean();
			boolean individualPhysics = dis.readBoolean();

			try {
				target.bwg$setGender(Gender.valueOf(genderName));
			} catch (IllegalArgumentException ignored) {
				// unknown gender name: keep whatever we have
			}
			target.bwg$setBreastSize(breastSize);
			target.bwg$setBreastSeparation(breastSeparation);
			target.bwg$setJiggleDirection(jiggleDirection);
			target.bwg$setJiggleAmount(jiggleAmount);
			target.bwg$setJiggleEnabled(jiggleEnabled);
			target.bwg$setJiggleWithArmor(jiggleWithArmor);
			target.bwg$setIndividualPhysics(individualPhysics);
			return true;
		}
	}
}
