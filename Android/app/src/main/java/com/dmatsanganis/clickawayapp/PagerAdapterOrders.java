package com.dmatsanganis.clickawayapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.dmatsanganis.clickawayapp.OrderFragments.CompletedFragment;
import com.dmatsanganis.clickawayapp.OrderFragments.ExpiredFragment;
import com.dmatsanganis.clickawayapp.OrderFragments.PendingFragment;

public class PagerAdapterOrders extends FragmentStateAdapter {
    public PagerAdapterOrders(@NonNull FragmentActivity fragmentActivity){ super(fragmentActivity); }

    //Method that creates the specific Fragments for each page
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new PendingFragment();
            case 1:
                return new ExpiredFragment();
            case 2:
                return new CompletedFragment();
            default:
                return null;
        }
    }

    //Method that returns the number of the pages
    @Override
    public int getItemCount() {
        return 3;
    }
}

