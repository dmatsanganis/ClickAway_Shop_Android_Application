package com.dmatsanganis.clickawayapp.MenuFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.dmatsanganis.clickawayapp.PagerAdapterOrders;
import com.dmatsanganis.clickawayapp.R;


public class ActiveOrdersFragment extends Fragment {

    public ActiveOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_active_orders, container, false);

        ViewPager2 viewPager2 = view.findViewById(R.id.viewPager1);
        PagerAdapterOrders pagerAdapter = new PagerAdapterOrders(getActivity());
        viewPager2.setAdapter(pagerAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tabBar1);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position){
                case 0:{
                    tab.setText(getString(R.string.pending));
                    tab.setIcon(R.drawable.ic_access_time_filled);
                    break;
                }
                case 1:{
                    tab.setText(R.string.expired);
                    tab.setIcon(R.drawable.ic_cancel_2);
                    break;
                }
                case 2:{
                    tab.setText(getString(R.string.completed));
                    tab.setIcon(R.drawable.ic_check_circle);
                    break;
                }
            }
        });
        tabLayoutMediator.attach();
        return view;
    }
}