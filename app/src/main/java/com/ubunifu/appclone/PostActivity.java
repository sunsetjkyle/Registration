package com.ubunifu.appclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

public class PostActivity extends AppCompatActivity {

    Uri filePath;
    String downloadUrl, postCurrentDate, postCurrentTime, currentUserId, img_url, name;
    StorageTask uploadTask;
    StorageReference storageReference;
    FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    ImageView close;
    ImageView image_added;
    TextView post;
    EditText description;
    private static final int gallery_pick = 1;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Intent commentintent = new Intent();
        commentintent.getStringExtra("comment");
        commentintent.getStringExtra("comment_id");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        mFirestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);



        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);


        //storage location

    storageReference = FirebaseStorage.getInstance().getReference("posts_images");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this , MainActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        image_added.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_PICK);
                startActivityForResult(galleryIntent, gallery_pick);

            }

        });

        DocumentReference documentReference = mFirestore.collection("posts").document();
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

                if(snapshot.exists()) {

                    Glide.with(getApplicationContext()).load(snapshot.getString("img_url")).placeholder(R.drawable.empty).into(image_added);

                }
            }
        });
        getProfileData();

    }
    private void getProfileData() {

        DocumentReference documentReference = mFirestore.collection("Users").document(currentUserId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

                if(snapshot.exists()) {

                    img_url = snapshot.getString("profile_img_url");
                    name = snapshot.getString("full_names");

                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == gallery_pick && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                image_added.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }


        if(filePath != null)
        {
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("Post Images/"+ UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(PostActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    downloadUrl = uri.toString();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(PostActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });


        }
    }

    private void uploadImage() {


        String caption = description.getText().toString();
        String id = mFirestore.collection("posts").document().getId();


        //            get the current date

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat current_Date = new SimpleDateFormat("dd-MMMM-yyyy");
        postCurrentDate = current_Date.format(calendar.getTime());

//            get the current time


        Calendar time = Calendar.getInstance();
        SimpleDateFormat current_Time = new SimpleDateFormat("HH:mm");
        postCurrentTime = current_Time.format(time.getTime());

        if (TextUtils.isEmpty(caption)) {
            description.setError("Write your Post!");
        }

        if (filePath == null) {

            Toast.makeText(this, "Please select an Image!", Toast.LENGTH_SHORT).show();
        }

        else {

            progressDialog.setTitle("Posting...");
            progressDialog.setMessage("Wait as we upload your Post");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);

            HashMap postsMap = new HashMap();
            postsMap.put("caption", caption);
            postsMap.put("post_id", id);
            postsMap.put("post_img_url", downloadUrl);
            postsMap.put("current_date", postCurrentDate);
            postsMap.put("current_time", postCurrentTime);
            postsMap.put("publisher", currentUserId);
            postsMap.put("profile_img_url", img_url);
            postsMap.put("full_names", name);

            DocumentReference documentReference = mFirestore.collection("posts").document(id);
            documentReference.set(postsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {
                        Toast.makeText(PostActivity.this, "Posted Successfully", Toast.LENGTH_SHORT).show();
                        toMainActivity();
                    } else {
                        Toast.makeText(PostActivity.this, "Error Occurred!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }

    private void toMainActivity() {

        Intent intent = new Intent(PostActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }
}