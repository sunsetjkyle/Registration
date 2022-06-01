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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etPass, etEmail;
    private Button btnRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();

        //initialize UI

        etEmail = findViewById(R.id.et_email);
        etPass = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

    }

    private void registerUser() {

        //get our strings from edittexts
        String email, password;
        email = etEmail.getText().toString();
        password = etPass.getText().toString();

        //validate email n password

        if (TextUtils.isEmpty(email)){
            etEmail.setError("Please enter your email!");
            return;
        }

        if (TextUtils.isEmpty(password)){
            etPass.setError("Please enter password!");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                            sendVerificationEmail();


                        }else {

                            Toast.makeText(RegistrationActivity.this, "Registration Failed. Try Again!", Toast.LENGTH_SHORT).show();


                        }
                    }
                });

    }

    private void sendVerificationEmail() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {

                        Toast.makeText(RegistrationActivity.this, "Registration successful. Please verify your account!", Toast.LENGTH_SHORT).show();
                        toLoginActivity();
                        mAuth.signOut();

                    }else {

                        Toast.makeText(RegistrationActivity.this, "Registration Failed!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        mAuth.signOut();


                    }
                }
            });
        }
    }

    private void toLoginActivity() {

        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
