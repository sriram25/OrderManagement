package com.example.ordermanagement;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements IMethodCaller {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.recylerView)
    RecyclerView recyclerView;

    OrdersAdapter mAdapter;
    List<Order> orderList;
    AlertDialog.Builder dialogBuilder;
    LayoutInflater alertInflater;
    private float lat;
    private float lng;
    private String adddress;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    ProgressDialog progress;
    Geocoder geocoder;
    List<Address> addresses;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.addOrder) {
            showLoader("Fetching your location..");
            getDeviceLocation();
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        orderList = new ArrayList<>();

        mAdapter = new OrdersAdapter(orderList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        dialogBuilder = new AlertDialog.Builder(this);
        alertInflater = this.getLayoutInflater();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        geocoder = new Geocoder(this, Locale.getDefault());

        loadData();

    }

    public void showLoader(String msg) {
        if(progress == null) {
            progress = new ProgressDialog(this);
            progress.setTitle("Loading");
            progress.setMessage(msg);
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
        }
    }

    public void loadData() {

        Order order = new Order("123", "25-09-2019", "Sriram", "0987654321", "K P H B Phase 9, Kukatpally, Hyderabad, Telangana 500085", "250", "17.123123", "78.123123");

        for (int i = 0; i < 5 ; i++) {
            orderList.add(order);
        }

        mAdapter.notifyDataSetChanged();

    }

    public void openNewOrderAlert(String type, final Order oldOrder) {
        final View dialogView = alertInflater.inflate(R.layout.neworder_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText nameEditText = (EditText) dialogView.findViewById(R.id.customerNameEditText);
        final EditText phoneEditText = (EditText) dialogView.findViewById(R.id.phoneEditText);
        final EditText orderNumberEditText = dialogView.findViewById(R.id.orderNumEditText);
        final EditText orderDueDateEditText = dialogView.findViewById(R.id.orderDueDateEditText);
        final EditText orderTotalEditText = dialogView.findViewById(R.id.orderTotalEditText);
        final TextView title = dialogView.findViewById(R.id.title);

        if (!type.equalsIgnoreCase("new")) {
            title.setText(getString(R.string.edit_order));
            if (!oldOrder.getOrderNum().isEmpty()) {
                nameEditText.setText(oldOrder.getCustomerName());
                phoneEditText.setText(oldOrder.getCustomerPhoneNumber());
                orderNumberEditText.setText(oldOrder.getOrderNum());
                orderDueDateEditText.setText(oldOrder.getOrderDueDate());
                orderTotalEditText.setText(oldOrder.getOrderTotal());
            }
        } else {
            title.setText(getString(R.string.new_order));
        }

        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        final AlertDialog b = dialogBuilder.create();
        b.setCanceledOnTouchOutside(false);
        b.show();
        b.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameValue = nameEditText.getText().toString();
                String phoneValue = phoneEditText.getText().toString();
                String orderNumValue = orderNumberEditText.getText().toString();
                String orderDueDateValue = orderDueDateEditText.getText().toString();
                String orderTotalValue = orderTotalEditText.getText().toString();

                if (!validateEditText(orderNumValue) ) {
                    //Don't dismiss
                    handleToasts(getString(R.string.order_num_errmsg));
                } else if (!validateEditText(orderDueDateValue) ) {
                    //Don't dismiss
                    handleToasts(getString(R.string.order_due_date_errmsg));
                } else if (!validateEditText(nameValue) ) {
                    //Don't dismiss
                    handleToasts(getString(R.string.name_errmsg));
                } else if (!validateEditText(phoneValue)) {
                    handleToasts(getString(R.string.phone_errmsg));
                } else if (!validateEditText(orderTotalValue)){
                    handleToasts(getString(R.string.order_total_errmsg));
                } else{

                    Order order = new Order(
                            orderNumValue,
                            orderDueDateValue,
                            nameValue,
                            phoneValue,
                            adddress,
                            orderTotalValue,
                            Float.toString(lat),
                            Float.toString(lng)
                    );

                    orderList.add(order);
                    mAdapter.notifyDataSetChanged();

                    b.dismiss();
                }
            }
        });
    }

    private void handleToasts(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private Boolean validateEditText(String str) {
        if (str.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mFusedLocationProviderClient == null) {
                // Construct a FusedLocationProviderClient.
                mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            }
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();
//                        Log.d("Location", "my location is " + mLastKnownLocation.getLatitude() + ", " + mLastKnownLocation.getLongitude());
                        lat = (float) mLastKnownLocation.getLatitude();
                        lng = (float) mLastKnownLocation.getLongitude();
                        if (mLastKnownLocation != null) {
                            progress.dismiss();
                            LatLng latLng = new LatLng(lat, lng);
                            getAddress(latLng);
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getAddress(LatLng latLng) {
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            String city = addresses.get(0).getLocality();
            String country = addresses.get(0).getCountryName();
            adddress = city + ", " + country;
            openNewOrderAlert("new", new Order("", "", "", "", "", "", "", ""));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                getDeviceLocation();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void showConfirmationAlert(final int pos) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        //Uncomment the below code to Set the message and title from the strings.xml file
        dialogBuilder.setMessage(R.string.dialog_message) .setTitle(R.string.dialog_title);

        //Setting message manually and performing action on button click
        dialogBuilder
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        orderList.remove(pos);
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        //Creating dialog box
        AlertDialog alert = dialogBuilder.create();
        //Setting the title manually
//        alert.setTitle("AlertDialogExample");
        alert.show();
    }

    @Override
    public void openEditOrderDialog(Order order) {
        openNewOrderAlert("edit", order);
    }

    @Override
    public void deleteOrder(int pos) {
        showConfirmationAlert(pos);
    }
}
