package com.example.agrimall;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.agrimall.CartFragment;
import com.example.agrimall.HomeFragment;
import com.example.agrimall.ProfileFragment;
import com.example.agrimall.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Load HomeFragment by default
        loadFragment(new HomeFragment());


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                Fragment selectedFragment = null;
                if (item.getItemId() == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (item.getItemId() == R.id.nav_cart) {
                    selectedFragment = new CartFragment();
                } else if (item.getItemId() == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                } else if (item.getItemId()==R.id.nav_services){
                    selectedFragment = new ServicesFragment();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }
                return true;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment) // Fragment loads here
                .commit();
    }

}
