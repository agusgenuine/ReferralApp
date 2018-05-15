package com.gaurav.refferalapp;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.data.DataBufferSafeParcelable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class RedeemCodeActivity extends AppCompatActivity {

  private String currentBalance;
  private String friendsBalance;
  private EditText referralCodeEditText;
  private DatabaseReference checkRef;
  private DatabaseReference sendData;
  private String userId;
  private FirebaseAuth mAuth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_redeem_code);

    mAuth = FirebaseAuth.getInstance();
    userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

    currentBalance = getIntent().getStringExtra("current_balance");
    friendsBalance = getIntent().getStringExtra("refferers_balance");
    referralCodeEditText = findViewById(R.id.referral_code_edit_text);
    checkRef = FirebaseDatabase.getInstance().getReference().child("Users");


  }

  public void RedeemCode(View view){
    final String referral_code = referralCodeEditText.getText().toString().trim();
    if (TextUtils.isEmpty(referral_code)){

      new AlertDialog.Builder(RedeemCodeActivity.this)
              .setTitle("Enter the code!")
              .setNeutralButton("Ok", null)
              .show();

    }
    else {
      checkRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          for (DataSnapshot check : dataSnapshot.getChildren()) {
            if (referral_code.equalsIgnoreCase((String) check.child("referral_code").getValue())) {
              int intBalance = Integer.parseInt(currentBalance);
              int intFriendsBalance = Integer.parseInt(friendsBalance);
              int add = intBalance + 100;
              int add2 = intFriendsBalance + 100;
              final String balance = String.valueOf(add);
              final String friend = String.valueOf(add2);
              sendData(balance);
              addMoneyToFriendsAccount(friend);
            }
          }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
      });
    }
  }

  //------------------------------------------------------- Add money to friends account ----------------------------------------------------------
  public void addMoneyToFriendsAccount(final String friend){
    final String friends_name = getIntent().getStringExtra("referred_by_name");
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
    reference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot: dataSnapshot.getChildren()){
          if (friends_name.equalsIgnoreCase((String) snapshot.child("name").getValue())){
            DatabaseReference friendsDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(snapshot.getKey());
            final DatabaseReference addMoney = friendsDatabase;
            friendsDatabase.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                addMoney.child("account_balance").setValue(friend);
                Toast.makeText(RedeemCodeActivity.this, "100Rs. added to friends account!", Toast.LENGTH_SHORT).show();
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
            });
          }
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });
  }


  public void sendData(final String balance){
    sendData = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
    final DatabaseReference send = sendData.child("account_balance");
    sendData.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        send.setValue(balance);
        Toast.makeText(RedeemCodeActivity.this, "Redeemed!", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });
  }


}
