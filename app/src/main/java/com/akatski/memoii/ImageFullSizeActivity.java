package com.akatski.memoii;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageFullSizeActivity extends AppCompatActivity {
    private ImageView cardviewimage;
    private Button sharebtn;
    private Context mContext = ImageFullSizeActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full_size);
        cardviewimage = findViewById(R.id.cardviewimage);
        sharebtn = findViewById(R.id.sharebtn);


        Bundle extras = getIntent().getExtras();
        byte[] b = extras.getByteArray("url");
        Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
        cardviewimage.setImageBitmap(bmp);
        sharebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) mContext, new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    } else {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions((Activity) mContext, new String[]{
                                        Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                            } else {
                                getLocalBitmapUri(cardviewimage);


                            }
                        }


                    }
                }


            }
        });
    }

    private void getLocalBitmapUri(ImageView imageView) {

        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setMessage("Preparing your MEME!!");
        dialog.show();

        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {

            dialog.dismiss();
            Toast.makeText(mContext, "Error occurred\nPlease try again", Toast.LENGTH_SHORT).show();
        }

        Uri bmpUri = null;

        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");


            bmpUri = FileProvider.getUriForFile(ImageFullSizeActivity.this, "com.codepath.fileprovider", file);

            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();


            if (bmpUri != null) {

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                try {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Caption : " + "\nDownload memoii if you want of other memes :)\nLink : " + mContext.getResources().getString(R.string.app_name));
                } catch (Exception e) {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Download memoii if you love memes \nLink : " + mContext.getResources().getString(R.string.app_name));
                }

                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/*");

                dialog.dismiss();
                mContext.startActivity(Intent.createChooser(shareIntent, "Share memes via"));
            } else {
                dialog.dismiss();
                Toast.makeText(mContext, "Error occurred\nPlease try again", Toast.LENGTH_SHORT).show();
            }


        } catch (IOException e) {
            e.printStackTrace();
            dialog.dismiss();
            Toast.makeText(mContext, "Error occurred\nPlease try again", Toast.LENGTH_SHORT).show();
        }
    }


}


