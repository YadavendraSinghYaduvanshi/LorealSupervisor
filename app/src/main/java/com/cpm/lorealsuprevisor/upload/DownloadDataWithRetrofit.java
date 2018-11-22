package com.cpm.lorealsuprevisor.upload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.cpm.lorealsuprevisor.Constant.AlertandMessages;
import com.cpm.lorealsuprevisor.Constant.CommonString;
import com.cpm.lorealsuprevisor.Database.LorealSupervisorDB;
import com.cpm.lorealsuprevisor.GetterSetter.ReferenceVariablesForDownloadActivity;
import com.cpm.lorealsuprevisor.GetterSetter.TableQuery;
import com.cpm.lorealsuprevisor.GetterSetter.TableStructureGetterSetter;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by upendra on 5/22/2018.
 */

public class DownloadDataWithRetrofit extends ReferenceVariablesForDownloadActivity {

    boolean isvalid;
    private Retrofit adapter;
    Context context;
    public int listSize = 0;
    int status = 0;
    LorealSupervisorDB db;
    ProgressDialog pd;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String _UserId, date, app_ver;

/*    private NonWorkingReasonGetterSetter nonWorkingReasonObj;
    private SkuMasterGetterSetter skuMasterObj;
    private PosmMasterGetterSetter posmMasterObj;
    private NonPosmReasonGetterSetter nonPosmReasonObj;
    private MappingStockGetterSetter mappingStockObj;
    private BrandMasterGetterSetter brandMasterObj;
    private MappingPOSMGetterSetter mappingPOSMObj;
    private JourneyPlanGetterSetter journeyPlanObj;*/

    int from;
    public DownloadDataWithRetrofit(Context context) {
        this.context = context;
    }

    public DownloadDataWithRetrofit(Context context, LorealSupervisorDB db, ProgressDialog pd, int from) {
        this.context = context;
        this.db = db;
        this.pd = pd;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
        this.from = from;
        _UserId = preferences.getString(CommonString.KEY_USERNAME, "");
        date = preferences.getString(CommonString.KEY_DATE, null);
        try {
            app_ver = String.valueOf(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        db.open();
    }

    public String downloadDataUniversal(final String jsonString, int type) {
        try {
            status = 0;
            isvalid = false;
            final String[] data_global = {""};
            RequestBody jsonData = RequestBody.create(MediaType.parse("application/json"), jsonString);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .build();

            adapter = new Retrofit.Builder()
                    .baseUrl(CommonString.URL2)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
            PostApi api = adapter.create(PostApi.class);
            Call<ResponseBody> call = null;
            if (type == CommonString.LOGIN_SERVICE) {
                call = api.getLogindetail(jsonData);
            } else if (type == CommonString.DOWNLOAD_ALL_SERVICE) {
                call = api.getDownloadAll(jsonData);
            } else if (type == CommonString.COVERAGE_DETAIL) {
                call = api.getCoverageDetail(jsonData);
            } else if (type == CommonString.UPLOADJCPDetail) {
                call = api.getUploadJCPDetail(jsonData);
            } else if (type == CommonString.UPLOADJsonDetail) {
                call = api.getUploadJsonDetail(jsonData);
            } else if (type == CommonString.COVERAGEStatusDetail) {
                call = api.getCoverageStatusDetail(jsonData);
            } else if (type == CommonString.CHECKOUTDetail) {
                call = api.getCheckout(jsonData);
            } else if (type == CommonString.DELETE_COVERAGE) {
                call = api.deleteCoverageData(jsonData);
            } else if (type == CommonString.SUP_ATTENDANCE) {
                call = api.setSupAttendanceData(jsonData);
            }else if (type == CommonString.DEVIATION_DETAILS) {
                call = api.setSupDeviationData(jsonData);
            }

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    ResponseBody responseBody = response.body();
                    String data = null;
                    if (responseBody != null && response.isSuccessful()) {
                        try {
                            data = response.body().string();
                            if (data.equalsIgnoreCase("")) {
                                data_global[0] = "";
                                isvalid = true;
                                status = 1;
                            } else {
                                data = data.substring(1, data.length() - 1).replace("\\", "");
                                data_global[0] = data;
                                isvalid = true;
                                status = 1;
                            }
                        } catch (Exception e) {
                            Crashlytics.logException(e);
                            e.printStackTrace();
                            isvalid = true;
                            status = -2;
                        }
                    } else {
                        isvalid = true;
                        status = -1;
                    }
                }

                @Override

                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    isvalid = true;
                    if (t instanceof SocketTimeoutException) {
                        status = 3;
                    } else if (t instanceof IOException) {
                        status = 3;
                    } else {
                        status = 3;
                    }

                }
            });

