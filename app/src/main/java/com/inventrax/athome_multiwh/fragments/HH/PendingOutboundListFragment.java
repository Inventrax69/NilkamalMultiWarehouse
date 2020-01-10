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
import com.inventrax.athome_multiwh.adapters.PendingOutboundListAdapter;
import com.inventrax.athome_multiwh.common.Common;
import com.inventrax.athome_multiwh.common.constants.EndpointConstants;
import com.inventrax.athome_multiwh.common.constants.ErrorMessages;
import com.inventrax.athome_multiwh.fragments.HU.RSNGoodsFragmentHU;
import com.inventrax.athome_multiwh.fragments.HomeFragment;
import com.inventrax.athome_multiwh.interfaces.ApiInterface;
import com.inventrax.athome_multiwh.pojos.OutboundDTO;
import com.inventrax.athome_multiwh.pojos.PickPendingItemDTO;
import com.inventrax.athome_multiwh.pojos.VLPDRequestDTO;
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

public class PendingOutboundListFragment extends Fragment implements View.OnClickListener {

    private static final String classCode = "API_FRAG_PENDING OUTBOUND ";
    private View rootView;

    private Button btnCancel;
    private RecyclerView rvOutboundPendingList;
    private LinearLayoutManager linearLayoutManager;

    private Gson gson;
    private WMSCoreMessage core;
    private Common common;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;

    private String materialType = null;
    private String clientId= null,inboundId= null;
    List<OutboundDTO> lstOutbound = null;
    private String userId = null;
    private String storeRefNo = null;

