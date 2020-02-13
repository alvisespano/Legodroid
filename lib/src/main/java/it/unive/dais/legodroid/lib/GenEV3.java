package it.unive.dais.legodroid.lib;

import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import it.unive.dais.legodroid.lib.comm.AsyncChannel;
import it.unive.dais.legodroid.lib.comm.Channel;
import it.unive.dais.legodroid.lib.comm.SpooledAsyncChannel;
import it.unive.dais.legodroid.lib.util.Consumer;
import it.unive.dais.legodroid.lib.util.Function;

import static it.unive.dais.legodroid.lib.util.Prelude.ReTAG;

/**
 * Instances of this class represent LEGO Mindstorms EV3 bricks: a program shall create one instance of this class for each physical brick it controls.
 * An object of type AsyncChannel is required for calling the constructor, making this class instantiable only once a connection with the brick has been established.
 *
 * @param <A> a generic abstracting the Api type: users willing to extend the Api class can provide custom subclasses as type argument.
 */
public class GenEV3<A extends EV3.Api> {
    private static final String TAG = ReTAG("GenEV3");

    /**
     * Specialized exception subclass.
     */
    public static class AlreadyRunningException extends ExecutionException {
        public AlreadyRunningException(String msg) {
            super(msg);
        }
    }

    @NonNull
    protected final AsyncChannel channel;

    @Nullable
    private AsyncTask<Void, Void, Void> task = null;

    /**
     * Main constructor.
     *
     * @param channel an asynchronous channel object of type AsyncChannel.
     */
    public GenEV3(@NonNull AsyncChannel channel) {
        this.channel = channel;
    }

    /**
     * Facility constructor. Creates a SpooledAsyncChannel automatically with the given synchrounous channel
     *
     * @param channel a synchrounous channel object.
     */
    public GenEV3(@NonNull Channel channel) {
        this(new SpooledAsyncChannel(channel));
    }

    /**
     * Run the legoMain callback as the main program for the EV3 brick.
     * The callback is a function parametric over an object of type A and returning nothing, hence the Consumer type.
     * Programmers can control the EV3 brick by calling methods of the Api object passed as argument.
     * Notably, there is no other way of getting the Api object for safety reasons.
     * The callback is executed by a worker thread, thus any operation on the UI must be delegated to runOnUiThread() invocations.
     * Also, each GenEV3 instance can have at most one running task - i.e. one worker thread can be up at any given time.
     *
     * @param legoMain functional object that takes a parameter of type Api and has no return type.
     * @param makeApi  functional object that constructs an Api object given an GenEV3 object; the return Api object is passed as argument to the legoMain function object.
     * @throws AlreadyRunningException thrown when the worker thread is already up.
     */
    public synchronized void run(@NonNull Consumer<A> legoMain, @NonNull Function<GenEV3<A>, A> makeApi) throws AlreadyRunningException {
        if (task != null)
            throw new AlreadyRunningException("GenEV3 task is already running");
        task = new MyAsyncTask<>(this, legoMain, makeApi).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // inner async task
    private static class MyAsyncTask<A extends EV3.Api> extends AsyncTask<Void, Void, Void> {
        private final String TAG = ReTAG(GenEV3.TAG, "AsyncTask");

        @NonNull
        private final GenEV3<A> ev3;
        @NonNull
        private final Consumer<A> main;
        @NonNull
        private final Function<GenEV3<A>, A> make;

        private MyAsyncTask(@NonNull GenEV3<A> ev3, @NonNull Consumer<A> main, @NonNull Function<GenEV3<A>, A> make) {
            this.ev3 = ev3;
            this.main = main;
            this.make = make;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Thread.currentThread().setName(TAG);
            Log.v(TAG, "starting EV3 task");
            try {
                main.call(make.apply(ev3));
                Log.v(TAG, "exiting EV3 task");
            } catch (Throwable e) {
                Log.e(TAG, String.format("uncaught exception: %s. Aborting EV3 task", e.getMessage()));
                e.printStackTrace();
            }
            synchronized (ev3) {
                ev3.task = null;
            }
            return null;
        }
    }

    /**
     * Cancel the GenEV3 task currently being run by the worker thread.
     * Cancellation is not equivalent to killing the thread, it is just a software flag the task code can peek.
     * This method is thread-safe and can be either called from the callback or from any other thread.
     *
     * @see #isCancelled()
     */
    public synchronized void cancel() {
        if (task != null) {
            Log.v(TAG, "cancelling task");
            task.cancel(true);
        }
    }

    /**
     * Test the cancellation flag. This is meant to be called from within the GenEV3 task callback.
     *
     * @return returns true when a call to cancel() has been performed.
     * @see #cancel()
     */
    public synchronized boolean isCancelled() {
        return task == null || task.isCancelled();
    }

}
