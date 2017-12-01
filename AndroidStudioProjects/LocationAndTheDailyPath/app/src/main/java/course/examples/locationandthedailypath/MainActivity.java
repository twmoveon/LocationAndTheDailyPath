package course.examples.locationandthedailypath;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private LocationManager locationManager;
    private String provider;
    TextView latitude;
    TextView longitude;
    GoogleApiClient mGoogleApiClient;
    TextView address;
    Location loc;
    Geocoder geo;
    SQLiteDatabase db;
    Button checkin ;
    Button autoCheckIn;
    double myLat=0,myLng=0;
    String myadd="";
    String myname="";
    String mytime="";
    ListView listview;
    SimpleAdapter adapter;
    EditText edittext;
    List<Map<String,String>> checkinlist = new ArrayList<>();
    boolean autoCheckPass = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        db = openOrCreateDatabase("location",MODE_APPEND,null);
        //db.execSQL("Drop table Location");
        String sql = "create table if not exists Location(_id integer primary key autoincrement,address text,latitude double,longitude double,time text,name text)";
        db.execSQL(sql);


        checkin=(Button)findViewById(R.id.button) ;
        autoCheckIn=(Button)findViewById(R.id.autoCheckIn);
        listview=(ListView)findViewById(R.id.list);
        edittext = (EditText)findViewById(R.id.edittext);
        myname=edittext.getText().toString();

        adapter=new SimpleAdapter(this,checkinlist,R.layout.item,new String[]{"LatLng","addtime","name"},new int[]{R.id.item1,R.id.item2,R.id.item3});
        listview.setAdapter(adapter);
        loadData();

        checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myname=edittext.getText().toString();
                CheckIn();
            }
        });
        final Intent intent = new Intent(this,MapsActivity.class);

        Button showmap = (Button)findViewById(R.id.showmap);
        showmap.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                if(checkinlist.size()==0){
                    ShowAlert();
                }else {
                    startActivity(intent);
                }
            }
        });

        final Intent in = new Intent(this,LongRunningService.class);
        autoCheckIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                test();
                startService(in);
                autoCheckPass=true;
            }
        });



        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (autoCheckPass){
                    if (GetTheDistance(myLat,myLng,location.getLatitude(),location.getLongitude())>100){
                        updateLocation();
                        CheckIn();
                    }
                    else{
                        updateLocation();
                    }
                }else{
                    updateLocation();
                }


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {



            return;
        }
        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc == null) {
            // fall back to network if GPS is not available
            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (loc != null) {
            myLat = loc.getLatitude();
            myLng = loc.getLongitude();

            SimpleDateFormat simpleDataFormat = new SimpleDateFormat();
            Date curDate = new Date(System.currentTimeMillis());
            mytime = simpleDataFormat.format(curDate);
            Toast.makeText(this, mytime, Toast.LENGTH_SHORT).show();

            latitude = (TextView) findViewById(R.id.tv_currentlocation);
            longitude = (TextView) findViewById(R.id.tv_currentlocation2);
            address = (TextView) findViewById(R.id.tv_address);
            latitude.setText(String.valueOf(myLat));
            longitude.setText(String.valueOf(myLng));

            StringBuilder stringBuilder = new StringBuilder();
            geo = new Geocoder(this);
            if (geo.isPresent()) {
                Toast.makeText(this, "Get Current Location", Toast.LENGTH_SHORT).show();
            }
            try {
                List<Address> addresses = geo.getFromLocation(myLat, myLng, 1);

                if (addresses.size() > 0) {
                    Address maddress = addresses.get(0);
                    for (int i = 0; i < maddress.getMaxAddressLineIndex(); i++) {
                        stringBuilder.append(maddress.getAddressLine(i)).append("\n");
                    }
                    stringBuilder.append(maddress.getCountryName()).append("");
                    myadd=stringBuilder.toString();
                    address.setText(stringBuilder.toString());


                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("CHECK-IN");
        registerReceiver(new ReceiveCheck(),filter);



    }

    private void ShowAlert() {
        Toast.makeText(this,"Please Check-In Locations",Toast.LENGTH_SHORT).show();
    }

    public void updateLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            
            return;
        }
        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (loc == null) {
            // fall back to network if GPS is not available
            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (loc != null) {
            Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();
            myLat = loc.getLatitude();
            myLng = loc.getLongitude();

            SimpleDateFormat simpleDataFormat = new SimpleDateFormat();
            Date curDate = new Date(System.currentTimeMillis());
            mytime = simpleDataFormat.format(curDate);

            latitude = (TextView) findViewById(R.id.tv_currentlocation);
            longitude = (TextView) findViewById(R.id.tv_currentlocation2);
            address = (TextView) findViewById(R.id.tv_address);
            latitude.setText(String.valueOf(myLat));
            longitude.setText(String.valueOf(myLng));
            StringBuilder stringBuilder = new StringBuilder();
            geo = new Geocoder(this);

            try {
                List<Address> addresses = geo.getFromLocation(myLat, myLng, 1);

                if (addresses.size() > 0) {
                    Address maddress = addresses.get(0);
                    for (int i = 0; i < maddress.getMaxAddressLineIndex(); i++) {
                        stringBuilder.append(maddress.getAddressLine(i)).append("\n");
                    }
                    stringBuilder.append(maddress.getCountryName()).append("_");
                    address.setText(stringBuilder.toString());
                    myadd=stringBuilder.toString();



                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    public void CheckIn(){

        myname=edittext.getText().toString();
        Cursor cursor = db.query("Location", null, null, null, null, null, null, null);
        while (cursor.moveToNext()){
            if(GetTheDistance(myLat,myLng,cursor.getDouble(2),cursor.getDouble(3))<30){
                Toast.makeText(this,"The location is near to a before Check-in",Toast.LENGTH_SHORT).show();
                myname=cursor.getString(5);
                myadd=cursor.getString(1);
                break;
            }
        }
        PutIntoList();
        PutIntoSQL();
        Toast.makeText(this,"Check-in",Toast.LENGTH_SHORT).show();
        cursor.close();
    }
public void loadData(){
    Cursor cursor = db.query("Location", null, null, null, null, null, null, null);
    while (cursor.moveToNext()){
        Map<String, String> map = new HashMap<>();
        map.put("LatLng", "(" + cursor.getDouble(2) + ", " + cursor.getDouble(3) + ")");
        map.put("addtime", cursor.getString(1) + ", " + cursor.getString(4));
        map.put("name", cursor.getString(5));
        checkinlist.add(map);
        adapter.notifyDataSetChanged();
        }

    }
    private void PutIntoList() {
        Map<String, String> map = new HashMap<>();
        map.put("LatLng", "(" + myLat + ", " + myLng + ")");
        map.put("addtime", myadd + ", " + mytime);
        map.put("name", myname);
        checkinlist.add(map);
        adapter.notifyDataSetChanged();
    }

    private double GetTheDistance(double lat_a, double lng_a, double lat_b, double lng_b) {
        double EARTH_RADIUS = 6378137.0;
        double radLat1 = (lat_a * Math.PI / 180.0);
        double radLat2 = (lat_b * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (lng_a - lng_b) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    public void PutIntoSQL(){
        ContentValues cv = new ContentValues();
        cv.put("address", myadd);
        cv.put("latitude", myLat);
        cv.put("longitude", myLng);
        cv.put("time", String.valueOf(mytime));
        cv.put("name", myname);
        db.insert("Location", null, cv);
    }
public void test(){
    Toast.makeText(this,"Start Auto-CheckIn!",Toast.LENGTH_SHORT).show();
}

    public class ReceiveCheck extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            MainActivity mainActivity =(MainActivity)context;
            mainActivity.CheckIn();
            Log.d("Activity received", "executed at " + new Date().
                    toString());
        }
    }





    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


}