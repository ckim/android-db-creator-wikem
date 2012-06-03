/*
f * Copyright (C) 2010 The Android Open Source Project
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

//TODO clase
package com.example.android.searchabledict;

 //TODO CK MADE THIS 09092011, this is a debug version ... USES OLD XML AND REBUILDS A DB
//TODO 1)add the shared prefs "1st opened bool" 1)closeAllAndExit
//delete viewbackup and restore and remove from manifest

import java.io.File;
import java.sql.Date;

import wikem.chris.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * The main activity for the dictionary.
 * Displays search results triggered by the search dialog and handles
 * actions from search suggestions.
 */
/*
 * 
<!--  Shared prefs: -->
<!-- int max -> number of entries pulled from xml -->
<!--boolean db_loaded -> if not loaded all entires on dbloadactivity will be false...either force reload or just toast and warn   -->
<!--  boolean sucessfully_updated  -> basically always false, only true to throw toast that says 'sucessfully updated'-->
<!--  long = epoch -> the time stamp of update pulled form xml -->

 */
public class SearchableDictionary extends Activity {

    private TextView mTextView;
    private ListView mListView;
    public static boolean firstOpened =true; //for the first time toast 
    public static final String PREFS_NAME = "MyPrefsFile";
    public static long EPOCH=1288369494 ; //initial value.. will be replaced
    public static String [] headers;
    public static String query;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * TODO FIX the way it initializes the program
         * 
         */
       
       /*as this is main activity per the manifest file. will do some housekeeping first
        *  
        */
        try {
 			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
 	           throw new AndroidRuntimeException(
 	               "External storage (SD-Card) not mounted");}
 	       } catch (Exception e) { Log.d("SEARCHDICT on create", " caugth an exception. crap.");}
 	     
 	       
 	  
 	       
 	      
 	       
 	       File dbDir = new File(
 	       Environment.getExternalStorageDirectory(),DownloaderTest.DATA_PATH);
       // String dataPath = DownloaderTest.DATA_PATH;
   	 	String dest = DownloaderTest.DESTINATION_FILE;   	 
   	 	File destFile = new File(dbDir, dest);
   	 	if (!destFile.exists()){
   	 		//if the destination file doesn't exist . assume first time use and override any old preferences..
   	 		//unfortunately if sdcard is pulled out or changed will go through this method even though .db file exists in memory
   	 	 SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);
         SharedPreferences.Editor editor = settings.edit();
         editor.putLong("epoch",EPOCH ); //random old epoch such that will download newer
         // Commit the edits!
         editor.commit();
         
         
   	 		Toast toast = Toast.makeText(getApplicationContext(), R.string.first_instructions, Toast.LENGTH_LONG);
   	 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
   	 		toast.show();
   	 		startActivity(new Intent(this, DownloaderTest.class));
   	 		firstOpened = false; 	 		
   	 	
   	 	}
   	 	else{
   	 		if(firstOpened){
   	 			//opened program. data present. now load the last updated such that doesn;t always need unnecceary updats
   	 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
   	        EPOCH = settings.getLong("epoch", 1288369494); //the 128.. is just a dfault value if c
   	 			
   	 		Toast toast = Toast.makeText(getApplicationContext(), R.string.search_instructions, Toast.LENGTH_LONG);
   	 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
   	 		toast.show();
   	 		firstOpened = false;   	 		
   	 		
   	 		}
   	 	}
    	
   	 	
   	 	checkIfNeedDBRebuild(); toastIfJustSuccessfullyUdated();
        setContentView(R.layout.main);

        mTextView = (TextView) findViewById(R.id.text);
        mListView = (ListView) findViewById(R.id.list);
        
   /* ok to make ugly, imho useless, category thing 
    *      
    */
        String []categories=  getResources().getStringArray(R.array.category_array);
        headers = getResources().getStringArray(R.array.header_array);
        
        mListView.setAdapter(  new ArrayAdapter<String>(this, R.layout.list_item, categories));
        
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                  // When clicked, show a toast with the TextView text
                   Log.d("SAERCHDICT", "clicked on list:" +((TextView) view).getText());
                   //string?
                   String cat = (String) ((TextView) view).getText();
              //     showCategories(cat);
                   Intent catIntent = new Intent(getApplicationContext(), CategoryActivity.class);
                   catIntent.putExtra("category", cat);
                   startActivity(catIntent);
                }
              });
        
