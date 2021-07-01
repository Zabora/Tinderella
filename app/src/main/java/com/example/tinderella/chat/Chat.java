package com.example.tinderella.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tinderella.MatchesActivity;
import com.example.tinderella.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat extends AppCompatActivity {
  private RecyclerView mRecyclerView;
  private RecyclerView.Adapter mChatAdapter;
  private RecyclerView.LayoutManager mChatLayoutManager;

  private EditText mSendEditText;
  private ImageButton mBack;

  private ArrayList<ChatObject> resultsChat = new ArrayList<>();

  private ImageButton mSendButton;
  private String notification;
  private String currentUserID, matchId, chatId;
  private String matchName, matchGive, matchNeed, matchBudget, matchProfile;
  private String lastMessage, lastTimeStamp;
  private String message, createdByUser, isSeen, messageId, currentUserName;
  private Boolean currentUserBoolean;
  ValueEventListener seeListener;
  DatabaseReference mDatabaseUser, mDatabaseChat;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);

    matchId = getIntent().getExtras().getString("matchId");
    matchName = getIntent().getExtras().getString("matchName");
    matchGive = getIntent().getExtras().getString("give");
    matchNeed = getIntent().getExtras().getString("need");
    matchBudget = getIntent().getExtras().getString("budget");
    matchProfile = getIntent().getExtras().getString("profile");

    currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID)
        .child("connections").child("matches").child(matchId).child("ChatId");

    getChatId();

    mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    mRecyclerView.setNestedScrollingEnabled(false);
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setFocusable(false);

    mChatLayoutManager = new LinearLayoutManager(Chat.this);
    mRecyclerView.setLayoutManager(mChatLayoutManager);
    mChatAdapter = new ChatAdapter(getDataSetChat(), Chat.this);
    mRecyclerView.setAdapter(mChatAdapter);

    mSendEditText = (EditText) findViewById(R.id.message);
    mBack = (ImageButton) findViewById(R.id.chatBack);

    mSendButton = findViewById(R.id.sendBtn);
    mSendButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendMessage();
      }
    });

    mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
      @Override
      public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (bottom < oldBottom) {
          mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
              mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
            }
          }, 100);
        }
      }
    });

    mBack.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(Chat.this, MatchesActivity.class);
        startActivity(i);
        finish();
      }
    });

    Toolbar toolbar = (Toolbar) findViewById(R.id.chatToolbar);
    setSupportActionBar(toolbar);

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID);
    Map<String, Object> onChat = new HashMap<>();
    onChat.put("onChat", matchId);
    reference.updateChildren(onChat);

    DatabaseReference current = FirebaseDatabase.getInstance().getReference("Users").child(matchId)
        .child("connections").child("matches").child(currentUserID);
    Map<String, Object> lastSeen = new HashMap<>();
    lastSeen.put("lastSeen", "false");
    current.updateChildren(lastSeen);
  }

  private void sendMessage() {
    final String sendMessageText = mSendEditText.getText().toString();
    long now = System.currentTimeMillis();
    String timeStamp = Long.toString(now);

    if (!sendMessageText.isEmpty()) {
      DatabaseReference newMessageDB = mDatabaseChat.push();

      Map<String, Object> newMessage = new HashMap<>();
      newMessage.put("createdByUser", currentUserID);
      newMessage.put("text", sendMessageText);
      newMessage.put("timeStamp", timeStamp);
      newMessage.put("seen", "false");

      DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
      ref.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
          if (snapshot.exists()) {
            if (snapshot.child("name").exists()) {
              currentUserName = snapshot.child("name").getValue().toString();
            }
          }
        }

        @Override
        public void onCancelled(@NonNull @NotNull DatabaseError error) {

        }
      });

      lastMessage = sendMessageText;
      lastTimeStamp = timeStamp;
      updateLastMessage();
      seenMessage(sendMessageText);
      newMessageDB.setValue(newMessage);
    }

    mSendEditText.setText(null);
  }

  private void updateLastMessage() {
    DatabaseReference currentUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID)
        .child("connections").child("matches").child(matchId);
    DatabaseReference matchDB = FirebaseDatabase.getInstance().getReference().child("Users").child(matchId)
        .child("connections").child("matches").child(currentUserID);

    Map<String, Object> lastMessageMap = new HashMap<>();
    lastMessageMap.put("lastMessage", lastMessage);

    Map<String, Object> lastTimeStampMap = new HashMap<>();
    lastTimeStampMap.put("lastTimeStamp", lastTimeStamp);

    Map<String, Object> lastSeen = new HashMap<>();
    lastSeen.put("lastSeen", "true");

    currentUserDB.updateChildren(lastSeen);
    currentUserDB.updateChildren(lastMessageMap);
    currentUserDB.updateChildren(lastTimeStampMap);

    matchDB.updateChildren(lastMessageMap);
    matchDB.updateChildren(lastTimeStampMap);
  }

  private void seenMessage(String text) {
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID);
    reference.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
        if (snapshot.exists()) {
          if (snapshot.child("onChat").exists()) {
            if (snapshot.child("notificationKey").exists()) {
              notification = snapshot.child("notificationKey").getValue().toString();
            } else {
              notification = "";
            }

            if (!snapshot.child("onChat").getValue().toString().equals(currentUserID)) {
              new SendNotification(text, "New message from: " + currentUserName, notification, "activityToBeOpened", "MatchesActivity");
            } else {
              DatabaseReference current = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID)
                  .child("connections").child("matches").child(matchId);
              Map<String, Object> seenInfo = new HashMap<>();
              seenInfo.put("lastSeen", "false");
              current.updateChildren(seenInfo);
            }
          }
        }
      }

      @Override
      public void onCancelled(@NonNull @NotNull DatabaseError error) {

      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.chat_menu, menu);
    TextView mMatchNameTextView = (TextView) findViewById(R.id.chatToolbar);
    mMatchNameTextView.setText(matchName);
    return true;
  }

  public void showProfile(View v) {
    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    View popupView = inflater.inflate(R.layout.item_profile, null);

    TextView name = popupView.findViewById(R.id.name);
    ImageView image = popupView.findViewById(R.id.image);
    TextView budget = popupView.findViewById(R.id.budget);
    ImageView mNeedImage = popupView.findViewById(R.id.needImage);
    ImageView mGiveImage = popupView.findViewById(R.id.giveImage);

    name.setText(matchName);
    budget.setText(matchBudget);

    //NEED IMAGE
    if (matchNeed.equals("Netflix"))
      mNeedImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.netflix));
    else if (matchNeed.equals("Amazon Prime"))
      mNeedImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.amazon));
    else if (matchNeed.equals("Hulu"))
      mNeedImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hulu));
    else if (matchNeed.equals("Vudu"))
      mNeedImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.vudu));
    else if (matchNeed.equals("HBO Now"))
      mNeedImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hbo));
    else if (matchNeed.equals("Youtube Orginals"))
      mNeedImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.youtube));
    else
      mNeedImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.none));

    //GIVE IMAGE
    if (matchGive.equals("Netflix"))
      mGiveImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.netflix));
    else if (matchGive.equals("Amazon Prime"))
      mGiveImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.amazon));
    else if (matchGive.equals("Hulu"))
      mGiveImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hulu));
    else if (matchGive.equals("Vudu"))
      mGiveImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.vudu));
    else if (matchGive.equals("HBO Now"))
      mGiveImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hbo));
    else if (matchGive.equals("Youtube Orginals"))
      mGiveImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.youtube));
    else
      mGiveImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.none));

    switch (matchProfile) {
      case "default":
        Glide.with(popupView.getContext()).load(R.drawable.profile).into(image);
        break;
      default:
        Glide.clear(image);
        Glide.with(popupView.getContext()).load(matchProfile).into(image);
        break;
    }

    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
    int height = LinearLayout.LayoutParams.MATCH_PARENT;
    boolean focusable = true;
    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

    hideSoftKeyBoard();

    popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
    popupView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        popupWindow.dismiss();
        return true;
      }
    });
  }

  private void hideSoftKeyBoard() {
    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

    if (inputMethodManager.isAcceptingText()) {
      inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
  }

  private List<ChatObject> getDataSetChat() {
    return resultsChat;
  }

  private void getChatId() {
    mDatabaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
        if (snapshot.exists()) {
          chatId = snapshot.getValue().toString();
          mDatabaseChat = mDatabaseChat.child(chatId);
          getChatMessages();
        }
      }

      @Override
      public void onCancelled(@NonNull @NotNull DatabaseError error) {

      }
    });
  }

  private void getChatMessages() {
    mDatabaseChat.addChildEventListener(new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
        if (snapshot.exists()) {
          messageId = null;
          message = null;
          createdByUser = null;
          isSeen = null;

          if (snapshot.child("text").getValue() != null) {
            message = snapshot.child("text").getValue().toString();
          }
          if (snapshot.child("createdByUser").getValue() != null) {
            createdByUser = snapshot.child("createdByUser").getValue().toString();
          }
          if (snapshot.child("seen").getValue() != null) {
            isSeen = snapshot.child("seen").getValue().toString();
          } else {
            isSeen = "true";
          }

          messageId = snapshot.getKey().toString();
          if (message != null && createdByUser != null) {
            currentUserBoolean = false;
            if (createdByUser.equals(currentUserID)) {
              currentUserBoolean = true;
            }

            ChatObject newMessage = null;
            if (isSeen.equals("false")) {
              if (!currentUserBoolean) {
                isSeen = "true";
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chat")
                    .child(chatId).child(messageId);

                Map<String, Object> seenInfo = new HashMap<>();
                seenInfo.put("seen", "true");
                reference.updateChildren(seenInfo);

                newMessage = new ChatObject(message, currentUserBoolean, true);
              } else {
                newMessage = new ChatObject(message, currentUserBoolean, false);
              }
            } else {
              newMessage = new ChatObject(message, currentUserBoolean, true);
            }

            DatabaseReference usersInChat = FirebaseDatabase.getInstance().getReference().child("Chat").child(matchId);

            resultsChat.add(newMessage);
            mChatAdapter.notifyDataSetChanged();
            if (mRecyclerView.getAdapter() != null && resultsChat.size() > 0) {
              mRecyclerView.smoothScrollToPosition(resultsChat.size() - 1);
            } else {
              Toast.makeText(Chat.this, "Chat empty", Toast.LENGTH_SHORT);
            }
          }
        }
      }

      @Override
      public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

      }

      @Override
      public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

      }

      @Override
      public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

      }

      @Override
      public void onCancelled(@NonNull @NotNull DatabaseError error) {

      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.unmatch) {
      new AlertDialog.Builder(Chat.this).setTitle("Unmatch").setMessage("Are you sure you want to unmatch?")
          .setPositiveButton("Unmatch", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              deleteMatch(matchId);
              Intent i = new Intent(Chat.this, MatchesActivity.class);
              startActivity(i);
              finish();
              Toast.makeText(Chat.this, "Unmatch successfully", Toast.LENGTH_SHORT).show();
            }
          }).setNegativeButton("Dismiss", null).setIcon(android.R.drawable.ic_dialog_alert).show();
    } else if (item.getItemId() == R.id.viewProfile) {
      showProfile(findViewById(R.id.content));
    }

    return super.onOptionsItemSelected(item);
  }

  private void deleteMatch(String matchId) {
    DatabaseReference matchInUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("connections").child("matches").child(matchId);
    DatabaseReference userInMatchRef = FirebaseDatabase.getInstance().getReference().child("Users").child(matchId).child("connections").child("matches").child(currentUserID);

    DatabaseReference yepsInMatchRef = FirebaseDatabase.getInstance().getReference().child("Users").child(matchId).child("connections").child("yeps").child(currentUserID);
    DatabaseReference yepsInUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("connections").child("yeps").child(matchId);

    DatabaseReference matchChatRef = FirebaseDatabase.getInstance().getReference().child("Chat").child(chatId);

    matchInUserRef.removeValue();
    userInMatchRef.removeValue();

    yepsInMatchRef.removeValue();
    yepsInUserRef.removeValue();

    matchChatRef.removeValue();
  }

  @Override
  protected void onPause() {
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID);
    Map<String, Object> onChat = new HashMap<>();
    onChat.put("onChat", "None");
    reference.updateChildren(onChat);
    super.onPause();
  }

  @Override
  protected void onStop() {
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID);
    Map<String, Object> onChat = new HashMap<>();
    onChat.put("onChat", "None");
    reference.updateChildren(onChat);
    super.onStop();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    finish();
  }
}