package com.dmatsanganis.clickawayapp.RecyclerViewAdapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dmatsanganis.clickawayapp.Classes.Laptop;
import com.dmatsanganis.clickawayapp.ProductDetails.LaptopDetails;
import com.dmatsanganis.clickawayapp.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class LaptopRecyclerViewAdapter extends RecyclerView.Adapter<LaptopRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Laptop> laptops;
    public LaptopRecyclerViewAdapter(ArrayList<Laptop> laptops){
        this.laptops = laptops;
    }

    //Create view holder for the recycler view
    @NonNull
    @Override
    public LaptopRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for this view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //Method that binds the elements of the view holder
    @Override
    public void onBindViewHolder(@NonNull LaptopRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.bind(laptops.get(position));
    }

    //Method that returns the number of the items which the recycler view contains
    @Override
    public int getItemCount() {
        return laptops.size();
    }

    //Set the views for the view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;
        ImageView imageView;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageViewProduct);
        }

        public void bind(Laptop laptop){
            DecimalFormat formatter = new DecimalFormat("0.00");
            price.setText(itemView.getResources().getString(R.string.price) + ": " + formatter.format(laptop.getPrice()) + "€");
            name.setText(laptop.getName());
            Glide.with(itemView.getContext()).load(laptop.getImage_url()).into(imageView);

            //Add click listener on each card view in order to open the details of the laptop
            cardView.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), LaptopDetails.class);
                intent.putExtra("Laptop", laptop);
                view.getContext().startActivity(intent);
            });
        }
    }
}
