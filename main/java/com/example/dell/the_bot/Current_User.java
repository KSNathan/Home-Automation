package com.example.dell.the_bot;

import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Current_User extends AppCompatActivity {
    private Switch direct_light,direct_fan;
    private CheckBox cb;
    private SeekBar thresh;
    private Socket socket;
    private SocketAddress sock;
    private BufferedReader in;
    private OutputStream out;
    private ProgressBar direct,auto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current__user);
        direct_light = (Switch) findViewById(R.id.light_switch);
        direct_fan = (Switch) findViewById(R.id.fan_switch);
        cb = (CheckBox) findViewById(R.id.enable_checkbox);
        thresh = (SeekBar) findViewById(R.id.light_thresh);
        direct = (ProgressBar) findViewById(R.id.direct_progress);
        auto = (ProgressBar) findViewById(R.id.auto_progress);
        direct.setVisibility(View.VISIBLE);
        auto.setVisibility(View.VISIBLE);
        if(checkForNetwork()) {
            try {
                sock = new InetSocketAddress(InetAddress.getByName("192.168.1.4"), 20001);
                socket = new Socket();
                socket.connect(sock,20000);
                if (socket.isConnected()) {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = socket.getOutputStream();
                    Toast.makeText(getApplicationContext(), "Successfully Connected", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                direct.setVisibility(View.GONE);
                auto.setVisibility(View.GONE);
                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            buttonView.setText("In Automation Mode");
                            direct_fan.setClickable(false);
                            direct_light.setClickable(false);
                            thresh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                int the_progress;
                                String action = "seekbar";

                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    the_progress = progress;

                                }
                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {

                                }
                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                    NetworkJob job = new NetworkJob();
                                    JSONObject ob = new JSONObject();
                                    try {
                                        ob.put("action", action);
                                        ob.put("threshold", the_progress);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        job.execute(ob);
                                    }
                                }
                            });
                        } else {
                            buttonView.setText("In direct mode");
                            direct_light.setClickable(true);
                            direct_fan.setClickable(true);
                            thresh.setClickable(false);
                            direct_light.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                String action = "light";
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    JSONObject newob = new JSONObject();
                                    if (isChecked) {
                                        buttonView.setText("ON");

                                    } else {
                                        buttonView.setText("OFF");
                                    }
                                    try{

                                        newob.put("action",action);
                                        newob.put("status",isChecked);
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }finally {
                                        TheNetworkJob job = new TheNetworkJob();
                                        job.execute(newob);
                                    }
                                }
                            });
                            direct_fan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                String action = "fan";
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    JSONObject newob = new JSONObject();
                                    if(isChecked){
                                        buttonView.setText("ON");
                                    }else{
                                        buttonView.setText("OFF");
                                    }
                                    try{

                                        newob.put("action",action);
                                        newob.put("status",isChecked);
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }finally {
                                        TheNetworkJob job = new TheNetworkJob();
                                        job.execute(newob);
                                    }
                                }
                            });
                        }
                    }

                });
            }
        }
    }
    class TheNetworkJob extends AsyncTask<Object,Object,Object>{
        private String status;
        private JSONObject object;
        private String result;
        private Socket socket;
        private SocketAddress sock;
        private BufferedReader in;
        private OutputStream out;
        @Override
        protected Object doInBackground(Object... params) {
           object = (JSONObject)params[0];

            try {
                socket = new Socket();
                sock = new InetSocketAddress(InetAddress.getByName("192.168.1.4"),20001);
                socket.connect(sock,20000);

                if(socket.isConnected()) {
                    out = socket.getOutputStream();
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out.write(object.toString().getBytes());
                    result = in.readLine();
                }else{
                    Toast.makeText(getApplicationContext(),"Unable to perform thenetworkjob",Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

    }
    class NetworkJob extends AsyncTask<Object,Object,Object>{
        String result;private Socket socket;
        private SocketAddress sock;
        private BufferedReader in;
        private OutputStream out;

        @Override
        protected Object doInBackground(Object... params) {
            JSONObject ob = new JSONObject();
            ob = (JSONObject)params[0];
            socket = new Socket();
            try{
            sock = new InetSocketAddress(InetAddress.getByName("192.168.1.4"),20001);
                socket.connect(sock,10000);
                if(socket.isConnected()){
                    out = socket.getOutputStream();
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out.write(ob.toString().getBytes());
                    result = in.readLine();

                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return result;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_current__user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void onBackPressed(){
        finish();
    }
    private boolean checkForNetwork() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec =(ConnectivityManager)getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||

                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            Toast.makeText(this, "Yes Network Available", Toast.LENGTH_LONG).show();
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED  ) {
            Toast.makeText(this, "Either Wifi or Mobile data have to be enabled", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }
}
