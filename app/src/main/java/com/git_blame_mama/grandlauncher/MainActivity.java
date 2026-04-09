package com.git_blame_mama.grandlauncher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvBattery;
    private RecyclerView recyclerView;
    private PrefsManager prefsManager;

    // Ресивер для батареи
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            tvBattery.setText(getString(R.string.battery_prefix, level));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefsManager = new PrefsManager(this);
        tvBattery = findViewById(R.id.tvBattery);
        recyclerView = findViewById(R.id.recyclerView);
        Button btnSettings = findViewById(R.id.btnSettings);

        CallNotificationHelper.createChannel(this);

        btnSettings.setOnClickListener(v ->
                Toast.makeText(this, R.string.toast_hold_for_settings, Toast.LENGTH_SHORT).show()
        );
        btnSettings.setOnLongClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        });

        requestEssentialPermissions();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Ничего не делаем, так как это лаунчер
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем сетку при каждом возврате на главный экран
        setupGrid();
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(batteryReceiver);
    }

    private void requestEssentialPermissions() {
        java.util.List<String> needed = new java.util.ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.CALL_PHONE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (!needed.isEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), 1);
        }
    }

    private void setupGrid() {
        List<GridItem> items = prefsManager.getGridItems();
        // Сетка в 2 столбца
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // ВАЖНО: Ниже псевдокод адаптера. Вам нужно создать GridAdapter,
        // который принимает список items и назначает разные цвета (res/colors) в зависимости от item.type
        GridAdapter adapter = new GridAdapter(items, item -> {
            if (item.type == GridItem.Type.CONTACT || item.type == GridItem.Type.SOS) {
                makeCall(item.data);
            } else if (item.type == GridItem.Type.APP) {
                launchApp(item.data);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void makeCall(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        startActivity(callIntent);
    }

    private void launchApp(String packageName) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Toast.makeText(this, R.string.app_not_found, Toast.LENGTH_SHORT).show();
        }
    }
}