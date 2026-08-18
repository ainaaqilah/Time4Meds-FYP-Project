package com.example.time4meds;

// ForgotPasswordActivity: Allows users to request a password reset via email using Firebase Authentication
// and redirects back to LoginActivity

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private Button buttonResetPassword;
    private TextView textViewLogin;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // link all the components to the UI elements
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
        textViewLogin = findViewById(R.id.textViewLogin);

        String text = "Sign In"; // to display the text
        SpannableString spannable = new SpannableString(text); // to style the text

        // Find the start and end of "sign in"
        int start = text.indexOf("Sign In");
        int end = start + "Sign In".length();

        // apply color to Sign in text
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FFB300")),
                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewLogin.setText(spannable); // set styled text to TextView

        mAuth = FirebaseAuth.getInstance();

        // to display the progress dialog while sending password reset link
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending password reset link...");
        progressDialog.setCancelable(false);

        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString().trim();

                // if email is empty
                if (TextUtils.isEmpty(email)) {
                    editTextEmail.setError("Email is required");
                    return;
                }
                sendPasswordResetEmail(email);
            }
        });

        // go to login page
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        progressDialog.show();
        buttonResetPassword.setEnabled(false);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        buttonResetPassword.setEnabled(true);

                        // if successfully, password reset link is sent
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "If this email is registered, a password reset link has been sent. Please check your emai.",
                                    Toast.LENGTH_LONG).show();

                            // redirect to login page
                            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
