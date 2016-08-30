package com.example.dell.the_bot;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class MainActivity extends AppCompatActivity {
    private EditText et_username,et_password,new_username,new_password,new_verify_pass;
    private Button loginas,login,register;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edit;
    private Dialog login_as,logging;
    private ListView login_list;
    private ProgressBar loginas_process;
    private String action,current_user;
    private Socket socket;
    private SocketAddress sock;
    private BufferedReader in;
    private OutputStream out;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("MyPrefs",0);
        et_username = (EditText) findViewById(R.id.name);
        et_password = (EditText) findViewById(R.id.password);
        new_username = (EditText) findViewById(R.id.new_username);
        new_password = (EditText) findViewById(R.id.new_password);
        new_verify_pass = (EditText) findViewById(R.id.new_verify_password);
        loginas = (Button) findViewById(R.id.loginas);
        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
        edit = prefs.edit();
        loginas.setOnClickListener(new View.OnClickListener() {

            private ArrayAdapter<String> adapter;
            private String[] names;
            private int count;
            private TextView tv;

            @Override
            public void onClick(View v) {
                tv = (TextView) v;
                action = tv.getText().toString();
                if(prefs.getInt("count",-1)>0) {
                    count = prefs.getInt("count",-1);
                    names = new String[count];
                    for (int i = 0; i < count;i++){
                        if(!prefs.getString("Username"+i,"empty").matches("empty")) {
                            names[i] = prefs.getString("Username"+i,"empty");
                        }
                    }
                    login_as = new Dialog(v.getContext());
                    login_as.setTitle("Login As");
                    login_as.setContentView(R.layout.login_as_layout);
                    login_list = (ListView) login_as.findViewById(R.id.login_as_list);
                    loginas_process = (ProgressBar) login_as.findViewById(R.id.login_as_progress);
                    adapter = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_list_item_1, names);
                    login_list.setAdapter(adapter);
                    loginas_process.setVisibility(View.GONE);
                    login_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String the = ((TextView) view).getText().toString();
                            Toast.makeText(getApplicationContext(), the, Toast.LENGTH_SHORT).show();
                            loginas_process.setVisibility(View.VISIBLE);
                            NetworkOperation op = new NetworkOperation();
                            op.execute(the);
                        }
                    });
                    login_as.show();
                }else{
                    Toast.makeText(getApplicationContext(),"No users to display",Toast.LENGTH_SHORT).show();
                }
            }
        });
        login.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

            }
        });
        register.setOnClickListener(new View.OnClickListener(){
            private int count = 0;
            String user,pass,verify;
            @Override
            public void onClick(View v) {
                user = new_username.getText().toString();
                pass = new_password.getText().toString();
                verify = new_verify_pass.getText().toString();
                if(!(user.isEmpty() || pass.isEmpty() || verify.isEmpty())) {
                    if(pass.matches(verify)) {
                        count = prefs.getInt("count", -1);
                        if (count < 0) {
                            count = 0;
                        }
                        edit.putString("Username"+count,user).commit();
                        edit.putString("Password"+count,pass).commit();
                        count++;
                        edit.putInt("count",count).commit();

                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Password not matching",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Enter all details",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    class NetworkOperation extends AsyncTask<Object,Object,Object>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loginas_process.setVisibility(View.VISIBLE);

        }

        @Override
        protected Object doInBackground(Object... params) {
            String result="empty";
            socket = new Socket();

            try {
                sock = new InetSocketAddress(InetAddress.getByName("192.168.1.4"), 12001);
                socket.connect(sock,10000);
                if(socket.isConnected()){
                    out = socket.getOutputStream();
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out.write(((String)params[0]).getBytes());
                    result = in.readLine();
                }else{
                    Toast.makeText(getApplicationContext(),"Not Connected",Toast.LENGTH_SHORT).show();
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally {
                current_user = (String)params[0];
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(!((String)o).matches("empty")){
                loginas_process.setVisibility(View.GONE);
                edit.putString("Current_User",current_user).commit();
                login_as.dismiss();
                startActivity(new Intent(MainActivity.this, Current_User.class));
            }else{
                Toast.makeText(getApplicationContext(),"Sory unsuccesful",Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
