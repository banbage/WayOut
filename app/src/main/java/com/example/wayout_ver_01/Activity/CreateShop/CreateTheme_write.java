package com.example.wayout_ver_01.Activity.CreateShop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.wayout_ver_01.Activity.FreeBoard.FreeBoard_write;
import com.example.wayout_ver_01.Activity.Home;
import com.example.wayout_ver_01.Class.DateConverter;
import com.example.wayout_ver_01.Class.PreferenceManager;
import com.example.wayout_ver_01.R;
import com.example.wayout_ver_01.RecyclerView.FreeBoard.ItemTouchHelperCallback;
import com.example.wayout_ver_01.RecyclerView.Gallery.GalleryWrite_adapter;
import com.example.wayout_ver_01.RecyclerView.Theme.Theme_adapter;
import com.example.wayout_ver_01.Retrofit.DTO_gallery;
import com.example.wayout_ver_01.Retrofit.DTO_shop;
import com.example.wayout_ver_01.Retrofit.DTO_theme;
import com.example.wayout_ver_01.Retrofit.RetrofitClient;
import com.example.wayout_ver_01.Retrofit.RetrofitInterface;
import com.example.wayout_ver_01.databinding.ActivityCreateThemeWriteBinding;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import lib.kingja.switchbutton.SwitchMultiButton;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTheme_write extends AppCompatActivity {
    private ActivityCreateThemeWriteBinding binding;
    public static Activity CreateShop;
    // ?????? ???????????? ????????? ????????????, shop_index = ?????? index
    private String shop_name, shop_address, shop_more, shop_info, shop_open, shop_close, shop_holiday, shop_index, theme_writer;
    private ArrayList<String> shop_images = new ArrayList<>();
    // ?????? ?????? ????????????
    private ArrayList<DTO_theme> themes = new ArrayList<>();
    // ?????? ?????? ?????? array
    private ArrayList<String> images = new ArrayList<>();
    // ?????? ?????? Layout Open, Close ??????
    private boolean writeTheme;
    // ????????? ?????? ?????????
    private GalleryWrite_adapter createTheme_img_adapter;
    // ?????? ?????? ?????????
    private Theme_adapter theme_adapter;
    // ????????? ??? Switch view
    private SwitchMultiButton switchMultiButton;
    // ????????? ??? ??????
    private String theme_difficult = "Easy";
    // ?????? ?????????
    private ArrayList<String> theme_images = new ArrayList<>();
    // ??????, ????????????, ?????? ??????, ?????? ??????
    private String theme_limit, theme_genre, theme_info, theme_name;
    // ????????? ??????
    private InputMethodManager imm;
    private CreateShop_write my;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateThemeWriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // ?????? ?????? ????????????
        my = (CreateShop_write) CreateShop_write.my;

        // ???????????????
        setIntent();
        // adapter ??????
        setAdapter();

        // ?????? ?????? ?????? ???????????? up & down
        binding.createThemeAddTheme.setVisibility(View.GONE);
        binding.createThemeWriteTheme.setOnClickListener(v -> {
            AddTheme();
        });

        // ????????? ?????? ??????
        binding.createThemeAddTheme.setOnClickListener(v -> {
            if (getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                getCurrentFocus().clearFocus();
            }
        });

        // ????????? ????????? ????????? ??????
        binding.createThemeSubmit.setOnClickListener(v -> {
            if (getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
            // ?????? DB ??? ?????? ????????? ?????? ->  ????????? ???????????? index ??? ?????????
            // createCafe ?????? ??? -> CreateTheme ??? ????????? ?????? ??????
            createCafe();
        });

        // ????????? ?????? ???????????? ??????
        binding.createThemeMakeTheme.setOnClickListener(v -> {
            // theme_image ??? ???????????? ????????????
            // ???????????? adapter ??? ?????????
            write_theme();
        });

        // ?????? info ????????? textWatcher ??? ?????? ????????? ??????
        binding.createThemeInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.createThemeInfo.isFocusable() && !s.toString().isEmpty()) {
                    int number = binding.createThemeInfo.getText().toString().length();
                    binding.createThemeInfoNumber.setText(number + "");
                    theme_info = binding.createThemeInfo.getText().toString();
//                    Log.e("?????? ?????? ??????", "theme_info : " + theme_info);
                    if (number >= 200) {
                        number = 200;
                        Toast.makeText(getApplicationContext(), "200????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // ????????? ??????
        binding.createThemeDifficult.setOnSwitchListener((position, tabText) -> {
            theme_difficult = tabText;

//            Log.e("?????? ????????? ??????", "????????? : " + tabText);
        });


        // ?????? ?????? Spinner ?????? ????????? ????????? ??????
        binding.createThemeGenre.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                theme_genre = newItem;
//                Log.e("?????? ?????? ??????", "?????? :" + newItem);
            }
        });

        // ???????????? ?????? Spinner ?????? ????????? ????????? ??????
        binding.createThemeLimit.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                theme_limit = newItem;
//                Log.e("?????? ???????????? ??????", "?????? ?????? : " + newItem);
            }
        });

        // ????????? ?????? ????????? -> ??????????????? ????????? url ???????????? ????????? Adapter ??? ??????
        binding.createThemeImg.setOnClickListener(v -> {
            // ?????? ????????? ?????? ?????? ?????? ???????????? ?????????
            // ???????????? ????????? ???????????? ???????????? ?????? ?????????
            images.clear();
            selectImage();
        });
    }


    private void click_spinner() {
        binding.createThemeLimit.setOnClickListener(v -> {
            if (getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });

        binding.createThemeGenre.setOnClickListener(v -> {
            if (getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });
    }

    private void createTheme() {
        themes = theme_adapter.getItems();
        String user_index = PreferenceManager.getString(getApplicationContext(),"userIndex");
        // ????????? ????????? ?????? ????????? ????????? ????????? ?????????
        for (int i = 0; i < themes.size(); i++) {
            RequestBody name = RequestBody.create(themes.get(i).getName(), MediaType.parse("text/plain"));
            RequestBody diff = RequestBody.create(themes.get(i).getDifficult(), MediaType.parse("text/plain"));
            RequestBody limit = RequestBody.create(themes.get(i).getLimit(), MediaType.parse("text/plain"));
            RequestBody genre = RequestBody.create(themes.get(i).getGenre(), MediaType.parse("text/plain"));
            RequestBody info = RequestBody.create(themes.get(i).getInfo(), MediaType.parse("text/plain"));
            RequestBody cafe = RequestBody.create(shop_name, MediaType.parse("text/plain"));
            RequestBody index = RequestBody.create(shop_index, MediaType.parse("text/plain"));
            RequestBody user = RequestBody.create(user_index, MediaType.parse("text/plain"));

            // ??????????????? ????????? ???????????? ????????? ?????????. -> DTO ????????? ?????? ???????????? ????????? ???????????? ??? ????????????
            ArrayList<String> items = themes.get(i).getImages();
            ArrayList<MultipartBody.Part> files = new ArrayList<>();
            for (int j = 0; j < items.size(); j++) {
                // ??????????????? ?????? ????????? ??????
                File file = new File(items.get(j));
                Log.e("?????? ?????????", "?????? ?????? ??? : " + items.get(j));
                RequestBody RequestFile = RequestBody.create(file, MediaType.parse("multipart/form-date"));
                MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file" + i, i + "", RequestFile);
                files.add(body);
            }

            // ???????????? - ??????, ?????????, ????????????, ??????, ????????????, ?????? ??????
            RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
            Call<DTO_theme> call = retrofitInterface.writeTheme(files, name, diff, limit, genre, info, cafe, index, user);
            call.enqueue(new Callback<DTO_theme>() {
                @Override
                public void onResponse(Call<DTO_theme> call, Response<DTO_theme> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.e("???????????? ?????? ?????? ", "????????? ???????????? : " + name);
                    }
                }

                @Override
                public void onFailure(Call<DTO_theme> call, Throwable t) {

                }
            });
        }

        Intent intent = new Intent(CreateTheme_write.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }


    private void createCafe() {
        // ?????? ????????? RequestBody ??? ????????????
        // ????????? ??????
        // shop_name, ?????? ?????? (????????? uri), shop_address, shop_more (??????, ?????? ??????)
        // shop_info , shop_open,close (open, close) ,shop_holiday ?????????, shop_writer ?????? ??????
        String owner = PreferenceManager.getString(getApplicationContext(), "userIndex");
        RequestBody body_name = RequestBody.create(shop_name, MediaType.parse("text/plain"));
        RequestBody body_address = RequestBody.create(shop_address, MediaType.parse("text/plain"));
        RequestBody body_more = RequestBody.create(shop_more, MediaType.parse("text/plain"));
        RequestBody body_info = RequestBody.create(shop_info, MediaType.parse("text/plain"));
        RequestBody body_open = RequestBody.create(shop_open, MediaType.parse("text/plain"));
        RequestBody body_close = RequestBody.create(shop_close, MediaType.parse("text/plain"));
        RequestBody body_holiday = RequestBody.create(shop_holiday, MediaType.parse("text/plain"));
        RequestBody body_writer = RequestBody.create(owner, MediaType.parse("text/plain"));

        ArrayList<String> items = shop_images;
        ArrayList<MultipartBody.Part> theme_files = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            // ??????????????? ?????? ????????? ??????
            File file = new File(items.get(i));
            RequestBody RequestFile = RequestBody.create(file, MediaType.parse("multipart/form-date"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file" + i, i + "", RequestFile);
            theme_files.add(body);
        }

        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<DTO_shop> call = retrofitInterface.writeCafe(body_name, body_address, body_more, body_info, body_open, body_close, body_holiday, body_writer, theme_files);
        call.enqueue(new Callback<DTO_shop>() {
            @Override
            public void onResponse(Call<DTO_shop> call, Response<DTO_shop> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ?????? ??? ?????????
                    shop_index = response.body().getCafe_index();
                    Log.e("?????? ???????????? ???????????? ????????????", "shop_index : " + shop_index);
                    // ????????? index ??? Theme ?????? index ??? ???????????? ???????????? ?????? ??????.
                    createTheme();
                }
            }

            @Override
            public void onFailure(Call<DTO_shop> call, Throwable t) {

            }
        });
    }

    private void write_theme() {
        // ?????? ??????, ??????, ?????????, ??????, ????????????, ?????? ?????? ????????? ??????
        // ?????? ???????????? ????????? ??????????????? ????????????. -> ????????? ???????????? ?????? ??????????????? ?????????
        theme_name = binding.createThemeName.getText().toString();
        theme_images = createTheme_img_adapter.getImages();

        if(theme_name.isEmpty()){
            binding.createThemeName.setError("???????????? ??????????????????");
            binding.createThemeName.requestFocus();
            return;
        }

        /* ????????? : ?????? ???????????? ?????????, ?????? ????????? ???????????? ????????? ???????????? ????????? */
        if(createTheme_img_adapter.getItemCount() < 1){
            Toast.makeText(getApplicationContext(), "????????? ??????????????????", Toast.LENGTH_SHORT).show();
            binding.createThemeImg.requestFocus();
            return;
        }

        DTO_theme item = new DTO_theme(theme_name, theme_difficult, theme_limit, theme_genre, shop_name, theme_info, theme_images);
        theme_adapter.addItem(item);


        // ?????? ?????? ??????
        writeTheme = false;
        binding.createThemeAddTheme.setVisibility(View.GONE);
        binding.createThemeArrow.setImageResource(R.drawable.down);

        // ?????????
        binding.createThemeName.setText("");
        createTheme_img_adapter.clearImages();
        binding.createThemeImgNum.setText("0/3");
        binding.createThemeDifficult.setSelectedTab(0);
        binding.createThemeGenre.setText("??????");
        binding.createThemeLimit.setText("????????????");
        binding.createThemeInfo.setText("");

        imm.hideSoftInputFromWindow(binding.createThemeMakeTheme.getWindowToken(), 0);

        // theme ????????? ?????? ??????
        if (theme_adapter.getItemCount() > 0) {
            binding.createThemeNoItem.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) { // ????????? ????????? ????????????
            Toast.makeText(getApplicationContext(), "???????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
        } else {// ???????????? ?????? ?????? ????????? ?????? -> 1??? or 2??? ??????
            if (data.getClipData() == null) { // ???????????? 1??? ????????? ??????
                String image_uri = getReadPathFromUri(data.getData());
                images.add(image_uri);
            } else { // ???????????? ????????? ????????? ?????? 2??? ??????
                ClipData clipData = data.getClipData();

                if (clipData.getItemCount() > 3) {// 3??? ?????? ?????????
                    Toast.makeText(getApplicationContext(), "????????? 3????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
                } else {
                    for (int i = 0; i < clipData.getItemCount(); i++) { // ????????? ?????? ?????? ?????? ??????
                        String image_uri = getReadPathFromUri(clipData.getItemAt(i).getUri());
                        images.add(image_uri);
                    }
                }
            }

        }
        // ????????? ????????? ????????? Adapter ??? ???????????? ?????? ????????? ??????????????? ??????
        // ????????? ???????????? ????????? ???????????? 3?????? ???????????? ?????? ??????
        if (createTheme_img_adapter.getItemCount() + images.size() < 4) {
            for (int i = 0; i < images.size(); i++) {
                createTheme_img_adapter.setUri(images.get(i));
                createTheme_img_adapter.notifyItemInserted(createTheme_img_adapter.getItemCount());
                // ????????? ?????? ??????
                binding.createThemeImgNum.setText(createTheme_img_adapter.getItemCount() + "/3");
            }
        }
    }


    private String getReadPathFromUri(Uri uri) {
        String[] strArray = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(getApplicationContext(), uri, strArray, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String image_uri = cursor.getString(columnIndex);
        cursor.close();
        return image_uri;
    }

    private void selectImage() {
        // ????????? ???????????? 3??? ?????? ????????????
        if (createTheme_img_adapter.getItemCount() < 3) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, 2222);
        } else {
            Toast.makeText(getApplicationContext(), "????????? 3????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
        }
    }

    private void AddTheme() {
        if (writeTheme) {
            binding.createThemeAddTheme.setVisibility(View.GONE);
            binding.createThemeArrow.setImageResource(R.drawable.down);
            writeTheme = false;
        } else {
            binding.createThemeAddTheme.setVisibility(View.VISIBLE);
            binding.createThemeArrow.setImageResource(R.drawable.up);
            writeTheme = true;
        }
    }

    private void setAdapter() {

        // ????????? ??????
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        // ????????? ?????????????????? ??????
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CreateTheme_write.this, LinearLayoutManager.HORIZONTAL, false);
        binding.createThemeImageRv.setLayoutManager(linearLayoutManager);
        createTheme_img_adapter = new GalleryWrite_adapter(CreateTheme_write.this, binding.createThemeImgNum);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(createTheme_img_adapter));
        itemTouchHelper.attachToRecyclerView(binding.createThemeImageRv);
        binding.createThemeImageRv.setAdapter(createTheme_img_adapter);

        // ?????? ?????????????????? ??????
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(CreateTheme_write.this, LinearLayoutManager.HORIZONTAL, false);
        binding.createThemeThemeRv.setLayoutManager(linearLayoutManager1);
        theme_adapter = new Theme_adapter(CreateTheme_write.this);
        ItemTouchHelper itemTouchHelper1 = new ItemTouchHelper(new ItemTouchHelperCallback(theme_adapter));
        itemTouchHelper1.attachToRecyclerView(binding.createThemeThemeRv);
        binding.createThemeThemeRv.setAdapter(theme_adapter);
        theme_adapter.onDelete();
    }

    private void setIntent() {
        Intent i = getIntent();
        shop_name = i.getStringExtra("shop_name");
        shop_address = i.getStringExtra("shop_address");
        shop_more = i.getStringExtra("shop_address_more");
        shop_info = i.getStringExtra("shop_info");
        shop_open = i.getStringExtra("shop_open");
        shop_close = i.getStringExtra("shop_close");
        shop_holiday = i.getStringExtra("shop_holiday");
        shop_images = i.getStringArrayListExtra("shop_image");
        Log.e("????????? ??????", "  Shop_name : " + shop_name);

    }

    // ????????? -> File??? ??????
    private File bitmapToFile(Bitmap getBitmap, String filename) {
        // bitmap ??? ?????? File ??? ????????????
        File f = new File(getCacheDir(), filename);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // bitmap ??? byte array ?????????
        Bitmap bitmap = getBitmap;
        // bitmap ??? byte ????????? ?????????.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // ???????????? png ???????????? byte[] ?????? ???????????? bos ??? ?????????.
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bitmapData = bos.toByteArray();

        // byte [] ??? File ??? ?????????.
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    @Override
    /* ????????? ?????? ????????? ?????? */
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            // ???????????? ????????? ????????? (Rectangle) ????????????
            Rect rect = new Rect();
            // focus ??? View ??? ?????? ????????? ?????????
            focusView.getGlobalVisibleRect(rect);
            // ?????? ???????????? ????????? x, y ????????? ?????????
            int x = (int) ev.getX(), y = (int) ev.getY();
            // ?????????????????? focusView ?????? ?????? ??????????????? ??????
            if (!rect.contains(x, y)) {
                imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}