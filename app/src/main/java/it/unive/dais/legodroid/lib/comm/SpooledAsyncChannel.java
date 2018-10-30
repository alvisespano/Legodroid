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
import java.util.concurrent.TimeoutException;

public class SpooledAsyncChannel implements AsyncChannel {

    @NonNull
    private final Channel channel;
    @NonNull
    private final List<MyFuture> q = Collections.synchronizedList(new ArrayList<>());
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
                    for (MyFuture t : q) {
                        if (t.id == r.counter) {
                            t.setReply(r);
                            break;
                        }
                    }
                } catch (IOException | TimeoutException e) {
                    Log.e(TAG, "recoverable exception caught");
                    e.printStackTrace();
                }
            }
            Log.v(TAG, "quitting spooler task");
            return null;
        }
    }

    public class MyFuture implements Future<Reply> {
        private static final long GET_MAX_TIMEOUT = 5000;
        private final int id;
        @Nullable
        private Reply reply = null;

        public MyFuture(int id) {
            this.id = id;
        }

        public synchronized void setReply(Reply r) {
            reply = r;
            notifyAll();
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
        public synchronized boolean isDone() {
            return reply != null;
        }

        @Override
        @NonNull
        public Reply get() throws InterruptedException {
            return get(GET_MAX_TIMEOUT, TimeUnit.MILLISECONDS);
        }

        @NonNull
        @Override
        public synchronized Reply get(long l, @NonNull TimeUnit timeUnit) throws InterruptedException {
            if (reply == null)
                wait(timeUnit.toMillis(l));
            assert reply != null;
            return reply;
        }
    }

    @Override
    @NonNull
    public MyFuture send(@NonNull Command cmd) throws IOException {
        channel.write(cmd);
        MyFuture r = new MyFuture(cmd.getCounter());
        q.add(r);
        return r;
    }

    @NonNull
    @Override
    public MyFuture send(int reservation, @NonNull Bytecode bc) throws IOException {
        return send(new Command(true, 0, reservation, bc.getBytes()));
    }

    @Override
    public void sendNoReply(@NonNull Bytecode bc) throws IOException {
        channel.write(new Command(false, 0, 0, bc.getBytes()));
    }


}
