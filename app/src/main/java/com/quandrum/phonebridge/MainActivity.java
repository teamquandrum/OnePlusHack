package com.quandrum.phonebridge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {


    public static final String MyPREFERENCES = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Check if the user is already registered
         */

        SharedPreferences.Editor editor = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE).edit();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        editor.putInt("height", height);
        editor.putInt("width", width);
        editor.apply();

        if(this.getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE).getString("myid",null)==null)
            startActivity(new Intent(this, GcmActivity.class));
    }

    public void ask(View view)
    {
        startActivity(new Intent(MainActivity.this, AskActivity.class));
        //finish();
    }

    public void answer(View view)
    {
        startActivity(new Intent(MainActivity.this, AnswerActivity.class));
        //finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
