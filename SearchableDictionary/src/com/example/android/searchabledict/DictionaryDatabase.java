 

package com.example.android.searchabledict;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
//import android.content.res.Resources;
//import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
 //import android.os.Looper;
import android.provider.BaseColumns;
//import android.text.TextUtils;
import android.util.Log;
//import android.view.Gravity;
//import android.widget.Toast;
//import android.view.Gravity;
//import android.widget.Toast;

//import java.io.BufferedReader;
//import java.io.FileInputStream;
  //import java.io.FileNotFoundException;
 //import java.io.InputStream;
//import java.io.InputStreamReader;
import java.util.HashMap;

import com.example.android.searchabledict.ExternalSQLHelper;

  

/**
 * Contains logic to return specific words from the dictionary, and
 * load the dictionary table when it needs to be created.
 */
public class DictionaryDatabase {
    private static final String TAG = "DictionaryDatabase";
    public static final String LAST_UPDATE = "LAST_UPDATE";
    //The columns we'll include in the dictionary table
    public static final String KEY_WORD = SearchManager.SUGGEST_COLUMN_TEXT_1;
  //  public static final String KEY_DEFINITION = SearchManager.SUGGEST_COLUMN_TEXT_2; //optional second line for search suggestions
  //now adding categories
    public static final String KEY_DEFINITION = "DEFINITIONS"; //hash map will take care of what the actual name is
    public static final String WIKEM_CATEGORY = SearchManager.SUGGEST_COLUMN_TEXT_2;
    public static final String WIKEM_URI = "WIKEM_URI"; //basically redundant rowID
    public static final String FAVORITE = "FAVORITE";
    

    private static final String DATABASE_NAME = "dictionary";
    private static final String FTS_VIRTUAL_TABLE = "FTSdictionary";
    private static int DATABASE_VERSION = 3;

    private static ExternalSQLHelper e; //use here? 6/2011
    private static DictionaryOpenHelper mDatabaseOpenHelper;
    private static final HashMap<String,String> mColumnMap = buildColumnMap();
    
    /**
     * Constructor
     * @param context The Context within which to work, used to create the DB
     */
    public DictionaryDatabase(Context context) {
        mDatabaseOpenHelper = new DictionaryOpenHelper(context);
    }
    //crap i added 
     public static void upgrade(){
       	//close the db, upgrade the dbversion...then try to reload it? don't know if this works
       	//the old version of the db is within the .db itself...
       	mDatabaseOpenHelper.close();
       	DATABASE_VERSION+=1;
        //mDatabaseOpenHelper = new DictionaryOpenHelper(getContext());

       	Log.d(TAG, "ok trying to ++ the db version");
       	SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
       	mDatabaseOpenHelper.onUpgrade(db,DATABASE_VERSION, DATABASE_VERSION++);
       }
     
     //a method i added for updating a favorite column
     public static boolean addOrRemoveFavorite(String f, boolean add){
    	 
    	 SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
    	 String[] whereArgs = new String [] { f };
    	 ContentValues newFav = new ContentValues();
    	 if (add){
    		 newFav.put(FAVORITE, "1");
    	 }else{
    		 newFav.put(FAVORITE, "0");
    	 }
    	
   //put 1 in favorite column where the keyword matches the passed string "f" ..i hope
    	 int check = db.update(FTS_VIRTUAL_TABLE, newFav, KEY_WORD + "=?", whereArgs); //returns #row affectd
    	 if (check ==0){
    		 Log.d(TAG, "uh. addfav not working.. nothing changed for:" + f);
    		 return false;
    	 }
    	 else{
    		 Log.d(TAG,"add fav" + f +" row changed" );
    	 }
    	 return true;
     }
  
      
    /**
     * Builds a map for all columns that may be requested, which will be given to the 
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include 
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(KEY_WORD, KEY_WORD);
        map.put(KEY_DEFINITION, KEY_DEFINITION);
        map.put(WIKEM_CATEGORY, WIKEM_CATEGORY);
        map.put(WIKEM_URI, WIKEM_URI);
        map.put(FAVORITE, FAVORITE);
        
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }

    /**
     * Returns a Cursor positioned at the word specified by rowId
     *
     * @param rowId id of word to retrieve
     * @param columns The columns to include, if null then all are included
     * @return Cursor positioned to matching word, or null if not found.
     */
    public Cursor getWord(String rowId, String[] columns) {
        String selection = "rowid = ?";
        String[] selectionArgs = new String[] {rowId};

        return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
         */
    }

