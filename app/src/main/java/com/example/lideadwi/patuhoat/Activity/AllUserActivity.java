package com.example.lideadwi.patuhoat.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lideadwi.patuhoat.Model.Users;

import com.example.lideadwi.patuhoat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUserActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mListView;
    private String nambahaja;
    private DatabaseReference mDatabaseReference;
    //private FirebaseListAdapter adapter;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseRecyclerAdapter<Users,UserviewHolder> adapter;

    private ProgressDialog mProgressDialog;
    private ImageView statusOnline;
    private TextView notifnull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user);

        //database ref users
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseReference.keepSynced(true);

        mToolbar = (Toolbar) findViewById(R.id.user_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });
        statusOnline = (ImageView) findViewById(R.id.user_single_online_icon);

        notifnull = findViewById(R.id.notifnulluser);

        //Recycle view
        mListView = (RecyclerView) findViewById(R.id.user_list);
        mListView.setHasFixedSize(true);
        mListView.setLayoutManager(new LinearLayoutManager(this));

        //set query
        Query query = FirebaseDatabase.getInstance().getReference().child("Users").limitToLast(50);

        //set Options
        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, Users.class)
                        .setLifecycleOwner(this)
                        .build();
        //Set adapter from Recycle firebased database ui
        adapter = new FirebaseRecyclerAdapter<Users, UserviewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserviewHolder holder, int position, @NonNull Users model) {
                holder.setNama(model.getName());
                final String nama = model.getName();
                holder.setstatus(model.getAddress());
                holder.setMcCircleImageView(model.getThumb_image());





                final String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        if (!user_id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                            Intent i = new Intent(AllUserActivity.this, ProfilActivity.class);
                            i.putExtra("user_id", user_id);
                            startActivity(i);
                        }else {
                            Intent i = new Intent(AllUserActivity.this, AcountSettings.class);
                            i.putExtra("user_id", user_id);
                            startActivity(i);
                        }

                    }
                });
            }

            @NonNull
            @Override
            public UserviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_singgle_layout, parent, false);
                return new UserviewHolder(mView);
            }
        };
        //if data != null setadapter to friendlist
        FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long jumlah = dataSnapshot.getChildrenCount();
                if (jumlah>0){
                    mListView.setAdapter(adapter);
                }else {
                    notifnull.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();


        //important for fibrebase database ui
        adapter.startListening();



    }

    @Override
    protected void onStop() {
        super.onStop();
        //important for fibrebase database ui
        adapter.stopListening();
    }

    //VIEW HOLDER
    public class UserviewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView mdisplayname,mstatus ;
        CircleImageView mcCircleImageView;
        public UserviewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mdisplayname = (TextView) mView.findViewById(R.id.user_singgle_name);
            mstatus  = (TextView) mView.findViewById(R.id.user_single_status);
            mcCircleImageView = (CircleImageView) mView.findViewById(R.id.profil_single_layout);



        }
        public void setNama(String display_name){

            mdisplayname.setText(display_name);

        }
        public void setstatus(String status){
            mstatus.setText(status);
        }
        public  void setMcCircleImageView(final String img_uri){
            //Picasso.with(UsersActivity.this).load(img_uri).placeholder(R.drawable.user).into(mcCircleImageView);
            if (!img_uri.equals("default")){
                Picasso.with(AllUserActivity.this).load(img_uri).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar).into(mcCircleImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(AllUserActivity.this).load(img_uri).placeholder(R.drawable.default_avatar).into(mcCircleImageView);

                    }
                });

            }else{
                Picasso.with(AllUserActivity.this).load(R.drawable.default_avatar).into(mcCircleImageView);

            }
        }
    }
}
