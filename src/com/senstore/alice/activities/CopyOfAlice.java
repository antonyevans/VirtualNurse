package com.senstore.alice.activities;

import com.nuance.nmdp.speechkit.Prompt;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;
import com.nuance.nmdp.speechkit.Vocalizer;
import com.senstore.alice.R;
import com.senstore.alice.utils.AppInfo;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;
import android.content.*;
import android.media.AudioManager;

//packages for the threading
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;

//packages for the API calls
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class CopyOfAlice extends Activity {

    private static SpeechKit _speechKit;
    private static final int LISTENING_DIALOG = 0;
    private Handler _handler = null;
    private final Recognizer.Listener _listener;
    private Recognizer _currentRecognizer;
    private ListeningDialog _listeningDialog;
    private ArrayAdapter<String> _arrayAdapter;
    private boolean _destroyed;
    
    private Vocalizer _vocalizer;
    private Object _lastTtsContext = null;

    //variables required for asking questions
    private String user_ID = "1";
    private Integer chat_length;
    		
    
    //variables required for the http requests
    private TextWatcher textWatcher;

    private Handler guiThread;
    private ExecutorService askAliceThread;
    private Runnable questionTask;
    private Future questionPending;
    
    // Allow other activities to access the SpeechKit instance.
    static SpeechKit getSpeechKit()
    {
        return _speechKit;
    }

    private class SavedState
    {
        String DialogText;
        String DialogLevel;
        boolean DialogRecording;
        Recognizer Recognizer;
        Handler Handler;
        //added this next code
        //int TextColor;
        String Text;
        Vocalizer Vocalizer;
        Object Context;
    }

    public CopyOfAlice()
    {
        super();
        _listener = createListener();
        _currentRecognizer = null;
        _listeningDialog = null;
        _destroyed = true;
    }

    @Override
    protected void onPrepareDialog(int id, final Dialog dialog) {
        switch(id)
        {
        case LISTENING_DIALOG:
            _listeningDialog.prepare(new Button.OnClickListener()
            {
                
                public void onClick(View v) {
                    if (_currentRecognizer != null)
                    {
                        _currentRecognizer.stopRecording();
                    }
                }
            });
            break;
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id)
        {
        case LISTENING_DIALOG:
            return _listeningDialog;
        }
        return null;
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //create threads for the http requests
        initThreading();
        
        //set the chat length variable to 0
        chat_length = 0;

        //set volume control to media
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
   
        //Adjust the volume
        AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int max_volume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //audio.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);
        		
        
        // If this Activity is being recreated due to a config change (e.g. 
        // screen rotation), check for the saved SpeechKit instance.
        _speechKit = (SpeechKit)getLastNonConfigurationInstance();
        if (_speechKit == null)
        {
            _speechKit = SpeechKit.initialize(getApplication().getApplicationContext(), AppInfo.SpeechKitAppId, AppInfo.SpeechKitServer, AppInfo.SpeechKitPort, AppInfo.SpeechKitSsl, AppInfo.SpeechKitApplicationKey);
            _speechKit.connect();
            // TODO: Keep an eye out for audio prompts not working on the Droid 2 or other 2.2 devices.
            Prompt beep = _speechKit.defineAudioPrompt(R.raw.beep);
            _speechKit.setDefaultRecognizerPrompts(beep, Prompt.vibration(100), null, null);
        }
        _destroyed = false;
/*
        // Use the handler for the diagnosis button
        final Button diagnosisButton = (Button)findViewById(R.id.button1);
        Button.OnClickListener startDiagnosisListener = new Button.OnClickListener()
        {
            public void onClick(View v) {
            	setContentView(R.layout.diagnosis);
              }
        };
        diagnosisButton.setOnClickListener(startDiagnosisListener);

        // Use the handler for the dictation button
        final ImageButton dictationButton = (ImageButton)findViewById(R.id.btn_startDictation);
        ImageButton.OnClickListener startListener = new ImageButton.OnClickListener()
        {
            
            public void onClick(View v) {
            	_listeningDialog.setText("Initializing...");   
                showDialog(LISTENING_DIALOG);
            	_listeningDialog.setStoppable(false);
                setResults(new Recognition.Result[0]);
                
                 _currentRecognizer = Alice.getSpeechKit().createRecognizer(Recognizer.RecognizerType.Dictation, Recognizer.EndOfSpeechDetection.Long, "en_US", _listener, _handler);
                 _currentRecognizer.start();
            }
        };
        dictationButton.setOnClickListener(startListener);
*/
        // Set up the list to display multiple results
       // ListView list = (ListView)findViewById(R.id.list_results);
        //_arrayAdapter = new ArrayAdapter<String>(list.getContext(), R.layout.message);
//        StringBuffer mOutStringBuffer = new StringBuffer(""); 
        
        // *** Do we need this? ***
/**       {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Button b = (Button)super.getView(position, convertView, parent);
                b.setBackgroundColor(Color.GREEN);
                b.setOnClickListener(new Button.OnClickListener()
                {
*/                    
//                    public void onClick(View v) {
/**                        Button b = (Button)v;
                        EditText t = (EditText)findViewById(R.id.text_DictationResult);
                        
                        // Copy the text (without the [score]) into the edit box
                        String text = b.getText().toString();
                        int startIndex = text.indexOf("]: ");
                        t.setText(text.substring(startIndex > 0 ? (startIndex + 3) : 0));*/
//                   }
//                });
//                return b;
//            }   
//        };
        //list.setAdapter(_arrayAdapter);

        // Initialize the listening dialog
        createListeningDialog();
        
        // Create Vocalizer listener
        Vocalizer.Listener vocalizerListener = new Vocalizer.Listener()
        {
            
            public void onSpeakingBegin(Vocalizer vocalizer, String text, Object context) {
                updateCurrentText("Alice:  " + text, Color.GRAY, false);
            }

            
            public void onSpeakingDone(Vocalizer vocalizer,
                    String text, SpeechError error, Object context) 
            {
                // Use the context to detemine if this was the final TTS phrase
                if (context != _lastTtsContext)
                {
//                    updateCurrentText("More phrases remaining", Color.YELLOW, false);
                } else
                {
                    //updateCurrentText("", Color.WHITE, false);
                }
            }
        };
        _vocalizer = CopyOfAlice.getSpeechKit().createVocalizerWithLanguage("en_US", vocalizerListener, new Handler());
        _vocalizer.setVoice("Serena");

        SavedState savedState = (SavedState)getLastNonConfigurationInstance();
        if (savedState == null)
        {
            // Initialize the handler, for access to this application's message queue
            _handler = new Handler();
        } else
        {
            // There was a recognition in progress when the OS destroyed/
            // recreated this activity, so restore the existing recognition
            _currentRecognizer = savedState.Recognizer;
            _listeningDialog.setText(savedState.DialogText);
            _listeningDialog.setLevel(savedState.DialogLevel);
            _listeningDialog.setRecording(savedState.DialogRecording);
            _handler = savedState.Handler;
            
            if (savedState.DialogRecording)
            {
                // Simulate onRecordingBegin() to start animation
                _listener.onRecordingBegin(_currentRecognizer);
            }
            
            _currentRecognizer.setListener(_listener);

        }
        
        speakReply("Welcome to the Pocket Doctor. I am Alice, how can I help you today? You can click on the microphone to talk to me.");
    }
    
    public void startDictation(View view) {
    	_listeningDialog.setText("Initializing...");   
        showDialog(LISTENING_DIALOG);
    	_listeningDialog.setStoppable(false);
        setResults(new Recognition.Result[0]);
        
         _currentRecognizer = CopyOfAlice.getSpeechKit().createRecognizer(Recognizer.RecognizerType.Dictation, Recognizer.EndOfSpeechDetection.Long, "en_US", _listener, _handler);
         _currentRecognizer.start();
    }

    public void showDiagnosis(View view) 
    {  
    	 //setContentView(R.layout.diagnosis);
    }
    
    public void showMain(View view) 
    {      	
    	 setContentView(R.layout.main); 
    }
    
    private void updateCurrentText(String text, int color, boolean onlyIfBlank)
    {
        //TextView newText = (TextView)findViewById(R.id.text_currentTts);
        //newText.setMovementMethod(ScrollingMovementMethod.getInstance());

        //String dialogue = "Me:  " + newText.getText().toString() + "\n" + text;
        //String dialogue = "Me:  " + text;
       // String message = newText.getText().toString();
       // sendMessage(message);
        
        if (text.length() > 0)
//        if (!onlyIfBlank || newText.getText().length() == 0)
        {
            //newText.setTextColor(color);
            //newText.setText(dialogue);
            //newText.setText(text);
            _arrayAdapter.add(text);
        }
    }

/*    
    private void updateCurrentText(String text, int color, boolean onlyIfBlank)
    {
        TextView newText = (TextView)findViewById(R.id.text_currentTts);
        newText.setMovementMethod(ScrollingMovementMethod.getInstance());

        String dialogue = newText.getText().toString() + "\n" + text;
        
        if (!onlyIfBlank || newText.getText().length() == 0)
        {
            newText.setTextColor(color);
            newText.setText(dialogue);
        }
    }
*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _destroyed = true;
        if (_currentRecognizer !=  null)
        {
            _currentRecognizer.cancel();
            _currentRecognizer = null;
        }
        if (_vocalizer != null)
        {
            _vocalizer.cancel();
            _vocalizer = null;
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance()
    {
        if (_listeningDialog.isShowing() && _currentRecognizer != null)
        {
            // If a recognition is in progress, save it, because the activity
            // is about to be destroyed and recreated
            SavedState savedState = new SavedState();
            savedState.Recognizer = _currentRecognizer;
            savedState.DialogText = _listeningDialog.getText();
            savedState.DialogLevel = _listeningDialog.getLevel();
            savedState.DialogRecording = _listeningDialog.isRecording();
            savedState.Handler = _handler;
            
            _currentRecognizer = null; // Prevent onDestroy() from canceling
            
            // Save the Vocalizer state, because we know the Activity will be
            // immediately recreated.
//            TextView textView = (TextView)findViewById(R.id.text_currentTts);
            
//            savedState.Text = textView.getText().toString();
            //savedState.TextColor = textView.getTextColors().getDefaultColor();
            savedState.Vocalizer = _vocalizer;
            savedState.Context = _lastTtsContext;

            _vocalizer = null; // Prevent onDestroy() from canceling
            
            return savedState;
        }
        return null;
    }

    private Recognizer.Listener createListener()
    {
        return new Recognizer.Listener()
        {            
            
            public void onRecordingBegin(Recognizer recognizer) 
            {
                _listeningDialog.setText("Recording...");
            	_listeningDialog.setStoppable(true);
                _listeningDialog.setRecording(true);
                
                // Create a repeating task to update the audio level
                Runnable r = new Runnable()
                {
                    public void run()
                    {
                        if (_listeningDialog != null && _listeningDialog.isRecording() && _currentRecognizer != null)
                        {
                            _listeningDialog.setLevel(Float.toString(_currentRecognizer.getAudioLevel()));
                            _handler.postDelayed(this, 500);
                        }
                    }
                };
                r.run();
            }

            
            public void onRecordingDone(Recognizer recognizer) 
            {
                _listeningDialog.setText("Processing...");
                _listeningDialog.setLevel("");
                _listeningDialog.setRecording(false);
            	_listeningDialog.setStoppable(false);
            }

            
            public void onError(Recognizer recognizer, SpeechError error) 
            {
            	if (recognizer != _currentRecognizer) return;
            	if (_listeningDialog.isShowing()) dismissDialog(LISTENING_DIALOG);
                _currentRecognizer = null;
                _listeningDialog.setRecording(false);

                // Display the error + suggestion in the edit box
                String detail = error.getErrorDetail();
                String suggestion = error.getSuggestion();
                
                if (suggestion == null) suggestion = "";
                updateCurrentText(detail + "\n" + suggestion, Color.GREEN, false);
            }

           
            public void onResults(Recognizer recognizer, Recognition results) {
                if (_listeningDialog.isShowing()) dismissDialog(LISTENING_DIALOG);
                _currentRecognizer = null;
                _listeningDialog.setRecording(false);
                int count = results.getResultCount();
                Recognition.Result [] rs = new Recognition.Result[count];
                for (int i = 0; i < count; i++)
                {
                    rs[i] = results.getResult(i);
                }
                setResults(rs);
            }
        };
    }
    
    private void setResults(Recognition.Result[] results)
    {
        //_arrayAdapter.clear();
        if (results.length > 0)
        {
            //setResult(results[0].getText());
        	String t = results[0].getText();
        	String dialogue = "Me:  " + t;
        	updateCurrentText(dialogue, Color.WHITE, false);
            //speakReply(askAlice(t));
         }  else
        {
           // setResult("");
        }
    }

    private void initThreading() {
        guiThread = new Handler();
        askAliceThread = Executors.newSingleThreadExecutor();
        
       
        //prepare question to ask alice
        questionTask = new Runnable() {
        	public void run() {
        		//do nothing for now
        	}
        };
    }
    
    private String askAlicez(String input) 
    {
    	String result = "";
    	HttpURLConnection con = null;
        Log.i("Ask Alice",  input);
    	
        try {
            // Check if task has been interrupted
            //if (Thread.interrupted())
            // throw new InterruptedException();

            // Build RESTful query for Alice_Brain API
            String q = URLEncoder.encode(input, "UTF-8");
            URL url = new URL(
            		"http://sharp-waterfall-5241.herokuapp.com/post.xml?input=" + URLEncoder.encode(input) 
            			+ "&user_id=" + URLEncoder.encode(user_ID) + "&chat_length=" + chat_length );
            con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(10000 /* milliseconds */);
            con.setConnectTimeout(15000 /* milliseconds */);
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            //tell the server what we are sending is xml
            con.setRequestProperty ( "Content-Type", "text/xml" );
            con.setUseCaches (false);
            con.setDefaultUseCaches (false);

            
            // Check if task has been interrupted
            //if (Thread.interrupted())
              // throw new InterruptedException();

    		SAXParserFactory spf = SAXParserFactory.newInstance();
    		SAXParser sp = spf.newSAXParser();

    		XMLReader xmlReader = sp.getXMLReader();
    		//ResponseParser rp = new ResponseParser();
    		//xmlReader.setContentHandler(rp);

    		xmlReader.parse(new InputSource(con.getInputStream()));
    		
    		//result = rp.reply;
    		chat_length = chat_length +1;
            // Read results from the query
            //BufferedReader reader = new BufferedReader(
            //      new InputStreamReader(con.getInputStream(), "UTF-8"));
            //String payload = reader.readLine();
            //reader.close();

            
            // Parse to get translated text
            //JSONObject jsonObject = new JSONObject(payload);
            //result = reader.readLine();

            // Check if task has been interrupted
            if (Thread.interrupted())
               throw new InterruptedException();

         } catch (IOException e) {
            Log.e("error", "IOException", e);
         } /*catch (JSONException e) {
            Log.e("error", "JSONException", e);
         } */catch (InterruptedException e) {
            Log.d("error", "InterruptedException", e);
            //result = translate.getResources().getString(
              //    R.string.translation_interrupted);
         } catch (ParserConfigurationException e) {
			throw new IllegalStateException("Error loading XML parser", e);
		} catch (SAXException e) {
			throw new IllegalStateException("Error parsing XML", e);
		} finally {
            if (con != null) {
               con.disconnect();
            }
         }
        
    	return result;
    }
    
    private void speakReply(String Reply)
    {
        _lastTtsContext = new Object();
        _vocalizer.speakString(Reply, _lastTtsContext); 
    }

    private void createListeningDialog()
    {
        _listeningDialog = new ListeningDialog(this);
        _listeningDialog.setOnDismissListener(new OnDismissListener()
        {
            public void onDismiss(DialogInterface dialog) {
                if (_currentRecognizer != null) // Cancel the current recognizer
                {
                    _currentRecognizer.cancel();
                    _currentRecognizer = null;
                }
                
                if (!_destroyed)
                {
                    // Remove the dialog so that it will be recreated next time.
                    // This is necessary to avoid a bug in Android >= 1.6 where the 
                    // animation stops working.
                    CopyOfAlice.this.removeDialog(LISTENING_DIALOG);
                    createListeningDialog();
                }
            }
        });
    }    
}