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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
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
	private String apiKey = "abc123";
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
    public static final int QUERYPOSITION = 9;
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

    public void queryPosition(){
        FountainControlTask requestPositionTask = new FountainControlTask();
        requestPositionTask.requestControl = QUERYPOSITION;
        requestPositionTask.isPost = true;
        requestPositionTask.addNVP(new BasicNameValuePair("apikey", "" + apiKey));
        requestPositionTask.addNVP(new BasicNameValuePair("controllerID", "" + userIDs.get(0)));
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


	private class FountainControlTask extends AsyncTask<String, Void, String>{

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		int requestControl = -1; //0 = request, 1 = query, 2 = release
		boolean isPost = false;
        JSONObject json = new JSONObject();

		public void addNVP(NameValuePair toAdd){
            try{
                json.put(toAdd.getName(), toAdd.getValue());
            }catch (Exception e){
                Log.e("JSON Exception", e.toString());
            }
		}
		@Override
		protected void onPreExecute(){
			mActivity.reloadProgress.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... arg0) {
			if (isPost){
				//control task
				try{
					HttpClient client = new DefaultHttpClient();
					HttpPost post = new HttpPost(arg0[0]);
                    StringEntity se = new StringEntity(json.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
					post.setEntity(se);
					HttpResponse response = client.execute(post);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    String json = reader.readLine();
					return json;
				}catch (Exception e){
					Log.e("Exception", e.toString());
				}
			}else if (!isPost){
				//query task
				try {
					HttpClient client = new DefaultHttpClient();
					HttpGet get = new HttpGet(arg0[0]);
					HttpResponse response = client.execute(get);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    String json = reader.readLine();
					return json;
				} catch (Exception e) {
					Log.e("BackgroundTaskException", e.toString());
				}
			}else{
				Log.e("TaskError", "Task executed in a bad state");
			}
			return null;
		}

		@Override
		protected void onPostExecute(String response){
			pairs.clear();
            if (response != null){
                Log.e("RESPONSE", requestControl + response.toString());
            }


			if (response == null){
				//bad request, check internet connection
				mActivity.pDialog.hide();
				mActivity.reloadProgress.setVisibility(View.INVISIBLE);
				return;
			}
			JSONArray finalResult = new JSONArray();
            JSONObject finalObject = new JSONObject();
            try{
                finalResult = new JSONArray(response);
            }catch (JSONException e){
                //must be a single object?
            }
            try{
                finalObject = new JSONObject(response);
            }catch (JSONException e){

            }
			try{
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
					currJSON = finalObject;
                    Log.e("CURRJSON", currJSON.toString());
					success = Boolean.parseBoolean(currJSON.getString("success"));
					priority = currJSON.getInt("priority");
					expires = currJSON.getInt("ttl");
					id = currJSON.getInt("controllerID");
					position = currJSON.getInt("queuePosition");
					//now that we have the data, make sure we remember it
					if (success){
						userIDs.add(id);
						reqControl = true;
						if (position == 0){
							changeControl(true, true);
							//got control right when we requested it
						}else{
							changeControl(false, true);
							//control requested
						}
					}
					
					break;
				case QUERYCONTROL:
					queue.clear();
					for (int i = 0; i < finalResult.length(); i++){
						currJSON = finalResult.getJSONObject(i);
						id = currJSON.getInt("controllerID");
						acquired = currJSON.getInt("acquire");
						expires = currJSON.getInt("ttl");
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
						for (int j = 0; j < queue.size(); j++){
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
						changeControl(false, false);
					}else if (bestPosition > 0){
						changeControl(false, true);
						//control has been requested, but is not acquired
					}else if (bestPosition == 0){
						changeControl(true, true);
						//control is acquired
					}
					//reassign the list with old id's removed
					userIDs = newList;

					break;
                case QUERYPOSITION:
                    currJSON = finalResult.getJSONObject(0);
                    success = currJSON.getBoolean("success");
                    int trueQueuePos = currJSON.getInt("trueQueuePosition");
                    int eta = currJSON.getInt("eta");
                    if (success && trueQueuePos == 0){
                        //got control
                        changeControl(true, true);
                    }else if (success && trueQueuePos != 0){
                        //still dont' have control, but has been requested
                        changeControl(false, true);
                    }else{
                        userIDs.remove(0);
                        changeControl(false, false);
                    }
                    break;
				case RELEASECONTROL:
					currJSON = finalResult.getJSONObject(0);
					success = currJSON.getBoolean("success");
					if (!success){
						//TODO some error message
					}else{
						changeControl(false, true);
					}
					break;
				case QUERYALLVALVES:
					for (int i = 0; i < finalResult.length(); i++){
						currJSON = finalResult.getJSONObject(i);
						id = currJSON.getInt("ID");
						int spraying = currJSON.getInt("spraying"); //fuck the police
                        if (spraying == 1){
                            mActivity.valveStates[id - 1] = true;
                        }else{
                            mActivity.valveStates[id - 1] = false;
                        }
						
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
                Log.e("Result Exception", "", e);
				Log.e("JSON", finalResult.toString());
			}finally{
				if (isLast){
					//mActivity.refresh();
					mActivity.reloadProgress.setVisibility(View.INVISIBLE);
					mActivity.pDialog.hide();
				}
			}
		}
	}
	
	/**
	 * Updates the UI whenever control changes
	 * @param hasControl if the user has control
	 * @param reqControl if the user has requested control regardless if they
	 * have it
	 */
	public void changeControl(boolean hasControl, boolean reqControl){
		if (!hasControl && !reqControl){
			//control isn't even requested
			this.reqControl = false;
			this.hasControl = false;
			mActivity.patternSpinner.setVisibility(View.GONE);
			mActivity.hasControl = false;
			mActivity.rightFountain.hasControl = false;
			mActivity.leftFountain.hasControl = false;
			mActivity.statusText.setText("Fountain Status");
			mActivity.refreshTime.setText("Request control to gain access.");
			mActivity.controlRequested = false;
			mActivity.sendButton.setText("Request Control");
		}else if (!hasControl && reqControl){
			//control has been requested, but is not acquired
			this.reqControl = true;
			this.hasControl = false;
			mActivity.patternSpinner.setVisibility(View.GONE);
			mActivity.hasControl = false;
			mActivity.rightFountain.hasControl = false;
			mActivity.leftFountain.hasControl = false;
			mActivity.statusText.setText("Waiting for Control");
			mActivity.refreshTime.setText("Another user has control. Please wait.");
			mActivity.controlRequested = true;
			mActivity.sendButton.setText("Please Wait");
		}else{
			//control is acquired
			this.hasControl = true;
			this.reqControl = reqControl;
			mActivity.patternSpinner.setVisibility(View.VISIBLE);
			mActivity.hasControl = true;
			mActivity.rightFountain.hasControl = true;
			mActivity.leftFountain.hasControl = true;
			mActivity.statusText.setText("You Have Control");
			mActivity.refreshTime.setText("Tap a valve to activate it or send a pattern.");
			mActivity.sendButton.setText("Release Control");
		}
	}

}