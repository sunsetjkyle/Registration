package com.ubunifu.appclone;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etPass, etEmail;
    private Button btnRegister,btnLogin;
    private TextView forgotPass;

    private FirebaseAuth mAuth;
    String email = "";

    private Boolean emailAddressChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        //initialize UI
        forgotPass = findViewById(R.id.forgot_password);
        etEmail = findViewById(R.id.edittext_email);
        etPass = findViewById(R.id.edittext_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.button);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);

            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });


        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toResetPass();

            }
        });


    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {

            // User is signed in
            sendUserToMainActivity();

        } else {
            // No user is signed in
        }
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void loginUser() {

        //get our strings from edittexts
        email = etEmail.getText().toString();
        String password = etPass.getText().toString();

        //validate email n password

        if (TextUtils.isEmpty(email)){
            etEmail.setError("Please enter your email!");
            return;
        }

        if (TextUtils.isEmpty(password)){
            etPass.setError("Please enter password!");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if (task.isSuccessful()){

                            VerifyEmailAddress();


                        }else {

                            Toast.makeText(LoginActivity.this, "Login Failed. Try Again!", Toast.LENGTH_SHORT).show();


                        }
                    }
                });
    }

    private void VerifyEmailAddress() {

        FirebaseUser user = mAuth.getCurrentUser();
        emailAddressChecker = user.isEmailVerified();

        if (emailAddressChecker){
            Toast.makeText(LoginActivity.this, "Successfully Logged In!", Toast.LENGTH_SHORT).show();
            sendUserToMainActivity();
        } else {

            Toast.makeText(this, "Please Verify your Account!", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        }

    }

    private void toResetPass() {
        Intent intent = new Intent(LoginActivity.this, PasswordResetActivity.class);
        intent.putExtra("email", email);
        Toast.makeText(LoginActivity.this, "Your email address is: " + email, Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

}
