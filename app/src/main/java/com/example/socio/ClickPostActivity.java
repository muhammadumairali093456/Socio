package com.example.socio;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView PostImage;
    private TextView PostDescription;
    private Button EditPostButton,DeletePostButton;
    private String PostKey,currentUserID,databaseUserID, description,image;
    private DatabaseReference ClickPostRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        PostKey = getIntent().getExtras().get("PostKey").toString();
        ClickPostRef = FirebaseDatabase.getInstance("https://socio-447bc-default-rtdb.firebaseio.com/").getReference().child("Posts").child(PostKey);

        mAuth =FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostImage = (ImageView)findViewById(R.id.click_post_image);
        PostDescription = (TextView)findViewById(R.id.click_post_description);
        EditPostButton = (Button)findViewById(R.id.edit_post_button);
        DeletePostButton = (Button)findViewById(R.id.delete_post_button);

        DeletePostButton.setVisibility(View.INVISIBLE);
        EditPostButton.setVisibility(View.INVISIBLE);



        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();
                    databaseUserID = dataSnapshot.child("uid").getValue().toString();

                    PostDescription.setText(description);
                    Picasso.with(ClickPostActivity.this).load(image).into(PostImage);
                    if(currentUserID.equals(databaseUserID)){
                        DeletePostButton.setVisibility(View.VISIBLE);
                        EditPostButton.setVisibility(View.VISIBLE);
                    }

                    EditPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditCurrentPost(description);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteCurrentPost();
            }
        });

    }

    private void EditCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");
        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this,"Post Updated Successfully",Toast.LENGTH_SHORT).show();

            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }

    private void DeleteCurrentPost() {
        ClickPostRef.removeValue();
        SendUserToHomeActivity();
        Toast.makeText(this,"Post has been Deleted",Toast.LENGTH_SHORT).show();
    }
    private void SendUserToHomeActivity()
    {
        Intent mainIntent = new Intent(ClickPostActivity.this, Home.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}