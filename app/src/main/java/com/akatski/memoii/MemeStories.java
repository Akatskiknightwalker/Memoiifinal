package com.akatski.memoii;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.akatski.memoii.utils.IFirebaseLoadDone;
import com.akatski.memoii.utils.meme;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class MemeStories extends AppCompatActivity implements IFirebaseLoadDone {


    StoriesProgressView storiesProgressView;
    ImageView storyimage;
    Button load_btn, pause_btn, reverse_btn, resume_btn;
    int counter = 0;
    DatabaseReference dbref;
    IFirebaseLoadDone mIFirebaseLoadDone;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_stories);
        dbref = FirebaseDatabase.getInstance().getReference("Memes");
        mIFirebaseLoadDone = this;

        load_btn = findViewById(R.id.load_btn);
        resume_btn = findViewById(R.id.resume_btn);
        pause_btn = findViewById(R.id.pause_btn);
        reverse_btn = findViewById(R.id.reverse_btn);
        storiesProgressView = findViewById(R.id.stories);
       storyimage = findViewById(R.id.storyimage);



        storyimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        reverse_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();

            }
        });
        pause_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.pause();
            }
        });
        resume_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.resume();
            }
        });
        load_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<meme> memes=new ArrayList<>();
                        for(DataSnapshot itemsnapshot:dataSnapshot.getChildren())
                        {
                            meme meme=itemsnapshot.getValue(meme.class);
                            memes.add(meme);

                        }
                        mIFirebaseLoadDone.onFirebaseLoadSuccess(memes);






                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        mIFirebaseLoadDone.onFirebaeLoadFailed(databaseError.getMessage());

                    }
                });

            }
        });

    }


    @Override
    public void onFirebaseLoadSuccess(final List<meme> memeList) {

        storiesProgressView.setStoriesCount(memeList.size());
        storiesProgressView.setStoryDuration(3000L);



        Picasso.get().load(memeList.get(0).getImageLink()).into(storyimage,new Callback() {
            @Override
            public void onSuccess() {
                storiesProgressView.startStories();



            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(MemeStories.this, "error", Toast.LENGTH_SHORT).show();

            }
        });

        storiesProgressView.setStoriesListener(new StoriesProgressView.StoriesListener() {
            @Override
            public void onNext() {
                if(counter < (memeList.size()))
                {
                    counter++;
                    Picasso.get().load(memeList.get(counter).getImageLink()).into(storyimage);
                }
            }

            @Override
            public void onPrev() {

                if(counter > 0)
                {
                    counter--;
                    Picasso.get().load(memeList.get(counter).getImageLink()).into(storyimage);
                }

            }

            @Override
            public void onComplete() {
                counter=0;
                Toast.makeText(MemeStories.this, "load done", Toast.LENGTH_LONG).show();

            }
        });




    }

    @Override
    public void onFirebaeLoadFailed(String message) {
        Toast.makeText(MemeStories.this, message, Toast.LENGTH_LONG).show();

    }



}
