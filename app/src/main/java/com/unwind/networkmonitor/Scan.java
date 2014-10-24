package com.unwind.networkmonitor;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.gms.ads.*;
import com.unwind.netTools.Pinger;
import com.unwind.netTools.model.Device;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class Scan extends ActionBarActivity {

    private AdView adView;
    private  NetDeviceAdapter adapter = new NetDeviceAdapter(new ArrayList<Device>(15), R.layout.device_fragment, this);;

    private static final String AD_UNIT_ID = "ca-app-pub-5497930890633928/3668252094";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(AD_UNIT_ID);

        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainView);
        mainLayout.addView(adView, 0);

        AdRequest request = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();


        adView.loadAd(request);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.list);

        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapter);


        //rescan();
    }
    private void rescan(){

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected() && mWifi.isAvailable()){
            AsyncScan scan = new AsyncScan();
            scan.execute(adapter);
        }else {
            Toast.makeText(this, "Not connected to wifi, no scanning permitted", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_refresh:
                rescan();
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }
    @Override
    public void onDestroy() {
        // Destroy the AdView.
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }


    private static class AsyncScan extends AsyncTask<NetDeviceAdapter, Void, List<Device>> {

        private NetDeviceAdapter adapter;
        @Override
        protected List<Device> doInBackground(NetDeviceAdapter... voids) {



            List<Device> addresses = Pinger.getDevicesOnNetwork("192.168.5");
            adapter = voids[0];
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Device> inetAddresses) {
            super.onPostExecute(inetAddresses);
            adapter.setAddresses(inetAddresses);
            adapter.notifyDataSetChanged();

        }

    }
}
