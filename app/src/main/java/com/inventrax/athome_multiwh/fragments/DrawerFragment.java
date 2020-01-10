package com.inventrax.athome_multiwh.fragments;


import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.inventrax.athome_multiwh.R;
import com.inventrax.athome_multiwh.adapters.ExpandableListAdapter;
import com.inventrax.athome_multiwh.fragments.HH.BoxLoadingFragmentHH;
import com.inventrax.athome_multiwh.fragments.HH.CycleCountFragmentHH;
import com.inventrax.athome_multiwh.fragments.HU.ECOMLoadingFragment;
import com.inventrax.athome_multiwh.fragments.HU.EcomBulkPackingFragment;
import com.inventrax.athome_multiwh.fragments.HH.ECOM.EcomPackingFragment;
import com.inventrax.athome_multiwh.fragments.HH.ECOM.EcomPickFragment;
import com.inventrax.athome_multiwh.fragments.HH.ECOM.EcomPickOnDemandFragment;
import com.inventrax.athome_multiwh.fragments.HH.HHBinReplnishment;
import com.inventrax.athome_multiwh.fragments.HH.ManualPacking;
import com.inventrax.athome_multiwh.fragments.HH.OBDPickingFragmentHH;
import com.inventrax.athome_multiwh.fragments.HH.PickOnDemandHHFragment;
import com.inventrax.athome_multiwh.fragments.HH.SLocToSLocFragmentHH;
import com.inventrax.athome_multiwh.fragments.HH.SkuToSkuFragmentHH;
import com.inventrax.athome_multiwh.fragments.HH.SorterPickFragmentHH;
import com.inventrax.athome_multiwh.fragments.HU.CaseNoMapping;
import com.inventrax.athome_multiwh.fragments.HU.CycleCountFragmentHU;
import com.inventrax.athome_multiwh.fragments.HU.EcomMarketPlacePackingFragment;
import com.inventrax.athome_multiwh.fragments.HU.MapPalletDockLoc;
import com.inventrax.athome_multiwh.fragments.HU.MattressesPrintFragmentHU;
import com.inventrax.athome_multiwh.fragments.HU.PickFragmentHU;
import com.inventrax.athome_multiwh.fragments.HU.PickOnDemandFragmentHU;
import com.inventrax.athome_multiwh.fragments.HU.PutawayFragmentHU;
import com.inventrax.athome_multiwh.fragments.HU.ReceiveFromSiteFragmentHU;
import com.inventrax.athome_multiwh.fragments.HU.SLocToSLocFragment;
import com.inventrax.athome_multiwh.fragments.HU.SkuToSkuFragment;
import com.inventrax.athome_multiwh.fragments.HU.VLPDLoadingFragment;
import com.inventrax.athome_multiwh.fragments.StockTake.StockTakeFragment;
import com.inventrax.athome_multiwh.model.MenuModel;
import com.inventrax.athome_multiwh.model.NavDrawerItem;
import com.inventrax.athome_multiwh.util.DialogUtils;
import com.inventrax.athome_multiwh.util.FragmentUtils;
import com.inventrax.athome_multiwh.util.ProgressDialogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DrawerFragment extends Fragment implements View.OnClickListener {

    private static final String classCode = "API_FRAG_NAVIGATION DRAWER";

    private static String TAG = DrawerFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private Context mContext;
    private ActionBarDrawerToggle mDrawerToggle;
    public DrawerLayout mDrawerLayout;
    private View containerView;
    private FragmentDrawerListener drawerListener;
    private View layout;
    private TextView txtLoginUser, tvCycleCount;

    public DrawerLayout drawerLayout;

    private AppCompatActivity appCompatActivity;

    private IntentFilter mIntentFilter;

    private String userName;
    private String division, menuLink;
    SharedPreferences sp;


    ExpandableListAdapter expandableListAdapter;
    ExpandableListView expandableListView;
    List<MenuModel> headerList = new ArrayList<>();
    HashMap<MenuModel, List<MenuModel>> childList = new HashMap<>();

    public void setDrawerListener(FragmentDrawerListener listener) {
        this.drawerListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflating view layout
        layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        appCompatActivity = (AppCompatActivity) getActivity();

        loadFormControls();


        return layout;
    }

    public void loadFormControls() {

        try {


            sp = getContext().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
            userName = sp.getString("UserName", "");   // Getting User name and division from Login
            division = sp.getString("division", "");

            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction("com.example.broadcast.counter");

            new ProgressDialogUtils(getContext());

            txtLoginUser = (TextView) layout.findViewById(R.id.txtLoginUser);
            txtLoginUser.setText(userName);
            mContext = getContext();

            expandableListView = (ExpandableListView) layout.findViewById(R.id.expandableListView);

            if (division.equals("HU")) {
                prepareMenuDataHU();         //HU menu items
            } else if (division.equals("HH")) {
                prepareMenuDataHH();         //HH menu items
            } else if (division.equals("Stock take")) {
                preparemenuStocktake();
            }

            // To add menu items
            populateExpandableList();


        } catch (Exception ex) {
            //Logger.Log(DrawerFragment.class.getName(), ex);
            DialogUtils.showAlertDialog(getActivity(), "Error while loading menu list");
            return;
        }
    }

    private void preparemenuStocktake() {
        MenuModel menuModel = new MenuModel("Stock take", true, false, "Stock take");
        headerList.add(menuModel);
    }


    // HU menu
    private void prepareMenuDataHU() {

        // if child is not there for a header, then add "null" in place of childModeList

        MenuModel menuModel = new MenuModel("Receipts", true, true, "Receipts");
        headerList.add(menuModel);

        List<MenuModel> childModelsList = new ArrayList<>();

        MenuModel childModel = new MenuModel("Goods-In", false, false, "Goods-In");
        childModelsList.add(childModel);

        /*childModel = new MenuModel("Receive from Site", false, false, "Receive from Site");
        childModelsList.add(childModel);*/

        childModel = new MenuModel("Putaway", false, false, "Putaway");
        childModelsList.add(childModel);

        childModel = new MenuModel("Stock Check", false, false, "Stock Check");
        childModelsList.add(childModel);

       /* childModel = new MenuModel("Case No. Mapping", false, false,"Case No. Mapping");
        childModelsList.add(childModel);*/

        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }


        menuModel = new MenuModel("Dispatches", true, true, "Dispatches");
        headerList.add(menuModel);

        childModelsList = new ArrayList<>();

        childModel = new MenuModel("Pick", false, false, "Pick");
        childModelsList.add(childModel);

        childModel = new MenuModel("Pick on Demand", false, false, "Pick on Demand");
        childModelsList.add(childModel);

        childModel = new MenuModel("Confirm Pallet", false, false, "Confirm Pallet");
        childModelsList.add(childModel);

        childModel = new MenuModel("Mattress Bundle", false, false, "Mattress Bundle");
        childModelsList.add(childModel);

        childModel = new MenuModel("VLPD Loading", false, false, "VLPD Loading");
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }


        /*menuModel = new MenuModel("ECOM Packing", true, true, "ECOM");
        headerList.add(menuModel);

        childModelsList = new ArrayList<>();

        childModel = new MenuModel("Bulk Order Packing", false, false, "Bulk Order Packing");
        childModelsList.add(childModel);

        childModel = new MenuModel("Market Place", false, false, "Market Place");
        childModelsList.add(childModel);

        childModel = new MenuModel("ECOM Loading", false, false, "ECOM Loading");
        childModelsList.add(childModel);

        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }*/

        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("Transfers", true, true, "Transfers");
        headerList.add(menuModel);

        childModel = new MenuModel("Bin to Bin", false, false, "Bin to Bin");
        childModelsList.add(childModel);

        childModel = new MenuModel("SKU to SKU", false, false, "SKU to SKU");
        childModelsList.add(childModel);

        childModel = new MenuModel("Sloc to Sloc", false, false, "Loc to Loc");
        childModelsList.add(childModel);

        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }

        menuModel = new MenuModel("Cycle Count", true, false, "Cycle Count");
        headerList.add(menuModel);

        if (!menuModel.hasChildren) {
            childList.put(menuModel, null);
        }
    }


    // HH menu
    private void prepareMenuDataHH() {

        // if child is not there for a header, then add "null" in place of childModeList

        MenuModel menuModel = new MenuModel("Receipts", true, true, "Receipts");
        headerList.add(menuModel);

        List<MenuModel> childModelsList = new ArrayList<>();

        MenuModel childModel = new MenuModel("Goods-In", false, false, "Goods-In");
        childModelsList.add(childModel);

        childModel = new MenuModel("Putaway", false, false, "Putaway");
        childModelsList.add(childModel);

        childModel = new MenuModel("Stock Check", false, false, "Stock Check");
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }


        menuModel = new MenuModel("ECOM Dispatches", true, true, "ECOM Dispatches");
        headerList.add(menuModel);

        childModelsList = new ArrayList<>();

        childModel = new MenuModel("ECOM Pick", false, false, "ECOM Pick");
        childModelsList.add(childModel);

        childModel = new MenuModel("ECOM Pick on Demand", false, false, "ECOM Pick on Demand");
        childModelsList.add(childModel);



      /*  childModel = new MenuModel("ECOM Packing", false, false,"ECOM Packing");
        childModelsList.add(childModel);*/

        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }


        menuModel = new MenuModel("Dispatches", true, true, "Dispatches");
        headerList.add(menuModel);

        childModelsList = new ArrayList<>();

        childModel = new MenuModel("Sorter Pick", false, false, "Sorter Pick");
        childModelsList.add(childModel);

        childModel = new MenuModel("OBD Pick", false, false, "OBD Pick");

        childModelsList.add(childModel);

        childModel = new MenuModel("Manual Packing", false, false, "Manual Packing");
        childModelsList.add(childModel);

        childModel = new MenuModel("Pick on Demand", false, false, "Pick on Demand");
        childModelsList.add(childModel);

        childModel = new MenuModel("Vehicle Loading", false, false, "Vehicle Loading");
        childModelsList.add(childModel);

        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }

        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("Transfers", true, true, "Transfers");
        headerList.add(menuModel);

        childModel = new MenuModel("Bin to Bin", false, false, "Bin to Bin");
        childModelsList.add(childModel);

        childModel = new MenuModel("SKU to SKU", false, false, "SKU to SKU");
        childModelsList.add(childModel);

        childModel = new MenuModel("Sloc to Sloc", false, false, "Loc to Loc");
        childModelsList.add(childModel);
        childModel = new MenuModel("Bin Replenishment", false, false, "Bin Replenishment");
        childModelsList.add(childModel);
        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }

        menuModel = new MenuModel("Cycle Count", true, false, "Cycle Count");
        headerList.add(menuModel);

        if (!menuModel.hasChildren) {
            childList.put(menuModel, null);
        }
    }

    private void populateExpandableList() {

        expandableListAdapter = new ExpandableListAdapter(getContext(), headerList, childList);
        expandableListView.setAdapter(expandableListAdapter);       // adding menu items to the view

        // Header Click event
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (headerList.get(groupPosition).isGroup) {
                    if (!headerList.get(groupPosition).hasChildren) {
                        menuLink = headerList.get(groupPosition).getMenuItemName();         // getting Header item
                        openFragment();                                                    // managing navigation menu clicks
                        onBackPressed();                                                  // closing navigaiton
                    }
                }

                return false;
            }
        });

        // Child Click event
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (childList.get(headerList.get(groupPosition)) != null) {
                    MenuModel model = childList.get(headerList.get(groupPosition)).get(childPosition);
                    if (model.menuItemName.length() > 0) {
                        menuLink = model.getMenuItemName();                                 // getting Header item
                        openFragment();                                                    // managing navigation menu clicks
                        onBackPressed();                                                    // closing navigaiton
                    }
                }

                return false;
            }
        });
    }

    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }


    public void openFragment() {

        if (division.equalsIgnoreCase("Stock take")) {
            switch (menuLink) {
                case "Stock take":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new StockTakeFragment());
                    break;
            }
        } else if (division.equals("HU")) {                            // HU menu item navigation clicks


            switch (menuLink) {

                case "Goods-In":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new UnloadingFragment());
                    break;

                case "Receive from Site":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new ReceiveFromSiteFragmentHU());
                    break;

                case "Putaway":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PutawayFragmentHU());
                    break;

                case "Stock Check":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new RsnTrackFragment());
                    break;

                case "Pick":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PickFragmentHU());
                    break;

                case "Pick on Demand":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PickOnDemandFragmentHU());
                    break;

                case "Confirm Pallet":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new MapPalletDockLoc());
                    break;

                case "VLPD Loading":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new VLPDLoadingFragment());
                    break;

                case "Bin to Bin":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new BintoBinFragment());
                    break;

                case "SKU to SKU":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new SkuToSkuFragment());
                    break;

                case "Loc to Loc":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new SLocToSLocFragment());
                    break;

                case "Cycle Count":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new CycleCountFragmentHU());
                    break;

                case "Case No. Mapping":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new CaseNoMapping());
                    break;

                case "Mattress Bundle":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new MattressesPrintFragmentHU());
                    break;

                case "Bulk Order Packing":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new EcomBulkPackingFragment());
                    break;

                case "Market Place":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new EcomMarketPlacePackingFragment());
                    break;

                case "ECOM Loading":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new ECOMLoadingFragment());
                    break;


            }
        } else {
            switch (menuLink) {                                   // HH menu item navigation clicks

                case "Goods-In":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new UnloadingFragment());
                    break;

                case "Putaway":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PutawayFragmentHU());
                    break;

                case "Stock Check":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new RsnTrackFragment());
                    break;

                case "ECOM Pick":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new EcomPickFragment());
                    break;

                case "ECOM Pick on Demand":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new EcomPickOnDemandFragment());
                    break;

                case "ECOM Packing":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new EcomPackingFragment());
                    break;

                case "Sorter Pick":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new SorterPickFragmentHH());
                    break;

                case "OBD Pick":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new OBDPickingFragmentHH());
                    break;

                case "Manual Packing":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new ManualPacking());
                    break;

                case "Pick on Demand":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PickOnDemandHHFragment());
                    break;

                case "Vehicle Loading":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new BoxLoadingFragmentHH());
                    break;

                case "Bin to Bin":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new BintoBinFragment());
                    break;

                case "SKU to SKU":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new SkuToSkuFragmentHH());
                    break;

                case "Loc to Loc":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new SLocToSLocFragmentHH());
                    break;

                case "Bin Replenishment":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HHBinReplnishment());
                    break;

                case "Cycle Count":
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new CycleCountFragmentHH());
                    break;


            }
        }

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*try
        {
            if ( user != null ) {

                appCompatActivity.getSupportActionBar().setTitle(StringUtils.toCamelCase(user.getFirstName()));
                appCompatActivity.getSupportActionBar().setSubtitle(user.getUserType().toUpperCase() + "  " + new SimpleDateFormat("dd-MM-yyyy").format(new Date()) );

            }

        }catch (Exception ex){
            Logger.Log(DrawerFragment.class.getName(),ex);
            DialogUtils.showAlertDialog(getActivity(), "Error while loading menu list");
            return;
        }*/

    }


    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {

        try {
            containerView = getActivity().findViewById(fragmentId);
            mDrawerLayout = drawerLayout;
            mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    getActivity().invalidateOptionsMenu();
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    getActivity().invalidateOptionsMenu();
                }

                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    super.onDrawerSlide(drawerView, slideOffset);
                    toolbar.setAlpha(1 - slideOffset / 2);
                }
            };


            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerLayout.post(new Runnable() {
                @Override
                public void run() {
                    mDrawerToggle.syncState();
                }
            });
        } catch (Exception ex) {
            // Logger.Log(DrawerFragment.class.getName(),ex);
            return;
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }


    public interface FragmentDrawerListener {
        void onDrawerItemSelected(View view, int position, NavDrawerItem menuItem);
    }

}