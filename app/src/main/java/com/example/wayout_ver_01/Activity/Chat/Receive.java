package com.example.wayout_ver_01.Activity.Chat;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wayout_ver_01.Activity.Chat.Chat.DTO_message;
import com.example.wayout_ver_01.Class.PreferenceManager;
import com.example.wayout_ver_01.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.channels.Channel;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Receive extends Thread implements Runnable {
    private BufferedReader br2 = null;
    private PrintWriter pw = null;
    private Socket sc = null;
    private String request = null;
    private String user_id;
    private Context context;
    private int temp = 0;

    public Receive(Socket socket, Context context) {
        this.sc = socket;
        this.context = context;
        try {
            this.br2 = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {
            String NOTIFICATION_CHANNEL_ID = "test1";

            /* TODO : ?????? ??????  */
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "channel_test1", NotificationManager.IMPORTANCE_HIGH);

                /*  ?????? ?????? */
                channel.setDescription("Channel description");
                channel.enableLights(true);
                channel.setLightColor(Color.RED);
                channel.setVibrationPattern(new long[]{0, 1000});
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }


            while (true) {
                request = br2.readLine();
                DTO_message chat = makeDTO(request);
                String name = PreferenceManager.getString(context, "userId");
                String image = chat.getImage();
                String message = chat.getMessage();
                String title = chat.getRoom();
                String code = chat.getCode();
                String writer = chat.getName();
                String room_name = chat.getRoom_name();
                int noti_id = Integer.parseInt(title);


                Log.w("//===========//", "================================================");
                Log.i("", "\n" + "[ Receive_Thread,47 :: ???????????? ?????? msg : " + request + "]  ");
                Log.w("//===========//", "================================================");

                if (request == null) {
                    Log.w("//===========//", "================================================");
                    Log.i("", "\n" + "[ Receive_Thread,54 :: ?????? ?????? ????????? ]  ");
                    Log.w("//===========//", "================================================");
                    break;
                }

                /* ????????? ??????????????? */
                String actName = getNowUseActivity(context);
                Log.w("//===========//", "================================================");
                Log.i("", "\n" + "[ Receive_thread_?????? ?????? ???????????? :: " + actName + "]");
                Log.w("//===========//", "================================================");

                /* ?????? ?????? ???????????? */
                switch (actName) {
                    case "com.example.wayout_ver_01.Activity.Chat.ChatRoom":
                        Log.w("//===========//", "================================================");
                        Log.i("", "\n" + "[ Receive_Thread,76 ????????? ?????? :: ChatRoom ?????? broad ]  ");
                        Log.w("//===========//", "================================================");
                        Intent intent2 = new Intent("msgReceive_room");
                        intent2.putExtra("msg", request);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
                        break;

                    default:
                        Log.w("//===========//", "================================================");
                        Log.i("", "\n" + "[ Receive_Thread,67 ????????? ?????? :: HomeActivity ?????? broad ]  ");
                        Log.w("//===========//", "================================================");
                        Intent intent = new Intent("msgReceive_home");
                        intent.putExtra("msg", request);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                        if (!code.equals("in")) {
                            if (!code.equals("out")) {
                                /* ?????????????????? ?????? ?????? ?????? ?????? */
                                NotificationCompat.Builder builder = null;
                                /* ??????????????? ????????? ??????????????? Chennal ??? ??? ??????????????? */
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

                                    /* ?????? ????????? ?????? ???????????? */
                                    Intent intent1 = new Intent(context, ChatRoom.class);
                                    intent1.putExtra("room_id", title);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 101, intent1, FLAG_CANCEL_CURRENT);

                                    Log.e("//===========//", "================================================");
                                    Log.e("", "\n" + "[ Noti code : " + code + "  ]");
                                    Log.e("", "\n" + "[ Noti message : " + message + "  ]");
                                    Log.e("", "\n" + "[ Noti writer : " + writer + "  ]");
                                    Log.e("", "\n" + "[ Noti title : " + room_name + "  ]");
                                    Log.e("//===========//", "================================================");

                                    /* builder ??? ?????? ????????? ???????????? ?????? */
                                    builder.setContentTitle(room_name)
                                            .setContentText(message)
                                            .setSmallIcon(R.drawable.exit)
                                            .setAutoCancel(true)
                                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                                            .setContentIntent(pendingIntent)
                                            .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE);
                                    if (noti_id == temp) {

                            /* builder ??? build() ??? ?????? Notification ????????? ????????????,
                               ????????? ?????? ?????? ?????? NotificationManagerCompat.notify() ???
                               ???????????? ????????? ?????? ID??? ?????? ?????? */
                                        Notification notification = builder.build();
                                        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                                        notificationManagerCompat.notify(noti_id, notification);
                                        temp = noti_id;
                                    } else {
                                        notificationManager.cancel(temp);
                                        temp = noti_id;
                                 /* builder ??? build() ??? ?????? Notification ????????? ????????????,
                                    ????????? ?????? ?????? ?????? NotificationManagerCompat.notify() ???
                                    ???????????? ????????? ?????? ID??? ?????? ?????? */
                                        Notification notification = builder.build();
                                        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                                        notificationManagerCompat.notify(noti_id, notification);
                                    }
                                }
                            }
                        }
                }
            }

        } catch (
                IOException e) {
            e.printStackTrace();
        } finally {
            Log.w("//===========//", "================================================");
            Log.i("", "\n" + "[ Receive_Thr :: Receive Thread ?????? ]");
            Log.w("//===========//", "================================================");
        }

    }

    private static String makeJson(String code, String room, String name, String message, String image, String date, String type) {
        /* ????????? ?????? ????????? ????????? Json ?????? */
        Gson gson = new Gson();
        DTO_message chat = new DTO_message(code, room, name, message, image, date, type);
        String jsonStr = gson.toJson(chat);
        System.out.println("ChatRoom_120 // ????????? ????????? " + jsonStr);
        return jsonStr;
    }

    /* DTO => JsonString ?????? ????????? ????????? ?????? */
    private static String DtoToJson(DTO_message item) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(item);
        System.out.println("ChatRoom_ // ????????? ????????? : " + jsonStr);
        return jsonStr;
    }

    // json String => DTO ??? ????????????
    private static DTO_message makeDTO(String json) {
        Gson gson = new Gson();
        DTO_message message = gson.fromJson(json, DTO_message.class);
        return message;
    }

    // ?????? ??????
    private static void consoleLog(String log) {
        System.out.println(log);
    }

    // TODO [?????? ???????????? ????????? ???????????? ??? ??????]
    public static String getNowUseActivity(Context mContext) {

        /**
         * ?????? : [?????? ??????????????? ?????? ???????????? ?????? ??????]
         * getClass().getName()
         * */

        // [?????? ?????? ?????? ?????? ?????? ?????? ??????]
        String returnActivityName = "";
        try {
            ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

            String className = "";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                className = String.valueOf(manager.getAppTasks().get(0).getTaskInfo().topActivity.getClassName());
            } else {
                List<ActivityManager.RunningTaskInfo> info = manager.getRunningTasks(1);
                ComponentName componentName = info.get(0).topActivity;
                className = componentName.getClassName();
            }


            // [?????? ?????? ????????? ?????? ??????]
            returnActivityName = className;
        } catch (Exception e) {
            //e.printStackTrace();

            Log.e("//===========//", "================================================");
            Log.i("", "\n" + "[C_Util >> getNowUseActivity() :: ?????? ?????? ?????? ????????? ???????????? ??????]");
            Log.i("", "\n" + "[catch [??????] :: " + String.valueOf(e.getMessage()) + "]");
            Log.e("//===========//", "================================================");
        }

        // [?????? ?????? ?????? ?????? ??????]
        return returnActivityName;
    }

}

