package com.dmatsanganis.clickawayapp.RecyclerViewAdapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dmatsanganis.clickawayapp.Classes.Store;
import com.dmatsanganis.clickawayapp.MapsActivity;
import com.dmatsanganis.clickawayapp.R;

import java.util.ArrayList;

public class StoreRecyclerViewAdapter  extends RecyclerView.Adapter<StoreRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Store> stores;
    public StoreRecyclerViewAdapter(ArrayList<Store> stores){
        this.stores = stores;
    }

    //Create view holder for the recycler view
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for this view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_item, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //Method that binds the elements of the view holder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(stores.get(position));
    }
    //Method that returns the number of the items which the recycler view contains
    @Override
    public int getItemCount() {
        return stores.size();
    }

    //Set the views for the view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, phone, address;
        Button call, map;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.storeTitle);
            phone = itemView.findViewById(R.id.storeAddress);
            address = itemView.findViewById(R.id.storePhone);
            call = itemView.findViewById(R.id.callStore);
            map = itemView.findViewById(R.id.mapStore);
        }

        public void bind(Store store){
            phone.setText(store.getPhone());
            if(itemView.getResources().getConfiguration().getLocales().get(0).toString().equals("el")){
                address.setText(store.getAddress_gr());
                title.setText(String.valueOf(store.getTitle_gr()));
            } else {
                address.setText(store.getAddress_en());
                title.setText(String.valueOf(store.getTitle_en()));
            }
            //Add click listener on each card view in order to call the store
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:"+store.getPhone()));

                    v.getContext().startActivity(intent);

                }
            });
            map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(),MapsActivity.class);
                    intent.putExtra("Store",store);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}
