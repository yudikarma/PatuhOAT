package com.example.lideadwi.patuhoat.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lideadwi.patuhoat.Adapter.MessageAdapter;
import com.example.lideadwi.patuhoat.Activity.Menu.Chats;
import com.example.lideadwi.patuhoat.Model.GetTimeAgo;
import com.example.lideadwi.patuhoat.Model.Messages;
import com.example.lideadwi.patuhoat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ChatsRoom extends AppCompatActivity {
    private Toolbar mtoolbar;
    private String mChatuser;
    private String musername;
    private DatabaseReference mRootDatabaseReference;

    private TextView mTitleView;
    private TextView mLastView;
    private CircleImageView mProfilImage;
    // private DatabaseReference firebaseUser;
    private DatabaseReference mUserRef;


    private RecyclerView mMessagesList;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private ImageButton mChatSendBtn;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private EditText mChatMessageView;
    private TextView time_text_layout;
    private SwipeRefreshLayout mRefreshLayout;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private static final int GALLERY_PICK = 1;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    static final int REQUEST_IMAGE_CAPTURE = 1;



    // Storage Firebase
    private StorageReference mImageStorage;


    //New Solution
    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";
    private ImageButton mChatAddBtn;

    //VARIABEL UNTUK TAKE PICTURE
    private Uri imageUri;
    private Uri newsImageUri;
    private File photofile = null;
    private File thumb_file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_room);

        //Navigation Toolbar
        mtoolbar = (Toolbar) findViewById(R.id.tampungchat_app_bar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mtoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        /* ==========Custom action bar item==========*/
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(actionbar_view);



        /* =================Get informasi user dari friend fragment ==========*/
        mChatuser = getIntent().getStringExtra("user_id");
        musername = getIntent().getStringExtra("user_name");


        /* =======================Inisialisasi Layout =============*/
        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastView = (TextView) findViewById(R.id.custom_bar_seen);
        mProfilImage = (CircleImageView) findViewById(R.id.custom_bar_image);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        time_text_layout = findViewById(R.id.time_text_layout);


        /*==================FIREBASE Insisualisasi =================*/

        //inisialisasi root database refference
        mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //inisilisasi get currrent user (User yang login)
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();


        /*===========Adapter FOR TAMPILIN PESAN ============*/
        mAdapter = new MessageAdapter(messagesList);
        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());


        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mRootDatabaseReference.child("Chat").child(mCurrentUserId).child(mChatuser).child("seen").setValue(true);

        /* ==========CallLog FUNCTION FirebaseStorage =========*/
        loadMessages();
        mTitleView.setText(musername);



        /*  ====== MENDAPATKAN INFORMASI DARI DATAABASE TENTANG USER YANG DI chAT ====*/
        mRootDatabaseReference.child("Users").child(mChatuser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                //jika dia online
                if (online.equals("true")) {
                    mLastView.setText("Online");
                } else {

                    //mendapatkan waktu kapan terakhir dia online
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    //settext kapan terakhir dia online
                    mLastView.setText(lastSeenTime);

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /* ==========APAKAH PESAN SUDAH DI BACA (Unread) =========*/
        mRootDatabaseReference.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatuser)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatuser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatuser + "/" + mCurrentUserId, chatAddMap);

                    mRootDatabaseReference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /* ========== Action Kirim pesan =========*/
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                // loadMessages();
            }
        });


        /* ==========Action Intent Get Image Form Galery (Tidak DI PAKAI)=========*/
        //TAKE PICTURE FROM GALERY PICK
       /* mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });*/



        /* ==========Action Intent Pick Image Form CAMERA =========*/
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //CHEK PERMISSION ACCES CAMERA
                if (ContextCompat.checkSelfPermission(ChatsRoom.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) {

                    //Meminta Akses Camera
                    ActivityCompat.requestPermissions(ChatsRoom.this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);

                }
                //CHEK Permission MENYIMPAN FILE (Storage)
                else if (ContextCompat.checkSelfPermission(ChatsRoom.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED) {

                    //MEMINTA ACCES
                    ActivityCompat.requestPermissions(ChatsRoom.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_IMAGE_CAPTURE);
                } else {


                    /* ==========Jika AKses diberikan, maka penggil Intent Camera =========*/
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                        //Generate Name
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        photofile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), timeStamp + "anu.jpg");

                        //Chek photofile tidak null;
                        if (photofile != null) {

                            //Mendapatkan Alamat URi dari foto File
                            imageUri = Uri.fromFile(photofile);

                            //Mengirim ALamat Uri
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                            //Memanggil Activity Onresult dari Camera Intent
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);


                        } else {
                            Toast.makeText(ChatsRoom.this, "PHOTO FILE NULL", Toast.LENGTH_LONG).show();
                        }

                    }
                }


            }
        });


        /* ==========Method Refresh recycleview =========*/
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                itemPos = 0;

                loadMoreMessages();


            }
        });
    }


    //Onactivity Result dari Intent Camera && Galery or Anyting (Tergantung request code)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Jika Request nya ImageCapture(Camera) dan user menekan OK (Selesai mengambil foto)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            //jika image Uri tidak kosong
            if (imageUri != null) {

                try {

                    //Kompress File asal kedalam File Baru dengan ukuran lebih ringan
                    File newsfile = new Compressor(ChatsRoom.this)
                            .compressToFile(photofile);

                    //Mendapatkan Uri file baru setelah di kompress
                    newsImageUri = Uri.fromFile(newsfile);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Path penyimpanan message disimpan untuk current user
                final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatuser;

                //Path penyimpanan message untuk other user
                final String chat_user_ref = "messages/" + mChatuser + "/" + mCurrentUserId;

                //mengenerate CHATS Id
                DatabaseReference user_message_push = mRootDatabaseReference.child("messages")
                        .child(mCurrentUserId).child(mChatuser).push();

                //Mendapatkan hasil generate CHATs ID
                final String push_id = user_message_push.getKey();


                //Path penyimpanan FILE Pada FIrebas Storage
                StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");

                /* ========== Method upload file ke Firebase Storage =========*/
                filepath.putFile(newsImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            //menthod mendapatkan URi download gambar
                            String download_url = task.getResult().getDownloadUrl().toString();

                            //simapn hasil kedatabase
                            Map messageMap = new HashMap();
                            messageMap.put("message", download_url);
                            messageMap.put("seen", false);
                            messageMap.put("type", "image");
                            messageMap.put("time", ServerValue.TIMESTAMP);
                            messageMap.put("from", mCurrentUserId);

                            Map messageUserMap = new HashMap();
                            //struktur database simpan message untku current user
                            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);

                            //struktur database simpan message untku current user
                            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                            mChatMessageView.setText("");

                            mRootDatabaseReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if (databaseError != null) {

                                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                    }

                                }
                            });


                        }

                    }
                });

            } else {
                Toast.makeText(ChatsRoom.this, "URI IS NULL", Toast.LENGTH_LONG).show();
            }

        }
    }

    /* ========== Method laod message on Refresh swipe =========*/
    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootDatabaseReference.child("messages").child(mCurrentUserId).child(mChatuser);
        messageRef.keepSynced(true);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {
                    messagesList.add(itemPos++, message);
                } else {
                    mPrevKey = mLastKey;
                }

                if (itemPos == 1) {
                    mLastKey = messageKey;
                }
                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(itemPos, 0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    /* ========== Method Load Message Ketika BUkan Swipe refresh =========*/
    private void loadMessages() {

        DatabaseReference messageRef = mRootDatabaseReference.child("messages").child(mCurrentUserId).child(mChatuser);
        messageRef.keepSynced(true);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);


                itemPos++;

                if (itemPos == 1) {

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                // setTime_text_layout(Long.parseLong(message.getMessage()));

                mMessagesList.scrollToPosition(messagesList.size() - 1);

                mRefreshLayout.setRefreshing(false);


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        //Set status online
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mUserRef.child("online").setValue("true");

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {

        //Online is Over
        super.onDestroy();
        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
    }


    /* ========== Method Mengirim pesan (Ketika Send Button Click) =========*/
    private void sendMessage() {

        String message = mChatMessageView.getText().toString();
        if (!TextUtils.isEmpty(message)) {

            //Path simpan message untuk current user (User yang login)
            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatuser;
            //path simpan message untuk other user
            String chat_user_ref = "messages/" + mChatuser + "/" + mCurrentUserId;

            //generate Message ID
            DatabaseReference user_message_push = mRootDatabaseReference.child("messages").child(mCurrentUserId).child(mChatuser).push();
            //Get Message ID
            String push_id = user_message_push.getKey();

            //Map untuk menampung message
            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            //Map Untuk menyimpan Message dalam database secara sekalian
            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap); //Path simpan untuk current user
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);      //path simpan untk other user

            mChatMessageView.setText(" ");

            //Set kapan pessan dibaca
            mRootDatabaseReference.child("Chat").child(mCurrentUserId).child(mChatuser).child("seen").setValue(true);
            mRootDatabaseReference.child("Chat").child(mCurrentUserId).child(mChatuser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootDatabaseReference.child("Chat").child(mChatuser).child(mCurrentUserId).child("seen").setValue(false);
            mRootDatabaseReference.child("Chat").child(mChatuser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootDatabaseReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                    // loadMessages();
                }
            });
        }

    }

    /* ========== Method SetTime Textlayout =========*/
    public void setTime_text_layout(final long timesend) {
        time_text_layout.setText(String.valueOf(timesend));
    }

}
