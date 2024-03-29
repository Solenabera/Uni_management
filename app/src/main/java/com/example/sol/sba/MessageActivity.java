package com.example.sol.sba;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {
    private Toolbar messageToolbar;

    private EditText comment_field;
    private ImageView comment_post_btn;

    private RecyclerView comment_list;
    private MessageRecyclerAdapter messageRecyclerAdapter;
    private List<AdminMessage> messagesList;
    private List<User> user_list;

    private String blog_post_id;
    private String current_user_id;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);


        messageToolbar = findViewById(R.id.message_toolbar);
        setSupportActionBar(messageToolbar);
        getSupportActionBar().setTitle("Messages");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        blog_post_id = getIntent().getStringExtra("blog_post_id");

        comment_field = findViewById(R.id.comment_field);
        comment_post_btn = findViewById(R.id.comment_post_btn);
        comment_list = findViewById(R.id.comment_list);

        messagesList = new ArrayList<>();
        user_list = new ArrayList<>();
        messageRecyclerAdapter = new MessageRecyclerAdapter(messagesList,user_list);
        comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(this));
        comment_list.setAdapter(messageRecyclerAdapter);

        firebaseFirestore.collection("Messages/" + current_user_id + "/Com")
                .addSnapshotListener(MessageActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (!documentSnapshots.isEmpty()) {

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

//                                            String commentId = doc.getDocument().getId();
//                                            Comments comments = doc.getDocument().toObject(Comments.class);
//                                            commentsList.add(comments);
//                                            commentsRecyclerAdapter.notifyDataSetChanged();


                                    final AdminMessage messages = doc.getDocument().toObject(AdminMessage.class);

                                    String user_id = doc.getDocument().getString("user_id");
                                    firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if(task.isSuccessful()) {

                                                User user = task.getResult().toObject(User.class);
                                                user_list.add(user);
                                                messagesList.add(messages);
                                                messageRecyclerAdapter.notifyDataSetChanged();

                                            }
                                        }
                                    });

//                                            String commentId = doc.getDocument().getId();
//                                            Comments comments = doc.getDocument().toObject(Comments.class);
//                                            commentsList.add(comments);
//                                            commentsRecyclerAdapter.notifyDataSetChanged();

                                }
                            }
                        }


                    }
                });

        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = comment_field.getText().toString();

                if(!message.isEmpty()){

                    Map<String ,Object> commentsMap = new HashMap<>();
                    commentsMap.put("message",message);
                    commentsMap.put("user_id",current_user_id);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Messages/" + current_user_id + "/Com" ).add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            if(!task.isSuccessful()){

                                Toast.makeText(MessageActivity.this,"Error Posting Comment : "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            }else{

                                comment_field.setText("");

                                // by me
//                                Intent intent = new Intent(MessageActivity.this,MessageActivity.class);
//                                startActivity(intent);
//                                finish();

                            }

                        }
                    });

                }
            }
        });
    }
}

