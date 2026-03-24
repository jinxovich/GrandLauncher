package com.git_blame_mama.grandlauncher; // Проверьте, чтобы package совпадал с вашим!

import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private PrefsManager prefsManager;
    private AllowedContactsAdapter allowedContactsAdapter;
    private MainMenuCellsAdapter mainMenuCellsAdapter;

    private TextView tvSelectedContactIcon;
    private TextView tvRoleStatus;
    private TextView tvScreeningLog;
    private View bannerRoleStatus;
    private TextView tvRoleBannerTitle;
    private TextView tvRoleBannerDesc;
    private TextView tvRoleBannerIcon;
    private EditText etName;
    private EditText etNumber;
    private EditText etSosNumber;
    private String selectedContactIconKey = "phone";

    private static final String[] CONTACT_ICON_KEYS = {
            "phone", "family", "home", "doctor", "favorite"
    };
    private static final int[] CONTACT_ICON_LABELS = {
            R.string.icon_phone,
            R.string.icon_family,
            R.string.icon_home,
            R.string.icon_doctor,
            R.string.icon_favorite
    };

    // Регистратор для запроса прав на фильтрацию звонков
    private final ActivityResultLauncher<Intent> roleRequestLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, R.string.toast_call_screening_enabled, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.toast_call_screening_rejected, Toast.LENGTH_SHORT).show();
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
        Button btnPickContactIcon = findViewById(R.id.btnPickContactIcon);

        RecyclerView rvAllowedContacts = findViewById(R.id.rvAllowedContacts);
        RecyclerView rvMainCells = findViewById(R.id.rvMainCells);

        etName = findViewById(R.id.etName);
        etNumber = findViewById(R.id.etNumber);
        etSosNumber = findViewById(R.id.etSosNumber);
        tvSelectedContactIcon = findViewById(R.id.tvSelectedContactIcon);
        tvRoleStatus          = findViewById(R.id.tvRoleStatus);
        tvScreeningLog        = findViewById(R.id.tvScreeningLog);
        bannerRoleStatus      = findViewById(R.id.bannerRoleStatus);
        tvRoleBannerTitle     = findViewById(R.id.tvRoleBannerTitle);
        tvRoleBannerDesc      = findViewById(R.id.tvRoleBannerDesc);
        tvRoleBannerIcon      = findViewById(R.id.tvRoleBannerIcon);

        CallNotificationHelper.createChannel(this);

        allowedContactsAdapter = new AllowedContactsAdapter(new AllowedContactsAdapter.OnAllowedContactActionListener() {
            @Override
            public void onAddToMain(AllowedContact contact) {
                addAllowedContactToMain(contact);
            }

            @Override
            public void onDelete(AllowedContact contact) {
                confirmDeleteAllowedContact(contact);
            }
        });
        rvAllowedContacts.setLayoutManager(new LinearLayoutManager(this));
        rvAllowedContacts.setAdapter(allowedContactsAdapter);

        mainMenuCellsAdapter = new MainMenuCellsAdapter(this::confirmDeleteMainCell);
        rvMainCells.setLayoutManager(new LinearLayoutManager(this));
        rvMainCells.setAdapter(mainMenuCellsAdapter);

        updateSelectedIconCaption();
        refreshScreenData();

        btnRequestRole.setOnClickListener(v -> requestCallScreeningRole());

        btnSaveSos.setOnClickListener(v -> {
            String sosNum = this.etSosNumber.getText().toString().trim();
            if (!sosNum.isEmpty()) {
                updateSosNumber(sosNum);
                Toast.makeText(this, R.string.toast_sos_updated, Toast.LENGTH_SHORT).show();
                refreshMainCells();
            } else {
                Toast.makeText(this, R.string.toast_enter_number, Toast.LENGTH_SHORT).show();
            }
        });

        btnPickContactIcon.setOnClickListener(v -> showContactIconDialog());

        btnAddContact.setOnClickListener(v -> {
            String name = this.etName.getText().toString().trim();
            String number = this.etNumber.getText().toString().trim();
            if(!name.isEmpty() && !number.isEmpty()) {
                prefsManager.addOrUpdateAllowedContact(name, number, selectedContactIconKey);
                Toast.makeText(this, R.string.toast_allowed_contact_saved, Toast.LENGTH_SHORT).show();
                this.etName.setText("");
                this.etNumber.setText("");
                refreshAllowedContacts();
            } else {
                Toast.makeText(this, R.string.toast_fill_fields, Toast.LENGTH_SHORT).show();
            }
        });

        btnAddApp.setOnClickListener(v -> showAppSelectionDialog());

        btnKillLauncher.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshScreenData();
    }

    private void refreshScreenData() {
        refreshAllowedContacts();
        refreshMainCells();
        fillSosField();
        refreshRoleStatus();
        refreshScreeningLog();
    }

    private void refreshRoleStatus() {
        RoleManager rm = (RoleManager) getSystemService(ROLE_SERVICE);
        boolean held = rm != null && rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING);

        // Маленький индикатор в карточке безопасности
        if (held) {
            tvRoleStatus.setText(R.string.role_status_active);
            tvRoleStatus.setTextColor(ContextCompat.getColor(this, R.color.status_ok));
        } else {
            tvRoleStatus.setText(R.string.role_status_inactive);
            tvRoleStatus.setTextColor(ContextCompat.getColor(this, R.color.status_error));
        }

        // Крупный баннер вверху настроек
        if (held) {
            bannerRoleStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.tile_contact));
            tvRoleBannerIcon.setText("✓");
            tvRoleBannerTitle.setText(R.string.role_banner_active_title);
            tvRoleBannerDesc.setText(R.string.role_banner_active_desc);
        } else {
            bannerRoleStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.tile_sos));
            tvRoleBannerIcon.setText("⚠");
            tvRoleBannerTitle.setText(R.string.role_banner_inactive_title);
            tvRoleBannerDesc.setText(R.string.role_banner_inactive_desc);
        }
    }

    private void refreshScreeningLog() {
        String log = prefsManager.getLastScreeningLog();
        tvScreeningLog.setText(log.isEmpty() ? getString(R.string.screening_log_empty) : log);
    }

    private void refreshAllowedContacts() {
        allowedContactsAdapter.submit(prefsManager.getAllowedContacts());
    }

    private void refreshMainCells() {
        mainMenuCellsAdapter.submit(prefsManager.getGridItems());
    }

    private void fillSosField() {
        for (GridItem item : prefsManager.getGridItems()) {
            if (item.type == GridItem.Type.SOS) {
                etSosNumber.setText(item.data);
                return;
            }
        }
    }

    private void updateSosNumber(String newNumber) {
        List<GridItem> items = prefsManager.getGridItems();
        boolean found = false;
        for (GridItem item : items) {
            if (item.type == GridItem.Type.SOS) {
                item.data = newNumber;
                found = true;
                break;
            }
        }
        if (!found) {
            items.add(0, new GridItem("SOS", newNumber, GridItem.Type.SOS, "sos"));
        }
        prefsManager.saveGridItems(items);
    }

    private void showAppSelectionDialog() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableApps = pm.queryIntentActivities(intent, 0);
        Collections.sort(availableApps, new ResolveInfo.DisplayNameComparator(pm));

        String[] appNames = new String[availableApps.size()];
        for (int i = 0; i < availableApps.size(); i++) {
            appNames[i] = availableApps.get(i).loadLabel(pm).toString();
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_select_app)
                .setItems(appNames, (dialog, which) -> {
                    ResolveInfo info = availableApps.get(which);
                    String packageName = info.activityInfo.packageName;
                    String appName = info.loadLabel(pm).toString();

                    List<GridItem> items = prefsManager.getGridItems();
                    items.add(new GridItem(appName, packageName, GridItem.Type.APP));
                    prefsManager.saveGridItems(items);

                    Toast.makeText(this, getString(R.string.toast_app_added, appName), Toast.LENGTH_SHORT).show();
                    refreshMainCells();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void addAllowedContactToMain(AllowedContact contact) {
        List<GridItem> items = new ArrayList<>(prefsManager.getGridItems());
        for (GridItem item : items) {
            if ((item.type == GridItem.Type.CONTACT || item.type == GridItem.Type.SOS) && item.data.equals(contact.number)) {
                Toast.makeText(this, R.string.toast_contact_already_on_main, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        items.add(new GridItem(contact.name, contact.number, GridItem.Type.CONTACT, contact.iconKey));
        prefsManager.saveGridItems(items);
        Toast.makeText(this, R.string.toast_contact_added_to_main, Toast.LENGTH_SHORT).show();
        refreshMainCells();
    }

    private void confirmDeleteAllowedContact(AllowedContact contact) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_allowed_title)
                .setMessage(getString(R.string.dialog_delete_allowed_message, contact.name))
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    prefsManager.removeAllowedContact(contact.number);
                    refreshAllowedContacts();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void confirmDeleteMainCell(GridItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_cell_title)
                .setMessage(getString(R.string.dialog_delete_cell_message, item.label))
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    List<GridItem> items = new ArrayList<>(prefsManager.getGridItems());
                    items.removeIf(entry ->
                            entry.type == item.type
                                    && safeEquals(entry.label, item.label)
                                    && safeEquals(entry.data, item.data)
                    );
                    prefsManager.saveGridItems(items);
                    refreshMainCells();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private boolean safeEquals(String first, String second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return first.equals(second);
    }

    private void showContactIconDialog() {
        String[] labels = new String[CONTACT_ICON_LABELS.length];
        int selectedIndex = 0;
        for (int i = 0; i < CONTACT_ICON_LABELS.length; i++) {
            labels[i] = getString(CONTACT_ICON_LABELS[i]);
            if (CONTACT_ICON_KEYS[i].equals(selectedContactIconKey)) {
                selectedIndex = i;
            }
        }

        final int[] pendingSelection = {selectedIndex};
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_choose_icon)
                .setSingleChoiceItems(labels, selectedIndex, (dialog, which) -> pendingSelection[0] = which)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    selectedContactIconKey = CONTACT_ICON_KEYS[pendingSelection[0]];
                    updateSelectedIconCaption();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void updateSelectedIconCaption() {
        int labelRes = R.string.icon_phone;
        for (int i = 0; i < CONTACT_ICON_KEYS.length; i++) {
            if (CONTACT_ICON_KEYS[i].equals(selectedContactIconKey)) {
                labelRes = CONTACT_ICON_LABELS[i];
                break;
            }
        }
        tvSelectedContactIcon.setText(getString(R.string.icon_selected, getString(labelRes)));
    }

    private void requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);
            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
            roleRequestLauncher.launch(intent);
        }
    }
}