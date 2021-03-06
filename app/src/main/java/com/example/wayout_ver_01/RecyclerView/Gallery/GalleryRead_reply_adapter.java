package com.example.wayout_ver_01.RecyclerView.Gallery;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wayout_ver_01.Class.DateConverter;
import com.example.wayout_ver_01.Class.PreferenceManager;
import com.example.wayout_ver_01.R;
import com.example.wayout_ver_01.RecyclerView.FreeBoard.FreeComment_adapter;
import com.example.wayout_ver_01.Retrofit.DTO_comment;
import com.example.wayout_ver_01.Retrofit.DTO_gallery;
import com.example.wayout_ver_01.Retrofit.RetrofitClient;
import com.example.wayout_ver_01.Retrofit.RetrofitInterface;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GalleryRead_reply_adapter extends RecyclerView.Adapter<GalleryRead_reply_adapter.viewHolder> {
    ArrayList<DTO_comment> items = new ArrayList<>();
    private boolean mode;
    Context context;
    String index;
    EditText update_content;
    int update_pos;
    InputMethodManager imm;

    public GalleryRead_reply_adapter(Context context) {
        this.context = context;
    }

    public void addItem(DTO_comment item) {
        items.add(item);
    }

    public void itemsClear(){
        this.items.clear();
        notifyDataSetChanged();
    }

    public void setEdit(EditText et) {
        this.update_content = et;
    }

    public void updateItem(int pos, String content){
        items.get(pos).setContent(content);
        notifyItemChanged(pos);
    }

    public void setImm (InputMethodManager imm) {this.imm = imm;}

    public boolean getMode() { return mode;}

    public int getPos() {return update_pos; }

    public void endMode() {
        this.mode = false;
    }

    public String getIndex(){
        return index;
    }



    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_free_comment_reply, parent, false);
        return new viewHolder(item, this);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        DTO_comment item = items.get(position);
        holder.item_writer.setText(item.getWriter());
        holder.item_content.setText(item.getContent());
        try {
            holder.item_date.setText(DateConverter.resultDateToString(item.getDate(), "M??? d??? a h:mm"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // ?????? ?????? ?????? ??????
        if (!item.getWriter().equals(PreferenceManager.getString(context, "autoNick"))) {
            holder.item_menu.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        private TextView item_writer, item_content, item_date;
        private ImageView item_menu;
        private String content, index;
        private boolean mode;

        public viewHolder(@NonNull View item, GalleryRead_reply_adapter adapter) {
            super(item);

            item_writer = item.findViewById(R.id.freeComment_writer);
            item_content = item.findViewById(R.id.freeComment_content);
            item_date = item.findViewById(R.id.freeComment_date);
            item_menu = item.findViewById(R.id.freeComment_menu);

            item_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String index = adapter.items.get(getAdapterPosition()).getComment_index();
                    setMenu(index, adapter);
                }
            });

        }

        private void setMenu(String index, GalleryRead_reply_adapter adapter) {

            final List<String> ListItems = new ArrayList<>();
            ListItems.add("?????? ??????");
            ListItems.add("?????? ??????");

            // String ?????? Items ??? ListItems ??? string[ListItems.size()] ????????? ????????????.
            final String[] items = ListItems.toArray(new String[ListItems.size()]);
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setItems(items, (dialog, pos) -> {
                String selectedText = items[pos];
                switch (selectedText) {
                    case "?????? ??????":
                        delete_reply(index, adapter);
                        break;
                    case "?????? ??????":
                        update_reply(index, adapter);
                        break;
                }
            });
            builder.show();
        }

        private void update_reply(String index, GalleryRead_reply_adapter adapter) {
            // ???????????? ??? ?????? ???????????? Et ??? ????????????
            // ????????? ????????? ?????? ????????????
            // ????????? ????????? ????????? ????????????
            // ?????? ??????????????? ????????? et ????????? ?????????
            // ????????? ?????? ??????????????? ?????????
            adapter.mode = true;
            adapter.update_content.setText(adapter.items.get(getAdapterPosition()).getContent());
            adapter.update_pos = getAdapterPosition();
            adapter.index = adapter.items.get(getAdapterPosition()).getComment_index();
            adapter.imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            adapter.update_content.requestFocus();
            adapter.update_content.setSelection(adapter.items.get(getAdapterPosition()).getContent().length());
        }

        private void delete_reply(String index, GalleryRead_reply_adapter adapter) {
            // ?????????
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(adapter.context);
            builder.setTitle("????????? ??????");
            builder.setMessage("\n????????? ?????????????????????????\n");
            builder.setPositiveButton("???",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // ?????? ????????????
                            DTO_comment item = adapter.items.get(getAdapterPosition());
                            RetrofitInterface retrofitInterface = RetrofitClient.getApiClint().create(RetrofitInterface.class);
                            Call<DTO_comment> call = retrofitInterface.deleteGalleryReply(item.getBoard_number(), item.getComment_index());
                            call.enqueue(new Callback<DTO_comment>() {
                                @Override
                                public void onResponse(Call<DTO_comment> call, Response<DTO_comment> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        adapter.items.remove(getAdapterPosition());
                                        adapter.notifyItemRemoved(getAdapterPosition());
                                        Log.e("????????? ?????? ??????", "?????? ?????? ??????");
                                    } else {
                                        Toast.makeText(itemView.getContext(), "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<DTO_comment> call, Throwable t) {
                                    Toast.makeText(itemView.getContext(), "?????? ?????? ?????? : " + t, Toast.LENGTH_SHORT).show();
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
    }
}
