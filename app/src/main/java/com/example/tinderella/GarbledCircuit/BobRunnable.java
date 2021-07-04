package com.example.tinderella.GarbledCircuit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BobRunnable implements Runnable {
  private String bobId;
  private String aliceId;
  private DatabaseReference bobQueriesRef;
  private ChildEventListener protocolExecutor;
  private Bob bob;

  public BobRunnable(String selfId, String aliceId, boolean choice) {
    this.bobId = selfId;
    this.aliceId = aliceId;

    this.bob = new Bob(choice);

    this.bobQueriesRef = FirebaseDatabase.getInstance().getReference().child("Users").child(bobId).child("queries").child(aliceId);

    this.protocolExecutor = new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
        String step = snapshot.getKey();
        if (step.equals("step3")) {
          HashMap<String, Object> step3 = (HashMap<String, Object>) snapshot.getValue();

          String encB0 = (String) step3.get("encB0");
          String encB1 = (String) step3.get("encB1");

          bob.receiveEncryptedLabels(encB0, encB1);
          String out = bob.getResult();

          Map<String, Object> step4 = new HashMap<>();
          step4.put("out", out);

          bobQueriesRef.child("step4").setValue(step4);

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
    // step 2
    bobQueriesRef.child("step1").addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
        if (snapshot.exists()) {
          HashMap<String, Object> step1 = (HashMap<String, Object>) snapshot.getValue();

          List<String> list = (List<String>) step1.get("garbledCircuit"); // garbledCircuit

          String[] garbledCircuit = list.toArray(new String[0]); // garbledCircuit
          String a = (String) step1.get("a"); // Alice input label
          String out0 = (String) step1.get("out0");
          String out1 = (String) step1.get("out1");
          String x0 = (String) step1.get("x0"); // First random message from Alice
          String x1 = (String) step1.get("x1"); // Second random message from Alice
          String publicKey = (String) step1.get("publicKey"); // Alice public key

          bob.setInfoFromAlice(garbledCircuit, a, out0, out1, x0, x1, publicKey); // set values from Alice and compute v
          String v = bob.getV(); // get v and send it to Alice

          Map<String, Object> step2 = new HashMap<>();
          step2.put("v", v);

          bobQueriesRef.child("step2").setValue(step2);

          // wait for Bob response
          bobQueriesRef.addChildEventListener(protocolExecutor);
        }
      }

      @Override
      public void onCancelled(@NonNull @NotNull DatabaseError error) { }
    });
  }
}
