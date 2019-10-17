package it.unive.dais.legodroid.lib.comm;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static it.unive.dais.legodroid.lib.util.Prelude.ReTAG;

/**
 * This class implements an asynchronous channel that sends commands and receives replies via a spooler thread.
 *
 * @see AsyncChannel
 */
public class SpooledAsyncChannel implements AsyncChannel {

    @NonNull
    private final Channel channel;
    @NonNull
    private final List<FutureReply> q = Collections.synchronizedList(new ArrayList<>());
    @NonNull
    private final SpoolerTask task;

    /**
     * Create an asynchronous channel given a synchrounous channel.
     *
     * @param channel a synchrounous channel.
     */
    public SpooledAsyncChannel(@NonNull Channel channel) {
        this.channel = channel;
        this.task = new SpoolerTask(channel, q);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void close() {
        task.cancel(true);
    }

    private static class SpoolerTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = ReTAG("SpoolerTask");
        private static final int MAX_RETRIES = 5;

        @NonNull
        private final Channel channel;
        @NonNull
        private final List<FutureReply> q;

        private SpoolerTask(@NonNull Channel ch, @NonNull List<FutureReply> q) {
            this.channel = ch;
            this.q = q;
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected Void doInBackground(Void... voids) {
            Log.v(TAG, "spooler task started");
            Thread.currentThread().setName(TAG);
            int retries = MAX_RETRIES;
            @NonNull String cause = "cancellation";
            @Nullable Throwable last = null;
            while (!isCancelled()) {
                try {
                    Reply r = channel.receive();
                    synchronized (q) {
                        for (FutureReply t : q) {
                            if (t.id == r.getCounter()) {
                                t.complete(r);
                                break;
                            }
                        }
                    }
                    retries = MAX_RETRIES;
                } catch (Throwable e) {
                    Log.e(TAG, String.format("recoverable exception caught: %s", e));
                    e.printStackTrace();
                    if (e.equals(last)) {
                        if (retries-- > 0)
                            Log.e(TAG, String.format("retries left: %d", retries));
                        else {
                            cause = String.format("max retries (%d) reached for exception %s", MAX_RETRIES, e.getMessage());
                            break;
                        }
                    } else retries = MAX_RETRIES;
                    last = e;
                }
            }
            Log.v(TAG, String.format("spooler task quitting due to %s", cause));
            return null;
        }
    }

    /**
     * This class extends {@link CompletableFuture} over a {@link Reply} object specifically tailored for the {@link SpooledAsyncChannel} class.
     *
     * @see Future
     * @see CompletableFuture
     */
    static class FutureReply extends CompletableFuture<Reply> {
        private final int id;
        private FutureReply(int id) {
            this.id = id;
        }
    }

    @Override
    @NonNull
    public CompletableFuture<Reply> send(@NonNull Command cmd) throws IOException {
        channel.send(cmd);
        FutureReply r = new FutureReply(cmd.getCounter());
        q.add(r);
        return r;
    }

    @NonNull
    @Override
    public CompletableFuture<Reply> send(int reservation, @NonNull Bytecode bc) throws IOException {
        return send(new Command(true, 0, reservation, bc.getBytes()));
    }

    @Override
    public void sendNoReply(@NonNull Bytecode bc) throws IOException {
        channel.send(new Command(false, 0, 0, bc.getBytes()));
    }

}
