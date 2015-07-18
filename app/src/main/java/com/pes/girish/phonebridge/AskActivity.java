package com.pes.girish.phonebridge;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.devspark.appmsg.AppMsg;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.apache.http.Header;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class AskActivity extends ActionBarActivity {

    ImageButton b1;
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp3";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private MediaRecorder recorder = null;
    private int currentFormat = 0;
    private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4,             MediaRecorder.OutputFormat.THREE_GPP };
    private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP };

    Context context;

    int flag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask);

        context = this;

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "5WyWNRtjgkV6iIT22R0yp4MEmLRLtYKq8C5vcoaF", "pmLLUFNkSdQL9qskqYJfZvGByboygsp5NZsAyQRZ");

        b1=(ImageButton)findViewById(R.id.imgbtn);
        b1.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        AppLog.logString("Start Recording");
                        startRecording();
                        break;
                    case MotionEvent.ACTION_UP:
                        AppLog.logString("Stop Recording");
                        stopRecording();
                        flag=1;
                        break;
                }
                return false;
            }
        });
    }

    public void submit(View view)
    {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);
        String fileName = "recording";
        ParseObject testObject = new ParseObject("Audio");
        File f = new File(file.getAbsolutePath() + "/" + fileName + file_exts[currentFormat]);
        uploadAudioToParse(f, testObject, "AudioFile");
    }
    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }
        String fileName = "recording";
        return (file.getAbsolutePath() + "/" + fileName + file_exts[currentFormat]);
    }
    private void startRecording(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats[currentFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getFilename());
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private MediaRecorder.OnErrorListener errorListener = new        MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Warning: " + what + ", " + extra);
        }
    };
    private void stopRecording(){
        if(null != recorder){
            recorder.stop();
            recorder.reset();
            recorder.release();

            recorder = null;
        }
    }


    private ParseObject uploadAudioToParse(File audioFile, ParseObject po, String columnName){

        if(audioFile != null){
            Log.d("EB", "audioFile is not NULL: " + audioFile.toString());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedInputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(audioFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int read;
            byte[] buff = new byte[1024];
            try {
                while ((read = in.read(buff)) > 0)
                {
                    out.write(buff, 0, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] audioBytes = out.toByteArray();

            // Create the ParseFile
            ParseFile file = new ParseFile(audioFile.getName() , audioBytes);
            po.put(columnName, file);

            String url = null;
            // Upload the file into Parse Cloud
            try {
                file.save();
                url = file.getUrl();
                Log.e("URL", url);
                po.save();
            } catch (ParseException e) {
                e.printStackTrace();
            } finally {
                //Register with the Server
                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();

                params.put("controller", "question");
                params.put("action", "newQuestion");
                if(flag==1)
                    params.put("url", url);
                else
                    params.put("url", "");

                EditText editText = (EditText)findViewById(R.id.editText);


                if(editText.getText().length()==0)
                    params.put("content", "");
                else
                    params.put("content", editText.getText().toString());

                params.put("askerid", "10");


                client.get("http://10.0.0.21:8080/oneplus/index.php/manager", params, new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        Toast.makeText(getApplicationContext(), "Sending Question",
                                Toast.LENGTH_SHORT).show();
                        // called before request is started
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        // called when response HTTP status is "200 OK"
                        Toast.makeText(getApplicationContext(), new String(response),
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(AskActivity.this, WaitingActivity.class));
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
            }

        }
        return po;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ask, menu);
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
