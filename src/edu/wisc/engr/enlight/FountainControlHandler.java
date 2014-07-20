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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
/**
 * This class controls the fountain control. Queries the server to see if the 
 * user can gain control and requests control from the server.
 * @author Sam
 *
 */
public class FountainControlHandler {
	private Context mContext;
	private ProgressDialog mDialog;
	private MainActivity mActivity;
	private String apiKey = "00000";
	public boolean hasControl = false;
	public boolean reqControl = false;
	public int userId = -1;
	public int userPosition = -1;
	public int userPrio = -1;
	public boolean isLast = true;
	public static final int REQUESTCONTROL = 0;
	public static final int QUERYCONTROL = 1;
	public static final int RELEASECONTROL = 2;
	public static final int QUERYALLVALVES = 3;
	public static final int SETALLVALVES = 4;
	public static final int	QUERYSINGLEVALVE = 5;
	public static final int SETSINGLEVALVE = 6;
	public static final int GETPATTERN = 7;
	public static final int SETPATTERN = 8;
	FountainViewCanvas leftView;
	FountainViewCanvas rightView;
	ArrayList<Integer> userIDs;
	ArrayList<UserEntry> queue;

	public FountainControlHandler(Context c){
		this.mContext = c;
		this.mActivity = (MainActivity) c;
		mDialog = mActivity.pDialog;
		userIDs = new ArrayList<Integer>();
		queue = new ArrayList<UserEntry>();
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
		releaseControlTask.execute(Utilities.releaseControlURL);
	}

	/**
	 * This method is called to get the state of all the valves
	 */
	public void queryAllValves(){
		FountainControlTask valveTask = new FountainControlTask();
		valveTask.requestControl = QUERYALLVALVES;
		valveTask.isPost = false;
		valveTask.execute(Utilities.valvesURL);
	}

	/**
	 * This method is used to set all the valves at once.
	 * @param bitmask The bitmask of which valves should be on (1 = on)
	 */
	public void setAllValves(int bitmask){
		FountainControlTask valveTask = new FountainControlTask();
		valveTask.requestControl = SETALLVALVES;
		valveTask.isPost = true;
		valveTask.addNVP(new BasicNameValuePair("apikey", "" + apiKey));
		valveTask.addNVP(new BasicNameValuePair("bitmask", "" + bitmask));
		valveTask.execute(Utilities.valvesURL);
	}

	/**
	 * This method is called to get the status of a single valve
	 * @param valveID The id of the valve
	 */
	public void querySingleValve(int valveID){
		FountainControlTask valveTask = new FountainControlTask();
		valveTask.requestControl = QUERYSINGLEVALVE;
		valveTask.isPost = false;
		valveTask.execute(Utilities.valvesURL + "/" + valveID);
	}

	/**
	 * This method is called to set the status of a single valve
	 * @param valveID The id of the valve
	 * @param on true = on, false = off
	 */
	public void setSingleValve(int valveID, boolean on){
		FountainControlTask valveTask = new FountainControlTask();
		valveTask.requestControl = SETSINGLEVALVE;
		valveTask.isPost = true;
		valveTask.addNVP(new BasicNameValuePair("apikey", "" + apiKey));
		valveTask.addNVP(new BasicNameValuePair("spraying", "" + on));
		valveTask.execute(Utilities.valvesURL + "/" + valveID);
	}

	/**
	 * This method is used to get all the available patterns, as well as 
	 * the currently active pattern (if there is any)
	 */
	public void getPattern(){
		FountainControlTask patternTask = new FountainControlTask();
		patternTask.requestControl = GETPATTERN;
		patternTask.isPost = false;
		patternTask.execute(Utilities.allPatternsURL);
	}

