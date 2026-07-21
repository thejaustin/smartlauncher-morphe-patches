package rikka.shizuku;

import android.content.pm.PackageManager;
import java.io.InputStream;

/**
 * Compile-time stub for Rikka Shizuku API.
 */
public class Shizuku {

    public static boolean pingBinder() {
        throw new UnsupportedOperationException("Stub");
    }

    public static int checkSelfPermission() {
        throw new UnsupportedOperationException("Stub");
    }

    public static void requestPermission(int requestCode) {
        throw new UnsupportedOperationException("Stub");
    }

    public static Process newProcess(String[] cmd, String[] env, String dir) {
        throw new UnsupportedOperationException("Stub");
    }

    public interface OnRequestPermissionResultListener {
        void onRequestPermissionResult(int requestCode, int grantResult);
    }
}
