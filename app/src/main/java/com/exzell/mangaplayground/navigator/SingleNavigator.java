package com.exzell.mangaplayground.navigator;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigator;
import androidx.navigation.fragment.FragmentNavigator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Navigator.Name("persist_fragment")
public class SingleNavigator extends FragmentNavigator {

    //instances we want to keep
    private List<String> mFragmentInstances;

    //instances that have already been created
    private Map<String, Fragment> mCreatedFragments = new HashMap<>();

    public SingleNavigator(Context context, FragmentManager manager, int containerId, String... excludeInstances) {
        super(context, manager, containerId);

        mFragmentInstances = Arrays.asList(excludeInstances);

        manager.setFragmentFactory(new Factory(manager.getFragmentFactory()));
    }


    public class Factory extends FragmentFactory {

        FragmentFactory mDefaultFactory;

        public Factory(FragmentFactory mDefaultFactory) {
            this.mDefaultFactory = mDefaultFactory;
        }

        @NonNull
        @Override
        public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
            if (mCreatedFragments.containsKey(className)) return mCreatedFragments.get(className);

            Fragment newInst = mDefaultFactory.instantiate(classLoader, className);

            if (!mFragmentInstances.contains(className)) mCreatedFragments.put(className, newInst);

            return newInst;
        }
    }
}
