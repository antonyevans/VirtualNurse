package com.senstore.alice.utils;


/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public class Constants {

	public static String FLURRY_API = "CCF4HBFDPHMQDTSNY4T5";
	
	public static String TAG = "Alice";
	public static String APP_NAME = "Birth Control";
	public static String PURCHASE_TYPE = "WomensSexualHealth";
	
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
	
	public static String NOT_PURCHASED_API_RESPONSE = "422 - {\"Status\":\"Please purchase the guide\"}";
	
	public static  String INFLATOR_SERVICE = "inflator_service";
	
	//the following are constants related to the billing service
	
	// The response codes for a request, defined by Android Market.
    public enum ResponseCode {
        RESULT_OK,
        RESULT_USER_CANCELED,
        RESULT_SERVICE_UNAVAILABLE,
        RESULT_BILLING_UNAVAILABLE,
        RESULT_ITEM_UNAVAILABLE,
        RESULT_DEVELOPER_ERROR,
        RESULT_ERROR;

        // Converts from an ordinal value to the ResponseCode
        public static ResponseCode valueOf(int index) {
            ResponseCode[] values = ResponseCode.values();
            if (index < 0 || index >= values.length) {
                return RESULT_ERROR;
            }
            return values[index];
        }
    }

    // The possible states of an in-app purchase, as defined by Android Market.
    public enum PurchaseState {
        // Responses to requestPurchase or restoreTransactions.
        PURCHASED,   // User was charged for the order.
        CANCELED,    // The charge failed on the server.
        REFUNDED;    // User received a refund for the order.

        // Converts from an ordinal value to the PurchaseState
        public static PurchaseState valueOf(int index) {
            PurchaseState[] values = PurchaseState.values();
            if (index < 0 || index >= values.length) {
                return CANCELED;
            }
            return values[index];
        }
    }

    /** This is the action we use to bind to the MarketBillingService. */
    public static final String MARKET_BILLING_SERVICE_ACTION =
        "com.android.vending.billing.MarketBillingService.BIND";

    // Intent actions that we send from the BillingReceiver to the
    // BillingService.  Defined by this application.
    public static final String ACTION_CONFIRM_NOTIFICATION =
            "com.example.subscriptions.CONFIRM_NOTIFICATION";
    public static final String ACTION_GET_PURCHASE_INFORMATION =
            "com.example.subscriptions.GET_PURCHASE_INFORMATION";
    public static final String ACTION_RESTORE_TRANSACTIONS =
            "com.example.subscriptions.RESTORE_TRANSACTIONS";

    // Intent actions that we receive in the BillingReceiver from Market.
    // These are defined by Market and cannot be changed.
    public static final String ACTION_NOTIFY = "com.android.vending.billing.IN_APP_NOTIFY";
    public static final String ACTION_RESPONSE_CODE =
        "com.android.vending.billing.RESPONSE_CODE";
    public static final String ACTION_PURCHASE_STATE_CHANGED =
        "com.android.vending.billing.PURCHASE_STATE_CHANGED";

    // These are the names of the extras that are passed in an intent from
    // Market to this application and cannot be changed.
    public static final String NOTIFICATION_ID = "notification_id";
    public static final String INAPP_SIGNED_DATA = "inapp_signed_data";
    public static final String INAPP_SIGNATURE = "inapp_signature";
    public static final String INAPP_REQUEST_ID = "request_id";
    public static final String INAPP_RESPONSE_CODE = "response_code";

    // These are the names of the fields in the request bundle.
    public static final String BILLING_REQUEST_METHOD = "BILLING_REQUEST";
    public static final String BILLING_REQUEST_API_VERSION = "API_VERSION";
    public static final String BILLING_REQUEST_PACKAGE_NAME = "PACKAGE_NAME";
    public static final String BILLING_REQUEST_ITEM_ID = "ITEM_ID";
    public static final String BILLING_REQUEST_ITEM_TYPE = "ITEM_TYPE";
    public static final String BILLING_REQUEST_DEVELOPER_PAYLOAD = "DEVELOPER_PAYLOAD";
    public static final String BILLING_REQUEST_NOTIFY_IDS = "NOTIFY_IDS";
    public static final String BILLING_REQUEST_NONCE = "NONCE";

    public static final String BILLING_RESPONSE_RESPONSE_CODE = "RESPONSE_CODE";
    public static final String BILLING_RESPONSE_PURCHASE_INTENT = "PURCHASE_INTENT";
    public static final String BILLING_RESPONSE_REQUEST_ID = "REQUEST_ID";
    public static long BILLING_RESPONSE_INVALID_REQUEST_ID = -1;

    // These are the types supported in the IAB v2
    public static final String ITEM_TYPE_INAPP = "inapp";
    public static final String ITEM_TYPE_SUBSCRIPTION = "subs";
    
    public static final boolean DEBUG = true;
	
}
