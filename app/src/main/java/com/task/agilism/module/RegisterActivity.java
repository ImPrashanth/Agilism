package com.task.agilism.module;

/**
 * Created by Prashanth on 23/10/19.
 */

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import android.util.Log;
import android.view.View;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.task.agilism.Constants;
import com.task.agilism.R;
import com.task.agilism.Utils;
import com.task.agilism.databinding.ActivityRegisterBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class RegisterActivity extends AppCompatActivity implements RegisterView {

    private RegisterPresenter mAddTaskPresenter;
    private ActivityRegisterBinding mRegisterBinding;

    private boolean isSettingsMatch = false;

    private int REQUEST_PERMISSIONS_REQUEST_CODE = 101;

    private static final String TAG = "RegisterActivity";

    private boolean locationSettingsDisabledPermantly = false;

    private LocationManager mLocationManager;

    private BluetoothAdapter mBluetoothAdapter;

    private AlertDialog mAlertDialog;

    private final String GOOGLE_API_KEY = "AIzaSyAK14EMjdoMkdXt10spiJ2_ksNAPteQ32M";
    private final String GOOGLE_API_KEY_NEW = "AIzaSyBDygmGUs7PxgOI44nHbsDEivXtvT-URAU";
    private final String GOOGLE_API_KEY_NEW_2 = "AIzaSyAddWALmv2ylzgGqFKChjBn4s6N1Tt4vTE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        mRegisterBinding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        mAddTaskPresenter = RegisterPresenterImpl.getInstance(this);

        String bleNAme = getLocalBluetoothName();

        if(bleNAme != null){
            bleNAme = bleNAme + " " + android.os.Build.MODEL;
        }else {
            bleNAme = android.os.Build.MODEL;
        }

        mRegisterBinding.activityRegisterDeviceNameTv.setText(bleNAme);

        mRegisterBinding.activityRegisterPullRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeConfirmation();
            }
        });


        if (isSettingsMatch) {
            showPermissionAlert(R.string.alert, R.string.location_not_enabled);
        }


        mRegisterBinding.activityRegisterSubmitTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClickRegister();

            }
        });


        refreshLocation();

    }



    public String getLocalBluetoothName(){
        if(mBluetoothAdapter == null){
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        String name = mBluetoothAdapter.getName();
        if(name == null){
            System.out.println("Name is null!");
            name = mBluetoothAdapter.getAddress();
        }
        return name;
    }





    public void swipeConfirmation() {
        // notify user
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert)
                .setMessage(R.string.refresh_confirmation)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        refreshLocation();

                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        mRegisterBinding.activityRegisterPullRefresh.setRefreshing(false);
                    }
                })
                .show();

    }


    private void showAlert(int title, int content) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(content)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }



    private void refreshLocation(){

        Location location = getLocation();
        if(location != null && location.getLatitude()!= 0.0 && 0.0 != location.getLongitude()){

            try {

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(this, Locale.getDefault());

                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                String subLocality = addresses.get(0).getSubLocality();

                if(null != subLocality){
                    mRegisterBinding.activityRegisterCityTv.setText(subLocality);
                }else {
                    mRegisterBinding.activityRegisterCityTv.setText(city);
                }


                mRegisterBinding.activityRegisterPullRefresh.setRefreshing(false);

            }catch (IOException exp){

                mRegisterBinding.activityRegisterPullRefresh.setRefreshing(false);

                showAlert(R.string.alert,R.string.error_occur_fetching_location);

                Log.e(TAG,"caught_exception:"+exp.getLocalizedMessage());

            }

        }else {

            showAlert(R.string.alert,R.string.error_occur_fetching_location);
            mRegisterBinding.activityRegisterPullRefresh.setRefreshing(false);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        checkRequiredSettings(this);

    }


    private void checkRequiredSettings(final Context context) {

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            permissionErrorAlert(R.string.alert, R.string.gps_network_not_enabled, true);
        } else if (!new Utils().hasNetworkConnection(this)) {
            permissionErrorAlert(R.string.alert, R.string.network_not_connected, false);
        } else {
            checkLocationSettings();
        }

    }


    public void permissionErrorAlert(int title, int content, final boolean isSettings) {
        // notify user
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        if (isSettings) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        } else {
                            checkRequiredSettings(getApplicationContext());
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        checkRequiredSettings(getApplicationContext());
                    }
                })
                .show();

    }


    private void checkLocationSettings() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);

        }
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            // for each permission check if the user granted/denied them
            // you may want to group the rationale in a single dialog,
            // this is just an example
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {
                        locationSettingsDisabledPermantly = true;
                        // user also CHECKED "never ask again"
                        // you can either enable some fall back,
                        // disable features of your app
                        // or open another dialog explaining
                        // again the permission and directing to
                        // the app setting

                    } else if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)) {
                        // user did NOT check "never ask again"
                        // this is a good place to explain the user
                        // why you need the permission and ask if he wants
                        // to accept it (the rationale)
                    }
                }
            }
        }
    }


    private void showPermissionAlert(int title, int content) {

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(content)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                            ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);

                        }
                    })
                    .show();
            return;
        }
        showPermissionAlert(R.string.alert, R.string.location_not_enabled);

    }


    public Location getLocation() {

        Location location = null;

        try {

            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // getting passive status
            boolean isPassiveEnabled = mLocationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                showAlert(R.string.alert,R.string.app_name);
            } else {

                if (isPassiveEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
//                        return TODO;
                    }
                    location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    if (location == null) {
                        location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (location != null) {
            android.util.Log.d(TAG, "getLocation: " + location.getLatitude());
            android.util.Log.d(TAG, "getLocation: " + location.getLongitude());
        }

        return location;
    }



    public void onClickRegister() {

        if(!mAddTaskPresenter.isValidName(mRegisterBinding.activityRegisterFirstNameEt.getText().toString())){
            Utils.showToast(this, Constants.StatusMessage.INVALID_FIRST_NAME);
            mRegisterBinding.activityRegisterFirstNameEt.requestFocus();
        }else if(!mAddTaskPresenter.isValidName(mRegisterBinding.activityRegisterLastNameEt.getText().toString())){
            Utils.showToast(this, Constants.StatusMessage.INVALID_LAST_NAME);
            mRegisterBinding.activityRegisterLastNameEt.requestFocus();
        }else if(!mAddTaskPresenter.isValidEmail(mRegisterBinding.activityRegisterEmailEt.getText().toString())) {
            Utils.showToast(this, Constants.StatusMessage.INVALID_EMAIL);
            mRegisterBinding.activityRegisterEmailEt.requestFocus();
        }else {

            showAlert(R.string.alert,R.string.register_success);
            mRegisterBinding.activityRegisterFirstNameEt.setText("");
            mRegisterBinding.activityRegisterFirstNameEt.requestFocus();
            mRegisterBinding.activityRegisterLastNameEt.setText("");
            mRegisterBinding.activityRegisterEmailEt.setText("");
        }
    }
}
