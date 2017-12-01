package course.examples.locationandthedailypath;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String myname, mytime, myadd;
    double myLat, myLng;
    String newTitle;
    MarkerOptions markerOptions;
    SQLiteDatabase db;
    LatLng newLatLng;
    Location loc;
    List<Marker> markerList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        db = openOrCreateDatabase("location", MODE_APPEND, null);
        Cursor cursor = db.query("Location", null, null, null, "address", null, null, null);
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
            myLat = loc.getLatitude();
            myLng = loc.getLongitude();

        }


        LatLng currentLatLng = new LatLng(myLat, myLng);

        mMap = googleMap;
        while (cursor.moveToNext()){

            Marker marker =mMap.addMarker(new MarkerOptions().position(new LatLng(cursor.getDouble(2), cursor.getDouble(3))).title(cursor.getString(5)).snippet(cursor.getString(4)));
                markerList.add(marker);
            if (GetTheDistance(myLat,myLng,cursor.getDouble(2),cursor.getDouble(3))<30){
                marker.showInfoWindow();
            }

        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                            myLat = loc.getLatitude();
                            myLng = loc.getLongitude();

                            for (int i=0;i<markerList.size();i++){

                                if (GetTheDistance(myLat, myLng, markerList.get(i).getPosition().latitude,markerList.get(i).getPosition().longitude ) < 30) {
                                    markerList.get(i).showInfoWindow();
                                }
                                if(GetTheDistance(myLat, myLng, markerList.get(i).getPosition().latitude,markerList.get(i).getPosition().longitude ) >= 30){
                                    markerList.get(i).hideInfoWindow();
                                }
                            }
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLat,myLng),15));
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

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,12));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);


        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                mMap.moveCamera(CameraUpdateFactory.zoomBy(3));

                return false;
            }
        });







        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {


            @Override
            public void onMapClick(LatLng latLng) {
                markerOptions = new MarkerOptions();
                markerOptions.draggable(true);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                markerOptions.position(latLng);
                newLatLng = latLng;

                PopUp(MapsActivity.this);


            }
        });


        cursor.close();
    }

    private void PopUp(Context context) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(
                R.layout.dialoglayout, null);
        final EditText edtInput = (EditText) textEntryView.findViewById(R.id.edtInput);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);

        builder.setTitle("Please input the name of new Marker");
        builder.setView(textEntryView);
        builder.setPositiveButton("Enter",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        newTitle = (edtInput.getText()).toString();
                        markerOptions.title(newTitle);
                        mMap.addMarker(markerOptions).showInfoWindow();


                        ContentValues cv = new ContentValues();
                        cv.put("address", getAddress(newLatLng.latitude,newLatLng.longitude));
                        cv.put("latitude", newLatLng.latitude);
                        cv.put("longitude", newLatLng.longitude);
                        cv.put("time", getTime());
                        cv.put("name", newTitle);
                        db.insert("Location", null, cv);

                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setTitle("");
                    }
                });
        builder.show();


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

    public String getAddress(double lat,double lon) {
        StringBuilder stringBuilder = new StringBuilder();
        Geocoder geo = new Geocoder(this);
        try {
            List<Address> addresses = geo.getFromLocation(lat, lon, 1);

            if (addresses.size() > 0) {
                Address maddress = addresses.get(0);
                for (int i = 0; i < maddress.getMaxAddressLineIndex(); i++) {
                    stringBuilder.append(maddress.getAddressLine(i)).append("\n");
                }
                stringBuilder.append(maddress.getCountryName()).append("_");
                //stringBuilder.append(maddress.getFeatureName()).append("_");//周边地址
                //stringBuilder.append(maddress.getLocality()).append("_");//市
                //stringBuilder.append(maddress.getPostalCode()).append("_");
                //stringBuilder.append(maddress.getCountryCode()).append("_");//国家编码
                //stringBuilder.append(maddress.getAdminArea()).append("_");//省份
                //stringBuilder.append(maddress.getSubAdminArea()).append("_");
                //stringBuilder.append(maddress.getThoroughfare()).append("_");//道路
                //stringBuilder.append(maddress.getSubLocality()).append("_");//香洲区
                //stringBuilder.append(maddress.getLatitude()).append("_");//经度
                //stringBuilder.append(maddress.getLongitude());//维度
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
    public String getTime(){
        SimpleDateFormat simpleDataFormat = new SimpleDateFormat();
        Date curDate = new Date(System.currentTimeMillis());
        return simpleDataFormat.format(curDate);
    }

}
