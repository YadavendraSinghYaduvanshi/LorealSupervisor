

package com.cpm.lorealsuprevisor;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cpm.lorealsuprevisor.Constant.AlertandMessages;
import com.cpm.lorealsuprevisor.Constant.CommonString;
import com.cpm.lorealsuprevisor.Get_IMEI_number.ImeiNumberClass;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.fabric.sdk.android.Fabric;


public class LoginActivity extends AppCompatActivity {

    private TextView tv_version;
    private String app_ver;
    private static int counter = 1;
    private SharedPreferences preferences = null;
    private SharedPreferences.Editor editor = null;
    private final String lat = "0.0";
    private final String lon = "0.0";
    // UI references.
    private AutoCompleteTextView museridView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Context context;
    private String userid;
    private String password;
    private int versionCode;
    private String[] imeiNumbers;
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 999;
    private Button museridSignInButton;
    private ImeiNumberClass imei;
    private String manufacturer;
    private String model;
    private String os_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Fabric.isInitialized()) {
            Fabric.with(this, new Crashlytics());
        }

        setContentView(R.layout.activity_login);
        declaration();
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            imeiNumbers = imei.getDeviceImei();
        }

        getDeviceName();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_PHONE_STATE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            imeiNumbers = imei.getDeviceImei();
        }

    }

    private void attemptLogin() {
        // Reset errors.
        museridView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        userid = museridView.getText().toString().trim();
        password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid userid address.
        if (TextUtils.isEmpty(userid)) {
            museridView.setError(getString(R.string.error_field_required));
            focusView = museridView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();

        } else if (!isuseridValid(userid)) {
            Snackbar.make(museridView, getString(R.string.error_incorrect_username), Snackbar.LENGTH_SHORT).show();
        } else if (!isPasswordValid(password)) {
            Snackbar.make(museridView, getString(R.string.error_incorrect_password), Snackbar.LENGTH_SHORT).show();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            new AuthenticateTask().execute();
        }
    }

    private boolean isuseridValid(String userid) {
        //TODO: Replace this with your own logic
        boolean flag = true;
        String u_id = preferences.getString(CommonString.KEY_USERNAME, "");
        if (!u_id.equals("") && !userid.equalsIgnoreCase(u_id)) {
            flag = false;
        }
        return flag;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        boolean flag = true;
        String pw = preferences.getString(CommonString.KEY_PASSWORD, "");
        if (!pw.equals("") && !password.equals(pw)) {
            flag = false;
        }
        return flag;
    }


    private class AuthenticateTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog dialog = null;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setTitle("Login");
            dialog.setMessage("Authenticating....");
            dialog.setCancelable(false);
            if (!dialog.isShowing())
                dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {

                versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Userid", userid);
                jsonObject.put("Password", password);
                jsonObject.put("Intime", getCurrentTime());
                jsonObject.put("Latitude", lat);
                jsonObject.put("Longitude", lon);
                jsonObject.put("Appversion", app_ver);
                jsonObject.put("Attmode", "0");
                jsonObject.put("Networkstatus", "0");
                jsonObject.put("Manufacturer", manufacturer);
                jsonObject.put("ModelNumber", model);
                jsonObject.put("OSVersion", os_version);

                if (imeiNumbers.length > 0) {
                    jsonObject.put("IMEINumber1", imeiNumbers[0]);
                    if (imeiNumbers.length > 1) {
                        jsonObject.put("IMEINumber2", imeiNumbers[1]);
                    } else {
                        jsonObject.put("IMEINumber2", "0");
                    }
                } else {
                    jsonObject.put("IMEINumber1", "0");
                    jsonObject.put("IMEINumber2", "0");
                }


                String jsonString2 = jsonObject.toString();

               /* DownloadDataWithRetrofit upload = new DownloadDataWithRetrofit(context);
                String result_str = upload.downloadDataUniversal(jsonString2, CommonString.LOGIN_SERVICE);*/


                String result_str = "";
                if (result_str.equalsIgnoreCase(CommonString.MESSAGE_SOCKETEXCEPTION)) {

                    throw new IOException();

                } else if (result_str.equalsIgnoreCase(CommonString.KEY_FAILURE)) {

                    throw new Exception();

                } else {

                    Gson gson = new Gson();
                   // GsonGetterSetter userObject = gson.fromJson(result_str, GsonGetterSetter.class);
                    // PUT IN PREFERENCES
                  /*  editor.putString(CommonString.KEY_USERNAME, userid);
                    editor.putString(CommonString.KEY_PASSWORD, password);
                     editor.putString(CommonString.KEY_VERSION, String.valueOf(userObject.getResult().get(0).getAppVersion()));
                    editor.putString(CommonString.KEY_PATH, userObject.getResult().get(0).getAppPath());
                    //userObject.getResult().get(0).setCurrentdate("01/16/2018");
                    editor.putString(CommonString.KEY_DATE, userObject.getResult().get(0).getCurrentdate());
                    editor.putString(CommonString.KEY_NOTICE_BOARD_LINK, userObject.getResult().get(0).getNotice());
                    Date initDate = new SimpleDateFormat("MM/dd/yyyy").parse(userObject.getResult().get(0).getCurrentdate());

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");*/
                   // String parsedDate = formatter.format(initDate);
                   // editor.putString(CommonString.KEY_YYYYMMDD_DATE, parsedDate);
                    //date is changed for previous day data
                    //editor.putString(CommonString.KEY_DATE, "11/22/2017");
                    editor.commit();
                    Crashlytics.setUserIdentifier(userid);

                    return CommonString.KEY_SUCCESS;

                }


            } catch (MalformedURLException e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        AlertandMessages.showAlert((Activity) context, CommonString.MESSAGE_EXCEPTION, false);

                    }
                });

            } catch (IOException e) {
                Crashlytics.logException(e);
                counter++;
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        AlertandMessages.showAlert((Activity) context, CommonString.MESSAGE_SOCKETEXCEPTION, false);
                    }
                });
            } catch (Exception e) {
                Crashlytics.logException(e);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        AlertandMessages.showAlert((Activity) context, CommonString.MESSAGE_CHANGED, false);
                    }
                });
            }
            return "";

        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            dialog.dismiss();

