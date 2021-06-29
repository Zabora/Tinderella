package com.example.tinderella;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class Login_or_Register extends AppCompatActivity {

  private Button loginBtn, registerBtn;
  private ProgressBar pBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login_or_register);

    loginBtn = (Button) findViewById(R.id.login);
    registerBtn = (Button) findViewById(R.id.signup);
    pBar = (ProgressBar) findViewById(R.id.pBar);
    pBar.setVisibility(View.GONE);

    loginBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pBar.setVisibility(View.VISIBLE);

        Intent i = new Intent(Login_or_Register.this, Login.class);
        startActivity(i);
        finish();

        pBar.setVisibility(View.GONE);
      }
    });

    registerBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pBar.setVisibility(View.VISIBLE);

        Intent i = new Intent(Login_or_Register.this, Register.class);
        startActivity(i);
        finish();

        pBar.setVisibility(View.GONE);
      }
    });
  }
}