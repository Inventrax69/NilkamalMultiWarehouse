package com.inventrax.athome_multiwh.fragments.HU;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cipherlab.barcode.GeneralString;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;
import com.inventrax.athome_multiwh.R;
import com.inventrax.athome_multiwh.activities.MainActivity;
import com.inventrax.athome_multiwh.common.Common;
import com.inventrax.athome_multiwh.common.constants.EndpointConstants;
import com.inventrax.athome_multiwh.common.constants.ErrorMessages;
import com.inventrax.athome_multiwh.fragments.HomeFragment;
import com.inventrax.athome_multiwh.interfaces.ApiInterface;
import com.inventrax.athome_multiwh.pojos.ExecutionResponseDTO;
import com.inventrax.athome_multiwh.pojos.ItemInfoDTO;
import com.inventrax.athome_multiwh.pojos.OutboundDTO;
import com.inventrax.athome_multiwh.pojos.VLPDLoadingDTO;
import com.inventrax.athome_multiwh.pojos.VlpdDto;
import com.inventrax.athome_multiwh.pojos.WMSCoreMessage;
import com.inventrax.athome_multiwh.pojos.WMSExceptionMessage;
import com.inventrax.athome_multiwh.searchableSpinner.SearchableSpinner;
import com.inventrax.athome_multiwh.services.RestService;
import com.inventrax.athome_multiwh.util.DialogUtils;
import com.inventrax.athome_multiwh.util.ExceptionLoggerUtils;
import com.inventrax.athome_multiwh.util.FragmentUtils;
import com.inventrax.athome_multiwh.util.ProgressDialogUtils;
import com.inventrax.athome_multiwh.util.ScanValidator;
import com.inventrax.athome_multiwh.util.SoundUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VLPDLoadingFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_VLPD LOADING";
    private View rootView;

    private RelativeLayout rlVLPDSelect, rlVLPDLoading;
    private TextView lblVLPDNumber, lblScannedItem, lblBoxCount,lblPickQty,lblLoadQty,lblDesc;
    private CardView cvScan;
    private ImageView ivScan;
    private TextInputLayout txtInputLayoutQty;
    private EditText etQty;
    private SearchableSpinner spinnerSelectVLPDNo;
    private Button btnSubmit, btnClear, btnExport, btnGo, btnCloseOne, btnCloseTwo;

    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    private String userId = null, vlpdRefNo = null;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    SoundUtils sound;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private String materialType = "", rsnType  = "";
    private ErrorMessages errorMessages;
    int mmId=0;
    String vlpdId;

    SharedPreferences sp;
    String barcode="";

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public VLPDLoadingFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.hu_vlpd_loading, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        rlVLPDSelect = (RelativeLayout) rootView.findViewById(R.id.rlVLPDSelect);
        rlVLPDLoading = (RelativeLayout) rootView.findViewById(R.id.rlVLPDLoading);

        sp = getContext().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);

       // rsnType = sp.getString("rsnType", "");

        lblVLPDNumber = (TextView) rootView.findViewById(R.id.lblVLPDNumber);
        lblScannedItem = (TextView) rootView.findViewById(R.id.lblScannedItem);
        lblBoxCount = (TextView) rootView.findViewById(R.id.lblBoxCount);
        lblPickQty = (TextView) rootView.findViewById(R.id.lblPickQty);
        lblLoadQty = (TextView) rootView.findViewById(R.id.lblLoadQty);
        lblDesc = (TextView) rootView.findViewById(R.id.lblDesc);

        cvScan = (CardView) rootView.findViewById(R.id.cvScan);
        ivScan = (ImageView) rootView.findViewById(R.id.ivScan);

        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);
        etQty = (EditText) rootView.findViewById(R.id.etQty);

        spinnerSelectVLPDNo = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectVLPDNo);
        spinnerSelectVLPDNo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                vlpdRefNo= spinnerSelectVLPDNo.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);
        btnClear = (Button) rootView.findViewById(R.id.btnClear);
        btnGo = (Button) rootView.findViewById(R.id.btnGo);
        btnCloseTwo = (Button) rootView.findViewById(R.id.btnCloseTwo);
        btnExport = (Button) rootView.findViewById(R.id.btnExport);
        btnSubmit = (Button) rootView.findViewById(R.id.btnSubmit);

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");

        btnCloseOne.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnGo.setOnClickListener(this);
        btnCloseTwo.setOnClickListener(this);
        btnExport.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);



        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

        //For Honey well
        AidcManager.create(getActivity(), new AidcManager.CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
                try {
                    barcodeReader.claim();
                    HoneyWellBarcodeListeners();
                } catch (ScannerUnavailableException e) {
                    e.printStackTrace();
                }
            }
        });

        if (getArguments()!=null){

            rlVLPDSelect.setVisibility(View.GONE);
            rlVLPDLoading.setVisibility(View.VISIBLE);

            lblVLPDNumber.setText(getArguments().getString("vlpdRefNo"));
            vlpdRefNo = lblVLPDNumber.getText().toString();
            lblScannedItem.setText(getArguments().getString("RSN"));
            lblBoxCount.setText(getArguments().getString("count"));
            etQty.setText(getArguments().getString("qty"));
            return;

        }else {
            GetOpenRefNumberList();
        }


        btnSubmit.setEnabled(false);
        btnSubmit.setTextColor(getResources().getColor(R.color.black));
        btnSubmit.setBackgroundResource(R.drawable.button_hide);

    }

    private void GetOpenRefNumberList() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setiD(userId);
            vlpdDto.setType("2");
            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                call = apiService.GetOpenRefNumberList(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenRefNumberList_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lstvlpd = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstvlpd = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                List<String> lstvlpdnumbers= new ArrayList<String>();
                                VlpdDto dto = null;
                                for (int i = 0; i < _lstvlpd.size(); i++) {
                                    dto = new VlpdDto(_lstvlpd.get(i).entrySet());
                                    lstvlpdnumbers.add(dto.getvLPDNumber());
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                ArrayAdapter arrayAdapterStoreRefNo = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstvlpdnumbers);
                                spinnerSelectVLPDNo.setAdapter(arrayAdapterStoreRefNo);

                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenRefNumberList_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenRefNumberList_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenRefNumberList_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Alert");
        }

    }

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnCloseOne:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnCloseTwo:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnClear:
                ClearFields();;
                break;
            case R.id.btnGo:

                if ((vlpdRefNo=="" && vlpdRefNo==null) || vlpdRefNo.equals("Select")) {
                    common.showUserDefinedAlertType(errorMessages.EMC_0042, getActivity(), getContext(), "Error");
                    return;
                } else {
                    GetVLPDStatus();
                }

                break;

            case R.id.btnExport:
                if ((vlpdRefNo=="" && vlpdRefNo==null) || vlpdRefNo.equals("Select")) {
                    common.showUserDefinedAlertType(errorMessages.EMC_0042, getActivity(), getContext(), "Error");
                    return;
                } else {
                    Bundle bundle= new Bundle();
                    bundle.putString("vlpdRefNo",vlpdRefNo);
                    bundle.putString("RSN",lblScannedItem.getText().toString());
                    bundle.putString("count",lblBoxCount.getText().toString());
                    bundle.putString("qty",etQty.getText().toString());
                    PendingLoadingListFragment vlpdLoadingFragment = new PendingLoadingListFragment();
                    vlpdLoadingFragment.setArguments(bundle);
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, vlpdLoadingFragment);
                }


                break;
            case R.id.btnSubmit:
                rlVLPDSelect.setVisibility(View.GONE);
                rlVLPDLoading.setVisibility(View.VISIBLE);

                if(!lblScannedItem.getText().toString().isEmpty()){
                    if (barcode.split("/").length == 4 && barcode.split("/")[0].length() == 10) {
                        ConfirmVLPDLoading();
                    }
                    else if (ScanValidator.IsRSNScanned(barcode))
                    {
                        ConfirmVLPDLoading();
                    }
                    else if (ScanValidator.IsMatressBundleScanned(barcode)) {
                        ConfirmVLPDLoading();
                    }
                    else {
                        if(lblScannedItem.getText().toString().isEmpty()){
                            common.showUserDefinedAlertType("Please scan barcode", getActivity(), getActivity(), "Warning");
                            return;
                        }

                        if(Integer.parseInt(etQty.getText().toString())<=0){
                            common.showUserDefinedAlertType("Qty must be greater than zero", getActivity(), getActivity(), "Warning");
                            return;
                        }

                        if((Double.parseDouble(lblPickQty.getText().toString()) - Double.parseDouble(lblLoadQty.getText().toString()))
                                < Double.parseDouble(etQty.getText().toString())){
                            common.showUserDefinedAlertType("Entered qty is greater than required qty", getActivity(), getActivity(), "Warning");
                            return;
                        }

                        confirmNonRSNVLPDLoading();
                    }

                }else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0046,getActivity(),getContext(),"Error");
                }


                break;

            default:
                break;
        }
    }


    private void confirmNonRSNVLPDLoading() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.ItemInfoDTO, getContext());
            ItemInfoDTO infoDTO = new ItemInfoDTO();
            infoDTO.setMaterialMasterId(String.valueOf(mmId));
            infoDTO.setVlpdAssgnmentId(vlpdId);
            infoDTO.setReqQuantity(etQty.getText().toString());
            message.setEntityObject(infoDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                call = apiService.ConfirmNonRSNVLPDLoading(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmVLPDLoading_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lResponse = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lResponse = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                VLPDLoadingDTO dto = null;
                                for (int i = 0; i < _lResponse.size(); i++) {
                                    dto = new VLPDLoadingDTO(_lResponse.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                if (dto.getStatus()) {

                                    Common.setIsPopupActive(true);
                                    new SoundUtils().alertSuccess(getActivity(), getActivity());
                                    DialogUtils.showAlertDialog(getActivity(), "Success", dto.getMessage(), R.drawable.success,new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    etQty.setText("");
                                                    getNONRSNSKULoadingInfo(lblScannedItem.getText().toString());
                                                    Common.setIsPopupActive(false);
                                                    break;
                                            }
                                        }
                                    });
                                }else{
                                    common.showUserDefinedAlertType(dto.getMessage(), getActivity(), getContext(),"Warning");
                                }

                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmVLPDLoading_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmVLPDLoading_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmVLPDLoading_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }

    }

    private void GetVLPDStatus() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setvLPDNumber(vlpdRefNo);
            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
            try {
                call = apiService.GetVLPDStatus(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetVLPDStatus_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lInbound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInbound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                VlpdDto dto = null;
                                for (int i = 0; i < _lInbound.size(); i++) {
                                    dto = new VlpdDto(_lInbound.get(i).entrySet());
                                }
                                if (dto.getResult().toString().equals("0") || dto.getResult().toString().equals("4") || dto.getResult().toString().equals("5")) {

                                    rlVLPDLoading.setVisibility(View.GONE);
                                    rlVLPDSelect.setVisibility(View.VISIBLE);
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showUserDefinedAlertType(errorMessages.EMC_034, getActivity(), getContext(), "Error");
                                } else {
                                    rlVLPDLoading.setVisibility(View.VISIBLE);
                                    rlVLPDSelect.setVisibility(View.GONE);
                                    lblVLPDNumber.setText(vlpdRefNo);
                                    vlpdId =dto.getiD();
                                    ProgressDialogUtils.closeProgressDialog();
                                }

                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetVLPDStatus_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetVLPDStatus_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetVLPDStatus_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }


    }

    public void ClearFields() {
        cvScan.setCardBackgroundColor(getResources().getColor(R.color.scanColor));
        ivScan.setImageResource(R.drawable.fullscreen_img);
        etQty.setText("");
        lblBoxCount.setText("");
        btnSubmit.setTextColor(getResources().getColor(R.color.black));
        btnSubmit.setBackgroundResource(R.drawable.button_hide);
        lblScannedItem.setText("");
        lblScannedItem.setText("");
        lblDesc.setText("");
        lblPickQty.setText("");
        lblLoadQty.setText("");
        etQty.setText("");
    }


    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                getScanner = barcodeReadEvent.getBarcodeData();
                ProcessScannedinfo(getScanner);


            }

        });
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }

    @Override
    public void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent) {

    }

    public void HoneyWellBarcodeListeners() {

        barcodeReader.addTriggerListener(this);

        if (barcodeReader != null) {
            // set the trigger mode to client control
            barcodeReader.addBarcodeListener(this);
            try {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
            } catch (UnsupportedPropertyException e) {
                // Toast.makeText(this, "Failed to apply properties", Toast.LENGTH_SHORT).show();
            }

            Map<String, Object> properties = new HashMap<String, Object>();
            // Set Symbologies On/Off
            properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
            // Set Max Code 39 barcode length
            properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
            // Turn on center decoding
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Enable bad read response
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);
            // Apply the settings
            barcodeReader.setProperties(properties);
        }

    }


    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {

        if (scannedData != null && !Common.isPopupActive()) {

            if(!ProgressDialogUtils.isProgressActive()) {

           /* if (rlVLPDLoading.getVisibility() == View.VISIBLE) {
                lblVLPDNumber.setText(scannedData);
            }*/
                barcode=scannedData;
                btnSubmit.setEnabled(false);
                btnSubmit.setTextColor(getResources().getColor(R.color.black));
                btnSubmit.setBackgroundResource(R.drawable.button_hide);
                btnExport.setVisibility(View.VISIBLE);
                if (scannedData.split("/").length == 4 && scannedData.split("/")[0].length() == 10) {
                    etQty.setText("0");
                    lblScannedItem.setText(scannedData);
                    ConfirmVLPDLoading();
                }
                else if (ScanValidator.IsRSNScanned(scannedData))
                //else if (scannedData.Length == 17 && scannedData.Substring(0, 1) == "A")
                {
                    etQty.setText("0");
                    lblScannedItem.setText(scannedData);
                    ConfirmVLPDLoading();
                }
                else if (ScanValidator.IsMatressBundleScanned(scannedData)) {
                    lblScannedItem.setText(scannedData);
                    confirmMatressBunle(scannedData);
                }
                else {
                    btnExport.setVisibility(View.INVISIBLE);
                    btnSubmit.setEnabled(true);
                    btnSubmit.setTextColor(getResources().getColor(R.color.white));
                    btnSubmit.setBackgroundResource(R.drawable.button_shape);
                    getNONRSNSKULoadingInfo(scannedData);
                    //common.showUserDefinedAlertType(errorMessages.EMC_0045, getActivity(), getContext(), "Error");
                }
            }else {
                if(!Common.isPopupActive())
                {
                    common.showUserDefinedAlertType(errorMessages.EMC_081, getActivity(), getContext(), "Error");

                }
                sound.alertWarning(getActivity(),getContext());

            }
        }
    }


    private void getNONRSNSKULoadingInfo(String scannedCode) {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.ItemInfoDTO, getContext());
            ItemInfoDTO itemInfoDTO = new ItemInfoDTO();
            itemInfoDTO.setVlpdAssgnmentId(vlpdId);
            itemInfoDTO.setMcode(scannedCode);
            message.setEntityObject(itemInfoDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                call = apiService.getNONRSNSKULoadingInfo(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenRefNumberList_01", getActivity());
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

                                LinkedTreeMap<String, String> _lstItemInfo = new LinkedTreeMap<String, String>();
                                _lstItemInfo =(LinkedTreeMap<String, String>) core.getEntityObject();
                                ItemInfoDTO dto = null;
                                dto = new ItemInfoDTO(_lstItemInfo.entrySet());
                                if(dto!=null){
                                    lblScannedItem.setText(dto.getMcode());
                                    lblPickQty.setText(dto.getPickedQuantity());
                                    lblLoadQty.setText(dto.getPendingQuantity());
                                    lblDesc.setText(dto.getDescription());
                                    mmId = Integer.parseInt(dto.getMaterialMasterId());
                                }
                                ProgressDialogUtils.closeProgressDialog();
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenRefNumberList_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenRefNumberList_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenRefNumberList_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Alert");
        }

    }


    private void confirmMatressBunle(String Materessbundle) {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutboundDTO outboundDTO = new OutboundDTO();
            outboundDTO.setOBDNumber(vlpdRefNo);
            outboundDTO.setUserId(userId);
            outboundDTO.setBoxNO(Materessbundle);
            message.setEntityObject(outboundDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);
            try {
                call = apiService.ConfirmMatressBunle(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmMatressBunle_01", getActivity());
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

                                btnSubmit.setEnabled(false);

                                btnSubmit.setTextColor(getResources().getColor(R.color.black));
                                btnSubmit.setBackgroundResource(R.drawable.button_hide);

                                lblScannedItem.setText("");

                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lresponse = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lresponse = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                ExecutionResponseDTO dto = null;
                                for (int i = 0; i < _lresponse.size(); i++) {
                                    dto = new ExecutionResponseDTO(_lresponse.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                if (dto.getStatus())
                                {
                                    lblBoxCount.setText(dto.getMessage());
                                    etQty.setText("1");
                                    return;
                                }
                                else
                                {
                                    lblScannedItem.setText("");

                                    btnSubmit.setEnabled(false);
                                    btnSubmit.setTextColor(getResources().getColor(R.color.black));
                                    btnSubmit.setBackgroundResource(R.drawable.button_hide);

                                    common.showUserDefinedAlertType(dto.getMessage(), getActivity(), getContext(), "Error");

                                }

                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmMatressBunle_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmMatressBunle_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmMatressBunle_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }


    }

    private void ConfirmVLPDLoading() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setvLPDNumber(vlpdRefNo);
            vlpdDto.setQuantity(etQty.getText().toString());
            vlpdDto.setiD(userId);
            vlpdDto.setrSNNumber(lblScannedItem.getText().toString());

            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);
            try {
                call = apiService.ConfirmVLPDLoading(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmVLPDLoading_01", getActivity());
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

                                    lblScannedItem.setText("");

                                    btnSubmit.setTextColor(getResources().getColor(R.color.black));
                                    btnSubmit.setBackgroundResource(R.drawable.button_hide);

                                    btnSubmit.setEnabled(false);

                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                            } else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lResponse = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lResponse = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                VLPDLoadingDTO dto = null;
                                for (int i = 0; i < _lResponse.size(); i++) {
                                    dto = new VLPDLoadingDTO(_lResponse.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                if (dto.getStatus()) {
                                    lblBoxCount.setText(dto.getScannedQty());
                                    btnSubmit.setEnabled(false);
                                    etQty.setText("1");
                                    btnSubmit.setTextColor(getResources().getColor(R.color.black));
                                    btnSubmit.setBackgroundResource(R.drawable.button_hide);
                                    cvScan.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScan.setImageResource(R.drawable.check);
                                } else {
                                    if (dto.getMessage() == null || dto.getMessage().isEmpty()) {
                                        etQty.setText(dto.getSetQty());
                                        btnSubmit.setEnabled(true);

                                        btnSubmit.setTextColor(getResources().getColor(R.color.white));
                                        btnSubmit.setBackgroundResource(R.drawable.button_shape);

                                        cvScan.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScan.setImageResource(R.drawable.check);
                                    } else {

                                        btnSubmit.setEnabled(false);

                                        btnSubmit.setTextColor(getResources().getColor(R.color.black));
                                        btnSubmit.setBackgroundResource(R.drawable.button_hide);

                                        lblScannedItem.setText("");

                                        cvScan.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScan.setImageResource(R.drawable.invalid_cross);
                                        common.showUserDefinedAlertType(dto.getMessage(), getActivity(), getContext(), "Error");
                                        return;
                                    }
                                }
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmVLPDLoading_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmVLPDLoading_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmVLPDLoading_04", getActivity());
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
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);


                        } catch (Exception ex) {

                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002", getContext());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            logException();


                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        ProgressDialogUtils.closeProgressDialog();
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
            }
            // notifications while paused.
            barcodeReader.release();
        }
    }

    @Override
    public void onResume() {
        super.onResume();


        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                // Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_vlpd_loading));
    }

    //Barcode scanner API
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (barcodeReader != null) {
            // unregister barcode event listener honeywell
            barcodeReader.removeBarcodeListener((BarcodeReader.BarcodeListener) this);

            // unregister trigger state change listener
            barcodeReader.removeTriggerListener((BarcodeReader.TriggerListener) this);
        }

        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", false);
        getActivity().sendBroadcast(RTintent);
        getActivity().unregisterReceiver(this.myDataReceiver);
        super.onDestroyView();
    }

}