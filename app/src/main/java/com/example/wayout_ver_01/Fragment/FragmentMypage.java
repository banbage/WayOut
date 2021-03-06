package com.example.wayout_ver_01.Fragment;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wayout_ver_01.Activity.CreateShop.CreateShop_write;
import com.example.wayout_ver_01.Activity.MainActivity;
import com.example.wayout_ver_01.Activity.MyPage.MyLikeCafe;
import com.example.wayout_ver_01.Activity.MyPage.MyLikeTheme;
import com.example.wayout_ver_01.Activity.MyPage.MyManage;
import com.example.wayout_ver_01.Class.PreferenceManager;
import com.example.wayout_ver_01.R;
import com.example.wayout_ver_01.Retrofit.RetrofitClient;
import com.example.wayout_ver_01.Retrofit.RetrofitInterface;
import com.example.wayout_ver_01.Retrofit.User;
import com.example.wayout_ver_01.Activity.UserReset;
import com.example.wayout_ver_01.databinding.FragmentMypageBinding;
import com.example.wayout_ver_01.databinding.FragmentSearchCafeBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FragmentMypage extends Fragment {




    public static FragmentMypage newInstance() {
        
        Bundle args = new Bundle();
        FragmentMypage fragment = new FragmentMypage();
        fragment.setArguments(args);
        return fragment;
    }

    private final String TAG = this.getClass().getSimpleName();
    private TextView myPage_logout, myPage_Nick, mypage_follower, myPage_theme, myPage_cafe, myPage_delete, myPage_shop;
    private ImageView myPage_reset;
    private CircleImageView myPage_profile;
    private ArrayList<Uri> imageSaveList;
    private static final int REQUEST_CODE = 0;
    private static final int GALLEY_CODE = 10;
    private static final int TAKE_PICTURE = 1;
    private final static int REQUEST_TAKE_PHOTO = 1;
    private String imageUri = "";
    private String imageFilePath;
    private Uri photoUri;
    private View view;
    private FragmentMypageBinding bind;
    private int myIndex;
    String mCurrentPath;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        bind = FragmentMypageBinding.inflate(inflater,container,false);
        view = bind.getRoot();


        String ID = PreferenceManager.getString(view.getContext(), "autoId");
        String PW = PreferenceManager.getString(view.getContext(), "autoPw");
        myIndex =  PreferenceManager.getInt(requireContext(), "???????????????");

        // viewBinding
        setFindView();

        /*  ?????? ?????? */
        bind.myPageCafe.setOnClickListener((v -> {
            Intent intent = new Intent(view.getContext(), MyLikeCafe.class);
            startActivity(intent);
        }));

        /* ?????? ?????? */
        bind.myPageTheme.setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), MyLikeTheme.class);
            startActivity(intent);
        });


        // ????????????
        myPage_shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), CreateShop_write.class);
                startActivity(intent);
            }
        });

        /* ?????? ?????? */
        bind.myPageManage.setOnClickListener( v -> {
            Intent intent = new Intent(requireActivity(), MyManage.class);
            startActivity(intent);
        });

        // ????????????
        myPage_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PreferenceManager.clear(view.getContext());
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        // ????????? ?????? ??????
        myPage_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), UserReset.class);
                startActivity(intent);

            }
        });

        // ????????? ?????? ??????
        myPage_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> ListItems = new ArrayList<>();
                ListItems.add("???????????? ?????? ??????");
                ListItems.add("???????????? ??????");
                ListItems.add("????????? ?????? ??????");


                // String ?????? Items ??? ListItems ??? string[ListItems.size()] ????????? ????????????.
                final String[] items = ListItems.toArray(new String[ListItems.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setItems(items, (dialog, pos) -> {
                    String selectedText = items[pos];
                    switch (selectedText) {
                        case "???????????? ?????? ??????":
                            dispatchTakePictureIntent();
                            break;
                        case "???????????? ??????":
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            Log.e(TAG, "?????? : ?????? ????????? intent :" + intent);
                            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                            Log.e(TAG, "?????? : ?????? ????????? Intent2 : " + intent);
                            startActivityForResult(intent, GALLEY_CODE);
                            break;
                        case "????????? ?????? ??????":
                            deleteUserProfile(myIndex);
                            break;
                    }
                });
                builder.show();
            }
        });

        // ?????? ?????? ??????
        myPage_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("?????? ??????");
                builder.setMessage("\n????????? ?????????????????????????\n");
                builder.setPositiveButton("???",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.e("Test", "Activity : myPage // ???????????? ??????");
                                userDelete(myIndex);
                            }
                        });
                builder.setNegativeButton("?????????",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.e("Test", "Activity : myPage // ???????????? ??????");
                            }
                        });
                builder.show();

            }
        });

        return view;

        /////
    }

    private void setFindView() {
        myPage_logout = view.findViewById(R.id.myPage_logout);
        myPage_reset = view.findViewById(R.id.myPage_reset);
        myPage_theme = view.findViewById(R.id.myPage_theme);
        myPage_cafe = view.findViewById(R.id.myPage_cafe);
        myPage_delete = view.findViewById(R.id.myPage_delete);
        myPage_reset = view.findViewById(R.id.myPage_reset);
        myPage_profile = view.findViewById(R.id.myPage_profile);
        myPage_Nick = view.findViewById(R.id.myPage_Nick);
        myPage_shop = view.findViewById(R.id.myPage_Shop);
    }

    // ?????? ?????? ???????????? !!!!!
    private String getRealPathFromUri(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Log.e(TAG, "?????? : String[] proj : " + proj);
        CursorLoader cursorLoader = new CursorLoader(getContext(), uri, proj, null, null, null);
        Log.e(TAG, "?????? : CursorLoader : " + cursorLoader);
        Cursor cursor = cursorLoader. loadInBackground();
        Log.e(TAG, "?????? :  Cursor : " + cursor);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        Log.e(TAG, "?????? : columnIndex : " + columnIndex);
        cursor.moveToFirst();
        Log.e(TAG, "?????? :  Cursor.moveToFirst : " + cursor.moveToFirst());
        String url = cursor.getString(columnIndex);
        Log.e(TAG, "?????? : url : " + url);
        cursor.close();
        return url;
    }