            while (isvalid == false) {
                synchronized (this) {
                    this.wait(25);
                }
            }
            if (isvalid) {
                synchronized (this) {
                    this.notify();
                }
            }
            if (status == 1) {
                return data_global[0];
            } else if (status == 2) {
                return CommonString.MESSAGE_NO_RESPONSE_SERVER;
            } else if (status == 3) {
                return CommonString.MESSAGE_SOCKETEXCEPTION;
            } else if (status == -2) {
                return CommonString.MESSAGE_INVALID_JSON;
            } else {
                return CommonString.KEY_FAILURE;
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            return CommonString.KEY_FAILURE;
        }
    }


    public void downloadDataUniversalWithoutWait(final ArrayList<String> jsonStringList, final ArrayList<String> KeyNames, final int downloadindex, int type, final int downloadFlag) {
        status = 0;
        isvalid = false;
        final String[] data_global = {""};
        String jsonString = "", KeyName = "";
        int jsonIndex = 0;

        if (jsonStringList.size() > 0) {

            jsonString = jsonStringList.get(downloadindex);
            KeyName = KeyNames.get(downloadindex);
            jsonIndex = downloadindex;

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .build();

            pd.setMessage("Downloading (" + downloadindex + "/" + listSize + ") \n" + KeyName + "");
            RequestBody jsonData = RequestBody.create(MediaType.parse("application/json"), jsonString);
            adapter = new Retrofit.Builder().baseUrl(CommonString.URL2)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            PostApi api = adapter.create(PostApi.class);
            Call<ResponseBody> call = null;

            if (type == CommonString.LOGIN_SERVICE) {
                call = api.getLogindetail(jsonData);
            } else if (type == CommonString.DOWNLOAD_ALL_SERVICE) {
                call = api.getDownloadAll(jsonData);
            } else if (type == CommonString.COVERAGE_DETAIL) {
                call = api.getCoverageDetail(jsonData);
            } else if (type == CommonString.UPLOADJCPDetail) {
                call = api.getUploadJCPDetail(jsonData);
            } else if (type == CommonString.UPLOADJsonDetail) {
                call = api.getUploadJsonDetail(jsonData);
            } else if (type == CommonString.COVERAGEStatusDetail) {
                call = api.getCoverageStatusDetail(jsonData);
            } else if (type == CommonString.CHECKOUTDetail) {
                call = api.getCheckout(jsonData);
            } else if (type == CommonString.DELETE_COVERAGE) {
                call = api.deleteCoverageData(jsonData);
            }

            final int[] finalJsonIndex = {jsonIndex};
            final String finalKeyName = KeyName;
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    ResponseBody responseBody = response.body();
                    String data = null;
                    if (responseBody != null && response.isSuccessful()) {
                        try {
                            data = response.body().string();
                            if (data.equalsIgnoreCase("")) {
                                data_global[0] = "";
                            } else {
                                data = data.substring(1, data.length() - 1).replace("\\", "");
                                data_global[0] = data;

                                if (finalKeyName.equalsIgnoreCase("Table_Structure")) {

                                    editor.putInt(CommonString.KEY_DOWNLOAD_INDEX, finalJsonIndex[0]);
                                    editor.apply();
                                    tableStructureObj = new Gson().fromJson(data, TableStructureGetterSetter.class);
                                    String isAllTableCreated = createTable(tableStructureObj);
                                    if (isAllTableCreated != CommonString.KEY_SUCCESS) {
                                        pd.dismiss();
                                        AlertandMessages.showAlert((Activity) context, isAllTableCreated + " not created", true);
                                    }
                                } else {
                                    editor.putInt(CommonString.KEY_DOWNLOAD_INDEX, finalJsonIndex[0]);
                                    editor.apply();
                                    switch (finalKeyName) {

                                      /*  case "Journey_Plan":
                                            if(!data.contains("No Data")) {
                                                journeyPlanObj = new Gson().fromJson(data, JourneyPlanGetterSetter.class);
                                                db.open();
                                                if (journeyPlanObj != null && !db.insertJourneyPlanData(journeyPlanObj)) {
                                                    pd.dismiss();
                                                    AlertandMessages.showSnackbarMsg(context, "Journey plan data not saved");
                                                } else if (journeyPlanObj != null && !db.insertLoginVisitedDate(journeyPlanObj)) {
                                                    pd.dismiss();
                                                    AlertandMessages.showSnackbarMsg(context, "login time not inserted");
                                                }
                                            }else{
                                                throw new Exception();
                                            }
                                            break;

                                        case "Sku_Master":
                                            if(!data.contains("No Data")) {
                                                skuMasterObj = new Gson().fromJson(data, SkuMasterGetterSetter.class);
                                                db.open();
                                                if (skuMasterObj != null && !db.insertSkuMasterData(skuMasterObj)) {
                                                    pd.dismiss();
                                                    AlertandMessages.showSnackbarMsg(context, "Sku master data not saved");
                                                }
                                            }else{
                                                throw new Exception();
                                            }
                                            break;

                                        case "Posm_Master":
                                            if(!data.contains("No Data")) {
                                                posmMasterObj = new Gson().fromJson(data, PosmMasterGetterSetter.class);
                                                db.open();
                                                if (posmMasterObj != null && !db.insertPosmMasterData(posmMasterObj)) {
                                                    pd.dismiss();
                                                    AlertandMessages.showSnackbarMsg(context, "POSM master data not saved");
                                                }
                                            }else{
                                                throw new Exception();
                                            }
                                            break;

                                        case "Non_Posm_Reason":
                                            if(!data.contains("No Data")) {
                                                nonPosmReasonObj = new Gson().fromJson(data, NonPosmReasonGetterSetter.class);
                                                db.open();
                                                if (nonPosmReasonObj != null && !db.insertPosmReasonData(nonPosmReasonObj)) {
                                                    pd.dismiss();
                                                    AlertandMessages.showSnackbarMsg(context, "Non POSM data not saved");
                                                }
                                            }else{
                                                throw new Exception();
                                            }
                                            break;

                                        case "Mapping_Stock":
                                            if(!data.contains("No Data")) {
                                                mappingStockObj = new Gson().fromJson(data, MappingStockGetterSetter.class);
                                                db.open();
                                                if (mappingStockObj != null && !db.insertMappingStockData(mappingStockObj)) {
                                                    pd.dismiss();
                                                    AlertandMessages.showSnackbarMsg(context, "Mapping stock data not saved");
                                                }
                                            }else{
                                                throw new Exception();
                                            }
                                            break;
                                        case "Brand_Master":
                                            if(!data.contains("No Data")) {
                                                brandMasterObj = new Gson().fromJson(data, BrandMasterGetterSetter.class);
                                                db.open();
                                                if (brandMasterObj != null && !db.brandMasterObj(brandMasterObj)) {
                                                    pd.dismiss();
                                                    AlertandMessages.showSnackbarMsg(context, "Brand master data not saved");
                                                }
                                            }else{
                                                throw new Exception();
                                            }
                                            break;

                                        case "Non_Working_Reason":
                                            if(!data.contains("No Data")) {
                                                nonWorkingReasonObj = new Gson().fromJson(data, NonWorkingReasonGetterSetter.class);
                                                db.open();
                                                if (nonWorkingReasonObj != null && !db.insertNonWorkingResionData(nonWorkingReasonObj)) {
                                                    pd.dismiss();
                                                    AlertandMessages.showSnackbarMsg(context, "Non working reason not saved");
                                                }
                                            }else{
                                                throw new Exception();
                                            }
                                            break;

                                        case "Mapping_Posm":
                                            if(!data.contains("No Data")) {
                                                mappingPOSMObj = new Gson().fromJson(data, MappingPOSMGetterSetter.class);
                                                db.open();
                                                if (mappingPOSMObj != null && !db.insertMappingPOSMData(mappingPOSMObj)) {
                                                    pd.dismiss();
                                                    AlertandMessages.showSnackbarMsg(context, "Mapping POSM data not saved");
                                                }
                                            }else{
                                                throw new Exception();
                                            }
                                            break;*/
                                    }
                                }
                            }

                            finalJsonIndex[0]++;
                            if (finalJsonIndex[0] != KeyNames.size()) {
                                editor.putInt(CommonString.KEY_DOWNLOAD_INDEX, finalJsonIndex[0]);
                                editor.apply();
                                downloadDataUniversalWithoutWait(jsonStringList, KeyNames, finalJsonIndex[0], CommonString.DOWNLOAD_ALL_SERVICE, 1);
                            } else {
                                editor.putInt(CommonString.KEY_DOWNLOAD_INDEX, 0);
                                editor.putBoolean(CommonString.DOWNLOAD_STATUS, true);
                                editor.apply();
                                AlertandMessages.showAlert((Activity) context, "Data downloaded successfully", true);

                            }
                        } catch (Exception e) {
                            Crashlytics.logException(e);
                            e.printStackTrace();
                            editor.putInt(CommonString.KEY_DOWNLOAD_INDEX, finalJsonIndex[0]);
                            editor.apply();
                            pd.dismiss();
                            AlertandMessages.showAlert((Activity) context, " Data not found in " + finalKeyName, true);
                        }
                    } else {
                        editor.putInt(CommonString.KEY_DOWNLOAD_INDEX, finalJsonIndex[0]);
                        editor.apply();
                        pd.dismiss();
                        AlertandMessages.showAlert((Activity) context, "Error in downloading Data at " + finalKeyName, true);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    isvalid = true;
                    pd.dismiss();
                    if (t instanceof SocketTimeoutException) {
                        AlertandMessages.showAlert((Activity) context, CommonString.MESSAGE_INTERNET_NOT_AVALABLE, true);
                    } else if (t instanceof IOException) {
                        AlertandMessages.showAlert((Activity) context, CommonString.MESSAGE_INTERNET_NOT_AVALABLE, true);
                    } else if (t instanceof SocketException) {
                        AlertandMessages.showAlert((Activity) context, CommonString.MESSAGE_INTERNET_NOT_AVALABLE, true);
                    } else {
                        AlertandMessages.showAlert((Activity) context, CommonString.MESSAGE_INTERNET_NOT_AVALABLE, true);
                    }

                }
            });

        } else {
            editor.putInt(CommonString.KEY_DOWNLOAD_INDEX, 0);
            editor.apply();
        }
    }


    String createTable(TableStructureGetterSetter tableGetSet) {
        List<TableQuery> tableList = tableGetSet.getResult();
        for (int i = 0; i < tableList.size(); i++) {
            String table = tableList.get(i).getSqlText();
            if (db.createtable(table) == 0) {
                return table;
            }
        }
        return CommonString.KEY_SUCCESS;
    }
}
