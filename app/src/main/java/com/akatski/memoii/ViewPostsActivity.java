package com.akatski.memoii;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.mbms.StreamingServiceInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;

public class ViewPostsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener , AdapterView.OnItemLongClickListener
 {
 private ListView postsListView;
 private ArrayList<String> usernames;
 private ArrayAdapter adapter;
 private FirebaseAuth firebaseAuth;
 private ImageView postsImageView;
 private TextView txtDescription;
 private ArrayList<DataSnapshot> mDataSnapshots;
 private TextView text_description;
 private Button btn_MemeSearch;
  private    boolean isImageFitToScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);
        firebaseAuth=FirebaseAuth.getInstance();
        postsListView=findViewById(R.id.postsListView);

        txtDescription=findViewById(R.id.txtdescription);
        postsImageView=findViewById(R.id.postsImageView);
        text_description=findViewById(R.id.text_Description);
        //using button
        btn_MemeSearch=findViewById(R.id.btn_MemeSearch);
        btn_MemeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openWebViewAcitvity();
            }
        });
        //on clicking on image view
        postsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ViewPostsActivity.this, ImageFullSizeActivity.class);
                Drawable drawable=postsImageView.getDrawable();
                Bitmap bitmap= ((BitmapDrawable)drawable).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] b = baos.toByteArray();

                i.putExtra("url",  b);
                startActivity(i);

            }
        });



       usernames=new ArrayList<String>();
       mDataSnapshots=new ArrayList<>();
        adapter=new ArrayAdapter(this, android.R.layout.simple_list_item_1,usernames);
        postsListView.setOnItemClickListener(this);
        postsListView.setAdapter(adapter);
        postsListView.setOnItemLongClickListener(this);
        FirebaseDatabase.getInstance().getReference().child("app_users")
                .child(firebaseAuth.getCurrentUser().getUid()).child("received_posts")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                      mDataSnapshots.add(dataSnapshot);
                        String fromWhomUsername= (String)dataSnapshot.child("fromWhom").getValue();
                      usernames.add(fromWhomUsername);
                      adapter.notifyDataSetChanged();



                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        int i = 0;

                        for (DataSnapshot snapshot :mDataSnapshots) {

                            if (snapshot.getKey().equals(dataSnapshot.getKey())) {

                                mDataSnapshots.remove(i);
                                usernames.remove(i);

                            }

                            i++;

                        }
                        adapter.notifyDataSetChanged();
                        postsImageView.setImageResource(R.drawable.ic_home);
                        text_description.setText("");

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         DataSnapshot mydatasnapshot= mDataSnapshots.get(position);
        String downloadmlink=(String) mydatasnapshot.child("imageLink").getValue();
        Picasso.get().load( downloadmlink).into(postsImageView);
        text_description.setText((String)mydatasnapshot.child("des").getValue());


    }


     @Override
     public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
         AlertDialog builder = new AlertDialog.Builder(this)
                 .setTitle("Delete entry")
                 .setMessage("Are you sure this is worth deleting?")



                 .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         // Continue with delete operation
                         FirebaseStorage.getInstance().getReference()
                                 .child("myImages").child((String)mDataSnapshots
                                 .get(position).child("imageIdentifier").getValue())
                                 .delete();
                         FirebaseDatabase.getInstance().getReference()
                                 .child("app_users").child(FirebaseAuth.getInstance().getCurrentUser()
                                 .getUid()).child("received_posts")
                                 .child(mDataSnapshots.get(position).getKey()).removeValue();

                     }
                 })

                 // A null listener allows the button to dismiss the dialog and take no further action.
                 .setNegativeButton(android.R.string.no, null)
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .show();
        return false;
     }
     public void  openWebViewAcitvity()
     {
         Intent intent=new Intent(this,WebViewActivity.class);
         startActivity(intent);
     }
 }
