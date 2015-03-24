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
            processText(spokenText);


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * A crappy way to parse our stupid text. When I get around to it I might replace this
     * with a basic AI. Who knows. Currently it's just a bunch of hardcoded cases.
     * @param spokenText the input text that was spoken
     */
    private void processText(String spokenText){
        String[] tokens = spokenText.split(" ");
        int startNum = 0;
        int endNum = 0;
        boolean multiple = false;
        //See if we actually are trying to activate something
        if (tokens.length > 0) {
            if (tokens[0].equalsIgnoreCase("Activate")) {
                //see if we're trying to activate all the valves
                if (tokens.length > 2) {
                    if (tokens[1].equalsIgnoreCase("all") && tokens[2].contains("valve")) {
                        Toast toast = Toast.makeText(this, "Activating all valves", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    //now we must make sure that the second word is valve (no AI remember?)
                    if (tokens[1].contains("valve")) {
                        try {
                            startNum = Integer.parseInt(tokens[2]);
                            if (tokens.length > 4) {
                                if (tokens[3].equalsIgnoreCase("through") || tokens[3].equalsIgnoreCase("thru") && tokens.length > 4) {
                                    multiple = true;
                                    endNum = Integer.parseInt(tokens[4]);
                                }
                            }
                            if (!multiple) {
                                // Do something with spokenText
                                Toast toast = Toast.makeText(this, "Activating valve " + startNum, Toast.LENGTH_LONG);
                                toast.show();
                            } else {
                                Toast toast = Toast.makeText(this, "Activating valves " + startNum + " through " + endNum, Toast.LENGTH_LONG);
                                toast.show();
                            }
                        } catch (NumberFormatException e) {
                            Toast toast = Toast.makeText(this, "Invalid command format.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                }
            }
            //we might be trying to deactivate something
            else if (tokens[0].equalsIgnoreCase("Deactivate")) {
                if (tokens.length > 2) {
                    if (tokens[1].equalsIgnoreCase("all") && tokens[2].contains("valve")) {
                        Toast toast = Toast.makeText(this, "Deactivating all valves", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    if (tokens[1].contains("valve")) {
                        try {
                            startNum = Integer.parseInt(tokens[2]);
                            if (tokens.length > 4) {
                                if (tokens[3].equalsIgnoreCase("through") || tokens[3].equalsIgnoreCase("thru") && tokens.length > 4) {
                                    multiple = true;
                                    endNum = Integer.parseInt(tokens[4]);
                                }
                            }
                            if (!multiple) {
                                // Do something with spokenText
                                Toast toast = Toast.makeText(this, "Deactivating valve " + startNum, Toast.LENGTH_LONG);
                                toast.show();
                            } else {
                                Toast toast = Toast.makeText(this, "Deactivating valves " + startNum + " through " + endNum, Toast.LENGTH_LONG);
                                toast.show();
                            }
                        } catch (NumberFormatException e) {
                            Toast toast = Toast.makeText(this, "Invalid command format.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                }
            }
        }
    }

}
