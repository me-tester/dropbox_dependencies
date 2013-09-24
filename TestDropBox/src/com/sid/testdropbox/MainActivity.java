package com.sid.testdropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class MainActivity extends ActionBarActivity 
{

	final static private String APP_KEY = "tm9z8i89nrjoim6";
	final static private String APP_SECRET = "ut4902dtdhrvumm";
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	
	// In the class declaration section:
	private DropboxAPI<AndroidAuthSession> mDBApi;
	private String FileName = ""; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// And later in some initialization function:
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
		mDBApi = new DropboxAPI<AndroidAuthSession>(session);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void linkDrop() 
	{
		if( mDBApi.getSession().getAccessTokenPair() != null )
		{
			uploadDataDropbox();
		}
		else
		{
			mDBApi.getSession().startAuthentication(MainActivity.this);
		}
	}

	private boolean createAndSaveFile() 
	{
		Boolean response = false;
		int posRandInt = new Random().nextInt( Integer.MAX_VALUE ) + 1;
		EditText editText = (EditText) findViewById(R.id.textArea);
		
		if( !editText.getText().toString().trim().isEmpty() )
		{
			String FILENAME = this.getString(R.string.app_name) + "_" + posRandInt + ".txt" ;
			String string = "" + editText.getText().toString().trim();
			Log.d("FILENAME" , "FILENAME = " + FILENAME);
			this.FileName = FILENAME;
			
			FileOutputStream fos;
			try 
			{
				fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
				fos.write(string.getBytes());
				fos.close();
				
				response = true;
			} 
			catch (Exception e) 
			{
				response = false;
				Log.e("DropSink" , "File error = " + e);
			} 
		}
		else
		{
			Toast toast = Toast.makeText( this , "Enter some text to save !!", Toast.LENGTH_SHORT );
    		toast.show();
		}
		
		return response;
	}
	
	protected void onResume() 
	{
	    super.onResume();

	    if (mDBApi.getSession().authenticationSuccessful()) 
	    {
	        try 
	        {
	            // Required to complete auth, sets the access token on the session
	            mDBApi.getSession().finishAuthentication();

	            AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();
	            Log.i("Auth proper", "tokens = " + tokens);
	            
	            uploadDataDropbox();
	        } 
	        catch (IllegalStateException e) 
	        {
	            Log.i("DbAuthLog", "Error authenticating", e);
	        }
	    }
	}
	
	private void uploadDataDropbox()
	{
		final String myFileName = this.FileName;
		
		try 
		{
			Thread trd = new Thread(new Runnable()
			{
				public void run()
				{
					String fileName = getFilesDir() + "/" +  myFileName ;
					File file = new File(fileName);
					FileInputStream inputStream;
					try 
					{
						inputStream = new FileInputStream(file);
						//code to do the HTTP request
						Entry response = mDBApi.putFile( myFileName , inputStream,file.length(), null, null);
						
						Log.i("response", "response = " + response);
					} 
					catch (Exception e) 
					{
						Log.e("error", "error thread = " + e);
					}
				}
			});
			
			trd.start();			
		} 
		catch (Exception e) 
		{
			Log.e("error", "error here = " + e);
		} 
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    // Handle presses on the action bar items
	    switch (item.getItemId()) 
	    {
	        case R.id.action_saveDbx:
	        	
	        	boolean fileSave = createAndSaveFile();
	        	if( fileSave )
	        	{
	        		linkDrop();
	        		//Toast toast = Toast.makeText( this , "saved data", Toast.LENGTH_SHORT );
	        		//toast.show();
	        	}
	        	else
	        	{
	        		Toast toast = Toast.makeText( this , "Unable to save data", Toast.LENGTH_SHORT );
	        		toast.show();
	        	}
	            return true;
	            
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
