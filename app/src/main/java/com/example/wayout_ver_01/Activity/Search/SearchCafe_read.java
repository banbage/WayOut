package com.example.wayout_ver_01.Activity.Search;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wayout_ver_01.Class.PreferenceManager;
import com.example.wayout_ver_01.R;
import com.example.wayout_ver_01.RecyclerView.Theme.SearchCafe_review_adapter;
import com.example.wayout_ver_01.RecyclerView.Theme.SearchTheme_adpater;
import com.example.wayout_ver_01.Retrofit.DTO_address;
import com.example.wayout_ver_01.Retrofit.DTO_review;
import com.example.wayout_ver_01.Retrofit.DTO_shop;
import com.example.wayout_ver_01.Retrofit.DTO_theme;
import com.example.wayout_ver_01.Retrofit.RetrofitClient;
import com.example.wayout_ver_01.Retrofit.RetrofitInterface;
import com.example.wayout_ver_01.databinding.ActivitySearchCafeReadBinding;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;
import com.willy.ratingbar.ScaleRatingBar;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchCafe_read extends AppCompatActivity implements OnMapReadyCallback {
    private ActivitySearchCafeReadBinding bind;
    private ActivityResultLauncher<Intent> launcher;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    // ????????? ????????? ????????????
    private String index;
    private double x = -1, y = -1, myX, myY;
    private ProgressDialog progressDialog;
    private SearchTheme_adpater cafeRead_theme_adapter;
    private SearchCafe_review_adapter cafeRead_review_adapter;
    // ?????????, ????????? (??????, ??????)
    private String start, goal;
    private double distance;
    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;
    private Dialog dialog, menu_dialog;
    private ScaleRatingBar scaleRatingBar;
    private float dialog_rating, review_rating;
    private int page = 1, size = 8;
    /* true = 0, false = 1 */
    private int isChecked = 1;
    private int theme_number;

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("cafe_Read,89", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        theme_number = cafeRead_theme_adapter.getItemCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("cafe_Read,91", "onResume");
        setTheme();

    }

    private void setTheme() {
        String user_id = PreferenceManager.getString(getBaseContext(), "userIndex");
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<DTO_shop> call = retrofitInterface.getCafeRead(index, page, size, user_id);
        call.enqueue(new Callback<DTO_shop>() {
            @Override
            public void onResponse(Call<DTO_shop> call, Response<DTO_shop> response) {
                if(response.isSuccessful() && response.body() != null) {
                    if(response.body().getThemes().size() != theme_number) {
                        /* ?????? ?????? */
                        cafeRead_theme_adapter.clearItem();
                        for (int i = 0; i < response.body().getThemes().size(); i++) {
                            cafeRead_theme_adapter.addItem(new DTO_theme(
                                    response.body().getThemes().get(i).getIndex(),
                                    response.body().getThemes().get(i).getName(),
                                    response.body().getThemes().get(i).getDiff(),
                                    response.body().getThemes().get(i).getLimit(),
                                    response.body().getThemes().get(i).getGenre(),
                                    response.body().getThemes().get(i).getCafe(),
                                    response.body().getThemes().get(i).getImage(),
                                    response.body().getThemes().get(i).getRate()
                            ));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<DTO_shop> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivitySearchCafeReadBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        // ????????? ????????? ??????
        Intent intent = getIntent();
        index = intent.getStringExtra("?????????");
        Log.e("CafeRead, 85", "get Index : " + index);

        /* ????????? ?????? ??? ????????? ????????? ???????????? ????????? ????????? latLng ?????? */
        // ??? ?????? ?????? ??????
        // ???????????? ?????? ????????? ?????? ??????
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //???????????? ?????? ??????
        Location userLocation = getMyLocation();
        if (userLocation != null) {
            double latitude = userLocation.getLatitude();
            double longitude = userLocation.getLongitude();
            start = longitude + ", " + latitude;
        }

        /* Activity Result Launcher Callback Setting~!~~!~!~!~!~!~! */
        /* type : 1, ?????? ?????? ?????? , type : 2, ?????? ?????? */
        setLauncher();

        /* ????????? ?????? ???????????? ?????? ?????? ???????????? */
        // ?????? ????????? ??????
        setAdapter();

        // ?????? ????????? ??????
        setData();

        /* ?????? ??????  */
        bind.CafeReadWriteReview.setOnClickListener((v -> {
            // ??????????????? ??????
            dialog = new Dialog(SearchCafe_read.this); // ?????? ?????????
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // ????????? ??????
            dialog.setContentView(R.layout.item_dialog); // ??????????????? Dialog
            DialogShow();
        }));
        /* ?????? ?????? */
        /* 0. ?????? ??????, 1. ?????? ??????, 2. ?????? ?????? */
        bind.CafeReadMenu.setOnClickListener((v -> {
            menu_dialog = new Dialog(SearchCafe_read.this);
            menu_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            menu_dialog.setContentView(R.layout.item_dialog_menu_cafe);
            MenuDialogShow();
        }));
        /* ?????? ????????? */
        bind.CafeReadLike.setOnClickListener((v -> {
            // ????????? -> ????????? -> ???????????? , ??????
            // ????????? ( ?????? ?????????, ?????? ??????, ?????? isChecked)
            // true = 0, false = 1;
            if (isChecked == 0) {
                bind.CafeReadLike.setImageResource(R.drawable.heartwhite);
                setLike();
            } else {
                // ?????? ?????? -> ???????????? ???????????????, ?????? ??????
                bind.CafeReadLike.setImageResource(R.drawable.heartblack);
                setLike();
            }
        }));
        /* ????????? ????????? */
        bind.CafeReadScroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    getScroll();
                    Log.e("cafe_read, 135", "????????? OK");
                }
            }
        });
        /*=========================================================================================*/
    }

    private void setLauncher() {
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == Activity.RESULT_OK) {
                            Intent i = result.getData();
                            /* 1??? => ?????? ??????, 2??? => ?????? ?????? */
                            String callback = i.getStringExtra("callback");
                            switch (callback) {
                                case "add_theme":
                                case "cafe_update":
                                    setData();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
        );
    }

    private void MenuDialogShow() {
        menu_dialog.show();

        /* ??? ?????? ?????? ????????? ???????????? ????????? ???????????? ??????. */
        // * ????????? ???: findViewById()??? ??? ?????? -> ?????? ????????? ??????????????? ????????? ????????? ??????.
        TextView menu_add_theme, menu_update, menu_delete;
        menu_add_theme = menu_dialog.findViewById(R.id.item_dialog_add_theme);
        menu_update = menu_dialog.findViewById(R.id.item_dialog_update_cafe);
        menu_delete = menu_dialog.findViewById(R.id.item_dialog_delete_cafe);
        /* ???????????? : call -> ?????? ?????????????????? ???????????????
        *           callback -> ?????? ?????????????????? ????????? ???????????? */
        /* ?????? ?????? -> ?????? ?????? Activity ??? ?????? */
        menu_add_theme.setOnClickListener(v -> {
            Intent intent = new Intent(SearchCafe_read.this, SearchCafe_add_theme.class);
            intent.putExtra("cafe_index",index);
            intent.putExtra("cafe_name",bind.CafeReadName.getText().toString());
            intent.putExtra("call","cafe_read");
            launcher.launch(intent);
            menu_dialog.dismiss();
        });
        /* ???????????? : call -> ?????? ?????????????????? ???????????????
         *           callback -> ?????? ?????????????????? ????????? ???????????? */
        /* ?????? ?????? -> ?????? ?????? Activity ??? ?????? */
        menu_update.setOnClickListener(v -> {
            Intent intent = new Intent(SearchCafe_read.this, SearchCafe_update.class);
            intent.putExtra("cafe_index",index);
            intent.putExtra("call","cafe_read");
            launcher.launch(intent);
            menu_dialog.dismiss();
        });
        /* ?????? ?????? -> ?????? ?????????????????? ??? ?????? ??? ?????? */
        menu_delete.setOnClickListener(v -> {
            Dialog dialog = new Dialog(SearchCafe_read.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.item_dialog_check);
            showDeleteDialog(dialog);
            menu_dialog.dismiss();
        });
    }

    private void showDeleteDialog(Dialog dialog) {
        dialog.show();

        /* ?????? ??? ?????? -> ??????, ?????? ?????? */
        /* ????????????! ????????? ?????? ?????? ??????????????? */

        TextView dialog_title, dialog_content, dialog_yes, dialog_no;
        dialog_title = dialog.findViewById(R.id.item_dialog_yn_title);
        dialog_content = dialog.findViewById(R.id.item_dialog_yn_content);
        dialog_yes = dialog.findViewById(R.id.item_dialog_yn_yes);
        dialog_no = dialog.findViewById(R.id.item_dialog_yn_no);
        dialog_title.setText("?????? ??????");
        dialog_content.setText("????????? ?????????????????????????");
        /* ?????? ???????????? ?????? */
        dialog_yes.setOnClickListener(v -> {
            RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
            Call<DTO_shop> call = retrofitInterface.deleteCafe(index);
            call.enqueue(new Callback<DTO_shop>() {
                @Override
                public void onResponse(Call<DTO_shop> call, Response<DTO_shop> response) {
                    if(response.isSuccessful() && response.body() != null){
                    }
                }

                @Override
                public void onFailure(Call<DTO_shop> call, Throwable t) {
                    Log.e("cafe_read, 280", "?????? ?????? ?????? : " + t);
                }
            });

            dialog.dismiss();
            finish();
        });

        dialog_no.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    private void setLike() {
        String user_index = PreferenceManager.getString(SearchCafe_read.this, "userIndex");
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<DTO_shop> call = retrofitInterface.setLike(index, user_index, isChecked);
        call.enqueue(new Callback<DTO_shop>() {
            @Override
            public void onResponse(Call<DTO_shop> call, Response<DTO_shop> response) {
                if (response.body() != null && response.isSuccessful()) {
                    isChecked = response.body().getIsChecked();
                }
            }

            @Override
            public void onFailure(Call<DTO_shop> call, Throwable t) {
                Log.e("cafeRead, 183", "set_like ?????? : " + t);
            }
        });
    }

    private void getScroll() {
        if (page == 1) {
            page = 2;
        }
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<ArrayList<DTO_review>> call = retrofitInterface.getCafeReadReviewScroll(index, page, size);
        call.enqueue(new Callback<ArrayList<DTO_review>>() {
            @Override
            public void onResponse(Call<ArrayList<DTO_review>> call, Response<ArrayList<DTO_review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().size() > 0) {
                        page++;
                    }

                    /* ?????? ?????????????????? ?????? ??????  */
                    for (int i = 0; i < response.body().size(); i++) {
                        cafeRead_review_adapter.scrollItem(new DTO_review(
                                response.body().get(i).getWriter(),
                                response.body().get(i).getContent(),
                                response.body().get(i).getRate(),
                                response.body().get(i).getIndex(),
                                response.body().get(i).getDate()
                        ));
                        cafeRead_review_adapter.notifyItemInserted((page - 1) * size + i);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<DTO_review>> call, Throwable t) {
                Log.e("cafe_read, 158", "????????? ?????? ?????? : " + t);
            }
        });
    }

    private void setAdapter() {
        /* ?????? ?????? ????????? */
        LinearLayoutManager layoutManager = new LinearLayoutManager(SearchCafe_read.this, RecyclerView.HORIZONTAL, false);
        bind.CafeReadThemeRv.setLayoutManager(layoutManager);
        cafeRead_theme_adapter = new SearchTheme_adpater(SearchCafe_read.this);
        bind.CafeReadThemeRv.setAdapter(cafeRead_theme_adapter);

        /* ?????? ?????? ????????? */
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(SearchCafe_read.this, RecyclerView.VERTICAL, false);
        bind.CafeReadReviewRv.setLayoutManager(layoutManager2);
        cafeRead_review_adapter = new SearchCafe_review_adapter(SearchCafe_read.this, bind.CafeReadGrade, bind.CafeReadGrade2, bind.CafeReadScore, index);
        bind.CafeReadReviewRv.setAdapter(cafeRead_review_adapter);

    }

    private void DialogShow() {
        dialog.show();

        /* ??? ?????? ?????? ????????? ???????????? ????????? ???????????? ??????. */

        // ?????? ?????? ????????? ?????? ????????????
        // '?????? ????????? ??????' ?????? ???????????? ???????????? ???????????? ???????????? ????????????,
        // '?????? ??? ??????' ?????? ?????? ???????????? ??????????????? ???????????? ??????.
        // * ????????? ???: findViewById()??? ??? ?????? -> ?????? ????????? ??????????????? ????????? ????????? ??????.

        scaleRatingBar = dialog.findViewById(R.id.item_dialog_grade);
        EditText dialog_content = dialog.findViewById(R.id.item_dialog_content);

        //  ?????????
        TextView noBtn = dialog.findViewById(R.id.item_dialog_no);
        noBtn.setOnClickListener((v -> {
            dialog.dismiss();
//            Log.e("????????? ??????", "OK");
        }));
        // ???
        TextView yesBtn = dialog.findViewById(R.id.item_dialog_yes);
        yesBtn.setOnClickListener((v -> {
            // ????????? ?????? ????????????
            float rate = scaleRatingBar.getRating();
            String content = dialog_content.getText().toString();
            progressDialog = new ProgressDialog(SearchCafe_read.this);
            progressDialog.setMessage("Loading...");
            progressDialog.show();

            // ?????? ?????? ????????? ?????? ?????? -> ?????????????????? ????????? ??????
            if (content.isEmpty()) {
                Toast.makeText(SearchCafe_read.this, "?????? ????????? ??????????????????", Toast.LENGTH_SHORT).show();
                return;
            }

            /* ???????????? ????????? ?????? ??? ???????????? ?????? ????????? ???????????? ?????? ?????? */
            // ?????? ???????????? ( ?????? ?????? , ?????? ????????????)
            writeReview(content, rate);

            progressDialog.dismiss();
            // Dialog ??????
            dialog.dismiss();
        }));
    }

    private void writeReview(String content, float rate) {
        /* ????????? ??? ????????? ????????? ???????????? ?????? ????????? -> */
        page = 1;

        String writer = PreferenceManager.getString(getBaseContext(), "autoId");
        // ????????? ?????? ?????? adapter ??? ?????????
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<ArrayList<DTO_review>> call = retrofitInterface.writeCafeReview(index, writer, content, rate, page, size);
        call.enqueue(new Callback<ArrayList<DTO_review>>() {
            @Override
            public void onResponse(Call<ArrayList<DTO_review>> call, Response<ArrayList<DTO_review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    /* ?????? ?????? ?????? */
                    bind.CafeReadGrade.setRating(response.body().get(0).getTotal());
                    bind.CafeReadGrade2.setRating(response.body().get(0).getTotal());
                    bind.CafeReadScore.setText("??????  " + response.body().get(0).getTotal());

                    /* ?????? ?????????????????? ?????? ??????  */
                    cafeRead_review_adapter.clearItems();
                    for (int i = 0; i < response.body().size(); i++) {
                        cafeRead_review_adapter.addItem(new DTO_review(
                                response.body().get(i).getWriter(),
                                response.body().get(i).getContent(),
                                response.body().get(i).getRate(),
                                response.body().get(i).getIndex(),
                                response.body().get(i).getDate()
                        ));


                    }

                }
            }

            @Override
            public void onFailure(Call<ArrayList<DTO_review>> call, Throwable t) {
                Log.e("Cafe_Read, 205", "?????? ?????? ?????? : " + t);
            }
        });


    }


    /* ????????? ????????? ????????????. */
    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("////////////??????????????? ????????? ???????????????");
            ActivityCompat.requestPermissions((Activity) getBaseContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
            getMyLocation(); //?????? ???????????? ????????? ?????????, ??? ?????? ???????????? ?????? ????????? ??????????????? ????????????!
        } else {
            System.out.println("////////////???????????? ????????????");
            // ???????????? ?????? ?????????
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                double lat = currentLocation.getLatitude();
                double lng = currentLocation.getLongitude();
                Log.e("CafeRead,127", "?????? : " + lat);
                Log.e("CafeRead,126", "?????? : " + lng);
                // ????????? ??????
                start = lat + ", " + lng;
            }
        }
        return currentLocation;
    }

    private void setData() {
        // ????????? ??????
        progressDialog = new ProgressDialog(SearchCafe_read.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        /* 2 ??? ????????? ?????? */
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 3?????? ????????? ??????????????? ??????
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 2000);
            }
        });
        thread.start();

        /* ?????? ?????????, ?????????, ????????? , ?????? ????????? */
        String user_id = PreferenceManager.getString(getBaseContext(), "userIndex");
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<DTO_shop> call = retrofitInterface.getCafeRead(index, page, size, user_id);
        call.enqueue(new Callback<DTO_shop>() {
            @Override
            public void onResponse(Call<DTO_shop> call, Response<DTO_shop> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ????????? ??????
                    isChecked = response.body().getIsChecked();
                    // ????????? -> ?????????
                    // ????????? ( ?????? ?????????, ?????? ??????, ?????? isChecked)
                    // true = 0, false = 1;
                    if (isChecked == 0) {
                        bind.CafeReadLike.setImageResource(R.drawable.heartblack);
                    } else {
                        // ?????? ?????? -> ??????
                        bind.CafeReadLike.setImageResource(R.drawable.heartwhite);
                    }
                    String user_index = PreferenceManager.getString(SearchCafe_read.this, "userIndex");
                    /* ???????????? ?????? ?????? */
                    if (response.body().getWriter().equals(user_index)) {
                        bind.CafeReadMenu.setVisibility(View.VISIBLE);
                    }
                    // ????????? ?????? , ?????? ??????,
                    bind.CafeReadToolbar.setText(response.body().getName());
                    bind.CafeReadName.setText(response.body().getName());
                    bind.CafeReadOpen.setText(response.body().getOpen());
                    bind.CafeReadClose.setText(response.body().getClose());
                    bind.CafeReadHoliday.setText(response.body().getHoliday());
                    bind.CafeReadAddress.setText(response.body().getAddress() + " " + response.body().getMore_address());
                    bind.CafeReadInfo.setText(response.body().getInfo());
                    Glide.with(SearchCafe_read.this)
                            .load(response.body().getImage())
                            .error(R.drawable.basic)
                            .fitCenter()
                            .into(bind.CafeReadImage);

                    /* ?????? ?????? */
                    cafeRead_theme_adapter.clearItem();
                    for (int i = 0; i < response.body().getThemes().size(); i++) {
                        cafeRead_theme_adapter.addItem(new DTO_theme(
                                response.body().getThemes().get(i).getIndex(),
                                response.body().getThemes().get(i).getName(),
                                response.body().getThemes().get(i).getDiff(),
                                response.body().getThemes().get(i).getLimit(),
                                response.body().getThemes().get(i).getGenre(),
                                response.body().getThemes().get(i).getCafe(),
                                response.body().getThemes().get(i).getImage(),
                                response.body().getThemes().get(i).getRate()
                        ));
                    }

                    /* ?????? ?????? */
                    if (response.body().getReviews().size() > 0) {
                        bind.CafeReadGrade.setRating(response.body().getReviews().get(0).getTotal());
                        bind.CafeReadGrade2.setRating(response.body().getReviews().get(0).getTotal());
                        bind.CafeReadScore.setText("??????  " + response.body().getReviews().get(0).getTotal());


                        /* ?????? ?????? */
                        cafeRead_review_adapter.clearItems();
                        for (int j = 0; j < response.body().getReviews().size(); j++) {
                            cafeRead_review_adapter.addItem(new DTO_review(
                                    response.body().getReviews().get(j).getWriter(),
                                    response.body().getReviews().get(j).getContent(),
                                    response.body().getReviews().get(j).getRate(),
                                    response.body().getReviews().get(j).getIndex(),
                                    response.body().getReviews().get(j).getDate()
                            ));
                        }
                    }

                    Log.e("Cafe,Read,177", response.body().getThemes().get(0).getDiff());

                    // ????????? ?????? ?????? GeoCoding ?????? -> ?????? ?????? -> ??????, ????????? ??????
                    getAddress(response.body().getAddress());

                    Log.e("CafeRead, 94", "????????? ???????????? : OK");
                }
            }

            @Override
            public void onFailure(Call<DTO_shop> call, Throwable t) {

            }
        });
    }

    private void getAddress(String address) {
        RetrofitInterface retrofitInterface = RetrofitClient.getNaverApiClient().create(RetrofitInterface.class);
        Call<DTO_address> call = retrofitInterface.searchAddress(address, start);
        call.enqueue(new Callback<DTO_address>() {
            @Override
            public void onResponse(Call<DTO_address> call, Response<DTO_address> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // x = log(??????),  y = lat(??????)
                    x = response.body().getAddresses().get(0).getX();
                    y = response.body().getAddresses().get(0).getY();
                    distance = response.body().getAddresses().get(0).getDistance();

                    if (distance >= 1000) {
                        int km = (int) (distance / 1000);
                        bind.cafeReadDistance.setText(km + " KM");
                    } else {
                        int m = (int) distance;
                        bind.cafeReadDistance.setText(m + " M");
                    }

                    goal = x + ", " + y;
                    // ??? ?????? ??????
                    Map();

//                    Log.e("??????, ?????? ????????????", "status : " + response.body().getStatus());
//                    Log.e("??????, ?????? ????????????", "log : " + x);
//                    Log.e("??????, ?????? ????????????", "lat : " + y);
//                    Log.e("?????????", "goal : " + goal);
                }
            }

            @Override
            public void onFailure(Call<DTO_address> call, Throwable t) {
                Log.e("CafeRead, 332", "??????,?????? geocoding ?????? : " + t);
            }
        });

