package com.example.wayout_ver_01.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.wayout_ver_01.Activity.Gallery.GalleryBoard_write;
import com.example.wayout_ver_01.Class.PreferenceManager;
import com.example.wayout_ver_01.R;
import com.example.wayout_ver_01.RecyclerView.Gallery.GalleryBoard_adapter;
import com.example.wayout_ver_01.Retrofit.DTO_gallery;
import com.example.wayout_ver_01.Retrofit.RetrofitClient;
import com.example.wayout_ver_01.Retrofit.RetrofitInterface;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FragmentComm_GalleryBorad extends Fragment {

    public static FragmentComm_GalleryBorad newInstance() {

        Bundle args = new Bundle();

        FragmentComm_GalleryBorad fragment = new FragmentComm_GalleryBorad();
        fragment.setArguments(args);
        return fragment;
    }

    private ImageView like, galleryBoard_reset, galleryBoard_search_btn;
    private EditText galleryBoard_search;
    private TextView galleryBoard_tv_spinner;
    private Button galleryBoard_write_btn;
    private RecyclerView galleryBoard_rv;
    private NestedScrollView galleryBoard_scroll;
    private ProgressBar galleryBoard_progress;
    private GalleryBoard_adapter galleryBoard_adapter;
    private Spinner galleryBoard_spinner;
    private boolean isClicked;
    private String category = "cafe";
    private InputMethodManager imm;
    private String search_con;
    private SwipeRefreshLayout galleryBoard_swipe;

    // search true ??? ????????? ???????????? ???????????? ???????????? ????????????.
    // scroll ??? true ??? ????????? ?????? ????????? ????????? ?????? ???????????? ??????, ????????? ???????????? ????????????.
    private boolean scroll, search;
    private int page = 1, limit = 8;

    @Override
    public void onStop() {
        super.onStop();
        page = 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comm__gallery_borad, container, false);

        // view ??? ??????
        viewSet(view);
        // ????????? ?????? ?????? 1.????????? / 2.????????? / 3. ?????????
        setSpinner(view);
        // ?????????????????? ?????? ??? ??????
        setAdpater(view);

        // ????????? ??????????????? ????????????
        galleryBoard_write_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ??? ????????????
                Intent intent = new Intent(view.getContext(), GalleryBoard_write.class);
                intent.putExtra("mode", false);
                startActivity(intent);
            }
        });

        // ????????? ????????? ????????? ??????
        galleryBoard_scroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                galleryBoard_progress.setVisibility(View.VISIBLE);
                if(scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()){
                    page ++;
                    // ????????? ?????? ?????? ?????????
                    if(search){
                        // ?????? ?????? ?????? ?????????
                        getSearch();
                    }else {
                        // ?????? ?????? ?????????
                        getScroll();
                    }
                    galleryBoard_progress.setVisibility(View.INVISIBLE);
                }
            }
        });

        // ?????? ?????? ????????? ?????? ???????????????
        galleryBoard_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ?????? ?????? ??????
                search_con = galleryBoard_search.getText().toString();
                //  ?????? ??? ????????? ??????
                //  ?????? ?????????, ???????????? ????????????, ????????? ??????????????? ????????? ????????? ?????????, ?????? ??????
                page = 1;
                galleryBoard_reset.setVisibility(View.VISIBLE);
                galleryBoard_rv.requestFocus();
                search = true;

                // ?????? ????????? ???????????? ????????? ????????????
                getSearchItems();

            }
        });

        galleryBoard_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ?????? ?????????
                galleryBoard_search.setText("");
                page = 1;
                search = false;
                scroll = false;
                search_con = "";
                galleryBoard_reset.setVisibility(View.GONE);
                // ?????? ????????? ????????????
                getItems();
            }
        });
        // swipeRefresh ??? ???????????? ?????? ??????
        galleryBoard_swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // swipe ??? ????????? ??????
                // search true ??? ????????? ???????????? ???????????? ???????????? ????????????.
                // scroll ??? true ??? ????????? ?????? ????????? ????????? ?????? ???????????? ??????, ????????? ???????????? ????????????.
                // page ??? ????????? ????????? ?????? ??????
                // galleryBoard_search ??? ????????? ?????????
                // search_con ??? ????????? ????????? ????????? ??????
                // galleryBoard_reset ??? ???????????? ????????? ?????? ??????
                // getItems() ??? ?????? ????????? ???????????? ???????????????.
                galleryBoard_search.setText("");
                page = 1;
                search = false;
                scroll = false;
                search_con = "";
                galleryBoard_reset.setVisibility(View.GONE);
                getItems();

