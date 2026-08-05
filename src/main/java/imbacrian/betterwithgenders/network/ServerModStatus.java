package imbacrian.betterwithgenders.network;

public class ServerModStatus {
    private static boolean serverHasMod = false;

    public static boolean doesServerHaveMod() {
        return serverHasMod;
    }

    public static void setServerHasMod(boolean hasMod) {
        serverHasMod = hasMod;
    }

    public static void reset() {
        serverHasMod = false;
    }
}
