package com.gaurav.refferalapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import io.fabric.sdk.android.Fabric;

/**
 * @author Gaurav
 */
public class MainActivity extends AppCompatActivity  {

  private EditText logEmailEditText;
  private EditText logPassEditText;
  private FirebaseAuth mAuth;
  private DatabaseReference mDatabase;
  String logEmail;


  public void toSignUpActivity(View view){
    Intent tosignUpPage = new Intent(getApplicationContext(),SignUpActivity.class);
    startActivity(tosignUpPage);

  }

  public void logInFunction(View view){
    logEmail = logEmailEditText.getText().toString().trim();
    String logPass = logPassEditText.getText().toString().trim();
    if (TextUtils.isEmpty(logEmail) && TextUtils.isEmpty(logPass) || TextUtils.isEmpty(logEmail) || TextUtils.isEmpty(logPass)){
      new AlertDialog.Builder(MainActivity.this)
              .setTitle(" Enter Email & Password! ")
              .setNeutralButton("Ok",null)
              .show();
    }
    else if (!TextUtils.isEmpty(logEmail) && !TextUtils.isEmpty(logPass)){
      mAuth.signInWithEmailAndPassword(logEmail,logPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
          if (task.isSuccessful()){
            checkUserExists();
          }
          else if (!task.isSuccessful()){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Log in failed!\nEmail or Password incorrect. ")
                    .setNeutralButton("Retry", null)
                    .show();
          }
        }
      });

    }
  }

  public void checkUserExists(){
    final String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    mDatabase.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.hasChild(userId)){
          Intent toHomeActivity = new Intent(getApplicationContext(),HomeActivity.class);
          startActivity(toHomeActivity);
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
   Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
    // TODO: Move this to where you establish a user session
    logUser();
    setContentView(R.layout.activity_main);




    logEmailEditText = findViewById(R.id.log_email);
    logPassEditText = findViewById(R.id.log_pass);


    mAuth = FirebaseAuth.getInstance();
    if (mAuth.getCurrentUser() != null){
      Intent logedIn = new Intent(getApplicationContext(),HomeActivity.class);
      startActivity(logedIn);
    }
    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

    Bundle emailFromSignUp = getIntent().getExtras();
    if(emailFromSignUp != null) {
      String text = emailFromSignUp.getString("email");
      logEmailEditText.setText(text);
    }
  }

  private void logUser() {
    // TODO: Use the current user's information
    // You can call any combination of these three methods
///    Crashlytics.setUserIdentifier(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
    Crashlytics.setUserEmail(logEmail);
    // Crashlytics.setUserName("Test User");
  }

  //----------------------------------------Authentication------------------------------------------





  //--------------------------------------------------------------------------------------------
  //-------------------------------------------Exit on back start-------------------------------------
  private Boolean exit = false;
  @Override
  public void onBackPressed() {
    Intent intent = new Intent(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_HOME);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
    finish();
    System.exit(0);

  }
  //--------------------------------------- Exit on back end -----------------------------------------------




}
