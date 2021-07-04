package com.example.tinderella.GarbledCircuit;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AliceRunnable implements Runnable {
  private String aliceId;
  private String bobId;
  private DatabaseReference bobQueriesRef;
  private ChildEventListener protocolExecutor;
  private Alice alice;

  public AliceRunnable(String selfId, String bobId, boolean choice) {
    this.aliceId = selfId;
    this.bobId = bobId;

    this.alice = new Alice(choice);

    this.bobQueriesRef = FirebaseDatabase.getInstance().getReference().child("Users").child(bobId).child("queries").child(aliceId);

    this.protocolExecutor = new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
        String step = snapshot.getKey();
        if (step.equals("step2")) {
          // step 3
          HashMap<String, Object> step2 = (HashMap<String, Object>) snapshot.getValue();

          String v = (String) step2.get("v"); // Bob v

          alice.setV(v);
          String encB0 = alice.getEncB0();
          String encB1 = alice.getEncB1();

          Map<String, Object> step3 = new HashMap<>();
          step3.put("encB0", encB0);
          step3.put("encB1", encB1);

          bobQueriesRef.child("step3").setValue(step3);
        } else if (step.equals("step4")) {
          // step 5 (just say result and remove listener)
          HashMap<String, Object> step4 = (HashMap<String, Object>) snapshot.getValue();

          String out = (String) step4.get("out"); // Bob v
          boolean result = alice.getMatchResult(out);

          Map<String, Object> step5 = new HashMap<>();
          step5.put("result", result);

          bobQueriesRef.child("step5").setValue(step5);

          bobQueriesRef.removeEventListener(protocolExecutor);
        }
      }

      @Override
      public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) { }

      @Override
      public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) { }

      @Override
      public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) { }

      @Override
      public void onCancelled(@NonNull @NotNull DatabaseError error) { }
    };
  }

  @Override
  public void run() {
    // step 1
    String[] garbledCircuit = alice.getGarbledCircuit(); // garbledCircuit
    String a = alice.getLabelByChoice(); // Alice input label
    String out0 = alice.getOut0();
    String out1 = alice.getOut1();
    String x0 = alice.getX0(); // First random message
    String x1 = alice.getX1(); // Second random message
    String publicKey = alice.keyToString("public"); // Public key for Bob

    List<String> list = Arrays.asList(garbledCircuit);

    Map<String, Object> step1 = new HashMap<>();
    step1.put("garbledCircuit", list);
    step1.put("a", a);
    step1.put("out0", out0);
    step1.put("out1", out1);
    step1.put("x0", x0);
    step1.put("x1", x1);
    step1.put("publicKey", publicKey);

    bobQueriesRef.child("step1").setValue(step1);

    // wait for Bob response
    bobQueriesRef.addChildEventListener(protocolExecutor);
  }
}
