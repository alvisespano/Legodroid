package it.unive.dais.legodroid.lib.util;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.ExecutionException;

public class AsyncTaskResult<R> {

    private static final String TAG = "AsyncTaskResult";

    protected final AsyncTask<?, ?, Result<R>> task;

    public AsyncTaskResult(AsyncTask<?, ?, Result<R>> t) {
        this.task = t;
    }

    public Result<R> get() throws ExecutionException, InterruptedException {
        return task.get();
    }

    public boolean hasResult() throws ExecutionException, InterruptedException {
        return get().hasResult();
    }

    @Nullable
    public R getResult() throws ExecutionException, InterruptedException {
        return get().getResult();
    }

    @Nullable
    public Exception getException() throws ExecutionException, InterruptedException {
        return get().getException();
    }

    static class Result<R> {

        @Nullable
        private R result;
        @Nullable
        private Exception exn;

        Result(@Nullable R x) {
            result = x;
            exn = null;
        }

        Result(@NonNull Exception e) {
            exn = e;
            result = null;
        }

        public boolean hasResult() {
            return exn != null;
        }

        @Nullable
        public R getResult() {
            return result;
        }

        @NonNull
        public Exception getException() {
            if (exn != null) return exn;
            else throw new UnexpectedException("AsyncTask has result and no exception");
        }
    }

    // async task quick wrappers
    //

    @SuppressWarnings("unchecked")
    @NonNull
    public static <T, R> AsyncTaskResult<R> run(@NonNull Function<T, R> f, @Nullable T x) {
        return new AsyncTaskResult<>(new AsyncTask<T, Void, Result<R>>() {
            @Override
            protected AsyncTaskResult.Result<R> doInBackground(T... xs) {
                try {
                    final T x = xs[0];
                    Log.d(TAG, String.format("computing function application: %s(%s)", f, x));
                    final R r = f.apply(x);
                    Log.d(TAG, String.format("computation finished: %s(%s)", f, x));
                    return new AsyncTaskResult.Result<>(r);
                } catch (Exception e) {
                    return new AsyncTaskResult.Result<>(e);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, x));
    }

    public static <R> AsyncTaskResult<R> run(@NonNull Function<Void, R> f) {
        return run(f, null);
    }

    public static void run(Runnable r) {
        run(x -> {
            r.run();
            return null;
        });
    }

}
