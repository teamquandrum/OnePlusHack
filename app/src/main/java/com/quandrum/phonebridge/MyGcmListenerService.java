/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quandrum.phonebridge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle extras) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        //URL => Download screenshot => I am the helper
        if(extras.containsKey("url")){
            if(ImageViewer.activity == null) {
                Intent shady = new Intent(getApplicationContext(), ImageViewer.class);
                shady.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shady.putExtra("url", extras.getString("url"));
                shady.putExtra("askerid", extras.getString("askerid"));
                shady.putExtra("helperid", extras.getString("helperid"));
                startActivity(shady);
            }
            else {
                Transfer.url = extras.getString("url");
                ImageViewer.faHandler.sendEmptyMessage(1);
            }
        }

        //x => X and Y co-ordinates of where he needs to tap, i.e. I am the asker
        else if(extras.containsKey("x")){
            Intent shady = new Intent(getApplicationContext(),ServiceFloating.class);
            shady.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Transfer.x = Double.parseDouble(extras.getString("x"));
            Transfer.y = Double.parseDouble(extras.getString("y"));
            Log.e("GCM X", ":" + extras.getString("x"));
            Log.e("GCM Y", ":" + extras.getString("y"));
            Transfer.helperId = extras.getString("helperid");
            Transfer.askerId = extras.getString("askerid");
            stopService(shady);
            startService(shady);
        }

        //Else it needs to send the person to Home Screen to take a screenshot & start file monitor, i.e. I am the asker
        else {
            //Assured to be called if I am being helped
            Transfer.askerId = extras.getString("askerid");
            Transfer.helperId = extras.getString("helperid");
            Intent shady = new Intent(getApplicationContext(), Creator.class);
            shady.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(shady);

        }
        Log.e("GCMListener", "we been tickled lads");
    }



}