    //return the ID (as string) of rowID where the input to query is the Word (use for links)
    public static String getRowId(String word)
    {
    //	word = word.trim().substring(1); //get rid of forward slash
    	word = word.trim().substring(5);//get rid of  "wiki/"
   	    word = word.replace('-', ' ').trim();
    	String[] columns = new String[] {
    	           WIKEM_URI
    	          };
    	Cursor cursor = getWordMatches(word, columns);
    	//cursor.moveToFirst();
    	String rowIdofInterest = null;
    //	try{
    		if (cursor!=null ){
    			int index = cursor.getColumnIndex(WIKEM_URI);
    			cursor.moveToFirst();
    			
		    		if (index ==-1){
		    			cursor.close();
		    			return null;
		    		}
		    		else{
		    		rowIdofInterest = cursor.getString(index);
		    		
				   // 	}catch (IllegalArgumentException e) { Log.e("DICTDATABASE", "Illegal argument error ");}
				    	
				    	//String rowIdofInterest = cursor.getString(index); 
				    	//now if cursor has 1 element... then we're good. return
				    	Log.d("DICTDATABASE", "getRowId: yo yo, this is the row ID:" + rowIdofInterest);
				    	cursor.close();
				    	return rowIdofInterest;
		    		}
    		}
    		
    		return null;
    }
    /**
     * Returns a Cursor over all words that match the given query
     *
     * @param query The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all words that match, or null if none found.
     
     * 
     */
    public Cursor getEverything(String[] columns) {
		// TODO Auto-generated method stub
    	String selection = null; //...ie return all
    	String[] selectionArgs = null; // selectionargs null
    	  Log.d(TAG, " lets get everything");
		return query(selection, selectionArgs, columns);
 	}
    public Cursor getBackup(String[] columns) {
		// TODO Auto-generated method stub
    	String selection = null; //...ie return all
    	String[] selectionArgs = null; // selectionargs null
    	  Log.d(TAG, "beeear! lets get backup!");
		//return query(selection, selectionArgs, columns);
    	  return queryExternalDb(selection, selectionArgs,columns); //test if this works
	}
    public Cursor getFavorites( String[] columns) {
		 String selection = FAVORITE + " MATCH ?";
	     String [] selectionArgs = new String[]{"1"};
	     Log.d(TAG, "yo lets get favorites");
	      return query(selection, selectionArgs, columns); 
		 
	}
	public Cursor getAllCategory(String query, String[] columns) {
		String selection = WIKEM_CATEGORY + " MATCH ?";
		String[] selectionArgs = new String[]{query+"*"};
		Log.d(TAG, "yo yo yo, custom category accessed");
 		return query(selection, selectionArgs, columns); 
	}
    public static Cursor getWordMatches(String query, String[] columns) {
        String selection = KEY_WORD + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE <KEY_WORD> MATCH 'query*'
         * which is an FTS3 search for the query text (plus a wildcard) inside the word column.
         *
         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
         * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
         *   for suggestions to carry the proper intent data.
         *   These aliases are defined in the DictionaryProvider when queries are made.
         * - This can be revised to also search the definition text with FTS3 by changing
         *   the selection clause to use FTS_VIRTUAL_TABLE instead of KEY_WORD (to search across
         *   the entire table, but sorting the relevance could be difficult.
         */
    }
    public static Cursor getWordMatchesFromTable(String query, String[] columns) { //mine.. to search table
        String selection = FTS_VIRTUAL_TABLE + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE < the table > MATCH 'query*'
         * which is an FTS3 search for the query text (plus a wildcard) inside the word column.
         *
         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
         * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
         *   for suggestions to carry the proper intent data.
         *   These aliases are defined in the DictionaryProvider when queries are made.
         * - This can be revised to also search the definition text with FTS3 by changing
         *   the selection clause to use FTS_VIRTUAL_TABLE instead of KEY_WORD (to search across
         *   the entire table, but sorting the relevance could be difficult.
         */
    }
    public static Cursor getBackupWord(String rowId, String[] columns) { //mine.. to search table
    	String selection = "rowid = ?";
        String[] selectionArgs = new String[] {rowId};

       // return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
         */
        return queryExternalDb(selection, selectionArgs, columns);
     }

    /**
     * Performs a database query.
     * @param selection The selection clause
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param columns The columns to return
     * @return A Cursor over all rows matching the query
     */
    private static Cursor query(String selection, String[] selectionArgs, String[] columns) {
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        builder.setProjectionMap(mColumnMap);
        builder.setDistinct(true); //don't know if this works or not... still getting duplicates..
        Cursor cursor = builder.query( mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }
    
    public static void initializeDB(){ //for the external. otw was initializing too many times
    	 e = new ExternalSQLHelper ("db.db"); 
    }
    public static void closeExternalDB(){ //for the external
    	e.close();
    }
    private static Cursor queryExternalDb(String selection, String[] selectionArgs, String[] columns) {
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        builder.setProjectionMap(mColumnMap);
        builder.setDistinct(true); //don't know if this works or not... still getting duplicates..
        
 //		ExternalSQLHelper e = new ExternalSQLHelper ("db.db");
     //     e = new ExternalSQLHelper ("db.db"); move to initialize db
		
        Cursor cursor = builder.query( e.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) { Log.d(TAG, "cursor in ext db is null");
            return null;
        } else if (!cursor.moveToFirst()) { Log.d(TAG, "cursor in ext db is empty");
            cursor.close();
            return null;
        }
        Log.d(TAG, "cursor in ext db has something!");
        return cursor;
    }


    /**
     * This creates/opens the database.
     */
    public static class DictionaryOpenHelper extends SQLiteOpenHelper {
    	
        private final Context mHelperContext;
        private static SQLiteDatabase mDatabase;

        /* Note that FTS3 does not support column constraints and thus, you cannot
         * declare a primary key. However, "rowid" is automatically used as a unique
         * identifier, so when making requests, we will use "_id" as an alias for "rowid"
         */
        //constraint unique on conflict ignore so no duplicate wikem entry names
        //CREATE VIRTUAL TABLE simple USING fts3(tokenize=porter);

        private static final String FTS_TABLE_CREATE =
                    "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                    " USING fts3 (" +
                    //try add custom tokenizer here
                    "tokenize=porter, " +
                    KEY_WORD + " CONSTRAINT UNIQUE ON CONFLICT IGNORE "+ ", " +
                    KEY_DEFINITION + ", " +
                    WIKEM_CATEGORY + ", " + WIKEM_URI + ", " + FAVORITE + ", " + LAST_UPDATE + ");";
        
 //       private ProgressDialog myProgressDialog = null;
 //       private Handler mHandler;
        
        DictionaryOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            mDatabase.execSQL(FTS_TABLE_CREATE);
           // loadDictionary();
    
            Intent intent = new Intent(mHelperContext, DbLoadActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //try without
         //   startActivity(intent);
            mHelperContext.startActivity(intent);
        }

        /**
         * Starts a thread to load the database table with words
         */
 		
             
       
        public static long addWord(String word, String definition, String category, String crap, String lastupdate){
        	//ghetto solution for now. as the ID column should match the URI column i am making..
        	ContentValues initialValues = new ContentValues();
        	initialValues.put(KEY_WORD, word);
            initialValues.put(KEY_DEFINITION, ( definition));
            initialValues.put(WIKEM_CATEGORY, category);
            initialValues.put(WIKEM_URI, crap);
 //initialize all favorite as 0 for now... initialize all at once
            initialValues.put(FAVORITE, "0");
            initialValues.put(LAST_UPDATE, lastupdate);
            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }
     /*   public long addWord(String word, String definition, String category){
        	
        	
        	ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_WORD, word);
            initialValues.put(KEY_DEFINITION, ( definition));
            initialValues.put(WIKEM_CATEGORY, category);
            return   mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues); //insert new row here
        
        }*/
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	//if (needUpdate()){Log.d(TAG, "url connection works?");}
        	///////
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }
    }


	public static void close() {
		// TODO Auto-generated method stub 
       	mDatabaseOpenHelper.close();

		
	}



}  

		
