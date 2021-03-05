package com.exzell.mangaplayground;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.exzell.mangaplayground.databinding.ActivityDrawerBinding;
import com.exzell.mangaplayground.fragment.EmptyFragment;
import com.exzell.mangaplayground.fragment.MangaFragment;
import com.exzell.mangaplayground.navigator.SingleNavigator;

import java.util.Arrays;

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

        Fragment host = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController.getNavigatorProvider()
                .addNavigator(new SingleNavigator(this, host.getChildFragmentManager(), R.id.nav_host_fragment, MangaFragment.class.getName(), EmptyFragment.class.getName()));
        navController.setGraph(R.navigation.nav_graph);

        NavigationUI.setupWithNavController(mBinding.activity.toolbar, navController, configBar);
        NavigationUI.setupWithNavController(mBinding.navView, navController);

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (Arrays.asList(permissions).isEmpty()) return;
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

/*TODO: Planned Updates
    1) A Search bar in Home Fragment
     2) Actions to be added to the download and bookmark notifications*/
