package com.cpm.lorealsuprevisor.DailyEntry;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.cpm.lorealsuprevisor.Constant.AlertandMessages;
import com.cpm.lorealsuprevisor.Constant.CommonFunctions;
import com.cpm.lorealsuprevisor.Constant.CommonString;
import com.cpm.lorealsuprevisor.Database.LorealSupervisorDB;
import com.cpm.lorealsuprevisor.R;

import java.io.File;
import java.util.ArrayList;

public class GateMeetingActivity extends AppCompatActivity {

    LorealSupervisorDB db;

    Toolbar toolBar;
    Spinner spnTxt;
    FloatingActionButton saveBtn;
    ImageView img1,img2,img3;
    Context context ;
    String[] spinner_list = {"Select", "YES", "NO"};
    LinearLayout layout;
    boolean checkflag = true;
    String Error_Message= "",visit_date,_pathforcheck1 = "",str, image1 = "",image3="",image2="", visit_date_formatted, _path = "",store_id,_UserId,_pathforcheck2="",_pathforcheck3="";
    private SharedPreferences preferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_meeting_activity);

        db = new LorealSupervisorDB(this);
        declaration();
    }



    private void declaration() {
        context = this;
        toolBar = findViewById(R.id.meeting_toolbar);
        saveBtn = findViewById(R.id.competition_fab);
        img1 = findViewById(R.id.cameraImg1);


        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _pathforcheck1 = store_id + "_" + _UserId.replace(".", "") + "_Gate_Meeting-" + visit_date_formatted + "-" + CommonFunctions.getCurrentTimeHHMMSS() + ".jpg";
                _path = CommonString.FILE_PATH + _pathforcheck1;
                //CommonFunctions.startCameraActivity(activity, _path);
                CommonFunctions.startAnncaCameraActivity(context, _path, null,false);
            }
        });


/*
        SpinnerAdapterView adapter = new SpinnerAdapterView(getApplicationContext(), spinner_list);
        spnTxt.setAdapter(adapter);*/

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        visit_date = preferences.getString(CommonString.KEY_DATE, null);
        _UserId = preferences.getString(CommonString.KEY_USERNAME, "");
        visit_date_formatted = preferences.getString(CommonString.KEY_YYYYMMDD_DATE, "");
        str = CommonString.FILE_PATH;
        setSupportActionBar(toolBar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Gate Meeting - " + visit_date);

      /*  spnTxt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int itemPos, long l) {
                if (itemPos == 0) {
                    layout.setVisibility(View.GONE);
                    competitionData.setPresent("");
                } else if (itemPos == 1) {
                    layout.setVisibility(View.VISIBLE);
                    competitionData.setPresent("1");
                } else {
                    layout.setVisibility(View.GONE);
                    competitionData.setPresent("0");
                    if(!image1.equalsIgnoreCase("")) {
                        File file = new File(CommonString.FILE_PATH + image1);
                        if (file.exists()) {
                            file.delete();
                            img1.setImageResource(R.mipmap.camera_red);
                            image1 = "";
                        }
                    }
                    if(!image2.equalsIgnoreCase("")) {
                        File file = new File(CommonString.FILE_PATH + image2);
                        if (file.exists()) {
                            file.delete();
                            img2.setImageResource(R.mipmap.camera_red);
                            image2 = "";
                        }
                    }
                    if(!image3.equalsIgnoreCase("")) {
                        File file = new File(CommonString.FILE_PATH + image3);
                        if (file.exists()) {
                            file.delete();
                            img3.setImageResource(R.mipmap.camera_red);
                            image3 = "";
                        }
                    }
                }
                // set check flag true after select the value=
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(checkFields(competitionData,image1,image2, image3)){
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                    builder.setCancelable(false);
//                    builder.setMessage("Do you want to save Data?").setCancelable(false)
//                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//
//                                    CommonDataGetterSetter data = new CommonDataGetterSetter();
//
//                                    data.setStore_id(store_id);
//                                    data.setPresent(competitionData.getPresent());
//                                    data.setImg1(image1);
//                                    data.setImg2(image2);
//                                    data.setImg3(image3);
//                                    data.setVisit_date(visit_date);
//                                    saveCompetitionData(data);
//                                    dialog.dismiss();
//                                }
//                            })
//                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                    AlertDialog alert = builder.create();
//                    alert.show();
//                }else{
//                    AlertandMessages.showToastMsg(context, Error_Message);
//                }
            }
        });
    }

}
