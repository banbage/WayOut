package com.example.wayout_ver_01.Activity.Gallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wayout_ver_01.Class.DateConverter;
import com.example.wayout_ver_01.Class.PreferenceManager;
import com.example.wayout_ver_01.R;
import com.example.wayout_ver_01.RecyclerView.Gallery.GalleryRead_comment_adapter;
import com.example.wayout_ver_01.RecyclerView.Gallery.GalleryRead_adpater;
import com.example.wayout_ver_01.Retrofit.DTO_comment;
import com.example.wayout_ver_01.Retrofit.DTO_free_reply;
import com.example.wayout_ver_01.Retrofit.DTO_gallery;
import com.example.wayout_ver_01.Retrofit.DTO_image;
import com.example.wayout_ver_01.Retrofit.RetrofitClient;
import com.example.wayout_ver_01.Retrofit.RetrofitInterface;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GalleryBoard_read extends AppCompatActivity {

    private TextView galleryRead_cafe, galleryRead_theme,galleryRead_point, galleryRead_writer, galleryRead_date, galleryRead_content, galleryRead_comment_num, galleryRead_like_num, galleryRead_comment_submit;
    private ImageView galleryRead_menu, galleryRead_like_btn;
    private EditText galleryRead_comment_et;
    private RecyclerView galleryRead_rv, galleryRead_comment_rv;
    private GalleryRead_adpater galleryRead_adpater;
    private GalleryRead_comment_adapter galleryRead_comment_adapter;
    private ProgressDialog progressDialog;
    private NestedScrollView galleryRead_scroll;
    private LinearLayoutManager layoutManager, layoutManager_comment;
    private SwipeRefreshLayout galleryRead_swipe;
    private InputMethodManager imm;
    private String cafe, theme, content;

    // ????????? ?????? ??? ???
    // ?????????, ????????? ????????? ??????, ??? ?????? ??? , ??? ????????? ???, ??? ?????? ???
    private int page = 1, limit = 5, total_comment, total_like, total_reply;
    // ????????? True ??? ????????? ????????? ?????? , ???????????? true ??? ???????????? ??????
    private boolean scroll, swipe;
    // like ????????? ????????????????????? ????????? ??????
    // type_mode : ???????????? ?????? ????????? ????????????
    private boolean is_click, type_mode;
    // ????????? ??????, ????????? ??????
    private String board_number, writer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_board_read);

        // View ?????? ?????????
        setFindView();

        // ????????? ?????????????????? ??????
        // ???????????? ????????? ??????, ?????? ???????????? ??????
        // ????????? ????????? ??????, ???????????? : Context
        layoutManager = new LinearLayoutManager(GalleryBoard_read.this, LinearLayoutManager.VERTICAL, false);
        galleryRead_rv.setLayoutManager(layoutManager);
        galleryRead_adpater = new GalleryRead_adpater(GalleryBoard_read.this);
        galleryRead_rv.setAdapter(galleryRead_adpater);

        // ?????? ?????????????????? ??????
        // ???????????? ????????? ??????, ?????? ???????????? ??????
        // ?????? ????????? ??????, ???????????? : Context
        layoutManager_comment = new LinearLayoutManager(GalleryBoard_read.this, LinearLayoutManager.VERTICAL, false);
        galleryRead_comment_rv.setLayoutManager(layoutManager_comment);
        galleryRead_comment_adapter = new GalleryRead_comment_adapter(GalleryBoard_read.this, galleryRead_comment_num);
        galleryRead_comment_rv.setAdapter(galleryRead_comment_adapter);
        galleryRead_comment_adapter.setUpdate_content(galleryRead_comment_et);
        galleryRead_comment_adapter.setImm(imm);

        // ????????? ?????? : DB ?????? ????????? ??????
        // ????????? : ??????, ?????? ?????? ??????
        Intent intent = getIntent();
        board_number = intent.getStringExtra("board_number");
        writer = intent.getStringExtra("writer");
