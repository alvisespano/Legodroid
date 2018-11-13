package it.unive.dais.legodroid.lib.comm;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static it.unive.dais.legodroid.lib.util.Prelude.ReTAG;

public class SpooledAsyncChannel implements AsyncChannel {

    @NonNull
    private final Channel channel;
    @NonNull
    private final List<FutureReply> q = Collections.synchronizedList(new ArrayList<>());
    @NonNull
    private final SpoolerTask task;

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

        public SpoolerTask(@NonNull Channel ch, @NonNull List<FutureReply> q) {
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
                    Reply r = channel.read();
                    synchronized (q) {
                        for (FutureReply t : q) {
                            if (t.id == r.getCounter()) {
                                t.setReply(r);
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
                    }
                    else retries = MAX_RETRIES;
                    last = e;
                }
            }
            Log.v(TAG, String.format("spooler task quitting due to %s", cause));
            return null;
        }
    }

    public class FutureReply implements Future<Reply> {
        private static final long GET_MAX_TIMEOUT_MS = 5000;
        private final int id;
        @NonNull
        private final Lock lock = new ReentrantLock();
        @NonNull
        private final Condition cond = lock.newCondition();
        @Nullable
        private Reply reply = null;

        public FutureReply(int id) {
            this.id = id;
        }

        private void setReply(Reply r) {
            lock.lock();
            try {
                reply = r;
                cond.signalAll();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean cancel(boolean b) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            lock.lock();
            try {
                return reply != null;
            } finally {
                lock.unlock();
            }
        }

        @Override
        @NonNull
        public Reply get() throws InterruptedException {
            return get(GET_MAX_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        }

        @NonNull
        @Override
        public Reply get(long l, @NonNull TimeUnit timeUnit) throws InterruptedException {
            lock.lock();
            try {
                if (reply == null)
                    cond.await(l, timeUnit);
                assert reply != null;
                return reply;
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    @NonNull
    public FutureReply send(@NonNull Command cmd) throws IOException {
        channel.write(cmd);
        FutureReply r = new FutureReply(cmd.getCounter());
        q.add(r);
        return r;
    }

    @NonNull
    @Override
    public FutureReply send(int reservation, @NonNull Bytecode bc) throws IOException {
        return send(new Command(true, 0, reservation, bc.getBytes()));
    }

    @Override
    public void sendNoReply(@NonNull Bytecode bc) throws IOException {
        channel.write(new Command(false, 0, 0, bc.getBytes()));
    }


}
