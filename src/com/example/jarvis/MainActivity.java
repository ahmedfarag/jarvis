package com.example.jarvis;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.R.string;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.Paint.Join;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.format.Time;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnInitListener, OnClickListener {

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
	private static final int MY_DATA_CHECK_CODE = 1045;
	private static final int SHAKE_MIN_ACCEL = 7;

	private EditText metTextHint;
	TextView tv;
	private ListView mlvTextMatches;
	private Spinner msTextMatches;
	private Button btClearMemory, txtToSpeach;
	private TextToSpeech mTts;
	private String textToBeSpocken;
	HashMap<String, String> CategoriesKeys;
	Calendar lastTriggered, now;
	// Intent resumeMe;
	boolean textRecognitionOnProgress, textToSpeachStarted,destroyed, mute;// ,paused

	@Override
	public void onCreate(Bundle savedInstanceState) {
		destroyed = false;
		mute = false;
		textRecognitionOnProgress = false;
		super.onCreate(savedInstanceState);
		textToSpeachStarted = false;
		setContentView(R.layout.activity_main);
		metTextHint = (EditText) findViewById(R.id.etTextHint);
		mlvTextMatches = (ListView) findViewById(R.id.lvTextMatches);
		msTextMatches = (Spinner) findViewById(R.id.sNoOfMatches);
		btClearMemory= (Button) findViewById(R.id.btClearMemory);
		txtToSpeach = (Button) findViewById(R.id.button1);
		tv = (TextView) findViewById(R.id.resultTxt);
		textToBeSpocken = "";
		checkVoiceRecognition();

		CategoriesKeys = new HashMap<String, String>();
		CategoriesKeys.put("email", Intent.CATEGORY_APP_EMAIL);
		CategoriesKeys.put("mail", Intent.CATEGORY_APP_EMAIL);
		CategoriesKeys.put("calendar", Intent.CATEGORY_APP_CALENDAR);
		CategoriesKeys.put("calculator", Intent.CATEGORY_APP_CALCULATOR);
		CategoriesKeys.put("maps", Intent.CATEGORY_APP_MAPS);
		CategoriesKeys.put("market", Intent.CATEGORY_APP_MARKET);
		CategoriesKeys.put("gallery", Intent.CATEGORY_APP_GALLERY);
		CategoriesKeys.put("music", Intent.CATEGORY_APP_MUSIC);
		CategoriesKeys.put("browser", Intent.CATEGORY_APP_BROWSER);
		CategoriesKeys.put("chrome", Intent.CATEGORY_APP_BROWSER);

		// Intent checkIntent = new Intent();
		// checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		// startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
		// mTts.setLanguage(Locale.US);

		// shake
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		mAccel = 0.00f;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccelLast = SensorManager.GRAVITY_EARTH;

		lastTriggered = Calendar.getInstance();
		now = Calendar.getInstance();
		lastTriggered.setTime(new Date());
		now.setTime(new Date());

		// resumeMe = getIntent();
		btClearMemory.setOnClickListener(this);
		//open data file
		FileInputStream is;
		try {
			is = openFileInput("savedCommands.txt");
			InputStreamReader r = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(r);
			String line;
			mute = true;
			while((line = br.readLine())!= null)
			{
				thinkAbout(line);
			}
			mute = false;
			br.close();
			r.close();
			is.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public void checkVoiceRecognition() {
		// Check if voice recognition is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			/*mbtSpeak.setEnabled(false);
			mbtSpeak.setText("Voice recognizer not present");*/
			Toast.makeText(this, "Voice recognizer not present",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void textToSpeach() {
		if(mute)
			return;
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
		// mTts.setLanguage(Locale.US);

	}

	Intent voiceRecIntent;

	public void speak() {
		
		voiceRecIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		// Specify the calling package to identify your application
		voiceRecIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
				getClass().getPackage().getName());

		// Display an hint to the user about what he should say.
		voiceRecIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, metTextHint
				.getText().toString());

		// Given an hint to the recognizer about what the user is going to say
		// There are two form of language model available
		// 1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
		// 2.LANGUAGE_MODEL_FREE_FORM : If not sure about the words or phrases
		// and its domain.
		voiceRecIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

		// If number of Matches is not selected then return show toast message

		// Specify how many results you want to receive. The results will be
		// sorted where the first result is the one with higher confidence.
		voiceRecIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
		// Start the Voice recognizer activity for the result.
		if (!textRecognitionOnProgress) {
			textRecognitionOnProgress = true;
			startActivityForResult(voiceRecIntent,
					VOICE_RECOGNITION_REQUEST_CODE);

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == MY_DATA_CHECK_CODE) {
			textToSpeachStarted = true;
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				mTts = new TextToSpeech(this, (OnInitListener) this);
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}

		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)
			textRecognitionOnProgress = false;
		// If Voice recognition is successful then it returns RESULT_OK
		if (resultCode == RESULT_OK) {

			ArrayList<String> textMatchList = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			if (!textMatchList.isEmpty()) {
				// If first Match contains the 'search' word
				// Then start web search.
				thinkAbout(textMatchList.get(0));
				// if (textMatchList.get(0).contains("search")) {
				//
				// String searchQuery = textMatchList.get(0);
				// searchQuery = searchQuery.replace("search", "");
				// Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
				// search.putExtra(SearchManager.QUERY, searchQuery);
				// startActivity(search);
				// } else {
				// // populate the Matches
				// mlvTextMatches
				// .setAdapter(new ArrayAdapter<String>(this,
				// android.R.layout.simple_list_item_1,
				// textMatchList));
				//
				// }

			}
			// Result code for various error.
		} else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR) {
			showToastMessage("Audio Error");
		} else if (resultCode == RecognizerIntent.RESULT_CLIENT_ERROR) {
			showToastMessage("Client Error");
		} else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR) {
			showToastMessage("Network Error");
		} else if (resultCode == RecognizerIntent.RESULT_NO_MATCH) {
			showToastMessage("No Match");
		} else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR) {
			showToastMessage("Server Error");
		}

	}

	void thinkAbout(String key) {
		System.out.println(key);
		String org = key;
		key = Thinker.think(key);
		textToBeSpocken = key;
		String[] keywords = key.trim().split(" ");
//		System.out.println(" ====" + keywords[0] + "=====");
		if (keywords[0].toLowerCase().equals("search")) {

			String searchQuery = strJoin(keywords, " ",
					(keywords[1] != null && keywords[1].equals("for")) ? 2 : 1);
			Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
			search.putExtra(SearchManager.QUERY, searchQuery);
			textToSpeach();
			try {
				Thread.sleep(1200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			startActivity(search);
		} else if (keywords[0].toLowerCase().equals("open")) {
			openCertainApplication(keywords);
		}
		else
		{
			textToSpeach();
			
		}
		if(key.equals(Thinker.SAVED_MEMORY_REPLY) && !mute)
		{
			FileOutputStream os;
			try {
				os = openFileOutput("savedCommands.txt", Context.MODE_PRIVATE);
				OutputStreamWriter w = new OutputStreamWriter(os);
				BufferedWriter bw = new BufferedWriter(w);
				
				bw.append(org);
				
				bw.close();
				w.close();
				os.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			os.write("hello Woerld".getBytes());
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}

	}

	void openCertainApplication(String[] keywords) {
		textToBeSpocken = "openning " + strJoin(keywords, " ", 1)
				+ " for you, sir";
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");

		for (int i = 1; i < keywords.length; i++) {
			if (CategoriesKeys.containsKey(keywords[i].toLowerCase()))
				intent.addCategory(CategoriesKeys.get(keywords[i].toLowerCase()));
		}

		// intent.setComponent(ComponentName.unflattenFromString(""));
		// getpa]
		ResolveInfo ri = getPackageManager().resolveActivity(intent, 0);
		if (ri == null) {
			textToBeSpocken = "No Matched Application to your request, sir";
			textToSpeach();
			return;
		}
		textToSpeach();
		try {
			Thread.sleep(1200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		textToSpeachStarted = false;
		ActivityInfo ai = ri.activityInfo;
		Intent appIntent = new Intent("android.intent.action.MAIN");
		appIntent.addCategory("android.intent.category.LAUNCHER");
		appIntent.setComponent(new ComponentName(ai.packageName, ai.name));
		appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivity(appIntent);
		// finish();

	}

	public static String strJoin(String[] aArr, String sSep, int start) {
		StringBuilder sbStr = new StringBuilder();
		for (int i = start, il = aArr.length; i < il; i++) {
			if (i > start)
				sbStr.append(sSep);
			sbStr.append(aArr[i]);
		}
		return sbStr.toString();
	}

	/**
	 * Helper method to show the toast message
	 **/
	void showToastMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		//

		if (textToBeSpocken != "") {
			tv.setText(textToBeSpocken);
			mTts.speak(textToBeSpocken, TextToSpeech.QUEUE_FLUSH, null);

			// mTts.speak(textToBeSpocken, TextToSpeech.QUEUE_ADD, null);
		}

		while (mTts.isSpeaking())
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		mTts.shutdown();
		// speak();
	}

	// shaking

	private SensorManager mSensorManager;
	private float mAccel; // acceleration apart from gravity
	private float mAccelCurrent; // current acceleration including gravity
	private float mAccelLast; // last acceleration including gravity

	private final SensorEventListener mSensorListener = new SensorEventListener() {

		public void onSensorChanged(SensorEvent se) {
			float x = se.values[0];
			float y = se.values[1];
			float z = se.values[2];
			mAccelLast = mAccelCurrent;
			mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
			float delta = mAccelCurrent - mAccelLast;
			mAccel = mAccel * 0.9f + delta; // perform low-cut filter

			if (mAccel > SHAKE_MIN_ACCEL) {
				
				// lastTriggered.setTime(new Date());
				// if (paused) {
				System.out.println("bateeeeeeeeeee5-------===============");
				//
				// // resumeMe.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				if(!destroyed)
				{
//					startActivity(getIntent().setFlags(
//						getIntent().getFlags()
//								| Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
					speak();
					 
				}
				else
				{
					startActivity(new Intent(getApplicationContext(), MainActivity.class));
//					finish();
				}
				
				// }
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		destroyed = false;
		// paused = false;
		System.out.println("resumed");
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		//
		// if(mTts != null)
		// mTts.stop();
		// paused = true;
		System.out.println("paused");
		super.onPause();
		
	}

	@Override
	public void onStart() {
		System.out.println("started");
		super.onStart();
		destroyed = false;
		// paused = false;
	}

	@Override
	public void onStop() {
		System.out.println("stopped");
		super.onStop();
		
		// paused = true;
	}

	@Override
	public void onDestroy() {

		// mSensorManager.unregisterListener(mSensorListener);
		// mSensorManager.unregisterListener((SensorListener) this);
		destroyed = true;
		System.out.println("distroyed");

		super.onDestroy();
		// paused = true;
	}

	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.btClearMemory)
		{
			FileOutputStream os;
			try {
				os = openFileOutput("savedCommands.txt", Context.MODE_PRIVATE);
				OutputStreamWriter w = new OutputStreamWriter(os);
				BufferedWriter bw = new BufferedWriter(w);
				
				bw.write("");
				
				bw.close();
				w.close();
				os.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			os.write("hello Woerld".getBytes());
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
