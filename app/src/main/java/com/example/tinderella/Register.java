package com.example.tinderella;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("Convert2Lambda")
public class Register extends AppCompatActivity {
  private static final String TAG = "Register";

  private Button registerBtn;
  private ProgressBar pBar;
  private EditText emailEditText, passwordEditText, nameEditText, phoneEditText;
  private CheckBox tncCheckBox;

  private FirebaseAuth auth;
  private FirebaseAuth.AuthStateListener auth_state_listener;

  // RFC 5322
  private static final String emailRegex = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    pBar = (ProgressBar) findViewById(R.id.pBar);
    pBar.setVisibility(View.GONE);

    registerBtn = (Button) findViewById(R.id.register);
    emailEditText = (EditText) findViewById(R.id.email);
    passwordEditText = (EditText) findViewById(R.id.password);
    nameEditText = (EditText) findViewById(R.id.name);
    phoneEditText = (EditText) findViewById(R.id.phone);

    tncCheckBox = (CheckBox) findViewById(R.id.tncCheckBox);
    tncCheckBox.setText("");

    auth = FirebaseAuth.getInstance();
    auth_state_listener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull @org.jetbrains.annotations.NotNull FirebaseAuth firebaseAuth) {
        pBar.setVisibility(View.VISIBLE);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.isEmailVerified()) {
          Intent i = new Intent(Register.this, MainActivity.class);
          startActivity(i);
          finish();
        }

        pBar.setVisibility(View.GONE);
      }
    };

    TextView existing = (TextView) findViewById(R.id.existing);
    existing.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        backToLoginOrRegister();
      }
    });

    TextView textView = (TextView) findViewById(R.id.tncTextView);
    // TODO: generate TnC and pass it as href
    // textView.setMovementMethod(LinkMovementMethod.getInstance());

    registerBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pBar.setVisibility(View.VISIBLE);

        if (validateInput()) {
          auth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
              .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<AuthResult> task) {
                  if (!task.isSuccessful()) {
                    Toast.makeText(Register.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                  } else {
                    auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                          Toast.makeText(Register.this, "Registration successfully. Check your verification email.", Toast.LENGTH_SHORT).show();
                          final String userID = auth.getCurrentUser().getUid();
                          DatabaseReference currentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);

                          Map<String, Object> userInfo = new HashMap<>();
                          userInfo.put("name", nameEditText.getText().toString());
                          userInfo.put("phone", phoneEditText.getText().toString());
                          userInfo.put("profileImageUrl", "default");

                          currentUser.updateChildren(userInfo);
                          
                          backToLoginOrRegister();
                        } else {
                          Toast.makeText(Register.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                      }
                    });
                  }
                }
              });
        } else {
          // validation failed
        }

        pBar.setVisibility(View.GONE);
      }
    });
  }

  private void backToLoginOrRegister() {
    emailEditText.setText("");
    passwordEditText.setText("");
    nameEditText.setText("");

    Intent i = new Intent(Register.this, Login_or_Register.class);
    startActivity(i);
    finish();

    pBar.setVisibility(View.GONE);
  }

  private boolean validateInput() {
    if (
        emailEditText.getText().toString().isEmpty() ||
        passwordEditText.getText().toString().isEmpty() ||
        nameEditText.getText().toString().isEmpty() ||
        phoneEditText.getText().toString().isEmpty()
    ) {
      Toast.makeText(Register.this, "All fields required", Toast.LENGTH_SHORT).show();
      return false;
    }

    if (!emailEditText.getText().toString().matches(emailRegex)) {
      Toast.makeText(Register.this, "Invalid email address", Toast.LENGTH_SHORT).show();
      return false;
    }

    if (passwordEditText.getText().toString().length() < 6) {
      Toast.makeText(Register.this, "Invalid password (6 characters at least)", Toast.LENGTH_SHORT).show();
      return false;
    }

    if (!tncCheckBox.isChecked()) {
      Toast.makeText(Register.this, "Please accept Terms and Conditions", Toast.LENGTH_SHORT).show();
      return false;
    }

    return true;
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  public void onBackPressed() {
    Intent i = new Intent(Register.this, Login_or_Register.class);
    startActivity(i);
    finish();
  }
}