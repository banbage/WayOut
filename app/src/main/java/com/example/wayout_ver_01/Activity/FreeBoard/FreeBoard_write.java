package com.example.wayout_ver_01.Activity.FreeBoard;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wayout_ver_01.RecyclerView.FreeBoard.FreeWrite_Adapter;
import com.example.wayout_ver_01.RecyclerView.FreeBoard.ItemTouchHelperCallback;
import com.example.wayout_ver_01.Class.DateConverter;
import com.example.wayout_ver_01.Class.PreferenceManager;
import com.example.wayout_ver_01.R;
import com.example.wayout_ver_01.Retrofit.DTO_board;
import com.example.wayout_ver_01.Retrofit.DTO_img;
import com.example.wayout_ver_01.Retrofit.RetrofitClient;
import com.example.wayout_ver_01.Retrofit.RetrofitInterface;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FreeBoard_write extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private TextView freeWrite_btn, freeWrite_img_num;
    private EditText freeWrite_title, freeWrite_content;
    private ImageView freeWrite_img;
    private RecyclerView freeWrite_rv;
    private ProgressDialog progressDialog;
    private ArrayList<String> uriList = new ArrayList<>();
    private FreeWrite_Adapter freeWrite_adapter;
    public ItemTouchHelper helper;
    private boolean mode_update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_board_write);

        freeWrite_btn = findViewById(R.id.freeWrite_btn);
        freeWrite_title = findViewById(R.id.freeWrite_title);
        freeWrite_content = findViewById(R.id.freeWrite_content);
        freeWrite_img = findViewById(R.id.freeWrite_img);
        freeWrite_rv = findViewById(R.id.freeWrite_rv);
        freeWrite_img_num = findViewById(R.id.freeWrite_img_num);

        if (freeWrite_adapter == null) {
            freeWrite_rv.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FreeBoard_write.this, LinearLayoutManager.HORIZONTAL, false);
            freeWrite_rv.setLayoutManager(linearLayoutManager);
            freeWrite_adapter = new FreeWrite_Adapter(FreeBoard_write.this, freeWrite_img_num);
            helper = new ItemTouchHelper(new ItemTouchHelperCallback(freeWrite_adapter));
            helper.attachToRecyclerView(freeWrite_rv);
            freeWrite_rv.setAdapter(freeWrite_adapter);
        }

        // ??????
        freeWrite_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                // Retrofit ??????
                String title = freeWrite_title.getText().toString();
                String content = freeWrite_content.getText().toString();
                String date = null;
                try {
                    date = DateConverter.setDate();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String writer = PreferenceManager.getString(FreeBoard_write.this, "autoNick");
                Log.e(TAG, "?????? : writer : " + writer);

                RequestBody body_title = RequestBody.create(MediaType.parse("text/plain"), title);
                RequestBody body_content = RequestBody.create(MediaType.parse("text/plain"), content);
                RequestBody body_date = RequestBody.create(MediaType.parse("text/plain"), date);
                RequestBody body_writer = RequestBody.create(MediaType.parse("text/plain"), writer);

                // ????????? ??????
                if (title.isEmpty()) {
                    freeWrite_title.setError("????????? ??????????????????");
                    freeWrite_title.requestFocus();
                    return;
                }
                if (content.isEmpty()) {
                    freeWrite_content.setError("????????? ??????????????????");
                    freeWrite_content.requestFocus();
                    return;
                }

                // ?????????
                progressDialog = new ProgressDialog(FreeBoard_write.this);
                progressDialog.setMessage("Loading...");
                progressDialog.show();

                ArrayList<DTO_img> items = new ArrayList<>();
                items = freeWrite_adapter.getItems();
                ArrayList<MultipartBody.Part> files = new ArrayList<>();

                // bitmap ??? File ??? ????????? ??????
                if(items != null) {
                    for (int i = 0; i < items.size(); i++){
                        Log.e(TAG, "?????? : bitmapToFile : " + items.get(i));
                        File file = bitmapToFile(items.get(i).getBitmap(), "bitmaps" + i);
                        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-date"), file);
                        MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file" + i, i + "", requestFile);
                        files.add(body);
                        Log.e(TAG, "?????? : getFIles : " + files.get(i));
                    }
                }

//                // ????????? ????????? ???????????? ?????????
//                for (int i = 0; i < items.size(); i++) {
//                    File file = new File(items.get(i).getImageUri());
//                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-date"), file);
//                    MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file" + i, i + "", requestFile);
//                    files.add(body);
//                }

                // ?????? ????????????
                RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
                Call<DTO_board> call = retrofitInterface.writeFreeBoard(body_writer, body_title, body_content, body_date, items.size(), files);
                call.enqueue(new Callback<DTO_board>() {
                    @Override
                    public void onResponse(@NonNull Call<DTO_board> call, @NonNull Response<DTO_board> response) {
                        if (response.isSuccessful() && response.body() != null)
                            freeWrite_adapter.clearBitmaps();
                            progressDialog.dismiss();
                        {
                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(freeWrite_title.getWindowToken(), 0);
                            Log.e(TAG, "?????? :  request ?????? , response :" + response.body());
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DTO_board> call, @NonNull Throwable t) {
                        Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "?????? : ??????????????? : " + t.getLocalizedMessage());
                        progressDialog.dismiss();
                    }
                });
            }
        });

        // ????????? ????????????
        freeWrite_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uriList.clear();

                Log.e(TAG, "?????? : ????????? ?????? : " + freeWrite_adapter.getItemCount() + uriList.size());
                if (freeWrite_adapter.getItemCount() < 3) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.CONTENT_TYPE);
                    startActivityForResult(intent, 2222);
                } else {
                    Toast.makeText(getApplicationContext(), "????????? 3????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) { // ???????????? ???????????? ????????????
            Log.e(TAG, "?????? : ???????????? ???????????? ????????????");
            Toast.makeText(getApplicationContext(), "???????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
        } else {// ???????????? ???????????? ????????? ??????
            if (data.getClipData() == null) { // ???????????? ????????? ????????? ??????
                String imageUri = getRealPathFromUri(data.getData());
                uriList.add(imageUri);

            } else { //  ???????????? ????????? ????????? ??????
                ClipData clipData = data.getClipData();
                Log.e(TAG, "?????? : ClipData : " + String.valueOf(clipData.getItemCount()) + clipData);

                if (clipData.getItemCount() > 3) { // 3??? ?????? ?????????
                    Toast.makeText(getApplicationContext(), "????????? 3????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
                } else { // 1~3 ??? ????????? ??????
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        String imageUri = getRealPathFromUri(clipData.getItemAt(i).getUri()); // ????????? ???????????? uri ??? ????????????.
                        try {
                            uriList.add(imageUri); // uri ??? list ??? ?????????.

                            Log.e(TAG, "?????? : imageUri : " + imageUri);
                            Log.e(TAG, "?????? : uriList : " + uriList);

                        } catch (Exception e) {
                            Log.e(TAG, "??????????????? : " + e.getLocalizedMessage());
                        }
                    }
                }
            }
        }
        if (freeWrite_adapter.getItemCount() + uriList.size() < 4) {
            for (int i = 0; i < uriList.size(); i++) {
                freeWrite_adapter.addItem(new DTO_img(uriList.get(i)), freeWrite_adapter.getItemCount());
                freeWrite_adapter.notifyItemInserted(freeWrite_adapter.getItemCount());
                // ????????? ?????? ??????
                freeWrite_img_num.setText(freeWrite_adapter.getItemCount() + "/3");
            }
        } else {
            Toast.makeText(getApplicationContext(), "????????? 3????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
        }
    }

    // ?????? ?????? ???????????? !!!!!
    private String getRealPathFromUri(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(FreeBoard_write.this, uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String url = cursor.getString(columnIndex);
        cursor.close();
        return url;
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
}