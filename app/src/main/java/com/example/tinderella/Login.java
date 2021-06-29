package com.example.tinderella;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class Login extends AppCompatActivity {
  private Button loginBtn;
  private ProgressBar pBar;
  private EditText emailEditText, passwordEditText;

  private FirebaseAuth auth;
  private FirebaseAuth.AuthStateListener auth_state_listener;

  // RFC 5322
  private static final String emailRegex = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    pBar = (ProgressBar) findViewById(R.id.pBar);
    pBar.setVisibility(View.GONE);

    loginBtn = (Button) findViewById(R.id.login);
    emailEditText = (EditText) findViewById(R.id.email);
    passwordEditText = (EditText) findViewById(R.id.password);

    TextView noExisting = (TextView) findViewById(R.id.noExisting);
    noExisting.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        backToLoginOrRegister();
      }
    });

    TextView forgetPassword = (TextView) findViewById(R.id.forgetPassword);
    forgetPassword.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pBar.setVisibility(View.VISIBLE);

        Intent i = new Intent(Login.this, ForgetPassword.class);
        startActivity(i);
        finish();

        pBar.setVisibility(View.GONE);
      }
    });

    auth = FirebaseAuth.getInstance();
    auth_state_listener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull @NotNull FirebaseAuth firebaseAuth) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.isEmailVerified()) {
          pBar.setVisibility(View.VISIBLE);

          Intent i = new Intent(Login.this, MainActivity2.class);
          startActivity(i);
          finish();

          pBar.setVisibility(View.GONE);
        }
      }
    };

    loginBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pBar.setVisibility(View.VISIBLE);

        if (validateInput()) {
          auth.signInWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
              .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                  if (!task.isSuccessful()) {
                    Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                  } else {
                    if (auth.getCurrentUser().isEmailVerified()) {
                      Intent i = new Intent(Login.this, MainActivity2.class);
                      startActivity(i);
                      finish();
                    } else {
                      Toast.makeText(Login.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                    }
                  }

                  pBar.setVisibility(View.GONE);
                }
              });
        } else {
          pBar.setVisibility(View.GONE);
        }
      }
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    auth.addAuthStateListener(auth_state_listener);
  }

  @Override
  protected void onStop() {
    super.onStop();
    auth.removeAuthStateListener(auth_state_listener);
  }

  @Override
  public void onBackPressed() {
    Intent i = new Intent(Login.this, Login_or_Register.class);
    startActivity(i);
    finish();
  }

  private boolean validateInput() {
    if (emailEditText.getText().toString().isEmpty() || passwordEditText.getText().toString().isEmpty()) {
      Toast.makeText(Login.this, "All fields required", Toast.LENGTH_SHORT).show();
      return false;
    }

    if (!emailEditText.getText().toString().matches(emailRegex)) {
      Toast.makeText(Login.this, "Invalid email address", Toast.LENGTH_SHORT).show();
      return false;
    }

    if (passwordEditText.getText().toString().length() < 6) {
      Toast.makeText(Login.this, "Invalid password (6 characters at least)", Toast.LENGTH_SHORT).show();
      return false;
    }

    return true;
  }

  private void backToLoginOrRegister() {
    Intent i = new Intent(Login.this, Login_or_Register.class);
    startActivity(i);
    finish();
  }
}