    private String pallet=null,location=null,rsn = null,L = null,B= null,H= null,
            W= null,box = null,qty= null,vol=null,twt=null,caseString=null, sku=null,desc=null,ean=null,count=null;
    Bundle bundle;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (NetworkUtils.isInternetAvailable(getContext())) {
            rootView = inflater.inflate(R.layout.fragment_pending_obdpicking, container, false);
            loadFormControls();
        } else {
            DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
            return rootView;
        }
        return rootView;
    }

    public void loadFormControls() {

        rvOutboundPendingList = (RecyclerView) rootView.findViewById(R.id.rvInboundPendingList);
        rvOutboundPendingList.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());

        // use a linear layout manager
        rvOutboundPendingList.setLayoutManager(linearLayoutManager);

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
        if(getArguments()!= null) {

            if(materialType.equals("HU")){


               /* storeRefNo = getArguments().getString("StoreRefNo");
                clientId = getArguments().getString("ClientId");
                pallet = getArguments().getString("pallet");
                location = getArguments().getString("location");
                rsn = getArguments().getString("rsn");
                L = getArguments().getString("L");
                B = getArguments().getString("B");
                H = getArguments().getString("H");
                vol = getArguments().getString("V");
                W = getArguments().getString("W");
                box = getArguments().getString("box");
                qty = getArguments().getString("qty");
                twt = getArguments().getString("twt");
                caseString = getArguments().getString("caseString");
                sku = getArguments().getString("sku");
                desc = getArguments().getString("desc");
                count = getArguments().getString("count");*/

            }else if(materialType.equals("HH")){

                /*storeRefNo = getArguments().getString("StoreRefNo");
                clientId = getArguments().getString("ClientId");
                inboundId = getArguments().getString("InboundId");
                pallet = getArguments().getString("pallet");
                location = getArguments().getString("location");
                ean = getArguments().getString("ean");
                L = getArguments().getString("L");
                B = getArguments().getString("B");
                H = getArguments().getString("H");
                vol = getArguments().getString("V");
                W = getArguments().getString("W");
                qty = getArguments().getString("qty");
                twt = getArguments().getString("twt");
                caseString = getArguments().getString("caseString");
                sku = getArguments().getString("sku");
                desc = getArguments().getString("desc");
                count = getArguments().getString("count");*/
            }
        }


        // To get Pending Outbound List
        loadPendingOutboundListDetails();


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnCancel:
                FragmentUtils.replaceFragment(getActivity(),R.id.container_body,new HomeFragment());
                //goBack();
                break;
            default:
                break;

        }
    }

    public void goBack(){

        // Binding Values to the Bundle
        bundle = new Bundle();
        bundle.putString("StoreRefNo",storeRefNo);
        bundle.putString("ClientId",clientId);


        if (materialType=="HU") {

            /*bundle.putString("pallet",pallet);
            bundle.putString("location",location);
            bundle.putString("rsn",rsn);
            bundle.putString("L",L);
            bundle.putString("B",B);
            bundle.putString("H",H);
            bundle.putString("V",vol);
            bundle.putString("W",W);
            bundle.putString("box",box);
            bundle.putString("qty",qty);
            bundle.putString("twt",twt);
            bundle.putString("caseString",caseString);
            bundle.putString("sku",sku);
            bundle.putString("desc",desc);
            bundle.putString("count",count);*/

            RSNGoodsFragmentHU rsnGoodsFragmentHU = new RSNGoodsFragmentHU();
            rsnGoodsFragmentHU.setArguments(bundle);
            FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, rsnGoodsFragmentHU);
            return;
        }else {

            /*bundle.putString("pallet",pallet);
            bundle.putString("location",location);
            bundle.putString("ean",ean);
            bundle.putString("L",L);
            bundle.putString("B",B);
            bundle.putString("H",H);
            bundle.putString("V",vol);
            bundle.putString("W",W);
            bundle.putString("box",box);
            bundle.putString("qty",qty);
            bundle.putString("twt",twt);
            bundle.putString("caseString",caseString);
            bundle.putString("sku",sku);
            bundle.putString("desc",desc);
            bundle.putString("count",count);
            bundle.putString("InboundId",inboundId);*/

            GoodsInFragmentHH goodsInFragmentHH = new GoodsInFragmentHH();
            goodsInFragmentHH.setArguments(bundle);
            FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, goodsInFragmentHH);
        }
    }


    public  void loadPendingOutboundListDetails()
    {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.VLPDRequestDTO,getContext());
            VLPDRequestDTO vlpdRequestDTO = new VLPDRequestDTO();
            vlpdRequestDTO.setUserID(userId);
            vlpdRequestDTO.setVlpdID("0");
            message.setEntityObject(vlpdRequestDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetPendingPickItemsFromByDispatchID(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"GetPendingPickItemsFromByDispatchID_01",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002,getActivity(),getContext(),"Error");

            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if (core.getType()!=null) {
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                    }
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                            }else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lPickPendingItem = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lPickPendingItem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<PickPendingItemDTO> listPickPendingItem = new ArrayList<PickPendingItemDTO>();

                                for (int i = 0; i < _lPickPendingItem.size(); i++) {
                                    PickPendingItemDTO oPickPendingItem = new PickPendingItemDTO(_lPickPendingItem.get(i).entrySet());
                                    listPickPendingItem.add(oPickPendingItem);

                                }
                                // Setting Values to the view
                                PendingOutboundListAdapter pendingOutboundListAdapter = new PendingOutboundListAdapter(getActivity(),listPickPendingItem);
                                rvOutboundPendingList.setAdapter(pendingOutboundListAdapter);
                                ProgressDialogUtils.closeProgressDialog();

                            }
                            }else
                            {
                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch(Exception ex){
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"GetPendingPickItemsFromByDispatchID_02",getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }


                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"GetPendingPickItemsFromByDispatchID_03",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
            }
        }catch (Exception ex)
        {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"GetPendingPickItemsFromByDispatchID_04",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
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
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

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
                common.showUserDefinedAlertType(errorMessages.EMC_0002,getActivity(),getContext(),"Error");
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
                                    common.showAlertType(owmsExceptionMessage, getActivity(),getContext());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
                    }
                });
            } catch (Exception ex) {

                // Toast.makeText(LoginActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_pending_outboundist));
    }
}