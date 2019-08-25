package com.akatski.memoii;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ImagesActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private ImageAdapter adapter;
  private FirebaseStorage storage;
  private ValueEventListener mDBListener;
    private ProgressBar mProgressCircle;

    URL url;
    private String random;
    private UUID mUUID;
    private String downloaddir;
    private boolean isImageFitToScreen;
    Context mContext;
    String filepath;
  DownloadManager downloadManager;
    Long lastDownload = -1L;
    private DatabaseReference mDatabaseRef;
    private List<Upload> uploads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(30);
        mContext=ImagesActivity.this;



        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        storage = FirebaseStorage.getInstance();
        downloaddir="Internal storage/Download";

        mProgressCircle = findViewById(R.id.progress_circle);

        uploads = new ArrayList<>();
        adapter = new ImageAdapter(ImagesActivity.this, uploads);

        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(ImagesActivity.this);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UMWuploads");




       mDBListener= mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uploads.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setKey(postSnapshot.getKey());
                    uploads.add(upload);
                }
             adapter.notifyDataSetChanged();
                mProgressCircle.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ImagesActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                mProgressCircle.setVisibility(View.INVISIBLE);

            }
        });




    }


    @Override
    public void onItemClick(int position) {
        Toast.makeText(this, "Normal click at position: " + position, Toast.LENGTH_SHORT).show();
        Upload selectedItem= uploads.get(position);
        final String selectedkey= selectedItem.getKey();
        StorageReference imgRef= storage.getReferenceFromUrl(selectedItem.getImageUrl());

        Intent i = new Intent(ImagesActivity.this, UMWFullSizeImage.class);

        i.putExtra("url", selectedItem.getImageUrl());
        startActivity(i);


    }

    @Override
    public void onWhatEverClick(int position) {
        Toast.makeText(mContext, "hello there", Toast.LENGTH_LONG).show();




    }

    @Override
    public void onDownloadClick(int position) {
        Toast.makeText(this, "Downloading Started", Toast.LENGTH_LONG).show();


        Upload selectedItem = uploads.get(position);
        final String selectedkey = selectedItem.getKey();
        StorageReference imgRef = storage.getReferenceFromUrl(selectedItem.getImageUrl());



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {

                saveImage(selectedItem.getImageUrl());
            }
        }
    }

    private void saveImage(String url) {

        String timestamp = String.valueOf(new Date().getDate()) + String.valueOf(new Date().getMonth()) + String.valueOf(new Date().getYear()) + "_" + String.valueOf(new Date().getHours()) + ":" + String.valueOf(new Date().getMinutes()) + ":" + String.valueOf(new Date().getSeconds());
        downloadManager = (DownloadManager) mContext.getSystemService(mContext.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).mkdirs();
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.allowScanningByMediaScanner();
        lastDownload = downloadManager.enqueue(request
                .setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_MOBILE | android.app.DownloadManager.Request.NETWORK_WIFI)
                .setAllowedOverRoaming(true)
                .setTitle("Memoii")
                .setDescription("Apke MEMES")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES + "/memoii", "MaYmAy_" + timestamp + ".jpg")
                .setVisibleInDownloadsUi(true)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED));
        Toast.makeText(mContext, "Saved to  meme gallery", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {
        Upload selectedItem= uploads.get(position);
        final String selectedkey= selectedItem.getKey();
        StorageReference imgRef= storage.getReferenceFromUrl(selectedItem.getImageUrl());
        imgRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedkey).removeValue();
                Toast.makeText(ImagesActivity.this, "deleted", Toast.LENGTH_LONG).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ImagesActivity.this, "deleting  failed", Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    protected void onDestroy() {
        mDatabaseRef.removeEventListener(mDBListener);
        super.onDestroy();
    }

    private void getLocalBitmapUri(ImageView imageView, TextView caption) {

        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setMessage("Preparing your MEME stash");
        dialog.show();

        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {

            dialog.dismiss();
            Toast.makeText(mContext, "An error occurred\nPlease try again later", Toast.LENGTH_SHORT).show();
        }

        Uri bmpUri = null;

        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" +System.currentTimeMillis() + ".png");

            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            bmpUri = Uri.fromFile(file);


            if (bmpUri != null) {

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                try {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "MEME-Caption : " + caption.getText().toString() + "\n\nDownload memoi for tons of other memes :)\nLink : "+ mContext.getResources().getString(R.string.app_name));
                } catch (Exception e) {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Download memoi for tons of other memes :)\nLink : " + mContext.getResources().getString(R.string.app_name));
                }

                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/*");

                dialog.dismiss();
                mContext.startActivity(Intent.createChooser(shareIntent, "Share smiles via"));
            } else {
                dialog.dismiss();
                Toast.makeText(mContext, "An error occurred\nPlease try again later", Toast.LENGTH_SHORT).show();
            }


        } catch (IOException e) {
            e.printStackTrace();
            dialog.dismiss();
            Toast.makeText(mContext, " An error occurred\nPlease try again later", Toast.LENGTH_SHORT).show();
        }
    }





}
