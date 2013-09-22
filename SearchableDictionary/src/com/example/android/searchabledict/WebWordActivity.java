/*
 * 
 * 1) note still buggy. null pointer somewhere.. like when phone turn off and on. also use bundle? or save prefs?
 *  2)alternatively: to get icon for findnext. rather than use default in options switch. add 'findnext' to menu xml but remove it and then add it back
    3) show soft key on search box. also save instace state of search stuff?
 *
 */


package com.example.android.searchabledict;

 
 
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

import wikem.chris.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
 import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
 	 
	public class WebWordActivity extends Activity{
		
		private static final String ORIGINAL_CONTENT = null;
		String keyWord;
		String noteContent;//for pause and resume?
		private EditText mBodyText;
		Cursor cursor;
		private String mFragment; //for internal links
		// public static ArrayList <Integer> linksList;
		 private ArrayList <Integer> highlightList;
	        WebView wv;
	        String summary;
	        private boolean highlight_text;
	        private boolean is_move;
	    	private int foundText = 0;
	    	private boolean findNext=false;
	    	private Iterator iterator=null;


		
		@Override
		 protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	        String head_css = getString(R.string.head_css);
	        
	        setContentView(R.layout.web_relative_layout);
	        Intent intent = getIntent();
	        Uri uri=intent.getData();
	        highlight_text = intent.getBooleanExtra("is_query", false); //deafult false. but true if is
	        cursor = managedQuery(uri, null, null, null, null);    
	        wv = (WebView) findViewById (R.id.wv1);
	        this.registerForContextMenu(wv); // doesn't work 
	        wv.setWebViewClient(new MyWebViewClient());
	        WebSettings settings = wv.getSettings(); 
	        settings.setJavaScriptEnabled(true);  //for internal links using onpagefinished
	        TextView word = (TextView) findViewById(R.id.word);
	        
	        if (cursor == null) {
	            finish();
	        } else {
	            cursor.moveToFirst(); //uh. move to first row. there should only be one row...            
	            //get indexes of columns
	            int wIndex = cursor.getColumnIndexOrThrow(DictionaryDatabase.KEY_WORD);
	            keyWord = cursor.getString(wIndex);
	            int dIndex = cursor.getColumnIndexOrThrow(DictionaryDatabase.KEY_DEFINITION);
	            //set the word/wiki entry name textview layout from the string in the cursor
	            // word.setText(cursor.getString(wIndex));
	            //the definition won't be in a webview for now, but simple textview using "Spanned" and fromHtml method
	            String wikemEntry = cursor.getString(dIndex);
	            if( highlight_text){
		    		if (SearchableDictionary.query==null){Log.d("WVA", "watch out!!! the query text being called is null");	}
		    		else{
			            wikemEntry = doHighlight(cursor.getString(dIndex), SearchableDictionary.query);
		    			Log.d("WVA", "highlighting.") ;
		    		}
		    		highlight_text=false; //reset it to false
		    	}
	            
	            summary =  "<html>" + head_css + "<body>"+ MenuBuilder.menuBuild(wikemEntry)+"</body></html>";
 	             word.setText(cursor.getString(wIndex));
	            //wv.loadData(summary, mimeType, encoding); this piece of crap doesn't work ...drove me crazy
		        wv.loadDataWithBaseURL(null, summary,"text/html", "UTF-8", null);
	         }
	        mBodyText = (EditText) findViewById(R.id.body);
	        //hides soft ekyboard until edittext presed?
	        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	        
	        wv.requestFocus();

	     /*   if (savedInstanceState != null) { //in case wikem change screen orientation or forced closed
	            mBodyText.setText(savedInstanceState.getString(ORIGINAL_CONTENT));
	        	}*/ //<- this code block moved to onRestoreInstanceState function. same thing basically
	        displayNoteIfExists(); 
	 
	        
	/*        
	       wv.setOnTouchListener(new OnTouchListener() {

	            public boolean onTouch(View v, MotionEvent event) {

	            	switch (event.getAction()) { 
	            	 case MotionEvent.ACTION_DOWN:	            		 
	            		 
	            	        break;
	            	 case MotionEvent.ACTION_MOVE: //such that finger swipes don't bring up contextmenu
	            		 is_move=true;
	                        break;
	                    case MotionEvent.ACTION_UP:
	                    	if(( event.getEventTime()-event.getDownTime()  )<800){ //short click
	    		            	if(!is_move)
	    		            		{ //show contextmenu if short touch on top edge of screen
	    		            		v.showContextMenu(); 
	    		            		}
	    		            	else{
	    		            		is_move=false;
	    		            		}	                    		
	    		              	}
 	                        break;
	            	}
	                return false;
	            }
	        });*/
	        	        
			/*	
			wv.setOnLongClickListener(new OnLongClickListener() {
		        public boolean onLongClick(View v) {
		            Log.d("Debug","On Long Press Web View");
		            return false;
		        }
		    });*/
		}
		
	/*	@Override
		public void onCreateContextMenu(final ContextMenu menu, View v,
		                                ContextMenuInfo menuInfo) {
		  super.onCreateContextMenu(menu, v, menuInfo);
//inflate menu manually. the list of ints represesents the relevant internal link positions in the headers[] array
		  for(int i=0;i<linksList.size();i++)
		  {
			  menu.add(0,linksList.get(i).intValue(), 0, SearchableDictionary.headers[linksList.get(i).intValue()] );
		  }
		  menu.setHeaderTitle("Jump to section:" );
	 	//	remove the timer 
		  Timer timing = new Timer();
		  		          timing.schedule(new TimerTask() {
		 		              @Override
		               public void run() {
		  		                menu.close();
		  		              }
		  		          }, 1700);//enough time to find and click
		  
		}*/

	/*	@Override
		public boolean onContextItemSelected(MenuItem item) {
			  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			mFragment = SearchableDictionary.headers[item.getItemId()];
			Log.d("wv", " trying to reload w" + mFragment);
			//wv.reload(); gives blank page
	        wv.loadDataWithBaseURL(null, summary,"text/html", "UTF-8", null);

			return true;
	
		}
	*/
 		private void displayNoteIfExists(){
 			SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);
 			
 			
  			if (settings.getBoolean("displayNotePref", true)){ //ie if user wants to show note (the 2nd true is default)
  				
 			   mBodyText.setVisibility(View.VISIBLE);
	
  				
  				if(settings.contains("note-" + keyWord)){
  					String note = settings.getString("note-" + keyWord, "none"); //second "keyword" is just default}
  					if(note != "none"){ 
  						if (note!=null){//null protect string body from null string 
  						mBodyText.setTextKeepState(note); Log.d("WWAct", "trying to display note for keyword " + keyWord +". Note is:" + note);}			 	                
  		            }
  					else{ Log.d("WWact", "why is note saying NONE?!!");}
  				}
  			}
  			else { //ie if the displaynote prefs wants to hide the note
  			  mBodyText.setVisibility(View.GONE);
  			}
 				
			
 		}
		
		private void commitNote(){
			//TODO 
			// use.. getContentResolver().update(mUri, values, null, null);
			SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    			
            SharedPreferences.Editor editor = settings.edit();               
            String note =  mBodyText.getText().toString();
            if (note.length()>=1){ //cheap way to see if not empty
             editor.putString("note-" + keyWord, note);
             editor.commit();
             Log.d("WWAct", "note presumably committed  ") ;}

		}
			
		private class MyWebViewClient extends WebViewClient { //True if the host application wants to leave the current WebView and handle the url itself, otherwise return false.
		    @Override
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		    	Log.e("WEBWORDACTVITY", "the url clicked is : " +url);
		    	
			    URI link = null;
				try {
					link = new URI(url);
				} catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				if (link!=null )
				{	
				/* try { 
					 if (link.getScheme().equals("http") || link.isOpaque()) 			 
					 {
						 if (link.toString().startsWith("mailto")){
							// /*later...email stuff8
							 	return false;						  
						 }					 
						 return false; //ie let android deal with thease links
			    	   //  * later  try to send launch email activity on opaques
			    	    //* Intent intent = new Intent(Intent.ACTION_SEND);
			    	    //
					 }
				  }catch(NullPointerException e2){} //?shouldnt throw
				  */
				} 	  
			  
			    	 /*
			    	  * try make more rhobust...like only launch the following code if uri starts with a 
			    	  * 		    	  * relative uris are ok!
			    	  */	  	        
			        Log.d("WEBWORDACTIVITY", "URI IS " + url);
			        try {
			        	String rowID = DictionaryDatabase.getRowId(url);
			        	Uri data = Uri.withAppendedPath(DictionaryProvider.CONTENT_URI,
	                        rowID);
			        	Log.d("WEBWORD ACTIVIY","the full uri to go to is:" + data.toString());
			        	if (rowID.equals("null")){
			        		Log.e("WEBWORDACTIVITY", "crap the return from getRowId is null..im inside the if tho");
			        		
			                return true;
			        	}
			        	else{
			        		Intent wordIntent = new Intent(getApplicationContext(), WebWordActivity.class);
			        		wordIntent.setData(data);
			        		startActivity(wordIntent);
			        		return true;
			        		}		        		
			        }catch(Exception e){Log.e("WEBWORDACTIVITY", "crap the return from getRowId is null");
			        					String sub = url.substring(1); //get rid of backslash
			        					sub = Uri.decode(sub);
			        					Log.d("webword act:", sub);
			        					Intent wordSearchIntent = new Intent(getApplicationContext(), WordSearchActivity.class);
			        					wordSearchIntent.putExtra("word", sub);
			        					startActivity(wordSearchIntent);
			        					}
			        return true;
			    }
		    
		    public void onPageFinished(final WebView view, final String url) { 
		    /* this part has code such that javascript for detected contextmenu internal links. called after reloading the page * 	
		     */
		    	//view.loadUrl("javascript:searchPrompt('aaa', true, 'green', 'pink')");
		    	  // make it jump to the internal link 
		    	  if (mFragment != null) { 
		    	    view.loadUrl("javascript:location.href=\"#" + mFragment + "\""); 
		    		//  wv.loadDataWithBaseURL("javascript:location.href=\"#" + mFragment + "\"", summary,"text/html", "UTF-8", null);
		    	    
		    	    Log.d("wv" , "tried javascript...>>>>>>failed!!");
		    	    mFragment = null; //lol to avoid infinite loop
		    	    view.requestFocus();
		    	 }
		    	   
		    } 
		}

		 @Override
		 public boolean onPrepareOptionsMenu(Menu menu) {
			 //You must return true for the menu to be displayed; if you return false it will not be shown.
			 /*
			  * this is called right before the menu is shown, every time it is shown.
			  */
			// MenuInflater inflater = getMenuInflater();
		   //  inflater.inflate(R.menu.word_options2, menu); //this adds everything agian!
		     
			 if (findNext==true && iterator.hasNext()){
		        	menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Find Next!");
		        	Log.d("WWA", "should have added new menu item");
		        	findNext=false; //stop creating menus
		        }
			 
			 if (!displayNoteOrNot()) //ie hide the note
			 {
				// menu.removeItem(R.id.remove_edit_text);
				 menu.setGroupVisible(R.id.remove_edit_text_group, false);
				 menu.setGroupVisible(R.id.show_edit_text_group, true);

				 
			 }
			 else{
				// menu.add
				 //bc this is called every time... as long as the bool is accurate this will work
				 //show menu icon
				 //menu.removeItem(R.id.show_edit_text); ///////////////////////
				 
				 menu.setGroupVisible(R.id.remove_edit_text_group, true);
				 menu.setGroupVisible(R.id.show_edit_text_group, false);
				 
			 }
			 return true;
		 }
		 
		 @Override
		 public boolean onCreateOptionsMenu(Menu menu) {
			//	Log.d("WEBWORDACTIVITY", " ok.. inside on create menu");
			
			 	if (keyWord.equals("About WikEM")){
			 		 /*
					  * put in easter egg. for hidden functionality for beta testing
					  */
			 		Log.d("WEBWORDACTIVITY", " ok.. inside about wikem secret menu");
			 		 MenuInflater inflater = getMenuInflater();
				     inflater.inflate(R.menu.word_options2, menu);
				     return true;
			 		
			 	}
			 	else{
			        MenuInflater inflater = getMenuInflater();
			        inflater.inflate(R.menu.word_options2, menu);
			               
			        
			        return true;
			 	}
		    }
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.search:
	            //   ?? onSearchRequested(); ??what is this
	          // startSearch(null,false,null,false);	    
	            	findTextAlert();
	                return true;
	         //   case R.id.update: //menu item for updating
	          //  	startActivity(new Intent(this, DownloaderTest.class));
	            	//return true;
	            case R.id.favorite:
	            	favoriteOptionSelect();
	            	return true;
	            //case R.id.info:
	            case R.id.remove_edit_text:
	            	//displayInfo();
	            	undisplayNote();
	            	
	            	return true;
	            case R.id.show_edit_text:
	            	displayNote();
	            	return true;
	            case R.id.edit_online:
	            	editOnline();
	            	return true;
	            	            	
	            default: return findNext(); //since no unique id of findnext...just use default.
	            
	        }
	    }

	private boolean findNext() {
		if (iterator.hasNext())
		{
			mFragment = iterator.next().toString();
			wv.loadDataWithBaseURL(null,summary ,"text/html", "UTF-8", null);
			Log.d("WWA", "inside findnext");
			return true;
		}
			
			return false;
		}

	private void findTextAlert() {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
	
			alert.setTitle("Find on Page");
			alert.setMessage("Please enter a term to search for then press ok");
	
			// Set an EditText view to get user input 
			final EditText input = new EditText(this);
			alert.setView(input);
	
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			  String value = input.getText().toString();
			  // Do something with value!
			  findText(value);
			  
			  }
			});
	
			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
			});
	
			alert.show();			
		}
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * fix
	 */
	
	private void findText(String searchTerm){
		/*int foundText = wv.findAll(searchTerm);
		Log.d("WVA", "findall gets#" + foundText);
		// 
		for (int i=0; i<foundText; i++)
		{
		//if(foundText>0){ //ie number of returned search terms
	    		  wv.findNext(true);
	    		//  i++;
	    		  Log.d("WVA", "foundtxt is "+Integer.toString(i));
	    	  }	 */
		summary = doHighlight(summary, searchTerm);
		if (highlightList!=null && highlightList.size()>0)//redundant?!
		{
		mFragment = Integer.toString(1);// ie. start at the first link
		findNext=true; //ie add a menu option to find next now
		Toast.makeText(this, "Found " + Integer.toString(highlightList.size()) + " search items", Toast.LENGTH_SHORT).show();
		
		
		iterator = highlightList.iterator();
		if( iterator.hasNext()){
			//this.invalidateOptionsMenu();//not available till api 11
		}
		//mFragment = iterator.getClass()
		
		}
		   wv.loadDataWithBaseURL(null,summary ,"text/html", "UTF-8", null);
		
	}

	private boolean displayNoteOrNot(){ //returns the preference to display..t or f
		 
		SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
		return settings.getBoolean("displayNotePref", true);
	}
	private void displayNote(){
		//hideNoteonOptions = true; //ie. show the 'hide note' option again
		
		undisplayNote(); //i think undisplaynote should take care of all. since it is written as either/or scenario
		
		
	}
	private void undisplayNote(){
		/* so if prefs = hide. then keep hidden. 
		 * otw, if show note, then keep showing...
		 */
		SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
		boolean displayNotePref = settings.getBoolean("displayNotePref", true); //second "true" is just default
		if(displayNotePref ==true)
			{
	        SharedPreferences.Editor editor = settings.edit(); 
	        editor.putBoolean("displayNotePref", false);
	        editor.commit();
	        
			   mBodyText.setVisibility(View.GONE);
		//?what is this	   RelativeLayout.LayoutParams layoutParams;
			   //layoutParams.addRule(RelativeLayout.RIGHT_OF,
		    	 Log.d("WEBWORDACTIVITY  ", "make text invis..");
		    //	 hideNoteonOptions=true;
		    	 
			}
		else{ // ie' redisplay the note
			SharedPreferences.Editor editor = settings.edit(); 
	        editor.putBoolean("displayNotePref", true);
	        editor.commit();
	        displayNoteIfExists();
	    	// hideNoteonOptions=false;

			
		     }
			
	    }
	   
		private void editOnline(){
	    	
	    	String baseURL = "http://www.wikem.org/w/index.php?title=";
	    	String websiteURL= baseURL.concat(keyWord.replace(' ', '_')) + "&action=edit";
	    	 
	    	 Log.d("WEBWORDACTIVITY open ", websiteURL.concat(keyWord.replace(' ', '_')));
	    	 Uri uri = Uri.parse( websiteURL);
	    	 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	    	 startActivity(intent);
	    }

		
		private void favoriteOptionSelect() {
			//first check that not already favorite if not			
			// replace "favorites" shared preference wiht: favorite1, favorite2, fav3, ...
			SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
			String favs = settings.getString("favorites", " "); //second "keyword" is just default
            SharedPreferences.Editor editor = settings.edit();  
             
            if (favs.contains(keyWord)){ //ie already contains this favorite
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            	builder.setMessage(keyWord+ " already exists as favorite. Remove?")
            	       .setCancelable(false)
            	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	               ///// dunno what put here MyActivity.this.finish();
            	        	   removeFavorite();
            	           }
            	       })
            	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	                dialog.cancel();
            	           }
            	       });
            	AlertDialog alert = builder.create();      
            	alert.show();
            	
            } 
            //otherwise if the favorite already doesn't exist..
            else{
            favs = favs.concat("." + keyWord); //for first fav... i suppose will just add favorite twice
            editor.putString("favorites", favs);

             // Commit the edits!
             editor.commit();
             
			if (DictionaryDatabase.addOrRemoveFavorite(keyWord, true)){ 
				//add fav to the db so to be queried . boolean true is for add...not remove
				//returned true... toast 
				Log.d("WEBWORDACt", "favs are:" + favs);
				Toast toast = Toast.makeText(getApplicationContext(), keyWord + " successfully added to favorites", Toast.LENGTH_SHORT);
		 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		 		toast.show();
			}
			else{
				//favorite adding db issues..
			}
            }
		}
		
		
		protected void removeFavorite() {
			 
			if (DictionaryDatabase.addOrRemoveFavorite(keyWord, false)){
				//false ->remove
				Log.d("WEBWORDACt", "favorite removed");
				Toast toast = Toast.makeText(getApplicationContext(), keyWord + " successfully removed from favorites", Toast.LENGTH_SHORT);
		 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		 		toast.show();
			}
			else{
				//favorite removing db issues...
			}
			
		}
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
		    if ((keyCode == KeyEvent.KEYCODE_BACK) ) {
		    	 commitNote();

		    	Log.d("WEBWORD", "back button pressed");
		         //i still want the default back..just to do work b4
		    	 finish(); //ie. i want the event to be handled by the next receiver
				 return true;
		    }
		   
		    return false;
		}
	
		 @Override
		 protected void onPause() {
		        super.onPause();	
		        Log.d("WWAct", "on paused " );
		      //  noteContent = mBodyText.toString();
		        //commitNote();  
		    }
		 @Override
		 protected void onResume(){
			 super.onResume();//bc it crashed without it
				Log.d("WWAct", "on resumed " ); 
			//	foundText =0;//i think i put here bc with every new word this page no re-initialized. just resumed. and not always user will want to find text to reset
				
			// if (noteContent != null){
			//	 mBodyText.setText(noteContent);
			//	 Log.d("WWAct", "notecontent null/? " );
			// }		
			displayNoteIfExists();		// ? (ck old: 'i think being called too early...keyword needs to reset'
			//ck 3/26/11: dunno what that last comment means/.
		 }
		 @Override
		    protected void onRestoreInstanceState(Bundle savedInstanceState) {
		        super.onRestoreInstanceState(savedInstanceState);
		       // i think the difference here is that on FIRST open this ISNT called. otherwise oncreate and this both called on restore
			        if (savedInstanceState != null) { //in case wikem change screen orientation or forced closed
			        	String temp = savedInstanceState.getString(ORIGINAL_CONTENT);
			        	if (temp!=null){ //maybe it was null ?kept getting weird null edit text android..widget.edittext@ nonsense
			        		mBodyText.setText(temp);
			        		
			        	}
			        }
		        ////    //mBodyText.setText(savedInstanceState.getString(ORIGINAL_CONTENT));}	
		     }
		 @Override
	    protected void onSaveInstanceState(Bundle outState) {
		        // Save away the original text, so we still have it if the activity
		        // needs to be killed while paused.
//5/6/11 added
			   super.onSaveInstanceState(outState);
//end added//
		        outState.putString(ORIGINAL_CONTENT, mBodyText.getText().toString());
		        commitNote(); //what happens if i put the note here instead
		        /*
		         * 
		         * 1) onSaveInstanceState() is intended for the use to save UI states, I have no question about that.
				2) onPause() is intended for the use to save persistent data (database, files.. etc), in other words.. data that should persist across application sessions.

		         */
		    }
		 
		 private String doHighlight( String bodyText, String searchTerm){
			  String newText = "";
			  int i = -1;
			  String lcSearchTerm = searchTerm.toLowerCase();
			  String lcBodyText = bodyText.toLowerCase();
			  //unused highlight tags.. i assume if using css and span stuffs
			  String highlightStartTag;
			  String highlightEndTag;
			//  if ((!highlightStartTag) || (!highlightEndTag)) {
				    highlightStartTag = "<font style='color:blue; background-color:yellow;'>";
				    highlightEndTag = "</font>";
//				  }
				    int counter = 1; //start counter at 1
				    highlightList = new ArrayList<Integer>(); 
				    iterator = null;//reset the iterator to null here. as the list will be changed fosho
				    
				    try{
					  while (bodyText.length() > 0) {
					    i = lcBodyText.indexOf(lcSearchTerm, i+1);
					   // Log.d("WWA", "i is:"+ Integer.toString(i));
					    if (i < 0) {
					      newText += bodyText;
					      bodyText = "";
					    } else {
		
					      // skip anything inside an HTML tag
					      if (bodyText.lastIndexOf(">", i) >= bodyText.lastIndexOf("<", i)) {
					        // skip anything inside a <script> block
					      //  if (lcBodyText.lastIndexOf("/script>", i) >= lcBodyText.lastIndexOf("<script", i)) {
					    	  
					    	  
					    	 // )"<a name="+ "\"" + SearchableDictionary.headers[j]+"\"" +"></a>"  highlightList.add(new Integer(counter); counter++;
					    	  
					   //       newText += bodyText.substring(0, i) + highlightStartTag + bodyText.substring(i, searchTerm.length()) + highlightEndTag;
					    	   //	newText = newText.concat(bodyText.substring(0,i) + highlightStartTag);
					 //just add the link as just an integer number ..so first highlight will just be 1, second ->2.. etc
					    	  newText = newText.concat(bodyText.substring(0,i) 
					    			  +"<a name="+ "\"" + Integer.toString(counter)+ "\"" +"></a>" 
					    			  + highlightStartTag);
					    	   	highlightList.add(new Integer(counter)); 
					    	   	counter++;
					        	newText = newText.concat(bodyText.substring(i, i + searchTerm.length()) + highlightEndTag);
		        	
					          bodyText = bodyText.substring(i + searchTerm.length());
					          lcBodyText = bodyText.toLowerCase();
					          i = -1;
					      //  }
					      }
					    }
					  } //end of while
					  
				    }catch (IndexOutOfBoundsException e){Log.d("WWA", "highlight error msg!! " + e.getMessage()); return bodyText;}
			  return newText;
			}	 

		 public static class MenuBuilder {

				public static String menuBuild(String s){
					
					//deprecated since new server format.
					//toplinks included and can change with css
					
					return s;
					//return (topLinks + s);
				}
				
			}
		 
		 

	}
	
	
	
	
	
	
 