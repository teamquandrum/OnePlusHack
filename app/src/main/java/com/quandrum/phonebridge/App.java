package com.quandrum.phonebridge;

import android.app.Application;
import android.graphics.Typeface;

import com.parse.Parse;

/**
 * Created by Dmytro Denysenko on 5/6/15.
 */
public class App extends Application {
    private static final String CANARO_EXTRA_BOLD_PATH = "fonts/canaro_extra_bold.otf";
    public static Typeface canaroExtraBold;

    @Override
    public void onCreate() {
        super.onCreate();
        //initTypeface();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "5WyWNRtjgkV6iIT22R0yp4MEmLRLtYKq8C5vcoaF", "pmLLUFNkSdQL9qskqYJfZvGByboygsp5NZsAyQRZ");



    }

    private void initTypeface() {
        canaroExtraBold = Typeface.createFromAsset(getAssets(), CANARO_EXTRA_BOLD_PATH);

    }
}
