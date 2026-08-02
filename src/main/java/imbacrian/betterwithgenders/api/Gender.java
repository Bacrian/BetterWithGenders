package imbacrian.betterwithgenders.api;

public enum Gender {
	MALE, FEMALE, OTHER;

	public Gender next() {
		Gender[] v = values();
		return v[(this.ordinal() + 1) % v.length];
	}
}