//        Log.e("????????? ???????????? ????????????" , "board_num : "  + board_number);
//        Log.e("????????? ????????? ????????????", "writer : " + writer);

        // ????????? ?????????
        // is_click : true -> ????????? ??????, is_click : false -> ????????? ?????? ??? ??????
        galleryRead_like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_click) {
                    galleryRead_like_btn.setImageResource(R.drawable.heartwhite);
                    is_click = false;
                    click_unlike(board_number);
                } else {
                    galleryRead_like_btn.setImageResource(R.drawable.heartblack);
                    is_click = true;
                    click_like(board_number);
                }
            }
        });

        // ?????? ??????
        galleryRead_comment_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type_mode = galleryRead_comment_adapter.getMode();
                Log.e("????????? ?????? ?????? ", "type_mode : " + type_mode);
                if (type_mode) {
                    // ?????? ??????
                    update_comment(board_number);
                    galleryRead_comment_adapter.endUpdate();
                } else {
                    // ?????? ??????
                    submit_comment(board_number);
                }
            }
        });

        // ??????????????? ???????????? -> ?????? ????????????
        galleryRead_swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe = true;
                page = 1;
                type_mode = false;
                getGalleryRead(board_number);
                galleryRead_comment_et.setText("");
                galleryRead_swipe.setRefreshing(false);
                progressDialog.dismiss();
            }
        });

        // ?????? ?????????
        galleryRead_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_menu(board_number);
            }
        });

        // ????????? ?????????
        galleryRead_scroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    progressDialog.show();
                    progressDialog.setMessage("Loading...");
                    page++;
                    getScroll();
                    progressDialog.dismiss();
                }
            }
        });


    }

    // ?????? ???????????? ????????? ?????? -> ???????????? ????????? ????????? , ?????? ?????? ??????
    @Override
    protected void onStop() {
        super.onStop();

        swipe = true;
        page = 1;
        type_mode = false;
        getGalleryRead(board_number);
        progressDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ????????? ????????? ?????? ????????????
        // ????????? ???????????? ???????????? ??????, ????????? ?????? , ????????? ???????????? ?????????,
        getGalleryRead(board_number);

        // progressDialog ??????
        progressDialog.dismiss();
    }

    private void getScroll() {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<DTO_gallery> call = retrofitInterface.getGalleryScroll(page, board_number, writer);
        call.enqueue(new Callback<DTO_gallery>() {
            @Override
            public void onResponse(Call<DTO_gallery> call, Response<DTO_gallery> response) {

                // ????????? ????????? ??????????????? ????????? ????????? ??????
                if (response.isSuccessful() && response.body() != null) {
                    if(response.body().getGallery_comment().size() == 0){
                        page--;
                    }
                    Log.e("?????? ????????? ", "page : " + page);

                    for (int i = 0; i < response.body().getGallery_comment().size(); i++) {
                        galleryRead_comment_adapter.add_item(new DTO_comment(
                                response.body().getGallery_comment().get(i).getWriter(),
                                response.body().getGallery_comment().get(i).getContent(),
                                response.body().getGallery_comment().get(i).getDate(),
                                response.body().getGallery_comment().get(i).getBoard_number(),
                                response.body().getGallery_comment().get(i).getComment_index(),
                                response.body().getGallery_comment().get(i).getTotal_comment(),
                                response.body().getGallery_comment().get(i).getTotal_reply()
                        ));
                        galleryRead_comment_adapter.notifyItemInserted(i + (page - 1) * 6);
                    }


                }
            }

            @Override
            public void onFailure(Call<DTO_gallery> call, Throwable t) {

            }
        });

    }

    private void select_menu(String board_number) {
        final ArrayList<String> Items = new ArrayList<>();
        Items.add("????????? ??????");
        Items.add("????????? ??????");

        // Items ??? String[]??? ????????????.
        final String[] Strings = Items.toArray(new String[Items.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(GalleryBoard_read.this);
        builder.setItems(Strings, (dialog, pos) -> {
            String selectedText = Strings[pos];
            switch (selectedText) {
                case "????????? ??????":
                    delete_board(board_number);
                    break;
                case "????????? ??????":
                    update_board(board_number);
                    break;
            }
        });
        builder.show();
    }

    private void delete_board(String board_number) {

        // ?????????
        AlertDialog.Builder builder = new AlertDialog.Builder(GalleryBoard_read.this);
        builder.setTitle("????????? ??????");
        builder.setMessage("\n????????? ?????????????????????????\n");
        builder.setPositiveButton("???",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ?????? ????????????
                        writer = PreferenceManager.getString(getApplicationContext(), "autoNick");
                        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create((RetrofitInterface.class));
                        Call<DTO_gallery> call = retrofitInterface.deleteGallery(board_number, writer);
                        call.enqueue(new Callback<DTO_gallery>() {
                            @Override
                            public void onResponse(Call<DTO_gallery> call, Response<DTO_gallery> response) {
                                if (response.body().isSuccess() && response.body() != null) {
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<DTO_gallery> call, Throwable t) {
                                Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                                Log.e("????????? ?????? ?????? ?????? ", "?????? : " + t);
                            }
                        });
                    }
                });
        // =====
        builder.setNegativeButton("?????????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void update_board(String board_number) {
        // ????????? Uri ??? Intent ??? ????????? ?????????
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < galleryRead_adpater.getItemCount(); i++) {
            arrayList.add(galleryRead_adpater.getUri(i));
            Log.e("?????? ...", "" + galleryRead_adpater.getUri(i));
        }
        Intent intent = new Intent(GalleryBoard_read.this, GalleryBoard_write.class);
        intent.putExtra("mode", true);
        intent.putExtra("items", arrayList);
        intent.putExtra("board_number", board_number);
        intent.putExtra("cafe", cafe);
        intent.putExtra("theme", theme);
        intent.putExtra("content", galleryRead_content.getText().toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

    }

    private void update_comment(String board_number) {

        progressDialog = new ProgressDialog(GalleryBoard_read.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        // ???????????? ??????
        String setWriter = PreferenceManager.getString(getApplicationContext(), "autoNick");
        String setContent = galleryRead_comment_et.getText().toString();
        String setIndex = galleryRead_comment_adapter.getIndex();

        // ????????? ??????
        if (galleryRead_comment_et.getText().toString().isEmpty()) {
            galleryRead_comment_et.setError("????????? ??????????????????");
            galleryRead_comment_et.requestFocus();
            progressDialog.dismiss();
            return;
        }

        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<DTO_comment> call = retrofitInterface.updateGalleryComment(setWriter, setContent, setIndex);
        call.enqueue(new Callback<DTO_comment>() {
            @Override
            public void onResponse(Call<DTO_comment> call, Response<DTO_comment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    galleryRead_comment_adapter.updateItem(galleryRead_comment_adapter.getPos(), setContent);

                }
            }

            @Override
            public void onFailure(Call<DTO_comment> call, Throwable t) {
                Log.e("?????? ?????? ?????????", "?????? : ?????? ?????? ?????? : " + t);
            }
        });

        // ?????? ?????????
        galleryRead_comment_et.setText("");
        galleryRead_comment_adapter.endUpdate();
        progressDialog.dismiss();
        imm.hideSoftInputFromWindow(galleryRead_comment_et.getWindowToken(), 0);

    }

    private void submit_comment(String board_number) {
        // ????????? ??????
        if (galleryRead_comment_et.getText().toString().isEmpty()) {
            galleryRead_comment_et.setError("????????? ??????????????????");
            galleryRead_comment_et.requestFocus();
            progressDialog.dismiss();
            return;
        }

        // ?????????, ??????, ????????????
        String writer = PreferenceManager.getString(getApplicationContext(), "autoNick");
        String content = galleryRead_comment_et.getText().toString();

        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<DTO_comment> call = retrofitInterface.writerGalleryComment(writer, content, board_number);
        call.enqueue(new Callback<DTO_comment>() {
            @Override
            public void onResponse(Call<DTO_comment> call, Response<DTO_comment> response) {
                if (response.isSuccessful() && response.body() != null) {

                    galleryRead_comment_adapter.total_comment(total_comment);
                    // ????????? ??????
                    galleryRead_comment_num.setText("?????? " + response.body().getTotal_comment());
                    // ?????????, ?????? , ?????????, ?????? ??? ??????, ?????? ?????? ??????
                    // ?????????????????? ???????????? ?????? , ????????? ?????? ???????????? ????????? ???????????? ????????? ????????????????????? ????????????.
                    if(galleryRead_comment_adapter.getItemCount() % 6 != 0 || galleryRead_comment_adapter.getItemCount() == 0) {
                        galleryRead_comment_adapter.add_item(new DTO_comment(
                                writer,
                                content,
                                response.body().getDate(),
                                board_number,
                                response.body().getComment_index()
                        ));
//                        galleryRead_scroll.fullScroll(View.FOCUS_DOWN);
                    }
                    galleryRead_comment_adapter.notifyItemInserted(galleryRead_comment_adapter.getItemCount());



                    // ?????? ?????????
                    galleryRead_comment_et.setText("");
                    // ????????? ????????? ?????????
                    imm.hideSoftInputFromWindow(galleryRead_comment_et.getWindowToken(), 0);
                }
            }

            @Override
            public void onFailure(Call<DTO_comment> call, Throwable t) {

            }
        });


    }

    private void click_like(String board_number) {
        String user_id = PreferenceManager.getString(getApplicationContext(), "autoNick");
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<DTO_gallery> call = retrofitInterface.writeGalleryLike(user_id, board_number);
        call.enqueue(new Callback<DTO_gallery>() {
            @Override
            public void onResponse(Call<DTO_gallery> call, Response<DTO_gallery> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int total = response.body().getTotal_like();
                    galleryRead_like_num.setText("????????? " + total);
                }
            }

            @Override
            public void onFailure(Call<DTO_gallery> call, Throwable t) {

            }
        });
    }

    private void click_unlike(String board_number) {
        String user_id = PreferenceManager.getString(getApplicationContext(), "autoNick");
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<DTO_gallery> call = retrofitInterface.deleteGalleryLike(user_id, board_number);
        call.enqueue(new Callback<DTO_gallery>() {
            @Override
            public void onResponse(Call<DTO_gallery> call, Response<DTO_gallery> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int total = response.body().getTotal_like();
                    galleryRead_like_num.setText("????????? " + total);
                }
            }

            @Override
            public void onFailure(Call<DTO_gallery> call, Throwable t) {

            }
        });
    }


    // View ?????? ???????????????.
    private void setFindView() {
        galleryRead_cafe = findViewById(R.id.galleryRead_cafe);
        galleryRead_theme = findViewById(R.id.galleryRead_theme);
        galleryRead_writer = findViewById(R.id.galleryRead_writer);
        galleryRead_date = findViewById(R.id.galleryRead_date);
        galleryRead_content = findViewById(R.id.galleryRead_content);
        galleryRead_comment_num = findViewById(R.id.galleryRead_comment_num);
        galleryRead_like_num = findViewById(R.id.galleryRead_like_num);
        galleryRead_comment_submit = findViewById(R.id.galleryRead_comment_submit);
        galleryRead_menu = findViewById(R.id.galleryRead_menu);
        galleryRead_like_btn = findViewById(R.id.galleryRead_like_btn);
        galleryRead_comment_et = findViewById(R.id.galleryRead_comment_et);
        galleryRead_rv = findViewById(R.id.galleryRead_image_rv);
        galleryRead_comment_rv = findViewById(R.id.galleryRead_comment_rv);
        galleryRead_swipe = findViewById(R.id.galleryRead_swipe);
        galleryRead_scroll = findViewById(R.id.galleryRead_scroll);


        // ????????? ??????
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

    }

    private void getGalleryRead(String board_number) {
        // ????????? On
        progressDialog = new ProgressDialog(GalleryBoard_read.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        // ???????????? ??????????????? ????????? ????????????????????? ?????? ???????????? ????????? ?????? ????????????.
        if (swipe) {
            galleryRead_adpater.clearItems();
            galleryRead_adpater.notifyDataSetChanged();
            galleryRead_comment_adapter.clearItems();
            galleryRead_comment_adapter.notifyDataSetChanged();
            swipe = false;
        }

        String user_id = PreferenceManager.getString(getApplicationContext(), "autoNick");
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<DTO_gallery> call = retrofitInterface.getGalleryRead(page, user_id, board_number);
        call.enqueue(new Callback<DTO_gallery>() {
            @Override
            public void onResponse(Call<DTO_gallery> call, Response<DTO_gallery> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String userName = PreferenceManager.getString(getApplicationContext(), "autoNick");
                    String writer = response.body().getWriter();
                    total_comment = response.body().getTotal_comment();
                    total_like = response.body().getTotal_like();

                    // ???????????? ?????? ????????? ????????? ??????, ?????? ????????? ????????????
                    if (!(userName.equals(writer))) {
                        galleryRead_menu.setVisibility(View.INVISIBLE);
                    }

                    // ????????? ??????
                    galleryRead_writer.setText("????????? :  " + response.body().getWriter());
                    galleryRead_cafe.setText("????????? : " + response.body().getCafe());
                    galleryRead_theme.setText("????????? : " + response.body().getTheme());
                    galleryRead_content.setText(response.body().getContent());

                    cafe = response.body().getCafe();
                    theme = response.body().getTheme();
                    content = response.body().getContent();

                    // ?????? ??????
                    try {
                        galleryRead_date.setText(DateConverter.resultDateToString(response.body().getDate(), "yyyy-MM-dd a h:mm"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    // ????????? ??????
                    if (response.body().isClick()) {
                        galleryRead_like_btn.setImageResource(R.drawable.heartblack);
                        is_click = true;
                    } else {
                        galleryRead_like_btn.setImageResource(R.drawable.heartwhite);
                        is_click = false;
                    }
                    // ????????? ????????? ?????? ??????
                    galleryRead_like_num.setText("????????? " + total_like);
                    galleryRead_comment_num.setText("?????? " + total_comment);
                    galleryRead_comment_adapter.total_comment(total_comment);

                    // ????????? ?????????????????? ??????
                    if (galleryRead_adpater.getItemCount() == 0) {
                        for (int i = 0; i < response.body().getImages_uri().size(); i++) {
                            String image_uri = response.body().getImages_uri().get(i).getImage_uri();
                            galleryRead_adpater.addItem(new DTO_gallery(image_uri));
                            galleryRead_adpater.notifyItemInserted(i);
                        }
                    }

                    // ?????? ?????????????????? ??????
                    // ?????????, ??????, ????????????, ???????????????, ?????? ??????, ??? ????????? , ??? ?????? ???

                    if (response.body().getGallery_comment() != null) {
                        ArrayList<DTO_comment> items = new ArrayList<>();
                        items = response.body().getGallery_comment();

//                      // ?????? ?????? ??????????????? ????????? ??????
                        if (galleryRead_comment_adapter.getItemCount() == 0) {
                            for (int i = 0; i < response.body().getGallery_comment().size(); i++) {
                                galleryRead_comment_adapter.add_item(new DTO_comment(
                                        response.body().getGallery_comment().get(i).getWriter(),
                                        response.body().getGallery_comment().get(i).getContent(),
                                        response.body().getGallery_comment().get(i).getDate(),
                                        response.body().getGallery_comment().get(i).getBoard_number(),
                                        response.body().getGallery_comment().get(i).getComment_index(),
                                        response.body().getGallery_comment().get(i).getTotal_comment(),
                                        response.body().getGallery_comment().get(i).getTotal_reply()
                                ));
                                galleryRead_comment_adapter.notifyItemInserted(i);
                            }

//                            Log.e("?????? ?????????????????? ??????", "?????? ?????? ????????? ??????: " + items.get(0).getWriter());
//                            Log.e("?????? ?????????????????? ??????", "?????? : " + response.body().getGallery_comment().get(i).getContent());
//                            Log.e("?????? ?????????????????? ??????", "?????? : " + response.body().getGallery_comment().get(i).getDate());
//                            Log.e("?????? ?????????????????? ??????", "?????? : " + response.body().getGallery_comment().get(i).getBoard_number());
//                            Log.e("?????? ?????????????????? ??????", "?????? : " + response.body().getGallery_comment().get(i).getComment_index());
//                            Log.e("?????? ?????????????????? ??????", "?????? : " + response.body().getGallery_comment().get(i).getTotal_reply());
//                            Log.e("?????? ?????????????????? ??????", "?????? : " + response.body().getGallery_comment().get(i).getTotal_comment());
                        }else{
                            // ?????? ???????????? ????????? ????????? ???????????????
                            for (int i = 0; i < response.body().getGallery_comment().size(); i++) {
                                galleryRead_comment_adapter.change_item(i, new DTO_comment(
                                        response.body().getGallery_comment().get(i).getWriter(),
                                        response.body().getGallery_comment().get(i).getContent(),
                                        response.body().getGallery_comment().get(i).getDate(),
                                        response.body().getGallery_comment().get(i).getBoard_number(),
                                        response.body().getGallery_comment().get(i).getComment_index(),
                                        response.body().getGallery_comment().get(i).getTotal_comment(),
                                        response.body().getGallery_comment().get(i).getTotal_reply()
                                ));
                            }
                        }
                    }


                } else {
                    Log.e("Gallery_Read ???????????? ?????? ", "????????? ???????????? ?????????, ?????? ??????");
                }
            }

            @Override
            public void onFailure(Call<DTO_gallery> call, Throwable t) {
                Log.e("?????????Read ????????? ????????????", "?????? ????????? : " + t);
            }
        });

//        Log.e("???????????? ?????? ?????????", "page : " + page );
//        Log.e("???????????? ?????? ?????????", "user_id : " + user_id );
//        Log.e("???????????? ?????? ?????????", "board_number : " + board_number );
    }
}