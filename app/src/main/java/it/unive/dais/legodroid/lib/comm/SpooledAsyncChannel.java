package it.unive.dais.legodroid.lib.comm;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class SpooledAsyncChannel implements AsyncChannel {
    @NonNull
    private final Channel channel;
    @NonNull
    private final Executor exec = Executors.newSingleThreadExecutor();

    public SpooledAsyncChannel(@NonNull Channel channel) {
        this.channel = channel;
    }

    @Override
    public void write(Packet p) throws IOException {
        channel.write(p);
    }

    @Override
    public Future<Packet> read() {
        FutureTask<Packet> r = new FutureTask<>(channel::read);
        exec.execute(r);
        return r;
    }


}
