package com.dmatsanganis.clickawayapp.RecyclerViewAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dmatsanganis.clickawayapp.Classes.ItemToPurchase;
import com.dmatsanganis.clickawayapp.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class OrderCartRecyclerViewAdapter extends RecyclerView.Adapter<OrderCartRecyclerViewAdapter.ViewHolder> {
    private ArrayList<ItemToPurchase> items;

    public OrderCartRecyclerViewAdapter(ArrayList<ItemToPurchase> items) {
        this.items = items;
    }

    //Create view holder for the recycler view
    @NonNull
    @Override
    public OrderCartRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for this view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_cart_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //Method that binds the elements of the view holder
    @Override
    public void onBindViewHolder(@NonNull OrderCartRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    //Method that returns the number of the items which the recycler view contains
    @Override
    public int getItemCount() {
        return items.size();
    }

    //Set the views for the view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, cost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.orderProductName);
            cost = itemView.findViewById(R.id.orderProductCost);
        }

        public void bind(ItemToPurchase item){
            DecimalFormat formatter = new DecimalFormat("0.00");
            if (item.getSmartphone() != null) {
                name.setText(item.getSmartphone().getName());
                cost.setText(String.valueOf(item.getQuantity())  + " x " + formatter.format(item.getSmartphone().getPrice()) + "€ = " + formatter.format(item.getCost()) + "€");
            } else if (item.getTablet() != null) {
                name.setText(item.getTablet().getName());
                cost.setText(String.valueOf(item.getQuantity())  + " x " + formatter.format(item.getTablet().getPrice()) + "€ = " + formatter.format(item.getCost()) + "€");
            } else if (item.getLaptop() != null) {
                name.setText(item.getLaptop().getName());
                cost.setText(String.valueOf(item.getQuantity())  + " x " + formatter.format(item.getLaptop().getPrice()) + "€ = " + formatter.format(item.getCost()) + "€");
            }
        }
    }
}
