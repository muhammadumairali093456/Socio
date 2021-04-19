package com.example.socio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private TextView userName,userProfName,userCountry,userGender,userDob,userRelation,userStatus;
    private CircleImageView userProfileImage;
    private DatabaseReference profileUserRef , FriendsRef , PostsRef;
    private FirebaseAuth mAuth;

    private Button myPostsButton , myFriendsButton;

    private String currentUserId;
    private int countFriends = 0 , countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Users").child(currentUserId);
        FriendsRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Friends");
        PostsRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Posts");

        userName = (TextView) findViewById(R.id.my_username);
        userProfName = (TextView) findViewById(R.id.my_profile_full_name);
        userProfileImage = (CircleImageView) findViewById(R.id.my_profile_pic);
        userStatus = (TextView) findViewById(R.id.my_profile_status);
        userCountry = (TextView) findViewById(R.id.my_country);
        userGender = (TextView) findViewById(R.id.my_Gender);
        userDob = (TextView) findViewById(R.id.my_dob);
        userRelation = (TextView) findViewById(R.id.my_relationship_status);

        myPostsButton = (Button) findViewById(R.id.my_post_button);
        myFriendsButton = (Button) findViewById(R.id.my_friends_button);

        myFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToFriendsActivity();
            }
        });


        myPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMyPostsActivity();
            }
        });

        PostsRef.orderByChild("uid").startAt(currentUserId).endAt(currentUserId + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            countPosts = (int) dataSnapshot.getChildrenCount();
                            myPostsButton.setText(Integer.toString(countPosts) + " Posts");
                        } else {
                            myPostsButton.setText(" 0 Posts");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        FriendsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    countFriends = (int) dataSnapshot.getChildrenCount();
                    myFriendsButton.setText(Integer.toString(countFriends) + " Friends");
                } else {
                    myFriendsButton.setText("0 Friends");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myDoB = dataSnapshot.child("dob").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.with(ProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText(myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userCountry.setText("Country: " + myCountry);
                    userGender.setText("gender: " + myGender);
                    userDob.setText("DoB: " + myDoB);
                    userRelation.setText("Relationship: " + myRelationStatus);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToFriendsActivity()
    {
        Intent friendsIntent = new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void SendUserToMyPostsActivity()
    {
        Intent friendsIntent = new Intent(ProfileActivity.this, MyPostsActivity.class);
        startActivity(friendsIntent);
    }

}