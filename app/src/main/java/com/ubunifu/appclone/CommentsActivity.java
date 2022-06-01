package com.ubunifu.appclone;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ubunifu.appclone.adapters.CommentsAdapter;
import com.ubunifu.appclone.models.Comment;
import com.ubunifu.appclone.models.Posts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {



    Toolbar mToolbar;
    RecyclerView commentsRecyclerView;
    CommentsAdapter mCommentsAdapter;
    List<Comment> mCommentList;


    CircleImageView profImg;
    EditText etComment;
    TextView tvPost;

    FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    String currentUserId, postId, postPublisherId, img_url, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mToolbar = findViewById(R.id.comments_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        postPublisherId = intent.getStringExtra("publisherId");


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        mFirestore = FirebaseFirestore.getInstance();

        commentsRecyclerView = findViewById(R.id.comments_recycler_view);
        commentsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        commentsRecyclerView.setLayoutManager(linearLayoutManager);
        mCommentList = new ArrayList<>();
        mCommentsAdapter = new CommentsAdapter(this, mCommentList, postId);
        commentsRecyclerView.setAdapter(mCommentsAdapter);


        profImg = findViewById(R.id.image_profile);
        etComment = findViewById(R.id.add_comment);
        tvPost = findViewById(R.id.post);

        tvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comment();
            }
        });


        readComments();
        getProfileData();

    }

    private void getProfileData() {

        DocumentReference documentReference = mFirestore.collection("Users").document(currentUserId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

                if(snapshot.exists()) {

                    Glide.with(getApplicationContext()).load(snapshot.getString("profile_img_url")).placeholder(R.drawable.profile).into(profImg);
                    img_url = snapshot.getString("profile_img_url");
                    name = snapshot.getString("full_names");

                }
            }
        });

    }

    private void readComments() {

        mFirestore.collection("comments")
                .whereEqualTo("postId", postId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if (error != null) {
                            Log.e("Error!", error.getMessage());
                            return;
                        }

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                mCommentList.add(dc.getDocument().toObject(Comment.class));
                            }

                            mCommentsAdapter.notifyDataSetChanged();
                        }
                    }
                });

    }

    private void comment() {

        String commentTxt = etComment.getText().toString();
        String id = mFirestore.collection("comments").document().getId();

        if (TextUtils.isEmpty(commentTxt)) {
            etComment.setError("Write your Comment!");
        } else {

            HashMap commentsMap = new HashMap();
            commentsMap.put("comment", commentTxt);
            commentsMap.put("comment_id", id);
            commentsMap.put("publisher", currentUserId);
            commentsMap.put("postId", postId);
            commentsMap.put("profile_img_url", img_url);
            commentsMap.put("names", name);

            DocumentReference documentReference = mFirestore.collection("comments").document(id);
            documentReference.set(commentsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {
                        Toast.makeText(CommentsActivity.this, "Commented Successfully", Toast.LENGTH_SHORT).show();
                        etComment.setText("");

                    } else {
                        Toast.makeText(CommentsActivity.this, "Error Occurred!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}
