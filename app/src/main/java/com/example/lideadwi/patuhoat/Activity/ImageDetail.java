package com.example.lideadwi.patuhoat.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.lideadwi.patuhoat.R;
import com.squareup.picasso.Picasso;

public class ImageDetail extends AppCompatActivity {
    private ImageView mImageView;
    private    Picasso picasso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        mImageView = findViewById(R.id.full_image);

        //Getting Image Uri then show in imageview
        String urlImage = getIntent().getStringExtra("url_image");
        picasso.with(ImageDetail.this).load(urlImage).placeholder(R.drawable.default_avatar).into(mImageView);
    }
}
