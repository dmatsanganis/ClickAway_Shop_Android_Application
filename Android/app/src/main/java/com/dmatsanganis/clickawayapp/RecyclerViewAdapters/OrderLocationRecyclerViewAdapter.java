package com.dmatsanganis.clickawayapp.RecyclerViewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dmatsanganis.clickawayapp.Classes.ItemInStorage;
import com.dmatsanganis.clickawayapp.Classes.Store;
import com.dmatsanganis.clickawayapp.R;

import java.util.ArrayList;

public class OrderLocationRecyclerViewAdapter extends RecyclerView.Adapter<OrderLocationRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Store> stores;
    private Context context;
    private boolean canContinue;
    private int checkedPosition = -1;

    public OrderLocationRecyclerViewAdapter(Context context, ArrayList<Store> stores) {
        this.context = context;
        this.stores = stores;
    }

    public void setStores(ArrayList<Store> stores, boolean canContinue) {
        this.stores = stores;
        this.canContinue = canContinue;
        this.checkedPosition = -1;
        notifyDataSetChanged();
    }

    public Store getSelected(){
        if(checkedPosition != -1){
            return stores.get(checkedPosition);
        }
        return null;
    }

    //Create view holder for the recycler view
    @NonNull
    @Override
    public OrderLocationRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for this view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_location_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //Method that binds the elements of the view holder
    @Override
    public void onBindViewHolder(@NonNull OrderLocationRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.bind(stores.get(position), canContinue);
    }

    //Method that returns the number of the items which the recycler view contains
    @Override
    public int getItemCount() {
        return stores.size();
    }

    //Set the views for the view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView storeName, storeLocation;
        RecyclerView recyclerView;
        ImageView check;
        ArrayList<ItemInStorage> items;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            storeName = itemView.findViewById(R.id.storeName);
            storeLocation = itemView.findViewById(R.id.storeLocation);
            recyclerView = itemView.findViewById(R.id.recyclerViewItem);
            check = itemView.findViewById(R.id.check);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            items = new ArrayList<>();
        }


        public void bind(Store store, boolean canContinue){
            items.clear();
            if(itemView.getResources().getConfiguration().getLocales().get(0).toString().equals("el")){
                storeName.setText(store.getTitle_gr());
                storeLocation.setText(store.getAddress_gr());
            } else {
                storeName.setText(store.getTitle_en());
                storeLocation.setText(store.getAddress_en());
            }

            items.addAll(store.getStorage());

            OrderStorageRecyclerViewAdapter adapter = new OrderStorageRecyclerViewAdapter(items);
            recyclerView.setAdapter(adapter);


            if(canContinue){
                check.setVisibility(View.VISIBLE);
                check.setEnabled(true);
                if (checkedPosition == -1){
                    check.setImageResource(R.drawable.ic_radio_button_unchecked);
                } else {
                    //Else if a store is selected
                    //If the same store is clicked change image accordingly
                    if (checkedPosition == getAdapterPosition()){
                        check.setImageResource(R.drawable.ic_check_circle_outline);
                    } else{
                        //Else if another store is clicked change image accordingly
                        check.setImageResource(R.drawable.ic_radio_button_unchecked);
                    }
                }
                //When the image is clicked change the image and the checked position
                check.setOnClickListener(view -> {
                    check.setImageResource(R.drawable.ic_check_circle_outline);
                    if (checkedPosition != getAdapterPosition()){
                        notifyItemChanged(checkedPosition);
                        checkedPosition = getAdapterPosition();
                    }
                });
            } else {
                check.setVisibility(View.INVISIBLE);
                check.setEnabled(false);
            }
        }

    }
}
