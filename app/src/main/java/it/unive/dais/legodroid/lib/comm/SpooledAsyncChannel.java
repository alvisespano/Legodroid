package it.unive.dais.legodroid.lib.comm;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SpooledAsyncChannel implements AsyncChannel {

    @NonNull
    private final Channel channel;
    @NonNull
    private final List<FutureReply> q = Collections.synchronizedList(new ArrayList<>());
    @NonNull
    private final SpoolerTask task;

    public SpooledAsyncChannel(@NonNull Channel channel) {
        this.channel = channel;
        this.task = new SpoolerTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void close() {
        task.cancel(true);
    }

    @SuppressLint("StaticFieldLeak")
    private class SpoolerTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "SpoolerTask";

        @Override
        protected Void doInBackground(Void... voids) {
            Log.v(TAG, "starting spooler task");
            Thread.currentThread().setName(TAG);
            while (!isCancelled()) {
                try {
                    Reply r = channel.read();
                    synchronized (q) {
                        for (FutureReply t : q) {
                            if (t.id == r.counter) {
                                t.setReply(r);
                                break;
                            }
                        }
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "recoverable exception caught: %s");
                    e.printStackTrace();
                }
            }
            Log.v(TAG, "quitting spooler task");
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
                    cond.await(); //(l, timeUnit);
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
