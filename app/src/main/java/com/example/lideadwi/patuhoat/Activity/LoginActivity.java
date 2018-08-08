package com.example.lideadwi.patuhoat.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.lideadwi.patuhoat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    private LinearLayout regiSt;
    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mEmailSignInButton;
    private ProgressDialog mpProgressDialog;


    //Firebase
    private FirebaseAuth mAuth;
    //firebase user database
    private DatabaseReference muserDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);


        regiSt =  findViewById(R.id.tvRegister);
        mpProgressDialog = new ProgressDialog(this);


        //firebase
        muserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");


        mAuth = FirebaseAuth.getInstance();
        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmailView.getText().toString();
                final String password = mPasswordView.getText().toString();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    mpProgressDialog.setTitle("login..");
                    mpProgressDialog.setMessage("we are try connect your acount..");
                    mpProgressDialog.setCanceledOnTouchOutside(false);
                    mpProgressDialog.show();
                    loginUser(email,password);

                }else {
                    mpProgressDialog.hide();
                    Toast.makeText(LoginActivity.this,"please insert your email and password login",Toast.LENGTH_SHORT).show();
                }

            }
        });


        regiSt =  findViewById(R.id.tvRegister);
        regiSt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this,Registrasi.class);
                startActivity(i);

            }
        });

    }
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    final String device_token = FirebaseInstanceId.getInstance().getToken();
                    final String current_user_id = mAuth.getCurrentUser().getUid();

                    muserDatabaseReference.child(current_user_id).child("device_token").setValue(device_token).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mpProgressDialog.dismiss();

                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);
                            finish();
                        }
                    });


                }else{
                    mpProgressDialog.hide();
                    Toast.makeText(LoginActivity.this,"Maaf Proses LoginActivity gagal, silahkan periksa koneksi jaringan anda.. Terima Kasih",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
