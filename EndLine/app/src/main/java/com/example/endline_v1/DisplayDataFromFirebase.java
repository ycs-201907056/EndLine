package com.example.endline_v1;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class DisplayDataFromFirebase {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private CollectionReference collectionReference;
    private Query query;
    private Context context;
    private ArrayList<ItemDataSet> list;
    private RecyclerView recyclerView;
    private ItemRecyclerAdapter adapter;
    private String category, product_name, orderby;

    public DisplayDataFromFirebase(String category,RecyclerView recyclerView, Context context) {
        this.recyclerView = recyclerView;
        this.context = context;
        this.category = category;
    }

    public DisplayDataFromFirebase(String category, String orderby, RecyclerView recyclerView, Context context) {
        this.recyclerView = recyclerView;
        this.orderby = orderby;
        this.context = context;
        this.category = category;
    }

    public DisplayDataFromFirebase(String category, RecyclerView recyclerView, Context context, String product_name){
        this.recyclerView = recyclerView;
        this.context = context;
        this.category = category;
        this.product_name = product_name;
    }

    public void DisplayData(){
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        collectionReference = firestore.collection("mainData");

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        list = new ArrayList<>();
        adapter = new ItemRecyclerAdapter(list);
        recyclerView.setAdapter(adapter);
        getData();
    }

    private void getData() {
        Log.d("UID", user.getUid());
        if(category == "All"){
            query = collectionReference.whereEqualTo("UID", user.getUid()).orderBy(orderby, Query.Direction.DESCENDING);
        }else if(category == "Search"){
            query = collectionReference.whereEqualTo("UID", user.getUid()).whereEqualTo("product_name", product_name);
        }
        else{
            query = collectionReference.whereEqualTo("category", category).whereEqualTo("UID", user.getUid());
        }
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()){
                        if(document.get("product_name") != null){
                            Log.d("getData", document.getData().toString());
                            ItemDataSet itemDataSet = new ItemDataSet(
                                    document.get("product_name").toString(),
                                    document.get("category").toString(),
                                    document.get("buy_date").toString(),
                                    document.get("end_line").toString(),
                                    document.get("img").toString()
                            );
                            list.add(itemDataSet);
                            adapter.notifyDataSetChanged();
                        }else{
                            Log.w("getData", "No Data in uid");
                            Toast.makeText(context, "아직 데이터가 없습니다!", Toast.LENGTH_LONG).show();
                        }

                    }
                }else{
                    Log.w("getData", "fail");
                    Toast.makeText(context, "데이터 로딩 실패\n다시 시도해 보세요", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
