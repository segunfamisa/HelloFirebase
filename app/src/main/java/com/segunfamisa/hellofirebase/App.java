package com.segunfamisa.hellofirebase;

import android.app.Application;

import com.google.firebase.crash.FirebaseCrash;

/**
 * Created by segun.famisa on 26/05/2016.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                FirebaseCrash.report(ex);
            }
        });
    }
}
