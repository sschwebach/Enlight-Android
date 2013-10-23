package edu.wisc.engr.enlight;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        doSetup();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    private void doSetup(){
    	final LinearLayout buttonArea = (LinearLayout) findViewById(R.id.button_layout);
    	for (int i = 1; i <= 24; i++){
    		ToggleButton newButton = new ToggleButton(this);
    		newButton.setText("" + i);
    		newButton.setTextOn("" + i);
    		newButton.setTextOff("" + i);
    		newButton.setPadding(0,  0, 10, 0);
    		buttonArea.addView(newButton);
    	}
    	Button resetButton = (Button) findViewById(R.id.refresh_button);
    	resetButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			ContactFountainTask task = new ContactFountainTask();
    			int sum = 0;
    			for (int i = 0; i < 22; i++){
    				if (((ToggleButton) buttonArea.getChildAt(i)).isChecked() && i != 10 && i != 11){
    					sum = sum + (int) Math.pow(2, i);
    				}
    			}
    			task.setValveRequest(sum);
    			//task.setValveRequest(((Spinner) findViewById(R.id.preset_spinner)).getSelectedItemPosition());
    			task.execute("Hello");
    	    	Log.e("Button", "Button pressed!");
    	    	
    		}
    	});
    	
    }
    
}