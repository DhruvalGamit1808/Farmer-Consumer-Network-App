package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;
import java.util.Map;

public class ChangeAddressActivity extends AppCompatActivity {

    private MapView map;
    private FirebaseFirestore db;
    private String userId;

    private double selectedLat = -1;
    private double selectedLng = -1;

    private Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_change_address);

        map = findViewById(R.id.map);
        Button saveBtn = findViewById(R.id.btnSaveLocation);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        // =======================
        // MAP SETUP (IMPORTANT)
        // =======================
        map.setMultiTouchControls(true);
        map.setClickable(true);

        GeoPoint defaultPoint = new GeoPoint(23.0225, 72.5714);
        map.getController().setZoom(15.0);
        map.getController().setCenter(defaultPoint);

        // =======================
        // LOAD SAVED LOCATION
        // =======================
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {

                    Double lat = doc.getDouble("lat");
                    Double lng = doc.getDouble("lng");

                    if (lat != null && lng != null) {

                        GeoPoint saved = new GeoPoint(lat, lng);

                        selectedLat = lat;
                        selectedLng = lng;

                        map.getController().setCenter(saved);

                        showMarker(saved);
                    }
                });

        // =======================
        // MAP TAP LISTENER
        // =======================
        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                selectedLat = p.getLatitude();
                selectedLng = p.getLongitude();

                showMarker(p);

                Toast.makeText(ChangeAddressActivity.this,
                        "Location Selected",
                        Toast.LENGTH_SHORT).show();

                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay overlay = new MapEventsOverlay(receiver);
        map.getOverlays().add(overlay);

        // =======================
        // SAVE LOCATION
        // =======================
        saveBtn.setOnClickListener(v -> {

            if (selectedLat == -1 || selectedLng == -1) {
                Toast.makeText(this,
                        "Please select location first",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("lat", selectedLat);
            data.put("lng", selectedLng);

            db.collection("users")
                    .document(userId)
                    .update(data)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this,
                                    "Address Updated",
                                    Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Update Failed",
                                    Toast.LENGTH_SHORT).show()
                    );
        });
    }

    // =======================
    // MARKER HANDLER (SAFE)
    // =======================
    private void showMarker(GeoPoint point) {

        if (currentMarker != null) {
            map.getOverlays().remove(currentMarker);
        }

        currentMarker = new Marker(map);
        currentMarker.setPosition(point);
        currentMarker.setTitle("Selected Location");

        map.getOverlays().add(currentMarker);
        map.invalidate();
    }
}