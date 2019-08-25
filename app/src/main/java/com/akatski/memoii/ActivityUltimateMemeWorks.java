package com.akatski.memoii;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class ActivityUltimateMemeWorks extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Button vButtonChooseImage;
    private Button vButtonUpload;
    private TextView aShowUploads;
    private EditText aFileName;
    private ImageView aImageView;
    private ProgressBar mProgressBar;
    private StorageReference vStorageRef;
    private DatabaseReference vDatabaseRef;
    private StorageTask mUploadTask;
    private String imageIdentifier;


    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ultimate_meme_works);

        vButtonChooseImage = findViewById(R.id.button_choose_image);
        vButtonUpload = findViewById(R.id.button_upload);
        aShowUploads = findViewById(R.id.text_view_show_uploads);
        aFileName = findViewById(R.id.edit_text_file_name);
        imageIdentifier = UUID.randomUUID().toString() + ".png";
        aImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);
        vStorageRef = FirebaseStorage.getInstance().getReference("UMWuploads").child("images").child(imageIdentifier);
        vDatabaseRef = FirebaseDatabase.getInstance().getReference("UMWuploads");


        vButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        vButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(ActivityUltimateMemeWorks.this, "Upload in progress", Toast.LENGTH_LONG).show();
                } else {

                    uploadFile();
                }

            }
        });

        aShowUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagesActivity();

            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();

            Picasso.get().load(mImageUri).into(aImageView);
        }
    }

    private void uploadFile() {
        imageIdentifier = UUID.randomUUID().toString() + ".png";
        vStorageRef = FirebaseStorage.getInstance().getReference("UMWuploads").child(imageIdentifier);
        if (mImageUri != null) {
            vStorageRef.putFile(mImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return vStorageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Toast.makeText(ActivityUltimateMemeWorks.this, "uploading", Toast.LENGTH_SHORT).show();
                        Log.e("memoii", "then: " + downloadUri.toString());
                        Upload upload = new Upload(aFileName.getText().toString().trim(),
                                downloadUri.toString());
                        vDatabaseRef.push().setValue(upload);
                    } else {
                        Toast.makeText(ActivityUltimateMemeWorks.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ActivityUltimateMemeWorks.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
           vStorageRef.getStream().addOnProgressListener(new OnProgressListener<StreamDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.i("hello", "onProgress:" +
                            "bytesTransferred:" + taskSnapshot.getBytesTransferred() +
                            "|totalByteCount:" + taskSnapshot.getTotalByteCount());
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mProgressBar.setProgress((int) progress);
                }
            });

        }
    }
    private void openImagesActivity() {
        Intent intent = new Intent(this, ImagesActivity.class);
        startActivity(intent);
    }
}
