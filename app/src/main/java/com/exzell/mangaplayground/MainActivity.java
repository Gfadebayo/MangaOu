package com.exzell.mangaplayground;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.core.content.pm.PermissionInfoCompat;
import androidx.core.view.LayoutInflaterCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.exzell.mangaplayground.download.DownloadManager;
import com.exzell.mangaplayground.notification.Notifications;
import com.exzell.mangaplayground.viewmodels.HomeViewModel;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    private AppBarLayout mBarLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBarLayout = findViewById(R.id.toolbar_layout);
        int elevation = (int) mBarLayout.getElevation();

        NavigationView navView = findViewById(R.id.nav_view);

        DrawerLayout drawer = findViewById(R.id.drawer);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration configBar = new AppBarConfiguration.Builder(R.id.nav_bookamrk)
                .setOpenableLayout(drawer).build();

        NavigationUI.setupWithNavController(toolbar, navController, configBar);
        setupNavViewWithNavController(drawer, navView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.frag_manga) {
                mBarLayout.setElevation(0f);
                mBarLayout.setBackground(null);
            } else {
                int color = getResources().getColor(R.color.colorPrimary, null);
                mBarLayout.setBackgroundColor(color);
                mBarLayout.setElevation(elevation);
            }
        });

        if (BuildConfig.DEBUG) {
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, Stream.of(Manifest.permission.WRITE_EXTERNAL_STORAGE).toArray(String[]::new), 0);
            }
        }

        Notifications.INSTANCE.createChannels(this);
    }

    /**
     * This method will replace {@link NavigationUI#setupWithNavController(NavigationView, NavController)}
     * as we intend to keep only 1 instance of each fragment but said method creates a new one each time
     */
    private void setupNavViewWithNavController(DrawerLayout drawer, NavigationView navView, NavController controller) {

        navView.setNavigationItemSelectedListener(item -> {
            boolean popBack = controller.popBackStack(item.getItemId(), false);
            if (!popBack) popBack = NavigationUI.onNavDestinationSelected(item, controller);

            if (popBack) drawer.close();

            return popBack;
        });

        controller.addOnDestinationChangedListener((controller1, destination, arguments) ->
                IntStream.range(0, navView.getMenu().size()).forEach(i -> {
                    MenuItem item = navView.getMenu().getItem(i);
                    item.setChecked(destination.getId() == item.getItemId());
                }));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (Arrays.asList(permissions).isEmpty()) return;
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

/*TODO: Planned Updates
    1) A Search bar in Home Fragment
     2) Actions to be added to the download and bookmark notifications*/
