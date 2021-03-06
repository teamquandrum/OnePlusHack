package com.quandrum.phonebridge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class TakeScreenshot extends Activity {

    /**
     * If you are taking a screenshot, it means you are the asker
     * We need to pass the helper-id to this class
     */

    private static final String TAG = TakeScreenshot.class.getName();
    private static final int REQUEST_CODE = 100;
    private static MediaProjection MEDIA_PROJECTION;
    private static String STORE_DIRECTORY;
    private static int IMAGE_NO;
    private int IMAGES_PRODUCED;
    private MediaProjectionManager mProjectionManager;
    private ImageReader mImageReader;
    private Handler mHandler;
    private int mWidth;
    private int mHeight;
    private SharedPreferences sharedpreferences;
    private String myid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);

        //Parse.enableLocalDatastore(this);

        sharedpreferences = getSharedPreferences(MainActivity.MyPREFERENCES, Context.MODE_PRIVATE);
        myid = sharedpreferences.getString("myid", "12");


        // call for the projection manager
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // start projection
        startProjection();
        /*
        Button startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startProjection();
            }
        });

        // stop projection
        Button stopButton = (Button)findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                stopProjection();
            }
        });
        */

        // start capture handling thread
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            MEDIA_PROJECTION = mProjectionManager.getMediaProjection(resultCode, data);

            if (MEDIA_PROJECTION != null) {
                STORE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/OnePlus/";
                File storeDirectory = new File(STORE_DIRECTORY);
                if (!storeDirectory.exists()) {
                    boolean success = storeDirectory.mkdirs();
                    if (!success) {
                        Log.e(TAG, "failed to create file storage directory.");
                        return;
                    }
                }

                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int density = metrics.densityDpi;
                int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                mWidth = size.x;
                mHeight = size.y;

                mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
                MEDIA_PROJECTION.createVirtualDisplay("screencap", mWidth, mHeight, density, flags, mImageReader.getSurface(), null, mHandler);
                mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mHandler);
            }
        }
        //finish();
    }

    private void startProjection() {
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private void stopProjection() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (MEDIA_PROJECTION != null) MEDIA_PROJECTION.stop();
            }
        });
    }

    /**
     * Uploads images to my Python Server
     *
     * @param path: path to the image
     * @return: url of the uploaded image
     */
    private void uploadImage(String path) {
        final String[] myurl = new String[1];
        SyncHttpClient client = new SyncHttpClient();
        RequestParams params = new RequestParams();
        try {
            params.put("file", new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        client.post("http://amanachpal.pythonanywhere.com", params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String url="";
                // called when response HTTP status is "200 OK"
                Log.e("Success:", new String(response));
                try {
                    JSONObject myobj = new JSONObject(new String(response));
                    Log.e("Yay", myobj.getString("url"));
                    url = myobj.getString("url");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mainUpload(url);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                if (e != null)
                    Log.e("Failure:", e.toString());
                if (errorResponse != null)
                    Log.e("Failure:", new String(errorResponse));
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }

    void mainUpload(String url) {

        try {
/*
                        file.save();
                        url = file.getUrl();
                        Log.e("URL", url);
                        po.save();
*/
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();

            params.put("controller", "gcm");
            params.put("action", "sendimageurlmessage");
            params.put("url", url);
            params.put("askerid", myid);
            params.put("helperid", Transfer.helperId);


            client.get(Transfer.URL, params, new AsyncHttpResponseHandler() {

                @Override
                public void onStart() {
                    Toast.makeText(getApplicationContext(), "Sending Image",
                            Toast.LENGTH_SHORT).show();
                    // called before request is started
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    // called when response HTTP status is "200 OK"
                    Toast.makeText(getApplicationContext(), new String(response),
                            Toast.LENGTH_SHORT).show();
                    Log.e("success", new String(response));
                    //startActivity(new Intent(AskActivity.this, WaitingActivity.class));
                    //finish();
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

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Register with the Server
        }

    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            stopProjection();
            /*try {
                Thread.sleep(3000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            Image image = null;
            FileOutputStream fos = null;
            Bitmap bitmap = null;

            try {
                image = mImageReader.acquireLatestImage();
                if (image != null && IMAGES_PRODUCED < 1) {


                    IMAGES_PRODUCED++;
                    IMAGE_NO++;
                    Log.e(TAG, "captured image: " + IMAGES_PRODUCED);
                    final Image.Plane[] planes = image.getPlanes();
                    final ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mWidth;

                    // create bitmap
                    bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    // write bitmap to a file
                    String path = STORE_DIRECTORY + "/myscreen_" + IMAGE_NO + ".png";
                    fos = new FileOutputStream(path);
                    bitmap.compress(CompressFormat.JPEG, 50, fos);

                    Toast.makeText(TakeScreenshot.this, path, Toast.LENGTH_SHORT).show();


/*

                    FileInputStream streamIn = new FileInputStream(path);
                    Bitmap bitmap2 = BitmapFactory.decodeStream(streamIn);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap2.compress(Bitmap.CompressFormat.PNG, 20, stream);
                    byte[] data = stream.toByteArray();


                    ParseObject po = new ParseObject("Image");
                    po.save();
                    // Create the ParseFile
                    ParseFile file = new ParseFile("image.png", data);
                    po.put("ImageFile", file);
*/

                    uploadImage(path);


                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    bitmap.recycle();
                }

                if (image != null) {
                    image.close();
                }
            }

        }
    }
}