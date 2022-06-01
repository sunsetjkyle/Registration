package com.ubunifu.appclone;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText etFullNames, etEmail, etPhone;
    private Button btnSave;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    String currentUserId;
    private CircleImageView profileImg;
    private Uri resultUri;
    private DocumentReference documentReference;


    private static final int gallery_pick = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        mFirestore = FirebaseFirestore.getInstance();
        documentReference= mFirestore.collection("Users").document(currentUserId);



        profileImg = findViewById(R.id.profile_image);
        etFullNames = findViewById(R.id.et_full_names);
        etEmail = findViewById(R.id.et_userEmailAddress);
        etPhone = findViewById(R.id.et_phoneNumber);
        btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInfo();
            }
        });


        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 33);

                    } else {

                        Intent galleryIntent = new Intent();
                        galleryIntent.setType("image/*");
                        galleryIntent.setAction(Intent.ACTION_PICK);
                        startActivityForResult(galleryIntent, gallery_pick);

                    }

                }
//
            }
        });

        DocumentReference documentReference = mFirestore.collection("Users").document(currentUserId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

                if(snapshot.exists()) {

                    Glide.with(getApplicationContext()).load(snapshot.getString("profile_img_url")).placeholder(R.drawable.profile).into(profileImg);

                }
            }
        });


    }



    private void saveUserInfo() {

        String user_name = etEmail.getText().toString();
        String full_name = etFullNames.getText().toString();
        String phone = etPhone.getText().toString();


        if (TextUtils.isEmpty(user_name)) {


            etEmail.setError("Should not be Empty!");

        }

        if (TextUtils.isEmpty(full_name)) {

            etFullNames.setError("Should not be Empty!");

        }
        if (TextUtils.isEmpty(phone)) {

            etPhone.setError("Should not be Empty!");

        } else {

            HashMap usersMap = new HashMap();
            usersMap.put("email", user_name);
            usersMap.put("full_names", full_name);
            usersMap.put("phone_number", phone);
            usersMap.put("user_id", currentUserId);


            documentReference.set(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {

                        Toast.makeText(SetupActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        sendToMainActivity();

                    } else {

                        Toast.makeText(SetupActivity.this, "Error occurred" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });


        }

        if (resultUri != null) {
            StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profile_images").child(currentUserId);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);

            byte [] data = baos.toByteArray();
            UploadTask uploadTask = filepath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (taskSnapshot.getMetadata() != null) {
                        if (taskSnapshot.getMetadata().getReference() != null) {

                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String imageUrl = uri.toString();
                                    Map newImage = new HashMap();
                                    newImage.put("profile_img_url", imageUrl);
                                    documentReference.update(newImage);
                                    finish();
                                    return;
                                }
                            });
                        }
                    }
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            profileImg.setImageURI(resultUri);
        }
    }

    private void sendToMainActivity() {

        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
