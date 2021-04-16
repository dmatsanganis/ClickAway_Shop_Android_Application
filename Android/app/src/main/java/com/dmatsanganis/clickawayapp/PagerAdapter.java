package com.dmatsanganis.clickawayapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.dmatsanganis.clickawayapp.ProductFragments.LaptopsFragment;
import com.dmatsanganis.clickawayapp.ProductFragments.SmartphonesFragment;
import com.dmatsanganis.clickawayapp.ProductFragments.TabletsFragment;

public class PagerAdapter extends FragmentStateAdapter {
    public PagerAdapter(@NonNull FragmentActivity fragmentActivity){ super(fragmentActivity); }

    //Method that creates the specific Fragments for each page
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new SmartphonesFragment();
            case 1:
                return new TabletsFragment();
            case 2:
                return new LaptopsFragment();
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

