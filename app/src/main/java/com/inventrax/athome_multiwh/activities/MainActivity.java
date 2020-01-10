package com.inventrax.athome_multiwh.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.honeywell.aidc.BarcodeReader;
import com.inventrax.athome_multiwh.R;
import com.inventrax.athome_multiwh.application.AbstractApplication;
import com.inventrax.athome_multiwh.fragments.AboutFragment;

import com.inventrax.athome_multiwh.fragments.DrawerFragment;
import com.inventrax.athome_multiwh.fragments.HomeFragment;

import com.inventrax.athome_multiwh.logout.LogoutUtil;
import com.inventrax.athome_multiwh.model.NavDrawerItem;
import com.inventrax.athome_multiwh.util.AndroidUtils;
import com.inventrax.athome_multiwh.util.DialogUtils;
import com.inventrax.athome_multiwh.util.FragmentUtils;
import com.inventrax.athome_multiwh.util.ProgressDialogUtils;
import com.inventrax.athome_multiwh.util.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DrawerFragment.FragmentDrawerListener{

    private static final String classCode = "API_ACT_002";

       private Toolbar mToolbar;
       private DrawerFragment drawerFragment;
       private FragmentUtils fragmentUtils;
       private CharSequence[] userRouteCharSequences;
       private List<String> userRouteStringList;
       private String selectedRouteCode;
       private FragmentActivity fragmentActivity;
       private SharedPreferencesUtils sharedPreferencesUtils;
       private LogoutUtil logoutUtil;
       private static BarcodeReader barcodeReader;

      public static BarcodeReader getBarcodeObject() {
        return barcodeReader;
     }
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            try {




                setContentView(R.layout.activity_main);

                loadFormControls();


            } catch (Exception ex) {
               // Logger.Log(MainActivity.class.getName(), ex);
            }
        }

        public void loadFormControls() {
            try {

                logoutUtil = new LogoutUtil();
                 mToolbar = (Toolbar) findViewById(R.id.toolbar);

                fragmentUtils = new FragmentUtils();

                fragmentActivity = this;

                new ProgressDialogUtils(this);

                AbstractApplication.FRAGMENT_ACTIVITY = this;

                setSupportActionBar(mToolbar);

           /* if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setIcon(R.mipmap.ic_launcher);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }*/

                View logoView = AndroidUtils.getToolbarLogoIcon(mToolbar);

                if (logoView != null)
                    logoView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentUtils.replaceFragmentWithBackStack(fragmentActivity, R.id.container_body, new HomeFragment());
                        }
                    });

                drawerFragment = (DrawerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
                drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
                drawerFragment.setDrawerListener(this);

                sharedPreferencesUtils = new SharedPreferencesUtils("LoginActivity", getApplicationContext());

                userRouteStringList = new ArrayList<>();

                userRouteCharSequences = userRouteStringList.toArray(new CharSequence[userRouteStringList.size()]);
                logoutUtil.setActivity(this);
                logoutUtil.setFragmentActivity(fragmentActivity);
                logoutUtil.setSharedPreferencesUtils(sharedPreferencesUtils);
                // display the first navigation drawer view on app launch
                displayView(0, new NavDrawerItem(false, "Home"));

            } catch (Exception ex) {
                DialogUtils.showAlertDialog(this, "Error while loading form controls");
                return;
            }
        }



        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);

            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement

            switch (id) {
                case R.id.action_logout: {

                   logoutUtil.doLogout();

                }
                break;

             /*   case R.id.action_settings: {
                    FragmentUtils.replaceFragmentWithBackStack(this, R.id.container_body, new SettingsFragment());
                }*/

                case R.id.action_about: {

                   FragmentUtils.replaceFragmentWithBackStack(this, R.id.container_body, new AboutFragment());

                }
                break;

            }


            return super.onOptionsItemSelected(item);
        }


        @Override
        public void onDrawerItemSelected(View view, int position, NavDrawerItem menuItem) {

            displayView(position, menuItem);
        }



    private void displayView(int position, NavDrawerItem menuItem) {

        Fragment fragment = null;
        String title = getString(R.string.app_name);

        switch (menuItem.getTitle()) {
            case "Home": {
                fragment = new HomeFragment();
                title = "Home";
            }
            break;


            default:
                break;
        }

        if (fragment != null) {

            fragmentUtils.replaceFragmentWithBackStack(this, R.id.container_body, fragment);

            // set the toolbar title
            getSupportActionBar().setTitle(title);

        }
    }

        @Override
        protected void onPause() {

            super.onPause();

        }

        @Override
        protected void onResume() {
            super.onResume();

       /* try {

            EnterpriseDeviceManager enterpriseDeviceManager = (EnterpriseDeviceManager)getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);

            RestrictionPolicy restrictionPolicy = enterpriseDeviceManager.getRestrictionPolicy();

            restrictionPolicy.allowSettingsChanges(false);

        }catch (Exception ex){

        }*/
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
        }

        private void initiateBackgroundServices() {



        }

}
