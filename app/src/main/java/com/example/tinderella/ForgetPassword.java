package com.example.tinderella;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import org.jetbrains.annotations.NotNull;

public class ForgetPassword extends AppCompatActivity {
  private Button resetPasswordBtn;
  private ProgressBar pBar;
  private EditText emailEditText;

  private FirebaseAuth auth;

  // RFC 5322
  private static final String emailRegex = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_forget_password);

    resetPasswordBtn = (Button) findViewById(R.id.resetPasswordBtn);
    pBar = (ProgressBar) findViewById(R.id.pBar);
    emailEditText = (EditText) findViewById(R.id.email);

    auth = FirebaseAuth.getInstance();

    resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (emailEditText.getText().toString().isEmpty() || !emailEditText.getText().toString().matches(emailRegex)) {
          Toast.makeText(ForgetPassword.this, "Invalid email address", Toast.LENGTH_SHORT).show();
        } else {
          pBar.setVisibility(View.VISIBLE);

          auth.fetchSignInMethodsForEmail(emailEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<SignInMethodQueryResult> task) {
              auth.sendPasswordResetEmail(emailEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                  if (task.isSuccessful()) {
                    Toast.makeText(ForgetPassword.this, "Check your email box for password reset instructions.", Toast.LENGTH_SHORT).show();
                  } else {
                    Toast.makeText(ForgetPassword.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                  }
                }
              });
            }
          });
        }
      }
    });
  }

  @Override
  public void onBackPressed() {
    Intent i = new Intent(ForgetPassword.this, Login.class);
    startActivity(i);
    super.onBackPressed();
    finish();
  }
}