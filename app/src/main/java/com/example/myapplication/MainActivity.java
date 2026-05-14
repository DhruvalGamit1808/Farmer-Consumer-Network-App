package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.widget.SearchView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import android.os.Build;
import androidx.core.app.ActivityCompat;
import com.google.firebase.messaging.FirebaseMessaging;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    CircleImageView profileButton;
    ImageView cartButton;

    private RecyclerView recyclerView;
    private VegetableAdapter adapter;
    private final List<Vegetable> vegetableList = new ArrayList<>();
    private final List<Vegetable> allVegetables = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔔 Notification permission (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) return;

                    String token = task.getResult();
                    String userId = FirebaseAuth.getInstance().getUid();

                    if (userId == null || token == null) return;

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .update("fcmToken", token)
                            .addOnFailureListener(e -> {
                                // if document doesn't exist, create it safely
                                FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(userId)
                                        .set(new java.util.HashMap<String, Object>() {{
                                            put("fcmToken", token);
                                        }}, com.google.firebase.firestore.SetOptions.merge());
                            });
                });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setItemIconTintList(null);
        profileButton = findViewById(R.id.profile_button);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.ExamGreen);
        cartButton = findViewById(R.id.cart_button);

        cartButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CartActivity.class))
        );

        recyclerView = findViewById(R.id.consumer_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SearchView searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            searchView.setQueryHint("Search vegetables...");
        });

        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchIcon != null) {
            searchIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        }

        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeButton != null) {
            closeButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            closeButton.setOnClickListener(v -> {
                searchView.setQuery("", false);
                searchView.clearFocus();
                filterVegetables("");
            });
        }

        TextView searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchText != null) {
            searchText.setTextColor(Color.WHITE);
            searchText.setHintTextColor(Color.LTGRAY);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterVegetables(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterVegetables(newText);
                return true;
            }
        });

        adapter = new VegetableAdapter(vegetableList, this);

        recyclerView.setAdapter(adapter);

        profileButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int menuId = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);

            if (menuId == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else if (menuId == R.id.nav_my_orders) {
                startActivity(new Intent(MainActivity.this, MyOrdersActivity.class));
            } else if (menuId == R.id.nav_orders) {
                startActivity(new Intent(MainActivity.this, SeeOrdersActivity.class));
            } else if (menuId == R.id.nav_delivered_orders) {
                startActivity(new Intent(MainActivity.this, DeliveredOrdersActivity.class));
            } else if (menuId == R.id.nav_add_vegetable) {
                startActivity(new Intent(MainActivity.this, AddVegetableActivity.class));
            }else if (menuId == R.id.nav_settings) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                            } else if (menuId == R.id.nav_logout) {
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
            return true;
        });

        loadVegetables();
        swipeRefresh.setOnRefreshListener(this::loadVegetables);
    }

    private void loadVegetables() {
        db.collection("vegetables")
                .get()
                .addOnSuccessListener(q -> {
                    vegetableList.clear();
                    allVegetables.clear();

                    for (DocumentSnapshot d : q) {
                        int quantity = toInt(d.get("quantity"));
                        if (quantity <= 0) continue;

                        Vegetable v = new Vegetable(
                                d.getId(),
                                d.getString("name"),
                                toInt(d.get("price")),
                                quantity
                        );

                        v.setImageName(d.getString("imageName"));
                        v.setOwnerId(d.getString("farmerId"));
                        v.setOwnerName(d.getString("farmerName"));
                        v.setOwnerPhone(d.getString("farmerPhone"));

                        // ✅ NEW: Fetch farmer address
                        v.setOwnerAddress(d.getString("farmerAddress"));

                        vegetableList.add(v);
                        allVegetables.add(v);
                    }

                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load vegetables", Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void filterVegetables(String query) {
        List<Vegetable> filteredList = new ArrayList<>();
        for (Vegetable v : allVegetables) {
            if (v.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(v);
            }
        }
        vegetableList.clear();
        vegetableList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }

    private int toInt(Object o) {
        if (o instanceof Long) return ((Long) o).intValue();
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof String) try {
            return Integer.parseInt((String) o);
        } catch (Exception ignored) {
        }
        return 0;
    }
}
