package io.archylex.ovpnd;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ScrollingActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private TextView textView;
    private List<String> countries;
    private List<String> configs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DownloaderAsyncTask().execute();
                Snackbar.make(view, "Get ready to download", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        textView = (TextView) findViewById(R.id.textview);
        textView.setText("\n\nPress the floating button to download OpenVPN configuration files.");

        configs = new ArrayList<String>();
        countries = new ArrayList<String>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading...");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DownloaderAsyncTask extends AsyncTask<String, Integer, Void> {
        private static final String TAG = "ScrollingActivity";

        @Override
        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL("http://www.vpngate.net/api/iphone/");
                InputStreamReader stream = new InputStreamReader(url.openStream());
                BufferedReader input = new BufferedReader(stream);
                String line;
                int count = 0;

                DeleteRecursive(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/vpn-configs/"));

                while ((line = input.readLine()) != null) {
                    count++;
                    String arr[];
                    if ((arr = line.split(",")).length > 14) {
                        countries.add(arr[5]);
                        byte[] decoded = Base64.decode(arr[14], Base64.DEFAULT);
                        configs.add(new String(decoded));
                        writeToFile(new String(decoded), arr[5], arr[0]);
                        publishProgress(count);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            textView.setText("\n\nAll configuration files was download. Please go to OpenVPN app and choice a config file from \"sdcard/openvpn-configs/\".");
        }
    }

    private void writeToFile(String data, String country, String fname) {
        try {
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + java.io.File.separator + "OVPND");

            if (!directory.exists())
                directory.mkdirs();

            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + java.io.File.separator + "OVPND" + java.io.File.separator + country);
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, fname + ".ovpn");
            FileOutputStream f = new FileOutputStream(file);
            InputStream in = new ByteArrayInputStream(data.getBytes("UTF-8"));
            byte[] buffer = new byte[1024];
            int len1 = 0;

            while ((len1 = in.read(buffer)) > 0)
                f.write(buffer, 0, len1);

            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void DeleteRecursive(File fd) {
        if (fd.isDirectory())
            for (File child : fd.listFiles())
                DeleteRecursive(child);

        fd.delete();
    }
}
