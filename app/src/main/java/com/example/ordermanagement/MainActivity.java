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
import android.net.Uri;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
    FirebaseDatabase database;
    DatabaseReference myRef;
    HelperMethods helperMethods;

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

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("myDB").child("orders");

        helperMethods = new HelperMethods();

        showLoader("Loading Data...");
        fetchData();

    }

    // displays progress dialog
    public void showLoader(String msg) {
        if(progress == null) {
            progress = new ProgressDialog(this);
            progress.setTitle("Loading");
            progress.setMessage(msg);
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
        }
    }

    // stores data in firebase
    public void storeData(Order order) {

        orderList.add(order);
        myRef.setValue(orderList);
        mAdapter.notifyDataSetChanged();

    }

    // fetches data from firebase
    public void fetchData() {

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                progress.dismiss();
                orderList.clear();

                // This method is called once with the initial value and again
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    // TODO: handle the post
                    Order order1 = postSnapshot.getValue(Order.class);
                    assert order1 != null;
                    Log.d("TAG", order1.getCustomerName());
                    orderList.add(order1);
                }

                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    // opens alert dialog for new order and edit order.
    public void openNewOrderAlert(final String type, final int pos) {
        final View dialogView = alertInflater.inflate(R.layout.neworder_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText nameEditText = (EditText) dialogView.findViewById(R.id.customerNameEditText);
        final EditText phoneEditText = (EditText) dialogView.findViewById(R.id.phoneEditText);
        final EditText orderNumberEditText = dialogView.findViewById(R.id.orderNumEditText);
        final EditText orderDueDateEditText = dialogView.findViewById(R.id.orderDueDateEditText);
        final EditText orderTotalEditText = dialogView.findViewById(R.id.orderTotalEditText);
        final TextView title = dialogView.findViewById(R.id.title);


        orderNumberEditText.setEnabled(false);

        if (!type.equalsIgnoreCase("new")) {
            title.setText(getString(R.string.edit_order));
            Order oldOrder = orderList.get(pos);
            if (!oldOrder.getOrderNum().isEmpty()) {
                nameEditText.setText(oldOrder.getCustomerName());
                phoneEditText.setText(oldOrder.getCustomerPhoneNumber());
                orderNumberEditText.setText(oldOrder.getOrderNum());
                orderDueDateEditText.setText(oldOrder.getOrderDueDate());
                orderTotalEditText.setText(oldOrder.getOrderTotal());
            }
        } else {
            title.setText(getString(R.string.new_order));
            long millis = new Date().getTime();
            orderNumberEditText.setText(String.valueOf(millis));
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

                if (!helperMethods.validateEditText(orderNumValue) ) {
                    helperMethods.handleToasts(getString(R.string.order_num_errmsg), getApplicationContext());
                } else if (!helperMethods.validateEditText(orderDueDateValue) ) {
                    helperMethods.handleToasts(getString(R.string.order_due_date_errmsg), getApplicationContext());
                } else if (!helperMethods.validateEditText(nameValue) ) {
                    helperMethods.handleToasts(getString(R.string.name_errmsg), getApplicationContext());
                } else if (!helperMethods.validateEditText(phoneValue)) {
                    helperMethods.handleToasts(getString(R.string.phone_errmsg), getApplicationContext());
                } else if (!helperMethods.validateEditText(orderTotalValue)){
                    helperMethods.handleToasts(getString(R.string.order_total_errmsg), getApplicationContext());
                } else{

                    if (!type.equalsIgnoreCase("new")) {
                        Order oldOrder = orderList.get(pos);
                        Order order = new Order(
                                orderNumValue,
                                orderDueDateValue,
                                nameValue,
                                phoneValue,
                                oldOrder.getCustomerAddrs(),
                                orderTotalValue,
                                oldOrder.getLat(),
                                oldOrder.getLng()
                        );
                        orderList.set(pos, order);
                        myRef.child(String.valueOf(pos)).updateChildren(order.toMap());
                    } else {
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
                        storeData(order);
                        orderList.add(order);
                    }

                    mAdapter.notifyDataSetChanged();

                    b.dismiss();
                }
            }
        });
    }

    // fetches current location
    private void getDeviceLocation() {
        try {
            if (mFusedLocationProviderClient == null) {
                mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            }
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        mLastKnownLocation = task.getResult();
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

    // fetches address from latlng
    private void getAddress(LatLng latLng) {
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            String city = addresses.get(0).getLocality();
            String country = addresses.get(0).getCountryName();
            adddress = city + ", " + country;
            openNewOrderAlert("new", 0);
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

    // shows confirmation alert dialog
    private void showConfirmationAlert(final int pos) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.dialog_message) .setTitle(R.string.dialog_title);
        dialogBuilder
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        myRef.child(String.valueOf(pos)).removeValue();
                        orderList.remove(pos);
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = dialogBuilder.create();
        alert.show();
    }

    // Opens Google Maps with given location.
    public void openGoogleMaps(Order order) {
        String strUri = "http://maps.google.com/maps?q=loc:" + Float.parseFloat(order.getLat()) + "," + Float.parseFloat(order.getLng()) + " (" + "Label which you want" + ")";
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    @Override
    public void openEditOrderDialog(int pos) {
        openNewOrderAlert("edit", pos);
    }

    @Override
    public void deleteOrder(int pos) {
        showConfirmationAlert(pos);
    }

    @Override
    public void opemGoogleMaps(Order order) {
        openGoogleMaps(order);
    }

}
