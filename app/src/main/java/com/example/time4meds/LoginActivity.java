package com.example.time4meds;

// LoginActivity: Handles user login with Firebase Authentication
// allows password visibility toggle
// and provides navigation to registration or password reset pages

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerLink, forgotPasswordLink;
    private ImageView togglePasswordLogin;
    private FirebaseAuth auth;
    private boolean isPasswordVisible = false; // starts with close eyes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance(); // to connect to firebase authentication

        // link all the components to the UI elements
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        String text = "Sign Up"; // to display the text
        SpannableString spannable = new SpannableString(text); // to style the text

        // find the start and end of "Sign Up"
        int start = text.indexOf("Sign Up");
        int end = start + "Sign Up".length();

        // apply color to Sign up text
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FFB300")),
                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        registerLink.setText(spannable); // set styled text to TextView

        // link all the components to the UI elements
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
        togglePasswordLogin = findViewById(R.id.togglePasswordLogin);

        // for toggle password visibility
        togglePasswordLogin.setOnClickListener(v -> { // attach listener - codes that react when action happens (clicking)
            if (isPasswordVisible) { // to check current state of the icon
                // to hide password
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                togglePasswordLogin.setImageResource(R.drawable.ic_eye_closed);

            } else {
                // to show password
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                togglePasswordLogin.setImageResource(R.drawable.ic_eye_open);
            }
            isPasswordVisible = !isPasswordVisible;
            passwordEditText.setSelection(passwordEditText.length());
        });

        loginButton.setOnClickListener(v -> { // login button to logging account
            // to removes any extra spaces, and stores them in variables
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // check for any empty fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) { // if successfully logged in
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, ProfileListActivity.class);
                            intent.putExtra("SHOW_WELCOME_POPUP", true);
                            startActivity(intent);
                            finish();

                        } else { // if failing to login
                            Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // go to register page
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // go to forgot password page
        forgotPasswordLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    // when launching app, if already logged in, no need to login again just go directly to profile list page
    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, ProfileListActivity.class));
            finish();
        }
    }
}
