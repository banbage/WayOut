package com.example.wayout_ver_01.Activity.Search;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wayout_ver_01.Activity.CreateShop.CreateShop_address;
import com.example.wayout_ver_01.Activity.CreateShop.CreateShop_write;
import com.example.wayout_ver_01.Activity.CreateShop.CreateTheme_write;
import com.example.wayout_ver_01.Class.NetworkState;
import com.example.wayout_ver_01.Class.PreferenceManager;
import com.example.wayout_ver_01.R;
import com.example.wayout_ver_01.RecyclerView.FreeBoard.ItemTouchHelperCallback;
import com.example.wayout_ver_01.RecyclerView.Gallery.GalleryWrite_adapter;
import com.example.wayout_ver_01.Retrofit.DTO_gallery;
import com.example.wayout_ver_01.Retrofit.DTO_image;
import com.example.wayout_ver_01.Retrofit.DTO_img;
import com.example.wayout_ver_01.Retrofit.DTO_review;
import com.example.wayout_ver_01.Retrofit.DTO_shop;
import com.example.wayout_ver_01.Retrofit.DTO_theme;
import com.example.wayout_ver_01.Retrofit.RetrofitClient;
import com.example.wayout_ver_01.Retrofit.RetrofitInterface;
import com.example.wayout_ver_01.databinding.ActivitySearchCafeUpdateBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Multipart;

public class SearchCafe_update extends AppCompatActivity {
    private ActivitySearchCafeUpdateBinding bind;
    private ArrayList<String> images = new ArrayList<>();
    private GalleryWrite_adapter cafe_update_adapter;
    private ItemTouchHelper itemTouchHelper;
    private String open_time, close_time, call, cafe_index;
    private ProgressDialog progressDialog;
    private InputMethodManager imm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivitySearchCafeUpdateBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        setAdapter();
        setData();

