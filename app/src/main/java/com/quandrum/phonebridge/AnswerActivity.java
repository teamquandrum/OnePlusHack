package com.quandrum.phonebridge;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;


public class AnswerActivity extends ActionBarActivity {

    String title[];
    String body[];
    String askerid[];
    String qid[];
    String myid;
    Context context;
    SharedPreferences sharedpreferences;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "CardViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);

        sharedpreferences = getSharedPreferences(MainActivity.MyPREFERENCES, Context.MODE_PRIVATE);
        myid = sharedpreferences.getString("myid", "12");

        //Parse.initialize(this, "5WyWNRtjgkV6iIT22R0yp4MEmLRLtYKq8C5vcoaF", "pmLLUFNkSdQL9qskqYJfZvGByboygsp5NZsAyQRZ");

        context = this;

        mRecyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(AnswerActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ArrayList results = new ArrayList<DataObject>();
        results.add(0, new DataObject("Please wait", ""));

        mAdapter = new MyRecyclerViewAdapter(results);
        mRecyclerView.setAdapter(mAdapter);

        //Register with the Server
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("controller", "question");
        params.put("action", "getallquestions");


        client.get(Transfer.URL, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                Toast.makeText(getApplicationContext(), "Sending Request",
                        Toast.LENGTH_SHORT).show();
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                //Toast.makeText(getApplicationContext(), new String(response),
                        //Toast.LENGTH_SHORT).show();

                try {
                    //Log.e("JSON", new String(response));
                    String content = new String(response);
                    JSONObject json = new JSONObject(content);
                    JSONArray questions = json.getJSONArray("result");

                    title = new String[questions.length()];
                    body = new String[questions.length()];

                    askerid = new String[questions.length()];
                    qid = new String[questions.length()];

                    for(int i=0; i<questions.length();i++)
                    {
                        JSONObject q = questions.getJSONObject(i);
                        title[i]=q.getString("url");
                        body[i]=q.getString("content");
                        askerid[i]=q.getString("askerid");
                        qid[i]=q.getString("qid");
                        System.out.println(title[i]);
                    }
                } catch(Exception e){
                    e.printStackTrace();
                } finally {

                    mRecyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
                    mRecyclerView.setHasFixedSize(true);
                    mLayoutManager = new LinearLayoutManager(AnswerActivity.this);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mAdapter = new MyRecyclerViewAdapter(getDataSet());
                    System.out.println(getDataSet());

                    ScaleInAnimationAdapter alphaAdapter = new ScaleInAnimationAdapter(mAdapter);
                    //AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(mAdapter);
                    alphaAdapter.setDuration(700);
                    alphaAdapter.setInterpolator(new OvershootInterpolator());
                    mRecyclerView.setAdapter(alphaAdapter);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Toast.makeText(getApplicationContext(), e.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e("Failure", "fail");
            }

            @Override
            public void onRetry(int retryNo) {
                Toast.makeText(getApplicationContext(), "Retrying",
                        Toast.LENGTH_SHORT).show();
                // called when request is retried
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAdapter!=null) {
            ((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter
                    .MyClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    Log.i(LOG_TAG, " Clicked on Item " + position);
/*
                    Intent i = new Intent(AnswerActivity.this, AcceptActivity.class);
                    i.putExtra("title", title[position]);
                    i.putExtra("body", body[position]);
                    i.putExtra("qid", qid[position]);
                    i.putExtra("askerid", askerid[position]);
                    Transfer.askerId=askerid[position];
                    startActivity(i);
                    finish();
                    */
                    registerNow(qid[position], myid);
                }
            });
        }
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

    private ArrayList<DataObject> getDataSet() {
        ArrayList results = new ArrayList<DataObject>();
        /*
        for (int index = 0; index < 5; index++) {

            DataObject obj = new DataObject("Some Primary Text " + index,
                    "Secondary " + index);
            results.add(index, obj);

            DataObject obj = new DataObject("Girish Bathala " + index,
                    "Idli: 2\nDosa: 2\nPaneer Paratha: 1 " + index);
            results.add(index, obj);
        }
    */
        /*
        DataObject obj = new DataObject("Food Point", "");
        results.add(0, obj);
        results.add(1, obj);results.add(2, obj);results.add(3, obj);results.add(4, obj);results.add(5, obj);results.add(6, obj);results.add(7, obj);results.add(8, obj);results.add(9, obj);results.add(10, obj);results.add(11, obj);
        */
        //DataObject obj4 = new DataObject("Chandar",
        //      "Vada: 1\nGobi Paratha: 2");
        //results.add(3, obj4);
        for (int index = 0; index < title.length; index++) {

            DataObject obj = new DataObject(body[index], title[index]);
            results.add(index, obj);
        }

        return results;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_answer, menu);
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
