package com.example.time4meds;

// RegisterActivity: Handles user registration with Firebase Authentication
// enforces strong password rules
// allows password visibility toggle
// and navigates to ProfileListActivity upon successful registration

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    // all the components used for user input and navigation
    private EditText editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLogin;
    private FirebaseAuth mAuth; // for firebase authentication instance
    private ImageView togglePassword, toggleConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // restore any saved data
        setContentView(R.layout.activity_register); // set the register screen to be displayed
        mAuth = FirebaseAuth.getInstance(); // initializes Firebase Authentication instance so it can manage user login and authentication

        // link all the components to the UI elements
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);

        String text = "Sign in"; // to display the text
        SpannableString spannable = new SpannableString(text); // to style the text

        // Find the start and end of "Sign in"
        int start = text.indexOf("Sign in");
        int end = start + "Sign in".length();

        // Apply color to Sign in text
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FFB300")),
                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewLogin.setText(spannable); // set styled text to TextView

        // link eye icons yang for show/hide password to UI elements
        togglePassword = findViewById(R.id.togglePassword);
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);

        // ================= in the login & register page =================
        togglePassword.setOnClickListener(v -> { // attach listener - codes that react when action happens (clicking eye icon)

            // to check if password is currently hidden
            if (editTextPassword.getTransformationMethod().equals(android.text.method.PasswordTransformationMethod.getInstance())) {

                // to show password
                editTextPassword.setTransformationMethod(
                        android.text.method.HideReturnsTransformationMethod.getInstance());
                togglePassword.setImageResource(R.drawable.ic_eye_open);

            } else {
                // hide password
                editTextPassword.setTransformationMethod(
                        android.text.method.PasswordTransformationMethod.getInstance());
                togglePassword.setImageResource(R.drawable.ic_eye_closed);
            }
            editTextPassword.setSelection(editTextPassword.getText().length()); // to make sure the cursor stays at the end of the words even after click eye icon
        });

        // ================= in the forgot password page =================
        toggleConfirmPassword.setOnClickListener(v -> { // attach listener - codes that react when action happens (clicking eye icon)

            //check if betul hidden
            if (editTextConfirmPassword.getTransformationMethod().equals(android.text.method.PasswordTransformationMethod.getInstance())) {
                // to show password
                editTextConfirmPassword.setTransformationMethod(
                        android.text.method.HideReturnsTransformationMethod.getInstance());
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_open);

            } else {
                // to hide password
                editTextConfirmPassword.setTransformationMethod(
                        android.text.method.PasswordTransformationMethod.getInstance());
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_closed);
            }
            editTextConfirmPassword.setSelection(editTextConfirmPassword.getText().length());
        });

        // register button to register user
        buttonRegister.setOnClickListener(v -> registerUser());

        // go to login screen when "Sign in" text is clicked
        textViewLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() { // to removes any extra spaces, and stores them in variables
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // check for any empty fields
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // check if password match with the first password inserted
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // check for the password requirements
        if (!isStrongPassword(password)) {
            Toast.makeText(this,
                    "Password must include uppercase, lowercase, number, symbol, and be at least 8 characters.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // to display progress dialog while registering
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // to create user in firebase authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss(); // to hide the loading dialog

                    // if successfully registered
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

                        // to go profile list page
                        Intent intent = new Intent(RegisterActivity.this, ProfileListActivity.class);
                        intent.putExtra("SHOW_WELCOME_POPUP", true);
                        startActivity(intent);
                        finish();

                    } else {
                        // if failing to register
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // setup security requirements untuk password (uppercase, lowercase, number, special char, min 8 chars)
    private boolean isStrongPassword(String password) {
        String pattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        return password.matches(pattern);
    }
}



