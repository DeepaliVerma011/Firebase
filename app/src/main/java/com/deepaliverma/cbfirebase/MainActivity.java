package com.deepaliverma.cbfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1000;
    private static final String TAG = "TAG";
    Button button;
    EditText editText;
    ArrayList<String> notes;
    ListView listView;
    ArrayAdapter arrayAdapter;
DatabaseReference dRef;
FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        editText = findViewById(R.id.editText);
        listView = findViewById(R.id.listView);
        notes= new ArrayList<>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                R.layout.item_row,
                R.id.tViewList,
                notes);
       // listView.setAdapter(arrayAdapter);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            //loggedIn


            DatabaseReference dRef = FirebaseDatabase.getInstance().getReference();

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String note = editText.getText().toString();
                    //upload note to database

                    //setting the value to root node
                    //FirebaseDatabase.getInstance().getReference().setValue(note);

                    //creating a new node Everytime
                    //FirebaseDatabase.getInstance().getReference().push().setValue(note);

                    //Note n = new Note("Hello", "World");
                    dRef.child("note").child(firebaseUser.getUid()).push().setValue(note);
                    //      dRef.child("todo").push().setValue(note);
                }
            });


           addListeners();
        }
        else{
            //loggedOut
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false) //disable goggle smart lock
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                                    new AuthUI.IdpConfig.MicrosoftBuilder().build(),
                                    new AuthUI.IdpConfig.YahooBuilder().build(),
                                    new AuthUI.IdpConfig.AppleBuilder().build(),
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                                    new AuthUI.IdpConfig.AnonymousBuilder().build()))
                            .build(),
                    RC_SIGN_IN);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                firebaseUser =FirebaseAuth.getInstance().getCurrentUser();
                //startActivity(SignedInActivity.createIntent(this, response));
              addListeners();
              Log.e("TAG", "onActivityResult:"+firebaseUser.getDisplayName());
              Log.e("TAG","onActivityResult:"+firebaseUser.getUid());
                finish();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    //showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    //showSnackbar(R.string.no_internet_connection);
                    return;
                }

               // showSnackbar(R.string.unknown_error);
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }

    public void addListeners(){
        dRef.child("note").child(firebaseUser.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
//called when a new data is inserted to the "note" node
                //forWriting
                String data = snapshot.getValue(String.class);
                notes.add(data);
                arrayAdapter.notifyDataSetChanged();
                //forReading
                // Note data=  snapshot.getValue(Note.class);
            }

            @Override
            public void onChildChanged(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
//an existing data is updated
            }

            @Override
            public void onChildRemoved(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
//when a data is removed
            }

            @Override
            public void onChildMoved(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
//when position of subnode changed
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {
//when operation failed
            }
        });

        //Give the entire database updated after any change in Database-->addValueEventListener[more time]

        dRef.child("note").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                //gets the entire database everytime
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

}