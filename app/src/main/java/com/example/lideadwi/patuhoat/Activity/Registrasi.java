package com.example.lideadwi.patuhoat.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.lideadwi.patuhoat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Registrasi extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private TextInputLayout displayname;
    private TextInputLayout email;
    private TextInputLayout password;
    private TextInputLayout no_hp;
    private TextInputLayout alamat;
    private RadioGroup mRadioGroup;
    private RadioButton mRadioButton_lk,mRadioButton_pr;

    private Button register;
    private Toolbar mToolbar;
    private ProgressDialog mpProgressDialog;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //database firebase
    private DatabaseReference mDatabaseReference;
    private DatabaseReference databaseUserCampur;
    private FirebaseUser currentUser;

    private Spinner mSpinner;
    private ArrayAdapter<CharSequence> spinneradapter;
    private String sjenisuser,skategoripengguna;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);

        setContentView(R.layout.activity_registrasi);
        mAuth = FirebaseAuth.getInstance();
        mToolbar = (Toolbar) findViewById(R.id.registtoolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Registrasi");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Reference

        displayname = (TextInputLayout) findViewById(R.id.reg_display_name);
        email = (TextInputLayout) findViewById(R.id.reg_email);
        password = (TextInputLayout) findViewById(R.id.reg_password);
        register = (Button) findViewById(R.id.regist_btn);
        no_hp = (TextInputLayout) findViewById(R.id.reg_no_hp);
        alamat = (TextInputLayout) findViewById(R.id.reg_alamat);
        mRadioGroup = (RadioGroup) findViewById(R.id.reg_JenisLK);
        mRadioButton_lk = (RadioButton) findViewById(R.id.perempuan);
        mRadioButton_pr = (RadioButton) findViewById(R.id.laki);
        mSpinner = findViewById(R.id.reg_jenis_user);
        spinneradapter = ArrayAdapter.createFromResource(this, R.array.jenis_user, android.R.layout.simple_spinner_item);
        spinneradapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinneradapter);
        mSpinner.setOnItemSelectedListener(this);



        mpProgressDialog = new ProgressDialog(this);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String edisplayname = displayname.getEditText().getText().toString();
                String eemail = email.getEditText().getText().toString();
                String epassword = password.getEditText().getText().toString();
                String eno_hp = no_hp.getEditText().getText().toString();
                String ealamat = alamat.getEditText().getText().toString();
                String jenislk = "";
                int jenisid = mRadioGroup.getCheckedRadioButtonId();
                if (jenisid==mRadioButton_lk.getId()){
                    jenislk = mRadioButton_lk.getText().toString();
                }else if (jenisid==mRadioButton_pr.getId()){
                    jenislk = mRadioButton_pr.getText().toString();
                }else {
                    jenislk = "bukan apa2";
                }

                if (sjenisuser.equalsIgnoreCase("Dokter")){
                    skategoripengguna = sjenisuser;
                    edisplayname = "Dr. "+edisplayname;
                }else {
                    skategoripengguna = sjenisuser;
                }





                if(!TextUtils.isEmpty(edisplayname)&& !TextUtils.isEmpty(eemail)&& !TextUtils.isEmpty(epassword)
                        && !TextUtils.isEmpty(eno_hp)&& !TextUtils.isEmpty(ealamat)&& !TextUtils.isEmpty(jenislk) ) {
                    mpProgressDialog.setTitle("Creating new acount..");
                    mpProgressDialog.setMessage("Please wait.. while we create your acount..");
                    mpProgressDialog.setCanceledOnTouchOutside(false);
                    mpProgressDialog.show();
                    register_user(edisplayname, eemail, epassword,eno_hp,ealamat,skategoripengguna,jenislk);
                }else{
                    mpProgressDialog.hide();
                    Toast.makeText(Registrasi.this,"Harap isi seluruh form",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void register_user(final String edisplayname, final String eemail, String epassword, final String eno_hp, final String ealamat, String euser, final String jenislk) {
        mAuth.createUserWithEmailAndPassword(eemail,epassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = currentUser.getUid();

                    //child first is root child,then second child
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);



                    HashMap<String,String> userMap = new HashMap<>();
                    userMap.put("name",edisplayname);
                    userMap.put("status","Harus Semangat. Rajin Minum Obat");
                    userMap.put("image","default");
                    userMap.put("thumb_image","default");
                    userMap.put("email",eemail);
                    userMap.put("no_hp",eno_hp);
                    userMap.put("address",ealamat);
                    userMap.put("Jenis_kelamin",jenislk);
                    userMap.put("uid",mAuth.getUid() );
                    userMap.put("kategori_pengguna", skategoripengguna);

                    mDatabaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mpProgressDialog.dismiss();
                                Intent intent = new Intent(Registrasi.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                mpProgressDialog.hide();
                                Toast.makeText(Registrasi.this, "Terjadi Kesalahan !!, periksa jaringan anda dan coba lagi", Toast.LENGTH_LONG).show();

                            }

                        }
                    });


                    /*mpProgressDialog.dismiss();
                    Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();*/

                }else{
                    mpProgressDialog.hide();
                    Toast.makeText(Registrasi.this,"Registrasi dengan FirebaseAuth failed ..",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
       sjenisuser = adapterView.getItemAtPosition(i).toString();
       /*Toast.makeText(Registrasi.this, sjenisuser, Toast.LENGTH_LONG).show();*/

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}

