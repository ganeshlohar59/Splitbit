package app.splitbit.GroupSplits.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import app.splitbit.GroupSplits.AllTransactions;
import app.splitbit.GroupSplits.Model.Transaction;
import app.splitbit.GroupSplits.View.TransactionsAdapter;
import app.splitbit.R;

public class Transactions extends Fragment {

    //-- Strings
    private String EVENT_ROOM_ID;
    private String ADMIN;


    //-- UI
    private RecyclerView recyclerView_transactions;
    private ArrayList<Transaction> arraylist_transactions;
    private TransactionsAdapter transactionsAdapter;
    private LinearLayout noTransactions;

    //-- Firebase
    private DatabaseReference db;
    private DatabaseReference transactionsRef;
    private FirebaseAuth auth;
    private ChildEventListener childEventListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        //-- Strings
        EVENT_ROOM_ID = getArguments().getString("key");
        ADMIN = getArguments().getString("admin");

        //--  Firebase
        db = FirebaseDatabase.getInstance().getReference().child("splitbit");
        transactionsRef = db.child("transactions").child(EVENT_ROOM_ID);
        auth = FirebaseAuth.getInstance();

        //-- UI
        noTransactions = (LinearLayout) view.findViewById(R.id.noTransactionsLayout);

        recyclerView_transactions = (RecyclerView) view.findViewById(R.id.recyclerview_transactions);
        recyclerView_transactions.setNestedScrollingEnabled(false);
        arraylist_transactions = new ArrayList<>();
        transactionsAdapter = new TransactionsAdapter(arraylist_transactions, Transactions.this.getContext(),EVENT_ROOM_ID);

        LinearLayoutManager layoutManager = new LinearLayoutManager(Transactions.this.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView_transactions.setLayoutManager(layoutManager);
        recyclerView_transactions.setAdapter(transactionsAdapter);

        //--
        synchTransactionList();


        return view;
    }


    private void synchTransactionList(){
        childEventListener = transactionsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //--
                noTransactions.setVisibility(View.GONE);
                Transaction transaction = dataSnapshot.getValue(Transaction.class);
                transaction.setKey(dataSnapshot.getKey());

                if(!arraylist_transactions.contains(transaction)){
                    arraylist_transactions.add(transaction);
                    transactionsAdapter.notifyItemInserted(arraylist_transactions.indexOf(transaction));
                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("","    Child Changed  :  "+dataSnapshot.getValue().toString());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Transaction transaction = dataSnapshot.getValue(Transaction.class);
                transaction.setKey(dataSnapshot.getKey());
                arraylist_transactions.remove(transaction);
                transactionsAdapter.notifyDataSetChanged();
                if(arraylist_transactions.size() == 0){
                    noTransactions.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("","    Child Moved  :  "+dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        transactionsRef.removeEventListener(childEventListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        transactionsRef.addChildEventListener(childEventListener);
    }
}
