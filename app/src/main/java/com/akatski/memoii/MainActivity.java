package com.akatski.memoii;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import android.util.Patterns;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private EditText editEmail,editPassword,editUsername;
    private Button btnSignUp,btnSignIn;
    private FirebaseAuth mAuth;
    private long backPressedTime;
    private Toast backToast;
    private LinearLayout homescreenLinearLayout;
    private EditText codeEditText;
    private EditText phoneEditText;
    String codeSent;
    private String theEmail;
    private String thePassWord;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editEmail = findViewById(R.id.editEmail);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        theEmail=editEmail.getText().toString();
        thePassWord=editPassword.getText().toString();
        homescreenLinearLayout=findViewById(R.id.homescreenLinearLayout);
        AnimationDrawable animationDrawable =(AnimationDrawable) homescreenLinearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        btnSignIn=findViewById(R.id.btnSignIn);
        btnSignUp=findViewById(R.id.btnSignUP);
        mAuth = FirebaseAuth.getInstance();
          codeEditText=findViewById(R.id.codeEditText);
        phoneEditText=findViewById(R.id.phoneEditText);



//using for phone verification
        findViewById(R.id.sendPhoneNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        "sending verification code", Toast.LENGTH_LONG).show();
                sendVerificationCode();



            }
        });

        //using for phone verification
       findViewById(R.id.btnsendCode).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Toast.makeText(getApplicationContext(),
                       "veryfying code ", Toast.LENGTH_LONG).show();
               verifySignInCode();


           }
       });






         btnSignUp.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {



                 setBtnSignUp();

             }
         });

       btnSignIn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               setBtnSignIn();
           }
       });
       // expermental code







    }

    private void verifySignInCode(){
        String code = codeEditText.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        signInWithPhoneAuthCredential(credential);
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // open new activity

                           TransitionToSocialMediaActivity();
                            Toast.makeText(getApplicationContext(),
                                    "Login Successfull", Toast.LENGTH_LONG).show();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),
                                        "Incorrect Verification Code ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }





    private void sendVerificationCode() {

        String phone = phoneEditText.getText().toString();

        if(phone.isEmpty()){
            phoneEditText.setError("Phone number is required");
            phoneEditText.requestFocus();
            return;
        }

        if(phone.length() < 10 ){
            phoneEditText.setError("Please enter a valid phone");
            phoneEditText.requestFocus();
            return;
        }


        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks



    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            codeSent = s;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            //will go to next intended activity
            TransitionToSocialMediaActivity();

        }
    }



    private void setBtnSignUp(){
        mAuth.createUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    TransitionToSocialMediaActivity();
                    FirebaseDatabase.getInstance().getReference()
                            .child("app_users").child(task.getResult()
                            .getUser().getUid()).child("email")
                            .setValue(task.getResult().getUser()
                                    .getEmail());
                    FirebaseDatabase.getInstance().getReference()
                            .child("app_users").child(task.getResult()
                            .getUser().getUid()).child("name")
                            .setValue(editUsername.getText().toString());
                    Toast.makeText(MainActivity.this, "signing up successfull", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "signing up unsuccessfull", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }


    private void setBtnSignIn(){
        mAuth.signInWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, "signing in successfull", Toast.LENGTH_SHORT).show();

                    TransitionToSocialMediaActivity();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "signing in unsuccessfull", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
      private void TransitionToSocialMediaActivity()
      {

          Intent intent =new Intent(this,SocialMediaActivity.class);
          startActivity(intent);
      }
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            System.exit(0);
            return;
        } else {
            backToast = Toast.makeText(getBaseContext(), "Press back 2 times again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();
    }
}