//        Log.e("??????, ?????? ????????????", "x : " + x + " , y : " + y);
    }

    private void Map() {
        // ?????? ?????? ??????
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.Cafe_read_map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.Cafe_read_map, mapFragment).commit();
        }

        // getMapAsync ??? ???????????? ???????????? onMapReady ?????? ????????? ??????
        // onMapReady ?????? NaverMap ????????? ????????? -> onMapReady ??????
        mapFragment.getMapAsync(this);

        // ????????? ???????????? ???????????? FusedLocationSource ??????
        locationSource =
                new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        // NaverMap ?????? ????????? NaverMap ????????? ?????? ?????? ?????????
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);

        // ?????? ??????
        Marker marker = new Marker();
        marker.setPosition(new LatLng(y, x));
        marker.setIcon(MarkerIcons.BLACK);
        marker.setIconTintColor(Color.RED);
        marker.setWidth(60);
        marker.setHeight(70);
        marker.setMap(naverMap);

        // ????????? ?????? ( ?????? ??????, ??? ??????) -> ????????? ???????????? ?????? ????????????
        LatLng mLatLng = new LatLng(y, x);
        CameraPosition cameraPosition = new CameraPosition(mLatLng, 14);
        naverMap.setCameraPosition(cameraPosition);

        // ????????? ??????
        naverMap.setMaxZoom(18.0);
        naverMap.setMinZoom(14.0);
        // ??? ?????? ????????????
