package com.example.time4meds;

// SplashActivity: Displays the app's splash screen with animation
// handles notification permission (Android 13+)
// and navigates to the LoginActivity after a delay

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.airbnb.lottie.LottieAnimationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 8000; // set the splash screen to be in 8 seconds
    private static final int NOTIFICATION_PERMISSION_REQUEST = 101; // this is for notification permission

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // restore any saved data
        setContentView(R.layout.activity_splash); // set the splash screen to be displayed

        // for the fade-in animation
        LottieAnimationView lottie = findViewById(R.id.lottieSplash);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        lottie.startAnimation(fadeIn);



        // to request notification permission for Android version 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST);
            }
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // for fade out (Lottie animation)
            lottie.animate()
                    .alpha(0f) // this one for fade out to become transparent
                    .setDuration(100)      // 0.1 seconds
                    .withEndAction(() -> {
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent); // navigate to login page
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // apply the fade transition
                        finish(); // close the splash
                    });
        }, SPLASH_TIME_OUT);

    }
}
