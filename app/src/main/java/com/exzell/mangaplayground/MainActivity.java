package com.exzell.mangaplayground;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.exzell.mangaplayground.databinding.ActivityDrawerBinding;
import com.exzell.mangaplayground.fragment.MangaFragment;

import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private ActivityDrawerBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityDrawerBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        setSupportActionBar(mBinding.activity.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration configBar = new AppBarConfiguration.Builder(R.id.nav_bookamrk)
                .setOpenableLayout(mBinding.drawer).build();

        NavigationUI.setupWithNavController(mBinding.activity.toolbar, navController, configBar);
        setupNavViewWithNavController(mBinding.drawer, mBinding.navView, navController);

        setLayoutBehaviour(navController);

        if(getIntent().getAction().equals(Intent.ACTION_VIEW) && getIntent().hasCategory(Intent.CATEGORY_BROWSABLE)){
            String link = getIntent().getData().getPath();
            Timber.i("Path is %s", link);
            Bundle linkBundle = new Bundle(1);
            linkBundle.putString(MangaFragment.MANGA_LINK, link);

            navController.navigate(R.id.frag_manga, linkBundle);
        }

        if (BuildConfig.DEBUG) {
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, Stream.of(Manifest.permission.WRITE_EXTERNAL_STORAGE).toArray(String[]::new), 0);
            }
        }
    }

    /**
     * Set how we want the toolbar to be when we navigate to {@link com.exzell.mangaplayground.fragment.MangaFragment}
     */
    private void setLayoutBehaviour(NavController controller){

        controller.addOnDestinationChangedListener((control, dest, args) -> {
            mBinding.activity.toolbarLayout.setExpanded(true, false);

            if(dest.getId() == R.id.frag_manga){
                mBinding.activity.toolbar.setTitle(null);
                mBinding.activity.toolbarLayout.setBackground(null);
            }else{
                int color = getResources().getColor(R.color.primary, null);
                mBinding.activity.toolbarLayout.setBackgroundColor(color);
            }

            mBinding.activity.fab.setVisibility(dest.getId() == R.id.nav_search ? View.VISIBLE : View.GONE);
        });
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

    @Override
    public void onLowMemory() {
        Timber.d("Warning!! Low memory");
    }
}

/*TODO: Planned Updates
    1) A Search bar in Home Fragment
     2) Actions to be added to the download and bookmark notifications*/
