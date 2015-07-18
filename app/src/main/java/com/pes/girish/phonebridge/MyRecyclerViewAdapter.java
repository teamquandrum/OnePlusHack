package com.pes.girish.phonebridge;

/**
 * Created by Girish on 07-07-2015.
 */

import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView
        .Adapter<MyRecyclerViewAdapter
        .DataObjectHolder> {
    private static String LOG_TAG = "MyRecyclerViewAdapter";
    private ArrayList<DataObject> mDataset;
    private static MyClickListener myClickListener;

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView label;
        TextView dateTime;
        ImageButton imageButton;

        public DataObjectHolder(final View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.textView);
            dateTime = (TextView) itemView.findViewById(R.id.textView2);
            Log.i(LOG_TAG, "Adding Listener");
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public MyRecyclerViewAdapter(ArrayList<DataObject> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_row, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {
        holder.label.setText(mDataset.get(position).getmText1());
        //holder.dateTime.setText(mDataset.get(position).getmText2());
/*
        ImageButton imageButton = (ImageButton) holder.itemView.findViewById(R.id.imageButton1);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.itemView.setBackgroundColor(Color.GREEN);
            }
        });
        holder.itemView.setBackgroundColor(Color.WHITE);

        ImageButton imageButton2 = (ImageButton) holder.itemView.findViewById(R.id.imageButton2);
        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.itemView.setBackgroundColor(Color.RED);
            }
        });
        holder.itemView.setBackgroundColor(Color.WHITE);
        */
        ImageButton imageButton2 = (ImageButton) holder.itemView.findViewById(R.id.imageButton);
        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    pos = position;
                    //new MyTask().execute(mDataset.get(position).getmText2());
                    Ion.with(holder.itemView.getContext())
                            .load(mDataset.get(position).getmText2())
                            .write(new File(Environment.getExternalStorageDirectory() + "/abc.mp3"))
                            .setCallback(new FutureCallback<File>() {
                                @Override
                                public void onCompleted(Exception e, File file) {
                                    // download done...
                                    // do stuff with the File or error
                                    Log.e("done", "done");
                                    MediaPlayer player = new MediaPlayer();
                                    try {
                                        player.setDataSource(Environment.getExternalStorageDirectory() + "/abc.mp3");
                                        player.prepare();
                                        player.start();
                                        Log.e("player", "playing");
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            });

                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        });
    }

    public void addItem(DataObject dataObj, int index) {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }

    private class MyTask extends AsyncTask<String, Integer, String> {

        MediaPlayer player;
        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Do something like display a progress bar
        }

        // This is run in a background thread
        @Override
        protected String doInBackground(String... params) {
            // get the string from params, which is an array
            String myString = params[0];

            // Do something that takes a long time, for example:
            for (int i = 0; i <= 100; i++) {

                // Do things

                // Call this to update your progress
                publishProgress(i);
            }
            player = new MediaPlayer();
            Log.e("url", mDataset.get(pos).getmText2());
            try {
                player.setDataSource(mDataset.get(pos).getmText2());
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return "this string is passed to onPostExecute";
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            player.start();
            // Do things like hide the progress bar or change a TextView
        }
    }
    int pos;
}