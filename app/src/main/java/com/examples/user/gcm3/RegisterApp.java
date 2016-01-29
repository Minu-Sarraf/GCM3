package com.examples.user.gcm3;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by User on 1/28/2016.//ID:358284482786//AIzaSyATWPS8YN-w5ucU9aC7olo4jwEPZMqz54o
 */
public class RegisterApp extends AsyncTask<Void, Void, String> {


    private static final String TAG = "GCMRelated";
    Context ctx;
    GoogleCloudMessaging gcm;
    String SENDER_ID = "730749874588";
    String regid = null;
    private int appVersion;

    public RegisterApp(Context ctx, GoogleCloudMessaging gcm, int appVersion) {
        this.ctx = ctx;
        this.gcm = gcm;
        this.appVersion = appVersion;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected String doInBackground(Void... arg0) {
        String msg = "";
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(ctx);
            }
            Log.d("doinbackgrnd", "");
            regid = gcm.register(SENDER_ID);
            msg = "Device registered, registration ID=" + regid;
            Log.e("check", msg);
            // You should send the registration ID to your server over HTTP,
            // so it can use GCM/HTTP or CCS to send messages to your app.
            // The request to your server should be authenticated if your app
            // is using accounts.
            sendRegistrationIdToBackend();

            // For this demo: we don't need to send it because the device
            // will send upstream messages to a server that echo back the
            // message using the 'from' address in the message.

            // Persist the regID - no need to register again.
            storeRegistrationId(ctx, regid);
        } catch (IOException ex) {
            msg = "Error :" + ex.getMessage();
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
            Log.d("doinbackgrnd", msg);
        }

        return msg;
    }

    private void storeRegistrationId(Context ctx, String regid) {
        final SharedPreferences prefs = ctx.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("registration_id", regid);
        editor.putInt("appVersion", appVersion);
        editor.commit();

    }


    private void sendRegistrationIdToBackend() {
        String url = null;
        try {
            url = "http://192.168.137.1/GCM1/register.php?regId=" + regid;

            String res = run(url, null, 0);
            Log.e("result", res);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Toast.makeText(ctx, "Registration Completed. Now you can see the notifications", Toast.LENGTH_SHORT).show();
    }

    public static OkHttpClient client;

    public static OkHttpClient getConnection() {
        if (client == null) {
            client = new OkHttpClient();
        }
        return client;
    }


    public static String run(String url, RequestBody body, int type) throws IOException {
        OkHttpClient client = getConnection();
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (type == 0) {
            builder.get();
        } else {
            builder.post(body);
        }
        Request request = builder.build();
        Response response = client.newCall(request).execute();
        String result = response.body().string();
        response.body().close();
        return result;
    }
}