if (result.equals(CommonString.KEY_SUCCESS)) {
                if (preferences.getString(CommonString.KEY_VERSION, "").equals(String.valueOf(versionCode))) {
                   /* Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(intent);
                    finish();*/
                } else {
                    // if app version code does not match with live apk version code then update will be called.
                  /*  AutoUpdateActivity autoUpdateActivity = new AutoUpdateActivity(context, preferences.getString(CommonString.KEY_PATH, ""));
                    autoUpdateActivity.startAutoUpdateActivity();
                    finish();*/
                }
            }


        }

    }

    public void getDeviceName() {
        manufacturer = Build.MANUFACTURER;
        model = Build.MODEL;
        os_version = Build.VERSION.RELEASE;
    }


    private String getCurrentTime() {
        Calendar m_cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(m_cal.getTime());
    }

    private void declaration() {
        context = this;
        tv_version = findViewById(R.id.tv_version_code);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        museridView =  findViewById(R.id.userid);
        mPasswordView = findViewById(R.id.password);
         museridView.setText("arunkumar");
         mPasswordView.setText("Cpm@123%");


   museridView.setText("rajashekar.a");
        mPasswordView.setText("Cpm@123%");


        museridView.setText("Pradeep.sing");
        mPasswordView.setText("Cpm@123%");



        museridSignInButton =  findViewById(R.id.user_login_button);
        museridSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkNetIsAvailable()) {
                    attemptLogin();
                } else {
                    AlertandMessages.showAlert((Activity) context, CommonString.MESSAGE_INTERNET_NOT_AVALABLE, false);
                }
            }
        });
        try {
            app_ver = String.valueOf(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);

        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        tv_version.setText("Version - T2 " + app_ver);
        imei = new ImeiNumberClass(context);
    }

    private boolean checkNetIsAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}

