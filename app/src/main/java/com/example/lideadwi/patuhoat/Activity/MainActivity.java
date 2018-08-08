package com.example.lideadwi.patuhoat.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.lideadwi.patuhoat.Menu.Artikel;
import com.example.lideadwi.patuhoat.Menu.Chats;
import com.example.lideadwi.patuhoat.Menu.Home;
import com.example.lideadwi.patuhoat.R;
import com.example.lideadwi.patuhoat.Menu.Settings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {


    private FrameLayout mFrameLayout;

    //FRAGMENT NAVIGATION
    private Home home;
    private Artikel artikel;
    private Chats chats;
    private Settings settings;

    //fIREBASE
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserRef;

    private Toolbar mToolbar;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setFragment(home);

                case R.id.navigation_artikel:
                    setFragment(artikel);
                    return true;
                case R.id.navigation_chats:
                    setFragment(chats);
                    return true;
                case R.id.navigation_settings:
                    setFragment(settings);
                    return true;

                default:

                        setFragment(home);


            }
            return false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = findViewById(R.id.maintoolbar);
        setSupportActionBar(mToolbar);

        mFrameLayout = findViewById(R.id.main_frame);

        home = new Home();
        artikel = new Artikel();
        chats = new Chats();
        settings = new Settings();



        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {


            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }

    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);


        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.logout_menu_main) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

            FirebaseAuth.getInstance().signOut();
            sendTostart();
        } else if (item.getItemId() == R.id.acount_setting_main) {
            Intent i = new Intent(MainActivity.this, AcountSettings.class);
           /* i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);*/
            startActivity(i);
        } else if (item.getItemId() == R.id.All_user) {
            Intent i = new Intent(MainActivity.this, AllUserActivity.class);
           // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

        }
        return true;
    }
    private void sendTostart() {
        //Check i user is Sign-out
        Intent startIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(startIntent);
        finish();

    }

    @Override
    public void onStart() {
        super.onStart();
        /*FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){

            sendTostart();

        } else {

            mUserRef.child("online").setValue("true");

        }*/
        setFragment(home);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (!(currentUser != null)) {
            // !User is signed in
            sendTostart();


        } else {
            mUserRef.child("online").setValue("true");

        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

}
