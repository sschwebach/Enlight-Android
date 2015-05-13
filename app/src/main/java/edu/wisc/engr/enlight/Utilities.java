package edu.wisc.engr.enlight;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class Utilities {
    //URL is only used for the unused class ContactFountainTask
    //static final public String baseURL = "http://enlight.club:8082/api";
    //static final public String URL = "http://enlight.club:8082/api";
    static final public String URL = "http://enlight.engr.wisc.edu/api";
    static final public String baseURL = "http://enlight.engr.wisc.edu/api";
	//static final public String URL = "http://enlight.engr.wisc.edu/enlight-backend/php/act.php";
	//static final public String baseURL = "http://enlight.engr.wisc.edu/enlight-backend/api";
    //static final public String baseURL = "http://alexkersten.com/enlight/api";
	static final public String queryURL = baseURL + "/control/query";
	static final public String requestControlURL = baseURL + "/control/request";
	static final public String releaseControlURL = baseURL + "/control/release";
	static final public String valvesURL = baseURL + "/valves";
	static final public String allPatternsURL = baseURL + "/patterns";
	static final public String singlePatternsURL = baseURL + "/pattern";
    static final public int[] buttonToValveMap = {12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24}; //A mapping of the valve button to its actual valve
    static final public int[] valveToButtonMap = {}; //A mapping of the valves values back to the buttons

    //Various values for states
    public static final int NO_STATE = -1;
    public static final int NO_REQUESTS = 0;
    public static final int IN_QUEUE = 1;
    public static final int HAS_CONTROL = 2;

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    /**
     * An enum for listing the errors that could happen
     */
    public enum ERROR_STATE{
        INTERNET, DEFAULT;
    }
}