//        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if (result.getResultCode() == RESULT_OK) {
//                        Log.e(TAG, "result : " + result);
//                        Intent intent = result.getData();
//                        Log.e("test", "intent : " + intent);
//                        Uri uri = intent.getData();
//                        Log.e("test", "uri : " + uri);
////                        imageview.setImageURI(uri);
//                        Glide.with(FragmentMypage.this)
//                                .load(uri)
//                                .into(myPage_profile);
//                    }
//                }
//            });

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null)
        {

            switch (requestCode) {
                case GALLEY_CODE:
                    imageUri = getRealPathFromUri(data.getData());
                    Log.e(TAG, "?????? : ????????? ????????? ?????? ?????? : " + imageUri);
                    break;
                case REQUEST_TAKE_PHOTO:
                    imageUri = mCurrentPath;
                    Log.e(TAG, "?????? : ????????? ?????? ????????? ?????? ?????? : " + imageUri);
                    break;
            }
            ProgressDialog progressDialog = new ProgressDialog(requireContext());
            progressDialog.setMessage("Loading...");
            progressDialog.show();

            // ?????? ????????? ????????? ???????????? ??????????????? request body ??? multipart ???????????? ????????? ??????
            File file = new File(imageUri);
            Log.e(TAG, "?????? : file : " + file);
//                    ArrayList<MultipartBody.Part> files = new ArrayList<>();
//                    Log.e(TAG, "?????? : ArrayList<MultipartBody.Part> : " + files);
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            Log.e(TAG, "?????? : requestFile : " + requestFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "1234", requestFile);
            Log.e(TAG, "?????? : MultipartBody.Part : " + body);
//            files.add(body);
//            Log.e(TAG, "?????? : files : " + files);



            RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
            Log.e(TAG, "?????? retrofitInterface : " + retrofitInterface);
            Call<User> call = retrofitInterface.userProfile(body, myIndex);
            Log.e(TAG, "?????? : call : " + call);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if(response.isSuccessful() && response.body() != null)
                    {
                        Log.e(TAG, "?????? : === ????????? ?????? ?????? ????????? : ?????? ===");
                        Glide.with(FragmentMypage.this)
                                .load(response.body().getUserProfile())
                                .into(myPage_profile);
                     progressDialog.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e(TAG, "?????? error message : " + t);
                    progressDialog.dismiss();
                }
            });

        }
    }

    private void deleteUserProfile(int userIndex){
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<User> call = retrofitInterface.deleteUserProfile(userIndex);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful() && response.body() != null)
                {
                    Log.e(TAG, "?????? : === ????????? ?????? ?????? : ?????? ===");
                    Glide.with(getContext())
                            .load(response.body().getUserProfile())
                            .into(myPage_profile);
                    Log.e(TAG, "?????? : userProfile : " + response.body().getUserProfile());

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "?????? : === ????????? ?????? ?????? : ?????? // error message : " + t);
            }
        });
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if(takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null )
//        {
            File photoFile = null;
            try
            {
             photoFile = createImageFile();
            }catch (Exception e) { e.printStackTrace(); }
            if(photoFile != null)
            {
                Uri photoURI = FileProvider.getUriForFile(getContext(), "com.example.wayout_ver_01", photoFile);
//                Log.e(TAG, "?????? : photoUri : " + photoURI);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
//        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // ????????? ????????? ?????? ???????????? ????????? ?????????
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.e(TAG, "?????? : ????????? ????????? ?????? storageDir : " + storageDir);
        // ?????? ???????????? ????????? ?????????
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.e(TAG, "?????? : ?????? ???????????? ?????????????????? , jpg ???????????? ????????? image : " + image);
        // ?????? ?????? ??????
        mCurrentPath = image.getAbsolutePath();
        // ????????? ?????? ?????? ?????? ?????? ????????? -> ????????? ?????????
        Log.e(TAG, "?????? : ?????? ?????? ?????? : " + mCurrentPath);

        return image;
    }

    @Override
    public void onResume() {
        super.onResume();
        myIndex =  PreferenceManager.getInt(requireContext(), "???????????????");
        String Nick = PreferenceManager.getString(getContext(), "autoNick");
        myPage_Nick.setText(Nick);

        Log.e(TAG, "?????? : ===== onResume ======================");


        Log.e("mypage", "userIndex : " + myIndex);
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<User> call = retrofitInterface.getUserProfile(myIndex);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful() && response.body() != null)
                {
                        Glide.with(FragmentMypage.this)
                                .load(response.body().getUserProfile())
                                .into(myPage_profile);


                        Log.e(TAG, "?????? : ????????? ?????? : " +response.body().getUserProfile());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "?????? : onResume ?????? ????????? ?????? : "+t );
            }
        });
    }






    @Override
    public void onStart() {
        super.onStart();
        Log.e("Frag2 onStart", "onStart ok2");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("Frag2 onPause", "onPause ok2");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("Frag2 onStop", "onStop ok2");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("Frag2 onDestroyView", "onDestoryView ok2");
        bind = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Frag2 onDestroy", "onDestroy ok2");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e("Frag2 onDetach", "onDetach ok2");
    }


    private void userDelete(int userIndex) {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
        Call<User> call = retrofitInterface.userDelete(userIndex);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("Test", "Activity : myPage // ???????????? : ???????????? ?????? ?????? // ?????? ??? :");
                    boolean status = response.body().getStatus();

                    if (status) {
                        Log.e("Test", "Activity : myPage // ???????????? : PHP ?????? ?????? // ?????? ??? :" + response.body().getMessage());
                        Toast.makeText(getContext(), "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        PreferenceManager.clear(getContext());

                    } else {
                        Log.e("Test", "Activity : myPage // ???????????? : PHP ????????? ???????????? // ?????? ??? :" + response.body().getMessage());
                        Toast.makeText(getContext(), "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("Test", "Activity : myPage // ???????????? : ???????????? ?????? ?????? // ?????? ??? : " + t);
            }
        });
    }
}