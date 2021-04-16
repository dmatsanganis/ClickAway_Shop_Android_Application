package com.dmatsanganis.clickawayapp.RecyclerViewAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dmatsanganis.clickawayapp.Classes.ItemInStorage;
import com.dmatsanganis.clickawayapp.R;

import java.util.ArrayList;

public class OrderStorageRecyclerViewAdapter extends RecyclerView.Adapter<OrderStorageRecyclerViewAdapter.ViewHolder> {
    private ArrayList<ItemInStorage> items;

    public OrderStorageRecyclerViewAdapter(ArrayList<ItemInStorage> items) {
        this.items = items;
    }

    //Create view holder for the recycler view
    @NonNull
    @Override
    public OrderStorageRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for this view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_storage_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //Method that binds the elements of the view holder
    @Override
    public void onBindViewHolder(@NonNull OrderStorageRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    //Method that returns the number of the items which the recycler view contains
    @Override
    public int getItemCount() {
        return items.size();
    }

    //Set the views for the view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.orderStorageName);
            productQuantity = itemView.findViewById(R.id.orderStorageQuantity);
        }

        public void bind(ItemInStorage item){
            if (item.getSmartphone() != null){
                productName.setText(item.getSmartphone().getName());
                if (item.getQuantity()==0){
                    productQuantity.setText(itemView.getResources().getString(R.string.notAvailable));
                } else {
                    productQuantity.setText(String.valueOf(item.getQuantity()) + " " + itemView.getResources().getString(R.string.available));
                }
            }
            if (item.getTablet() != null){
               productName.setText(item.getTablet().getName());
                if (item.getQuantity()==0){
                    productQuantity.setText(itemView.getResources().getString(R.string.notAvailable));
                } else {
                    productQuantity.setText(String.valueOf(item.getQuantity()) + " " + itemView.getResources().getString(R.string.available));
                }
            }
            if (item.getLaptop() != null){
                productName.setText(item.getLaptop().getName());
                if (item.getQuantity()==0){
                    productQuantity.setText(itemView.getResources().getString(R.string.notAvailable));
                } else {
                    productQuantity.setText(String.valueOf(item.getQuantity()) + " " + itemView.getResources().getString(R.string.available));
                }
            }
        }
    }
}
