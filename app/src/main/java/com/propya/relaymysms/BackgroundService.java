package com.propya.relaymysms;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class BackgroundService extends Service {

    BroadcastReceiver receiver;

    public BackgroundService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        smsBroadcast();

        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "main")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Reading messages")
                .setContentText("Redirecting messages to specified webhook")
                .setPriority(NotificationCompat.PRIORITY_MIN);

        startForeground(58,builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra("stopMe", false)) {
            stopForeground(true);
            if(receiver!=null){
                unregisterReceiver(receiver);
                receiver = null;
            }
            return START_STICKY;
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void smsBroadcast() {
        String broadCastString = "android.provider.Telephony.SMS_RECEIVED";

        IntentFilter filter = new IntentFilter(broadCastString);
        filter.setPriority(Integer.MAX_VALUE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                Toast.makeText(context, "Got message", Toast.LENGTH_SHORT).show();
//                Log.i("TAG", "Intent recieved: " + intent.getAction());

                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    final SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    if (messages.length > -1) {
                        parseMessage(messages[0].getOriginatingAddress(), messages[0].getMessageBody());
                    }
                }

            }
        };

        registerReceiver(receiver, filter);
    }

    private void parseMessage(String originatingAddress, String messageBody) {
        SharedPreferences appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        String webhook = appPrefs.getString(Constants.WEB_HOOK_TEXT, null);

        if(webhook==null)
            return;

        if(appPrefs.getString(Constants.KEYWORD_TEXT,null)==null && !appPrefs.getBoolean(Constants.CONTAINS_KEYWORD,true)){
            sendMessage(originatingAddress,messageBody);
        }else{
            String keyWord = appPrefs.getString(Constants.KEYWORD_TEXT, null);
            if(appPrefs.getBoolean(Constants.KEYWORD_START,false)){
                if(messageBody.startsWith(keyWord)){
                    sendMessage(originatingAddress,messageBody);
                }
            }else{
                if(messageBody.contains(keyWord)){
                    sendMessage(originatingAddress,messageBody);
                }
            }
        }
    }

    private void sendMessage(String originatingAddress, String messageBody) {
        SharedPreferences appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String url = appPrefs.getString(Constants.WEB_HOOK_TEXT, null);
        if(url==null)
            return;
        Request request;
        Response.ErrorListener errorListener = error -> Toast.makeText(BackgroundService.this, "Error calling webhook", Toast.LENGTH_SHORT).show();
        if(appPrefs.getBoolean(Constants.POST_GET,false)){
//            post request
            JSONObject data = new JSONObject();

            try {
                data.put("phoneNumber",originatingAddress);
                data.put("messageBody",messageBody);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            request = new JsonObjectRequest(Request.Method.POST, url,data, response -> {
                Toast.makeText(this, "Message Sent to webhook", Toast.LENGTH_SHORT).show();
            },errorListener);
        }else{
            request = new StringRequest(Request.Method.GET, url, response -> {
                Toast.makeText(this, "Webhook called", Toast.LENGTH_SHORT).show();
            },errorListener);
        }
        Volley.newRequestQueue(this).add(request);

    }


    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Main";
            String description = "All notis";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("main", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}