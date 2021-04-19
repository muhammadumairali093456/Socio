package com.example.socio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawer;

    private RecyclerView postList;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef , LikesRef;

    String currentUserID;
    Boolean LikeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Likes");


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Activity");
        setSupportActionBar(toolbar);


        drawer = findViewById(R.id.drawer_layout);

        actionBarDrawerToggle = new ActionBarDrawerToggle(Home.this, drawer, R.string.Drawer_open, R.string.Drawer_close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View navView = navigationView.getHeaderView(0);

        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);

        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("fullname"))
                    {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(Home.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }
                    else
                    {
                        Toast.makeText(Home.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                UserMenuSelector(item);
                return false;
            }
        });

        DisplayAllUsersPosts();

    }


    public void updateUserStatus(String state)
    {
        String saveCurrentDate , saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        UsersRef.child(currentUserID).child("userState")
                .updateChildren(currentStateMap);
    }


    private void DisplayAllUsersPosts()
    {

        Query SortPostsInDecendingOrder = PostsRef.orderByChild("counter");

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_posts_layout,
                                PostsViewHolder.class,
                                SortPostsInDecendingOrder
                        )
                {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position)
                    {

                        final String PostKey = getRef(position).getKey();

                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());
                        viewHolder.setPostimage(getApplicationContext(), model.getPostimage());

                        viewHolder.setLikeButtonStatus(PostKey);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickpostIntent = new Intent(Home.this,ClickPostActivity.class);
                                clickpostIntent.putExtra("PostKey",PostKey);
                                startActivity(clickpostIntent);

                            }
                        });

                        viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent commentsIntent = new Intent(Home.this,CommentsActivity.class);
                                commentsIntent.putExtra("PostKey",PostKey);
                                startActivity(commentsIntent);

                            }
                        });

                        viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
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
        postList.setAdapter(firebaseRecyclerAdapter);

        updateUserStatus("online");
    }



    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        ImageButton LikePostButton , CommentPostButton;
        TextView DisplayNoofLikes;
        int countLikes;
        String currentUserID;
        DatabaseReference LikesRef;

        public PostsViewHolder(View itemView)
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



    private void SendUserToPostActivity()
    {
        Intent addNewPostIntent = new Intent(Home.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }



    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            SendUserToLoginActivity();
        }
        else
        {
            CheckUserExistence();
        }
    }



    private void CheckUserExistence()
    {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.hasChild(current_user_id))
                {
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void SendUserToSetupActivity()
    {
        Intent setupIntent = new Intent(Home.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }



    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(Home.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void UserMenuSelector(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_post:
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                SendUserToProfileActivity();
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_home:
                SendUserToHomeActivity();
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friends:
                SendUserToFriendsActivity();
                Toast.makeText(this, "Friend List", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_messages:
                SendUserToFriendsActivity();
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                SendUserToSettingsActivity();
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_logout:
                updateUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }

    private void SendUserToHomeActivity()
    {
        Intent settingsIntent = new Intent(Home.this, Home.class);
        startActivity(settingsIntent);
    }

    private void SendUserToSettingsActivity()
    {
        Intent settingsIntent = new Intent(Home.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToFindFriendsActivity()
    {
        Intent settingsIntent = new Intent(Home.this, FindFriendsActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToProfileActivity()
    {
        Intent profileIntent = new Intent(Home.this, ProfileActivity.class);
        startActivity(profileIntent);
    }
    private void SendUserToFriendsActivity()
    {
        Intent friendsIntent = new Intent(Home.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}