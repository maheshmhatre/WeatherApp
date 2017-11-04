package psl.prototypes.apps.weatherapp;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by maheshmhatre on 9/22/17.
 */

public class RequestQueueFactory {

    private static RequestQueueFactory mInstance;
    private RequestQueue requestQueue;
    private static Context mctx;

    private RequestQueueFactory(Context context){
        mctx = context;
        requestQueue = getRequestQueue();
    }




    public RequestQueue getRequestQueue(){

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(mctx.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized RequestQueueFactory getInstance(Context context){

        if(mInstance == null){
            mInstance = new RequestQueueFactory(context);
        }
        return mInstance;
    }

    public<T> void addToRequestQueue(Request<T> request){
        requestQueue.add(request);
    }
}
