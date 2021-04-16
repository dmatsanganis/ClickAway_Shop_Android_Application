package com.dmatsanganis.clickawayapp.RecyclerViewAdapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dmatsanganis.clickawayapp.Classes.MySnackBar;
import com.dmatsanganis.clickawayapp.Classes.Order;
import com.dmatsanganis.clickawayapp.Classes.Store;
import com.dmatsanganis.clickawayapp.OrderActivities.OrderDateTime;
import com.dmatsanganis.clickawayapp.OrderActivities.OrderPickup;
import com.dmatsanganis.clickawayapp.R;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OrderListRecyclerViewAdapter extends RecyclerView.Adapter<OrderListRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Order> orders;
    private ArrayList<Store> stores;
    private int status;
    private Context context;
    private MySnackBar mySnackBar;

    public OrderListRecyclerViewAdapter(Context context, ArrayList<Order> orders){
        this.orders = orders;
        this.context = context;
    }

    public void setOrders(ArrayList<Order> orders, ArrayList<Store> stores, int status){
        this.orders = orders;
        this.stores = stores;
        this.status = status;
        notifyDataSetChanged();
    }

    //Create view holder for the recycler view
    @NonNull
    @Override
    public OrderListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for this view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //Method that binds the elements of the view holder
    @Override
    public void onBindViewHolder(@NonNull OrderListRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.bind(orders.get(position), stores.get(position), status);
    }
    //Method that returns the number of the items which the recycler view contains
    @Override
    public int getItemCount() {
        return orders.size();
    }

    //Set the views for the view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView storeName, storeAddress, date, time, totalCost;
        Button edit, start;
        RecyclerView recyclerView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            edit = itemView.findViewById(R.id.editOrder);
            start = itemView.findViewById(R.id.startOrder);
            storeName = itemView.findViewById(R.id.finalStoreName);
            storeAddress = itemView.findViewById(R.id.finalStoreAddress);
            date = itemView.findViewById(R.id.finalDate);
            time = itemView.findViewById(R.id.finalTime);
            totalCost = itemView.findViewById(R.id.finalTotalCost);
            recyclerView = itemView.findViewById(R.id.recyclerViewFinalProducts);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

        }

        public void bind(Order order, Store store, int status){
            if (status == 0){
                //show all buttons
                edit.setVisibility(View.VISIBLE);
                start.setVisibility(View.VISIBLE);
            } else if (status == 1){
                //show only reschedule button
                edit.setVisibility(View.VISIBLE);
                start.setVisibility(View.GONE);
            } else if (status == 2){
                //don't show buttons
                edit.setVisibility(View.INVISIBLE);
                start.setVisibility(View.GONE);
            }
            date.setText(itemView.getResources().getString(R.string.date) + ": " + order.getDate());
            time.setText(itemView.getResources().getString(R.string.time) + ": " + order.getTime());
            if(itemView.getResources().getConfiguration().getLocales().get(0).toString().equals("el")){
                storeName.setText(store.getTitle_gr());
                storeAddress.setText(store.getAddress_gr());
            } else {
                storeName.setText(store.getTitle_en());
                storeAddress.setText(store.getAddress_en());
            }
            DecimalFormat formatter = new DecimalFormat("0.00");
            totalCost.setText(itemView.getResources().getString(R.string.total_cost) + ": " + formatter.format(order.getUser().getCart().getTotal_cost()) + "€");
            OrderCartRecyclerViewAdapter adapter = new OrderCartRecyclerViewAdapter(order.getUser().getCart().getItems());
            recyclerView.setAdapter(adapter);

            edit.setOnClickListener(view -> {
                Intent intent = new Intent(itemView.getContext(), OrderDateTime.class);
                intent.putExtra("User", order.getUser());
                intent.putExtra("Store", store);
                intent.putExtra("PastOrder", order);
                itemView.getContext().startActivity(intent);
            });

            start.setOnClickListener(view -> {
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                if(!order.getDate().equals(dateFormat.format(new Date()))){
                    mySnackBar = new MySnackBar();
                    mySnackBar.show(itemView.getResources().getString(R.string.notToday), itemView.getContext(), itemView, false);
                } else {
                    Intent intent = new Intent(itemView.getContext(), OrderPickup.class);
                    intent.putExtra("Store", store);
                    intent.putExtra("Order", order);
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }
}
