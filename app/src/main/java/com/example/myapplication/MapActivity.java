package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
public class MapActivity extends AppCompatActivity {

    private MapView map;

    private Marker deliveryMarker;
    private Marker customerMarker;
    private Polyline routeLine;

    private double currentLat = 23.0225;
    private double currentLng = 72.5714;

    private double destLat = 0;
    private double destLng = 0;

    private boolean isCustomerLoaded = false;

    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map);

        map = findViewById(R.id.map);
        map.setMultiTouchControls(true);

        // ✅ Instant map load
        GeoPoint defaultPoint = new GeoPoint(currentLat, currentLng);
        map.getController().setZoom(14.0);
        map.getController().setCenter(defaultPoint);



        orderId = getIntent().getStringExtra("orderId");

        if (orderId == null) {
            Toast.makeText(this, "Order ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCustomer();     // fast
        startTracking();    // live
    }

    // =========================
    // 🔴 CUSTOMER
    // =========================
    private void loadCustomer() {

        FirebaseFirestore.getInstance()
                .collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(orderDoc -> {

                    if (!orderDoc.exists()) return;

                    String buyerId = orderDoc.getString("buyerId");

                    if (buyerId == null) return;

                    // 🔥 REAL-TIME LISTENER FOR CUSTOMER LOCATION
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(buyerId)
                            .addSnapshotListener((userDoc, error) -> {

                                if (userDoc == null || !userDoc.exists()) return;

                                Double lat = userDoc.getDouble("lat");
                                Double lng = userDoc.getDouble("lng");

                                if (lat == null || lng == null) return;

                                destLat = lat;
                                destLng = lng;

                                GeoPoint dest = new GeoPoint(destLat, destLng);

                                // 🔴 Create marker once
                                if (customerMarker == null) {
                                    customerMarker = new Marker(map);
                                    customerMarker.setTitle("Customer");
                                    map.getOverlays().add(customerMarker);

                                    isCustomerLoaded = true; // ✅ IMPORTANT
                                }

                                customerMarker.setPosition(dest);
                                map.invalidate();

                                // 🔥 OPTIONAL: update route when customer moves
                                drawRoute();
                            });
                });
    }

    // =========================
    // 🟢 LIVE TRACKING (FAST)
    // =========================
    private void startTracking() {

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1
            );
            return;
        }

        // ✅ FAST PROVIDER
        lm.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                2000,
                2,
                location -> runOnUiThread(() -> updateLocation(location))
        );
    }

    private void updateLocation(android.location.Location location) {

        currentLat = location.getLatitude();
        currentLng = location.getLongitude();

        GeoPoint point = new GeoPoint(currentLat, currentLng);

        // 🟢 Create marker once
        if (deliveryMarker == null) {
            deliveryMarker = new Marker(map);
            deliveryMarker.setTitle("You");
            map.getOverlays().add(deliveryMarker);
        }

        deliveryMarker.setPosition(point);

        // ❌ DON'T RECENTER EVERY TIME
        if (map.getZoomLevelDouble() < 15) {
            map.getController().setZoom(15.0);
            map.getController().setCenter(point);
        }

        map.invalidate();

        // 🔥 DRAW ROUTE WHEN READY
        if (isCustomerLoaded) {
            drawRoute();
        }
    }

    // =========================
    // 🔵 ROUTE (SMART UPDATE)
    // =========================
    private void drawRoute() {

        new Thread(() -> {
            try {

                String urlStr = "https://router.project-osrm.org/route/v1/driving/"
                        + currentLng + "," + currentLat + ";"
                        + destLng + "," + destLat
                        + "?overview=full&geometries=geojson";

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

                StringBuilder json = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }

                JSONArray coords = new JSONObject(json.toString())
                        .getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates");

                ArrayList<GeoPoint> points = new ArrayList<>();

                for (int i = 0; i < coords.length(); i++) {
                    JSONArray p = coords.getJSONArray(i);
                    points.add(new GeoPoint(p.getDouble(1), p.getDouble(0)));
                }

                runOnUiThread(() -> {

                    // 🔥 REMOVE OLD ROUTE
                    if (routeLine != null) {
                        map.getOverlays().remove(routeLine);
                    }

                    routeLine = new Polyline();
                    routeLine.setPoints(points);
                    routeLine.setColor(android.graphics.Color.BLUE);
                    routeLine.setWidth(8f);

                    map.getOverlays().add(routeLine);
                    map.invalidate();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}