package it.unive.dais.legodroid.lib;

import it.unive.dais.legodroid.lib.lowlevel.Connector;
import it.unive.dais.legodroid.lib.lowlevel.PacketManager;
import it.unive.dais.legodroid.lib.lowlevel.Protocol;
import it.unive.dais.legodroid.lib.sensors.TouchSensor;
import it.unive.dais.legodroid.lib.util.Consumer;

import java.io.IOException;

public class EV3 {
    private PacketManager packetManager;

    public EV3(Connector connector) {
        this.packetManager = new PacketManager(connector);
    }

    public PacketManager getPacketManager() {
        return packetManager;
    }

    public void soundTone(int volume, int freq, int duration) throws IOException {
        Protocol.soundTone(packetManager, volume, freq, duration);
    }

    public void run(Consumer<Api> c) {
        
    }
}
