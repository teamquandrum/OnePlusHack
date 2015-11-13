package com.quandrum.phonebridge;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;


public class ImageViewer extends Activity implements View.OnTouchListener {

    public static ImageView img;
    public static String askerid, helperid;

    public static ImageViewer activity;

    public static Handler faHandler;

    public static boolean active = false;
    float[] values;

    Bitmap bitmap;
    ProgressDialog pDialog;

    public static void downloadImage(String url, String aid, String hid) {
        askerid = aid;
        helperid = hid;
        Ion.with(img)
                .placeholder(R.drawable.floating2)
                .error(R.drawable.floating3)
                .load(url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        faHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                downloadImage(Transfer.url, askerid, helperid);
            }
        };
        Intent intent = getIntent();
        active = true;

        activity = this;

        String url = intent.getStringExtra("url");
        askerid = intent.getStringExtra("askerid");
        helperid = intent.getStringExtra("helperid");

        setContentView(R.layout.activity_image_view);
        img = (ImageView) findViewById(R.id.girupie);
        img.setImageResource(R.drawable.floating2);
        img.setOnTouchListener(this);
        //UrlImageViewHelper.setUrlDrawable(img, url);
        //new DownloadImageTask(img).execute(url);
        onPosty();
        Ion.with(img)
                .placeholder(R.drawable.floating2)
                .error(R.drawable.floating3)
                .load(url);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        activity = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        activity = null;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int[] viewCoords = new int[2];
        img.getLocationOnScreen(viewCoords);
        float imageX = (event.getX() - values[2]) / values[0];
        float imageY = (event.getY() - values[5]) / values[4];
        float dx = img.getMeasuredWidth();
        float dy = img.getMeasuredHeight();
        imageX = imageX / dx;
        imageY = imageY / dy;
        imageX = imageX - 0.125f;
        imageY = imageY - 0.125f;
        Toast.makeText(this, "X: " + imageX + "\nY: " + imageY, Toast.LENGTH_SHORT).show();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("controller", "gcm");
        params.put("action", "sendCoordMessage");
        params.put("askerid", askerid);
        params.put("helperid", helperid);
        params.put("x", imageX);
        params.put("y", imageY);


        client.get(Transfer.URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                Toast.makeText(getApplicationContext(), "Contacting Server!",
                        Toast.LENGTH_SHORT).show();
                // called before request is started
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Toast.makeText(ImageViewer.this, "success", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Toast.makeText(getApplicationContext(), e.toString(),
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            @Override
            public void onRetry(int retryNo) {
                Toast.makeText(getApplicationContext(), "Retrying",
                        Toast.LENGTH_SHORT).show();
                // called when request is retried
            }
        });
        return false;
    }

    public void onPosty() {
        values = new float[9];
        img.getMatrix().getValues(values);
        img.setOnTouchListener(ImageViewer.this);
    }
}
