package com.git_blame_mama.grandlauncher; // Проверьте, чтобы package совпадал с вашим!

import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private PrefsManager prefsManager;

    // Регистратор для запроса прав на фильтрацию звонков
    private final ActivityResultLauncher<Intent> roleRequestLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, "Защита звонков успешно включена!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Вы отклонили запрос.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefsManager = new PrefsManager(this);

        Button btnRequestRole = findViewById(R.id.btnRequestRole);
        Button btnAddContact = findViewById(R.id.btnAddContact);
        Button btnAddApp = findViewById(R.id.btnAddApp);
        Button btnSaveSos = findViewById(R.id.btnSaveSos);
        Button btnKillLauncher = findViewById(R.id.btnKillLauncher);

        EditText etName = findViewById(R.id.etName);
        EditText etNumber = findViewById(R.id.etNumber);
        EditText etSosNumber = findViewById(R.id.etSosNumber);

        // --- 1. Включение защиты ---
        btnRequestRole.setOnClickListener(v -> requestCallScreeningRole());

        // --- 2. Сохранение номера SOS ---
        btnSaveSos.setOnClickListener(v -> {
            String sosNum = etSosNumber.getText().toString().trim();
            if (!sosNum.isEmpty()) {
                updateSosNumber(sosNum);
                Toast.makeText(this, "Номер SOS обновлен", Toast.LENGTH_SHORT).show();
                etSosNumber.setText("");
            } else {
                Toast.makeText(this, "Введите номер", Toast.LENGTH_SHORT).show();
            }
        });

        // --- 3. Добавление Контакта ---
        btnAddContact.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String number = etNumber.getText().toString().trim();
            if(!name.isEmpty() && !number.isEmpty()) {
                prefsManager.addNumberToWhitelist(number); // В белый список
                List<GridItem> items = prefsManager.getGridItems();
                items.add(new GridItem(name, number, GridItem.Type.CONTACT));
                prefsManager.saveGridItems(items); // На экран

                Toast.makeText(this, "Контакт добавлен", Toast.LENGTH_SHORT).show();
                etName.setText("");
                etNumber.setText("");
            } else {
                Toast.makeText(this, "Заполните оба поля", Toast.LENGTH_SHORT).show();
            }
        });

        // --- 4. Выбор и добавление Приложения ---
        btnAddApp.setOnClickListener(v -> showAppSelectionDialog());

        // --- 5. Выход из Лаунчера ---
        btnKillLauncher.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
            startActivity(intent);
        });
    }

    // Метод обновления номера SOS в настройках
    private void updateSosNumber(String newNumber) {
        List<GridItem> items = prefsManager.getGridItems();
        boolean found = false;
        for (GridItem item : items) {
            if (item.type == GridItem.Type.SOS) {
                item.data = newNumber; // Меняем номер
                found = true;
                break;
            }
        }
        // Если кто-то случайно удалил кнопку SOS, создадим её заново
        if (!found) {
            items.add(0, new GridItem("SOS", newNumber, GridItem.Type.SOS));
        }
        prefsManager.saveGridItems(items);
    }

    // Метод показа диалога со всеми установленными приложениями
    private void showAppSelectionDialog() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        // Получаем список всех приложений
        List<ResolveInfo> availableApps = pm.queryIntentActivities(intent, 0);

        // Сортируем по алфавиту
        Collections.sort(availableApps, new ResolveInfo.DisplayNameComparator(pm));

        // Массив имен для показа в списке
        String[] appNames = new String[availableApps.size()];
        for (int i = 0; i < availableApps.size(); i++) {
            appNames[i] = availableApps.get(i).loadLabel(pm).toString();
        }

        // Строим красивое всплывающее окно
        new AlertDialog.Builder(this)
                .setTitle("Выберите приложение")
                .setItems(appNames, (dialog, which) -> {
                    // Пользователь выбрал приложение
                    ResolveInfo info = availableApps.get(which);
                    String packageName = info.activityInfo.packageName;
                    String appName = info.loadLabel(pm).toString();

                    // Добавляем в нашу сетку
                    List<GridItem> items = prefsManager.getGridItems();
                    items.add(new GridItem(appName, packageName, GridItem.Type.APP));
                    prefsManager.saveGridItems(items);

                    Toast.makeText(this, appName + " добавлено на экран", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);
            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
            roleRequestLauncher.launch(intent);
        }
    }
}