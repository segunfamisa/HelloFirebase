package com.segunfamisa.hellofirebase;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    /**
     * Firebase Remote Config
     */
    FirebaseRemoteConfig mRemoteConfig;

    //remote config fields
    private static final String CONFIG_SIGNUP_PROMPT = "signup_prompt";
    private static final String CONFIG_MIN_PASSWORD_LEN = "min_password_length";
    private static final String CONFIG_IS_PROMO_ON = "is_promotion_on";
    private static final String CONFIG_COLOR_PRY = "color_primary";
    private static final String CONFIG_COLOR_PRY_DARK = "color_primary_dark";


    private Toolbar toolbar;
    private EditText mEditUsername;
    private EditText mEditEmail;
    private EditText mEditPassword;
    private EditText mEditPromoCode;
    private TextView mTextPrompt;
    private Button mButtonSignup;

    private int minPasswordLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mEditUsername = (EditText) findViewById(R.id.edit_username);
        mEditEmail = (EditText) findViewById(R.id.edit_email);
        mEditPassword = (EditText) findViewById(R.id.edit_password);
        mTextPrompt = (TextView) findViewById(R.id.textview_signup_prompt);
        mEditPromoCode = (EditText) findViewById(R.id.edit_promo_code);
        mButtonSignup = (Button) findViewById(R.id.button_signup);

        setSupportActionBar(toolbar);

        initRemoteConfig();

        setupView();
    }

    private void initRemoteConfig() {
        mRemoteConfig = FirebaseRemoteConfig.getInstance();

        Resources res = getResources();

        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put("signup_prompt", getString(R.string.config_signup_prompt));
        defaults.put("min_password_length", res.getInteger(R.integer.config_min_password_len));
        defaults.put("is_promotion_on", res.getBoolean(R.bool.config_promo_on));
        defaults.put("color_primary", getString(R.string.config_color_pry));
        defaults.put("color_primary_dark", getString(R.string.config_color_pry_dark));


        mRemoteConfig.setDefaults(defaults);
        FirebaseRemoteConfigSettings remoteConfigSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        mRemoteConfig.setConfigSettings(remoteConfigSettings);
        fetchRemoteConfigValues();
    }

    private void setupView() {
        setToolbarColor();
        setStatusBarColor();
        setSignupPrompt();
        setPromoCode();
        minPasswordLength = (int) mRemoteConfig.getLong(CONFIG_MIN_PASSWORD_LEN);

        mButtonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    Toast.makeText(getApplicationContext(), R.string.toast_sign_up, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateInput() {
        // insert validation for other fields

        //this is the password field we want to configure
        if (mEditPassword.getText().toString().length() < minPasswordLength) {
            mEditPassword.setError(String.format(getString(R.string.error_short_password), minPasswordLength));
            return false;
        } else {
            mEditPassword.setError(null);
            return true;
        }
    }

    private void fetchRemoteConfigValues() {
        long cacheExpiration = 3600;

        //expire the cache immediately for development mode.
        if (mRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        mRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            // task successful. Activate the fetched data
                            mRemoteConfig.activateFetched();
                            setupView();
                        } else {
                            //task failed
                        }
                    }
                });
    }

    /**
     * Sets the signup prompt
     */
    private void setSignupPrompt() {
        String prompt = mRemoteConfig.getString(CONFIG_SIGNUP_PROMPT);
        if (prompt != null) {
            mTextPrompt.setText(prompt);
        }
    }

    /**
     * Sets or hides the promo code field based on whether or not the promo is on
     */
    private void setPromoCode() {
        boolean isPromoOn = mRemoteConfig.getBoolean(CONFIG_IS_PROMO_ON);
        mEditPromoCode.setVisibility(isPromoOn ? View.VISIBLE : View.GONE);
    }

    /**
     * Sets the value of the toolbar color.
     */
    private void setToolbarColor() {
        boolean isPromoOn = mRemoteConfig.getBoolean(CONFIG_IS_PROMO_ON);
        int color = isPromoOn ? Color.parseColor(mRemoteConfig.getString(CONFIG_COLOR_PRY)) :
                ContextCompat.getColor(this, R.color.colorPrimary);

        toolbar.setBackgroundColor(color);
    }

    /**
     * Sets the status bar color
     */
    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean isPromoOn = mRemoteConfig.getBoolean(CONFIG_IS_PROMO_ON);
            int color = isPromoOn ? Color.parseColor(mRemoteConfig.getString(CONFIG_COLOR_PRY_DARK)) :
                    ContextCompat.getColor(this, R.color.colorPrimaryDark);

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
