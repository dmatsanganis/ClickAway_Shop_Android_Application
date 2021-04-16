package com.dmatsanganis.clickawayapp.MenuFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.dmatsanganis.clickawayapp.PagerAdapter;
import com.dmatsanganis.clickawayapp.R;

public class ProductsFragment extends Fragment {
    public ProductsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_products, container, false);

        //Create and set the view pager
        ViewPager2 viewPager2 = view.findViewById(R.id.viewPager);
        PagerAdapter pagerAdapter = new PagerAdapter(getActivity());
        viewPager2.setAdapter(pagerAdapter);

        //Create and set the tab bar for each product
        TabLayout tabLayout = view.findViewById(R.id.tabBar);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position){
                case 0:{
                    tab.setText("Smartphones");
                    tab.setIcon(R.drawable.ic_smartphone);
                    break;
                }
                case 1:{
                    tab.setText("Tablets");
                    tab.setIcon(R.drawable.ic_tablet);
                    break;
                }
                case 2:{
                    tab.setText("Laptops");
                    tab.setIcon(R.drawable.ic_laptop);
                    break;
                }
            }
        });

        tabLayoutMediator.attach();

        return view;
    }
}