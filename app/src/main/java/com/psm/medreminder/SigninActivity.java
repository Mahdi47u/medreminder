package com.psm.medreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.psm.medreminder.activity.IconTabsActivity;
import com.psm.medreminder.alarm.AlarmReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class SigninActivity extends AppCompatActivity {

    EditText email, password;
    TextView signup,login;
    String pass,emails;
    String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);
        signup = (TextView) findViewById(R.id.signup);
        email = (EditText) findViewById(R.id.emel);
        password = (EditText) findViewById(R.id.pswd);
        login = (TextView) findViewById(R.id.login);



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                emails = email.getText().toString();
                pass = password.getText().toString();

             /*   if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(SigninActivity.this, "fill details", Toast.LENGTH_SHORT).show();
                } else {
                    login(email, pass);
                } */

                checkData();

            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
                startActivity(intent);
//                notification();

            }
        });
    }

    public void notification()
    {
        Calendar calendar = Calendar.getInstance();
        if(Build.VERSION.SDK_INT >=23)
        {
            calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH),2,32,0);
        }
        else
        {
            calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH),2,32,0);
        }
        setAlarm(calendar.getTimeInMillis());
    }

    private void setAlarm(long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,timeInMillis,30000,pendingIntent);
        Toast.makeText(this,"Alarm is set",Toast.LENGTH_SHORT).show();
    }

    boolean isEmail(EditText text) {
        CharSequence email = text.getText().toString();
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }
    boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }
    void checkData() {
        if (isEmpty(email)) {
            email.setError("Please enter an email");
            return;
        }
        if (isEmail(email) == false) {
            email.setError("Invalid email");
            return;
        }
        if (isEmpty(password)) {
            password.setError("Please enter a password");
            return;
        }
        if (password.length() < 6) {
            password.setError("Invalid password");
            return;
        }
        login(emails, pass);
    }


    public void login(final String emel, final String pswd){

        if (LocalStore.login(SigninActivity.this, emel, pswd)) {
            Intent intent = new Intent(SigninActivity.this, IconTabsActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(SigninActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }

    }
}