//        naverMap.addOnLocationChangeListener(location1 -> {
//            double myX = location1.getLatitude();
//            double myY = location1.getLongitude();
//            start = myX + ", " + myY;
////            Log.e("?????????", "start : " + start);
//
//            if(!distance){
//                distance = true;
//                RetrofitInterface retrofitInterface = RetrofitClient.getNaverApiClient().create(RetrofitInterface.class);
//                Call<DTO_Direction> call = retrofitInterface.searchDirection(start,goal);
//                call.enqueue(new Callback<DTO_Direction>() {
//                    @Override
//                    public void onResponse(Call<DTO_Direction> call, Response<DTO_Direction> response) {
//
//                    }
//
//                    @Override
//                    public void onFailure(Call<DTO_Direction> call, Throwable t) {
//
//                    }
//                });
//            }
//
////            Location location = locationSource.getLastLocation();
////            if(location != null) {
////                double myX = location.getLatitude();
////                double myY = location.getLongitude();
////                start = myX + ", " + myY;
////                Log.e("?????????", "start : " + start);
////            }
//        });


        // ????????????. ????????? onRequestPermissionsResult ?????? ????????? ??????
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

        // ??????????????? ??????
        progressDialog.dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        // request code??? ???????????? ?????? ??????
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }

    }
}