package com.exzell.mangaplayground;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.exzell.mangaplayground.databinding.ActivityMainBinding;
import com.exzell.mangaplayground.fragment.MangaFragment;
import com.exzell.mangaplayground.utils.ContextExtKt;
import com.exzell.mangaplayground.utils.ViewExtKt;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private final List<Integer> mFabDest = Arrays.asList(R.id.nav_search, R.id.nav_downloads);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        setSupportActionBar(mBinding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration configBar = new AppBarConfiguration.Builder(R.id.nav_bookmark).build();

//        String[] exclude = {MangaFragment.class.getName(), EmptyFragment.class.getName()};
//        Fragment host = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
//        navController.getNavigatorProvider()
//                .addNavigator(new SingleNavigator(this, host.getChildFragmentManager(), R.id.nav_host_fragment, exclude));
        navController.setGraph(R.navigation.nav_graph);

        NavigationUI.setupWithNavController(mBinding.toolbar, navController, configBar);
        NavigationUI.setupWithNavController(mBinding.bottomNavView, navController);

        setLayoutBehaviour(navController);

        if (getIntent().getAction().equals(Intent.ACTION_VIEW) && getIntent().hasCategory(Intent.CATEGORY_BROWSABLE)) {
            String link = getIntent().getData().getPath();
            Timber.i("Path is %s", link);
            Bundle linkBundle = new Bundle(1);
            linkBundle.putString(MangaFragment.MANGA_LINK, link);

            navController.navigate(R.id.frag_manga, linkBundle);
        }
    }

    /**
     * Changes the attributes of the toolbar depending on the fragment
     */
    private void setLayoutBehaviour(NavController controller){

        controller.addOnDestinationChangedListener((control, dest, args) -> {
            mBinding.toolbarLayout.setExpanded(true, false);

            if (dest.getId() == R.id.frag_manga) {
                mBinding.toolbar.setTitle(null);
                mBinding.toolbarLayout.setBackgroundColor(Color.TRANSPARENT);
                ViewExtKt.toggleVisibility(mBinding.bottomNavView, false);
            } else {
                mBinding.toolbarLayout.setBackgroundColor(ContextExtKt.getPrimaryColor(this));
                ViewExtKt.toggleVisibility(mBinding.bottomNavView, true);
            }


            mBinding.fab.setVisibility(mFabDest.contains(dest.getId()) ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {

        if (item.getItemId() == R.id.action_settings) {
            Intent settingIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingIntent);
            return true;
        }
        return false;
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
