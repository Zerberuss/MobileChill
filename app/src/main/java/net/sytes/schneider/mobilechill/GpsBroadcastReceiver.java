package net.sytes.schneider.mobilechill;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Example of Broadcast receiver
 */
public class GpsBroadcastReceiver extends BroadcastReceiver {
    private LocationManager locManager;


    @Override
    public void onReceive(Context context, Intent intent) {


        Log.i("BROADCAST", "got a broadcast "+ intent.getAction());

        // check if GPS is enabled
        locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {


            Toast.makeText(context, "GPS is active ", Toast.LENGTH_LONG).show();

            // intent start main activity if gps is enbled
            Intent i = new Intent(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   /// Flag to allow starting activit from the outside
            context.startActivity(i);

        }

    }
}