/*part of Activity class
 * Intent	 getIntent() -> Return the intent that started this activity.
 */    Intent intent = getIntent();
       
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL); //call in onCreate method to enable type to search 
      
        /* ACTION VIEW:  * Display the data to the user.   * This is the most common action performed on data -- 
        * it is the generic action you can use on a piece of data to         * get the most reasonable thing to occur. 
        *    * */
        
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // handles a click on a search SUGGESTION; launches activity to show word
            Intent wordIntent = new Intent(this, WebWordActivity.class);
            wordIntent.setData(intent.getData());
            //here too... start for result
            startActivityForResult(wordIntent, 26); //again...arbitrary int 
            finish();
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
             query = intent.getStringExtra(SearchManager.QUERY);
            showResults(query);
        }
    }

    private void toastIfJustSuccessfullyUdated() {
		// TODO Auto-generated method stub
    	
    	SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
    	if (settings.getBoolean("successfully_updated", false)){ //default false
     		Toast toast = Toast.makeText(getApplicationContext(), "Successfully Updated WikEM", Toast.LENGTH_LONG);
   	 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
   	 		toast.show();
    	}
    	SharedPreferences.Editor editor = settings.edit();
 			editor.putBoolean("successfully_updated", false);
			editor.commit();
			// always set back to false so only true when just succeeded
	}

	private void checkIfNeedDBRebuild() {
		
		SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
    	//if (!settings.getBoolean("db_loaded", true)){ //ie. if not dbloaded...just set default to true //update: no longer used this bool
    	//	DictionaryDatabase.upgrade();
	/*	 Cursor cursor = DictionaryProvider.getEverything();
		 int count;
		 if (cursor ==null){ //ie no results 
			 count = 0;
			 Log.d("searchdict", "why is cursur null??");
		 }else{
			 
			   count = cursor.getCount();
			if ( settings.getInt("max", DbLoadActivity.max) != count){			 Log.d("searchdict", "why max !=count??");

    		Toast toast = Toast.makeText(getApplicationContext(), "Please press menu and re-update. Previous attempt interrupted", Toast.LENGTH_LONG);
   	 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
   	 		toast.show();
			}
			else{
				 Log.d("searchdict", "ok, cursor.count = max.. all is ok?");
			}
		} 
		 cursor.close();*/
	}

	/**
     * Searches the dictionary and displays results for the given query.
     * @param query The search query
     */
   
    
    private void showResults(String query) {

        Cursor cursor = managedQuery(DictionaryProvider.CONTENT_URI, null, null,
                                new String[] {query}, null);
     

        if (cursor == null) {
            // There are no results
            mTextView.setText(getString(R.string.no_results, new Object[] {query}));
        } else {
            // Display the number of results
            int count = cursor.getCount();
            String countString = //getResources().getQuantityString(R.plurals.search_results,
                                   // count, new Object[] {count, query});
            	" "; //TODO don't care
            mTextView.setText(countString);

            // Specify the columns we want to display in the result
            String[] from = new String[] { DictionaryDatabase.KEY_WORD, 
            		DictionaryDatabase.WIKEM_CATEGORY };

            // Specify the corresponding layout elements where we want the columns to go
            int[] to = new int[] { R.id.word, R.id.definition};

            // Create a simple cursor adapter for the definitions and apply them to the ListView
            SimpleCursorAdapter words = new SimpleCursorAdapter(this,
                                          R.layout.result, cursor, from, to);
            mListView.setAdapter(words);

		            // Define the on-click listener for the list items
		            mListView.setOnItemClickListener(new OnItemClickListener() {
		                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		                    // Build the Intent used to open WordActivity with a specific word Uri
		                    Intent wordIntent = new Intent(getApplicationContext(), WebWordActivity.class);
		                    Uri data = Uri.withAppendedPath(DictionaryProvider.CONTENT_URI,
		                                                    String.valueOf(id));
		  //         Log.d("SEARCHABLE DICT", "uri data is.............. " + data.toString());
		                    wordIntent.setData(data);
		                   // startActivity(wordIntent);
//so text queries have the searched terms highlighted...let webwordactivity to highlight
		                    wordIntent.putExtra("is_query", true); 
		                    startActivityForResult(wordIntent, 26);//arbitrary int, just used for position <-??wth is this?
		                }
		            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;
            case R.id.update: //menu item for updating
            	updateDb();
            	return true;
            case R.id.favorite:
            	displayFavs();
            	return true; 

 			case R.id.info:
 				displayInfo();
 				return true;
 			
 			case R.id.viewAll:
 				viewAll();
 				return true;
 
            default:
                return false;
        }
    }
    
    private void viewAll() {
 
    	startActivity(new Intent(this, ViewAllActivity.class));
    	//startActivity(new Intent(this, ViewBackupActivity.class));
    	//startActivity(new Intent(this, RestoreActivity.class));
    	finish();
    	
	}

	//get string of date to display info
    public String getUpdateDate(){
    	Date theLastUpdate = new Date(EPOCH * 1000);
    	return theLastUpdate.toString();
    }
    
    
   /* private void firstTimeSplash(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//String string = getString(R.string.first_splash);

    	builder.setMessage(getString(R.string.first_splash))
    	       .setCancelable(false)
    	       .setPositiveButton("Return to WikEM", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	               ///// dunno what put here MyActivity.this.finish();
    	        	   dialog.cancel();
    	           }
    	       });    	       
    	AlertDialog alert = builder.create();      
    	alert.show();
    }
    */
    
    private void displayInfo() {
		
		
		String lastUpdated = "Last update was: " + getUpdateDate() + " ";
		 
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String string = getString(R.string.info);

    	builder.setMessage(lastUpdated + string)
    	       .setCancelable(false)
    	       .setPositiveButton("Return to WikEM", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	               ///// dunno what put here MyActivity.this.finish();
    	        	   dialog.cancel();
    	           }
    	       });    	       
    	AlertDialog alert = builder.create();      
    	alert.show();
		
	}

	private void displayFavs() {
     	startActivity(new Intent(this, FavoriteActivity.class));
		
	}

	public void updateDb(){
    	
    	startActivity(new Intent(this, DownloaderTest.class));
    	finish();
    }
    
   
 
    

    


    @Override
        protected void onStop(){
           super.onStop();

          // We need an Editor object to make preference changes.
          // All objects are from android.context.Context
          SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
          SharedPreferences.Editor editor = settings.edit();
          editor.putLong("epoch", EPOCH);

          // Commit the edits!
          editor.commit();
        }
    
 


}

