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
import com.dmatsanganis.clickawayapp.R;
import com.dmatsanganis.clickawayapp.Classes.Smartphone;
import com.dmatsanganis.clickawayapp.ProductDetails.SmartphoneDetails;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class SmartphoneRecyclerViewAdapter extends RecyclerView.Adapter<SmartphoneRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Smartphone> smartphones;
    public SmartphoneRecyclerViewAdapter(ArrayList<Smartphone> smartphones){
        this.smartphones = smartphones;
    }

    //Create view holder for the recycler view
    @NonNull
    @Override
    public SmartphoneRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for this view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //Method that binds the elements of the view holder
    @Override
    public void onBindViewHolder(@NonNull SmartphoneRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.bind(smartphones.get(position));
    }

    //Method that returns the number of the items which the recycler view contains
    @Override
    public int getItemCount() {
        return smartphones.size();
    }

    //Set the views for the view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;
        CardView cardView;
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageViewProduct);
        }

        public void bind(Smartphone smartphone){
            DecimalFormat formatter = new DecimalFormat("0.00");
            price.setText(itemView.getResources().getString(R.string.price) + ": " + formatter.format(smartphone.getPrice()) + "€");
            name.setText(smartphone.getName());
            Glide.with(itemView.getContext()).load(smartphone.getImage_url()).into(imageView);
            //Add click listener on each card view in order to open the details of the smartphone
            cardView.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), SmartphoneDetails.class);
                intent.putExtra("Smartphone", smartphone);
                view.getContext().startActivity(intent);
            });
        }
    }
}
