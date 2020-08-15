package com.example.psychapp.ui.main;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.psychapp.R;
import com.example.psychapp.ui.Settings_Account_Fragment;
import com.example.psychapp.ui.Settings_Application_Fragment;
import com.example.psychapp.ui.Settings_Notification_Fragment;

import java.util.InputMismatchException;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SettingsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_application, R.string.tab_notification, R.string.tab_account};
    private final Context mContext;

    public SettingsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new Settings_Application_Fragment();
            case 1:
                return new Settings_Notification_Fragment();
            case 2:
                return new Settings_Account_Fragment();
            default:
                throw new InputMismatchException();
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 3;
    }
}