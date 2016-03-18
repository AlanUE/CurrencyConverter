package sg.com.kaplan.pdma.currencyconverter;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class currency_converter_service extends Service {
	// This variable is used for debug log (LogCat)
	private static final String TAG = "CC:Service";

	// Intent string for broadcasting
	public static final String ACTIVITY_TO_SERVICE_BROADCAST = "CC_A_TO_S_BROADCAST";
	public static final String SERVICE_TO_ACTIVITY_BROADCAST = "CC_S_TO_A_BROADCAST";

	// Intent key for broadcasting
	private static final String BROADCAST_KEY_ROAMING_OPT = "roaming";
	private static final String BROADCAST_KEY_LASTUPDATETIME = "lastupdatetime";

	// EU Bank Currency Rate data source URL
	private static final String EU_BANK_XML_URL = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

	// broadcast receiver
	private Broadcast_Receiver my_intent_receiver;

	// task delay time (in ms)
	private long task_delay = 60 * 1000;

	private long ref_time = 0;
	private boolean ref_roaming = false;

	private internet_connection				cc_connection;
	private currency_rate_parser_ecb		cc_parser_ECB;

	private Thread parser_thread;
	private boolean parser_thread_alive = true;

	@Override
	public IBinder onBind(Intent i) {
		Log.d(TAG, "onBind >>>>>");

    	Log.d(TAG, "onBind <<<<<");
		return null;
	}

	@Override
	public boolean onUnbind(Intent i) {
		Log.d(TAG, "onUnbind >>>>>");

    	Log.d(TAG, "onUnbind <<<<<");
		return false;
	}

	@Override
	public void onRebind(Intent i) {
		Log.d(TAG, "onRebind >>>>>");

    	Log.d(TAG, "onRebind <<<<<");
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate >>>>>");
		super.onCreate();

		cc_connection = new internet_connection(this);
		cc_parser_ECB = new currency_rate_parser_ecb();

		// register broadcast receiver
		IntentFilter filter = new IntentFilter(ACTIVITY_TO_SERVICE_BROADCAST);
		my_intent_receiver = new Broadcast_Receiver();
		registerReceiver(my_intent_receiver, filter);

    	Log.d(TAG, "onCreate <<<<<");
	}

	@Override
	public void onStart(Intent i, int startId) {
		Log.d(TAG, "onStart >>>>>");
		super.onStart(i, startId);

		// default values
		ref_time = 0;
		ref_roaming = false;

		try {
			ref_time = i.getExtras().getLong(BROADCAST_KEY_LASTUPDATETIME);
			ref_roaming = i.getExtras().getBoolean(BROADCAST_KEY_ROAMING_OPT, false);
		} catch (Exception e) {
			Log.e(TAG, "onStart: " + e.toString());
		}

		// start a new thread to handle database update
		parser_thread_alive = true;
		parser_thread = new Thread(mTask);
		parser_thread.start();

    	Log.d(TAG, "onStart <<<<<");
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy >>>>>");
		super.onDestroy();

		parser_thread_alive = false;
		parser_thread.interrupt();

		// remove broadcast receiver
		unregisterReceiver(my_intent_receiver);

    	Log.d(TAG, "onDestroy <<<<<");
	}

	// background thread to get data from internet
	private Runnable mTask = new Runnable() {
		long	timediff;

		private void delay() {
			try {
				Thread.sleep(task_delay);
			} catch (InterruptedException e) {
				Log.d(TAG, "Parser thread receive interrupt");
			}
		}

		public void run() {
			do {
				timediff = getDiffTime(ref_time);

				// 60000 = 60sec * 1000ms
				if(timediff >= 60000) {
					// update
					try {
						boolean result = cc_connection.TestConnection(EU_BANK_XML_URL);

						if(result) {
							if(cc_parser_ECB.StartParser(EU_BANK_XML_URL)) {
								// update last update time
								ref_time = System.currentTimeMillis();

								// send data to activity to update database
								sendSettingToActivity();
							}
						}
					} catch (Exception e) {
						Log.e(TAG, "mTask: " + e.toString());
					}
				} else {
					task_delay = 60000 - timediff;
					Log.d(TAG, "Task: Increase delay time: " + Double.toString((double) task_delay / 1000) + " sec(s)");
				}

				// call this task again
				delay();

			} while(parser_thread_alive);
		}
	};

	// receive data from other activities
	public class Broadcast_Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// receive intent from activity
			Log.d(TAG, "receive data from activity >>>>>");

			try {
				ref_time = intent.getExtras().getLong(BROADCAST_KEY_LASTUPDATETIME);
				ref_roaming = intent.getExtras().getBoolean(BROADCAST_KEY_ROAMING_OPT, false);

				cc_connection.EnableNetworkRoaming(ref_roaming);
			} catch (Exception e) {
				Log.e(TAG, "Broadcast_Receiver:" + e.toString());
			}
		}
	}

	// send data to activity
	void sendSettingToActivity() {
		Intent intent = new Intent(SERVICE_TO_ACTIVITY_BROADCAST);

		intent.putExtra(BROADCAST_KEY_LASTUPDATETIME, ref_time);

		List<currency_rate_parser_ecb.currency_rate> onlinedata = cc_parser_ECB.getRates();

		for(int i=0; i<onlinedata.size(); i++) {
			try {
				intent.putExtra(onlinedata.get(i).m_name, onlinedata.get(i).m_rate);
			} catch (Exception e) {
				Log.e(TAG, "sendSettingToActivity:" + e.toString());
			}
		}

		Log.d(TAG, "send data to activity >>>>>");
		sendBroadcast(intent);

		// sycn EU every minutes
		task_delay = 60000;
	}

	// return difference in hour
	private long getDiffTime(long reftime) {
		long currenttime = System.currentTimeMillis();

		return (currenttime - reftime);
	}
}
