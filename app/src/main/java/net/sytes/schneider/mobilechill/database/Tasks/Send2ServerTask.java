package net.sytes.schneider.mobilechill.database.Tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Timo Hasenbichler on 08.01.2018.
 */

public class Send2ServerTask extends AsyncTask<String,Void,Void> {

    @Override
    protected Void doInBackground(String... strings) {
        String data = strings[0];
        String url = " https://httpstat.us/200";
        HttpURLConnection httpURLConnection = null;
        try {

            httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setRequestMethod("POST");

            httpURLConnection.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
            wr.writeBytes("PostData=" + data);
            wr.flush();
            wr.close();

            InputStream in = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(in);

            int inputStreamData = inputStreamReader.read();
            while (inputStreamData != -1) {
                char current = (char) inputStreamData;
                inputStreamData = inputStreamReader.read();
                data += current;
            }
            Log.i("SUCCESS","DID SHADY STUFF");
        } catch (Exception e) {
            Log.i("OOPS","FAILED TO DO SHADY STUFF");
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }




        return null;
    }
}
