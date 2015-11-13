package com.quandrum.phonebridge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;


public class AcceptActivity extends ActionBarActivity {

    SharedPreferences sharedpreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept);

        Intent intent = getIntent();

        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("body");
        String qid = intent.getStringExtra("qid");
        String askid = intent.getStringExtra("askerid");

        sharedpreferences = getSharedPreferences(MainActivity.MyPREFERENCES, Context.MODE_PRIVATE);
        String userid = sharedpreferences.getString("myid", "12");
        registerNow(qid, userid);
    }

    public void registerNow(final String qid, String userid) {


        //Register with the Server
        AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params = new RequestParams();

        params.put("controller", "offer");
        params.put("action", "newOffer");
        params.put("qid", qid);
        params.put("helperid", userid);



        client.get(Transfer.URL, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                Toast.makeText(getApplicationContext(), "Sending Question",
                        Toast.LENGTH_SHORT).show();
                Log.e("req", "" + qid);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                Toast.makeText(getApplicationContext(), new String(response),
                        Toast.LENGTH_SHORT).show();
                Log.e("fail", new String(response));
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Toast.makeText(getApplicationContext(), e.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e("fail", e.toString());
            }

            @Override
            public void onRetry(int retryNo) {
                Toast.makeText(getApplicationContext(), "Retrying",
                        Toast.LENGTH_SHORT).show();
                // called when request is retried
            }
        });

    }
}
