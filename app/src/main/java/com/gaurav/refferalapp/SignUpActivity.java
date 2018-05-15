package com.gaurav.refferalapp;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Gaurav
 */
public class SignUpActivity extends AppCompatActivity {

  private static final int PROFILE_PIC_REQUEST = 1;
  private EditText userNameEditText;
  private EditText emailEditText;
  private EditText passEditText;
  private ImageView profilePicImageView;
  private FirebaseAuth mAuth;
  private DatabaseReference mDatabase;
  private StorageReference storageReference;
  private Uri profileUri;
  private  String uniqueReferalKey = UUID.randomUUID().toString();


  public void selectProfilePicFunction(View view) {
    Intent profilePicIntent = new Intent(Intent.ACTION_GET_CONTENT);
    profilePicIntent.setType("image/*");
    startActivityForResult(profilePicIntent,PROFILE_PIC_REQUEST);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == PROFILE_PIC_REQUEST && resultCode == RESULT_OK  ){
      profileUri = data.getData();
      profilePicImageView = findViewById(R.id.profile_pic);
      Picasso.get()
              .load(profileUri)
              .into(profilePicImageView);
    }
  }

  public void signUpFunction(View view){
    final String name = userNameEditText.getText().toString().trim();
    final String email = emailEditText.getText().toString().trim();
    final String password = passEditText.getText().toString().trim();

    if (TextUtils.isEmpty(name) && TextUtils.isEmpty(email) && TextUtils.isEmpty(password) || TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
      new AlertDialog.Builder(SignUpActivity.this)
              .setTitle("Enter Name,Email & Password")
              .setNeutralButton("Ok",null)
              .show();
    }else if (profileUri == null){
      new AlertDialog.Builder(SignUpActivity.this)
              .setTitle("Select a profile picture!")
              .setNeutralButton("Ok",null)
              .show();
    }
    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && profileUri != null){
      mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
          if (task.isSuccessful()){

            String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            final DatabaseReference currentUserDatabase = mDatabase.child(userId);
            currentUserDatabase.child("name").setValue(name);
            currentUserDatabase.child("email_id").setValue(email);
            currentUserDatabase.child("uid_current_user").setValue(userId);
            currentUserDatabase.child("referral_code").setValue(uniqueReferalKey);
            currentUserDatabase.child("account_balance").setValue(String.valueOf(100));
            final StorageReference filepath = storageReference.child("users_profile").child(userId);
            filepath.putFile(profileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
              @Override
              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                final Uri downloadUri = taskSnapshot.getUploadSessionUri();
                currentUserDatabase.child("profile_image").setValue(Objects.requireNonNull(downloadUri).toString());
              }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
              @Override
              public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(SignUpActivity.this, "Uploading...", Toast.LENGTH_SHORT).show();

              }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
              @Override
              public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Toast.makeText(SignUpActivity.this, "Upload complete!", Toast.LENGTH_SHORT).show();
              }
            }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this, "Error!", Toast.LENGTH_SHORT).show();
              }
            });

            String text = emailEditText.getText().toString().trim();
            Intent toMainActivity = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(toMainActivity);

          }else if(!task.isSuccessful()){
            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
              new AlertDialog.Builder(SignUpActivity.this)
                      .setTitle("User already exits!\nLog in below")
                      .setNeutralButton("Log In", null)
                      .show();
            }
          }

          else {
            Exception exception = task.getException();
            assert exception != null;
            Log.i("Error",exception.getMessage());

          }
        }
      });
    }

  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


    setContentView(R.layout.activity_sign_up);

    userNameEditText = findViewById(R.id.sign_up_name);
    emailEditText = findViewById(R.id.sign_up_email);
    passEditText = findViewById(R.id.sign_up_pass);
    profilePicImageView = findViewById(R.id.profile_pic);

    mAuth = FirebaseAuth.getInstance();
    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    storageReference = FirebaseStorage.getInstance().getReference();
  }

  //---------------------------------------- On back press exit -------------------------------------------------
  private Boolean exit = false;
  @Override
  public void onBackPressed() {
    Intent intent = new Intent(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_HOME);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
    finish();
    System.exit(0);


//---------------------------------------------------------------------------------------------------------------
  }


}
