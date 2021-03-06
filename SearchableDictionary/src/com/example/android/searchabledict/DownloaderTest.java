package com.example.android.searchabledict;

/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 */
import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import wikem.chris.R;

/*todo:
 * will need to delete old data. not sure if downloadFile is appending old file. don't think so?
 *  might as well make public static File? no?
 */

public class DownloaderTest extends Activity {
	
    private final static String FILE_CONFIG_URL = "http://dl.wikem.org/files/info.xml";
	//private final static String FILE_CONFIG_URL = "http://christopherkim.bol.ucla.edu/info.xml";
    private final static String CONFIG_VERSION = "4.0"; //i think just matching the config file

    public final static String DATA_PATH = "/Android/data/wikem.chris/files/wikEM_data/"; //directory to store device
    public final static String BACKUP_PATH = "/Android/data/wikem.chris/files/"; //different than normal datapath...
    public static final String DESTINATION_FILE = "wikem_dest";
    private final static String USER_AGENT = "wikem Downloader Activity"; //user agent string when fetching urls
    public  static String LAST_UPDATED = ""; //jsut a default value... will be replaced
    public static final String SRC_OF_DATA =  "http://dl.wikem.org/files/db-update.xml";
   // public static final String SRC_OF_DATA = "http://dl.wikem.org/database.xml";
    
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //also pass along last_updated
        
        LAST_UPDATED = Long.toString(SearchableDictionary.EPOCH);
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new AndroidRuntimeException(
                "External storage (SD-Card) not mounted");
        } 
        File appDbDir = new File(
            Environment.getExternalStorageDirectory(),DATA_PATH);
        
        Log.d("DOWNLOADER TEST" , "THE FILE IS " + appDbDir.toString());
        if (! DownloaderActivity.ensureDownloaded(this,
                getString(R.string.app_name), FILE_CONFIG_URL,
                //CONFIG_VERSION, DATA_PATH, USER_AGENT, LAST_UPDATED, SRC_OF_DATA, DESTINATION_FILE)) {
                CONFIG_VERSION, appDbDir.toString(), USER_AGENT, LAST_UPDATED, SRC_OF_DATA, DESTINATION_FILE)) {
            return;
        }
        setContentView(R.layout.dmain);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = true;
        int id = item.getItemId();

        if (id == R.id.menu_main_download_again) {
            downloadAgain();
        } else {
            handled = false;
        }

        if (!handled) {
            handled = super.onOptionsItemSelected(item);
        }
        return handled;
    }

    private void downloadAgain() {
    	File appDbDir = new File(
                Environment.getExternalStorageDirectory(),DATA_PATH);
        DownloaderActivity.deleteData(appDbDir.toString());
        startActivity(getIntent());
        finish();
    }
    
}