package com.example.socio;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef;

    public MessagesAdapter(List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder
    {
        public TextView SenderMessagesText, ReceiverMessagesText;
        public CircleImageView receiverProfileImage;

        public MessagesViewHolder(@NonNull View itemView)
        {
            super(itemView);
            SenderMessagesText = (TextView) itemView.findViewById(R.id.sender_message_text);
            ReceiverMessagesText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
        }
    }


    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout_of_users , parent , false);
        mAuth = FirebaseAuth.getInstance();
        return new MessagesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position)
    {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);


        String fromUserID = messages.getFrom();
        String fromMessagetype = messages.getType();


        usersDatabaseRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Users").child(fromUserID);
        usersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String image = dataSnapshot.child("profileimage").getValue().toString();

                    Picasso.with(holder.receiverProfileImage.getContext()).load(image).placeholder(R.drawable.profile).into(holder.receiverProfileImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (fromMessagetype.equals("Text"))
        {
            holder.ReceiverMessagesText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            if(fromUserID.equals(messageSenderID))
            {
                holder.SenderMessagesText.setBackgroundResource(R.drawable.sender_message_text_background);
                holder.SenderMessagesText.setTextColor(Color.WHITE);
                holder.SenderMessagesText.setGravity(Gravity.LEFT);
                holder.SenderMessagesText.setText(messages.getMessage());

            }
            else
            {
                holder.SenderMessagesText.setVisibility(View.INVISIBLE);

                holder.ReceiverMessagesText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.ReceiverMessagesText.setBackgroundResource(R.drawable.receiver_message_text_background);
                holder.ReceiverMessagesText.setTextColor(Color.WHITE);
                holder.ReceiverMessagesText.setGravity(Gravity.LEFT);
                holder.ReceiverMessagesText.setText(messages.getMessage());


            }
        }
    }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }
}

