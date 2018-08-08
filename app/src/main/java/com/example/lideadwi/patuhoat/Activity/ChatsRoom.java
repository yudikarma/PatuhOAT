package com.example.lideadwi.patuhoat.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import com.example.lideadwi.patuhoat.Menu.Chats;
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
    Uri imageUri;
    Uri newsImageUri;
    String imageFilePath;
    File photofile = null;
    Bitmap  thum_bitmap;
    File thumb_file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_room);


        mtoolbar = (Toolbar) findViewById(R.id.tampungchat_app_bar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mtoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatsRoom.this, Chats.class);

                startActivity(intent);
                finish();
            }
        });
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(actionbar_view);

        /* ==========Custom action bar item==========*/


        mChatuser = getIntent().getStringExtra("user_id");
        musername = getIntent().getStringExtra("user_name");
        /* =================Get informasi user dari friend fragment ==========*/

        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastView = (TextView) findViewById(R.id.custom_bar_seen);
        mProfilImage = (CircleImageView) findViewById(R.id.custom_bar_image);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        time_text_layout = findViewById(R.id.time_text_layout);

        /* =======================Inisialisasi Layout =============*/


        mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();
        // firebaseUser = FirebaseDatabase.getInstance().getReference().child("User");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        /*==================FIREBASE Insisualisasi =================*/

        mAdapter = new MessageAdapter(messagesList);
        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());


        /*===========Adapter FOR TAMPILIN PESAN ============*/

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
                /* String image = dataSnapshot.child("image").getValue().toString();*/


                /*setMcCircleImageView(image);*/

                if (online.equals("true")) {
                    mLastView.setText("Online");
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    mLastView.setText(lastSeenTime);

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                // loadMessages();
            }
        });


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

        //TAKE FOTO FROM CAMERA
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(ChatsRoom.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(ChatsRoom.this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);

                }
               else if (ContextCompat.checkSelfPermission(ChatsRoom.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED){
                    ActivityCompat.requestPermissions(ChatsRoom.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_IMAGE_CAPTURE);
                }else {

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {


                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        photofile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),timeStamp+"anu.jpg");







                        if (photofile != null){



                                Log.i("Thum File", ""+thumb_file);
                                imageUri = Uri.fromFile(photofile);


                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);


                        }else {
                            Toast.makeText(ChatsRoom.this, "PHOTO FILE NULL", Toast.LENGTH_LONG).show();
                        }

                    }
                }


            }
        });


        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                itemPos = 0;

                loadMoreMessages();


            }
        });
    }
    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "PatuhAOT");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {


            /* imageUri = data.getData();*/


            if (imageUri != null) {

                try {
                    File newsfile = new Compressor(ChatsRoom.this)
                            .compressToFile(photofile);
                    newsImageUri = Uri.fromFile(newsfile);

                } catch (IOException e) {
                    e.printStackTrace();
                }


                final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatuser;
                final String chat_user_ref = "messages/" + mChatuser + "/" + mCurrentUserId;

                DatabaseReference user_message_push = mRootDatabaseReference.child("messages")
                        .child(mCurrentUserId).child(mChatuser).push();

                final String push_id = user_message_push.getKey();


                StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");

                filepath.putFile(newsImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            String download_url = task.getResult().getDownloadUrl().toString();


                            Map messageMap = new HashMap();
                            messageMap.put("message", download_url);
                            messageMap.put("seen", false);
                            messageMap.put("type", "image");
                            messageMap.put("time", ServerValue.TIMESTAMP);
                            messageMap.put("from", mCurrentUserId);

                            Map messageUserMap = new HashMap();
                            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
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




    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootDatabaseReference.child("messages").child(mCurrentUserId).child(mChatuser);

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

                mLinearLayout.scrollToPositionWithOffset(10, 0);

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

    private void loadMessages() {

        DatabaseReference messageRef = mRootDatabaseReference.child("messages").child(mCurrentUserId).child(mChatuser);

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


        FirebaseUser currentUser = mAuth.getCurrentUser();

        mUserRef.child("online").setValue("true");

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
    }


    private void sendMessage() {

        String message = mChatMessageView.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatuser;
            String chat_user_ref = "messages/" + mChatuser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootDatabaseReference.child("messages").child(mCurrentUserId).child(mChatuser).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatMessageView.setText(" ");

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


    public void setTime_text_layout(final long timesend) {
        time_text_layout.setText(String.valueOf(timesend));
    }

}
