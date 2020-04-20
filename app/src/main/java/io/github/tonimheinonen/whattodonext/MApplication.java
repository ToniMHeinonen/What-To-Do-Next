package io.github.tonimheinonen.whattodonext;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Runs once when app starts, sets offline persistence for Firebase.
 * @author Toni Heinonen
 * @author toni1.heinonen@gmail.com
 * @version 1.0.2
 * @since 1.0.2
 */
public class MApplication extends Application {

    /**
     * Initializes offline persistence for Firebase.
     *
     * This only needs to be called once and before any other Firebase call,
     * otherwise the app crashes.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
