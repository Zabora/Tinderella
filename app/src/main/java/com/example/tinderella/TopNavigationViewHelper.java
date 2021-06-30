package com.example.tinderella;

import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Context;

public class TopNavigationViewHelper {
    private static final String TAG = "TopNAvigationViewHelper";
    public static void setupTopNavigationView(BottomNavigationView tv) {
        Log.d(TAG, "setupTopNavigationView: stting up navigationview");
    }
    public  static void enableNavigation(final Context context, BottomNavigationView view){
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ic_profile:
                        Intent i = new Intent(context, SettingsActivity.class);
                        context.startActivity(i);
                        break;
                    case R.id.ic_matched:
                        Intent i1 = new Intent(context, MatchesActivity.class);
                        context.startActivity(i1);
                        break;
                }

                return false;
            }
        });



    }
}
