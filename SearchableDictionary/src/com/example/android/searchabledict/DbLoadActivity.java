package com.example.android.searchabledict;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import wikem.chris.R;

import android.app.Activity; 
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
 import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
 
 /*single activity during database load
  * called after successful download
  * called from DownloadAtivity.onSuccessfulDownload -> DictoinaryDatabase.upgrade() ->
  *  -> SQLopenhelper.onUpgrade -> SQLOpenHelper.onCreate() -> this
  * 
  * integer max, set in DownloaderActivity
  * 
  * How is the backup DB loaded
  *  	showAll() ->  final Cursor cursor = managedQuery(DictionaryProvider.BACKUP_URI, null, null,null, null);
  *  Dictionary provider: ->getBackup() returns a cursor -> [DictionaryDatabase] mDictionary.getBackup( columns);  ->
  *  DictionaryDatabase: ->queryExternaldB returns a cursor ->		ExternalSQLHelper e = new ExternalSQLHelper ("db.db");
		
		then queries the db...the db stays open. never closes.
  *  
  *  
	How is the backup selection loaded
	 		     here:	 final Cursor cursor2 = managedQuery(DictionaryProvider.BCONTENT_URI, null, rowId,null, null);
	 		     Dictionary provider: ->bsearch -> mdictionary.getbackupword
	 		     DictionaryDatabase: -> queryExternaldB

  * 
  * 6/2011
  * changed: initialize in oncreate 	        DictionaryDatabase.initializeDB();
  *  			DictionaryDatabase.closeExternalDB();


  */
public class DbLoadActivity extends Activity{

		static final int PROGRESS_DIALOG = 0;
	    Button button;
	    ProgressThread progressThread;
	    ProgressDialog progressDialog;
		private final String TAG = "dbLoadActivity";
	    public static boolean firstOpened =true; //for the first time toast  //don't think i need this
	    
	    
	    private ProgressBar myProgressBar;
	    private ListView lv;
		private TextView tv;
		private WebView wv;
		
		int factor;

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	        Integer checkIfNull = null; //so basically if not null do nothing
	        try { 
	          checkIfNull = Integer.valueOf(max);
	        }
	        catch (NumberFormatException e) {
	          // ...null so reload the max... 
	        	Log.e("DbLoadActivity","er...max is null" );
	        	SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
	 			max = settings.getInt("max", 613); //default 613 ? whatever.
	        }
	        
	   /*CK DEBUG ONLY
	        
	        change max manually
	        */
 	 			
	        max = 1200;
	        ////////////
	        
	        requestWindowFeature(Window.FEATURE_PROGRESS);

	        setContentView(R.layout.db_load_layout);
	        
	        
	        //initialize the backup db... once... then close 6/2011
	        DictionaryDatabase.initializeDB();

	        //mTextView = (TextView) findViewById(R.id.dbLoad_id);	
	        //gonna switch form dialog to progres bar
	       // showDialog(PROGRESS_DIALOG);
	        
	     // Request the progress bar to be shown in the title
	       // requestWindowFeature(Window.FEATURE_PROGRESS);
	     //   setContentView(R.layout.progressbar_1);
	      

	        myProgressBar = (ProgressBar) findViewById(R.id.progress_horizontal);
	        myProgressBar.setMax(max);
	        progressThread = new ProgressThread(handler);
	            progressThread.start();
	        /*
	         * The progress for the progress bar. Valid ranges are from 0 to 10000 (both inclusive). 
	         * If 10000 is given, the progress bar will be completely filled and will fade out. 
	         */
	        if(myProgressBar.getMax()!=0){
	        	float f = 10000/max;
	        	factor = Math.round(f);
	        	}
	        setProgressBarVisibility(true);
	      //  setSecondaryProgress(myProgressBar.getSecondaryProgress() * 100);
	            lv = (ListView) findViewById(R.id.list);
	 	        tv = (TextView) findViewById(R.id.text);
	 	        wv = (WebView) findViewById (R.id.wv1);
	 	        wv.setVisibility(View.INVISIBLE);
	 	        wv.setWebViewClient(new MyWebViewClient()); 
	 	        lv.setVisibility(View.INVISIBLE);
	 	        
