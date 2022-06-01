package com.ubunifu.appclone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {

    String email;

    private EditText etEmail;
    private Button btnSendEmail;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        mAuth = FirebaseAuth.getInstance();

        email = getIntent().getStringExtra("email");

        etEmail = findViewById(R.id.editTextTextEmailAddress);
        btnSendEmail = findViewById(R.id.sendEmailBtn);

        etEmail.setText(email);



        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String email;
        email = etEmail.getText().toString();

        //validate email n password

        if (TextUtils.isEmpty(email)){
            etEmail.setError("Please enter your email!");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(PasswordResetActivity.this, "Check your email to reset password!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PasswordResetActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {

                            Toast.makeText(PasswordResetActivity.this, "Unable to reset your password!", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
}