//                // ??????????????? ???????????? ??????
                galleryBoard_swipe.setRefreshing(false);
            }
        });

        // ?????? ??????
        galleryBoard_progress.setVisibility(View.INVISIBLE);

        return view;
    }
    // ??????????????? ???????????? ??????

    @Override
    public void onResume() {
        super.onResume();

        if(!search){
            getItems();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        category = "gallery_cafe";
        scroll = false;
        search = false;
        page = 1;
    }

    private void getSearchItems() {
        search_con = galleryBoard_search.getText().toString();
        page = 1;
        // con ??? ???????????? ??????????????? ??????
        galleryBoard_reset.setVisibility(View.VISIBLE);
        galleryBoard_search.setText("");
        galleryBoard_rv.requestFocus();
        search = true;
        String user_id = PreferenceManager.getString(requireContext(),"autoNick");

        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<ArrayList<DTO_gallery>> call = retrofitInterface.getGallerySearch(page,limit,category,search_con, user_id);
        call.enqueue(new Callback<ArrayList<DTO_gallery>>() {
            @Override
            public void onResponse(Call<ArrayList<DTO_gallery>> call, Response<ArrayList<DTO_gallery>> response) {
                if(response.body() != null && response.isSuccessful()) {
                    if(!scroll) {
                        galleryBoard_adapter.clearList();
                        galleryBoard_adapter.notifyDataSetChanged();
                    }
                    for(int i = 0; i < response.body().size(); i++){
                        galleryBoard_adapter.addItem(new DTO_gallery(
                                    response.body().get(i).getWriter(),
                                    response.body().get(i).getCafe(),
                                    response.body().get(i).getTheme(),
                                    response.body().get(i).getDate(),
                                    response.body().get(i).getTotal_like(),
                                    response.body().get(i).isClick(),
                                    response.body().get(i).getContent(),
                                    response.body().get(i).getBoard_number(),
                                    response.body().get(i).getImage()));

                        galleryBoard_adapter.notifyItemInserted(i + (page - 1) * 8);
                    }
                    galleryBoard_progress.setVisibility(View.INVISIBLE);
                    imm.hideSoftInputFromWindow(galleryBoard_search.getWindowToken(), 0);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<DTO_gallery>> call, Throwable t) {

            }
        });
//        call.enqueue(new Callback<ArrayList<DTO_gallery>>() {
//            @Override
//            public void onResponse(Call<ArrayList<DTO_gallery>> call, Response<ArrayList<DTO_gallery>> response) {
//                if(response.body() != null && response.isSuccessful()){
//                    if (!scroll) {
//                        galleryBoard_adapter.clearList();
//                        galleryBoard_adapter.notifyDataSetChanged();
//                    }
//                    for (int i = 0; i < response.body().size(); i++) {
//                        galleryBoard_adapter.addItem(new DTO_gallery(
//                                    response.body().get(i).getWriter(),
//                                    response.body().get(i).getCafe(),
//                                    response.body().get(i).getTheme(),
//                                    response.body().get(i).getDate(),
//                                    response.body().get(i).getTotal_like(),
//                                    response.body().get(i).isClick(),
//                                    response.body().get(i).getContent(),
//                                    response.body().get(i).getBoard_number(),
//                                    response.body().get(i).getImage()));
//
//                        galleryBoard_adapter.notifyItemInserted(i + (page - 1) * 8);
//                    }
//                    galleryBoard_progress.setVisibility(View.INVISIBLE);
//                    imm.hideSoftInputFromWindow(galleryBoard_search.getWindowToken(), 0);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ArrayList<DTO_gallery>> call, Throwable t) {
//                Toast.makeText(requireContext(), "?????? ????????? : " + t, Toast.LENGTH_SHORT).show();
//
//            }
//        });

    }

    public void getItems() {
        String user_id = PreferenceManager.getString(requireContext(), "autoNick");
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<ArrayList<DTO_gallery>> call = retrofitInterface.getGalleryBoard(page,limit,user_id);
        call.enqueue(new Callback<ArrayList<DTO_gallery>>() {

            @Override
            public void onResponse(Call<ArrayList<DTO_gallery>> call, Response<ArrayList<DTO_gallery>> response) {
                if(response.isSuccessful() && response.body() != null){
                    galleryBoard_adapter.clearList();
                    galleryBoard_adapter.notifyDataSetChanged();
                    for (int i = 0; i < response.body().size(); i++){
                                galleryBoard_adapter.addItem(new DTO_gallery(
                                        response.body().get(i).getWriter(),
                                        response.body().get(i).getCafe(),
                                        response.body().get(i).getTheme(),
                                        response.body().get(i).getDate(),
                                        response.body().get(i).getTotal_like(),
                                        response.body().get(i).isClick(),
                                        response.body().get(i).getContent(),
                                        response.body().get(i).getBoard_number(),
                                        response.body().get(i).getImage()));
                                galleryBoard_adapter.notifyItemInserted(i);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<DTO_gallery>> call, Throwable t) {

            }
        });
    }

    private void getSearch() {
    }

    private void getScroll() {
        scroll = true;
        String user_id = PreferenceManager.getString(requireContext(), "autoNick");
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<ArrayList<DTO_gallery>> call = retrofitInterface.getGalleryBoard(page,limit,user_id);
        call.enqueue(new Callback<ArrayList<DTO_gallery>>() {

            @Override
            public void onResponse(Call<ArrayList<DTO_gallery>> call, Response<ArrayList<DTO_gallery>> response) {
                if(response.isSuccessful() && response.body() != null){
                    for (int i = 0; i < response.body().size(); i++){
                        galleryBoard_adapter.addItem(new DTO_gallery(
                                response.body().get(i).getWriter(),
                                response.body().get(i).getCafe(),
                                response.body().get(i).getTheme(),
                                response.body().get(i).getDate(),
                                response.body().get(i).getTotal_like(),
                                response.body().get(i).isClick(),
                                response.body().get(i).getContent(),
                                response.body().get(i).getBoard_number(),
                                response.body().get(i).getImage()));
                        galleryBoard_adapter.notifyItemInserted(galleryBoard_adapter.getItemCount() + i);
                    }

                }
            }

            @Override
            public void onFailure(Call<ArrayList<DTO_gallery>> call, Throwable t) {

            }
        });
    }

    private void setAdpater(View view) {
        //span ?????? ??????
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(),2, GridLayoutManager.VERTICAL ,false);
        galleryBoard_rv.setLayoutManager(gridLayoutManager);
        galleryBoard_adapter = new GalleryBoard_adapter(requireContext());
        galleryBoard_rv.setAdapter(galleryBoard_adapter);
    }

    private void setSpinner(View view) {
        Spinner spinner = view.findViewById(R.id.galleryBoard_spinner);
        String[] items = {"?????????", "?????????", "?????????"};

        // ????????? ????????? ?????? 1.?????? 2.???????????? 3. ?????????
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, items);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                galleryBoard_tv_spinner.setText(items[position]);
                switch (position) {
                    case 1:
                        category = "gallery_theme";
                        break;
                    case 2:
                        category = "gallery_writer";
                        break;
                    default:
                        category = "gallery_cafe";
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void viewSet(View view) {
        galleryBoard_swipe = view.findViewById(R.id.galleryBoard_swipe);
        galleryBoard_reset = view.findViewById(R.id.galleryBoard_reset);
        galleryBoard_search_btn = view.findViewById(R.id.galleryBoard_search_btn);
        galleryBoard_search = view.findViewById(R.id.galleryBoard_search);
        galleryBoard_tv_spinner = view.findViewById(R.id.galleryBoard_tv_spinner);
        galleryBoard_write_btn = view.findViewById(R.id.galleryBoard_write_btn);
        galleryBoard_rv = view.findViewById(R.id.galleryBoard_rv);
        galleryBoard_scroll = view.findViewById(R.id.galleryBoard_scroll);
        galleryBoard_progress = view.findViewById(R.id.galleryBoard_progress);
        imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }
}