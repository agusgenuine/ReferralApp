package com.gaurav.refferalapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.data.DataBufferSafeParcelable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

  private TextView remaingBalanceTextView;
  private Button redeemCodeButton;
  private EditText referredByEditText;
  private ImageView shareCodeImageView;
  private DatabaseReference accountBalanceRef;
  private DatabaseReference referralCodeRef;
  private DatabaseReference currentBalance;
  private FirebaseAuth mAuth;
  private String userId;
  private String shareSubject;
  private String shareBody;
  private String redeemCode;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    remaingBalanceTextView = findViewById(R.id.remaining_balance);
    redeemCodeButton = findViewById(R.id.redeem_code_button);
    referredByEditText = findViewById(R.id.referred_by_edit_text);
    shareCodeImageView = findViewById(R.id.share_code_image_view);

    mAuth = FirebaseAuth.getInstance();
    userId =  Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    shareWithFriends();
   // getCurrentBalance();


    //------------------------------------------ Display current Balance ----------------------------------------------------
    accountBalanceRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
    accountBalanceRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        remaingBalanceTextView.setText((String)dataSnapshot.child("account_balance").getValue()+" Rs.");
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });
    //----------------------------------------------------------------------------------------------------------------------



    //--------------------------------------------- To retrieve referral code from database ----------------------------------------------
    referralCodeRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
    final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
    referralCodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        shareBody = (String) dataSnapshot.child("referral_code").getValue();
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("referral_code", shareBody);
        editor.apply();
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });
    //------------------------------------------------------------------------------------------------------------------------------------

    //-------------------------------- To retrieve currentBalance -----------------------------------------------------------------
    currentBalance = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
    currentBalance.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        String balance = (String) dataSnapshot.child("account_balance").getValue();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("balance", balance);
        editor.apply();
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });

//--------------------------------- To go to RedeemCodeActivity & sending current balance to next Activity ------------------------
    redeemCodeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final String referred_by = referredByEditText.getText().toString().trim();
        if (TextUtils.isEmpty(referred_by)){
          new AlertDialog.Builder(HomeActivity.this)
                  .setTitle("Enter how referred the code!")
                  .setNeutralButton("Ok", null)
                  .show();
        }
        else{
          DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
          reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                if (referred_by.equalsIgnoreCase((String)snapshot.child("name").getValue())){
                  DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Users").child(snapshot.getKey());
                  reference1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                      String balanceOfrefferer = (String) dataSnapshot.child("account_balance").getValue();
                      //-------------------------------- To redeem code Activity -------------------------------------
                      Intent toRedeemActivity = new Intent(getApplicationContext(),RedeemCodeActivity.class);
                      String currentBalance = pref.getString("balance", String.valueOf(100));
                      toRedeemActivity.putExtra("current_balance",currentBalance);
                      toRedeemActivity.putExtra("refferers_balance",balanceOfrefferer);
                      toRedeemActivity.putExtra("referred_by_name",referred_by);
                      startActivity(toRedeemActivity);
                      //------------------------------------------------------------------------------------------------
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

          /*Intent toRedeemActivity = new Intent(getApplicationContext(),RedeemCodeActivity.class);
          String currentBalance = pref.getString("balance", String.valueOf(100));
          toRedeemActivity.putExtra("current_balance",currentBalance);
          startActivity(toRedeemActivity); */
        }


      }
    });

    //--------------------------------------------------------------------------------------------------------------------------------


  }

  //------------------------------------------ To share the code with friends -----------------------------------------------------------
  public void shareWithFriends(){
    DatabaseReference currentUserName =  FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
    currentUserName.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        String userName = (String) dataSnapshot.child("name").getValue();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("user_name", userName);
        editor.apply();
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });

    shareCodeImageView.setOnClickListener(new View.OnClickListener() {
      @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
      @Override
      public void onClick(View v) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        shareSubject = "ReferralApp";
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        String referral_code = pref.getString("referral_code", "not_loaded!");
        String currentUserName = pref.getString("user_name","not_loaded!");
        shareBody = "Redeem the code below and get 100Rs. in ReferralApp! \n" +referral_code+ "\nShared by: "+currentUserName;
        share.putExtra(Intent.EXTRA_TEXT,shareBody);
        startActivity(Intent.createChooser(share,"Share with"));
      }
    });
  }
  //--------------------------------------------------------------------------------------------------------------------------------------

  //------------------------------------- For menu ------------------------------------------------------
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu,menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {

    int id =  menuItem.getItemId();
    if ( id == R.id.sign_out_of_home_activity){
      FirebaseAuth.getInstance().signOut();
      Intent toLogInActivity = new Intent(getApplicationContext(),MainActivity.class);
      startActivity(toLogInActivity);

    }
    return super.onOptionsItemSelected(menuItem);
  }

//--------------------------------------------------------------------------------------------------------------------

}
