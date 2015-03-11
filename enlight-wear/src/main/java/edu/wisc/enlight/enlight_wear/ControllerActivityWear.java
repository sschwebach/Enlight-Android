package edu.wisc.enlight.enlight_wear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;


public class ControllerActivityWear extends Activity {
    private static final int SPEECH_REQUEST_CODE = 0;
    Button voiceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                doSetup(stub);
            }
        });
    }

    public void doSetup(WatchViewStub stub) {
        voiceButton = (Button) stub.findViewById(R.id.button_voice);
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySpeechRecognizer();
            }
        });

    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }


    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            String[] tokens = spokenText.split(" ");
            //See if we actually are trying to activate something
            if (tokens[0].equalsIgnoreCase("Activate")) {
                if (tokens[1].contains("valve")){
                    try {
                        int valve1 = Integer.parseInt(tokens[2]);
                        // Do something with spokenText
                        Toast toast = Toast.makeText(this, "Activating valve " + valve1, Toast.LENGTH_LONG);
                        toast.show();
                    }catch (NumberFormatException e){
                        Toast toast = Toast.makeText(this, tokens[2] + " is an invalid valve number", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
