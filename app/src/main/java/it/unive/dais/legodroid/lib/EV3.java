package it.unive.dais.legodroid.lib;

import it.unive.dais.legodroid.lib.lowlevel.Connector;
import it.unive.dais.legodroid.lib.lowlevel.PacketManager;
import it.unive.dais.legodroid.lib.lowlevel.Protocol;

public class EV3 {
    private PacketManager packetManager;

    public EV3(Connector connector) {
        this.packetManager = new PacketManager(connector);
    }

    public void soundTone(int volume, int freq, int duration) {
        Protocol.soundTone(packetManager, volume, freq, duration);
    }

    public PacketManager getPacketManager() {
        return packetManager;
    }
}
