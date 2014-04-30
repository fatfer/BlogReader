package com.example.blogreader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class MainListActivity extends ListActivity {
	
	protected String[] mBlogPostTitles;
	public static final int NUMBER_OF_POSTS = 20;
	public static final String TAG  = MainListActivity.class.getSimpleName();
	protected JSONObject mBlogData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        
        if (isNetworkAvailable()){
        	GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
        	getBlogPostsTask.execute();
        }
        else{
        	Toast.makeText(this, "Network is unavailable!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
    	ConnectivityManager manager = (ConnectivityManager)
    			getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo networkInfo = manager.getActiveNetworkInfo();
    	
    	boolean isAvailable = false;
    	if(networkInfo !=null && networkInfo.isConnected()){
    		isAvailable = true;
    	}
    	return isAvailable;
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
	public void updateList() {
		if(mBlogData == null){
			
			
		}
		else{
			try{
				JSONArray jsonPosts = mBlogData.getJSONArray("posts");
				mBlogPostTitles = new String[jsonPosts.length()];
				for(int i = 0; i < jsonPosts.length(); i++){
					JSONObject post = jsonPosts.getJSONObject(i);
					String title = post.getString("title");
					title = Html.fromHtml(title).toString();
					mBlogPostTitles[i] = title;
				}
				
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
						android.R.layout.simple_list_item_1, mBlogPostTitles);
				setListAdapter(adapter);
				
			}catch(JSONException e){
				Log.e(TAG, "Exception caught!", e);
			}
		}
	}
    
    private class GetBlogPostsTask extends AsyncTask<Object, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(Object... params) {
		    int responseCode = -1;
		    JSONObject jsonResponse = null;
		    StringBuilder builder = new StringBuilder();
		    HttpClient client = new DefaultHttpClient();
		    HttpGet httpget = new HttpGet("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS);

		    try {
		        HttpResponse response = client.execute(httpget);
		        StatusLine statusLine = response.getStatusLine();
		        responseCode = statusLine.getStatusCode();

		        if (responseCode == HttpURLConnection.HTTP_OK) {
		            HttpEntity entity = response.getEntity();
		            InputStream content = entity.getContent();
		            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
		            String line;
		            while((line = reader.readLine()) != null){
		                builder.append(line);
		            }

		            jsonResponse = new JSONObject(builder.toString());
		        }
		        else {
		            Log.i(TAG, String.format("Unsuccessful HTTP response code: %d", responseCode));
		        }
		    }
		    catch (JSONException e) {
		    	Log.e(TAG, "Exception caught: ", e);
		    }
		    catch (Exception e) {
		    	Log.e(TAG, "Exception caught: ", e);
		    }           

		    return jsonResponse;
		}   
		
		@Override
		protected void onPostExecute(JSONObject result){
			
			mBlogData = result;
			updateList();
		}
    	
    	
    }

}