 	 	      File dbDir = new File( Environment.getExternalStorageDirectory(),  DownloaderTest.BACKUP_PATH);
	 	      File backupDB = new File(dbDir, "db.db");
	 	      if (backupDB.exists()){ //ie. if a backup file even exists... ie a backup was made once upoon a time
	 	    	  Log.d( TAG, "ok, the backup db.db file is there...will hope to try to display");
		 	        lv.setVisibility(View.VISIBLE);

	 	    	  showAll();
	 	        }
	 	      else{
	 	    	  Log.d( TAG, "ok," + backupDB.toString() + " is not there");
					tv.setText(getString(R.string.first_use_thx) + getString(R.string.disclaimer));

	 	      }
	 	      
        }

       
 		public static long timestamp = 0;
 		public static int max;//fix this. is this really the best way to pass the max value? (from downloader activity)
 	
 		 
  		/*	
 		 protected Dialog onCreateDialog(int id) {
 	        switch(id) {
 	        case PROGRESS_DIALOG:
 	            progressDialog = new ProgressDialog(DbLoadActivity.this);
 	            progressDialog.setCancelable(false); //usually back button makes the progressdialog dissapear unnec..
 	            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 	            if (max>500){ //just in case max is null and throws an error
 	            	//if (max ==null){finish();}
 	               progressDialog.setMax(max);
 	            }
 	             	            progressDialog.setMessage("Building Wikem Database...");
 	            progressThread = new ProgressThread(handler);
 	            progressThread.start();
 	            return progressDialog;
 	        default:
 	            return null;
 	        }
 	    }		 
 		*/
 	    
 	    
 	   // Define the Handler that receives messages from the thread and update the progress
 	    final Handler handler = new Handler() {
 	        public void handleMessage(Message msg) {
 	            int total = msg.getData().getInt("total");
 	            myProgressBar.setProgress(total);
	            setProgress(total * factor);

 	        
 	                 
 	            
 	        }
 	    }; 
 	    
 	  private void backupDbToSD(){
 		 try {
 			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
 	           throw new AndroidRuntimeException(
 	               "External storage (SD-Card) not mounted");
 	       } 
 	       File dbDir = new File(
 	       Environment.getExternalStorageDirectory(),DownloaderTest.BACKUP_PATH); //my datapath location
 	       if (!dbDir.exists()) {
 	   			Log.d("dbloadact", "having to make datapath dir");
 	           dbDir.mkdirs();
 	       }
 			File data = Environment.getDataDirectory();
 			if (dbDir.canWrite()) {
 				String currentDBPath = "/data/wikem.chris/databases/dictionary";
	 			File currentDB = new File(data, currentDBPath);
	 			File backupDB = new File(dbDir, "db.db");
	 				 			
	 			if (currentDB.exists()) {
	 				Log.d("DBLOADACTIVITY-backupSD", " hooray, the currentDB exists");
	 				
		 			FileChannel src = new FileInputStream(currentDB).getChannel();
		 			FileChannel dst = new FileOutputStream(backupDB).getChannel();
		 			Log.d("DBLOADACTIVIY-backupSD", "SRC size is " + Long.toString(src.size()) );
		 			dst.transferFrom(src, 0, src.size());
		 			src.close();
		 			dst.close();
	 				}
	 			else{Log.d("DBLOADACTIVITY", " uhoh, the currentDB does NOT exists");}

 			}
 			else{ Log.d("DBLOADACT", " uh oh , can not write sd");}
 			} catch (Exception e) { Log.d("DBLOADACT", " caugth an exception. crap.");}
 		
 			SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
 		 	SharedPreferences.Editor editor = settings.edit();
 		 
 			editor.putBoolean("db_loaded", true);	//confirmation of complete db load...
 			editor.putBoolean("successfully_updated", true); //this is for recent update so _> toast
 			editor.commit();
 			
 			DictionaryDatabase.closeExternalDB(); //6/2011 clse db to ensure  no memory leaks
 			
 			startActivity(new Intent(this, SearchableDictionary.class)); //let's see if this helps
 	    finish(); //finish this activity here.
 	  }
 	   private class ProgressThread extends Thread {
 	        Handler mHandler;
 	        final static int STATE_DONE = 0;
 	        final static int STATE_RUNNING = 1;
 	        int mState;
 	        int total;
 	       
 	        ProgressThread(Handler h) {
 	            mHandler = h;
 	        }
 	       
 	        public void run() {
 	            mState = STATE_RUNNING;   
 	            total = 0;
 	            while (mState == STATE_RUNNING) {
		 	           try {
		                   loadWords();
		               } catch (IOException e) {
		                   throw new RuntimeException(e);
		               } catch (XmlPullParserException e) { Log.e("DBLOADACTIVITY", "couldn't load database...caught xmlpullparser excetion");
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		 	               
		 	            }
 	          }
 	        
 	        /* sets the current state for the thread,
 	         * used to stop the thread */
 	        public void setState(int state) {
 	            mState = state;
 	        }
 	        
 	 	  private void loadWords()throws IOException, XmlPullParserException{
 	 		  //wake lock during this part..
 	 		  Context c = getApplicationContext();
 	 		PowerManager pm = (PowerManager)c.getSystemService(
                    Context.POWER_SERVICE);
 	 		PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE,
                TAG);
 	 		wl.acquire();
// ...
 	 		
           	 Log.d(TAG, "Loading words from dbloadactivity");
           	if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                throw new AndroidRuntimeException(
                    "External storage (SD-Card) not mounted");
            } 
            File dataPath = new File(
                Environment.getExternalStorageDirectory(),            
                 DownloaderTest.DATA_PATH);
            
           	// String dataPath = DownloaderTest.DATA_PATH;
           	 String dest = DownloaderTest.DESTINATION_FILE;
           	 
           	 File destFile = new File(dataPath, dest);
           	 Log.d(TAG, "wikem destinatio files is " + destFile.toString());

           	 if (destFile.exists()){
	           	 FileInputStream fis = new FileInputStream(destFile);
	           	 
	           	 
	           	 XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	                factory.setNamespaceAware(true);
	                XmlPullParser xpp = factory.newPullParser();
	           	 
	                xpp.setInput(fis, "UTF-8");
	                int i =0; int counter =0;
	           	 String[] stringHolder = new  String[5];
	            	 Boolean isContent = false; //by default false. once within content bracket set to true to add buffer
	            	 StringBuffer contentStringBuffer = new StringBuffer(); // for return string of <html markup> nested within content
          	 
	 
	            	 try {
	    				xpp.next();
	    				int eventType = xpp.getEventType();
	    			     while (eventType != XmlPullParser.END_DOCUMENT)
	    			     {
	    			    	 if(eventType == XmlPullParser.START_DOCUMENT)
	    			   	    {
	    			   	    // nothing.
	    			         	Log.d(TAG, "xml loading now..");
	
	    			   	    }
	    			    	 
	    			 //now start the ugly else if parade...   	 
	    			    	
	    			   	    else if(eventType == XmlPullParser.START_TAG)
	    			   	    {
	    			   	    	if (xpp.getName().matches("folder"))
	    			   	    		{i=0;}
	    			   	    	if (xpp.getName().matches("name"))
	    			   	    		{i=1;}
	    			   	    	if (xpp.getName().matches("content"))
	    			   	    		{i=2;
	    			   	    		isContent = true;
	    			   	    		}
	    			   	    	if (xpp.getName().matches("last_update"))
	    			   	    		{i=3;}
	    			   	    	if (xpp.getName().matches("author"))
	    			   	    		{i=4	  ;  }			
	    			   	    	if (isContent == true) //all tags within content just add to one long string
	    			   	    	{	    	
	    			   	    		contentStringBuffer.append("<" + xpp.getName() + ">");			   	    						   	    	
	    			   	    	}
	    			   	    	
	    			        }
	    			   	 else if(eventType == XmlPullParser.TEXT) // nested if thens for text. if within content then just write to buffer
	    			  	    {
	    			  		  	  			  	    	
	    			  	    	if(isContent == true)
	    			  	    	{			  	    		
	    			  	    		contentStringBuffer.append(xpp.getText());
	
	    			  	    	}
	    			  	    	else { 
	    			  	    		if(xpp.isWhitespace()){
	    			  	    		Log.d("dbloadact", " hey the XML contains only whitespace?");
	    			  	    		}else{
	    			  	    			stringHolder[i] = xpp.getText();
	    			  	    			//FOR DEBUGGING
		    			  	    		if (i==1){
		    			  	    			Log.d( "dbloadact", stringHolder[i]);
		    			  	    		}
	    			  	    		}
	    			  	    	
	    			  	    	}		  	    	
	    			  	    }
	    			    	//ENDTAG/////////////////	    
	    			  	    else if(eventType == XmlPullParser.END_TAG)
	    			  	    {
	    			  	    	if(isContent ==true)
	    			  	    	{
	    			  	    		contentStringBuffer.append("</");
	    			  	    		contentStringBuffer.append(xpp.getName() );
	    			  	    		contentStringBuffer.append(">");	    		
	    			  	    	}
	    			  	    	if(xpp.getName().matches( "content"))
	    			  	    	{
	    			  	    		isContent = false; //reset the bool to false
	    			  	    		stringHolder[2] = contentStringBuffer.toString(); //finally, store content
	    			  	    		contentStringBuffer = new StringBuffer(); //re-initialize to start over for next
	    			  	    	}
	    			  	    	if(xpp.getName().matches("author"))//the last xml tag that is. now return the texts into the DB entry
	    			  	    	{		
	//    			  	    		long id = addWord( stringHolder[1], stringHolder[2]); // add name then content
	    			  	    //		String one = stringHolder[1]; String two = stringHolder[2]; String three = stringHolder[0];
	    			  	    		counter++;
	    			  	  //  		long id = DictionaryDatabase.DictionaryOpenHelper.addWord( one, two, three, Integer.toString(counter));
	    			  	    		long id = DictionaryDatabase.DictionaryOpenHelper.addWord( stringHolder[1], stringHolder[2], stringHolder[0], Integer.toString(counter), stringHolder[3]);
   			  	    		
	    		                    if (id < 0) {
	    		                        Log.e(TAG, "unable to add word: " + stringHolder[1].trim());
	    		                    }  
	    			  	    	}
	    			  	    }
	    			    Message msg = mHandler.obtainMessage();
	   	                Bundle b = new Bundle();
	   	                b.putInt("total", counter);
	   	                msg.setData(b);
	   	                mHandler.sendMessage(msg);
	   	               // total++;
	    			      //eventType=xpp.next();	
	   	             eventType=xpp.next();
/*
 * for some reason, old processed xml has blank space that was not accounted for . by new script, the new nokogiri m	    			      
 * made xml doesn't have the problem. just need to check eventtype?
 * something like this example
 * if(eventType == TEXT &&  isWhitespace()) {   // skip whitespace
      eventType = next();
   }
 */
	    			      
	    			     } 
	    			} catch (XmlPullParserException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace(); Log.e(TAG, "error loading database in XMLPullparser..");
	    			}finally {
	                   fis.close();
	               }
	    		//once done loading db.. now get the shared preferences and update favorites
	    			
	    		 loadFavorites();
	               backupDbToSD();//placed here...was getting weird duplicate requests

	           	 Log.d(TAG, "done loading the deeB son"); 
	       	    	wl.release();
	       	     setState(STATE_DONE);
           	 }
           	 else{ //ie. if the file doesn't even exist for some reason
           		Log.d(TAG, "ERROR WHY DOESNT XML FILE EXIST"); 
           	 }
           	 setState(STATE_DONE);
            }

		private void loadFavorites() {
			//
			SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);
			String favs = settings.getString("favorites", " "); //second thing is just default
			Log.d("DBLOADACT", "favs are" + favs);
			String [] temp;
			String delimiter = "\\."; //escaped period between  the favorites
			temp = favs.split(delimiter);
			 for(int i =0; i < temp.length ; i++){
				 DictionaryDatabase.addOrRemoveFavorite(temp[i],true);
			 }
			
		}
 	    }
 		 private void showAll() {
 			// changed cursor to final..? er not sure why. but onclicklistenter is an anonymous inner class iand i can't se nonfinal variables with scope outside of there
 				 //
 				 final Cursor cursor = managedQuery(DictionaryProvider.BACKUP_URI, null, null,null, null);
 				 if (cursor ==null){ //ie no results 
 					// tv.setText(getString(R.string.no_results));//
 					tv.setText(getString(R.string.first_use_thx) + getString(R.string.disclaimer));
 				 }else{
 					 
 				//	 int count = cursor.getCount();
 //		             String countString = getResources().getQuantityString(R.plurals.search_results,
 	//	                                     count, new Object[] {count, "WikEM entries"}); 
 					 String countString = "WikEM content is being updated. If this process is interrupted, 'Update' again and 'force rebuild'";
 		             tv.setText(countString);

 		             // Specify the columns we want to display in the result
 		             String[] from = new String[] { DictionaryDatabase.KEY_WORD, 
 		             		DictionaryDatabase.WIKEM_CATEGORY };

 		             // Specify the corresponding layout elements where we want the columns to go
 		             int[] to = new int[] { R.id.word, R.id.definition};

 		             // Create a simple cursor adapter for the definitions and apply them to the ListView
 		             SimpleCursorAdapter words = new SimpleCursorAdapter(this,
 		                                           R.layout.result, cursor, from, to);
 		             lv.setAdapter(words);

 		             // Define the on-click listener for the list items
 		             lv.setOnItemClickListener(new OnItemClickListener() {
 		                 public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		                	  //cursor.moveToFirst(); //uh. move to first row. there should only be one row...            
 		      	            //get indexes of columns
 		      	            cursor.moveToPosition(position);
 		      	            Log.d("VOIEWBACKUPO", " position is " + Integer.toString(position));
 		                	 int wIndex = cursor.getColumnIndexOrThrow(DictionaryDatabase.KEY_WORD);
 		                	 tv.setText(cursor.getString(wIndex));
 	 	      	   //         int dIndex = cursor.getColumnIndexOrThrow(DictionaryDatabase.KEY_DEFINITION);
 		                	 int rowID = cursor.getColumnIndexOrThrow(DictionaryDatabase.WIKEM_URI);
 		                	 
 		                	 String rowId = cursor.getString(rowID);
 		        			 final Cursor cursor2 = managedQuery(DictionaryProvider.BCONTENT_URI, null, rowId,null, null);
 		        			 cursor2.moveToFirst(); //should be only one row
 		        			 int dIndex = cursor2.getColumnIndexOrThrow(DictionaryDatabase.KEY_DEFINITION);
 		                	 
 		                	 String wikemEntry = cursor2.getString(dIndex);
 		                	 String head_css = getString(R.string.head_css);
 		      	       	                     
 		      	       	        //   String summary = "<html><body>" + wikemEntry + "</body></html>";
 		                	String summary = "<html>" + head_css + "<body>" + wikemEntry + "</body></html>";
 		      	       	          // word.setText(title + "(Old copy of wikem)" );
 		      	       	           
  		      	       	           wv.loadDataWithBaseURL(null, summary,"text/html", "UTF-8", null);
 		      	       	       //    lv.setVisibility(View.INVISIBLE);
  		      	       	    lv.setVisibility(View.GONE);//different than invisibile in that it  doesn't take any space
 		      	       	           wv.setVisibility(View.VISIBLE);
 		      	       	  //  wv.setGravity(Gravity.CENTER);//desperationmove...

 		                 }
 		             });
 		         }//end of else
 			
 		} 	
 		 
 		 private boolean first_toast = true;
 		 
 	 @Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
		    if ((keyCode == KeyEvent.KEYCODE_BACK) ) {
/*
* so during this activity i want back button to just flip the fram back to listview
* 
*     * If you handled the event, return true. If you want to allow the event to be handled by the next receiver, return false. 
*/
		    	if (wv.isShown()){ //returns True if this view and all of its ancestors are VISIBLE
		    		Log.d("dbload", "back button pressed");
			    	 lv.setVisibility(View.VISIBLE);
			    	 showAll(); ///i think needs to be called here
	    	       //  wv.setVisibility(View.INVISIBLE);
			    	 wv.setVisibility(View.GONE);
					 return true;
		    	}
		    	else{ //ie assume the listview is shown so toast can't go back
		    		if (first_toast){ 
		    			Toast.makeText(this, "WikEM is updating. Please wait.", Toast.LENGTH_SHORT).show();
		    			first_toast = false;
		    		}
				 return true;
		    	}
		    }
		    if(keyCode== KeyEvent.KEYCODE_SEARCH){
		    	//do nothing and avoid the default handle which would bring the search menu
		    	return true;
		    }
		   
		    return false;
		}


 	 private class MyWebViewClient extends WebViewClient { //True if the host application wants to leave the current WebView and handle the url itself, otherwise return false.
 	 			    @Override
 	 			    public boolean shouldOverrideUrlLoading(WebView view, String url) {
 	 			    		 //return false; //ie let android deal with thease links
 	 			    	Toast.makeText(DbLoadActivity.this, "Please wait until WikEM is finished updating...", Toast.LENGTH_SHORT).show();
 	 			        return true; //ie do nothing for our sake
 	 			    }
 	 			} 
}
 
