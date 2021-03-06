package com.example.wayout_ver_01.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.wayout_ver_01.Activity.CreateShop.Search_shop;
import com.example.wayout_ver_01.Class.OnSingleClickListener;
import com.example.wayout_ver_01.R;
import com.example.wayout_ver_01.RecyclerView.ViewPager.VP_adapter;
import com.example.wayout_ver_01.databinding.FragmentSearchBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FragmentSearch extends Fragment {
    private FragmentSearch_Cafe fragmentSearch_cafe;
    private FragmentSearch_Theme fragmentSearch_theme;
    private ViewPager2 viewPager2;
    private final String TAG = this.getClass().getSimpleName();
    private View view;
    private TabLayout tabLayout;
    private VP_adapter search_vp_adapter;
    private String search;
    private ImageView search_btn;
    private FragmentSearchBinding binding;
    private ActivityResultLauncher<Intent> launcher;
    private String str = "";

    public static FragmentSearch newInstance(String search) {
        Bundle args = new Bundle();
        FragmentSearch fragment = new FragmentSearch();
        args.putString("search", search);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            str = getArguments().getString("?????????");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        CreateFragment();
        CreateViewPager();
        settingTablayout();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        view = binding.getRoot();


        if(str == null){
            str = "";
        }

        CreateFragment();
        CreateViewPager();
        settingTablayout();


        // ????????? ?????? ?????? ?????? ??????
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent i = result.getData();
                            if(i != null) {
                                str = i.getStringExtra("?????????");
                                binding.searchEtContent.setText(str);
                                binding.searchBtnReset.setVisibility(View.VISIBLE);
                                CreateFragment();
                                CreateViewPager();
                                settingTablayout();
                                Log.e("FragmentSearch, 80", "str : " + str );
                            }
                        }
                    }
                });

        binding.searchBtnReset.setOnClickListener((v -> {
            str = "";
            // ??????????????? ??? ???????????? ????????? ?????? ??????
            CreateFragment();
            CreateViewPager();
            settingTablayout();
            v.setVisibility(View.INVISIBLE);
            binding.searchEtContent.setText("");
        }));

        // ????????? ?????? ??????????????? ?????? ?????? ??????
        binding.searchLayout.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent i = new Intent(view.getContext(), Search_shop.class);
                launcher.launch(i);
            }
        });

        // ??????????????? ?????? ??????
        binding.searchSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                str = "";
                // ??????????????? ??? ???????????? ????????? ?????? ??????
                CreateFragment();
                CreateViewPager();
                settingTablayout();
                binding.searchBtnReset.setVisibility(View.GONE);
                binding.searchEtContent.setText("");
                binding.searchSwipe.setRefreshing(false);
            }
        });



        return view;
    }



    private void settingTablayout() {
        tabLayout = view.findViewById(R.id.Search_TabLayout);
        // Tab ??????????????? ViewPager ??? ???????????? ???????????? ??? ??? ???????????????
        new TabLayoutMediator(
                tabLayout,
                viewPager2,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        String[] data = {"????????????", "????????????"};
                        tab.setText(data[position]);
                    }
                }).attach();
    }

    private void CreateViewPager() {
        viewPager2 = binding.searchViewPager;
        search_vp_adapter = new VP_adapter(this);
        search_vp_adapter.addItem(fragmentSearch_cafe);
        search_vp_adapter.addItem(fragmentSearch_theme);
        viewPager2.setAdapter(search_vp_adapter);
    }

    private void CreateFragment() {
            fragmentSearch_cafe = FragmentSearch_Cafe.newInstance(str);
            fragmentSearch_theme = FragmentSearch_Theme.newInstance(str);
    }


    //    private void replaceFragment(Fragment fragment) {
//        // ?????????????????????
//        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.left_to_right, R.anim.right_to_left);
////        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.replace(R.id.search_containers, fragment);
//        fragmentTransaction.commit();
//    }

}