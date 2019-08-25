package com.akatski.memoii;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;


public class UMWFullSizeImage extends AppCompatActivity {

    private ImageView umw_ImageView;
    private Context mContext;
    private Button umw_ShareBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_umwfull_size_image);
        umw_ImageView = findViewById(R.id.umw_ImageView);
        mContext = UMWFullSizeImage.this;
        umw_ShareBtn = findViewById(R.id.umw_ShareBtn);


        Intent i = getIntent();
        String url = i.getStringExtra("url");
        Picasso.get().load(url).into(umw_ImageView);

        umw_ShareBtn.setOnClickListener(new View.OnClickListener() {
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
                                getLocalBitmapUri(umw_ImageView);



                            }
                        }




                    }
                }




            }
        });


    }
    private void getLocalBitmapUri(ImageView imageView) {

        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setMessage("Preparing your stuff");
        dialog.show();
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            // null
            dialog.dismiss();
            Toast.makeText(mContext, "Error occurred\nPlease try again", Toast.LENGTH_SHORT).show();
        }
        // Store image to default external storage directory
        Uri bmpUri = null;

        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");

// wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
            bmpUri = FileProvider.getUriForFile(UMWFullSizeImage.this, "com.codepath.fileprovider", file);
            // above commented file name is because i want to replace the single file everytime
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();


            // considering I have the file URI
            if (bmpUri != null) {
                // Construct a ShareIntent with link to image
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                try {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Caption : "  + "\nDownload memoi for tons of other memes :)\nLink : "+ mContext.getResources().getString(R.string.app_name));
                } catch (Exception e) {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Download memoi for tons of other memes :)\nLink : " + mContext.getResources().getString(R.string.app_name));
                }

                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/*");
                // Launch sharing dialog for image
                dialog.dismiss();
                mContext.startActivity(Intent.createChooser(shareIntent, "Share smiles via"));
            } else {
                dialog.dismiss();
                Toast.makeText(mContext, "Error occurred" +
                        "\n" +
                        "Please try again", Toast.LENGTH_SHORT).show();
            }


        } catch (IOException e) {
            e.printStackTrace();
            dialog.dismiss();
            Toast.makeText(mContext, "Error occurred\nPlease try again", Toast.LENGTH_SHORT).show();
        }
    }








}













