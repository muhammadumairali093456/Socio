package com.example.socio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {
    private RecyclerView myFriendList;
    private DatabaseReference FriendsRef , UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Friends").child(online_user_id);
        UsersRef= FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Users");

        myFriendList = (RecyclerView) findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();

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

        UsersRef.child(online_user_id).child("userState")
                .updateChildren(currentStateMap);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");
    }

    private void DisplayAllFriends() {
        FirebaseRecyclerAdapter<Friends , FriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.all_users_display_layout,
                FriendsViewHolder.class,
                FriendsRef
        )
        {
            @Override
            protected void populateViewHolder(FriendsViewHolder friendsViewHolder, Friends friends, int i)
            {
                friendsViewHolder.setDate(friends.getDate());

                final String userIDs = getRef(i).getKey();
                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            final String userName = dataSnapshot.child("fullname").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileimage").getValue().toString();

                            final String type;

                            if(dataSnapshot.hasChild("userState"))
                            {
                                type = dataSnapshot.child("userState").child("type").getValue().toString();
                                if(type.equals("online"))
                                {
                                    friendsViewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    friendsViewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                                }
                            }

                            friendsViewHolder.setFullname(userName);
                            friendsViewHolder.setProfileimage(getApplicationContext(),profileImage);

                            friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v)
                                {
                                    CharSequence options[] = new CharSequence[]
                                            {
                                                    userName + "'s profile",
                                                    "Send Message"
                                            };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Options");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            if(which == 0)
                                            {
                                                Intent profileintent = new Intent(FriendsActivity.this,PersonProfileActivity.class);
                                                profileintent.putExtra("visit_user_id",userIDs);
                                                startActivity(profileintent);
                                            }
                                            if(which == 1)
                                            {
                                                Intent chatintent = new Intent(FriendsActivity.this,ChatActivity.class);
                                                chatintent.putExtra("visit_user_id",userIDs);
                                                chatintent.putExtra("userName" , userName);
                                                startActivity(chatintent);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        myFriendList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        ImageView onlineStatusView;

        public FriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mView = itemView;
            onlineStatusView = (ImageView) itemView.findViewById(R.id.all_user_online_icon);
        }
        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView myimage = (CircleImageView)mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myimage);
        }
        public void setFullname(String fullname){
            TextView myName = (TextView)mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }
        public void setDate(String date){
            TextView FriendsDate = (TextView)mView.findViewById(R.id.all_users_status);
            FriendsDate.setText("Friends Since: " +date);
        }
    }
}