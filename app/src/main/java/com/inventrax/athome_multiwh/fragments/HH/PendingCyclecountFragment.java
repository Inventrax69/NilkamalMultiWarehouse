package com.inventrax.athome_multiwh.fragments.HH;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.inventrax.athome_multiwh.R;
import com.inventrax.athome_multiwh.adapters.CycleCountPendingListAdapter;
import com.inventrax.athome_multiwh.common.Common;
import com.inventrax.athome_multiwh.common.constants.EndpointConstants;
import com.inventrax.athome_multiwh.common.constants.ErrorMessages;
import com.inventrax.athome_multiwh.fragments.HU.CycleCountFragmentHU;
import com.inventrax.athome_multiwh.interfaces.ApiInterface;
import com.inventrax.athome_multiwh.pojos.CycleCountDTO;
import com.inventrax.athome_multiwh.pojos.WMSCoreMessage;
import com.inventrax.athome_multiwh.pojos.WMSExceptionMessage;
import com.inventrax.athome_multiwh.services.RestService;
import com.inventrax.athome_multiwh.util.DialogUtils;
import com.inventrax.athome_multiwh.util.ExceptionLoggerUtils;
import com.inventrax.athome_multiwh.util.FragmentUtils;
import com.inventrax.athome_multiwh.util.NetworkUtils;
import com.inventrax.athome_multiwh.util.ProgressDialogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingCyclecountFragment extends Fragment implements View.OnClickListener {

    private static final String classCode = "API_FRAG_018";
    private View rootView;

    private Button btnCancel;
    private RecyclerView rvCycleCount;
    private LinearLayoutManager linearLayoutManager;

    private Gson gson;
    private WMSCoreMessage core;
    private Common common;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;

    private String materialType = null;

    Boolean exportState, cleaBinState;

    private String loc = null, count = null, sku = null, desc = null, barcode = null, qty = null, ccQty = null, userId = null, batch = null, sloc = null, boxNo = null;

    private CycleCountDTO cycleCountDto = null;


    Bundle bundle;

    // bundle.putString("cleaBinState", "" + cleaBinState);
    //        bundle.putString("exportState", "" + exportState);


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (NetworkUtils.isInternetAvailable(getContext())) {
            rootView = inflater.inflate(R.layout.hh_pending_manualpacking, container, false);
            loadFormControls();
        } else {
            DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
            return rootView;
        }
        return rootView;
    }

    public void loadFormControls() {

        rvCycleCount = (RecyclerView) rootView.findViewById(R.id.rvEcomPendingPacking);
        rvCycleCount.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());

        // use a linear layout manager
        rvCycleCount.setLayoutManager(linearLayoutManager);

        btnCancel = (Button) rootView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        // Getting values from Shared preferences
        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");

        common = new Common();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();


        // Getting arguments from Bundle
        if (getArguments() != null) {

            if (materialType.equals("HH")) {

                loc = getArguments().getString("loc");
                count = getArguments().getString("count");
                barcode = getArguments().getString("barcode");
                sku = getArguments().getString("sku");
                desc = getArguments().getString("desc");
                qty = getArguments().getString("qty");
                ccQty = getArguments().getString("ccQty");
                exportState = getArguments().getBoolean("cleaBinState");
                cleaBinState = getArguments().getBoolean("exportState");
                cycleCountDto = (CycleCountDTO) getArguments().getSerializable("cycleCountDto");

            } else {

                loc = getArguments().getString("loc");
                count = getArguments().getString("count");
                barcode = getArguments().getString("barcode");
                sku = getArguments().getString("sku");
                desc = getArguments().getString("desc");
                qty = getArguments().getString("qty");
                batch = getArguments().getString("batch");
                sloc = getArguments().getString("sloc");
                ccQty = getArguments().getString("ccQty");
                boxNo = getArguments().getString("boxNo");
                exportState = getArguments().getBoolean("cleaBinState");
                cleaBinState = getArguments().getBoolean("exportState");

                cycleCountDto = (CycleCountDTO) getArguments().getSerializable("cycleCountDto");

            }
        }


        // To get Pending Outbound List
        loadPendingCycleCount();


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnCancel:
                goBack();
                break;
            default:
                break;

        }
    }

    public void goBack() {

        bundle = new Bundle();

        if (materialType.equals("HH")) {

            bundle.putString("loc", loc);
            bundle.putString("count", count);
            bundle.putString("barcode", barcode);
            bundle.putString("sku", sku);
            bundle.putString("desc", desc);
            bundle.putString("qty", qty);
            bundle.putString("ccQty", ccQty);
            bundle.putBoolean("exportState", exportState);
            bundle.putBoolean("cleaBinState", cleaBinState);
            bundle.putSerializable("cycleCountDto", cycleCountDto);

            CycleCountFragmentHH cycleCountFragmentHH = new CycleCountFragmentHH();
            cycleCountFragmentHH.setArguments(bundle);
            FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, cycleCountFragmentHH);
            return;
        } else {

            bundle.putString("loc", loc);
            bundle.putString("count", count);
            bundle.putString("barcode", barcode);
            bundle.putString("sku", sku);
            bundle.putString("desc", desc);
            bundle.putString("qty", qty);
            bundle.putString("batch", batch);
            bundle.putString("sloc", sloc);
            bundle.putString("ccQty", ccQty);
            bundle.putString("boxNo", boxNo);
            bundle.putBoolean("exportState", exportState);
            bundle.putBoolean("cleaBinState", cleaBinState);

            bundle.putSerializable("cycleCountDto", cycleCountDto);


            CycleCountFragmentHU cycleCountFragmentHU = new CycleCountFragmentHU();
            cycleCountFragmentHU.setArguments(bundle);
            FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, cycleCountFragmentHU);
            return;
        }

    }


    public void loadPendingCycleCount() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.CycleCount, getContext());
            CycleCountDTO cycleCountDTO = new CycleCountDTO();
            cycleCountDTO.setUserId(userId);
            cycleCountDTO.setLocation(loc);
            if (materialType.equals("HH")) {
                cycleCountDTO.setEANScanned(true);
                cycleCountDTO.setEANSpecified(true);
            } else {
                cycleCountDTO.setEANScanned(false);
                cycleCountDTO.setEANSpecified(false);
            }
            message.setEntityObject(cycleCountDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetCycleCountInformation(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetCycleCountInformation_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }

            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                }
                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lPickPendingItem = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lPickPendingItem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<CycleCountDTO> listPendingCyclecount = new ArrayList<CycleCountDTO>();

                                for (int i = 0; i < _lPickPendingItem.size(); i++) {
                                    CycleCountDTO cycleCountDTO = new CycleCountDTO(_lPickPendingItem.get(i).entrySet());
                                    listPendingCyclecount.add(cycleCountDTO);

                                }

                                ProgressDialogUtils.closeProgressDialog();

                                // Setting Values to the view
                                CycleCountPendingListAdapter cycleCountPendingListAdapter = new CycleCountPendingListAdapter(getContext(), listPendingCyclecount);
                                rvCycleCount.setAdapter(cycleCountPendingListAdapter);
                                ProgressDialogUtils.closeProgressDialog();

                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetCycleCountInformation_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }

                        ProgressDialogUtils.closeProgressDialog();
                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetCycleCountInformation_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetCycleCountInformation_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    // sending exception to the database
    public void logException() {
        try {

            String textFromFile = exceptionLoggerUtils.readFromFile(getActivity());

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Exception, getActivity());
            WMSExceptionMessage wmsExceptionMessage = new WMSExceptionMessage();
            wmsExceptionMessage.setWMSMessage(textFromFile);
            message.setEntityObject(wmsExceptionMessage);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.LogException(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                            // if any Exception throws
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    ProgressDialogUtils.closeProgressDialog();
                                    return;
                                }
                            } else {
                                ProgressDialogUtils.closeProgressDialog();
                                LinkedTreeMap<String, String> _lResultvalue = new LinkedTreeMap<String, String>();
                                _lResultvalue = (LinkedTreeMap<String, String>) core.getEntityObject();
                                for (Map.Entry<String, String> entry : _lResultvalue.entrySet()) {
                                    if (entry.getKey().equals("Result")) {
                                        String Result = entry.getValue();
                                        if (Result.equals("0")) {

                                            return;
                                        } else {
                                            exceptionLoggerUtils.deleteFile(getActivity());
                                            ProgressDialogUtils.closeProgressDialog();
                                            return;
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {

                            /*try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002",getContext());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            logException();*/

                            ProgressDialogUtils.closeProgressDialog();
                            Log.d("Message", core.getEntityObject().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {

                // Toast.makeText(LoginActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_pending_cyclecount));
    }
}