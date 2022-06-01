package com.ubunifu.appclone.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.ubunifu.appclone.CommentsActivity;
import com.ubunifu.appclone.R;
import com.ubunifu.appclone.models.Posts;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    Context mContext;
    ArrayList<Posts> mPostsArrayList;
    FirebaseAuth mAuth;
    String currentUserId;


    public PostsAdapter(Context mContext, ArrayList<Posts> mPostsArrayList) {
        this.mContext = mContext;
        this.mPostsArrayList = mPostsArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.posts_item_list, parent, false);

        return new PostsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //get values from firestore db for every position
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        Posts posts = mPostsArrayList.get(position);
        holder.caption.setText(posts.getCaption());
        holder.name.setText(posts.getFull_names());
        Glide.with(mContext).load(mPostsArrayList.get(position).getPost_img_url()).into(holder.postImage);
        Glide.with(mContext).load(mPostsArrayList.get(position).getProfile_img_url()).into(holder.profImg);

        holder.imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext , v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()){
                            case R.id.edit :
                                editPost(posts.getPost_id());
                                return true;

                            case R.id.delete :

                                return true;

                            case R.id.report :
                                Toast.makeText(mContext, "Report Sent!", Toast.LENGTH_SHORT).show();
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                if (!posts.getPublisher().equals(currentUserId)){
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                popupMenu.show();
            }
        });



        // to comment activity

        holder.commentImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postId", posts.getPost_id());
                intent.putExtra("publisherId", posts.getPublisher());
//                Toast.makeText(mContext, "PostId = " + posts.getPost_id() + "Publisher is " + posts.getPublisher(), Toast.LENGTH_LONG).show();
                mContext.startActivity(intent);

            }
        });

    }

    private void editPost(String post_id) {
        AlertDialog.Builder alertdialog  = new AlertDialog.Builder(mContext);
        alertdialog.setTitle("Edit post");
        final EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        
        editText.setLayoutParams(layoutParams); 
        alertdialog.setView(editText);
        
        getText(post_id, editText);
        alertdialog.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                HashMap <String, Object> hashMap = new HashMap<>();
                hashMap.put("caption",  editText.getText().toString());

                FirebaseFirestore.getInstance().collection("posts")
                        .document(post_id)
                        .update(hashMap);

                notifyDataSetChanged();
            }
        });

        alertdialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });
        alertdialog.show();
        
    }

    private void getText(String post_id, final EditText editText) {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("posts").document(post_id);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        String caption = documentSnapshot.getString("caption");
                        editText.setText(caption);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPostsArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //Declare your views

        TextView caption, name;
        ImageView postImage, commentImg, saveImg, likeImg, imgMore;
        CircleImageView profImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //initialize views

            caption = itemView.findViewById(R.id.description);
            postImage = itemView.findViewById(R.id.post_image);
            commentImg = itemView.findViewById(R.id.comment);
            saveImg = itemView.findViewById(R.id.save);
            likeImg = itemView.findViewById(R.id.like);
            name = itemView.findViewById(R.id.username);
            profImg = itemView.findViewById(R.id.image_profile);
            imgMore = itemView.findViewById(R.id.more);



        }
    }
}