        // ????????? ??????????????? 3????????? ????????????
        bind.cafeUpdateImg.setOnClickListener(v -> {
            // ?????? ????????? ?????? ?????? ?????? ???????????? ?????????
            // ???????????? ????????? ???????????? ???????????? ?????? ?????????
            images.clear();

            // ????????? ???????????? 3??? ?????? ????????????
            if (cafe_update_adapter.getItemCount() < 3) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, 2222);
            } else {
                Toast.makeText(getApplicationContext(), "????????? 3????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
            }
        });

        bind.cafeUpdateKeyboard.setOnClickListener(v -> {
            if(getCurrentFocus() !=  null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });

        /* ????????? API -> ????????? ?????? ???????????? */
        bind.cafeUpdateAddress.setOnClickListener(v -> {
            int status = NetworkState.getConnectivityStatus(getApplicationContext());
            // ????????? ????????? ???????????? ?????????
            if (status == NetworkState.TYPE_MOBILE || status == NetworkState.TYPE_WIFI) {
                Log.e("?????? ????????? ??????", "????????? ?????? ?????? OK ");
                Intent i = new Intent(getApplicationContext(), CreateShop_address.class);
                // ?????? ?????? ??????????????? ?????????
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                overridePendingTransition(0, 0);
                startActivityForResult(i, 3333);
            } else {
                Toast.makeText(getApplicationContext(), "????????? ????????? ??????????????????", Toast.LENGTH_SHORT).show();
            }
        });

        /* ?????? ?????? ?????? */
        bind.cafeUpdateOpen.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(SearchCafe_update.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    (TimePickerDialog.OnTimeSetListener) (view, hourOfDay, minute) -> {
                        String am = "?????? ";
                        String hour = hourOfDay + " : ";
                        String min = minute + "";

                        if (hourOfDay < 10) {
                            hour = "0" + hourOfDay + " : ";
                        } else if (hourOfDay >= 12) {
                            am = "?????? ";
                            if (hourOfDay > 12) {
                                hour = (hourOfDay - 12) + " : ";
                            }
                        }
                        if (minute < 10) {
                            min = "0" + minute;
                        }
                        open_time = am + hour + min;
                        bind.cafeUpdateOpenTime.setText(open_time);
                    }
                    , 9, 0, false);
            timePickerDialog.setTitle("?????? ?????? ??????");
            timePickerDialog.show();
        });

        /* ?????? ?????? ?????? */
        bind.cafeUpdateClose.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(SearchCafe_update.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    (TimePickerDialog.OnTimeSetListener) (view, hourOfDay, minute) -> {
                        String am = "?????? ";
                        String hour = hourOfDay + " : ";
                        String min = minute + "";

                        if (hourOfDay < 10) {
                            hour = "0" + hourOfDay + " : ";
                        } else if (hourOfDay >= 12) {
                            am = "?????? ";
                            if (hourOfDay > 12) {
                                hour = (hourOfDay - 12) + " : ";
                            }
                        }
                        if (minute < 10) {
                            min = "0" + minute;
                        }
                        close_time = am + hour + min;
                        bind.cafeUpdateCloseTime.setText(close_time);
                    }
                    , 22, 0, false);
            timePickerDialog.setTitle("?????? ?????? ??????");
            timePickerDialog.show();
        });

        /* Info ?????? */
        bind.cafeUpdateInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (bind.cafeUpdateInfo.isFocusable() && !s.toString().isEmpty()) {
                    int number = bind.cafeUpdateInfoNumber.getText().toString().length();
                    bind.cafeUpdateInfoNumber.setText(number + "");
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

        /* ?????? ?????? ?????? ??? -> result ok ????????? */
        bind.cafeUpdateSubmit.setOnClickListener(v -> {

            // ????????? ??????
            // ?????? ??????
            // ?????? ?????? (??????, ?????? ??????)
            // ?????? ??????
            // ?????? ?????? (open, close)
            // ?????????
            RequestBody shop_name = RequestBody.create(bind.cafeUpdateName.getText().toString(), MediaType.parse("text/plain"));
            RequestBody shop_address = RequestBody.create(bind.cafeUpdateAddress.getText().toString(), MediaType.parse("text/plain"));
            RequestBody shop_address_more = RequestBody.create(bind.cafeUpdateAddressMore.getText().toString(), MediaType.parse("text/plain"));
            RequestBody shop_info = RequestBody.create(bind.cafeUpdateInfo.getText().toString(), MediaType.parse("text/plain"));
            RequestBody shop_open = RequestBody.create(bind.cafeUpdateOpenTime.getText().toString(), MediaType.parse("text/plain"));
            RequestBody shop_close = RequestBody.create(bind.cafeUpdateCloseTime.getText().toString(), MediaType.parse("text/plain"));
            RequestBody shop_holiday = RequestBody.create(bind.cafeUpdateHoliday.getText().toString(), MediaType.parse("text/plain"));
            RequestBody shop_index = RequestBody.create(cafe_index, MediaType.parse("text/plain"));

            ArrayList<DTO_gallery> items = new ArrayList<>();
            items = cafe_update_adapter.getItems();
            ArrayList<MultipartBody.Part> files = new ArrayList<>();

            // bitmap ??? File ??? ????????? ??????
            if(items != null) {
                for (int i = 0; i < items.size(); i++){
                    File file = bitmapToFile(items.get(i).getBitmap(), "bitmaps" + i);
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-date"), file);
                    MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file" + i, i + "", requestFile);
                    files.add(body);
                }
            }

//            // ?????? ?????? (????????? uri)
//            ArrayList<String> shop_image = cafe_update_adapter.getImages();
//            ArrayList<MultipartBody.Part> files = new ArrayList<>();
//            for(int i =0; i < shop_image.size(); i++){
//                // ??????????????? ?????? ????????? ??????
//                File file = new File(shop_image.get(i));
//                RequestBody RequestFile = RequestBody.create(file, MediaType.parse("multipart/form-date"));
//                MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file" + i, i+"", RequestFile);
//                files.add(body);
//            }

            // ?????? ???????????? ???????????? -> ?????? , ?????????, ???????????? , ??????, ????????????, ?????? ??????
            RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
            Call<DTO_shop> call = retrofitInterface.updateCafe(shop_name,shop_address,shop_address_more,shop_info,shop_open,shop_close,shop_holiday,shop_index,files);
            call.enqueue(new Callback<DTO_shop>() {
                @Override
                public void onResponse(Call<DTO_shop> call, Response<DTO_shop> response) {
                    if(response.body() != null && response.isSuccessful()){
                        Intent i = new Intent(getBaseContext(), SearchCafe_read.class);
                        i.putExtra("callback","cafe_update");
                        setResult(RESULT_OK,i);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<DTO_shop> call, Throwable t) {
                    Log.e("update_cafe, 226", "?????? ???????????? ?????? : " + t);
                }
            });
        });

        /* =============================================== */
    }

    private void setAdapter() {
        // ????????? ??????
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        // ????????? ?????????????????? ??????
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.HORIZONTAL, false);
        bind.cafeUpdateImageRv.setLayoutManager(linearLayoutManager);
        cafe_update_adapter = new GalleryWrite_adapter(getBaseContext(), bind.cafeUpdateImgNum);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(cafe_update_adapter));
        itemTouchHelper.attachToRecyclerView(bind.cafeUpdateImageRv);
        bind.cafeUpdateImageRv.setAdapter(cafe_update_adapter);
    }

    private void setData() {
        Intent i = getIntent();
        call = i.getStringExtra("call");
        cafe_index = i.getStringExtra("cafe_index");


        // ????????? ??????
        progressDialog = new ProgressDialog(SearchCafe_update.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        /* 2 ??? ????????? ?????? */
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 2?????? ????????? ??????????????? ??????
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
        Call<DTO_shop> call = retrofitInterface.getCafeRead(cafe_index, 1, 8, user_id);
        call.enqueue(new Callback<DTO_shop>() {
            @Override
            public void onResponse(Call<DTO_shop> call, Response<DTO_shop> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // ????????? ?????? , ?????? ??????,
                    bind.cafeUpdateName.setText(response.body().getName());
                    bind.cafeUpdateOpenTime.setText(response.body().getOpen());
                    bind.cafeUpdateCloseTime.setText(response.body().getClose());
                    bind.cafeUpdateHoliday.setText(response.body().getHoliday());
                    bind.cafeUpdateAddress.setText(response.body().getAddress());
                    bind.cafeUpdateAddressMore.setText(response.body().getMore_address());
                    bind.cafeUpdateInfo.setText(response.body().getInfo());
                    bind.cafeUpdateInfoNumber.setText("" + response.body().getInfo().length());
                    ArrayList<DTO_image> images = response.body().getImages();
                    Log.e("????????? ??????", images.get(0).getImage_uri());
                    for(int i =0; i < images.size(); i++){
                        cafe_update_adapter.addItems(images.get(i).getImage_uri());
                    }
                }

                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<DTO_shop> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case 2222:

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
                    if (cafe_update_adapter.getItemCount() + images.size() < 4) {
                        for (int i = 0; i < images.size(); i++) {
                            cafe_update_adapter.setUri(images.get(i));
                            cafe_update_adapter.notifyItemInserted(cafe_update_adapter.getItemCount());
                            // ????????? ?????? ??????
                            bind.cafeUpdateImgNum.setText(cafe_update_adapter.getItemCount() + "/3");
                        }
                    }
                    break;

                case 3333:
                    if (resultCode == RESULT_OK) {
                        String address = data.getExtras().getString("address");
                        if (address != null) {
                            Log.e("?????? ??????", "?????? ????????? : " + address);
                            bind.cafeUpdateAddress.setText(address);
                            bind.cafeUpdateAddressMore.requestFocus();
                        }
                    }

            }
        }
        ////
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

    public File bitmapToFile (Bitmap getBitmap, String filename)
    {
        //create a file to write bitmap data
        File f = new File(getCacheDir(), filename);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

//Convert bitmap to byte array
        Bitmap bitmap = getBitmap;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(bitmapdata);
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
