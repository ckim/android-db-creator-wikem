package com.example.android.searchabledict;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import wikem.chris.R;

public class WordSearchActivity extends Activity{
	

	private ListView lv;
	private TextView tv;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cat);
 	        lv = (ListView) findViewById(R.id.list);
 	        tv = (TextView) findViewById(R.id.text);
 	        String query = getIntent().getStringExtra("word");
 	        Log.d("SOWRDSEARCHACTIVITY", "WHERE IS ACTIVITY FOR:"+query);
 	        showResults(query);
	}
	
	

    private void showResults(String query) {

        Cursor cursor = managedQuery(DictionaryProvider.TITLE_URI, null, null,
                                new String[] {query}, null);
    	//let's see if this works to get all category
    	//Cursor cursor = managedQuery(DictionaryProvider.CATEGORY_URI, null, null,
    		//	new String[]{query}, null);

        if (cursor == null) {
            // There are no results
            tv.setText(getString(R.string.no_results, new Object[] {query}));
        } else {
            // Display the number of results
            int count = cursor.getCount();
            String countString = //getResources().getQuantityString(R.plurals.search_results,
                                   // count, new Object[] {count, query});
            	" "; //TODO 
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
                    // Build the Intent used to open WordActivity with a specific word Uri
                    Intent wordIntent = new Intent(getApplicationContext(), WebWordActivity.class);
                    Uri data = Uri.withAppendedPath(DictionaryProvider.CONTENT_URI,
                                                    String.valueOf(id));
           Log.d("SEARCHABLE DICT", "uri data is.............. " + data.toString());
                    wordIntent.setData(data);
                    startActivity(wordIntent);
                }
            });
        }
    }
		 
		 
}
