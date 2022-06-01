package com.ubunifu.appclone.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ubunifu.appclone.R;
import com.ubunifu.appclone.adapters.PostsAdapter;
import com.ubunifu.appclone.models.Posts;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

    private RecyclerView postsRecycler;
    private FirebaseFirestore mFirestore;
    ArrayList<Posts> mPostsArrayList;
    PostsAdapter mPostsAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle("Posts");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);



        mFirestore = FirebaseFirestore.getInstance();
        postsRecycler = view.findViewById(R.id.rv_posts);
        postsRecycler.setHasFixedSize(true);
        postsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        mPostsArrayList = new ArrayList<Posts>();
        mPostsAdapter = new PostsAdapter(getContext(), mPostsArrayList);
        postsRecycler.setAdapter(mPostsAdapter);

        eventChangeListener();

        return view;
    }

    private void eventChangeListener() {

        mFirestore.collection("posts")
                .orderBy("current_time", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if (error != null) {
                            Log.e("Error!", error.getMessage());
                            return;
                        }

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                mPostsArrayList.add(dc.getDocument().toObject(Posts.class));
                            }

                            mPostsAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}