package com.dmatsanganis.clickawayapp.RecyclerViewAdapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.dmatsanganis.clickawayapp.Classes.ItemToPurchase;
import com.dmatsanganis.clickawayapp.Classes.ShoppingCart;
import com.dmatsanganis.clickawayapp.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CartRecyclerViewAdapter extends RecyclerView.Adapter<CartRecyclerViewAdapter.ViewHolder> {
    private ArrayList<ItemToPurchase> items;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public CartRecyclerViewAdapter(ArrayList<ItemToPurchase> items) {
        this.items = items;
    }

    //Create view holder for the recycler view
    @NonNull
    @Override
    public CartRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for this view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_cart_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //Method that binds the elements of the view holder
    @Override
    public void onBindViewHolder(@NonNull CartRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    //Method that returns the number of the items which the recycler view contains
    @Override
    public int getItemCount() {
        return items.size();
    }

    //Set the views for the view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, cost;
        CardView cardView;
        Button edit, delete;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.orderProductName);
            cost = itemView.findViewById(R.id.orderProductPrice);
            cardView = itemView.findViewById(R.id.cardViewCart);
            edit = itemView.findViewById(R.id.cartEdit);
            delete = itemView.findViewById(R.id.cartDelete);
            imageView = itemView.findViewById(R.id.cartImage);
        }

        public void bind(ItemToPurchase item, int position){
            DecimalFormat formatter = new DecimalFormat("0.00");
            if (item.getSmartphone() != null) {
                name.setText(item.getSmartphone().getName());
                Glide.with(itemView.getContext()).load(item.getSmartphone().getImage_url()).into(imageView);
                cost.setText(String.valueOf(item.getQuantity())  + " x " + formatter.format(item.getSmartphone().getPrice()) + "€ = " + formatter.format(item.getCost()) + "€");
            }
            if (item.getTablet() != null) {
                name.setText(item.getTablet().getName());
                Glide.with(itemView.getContext()).load(item.getTablet().getImage_url()).into(imageView);
                cost.setText(String.valueOf(item.getQuantity())  + " x " + formatter.format(item.getTablet().getPrice()) + "€ = " + formatter.format(item.getCost()) + " €");
            }
            if (item.getLaptop() != null) {
                name.setText(item.getLaptop().getName());
                Glide.with(itemView.getContext()).load(item.getLaptop().getImage_url()).into(imageView);
                cost.setText(String.valueOf(item.getQuantity())  + " x " + formatter.format(item.getLaptop().getPrice()) + "€ = " + formatter.format(item.getCost()) + " €");
            }
            delete.setOnClickListener(view -> deleteFromCart(view, item, position));
            edit.setOnClickListener(view -> editFromCart(view, item, position));
        }

        //Method that deletes the selected product from the cart
        public void deleteFromCart(View view, ItemToPurchase item, int position){
            AlertDialog.Builder builder = new android.app.AlertDialog.Builder(view.getContext());
            View delete_layout = LayoutInflater.from(view.getContext()).inflate(R.layout.delete_dialog,null);
            builder.setView(delete_layout);
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            TextView message = delete_layout.findViewById(R.id.deleteMessage);
            Button buttonYes = delete_layout.findViewById(R.id.deleteYes);
            Button buttonNo = delete_layout.findViewById(R.id.deleteNo);
            ImageView image = delete_layout.findViewById(R.id.deleteImage);

            String item_name = null;
            String image_url = null;
            if(item.getSmartphone()!=null){
                item_name = item.getSmartphone().getName();
                image_url = item.getSmartphone().getImage_url();
            }
            else if(item.getTablet()!=null){
                item_name = item.getTablet().getName();
                image_url = item.getTablet().getImage_url();
            }
            else if(item.getLaptop()!=null){
                item_name = item.getLaptop().getName();
                image_url = item.getLaptop().getImage_url();
            }
            message.setText(itemView.getResources().getString(R.string.deleteCart) + item_name + itemView.getResources().getString(R.string.questiionMark));
            Glide.with(view.getContext()).load(image_url).into(image);

            mAuth = FirebaseAuth.getInstance();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            myRef = mFirebaseDatabase.getReference();
            currentUser = mAuth.getCurrentUser();

            buttonYes.setOnClickListener(view1 -> {
                items.remove(position);
                if (items.size()>0){
                    ShoppingCart shoppingCart = new ShoppingCart(items);
                    myRef = myRef.child("Users").child(currentUser.getUid()).child("Cart");
                    myRef.setValue(shoppingCart);
                } else {
                    myRef = myRef.child("Users").child(currentUser.getUid()).child("Cart");
                    myRef.removeValue();
                }
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, items.size());
                dialog.dismiss();
            });

            buttonNo.setOnClickListener(view1 -> {
                dialog.dismiss();
            });
        }

        //Method that edits the selected product from the cart
        public void editFromCart(View view, ItemToPurchase item, int position){
            AlertDialog.Builder builder = new android.app.AlertDialog.Builder(view.getContext());
            View edit_layout = LayoutInflater.from(view.getContext()).inflate(R.layout.edit_dialog,null);
            builder.setView(edit_layout);
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            TextView message = edit_layout.findViewById(R.id.editMessage);
            Button buttonYes = edit_layout.findViewById(R.id.editYes);
            ImageView image = edit_layout.findViewById(R.id.editImage);
            EditText editQuantity = edit_layout.findViewById(R.id.editQuantity);
            FloatingActionButton addOne = edit_layout.findViewById(R.id.addOne);
            FloatingActionButton removeOne = edit_layout.findViewById(R.id.removeOne);
            ConstraintLayout parent = edit_layout.findViewById(R.id.parent);

            parent.setOnClickListener(view1 -> parent.requestFocus());

            editQuantity.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            });

            editQuantity.setText(String.valueOf(item.getQuantity()));
            String item_name = null;
            String image_url = null;
            if(item.getSmartphone()!=null){
                item_name = item.getSmartphone().getName();
                image_url = item.getSmartphone().getImage_url();
            }
            else if(item.getTablet()!=null){
                item_name = item.getTablet().getName();
                image_url = item.getTablet().getImage_url();
            }
            else if(item.getLaptop()!=null){
                item_name = item.getLaptop().getName();
                image_url = item.getLaptop().getImage_url();
            }
            message.setText(itemView.getResources().getString(R.string.newEdit) + item_name);
            Glide.with(view.getContext()).load(image_url).into(image);

            mAuth = FirebaseAuth.getInstance();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            myRef = mFirebaseDatabase.getReference();
            currentUser = mAuth.getCurrentUser();

            addOne.setOnClickListener(view1 -> editQuantity.setText(String.valueOf(Integer.parseInt(editQuantity.getText().toString()) + 1)));
            removeOne.setOnClickListener(view1 -> {
                if (Integer.parseInt(editQuantity.getText().toString()) > 1){
                    editQuantity.setText(String.valueOf(Integer.parseInt(editQuantity.getText().toString()) - 1));
                }
            });

            buttonYes.setOnClickListener(view1 -> {
                if (editQuantity.getText().toString().equals("0")|| editQuantity.getText().toString().equals("")){
                    Toast.makeText(view.getContext(), view.getResources().getString(R.string.atLeastOne), Toast.LENGTH_SHORT).show();
                    editQuantity.setText("1");
                } else {
                    if (item.getLaptop()!=null){
                        item.setQuantity(Integer.parseInt(editQuantity.getText().toString()), item.getLaptop());
                    } else if (item.getSmartphone()!=null){
                        item.setQuantity(Integer.parseInt(editQuantity.getText().toString()), item.getSmartphone());
                    } else if (item.getTablet()!=null){
                        item.setQuantity(Integer.parseInt(editQuantity.getText().toString()), item.getTablet());
                    }
                    items.set(position, item);
                    myRef = myRef.child("Users").child(currentUser.getUid()).child("Cart").child("items").child(String.valueOf(position));
                    myRef.setValue(item);
                    notifyItemChanged(position);
                    dialog.dismiss();
                }
            });
        }
    }

    //Method that hides the keyboard
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
