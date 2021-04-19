package com.example.socio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostsList;
    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef ,UsersRef ,LikesRef;
    private String currentUserID;
    Boolean LikeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        PostsRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Posts");
        UsersRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Users");
        LikesRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Likes");

        mToolbar = (Toolbar) findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        myPostsList = (RecyclerView) findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);

        DisplayMyAllPosts();

    }

    private void DisplayMyAllPosts()
    {
        Query myPostsQuery = PostsRef.orderByChild("uid").startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerAdapter<Posts, MyPostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(
                Posts.class,
                R.layout.all_posts_layout,
                MyPostsViewHolder.class,
                myPostsQuery
        )
        {
            @Override
            protected void populateViewHolder(MyPostsViewHolder myPostsViewHolder, Posts posts, int i)
            {
                final String PostKey = getRef(i).getKey();

                myPostsViewHolder.setFullname(posts.getFullname());
                myPostsViewHolder.setTime(posts.getTime());
                myPostsViewHolder.setDate(posts.getDate());
                myPostsViewHolder.setDescription(posts.getDescription());
                myPostsViewHolder.setProfileimage(getApplicationContext(), posts.getProfileimage());
                myPostsViewHolder.setPostimage(getApplicationContext(), posts.getPostimage());

                myPostsViewHolder.setLikeButtonStatus(PostKey);

                myPostsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickpostIntent = new Intent(MyPostsActivity.this,ClickPostActivity.class);
                        clickpostIntent.putExtra("PostKey",PostKey);
                        startActivity(clickpostIntent);

                    }
                });

                myPostsViewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(MyPostsActivity.this,CommentsActivity.class);
                        commentsIntent.putExtra("PostKey",PostKey);
                        startActivity(commentsIntent);

                    }
                });

                myPostsViewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker = true;
                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(LikeChecker.equals(true))
                                {
                                    if(dataSnapshot.child(PostKey).hasChild(currentUserID))
                                    {
                                        LikesRef.child(PostKey).child(currentUserID).removeValue();
                                        LikeChecker = false;
                                    }
                                    else {

                                        LikesRef.child(PostKey).child(currentUserID).setValue(true);
                                        LikeChecker = false;
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }
        };
        myPostsList.setAdapter(firebaseRecyclerAdapter);
    }
    public static class MyPostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        ImageButton LikePostButton , CommentPostButton;
        TextView DisplayNoofLikes;
        int countLikes;
        String currentUserID;
        DatabaseReference LikesRef;

        public MyPostsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mView = itemView;

            LikePostButton = (ImageButton)mView.findViewById(R.id.like_button);
            CommentPostButton =(ImageButton)mView.findViewById(R.id.comment_button);
            DisplayNoofLikes =(TextView)mView.findViewById(R.id.display_no_of_likes);

            LikesRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }

        public void  setLikeButtonStatus(final String PostKey)
        {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(PostKey).hasChild(currentUserID))
                    {
                        countLikes  = (int)dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.like);
                        DisplayNoofLikes.setText((Integer.toString(countLikes)+(" Likes")));
                    }
                    else
                    {
                        countLikes  = (int)dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.dislike);
                        DisplayNoofLikes.setText(Integer.toString(countLikes)+(" Likes"));
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }


        public void setFullname(String fullname)
        {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }

        public void setTime(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("    " + time);
        }

        public void setDate(String date)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("    " + date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx1,  String postimage)
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx1).load(postimage).into(PostImage);
        }
    }
}