	/**
	 * This method is used to set a certain pattern.
	 * @param id The id of the pattern.
	 */
	public void setPattern(int id){
		//TODO ask Alex how to send "turn patterns off"
		FountainControlTask patternTask = new FountainControlTask();
		patternTask.requestControl = SETPATTERN;
		patternTask.isPost = true;
		patternTask.addNVP(new BasicNameValuePair("apikey", "" + apiKey));
		patternTask.addNVP(new BasicNameValuePair("setCurrent", "true"));
		patternTask.execute(Utilities.singlePatternsURL + "/" + id);
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
		protected void onPreExecute(){
			mActivity.reloadProgress.setVisibility(View.VISIBLE);
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
				mActivity.pDialog.hide();
				mActivity.reloadProgress.setVisibility(View.INVISIBLE);
				return;
			}
			try{
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
				String json = reader.readLine();
				JSONTokener tokener = new JSONTokener(json);
				JSONArray finalResult = new JSONArray(tokener);
				JSONObject currJSON;
				boolean success;
				int priority;
				int id;
				int acquired;
				int expires;
				int position;
				//TODO
				switch (requestControl){
				case REQUESTCONTROL:	
					//TODO
					currJSON = finalResult.getJSONObject(0);
					success = currJSON.getBoolean("success");
					priority = currJSON.getInt("priority");
					expires = currJSON.getInt("expires");
					id = currJSON.getInt("controllerID");
					position = currJSON.getInt("queuePosition");
					//now that we have the data, make sure we remember it
					if (success){
						userIDs.add(id);
						reqControl = true;
						if (position == 0){
							//got control right when we requested it
							hasControl = true;
							mActivity.hasControl = true;
							mActivity.rightFountain.hasControl = true;
							mActivity.leftFountain.hasControl = true;
							mActivity.statusText.setText("You Have Control");
							mActivity.refreshTime.setText("Tap a valve to activate it or send a pattern.");
							mActivity.sendButton.setText("Release Control");
						}else{
							reqControl = true;
							mActivity.hasControl = false;
							mActivity.rightFountain.hasControl = false;
							mActivity.leftFountain.hasControl = false;
							mActivity.statusText.setText("Waiting for Control");
							mActivity.refreshTime.setText("Another user has control. Please wait.");
							mActivity.controlRequested = true;
							mActivity.sendButton.setText("Please Wait");
						}
					}
					
					break;
				case QUERYCONTROL:
					queue.clear();
					for (int i = 0; i < finalResult.length(); i++){
						currJSON = finalResult.getJSONObject(i);
						id = currJSON.getInt("controllerID");
						acquired = currJSON.getInt("acquired");
						expires = currJSON.getInt("expires");
						priority = currJSON.getInt("priority");
						position = currJSON.getInt("queuePosition");
						queue.add(new UserEntry(id, acquired, expires, priority, position));
						//TODO build a queue AND SET THE ACTIVITY'S QUEUE TO THIS
					}
					//Now that we have the queue, see if any of our userids are in the queue
					int bestPosition = Integer.MAX_VALUE;
					ArrayList<Integer> newList = new ArrayList<Integer>();
					//find any entries matching one of our user ids in the list
					for (int i = 0; i < userIDs.size(); i++){
						boolean found = false;
						for (int j = 0; i < queue.size(); i++){
							if (queue.get(j).id == userIDs.get(i)){
								//we found a user
								found = true;
								//see if this is the best position found
								if (queue.get(j).position < bestPosition){
									bestPosition = queue.get(j).position;
								}
							}
						}
						if (found){
							//old id, add to old id list
							newList.add(i);
						}
					}
					if (bestPosition == Integer.MAX_VALUE){
						//control isn't even requested
						reqControl = false;
						mActivity.hasControl = false;
						mActivity.rightFountain.hasControl = false;
						mActivity.leftFountain.hasControl = false;
						mActivity.statusText.setText("Fountain Status");
						mActivity.refreshTime.setText("Request control to gain access.");
						mActivity.controlRequested = false;
						mActivity.sendButton.setText("Request Control");
					}else if (bestPosition > 0){
						reqControl = true;
						mActivity.hasControl = false;
						mActivity.rightFountain.hasControl = false;
						mActivity.leftFountain.hasControl = false;
						mActivity.statusText.setText("Waiting for Control");
						mActivity.refreshTime.setText("Another user has control. Please wait.");
						mActivity.controlRequested = true;
						mActivity.sendButton.setText("Please Wait");
					}else if (bestPosition == 0){
						hasControl = true;
						mActivity.hasControl = true;
						mActivity.rightFountain.hasControl = true;
						mActivity.leftFountain.hasControl = true;
						mActivity.statusText.setText("You Have Control");
						mActivity.refreshTime.setText("Tap a valve to activate it or send a pattern.");
						mActivity.sendButton.setText("Release Control");
					}
					//reassign the list with old id's removed
					userIDs = newList;

					break;
				case RELEASECONTROL:
					currJSON = finalResult.getJSONObject(0);
					success = currJSON.getBoolean("success");
					if (!success){
						//TODO some error message
					}else{
						//Make sure the canvas knows that control is lost
						hasControl = false;
						mActivity.hasControl = false;
						mActivity.rightFountain.hasControl = false;
						mActivity.leftFountain.hasControl = false;
						mActivity.statusText.setText("Fountain Status");
						mActivity.refreshTime.setText("Request control to gain access.");
					}
					break;
				case QUERYALLVALVES:
					for (int i = 0; i < finalResult.length(); i++){
						currJSON = finalResult.getJSONObject(i);
						id = currJSON.getInt("id");
						boolean spraying = currJSON.getBoolean("spraying");
						mActivity.valveStates[id - 1] = spraying;
						
					}
					if (!hasControl){
						//refresh the canvas views if the user doesn't have control
						mActivity.leftFountain.setValves(mActivity.valveStates);
						mActivity.rightFountain.setValves(mActivity.valveStates);
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
					id = currJSON.getInt("id");
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
				case GETPATTERN:
					ArrayList<Pattern> patterns = new ArrayList<Pattern>();
					for (int i = 0; i < finalResult.length(); i++){
						currJSON = finalResult.getJSONObject(i);
						int patternID = currJSON.getInt("id");
						String patternName = currJSON.getString("name");
						boolean isActive = currJSON.getBoolean("active");
						//TODO define a pattern object and make one here
						patterns.add(new Pattern(patternID, patternName, isActive));
					}
					mActivity.patterns = patterns;
					mActivity.refresh();
					//TODO now that we have all these patterns do something
					//with it (most likely in the activity)
					break;
				case SETPATTERN:
					//TODO
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
					mActivity.reloadProgress.setVisibility(View.INVISIBLE);
					mActivity.pDialog.hide();
					Log.e("Pdialog", "HIDE");
				}
			}
		}
	}

}
