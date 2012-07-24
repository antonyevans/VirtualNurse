package com.senstore.alice.utils;

/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class Constants {

	public static String TAG = "Alice";
	public static String APP_NAME = "Birth Control";
	
	public static String LOG_SERVICE_NAME = "AliceBackgroundLogService";
	public static String LOG_SERVICE_IN_MSG = "log_type";
	public static String LOG_SERVICE_OUT_MSG = "log_result";
	
	public static final String ACTION_RESP =
		      "com.senstore.alice.utils.intent.action.MESSAGE_PROCESSED";
	
	public static String REGISTRY_CONTEXT="context";
	public static String REGISTRY_LOCATION="deviceLocation";
	public static String REGISTRY_CALL="canCallDoctor";

	// Do not forget to add the trailing '/'
	public static String SERVER_URL = "http://www.senstore.com/";

	public static String SECURITY_HASH = "466c608460c08a33601114d141611fea";

	public static String TYPE_JSON = "json";
	public static String TYPE_XML = "xml";

	public static int ACTION_DIAGNOSIS = 1;
	public static int ACTION_LOGGING = 2;

	// Logging Activities, log types definitions - log_type=x
	public static int LOG_REGISTER = 1;
	public static int LOG_LOCATION = 2;
	public static int LOG_CALL_DOCTOR = 3;
	public static String LOG_CALL_DOCTOR_ACCEPT = "4a";
	public static String LOG_CALL_DOCTOR_REJECT = "4b";

	// Selection of guide.
	// and
	// Response of question
	// Diagnosis Activities, select type - select_type=voice, select_type=touch
	public static String DIAGNOSIS_VOICE = "voice";
	public static String DIAGNOSIS_DEFAULT_LAST_QUERY = "start";
	public static String DIAGNOSIS_TOUCH = "touch";
	public static int DIAGNOSIS_GUIDE_DEFAULT = 0;
	public static int CHAT_LENGTH_DEFAULT = 0;

	// Server response types
	public static int RESPONSE_CONFIRM = 1;// confirm dialog(YES/NO)
	public static int RESPONSE_OPTIONS = 2;// Options dialog(Options array)
	public static int RESPONSE_EMERGENCY = 3;// Emergency(text+map)
	public static int RESPONSE_CALLDOCTOR = 4;// Call doctor(text+button)
	public static int RESPONSE_INFORMATION = 5;// Information(text)
	
	
	public static  String INFLATOR_SERVICE = "inflator_service";
	
	
	
}
