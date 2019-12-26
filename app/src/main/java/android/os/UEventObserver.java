package android.os;

/**
 * Created by Jax on 2019-12-25.
 * Description :
 * Version : V1.0.0
 */

import java.util.ArrayList;
import java.util.HashMap;

public abstract class UEventObserver {
    private static final String TAG = "UEventObserver";
    private static final boolean DEBUG = false;
    private static UEventObserver.UEventThread sThread;

    private static native void nativeSetup();

    private static native String nativeWaitForNextEvent();

    private static native void nativeAddMatch(String var0);

    private static native void nativeRemoveMatch(String var0);

    public UEventObserver() {
    }

    protected void finalize() throws Throwable {
        try {
            this.stopObserving();
        } finally {
            super.finalize();
        }

    }

    private static UEventObserver.UEventThread getThread() {
        synchronized (UEventObserver.class) {
            if (sThread == null) {
                sThread = new UEventObserver.UEventThread();
                sThread.start();
            }

            return sThread;
        }
    }

    private static UEventObserver.UEventThread peekThread() {
        synchronized (UEventObserver.class) {
            return sThread;
        }
    }

    public final void startObserving(String match) {
        if (match != null && !match.isEmpty()) {
            UEventObserver.UEventThread t = getThread();
            t.addObserver(match, this);
        } else {
            throw new IllegalArgumentException("match substring must be non-empty");
        }
    }

    public final void stopObserving() {
        UEventObserver.UEventThread t = getThread();
        if (t != null) {
            t.removeObserver(this);
        }

    }

    public abstract void onUEvent(UEventObserver.UEvent var1);

    private static final class UEventThread extends Thread {
        private final ArrayList<Object> mKeysAndObservers = new ArrayList();
        private final ArrayList<UEventObserver> mTempObserversToSignal = new ArrayList();

        public UEventThread() {
            super("UEventObserver");
        }

        public void run() {
            UEventObserver.nativeSetup();

            while (true) {
                String message;
                do {
                    message = UEventObserver.nativeWaitForNextEvent();
                } while (message == null);

                this.sendEvent(message);
            }
        }

        private void sendEvent(String message) {
            int N;
            int i;
            synchronized (this.mKeysAndObservers) {
                N = this.mKeysAndObservers.size();

                for (i = 0; i < N; i += 2) {
                    String key = (String) this.mKeysAndObservers.get(i);
                    if (message.contains(key)) {
                        UEventObserver observer = (UEventObserver) this.mKeysAndObservers.get(i + 1);
                        this.mTempObserversToSignal.add(observer);
                    }
                }
            }

            if (!this.mTempObserversToSignal.isEmpty()) {
                UEventObserver.UEvent event = new UEventObserver.UEvent(message);
                N = this.mTempObserversToSignal.size();

                for (i = 0; i < N; ++i) {
                    UEventObserver observer = this.mTempObserversToSignal.get(i);
                    observer.onUEvent(event);
                }

                this.mTempObserversToSignal.clear();
            }

        }

        public void addObserver(String match, UEventObserver observer) {
            synchronized (this.mKeysAndObservers) {
                this.mKeysAndObservers.add(match);
                this.mKeysAndObservers.add(observer);
                UEventObserver.nativeAddMatch(match);
            }
        }

        public void removeObserver(UEventObserver observer) {
            synchronized (this.mKeysAndObservers) {
                int i = 0;

                while (i < this.mKeysAndObservers.size()) {
                    if (this.mKeysAndObservers.get(i + 1) == observer) {
                        this.mKeysAndObservers.remove(i + 1);
                        String match = (String) this.mKeysAndObservers.remove(i);
                        UEventObserver.nativeRemoveMatch(match);
                    } else {
                        i += 2;
                    }
                }

            }
        }
    }

    public static final class UEvent {
        private final HashMap<String, String> mMap = new HashMap();

        public UEvent(String message) {
            int offset = 0;

            int at;
            for (int length = message.length(); offset < length; offset = at + 1) {
                int equals = message.indexOf(61, offset);
                at = message.indexOf(0, offset);
                if (at < 0) {
                    break;
                }

                if (equals > offset && equals < at) {
                    this.mMap.put(message.substring(offset, equals), message.substring(equals + 1, at));
                }
            }

        }

        public String get(String key) {
            return this.mMap.get(key);
        }

        public String get(String key, String defaultValue) {
            String result = this.mMap.get(key);
            return result == null ? defaultValue : result;
        }

        public String toString() {
            return this.mMap.toString();
        }
    }
}
