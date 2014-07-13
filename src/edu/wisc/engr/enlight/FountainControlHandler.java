package edu.wisc.engr.enlight;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
/**
 * This class controls the fountain control. Queries the server to see if the 
 * user can gain control and requests control from the server.
 * @author Sam
 *
 */
public class FountainControlHandler {
	private Context mContext;
	private MainActivity mActivity;
	private String apiKey = "00000";
	public ProgressDialog progress;
	public boolean hasControl = false;
	public int userId = -1;
	public boolean isLast = true;
	public static final int REQUESTCONTROL = 0;
	public static final int QUERYCONTROL = 1;
	public static final int RELEASECONTROL = 2;
	public static final int QUERYALLVALVES = 3;
	public static final int SETALLVALVES = 4;
	public static final int	QUERYSINGLEVALVE = 5;
	public static final int SETSINGLEVALVE = 6;

	public FountainControlHandler(Context c){
		this.mContext = c;
		this.mActivity = (MainActivity) c;
		progress = new ProgressDialog(this.mContext);
	}

	//TODO
	//The following code is awfully repetitive...
	//should probably condense it in the future.

	/**
	 * A full refresh of the UI. Gets the status of the valves and the queue
	 * at the same time. Only refreshes the UI once.
	 */
	public void fullRefresh(){
		isLast = false;
		queryControl();
		isLast = true;
		queryAllValves();
	}

	/**
	 * This method is called to request control from the fountain.
	 */
	public void requestControl(){
		FountainControlTask requestControlTask = new FountainControlTask();
		requestControlTask.addNVP(new BasicNameValuePair("apikey", "" + apiKey));
		requestControlTask.addNVP(new BasicNameValuePair("requestedLength", "15"));
		requestControlTask.requestControl = REQUESTCONTROL;
		requestControlTask.isPost = true;
		progress.setTitle("Please Wait");
		progress.setMessage("Requesting Control...");
		progress.show();
		requestControlTask.execute(Utilities.requestControlURL);
	}

	/**
	 * This method is called to query the current state of control and the 
	 * queue for control.
	 */
	public void queryControl(){
		FountainControlTask requestQueryTask = new FountainControlTask();
		requestQueryTask.requestControl = QUERYCONTROL;
		requestQueryTask.isPost = false;
		progress.setTitle("Please Wait");
		progress.setMessage("Checking Status...");
		progress.show();
		requestQueryTask.execute(Utilities.queryURL);
	}

	/**
	 * This method is called to release control early.
	 */
	public void releaseControl(){
		FountainControlTask releaseControlTask = new FountainControlTask();
		releaseControlTask.requestControl = RELEASECONTROL;
		releaseControlTask.isPost = true;
		releaseControlTask.addNVP(new BasicNameValuePair("apikey", "" + apiKey));
		progress.setTitle("Please Wait");
		progress.setMessage("Releasing Control...");
		progress.show();
		releaseControlTask.execute(Utilities.releaseControlURL);
	}

	public void queryAllValves(){
		FountainControlTask valveTask = new FountainControlTask();
		valveTask.requestControl = QUERYALLVALVES;
		valveTask.isPost = false;
		progress.setTitle("Please Wait");
		progress.setMessage("Getting valve states...");
		progress.show();
		valveTask.execute(Utilities.valvesURL);
	}

	public void setAllValves(int bitmask){
		FountainControlTask valveTask = new FountainControlTask();
		valveTask.requestControl = SETALLVALVES;
		valveTask.isPost = true;
		valveTask.addNVP(new BasicNameValuePair("apikey", "" + apiKey));
		valveTask.addNVP(new BasicNameValuePair("bitmask", "" + bitmask));
		progress.setTitle("Please Wait");
		progress.setMessage("Setting Valve States...");
		progress.show();
		valveTask.execute(Utilities.valvesURL);
	}

	public void querySingleValve(int valveID){
		FountainControlTask valveTask = new FountainControlTask();
		valveTask.requestControl = QUERYSINGLEVALVE;
		valveTask.isPost = false;
		progress.setTitle("Please Wait");
		progress.setMessage("Getting State of Valve " + valveID + "...");
		progress.show();
		valveTask.execute(Utilities.valvesURL + "/" + valveID);
	}

