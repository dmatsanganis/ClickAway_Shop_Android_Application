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
import com.dmatsanganis.clickawayapp.Classes.Tablet;
import com.dmatsanganis.clickawayapp.R;
import com.dmatsanganis.clickawayapp.ProductDetails.TabletDetails;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class TabletRecyclerViewAdapter extends RecyclerView.Adapter<TabletRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Tablet> tablets;
    public TabletRecyclerViewAdapter(ArrayList<Tablet> tablets){
        this.tablets = tablets;
    }

    //Create view holder for the recycler view
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for this view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //Method that binds the elements of the view holder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(tablets.get(position));
    }

    //Method that returns the number of the items which the recycler view contains
    @Override
    public int getItemCount() {
        return tablets.size();
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
        public void bind(Tablet tablet){
            DecimalFormat formatter = new DecimalFormat("0.00");
            price.setText(itemView.getResources().getString(R.string.price) + ": " + formatter.format(tablet.getPrice()) + "€");
            name.setText(tablet.getName());
            Glide.with(itemView.getContext()).load(tablet.getImage_url()).into(imageView);

            //Add click listener on each card view in order to open the details of the tablet
            cardView.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), TabletDetails.class);
                intent.putExtra("Tablet", tablet);
                view.getContext().startActivity(intent);
            });
        }
    }
}
