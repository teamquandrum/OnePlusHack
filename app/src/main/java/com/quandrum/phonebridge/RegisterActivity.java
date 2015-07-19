package com.quandrum.phonebridge;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;


public class RegisterActivity extends ActionBarActivity {

    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        sharedPreferences = getSharedPreferences(MainActivity.MyPREFERENCES, Context.MODE_PRIVATE);

        String token = getIntent().getStringExtra("token");

        Log.e("token", token);
        String device = Devices.getDeviceName();
        sharedPreferences.edit().putString("device", device).apply();

        //Register with the Server
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("controller","user");
        params.put("name","OnePlusUser");
        params.put("action","newUser");
        params.put("gcmregid",token);
        params.put("model", device);

        client.get("http://a7ba0cec.ngrok.io/oneplus/index.php/manager", params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                Toast.makeText(getApplicationContext(), "Attempting registration.",
                        Toast.LENGTH_SHORT).show();
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                Toast.makeText(getApplicationContext(), "Registered successfully!",
                        Toast.LENGTH_SHORT).show();

                try {
                    JSONObject obj = new JSONObject(new String(response));
                    Log.e("JSON:",new String(response));
                    obj = obj.getJSONObject("result");
                    sharedPreferences.edit().putString("myid", obj.getString("userid")).apply();
                    Toast.makeText(getApplicationContext(),obj.getString("userid")+"",Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Toast.makeText(getApplicationContext(), e.toString(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRetry(int retryNo) {
                Toast.makeText(getApplicationContext(), "Retrying",
                        Toast.LENGTH_SHORT).show();
                // called when request is retried
            }
        });


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
