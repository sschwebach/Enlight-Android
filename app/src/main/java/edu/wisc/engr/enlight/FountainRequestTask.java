package edu.wisc.engr.enlight;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.util.Log;

public class FountainRequestTask extends AsyncTask<String, Void, String> {

	private Exception exception;
	private int valveReq;
	
	public void setValveRequest(int req){
		this.valveReq = req;
	}

	protected String doInBackground(String... requests) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(Utilities.URL);
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("request", "setValveState"));
			pairs.add(new BasicNameValuePair("STATE", "" + valveReq));
			try{
				post.setEntity(new UrlEncodedFormEntity(pairs));
				HttpResponse response = client.execute(post);
				return response.toString();
			} catch (Exception e) {
				this.exception = e;
				Log.e("Exceptoin", e.toString());
				return null;
			}
		} catch (Exception e) {
			this.exception = e;
			Log.e("Exception", e.toString());
			return null;
		}
	}

	protected void onPostExecute(String s) {
		// TODO: check this.exception 
		// TODO: do something with the feed
	}
}