package io.github.tonimheinonen.whattodonext;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

public class Debug {
    private static int DEBUG_LEVEL = 1;
    public static boolean showOnUI;

    /**
     * Prints debug message.
     * @param callerClass current context
     * @param methodName name of the current method
     * @param message message to print
     * @param level debug level
     */
    public static void print(Context callerClass, String methodName, String message, int level) {
        if (BuildConfig.DEBUG && level <= DEBUG_LEVEL) {
            String msg = methodName + ", " + message;
            String className = callerClass.getClass().getSimpleName();

            if (!showOnUI) {
                Log.d(className, msg);
            } else {
                CharSequence text = className + ": " + msg;

                Toast toast = Toast.makeText(callerClass, text, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    /**
     * Prints debug message without context.
     * @param callerClass name of the current class
     * @param methodName name of the current method
     * @param message message to print
     * @param level debug level
     */
    public static void print(String callerClass, String methodName, String message, int level) {
        if (BuildConfig.DEBUG && level <= DEBUG_LEVEL) {
            String msg = methodName + ", " + message;
            Log.d(callerClass, msg);
        }
    }

    /**
     * Loads debug level from resources.
     * @param host current context
     */
    public static void loadDebug(Context host) {
        Resources res = host.getResources();
        DEBUG_LEVEL = res.getInteger(R.integer.debug_level);
    }
}
