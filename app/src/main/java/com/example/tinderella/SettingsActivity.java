package com.example.tinderella;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

  private ProgressBar pBar;
  private Button confirm;
  private ImageButton back;
  private ImageView profileImage;
  private Spinner need, give;
  private EditText nameField, phoneField, budgetField;

  private FirebaseAuth auth;
  private DatabaseReference userDatabase;

  private String userId, name, phone, budget, profileImageUrl, userSex, userNeed, userGive;
  private int needIndex, giveIndex;
  private Uri resultUri;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    pBar = (ProgressBar) findViewById(R.id.pBar);
    pBar.setVisibility(View.GONE);

    nameField = (EditText) findViewById(R.id.name);
    phoneField = (EditText) findViewById(R.id.phone);
    budgetField = (EditText) findViewById(R.id.budget_settings);

    confirm = (Button) findViewById(R.id.confirm);
    back = (ImageButton) findViewById(R.id.settingsBack);
    profileImage = (ImageView) findViewById(R.id.profileImage);

    need = (Spinner) findViewById(R.id.spinner_need_settings);
    give = (Spinner) findViewById(R.id.spinner_give_settings);

    auth = FirebaseAuth.getInstance();
    if (auth == null || auth.getCurrentUser() == null) {
      finish();
      return;
    }

    userId = auth.getCurrentUser().getUid();
    userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

    ArrayAdapter<CharSequence> needAdapter = ArrayAdapter.createFromResource(this, R.array.services, android.R.layout.simple_spinner_item);
    needAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    need.setAdapter(needAdapter);

    ArrayAdapter<CharSequence> giveAdapter = ArrayAdapter.createFromResource(this, R.array.services, android.R.layout.simple_spinner_item);
    giveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    give.setAdapter(giveAdapter);

    getUserInfo();

    back.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pBar.setVisibility(View.VISIBLE);

        Intent i = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(i);
        finish();

        pBar.setVisibility(View.GONE);
      }
    });

    profileImage.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (checkPermission()) {
          Toast.makeText(SettingsActivity.this, "Allow access to continue", Toast.LENGTH_SHORT).show();
          requestPermissions();
        } else {
          Intent i = new Intent(Intent.ACTION_PICK);
          i.setType("image/*");
          startActivityForResult(i, 1);
        }
      }
    });

    confirm.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pBar.setVisibility(View.VISIBLE);

        // TODO: Here should be saveInfo method???
        saveUserInfo();

        Intent i = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(i);
        finish();

        pBar.setVisibility(View.GONE);
      }
    });

    Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
    setSupportActionBar(toolbar);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.settings_menu, menu);
    return true;
  }

  public boolean checkPermission() {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
  }

  public void requestPermissions() {
    ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 100);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 100) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, 1);
      }
    } else {
      Toast.makeText(SettingsActivity.this, "Allow access to continue", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.contactUs) {
      new AlertDialog.Builder(SettingsActivity.this).setTitle("Contact us").setMessage("Contact us: support@tinderella.com")
          .setNegativeButton("Dismiss", null).setIcon(android.R.drawable.ic_dialog_alert).show();
    } else if (item.getItemId() == R.id.logout) {
      pBar.setVisibility(View.VISIBLE);

      auth.signOut();
      Toast.makeText(SettingsActivity.this, "Log out successfully", Toast.LENGTH_SHORT).show();

      Intent i = new Intent(SettingsActivity.this, Login_or_Register.class);
      startActivity(i);
      finish();

      pBar.setVisibility(View.GONE);
    } else if (item.getItemId() == R.id.deleteAccount) {
      new AlertDialog.Builder(SettingsActivity.this).setTitle("Delete account").setMessage("The action of deleting the account is irreversible")
          .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                  pBar.setVisibility(View.VISIBLE);

                  if (task.isSuccessful()) {
                    deleteUserAccount(userId);
                    Toast.makeText(SettingsActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                  } else {
                    Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    auth.signOut();
                  }

                  Intent i = new Intent(SettingsActivity.this, Login_or_Register.class);
                  startActivity(i);
                  finish();

                  pBar.setVisibility(View.GONE);
                }
              });
            }
          }).setNegativeButton("Dismiss", null).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    return super.onOptionsItemSelected(item);
  }

  private void deleteUserAccount(String userId) {
    DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
    DatabaseReference currentUserMatchesRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId)
        .child("connections").child("matches");

    currentUserMatchesRef.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
        if (snapshot.exists()) {
          for (DataSnapshot match : snapshot.getChildren()) {
            deleteMatch(match.getKey(), match.child("ChatId").getValue().toString());
          }
        }
      }

      @Override
      public void onCancelled(@NonNull @NotNull DatabaseError error) {

      }
    });

    currentUserMatchesRef.removeValue();
    currentUserRef.removeValue();
  }

  public void deleteMatch(String matchId, String chatId) {
    // TODO: I am not sure that it is correct (look to Chat->deleteMatch)
    DatabaseReference matchInUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("connections").child("matches").child(matchId);
    DatabaseReference userInMatchRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("connections").child("matches").child(userId);

    DatabaseReference yepsInMatchRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("connections").child("yeps").child(userId);
    DatabaseReference yepsInUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("connections").child("yeps").child(matchId);

    DatabaseReference matchChatRef = FirebaseDatabase.getInstance().getReference().child("Chat").child(chatId);

    matchInUserRef.removeValue();
    userInMatchRef.removeValue();

    yepsInMatchRef.removeValue();
    yepsInUserRef.removeValue();

    matchChatRef.removeValue();
  }

  private void getUserInfo() {
    userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
          Map<String, Object> map = (Map<String, Object>) snapshot.getValue();

          if (map.get("name") != null) {
            name = map.get("name").toString();
            nameField.setText(name);
          }
          if (map.get("phone") != null) {
            phone = map.get("phone").toString();
            phoneField.setText(phone);
          }

          if (map.get("sex") != null) {
            userSex = map.get("sex").toString();
          } else {
            userSex = "";
          }
          if (map.get("budget") != null) {
            budget = map.get("budget").toString();
          } else {
            budget = "0";
          }
          budgetField.setText(budget);

          if (map.get("give") != null) {
            userGive = map.get("give").toString();
          } else {
            userGive = "";
          }
          if (map.get("need") != null) {
            userNeed = map.get("need").toString();
          } else {
            userNeed = "";
          }

          giveIndex = 0;
          needIndex = 0;
          String[] services = getResources().getStringArray(R.array.services);
          for (int i = 0; i < services.length; i++) {
            if (userNeed.equals(services[i])) {
              needIndex = 1;
            }
            if (userGive.equals(services[i])) {
              giveIndex = 1;
            }
          }

          give.setSelection(giveIndex);
          need.setSelection(needIndex);

          Glide.clear(profileImage);
          if (map.get("profileImageUrl") != null) {
            profileImageUrl = map.get("profileImageUrl").toString();
            if (profileImageUrl.equals("default")) {
              Glide.with(getApplication()).load(R.drawable.profile).into(profileImage);
            } else {
              Glide.with(getApplication()).load(profileImageUrl).into(profileImage);
            }
          }
        }
      }

      @Override
      public void onCancelled(@NonNull @NotNull DatabaseError error) {

      }
    });
  }

  private void saveUserInfo() {
    name = nameField.getText().toString();
    phone = phoneField.getText().toString();

    budget = budgetField.getText().toString();

    userGive = give.getSelectedItem().toString();
    userNeed = need.getSelectedItem().toString();

    Map<String, Object> userInfo = new HashMap();
    userInfo.put("name", name);
    userInfo.put("phone", phone);
    userInfo.put("budget", budget);
    userInfo.put("give", userGive);
    userInfo.put("need", userNeed);

    userDatabase.updateChildren(userInfo);
    if (resultUri != null) {
      StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profileImageUrl").child(userId);

      Bitmap bitmap = null;
      try {
        bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
      } catch (IOException e) {
        e.printStackTrace();
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
      byte[] data = baos.toByteArray();

      UploadTask uploadTask = filePath.putBytes(data);
      uploadTask.addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull @NotNull Exception e) {
          finish();
        }
      });
      uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
          Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();

          while (!uri.isComplete());

          Uri downloadUri = uri.getResult();

          Map<String, Object> userUri = new HashMap<String, Object>();
          userUri.put("profileImageUrl", downloadUri.toString());
          userDatabase.updateChildren(userUri);

          finish();
        }
      });
    } else {
      finish();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
      resultUri = data.getData();
      profileImage.setImageURI(resultUri);
    }
  }
}