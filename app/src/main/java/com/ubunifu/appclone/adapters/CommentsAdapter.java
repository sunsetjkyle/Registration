package com.ubunifu.appclone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ubunifu.appclone.R;
import com.ubunifu.appclone.models.Comment;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    Context mContext;
    List<Comment> mCommentArrayList;
    String postid;

    public CommentsAdapter(Context mContext, List<Comment> mCommentArrayList, String postid) {
        this.mContext = mContext;
        this.mCommentArrayList = mCommentArrayList;
        this.postid = postid;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);

        return new CommentsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Comment comment = mCommentArrayList.get(position);
        holder.comment.setText(comment.getComment());
        holder.name.setText(comment
        .getNames());

        Glide.with(mContext).load(mCommentArrayList.get(position).getProfile_img_url()).into(holder.profImg);


    }

    @Override
    public int getItemCount() {
        return mCommentArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //declare views

        CircleImageView profImg;
        TextView name, comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //initialization

            profImg = itemView.findViewById(R.id.image_profile);
            name = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);

        }
    }
}