	public void setSingleValve(int valveID, boolean on){
		FountainControlTask valveTask = new FountainControlTask();
		valveTask.requestControl = SETSINGLEVALVE;
		valveTask.isPost = true;
		valveTask.addNVP(new BasicNameValuePair("apikey", "" + apiKey));
		valveTask.addNVP(new BasicNameValuePair("spraying", "" + on));
		progress.setTitle("Please Wait");
		if (on){
			progress.setMessage("Getting State of Valve " + valveID + "to on...");
		}else{
			progress.setMessage("Getting State of Valve " + valveID + "to off...");
		}
		progress.show();
		valveTask.execute(Utilities.valvesURL + "/" + valveID);
	}
	/**
	 * A setter method for APIKey. In android it's probably better to just 
	 * do it directly, so it probably won't be used.
	 * @param newKey
	 */
	public void setApiKey(String newKey){
		this.apiKey = newKey;
	}


	private class FountainControlTask extends AsyncTask<String, Void, HttpResponse>{

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		int requestControl = -1; //0 = request, 1 = query, 2 = release
		boolean isPost = false;

		public void addNVP(NameValuePair toAdd){
			pairs.add(toAdd);
		}

		@Override
		protected HttpResponse doInBackground(String... arg0) {
			if (isPost){
				//control task
				try{
					HttpClient client = new DefaultHttpClient();
					HttpPost post = new HttpPost(arg0[0]);
					post.setEntity(new UrlEncodedFormEntity(pairs));
					HttpResponse response = client.execute(post);
					return response;
				}catch (Exception e){
					Log.e("Exception", e.toString());
				}
			}else if (!isPost){
				//query task
				try {
					HttpClient client = new DefaultHttpClient();
					HttpGet get = new HttpGet(arg0[0]);
					HttpResponse response = client.execute(get);
					return response;
				} catch (Exception e) {
					Log.e("Exception", e.toString());
				}
			}else{
				Log.e("TaskError", "Task executed in a bad state");
			}
			return null;
		}

		@Override
		protected void onPostExecute(HttpResponse response){
			pairs.clear();

			if (response == null){
				//bad request, check internet connection
				return;
			}
			try{
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
				String json = reader.readLine();
				JSONTokener tokener = new JSONTokener(json);
				JSONArray finalResult = new JSONArray(tokener);
				JSONObject currJSON;
				boolean success;
				//TODO
				switch (requestControl){
				case REQUESTCONTROL:	
					break;
				case QUERYCONTROL:
					for (int i = 0; i < finalResult.length(); i++){
						//TODO build a queue
					}
					break;
				case RELEASECONTROL:
					currJSON = finalResult.getJSONObject(0);
					success = currJSON.getBoolean("success");
					if (!success){
						//TODO some error message
					}
					break;
				case QUERYALLVALVES:
					for (int i = 0; i < finalResult.length(); i++){
						currJSON = finalResult.getJSONObject(i);
						int id = currJSON.getInt("id");
						boolean spraying = currJSON.getBoolean("spraying");
						mActivity.valveStates[id - 1] = spraying;
					}
					break;
				case SETALLVALVES:
					currJSON = finalResult.getJSONObject(0);
					success = currJSON.getBoolean("success");
					if (!success){
						//TODO some error message
					}
					break;
				case QUERYSINGLEVALVE:
					currJSON = finalResult.getJSONObject(0);
					int id = currJSON.getInt("id");
					boolean spraying = currJSON.getBoolean("spraying");
					mActivity.valveStates[id - 1] = spraying;
					break;
				case SETSINGLEVALVE:
					currJSON = finalResult.getJSONObject(0);
					success = currJSON.getBoolean("success");
					if (!success){
						//TODO some error message
					}
					break;
				default:
					break;
				}
				//do something with this result
			}catch (Exception e){
				Log.e("Exception", e.toString());
			}finally{
				if (isLast){
					mActivity.refresh();
				}
				progress.hide();
			}
		}
	}

}
