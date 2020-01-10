package com.inventrax.athome_multiwh.fragments.HH;

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
import com.inventrax.athome_multiwh.pojos.CycleCountDTO;
import com.inventrax.athome_multiwh.pojos.WMSCoreMessage;
import com.inventrax.athome_multiwh.pojos.WMSExceptionMessage;
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

public class CycleCountFragmentHH extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_006";
    private View rootView;

    private RelativeLayout rlCC;
    private TextView lblCount, lblScannedSku, lblDesc, lblLocation, lblSKU, lblQty;
    private CardView cvScanLocation, cvScan;
    private ImageView ivScanLocation, ivScan;
    private TextInputLayout txtInputLayoutBarcode, txtInputLayoutQty, txtInputLayoutCCQty;
    private EditText etCCQty;
    private Button btnClearBin, btnCloseBin, btnExport, btnClear, btnConfirm, btnClose;

    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;

    String userId = null, materialType = null;
    private int flag = 0;
    CycleCountDTO ccDto;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private boolean Isresult = false;
    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;


    Boolean cleaBinState = false, exportState = false;


    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public CycleCountFragmentHH() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.hh_fragment_cc, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        rlCC = (RelativeLayout) rootView.findViewById(R.id.rlCC);

        lblCount = (TextView) rootView.findViewById(R.id.lblCount);
        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);
        lblLocation = (TextView) rootView.findViewById(R.id.lblLocation);
        lblDesc = (TextView) rootView.findViewById(R.id.lblDesc);
        lblSKU = (TextView) rootView.findViewById(R.id.lblSKU);
        lblQty = (TextView) rootView.findViewById(R.id.lblQty);

        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScan = (CardView) rootView.findViewById(R.id.cvScan);

        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScan = (ImageView) rootView.findViewById(R.id.ivScan);


        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);
        txtInputLayoutCCQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutCCQty);

        etCCQty = (EditText) rootView.findViewById(R.id.etCCQty);

        btnClearBin = (Button) rootView.findViewById(R.id.btnClearBin);
        btnClose = (Button) rootView.findViewById(R.id.btnClose);
        btnCloseBin = (Button) rootView.findViewById(R.id.btnCloseBin);
        btnExport = (Button) rootView.findViewById(R.id.btnExport);
        btnClear = (Button) rootView.findViewById(R.id.btnClear);
        btnConfirm = (Button) rootView.findViewById(R.id.btnConfirm);

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");

        btnClearBin.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnCloseBin.setOnClickListener(this);
        btnExport.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        btnConfirm.setTextColor(getResources().getColor(R.color.black));
        btnConfirm.setBackgroundResource(R.drawable.button_hide);
        btnConfirm.setEnabled(false);
        ccDto = new CycleCountDTO();
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
        if (getArguments() != null) {

            lblLocation.setText(getArguments().getString("loc"));
            lblCount.setText(getArguments().getString("count"));
            lblScannedSku.setText(getArguments().getString("barcode"));
            lblSKU.setText(getArguments().getString("sku"));
            lblDesc.setText(getArguments().getString("desc"));
            lblQty.setText(getArguments().getString("qty"));
            etCCQty.setText(getArguments().getString("ccQty"));
            ccDto = (CycleCountDTO) getArguments().getSerializable("ItemInfoDto");
            cleaBinState = getArguments().getBoolean("cleaBinState");
            exportState = getArguments().getBoolean("exportState");

            if (cleaBinState) {
                btnClearBin.setEnabled(true);
            }

            if (exportState) {
                btnExport.setEnabled(true);
            }

            cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
            ivScanLocation.setImageResource(R.drawable.check);
        }

    }

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnClose:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnClear:

                ClearFields();

                lblScannedSku.setText("");

                break;
            case R.id.btnClearBin:
                clearBin();
                break;
            case R.id.btnCloseBin:

                if (lblLocation.getText().toString().isEmpty()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Error");
                    return;
                } else {
                    getActualAndCCQuantiitesByLocation();
                }

                break;
            case R.id.btnConfirm:

                if (etCCQty.getText().toString().isEmpty()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_0068, getActivity(), getContext(), "Error");
                    return;
                } else if (Integer.parseInt(etCCQty.getText().toString()) > 0) {
                    Confirmbin();
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_073, getActivity(), getContext(), "Error");
                }
                break;
            case R.id.btnExport:

                if (lblLocation.getText().toString().isEmpty()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Error");

                    return;
                } else {
                    goToExport();
                }

                break;

            default:
                break;
        }
    }

    public void goToExport() {


        Bundle bundle = new Bundle();

        bundle.putString("loc", lblLocation.getText().toString());
        bundle.putString("count", lblCount.getText().toString());
        bundle.putString("barcode", lblScannedSku.getText().toString());
        bundle.putString("sku", lblSKU.getText().toString());
        bundle.putString("desc", lblDesc.getText().toString());
        bundle.putString("qty", lblQty.getText().toString());
        bundle.putString("ccQty", etCCQty.getText().toString());

        bundle.putSerializable("cycleCountDto", ccDto);
        bundle.putBoolean("cleaBinState", cleaBinState);
        bundle.putBoolean("exportState", exportState);


        PendingCyclecountFragment pendingCyclecountFragment = new PendingCyclecountFragment();
        pendingCyclecountFragment.setArguments(bundle);

        FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, pendingCyclecountFragment);
    }

    public void clearBin() {

       /* cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanLocation.setImageResource(R.drawable.fullscreen_img);

        lblLocation.setText("");
        lblCount.setText("");
        ClearFields();*/

        clearBinapi();

    }

    private void clearBinapi() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.CycleCount, getContext());
            final CycleCountDTO oCyclecountDTO = new CycleCountDTO();
            oCyclecountDTO.setLocation(lblLocation.getText().toString());
            oCyclecountDTO.setEANScanned(true);
            oCyclecountDTO.setEANSpecified(true);
            oCyclecountDTO.setUserId(userId);

            message.setEntityObject(oCyclecountDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.ClearBin(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ClearBin_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                for (int i = 0; i < _lInventory.size(); i++) {
                                    ccDto = new CycleCountDTO(_lInventory.get(i).entrySet());

                                }
                                ProgressDialogUtils.closeProgressDialog();


                                if (ccDto.getResult().equals("Cleared successfully")) {


                                   /* cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                    ivScanLocation.setImageResource(R.drawable.fullscreen_img);
                                    lblLocation.setText("");*/
                                    lblCount.setText("");
                                    ClearFields();
                                    lblCount.setText("");
                                    ClearFields();
                                    btnClearBin.setEnabled(false);
                                    common.showUserDefinedAlertType(errorMessages.EMC_070, getActivity(), getContext(), "Success");
                                } else {
                                    common.showUserDefinedAlertType(errorMessages.EMC_071, getActivity(), getContext(), "Error");
                                    return;
                                }

                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ClearBin_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ClearBin_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ClearBin_04", getActivity());
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
        Isresult = false;
        etCCQty.setText("");
        lblSKU.setText("");
        lblDesc.setText("");
        lblQty.setText("");

        btnConfirm.setEnabled(false);
        etCCQty.setEnabled(false);


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
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
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
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);
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

        if (ScanValidator.IsLocationScanned(scannedData)) {

            ClearFields();
            lblCount.setText("");
            lblLocation.setText(scannedData.substring(0, 7));// scanneddata;
            CheckLocation();

        } else {

            if (!lblLocation.getText().toString().isEmpty()) {
                if (!ScanValidator.IsPalletScanned(scannedData) && !ScanValidator.IsLocationScanned(scannedData) && !ScanValidator.IsRSNScanned(scannedData)) {


                    if (btnConfirm.isEnabled()) {
                        common.showUserDefinedAlertType("Please confirm qty for " + lblScannedSku.getText().toString(), getActivity(), getContext(), "Warning");
                        return;
                    }


                    if (scannedData.split("[,]").length == 2) {

                        lblScannedSku.setText(scannedData.split("[,]")[0]);
                        if (scannedData.split("[,]")[1].equalsIgnoreCase("1")) {

                            lblQty.setText("1");
                            flag = 1;
                            GetSKUdeatils("auto");
                            etCCQty.setText("1");
                            etCCQty.setEnabled(false);
                            btnConfirm.setEnabled(false);
                            btnConfirm.setTextColor(getResources().getColor(R.color.black));
                            btnConfirm.setBackgroundResource(R.drawable.button_hide);
                            //  Confirmbin();

                            return;
                        } else {
                            //cyclecount.IsEAN = true;
                            // GetSKUdeatils(lblScanLocation.Text, lblBarcodevalue.Text, Program.Division);
                            lblQty.setText(scannedData.split("[,]")[1]);
                            flag = 0;
                            etCCQty.setEnabled(true);
                            btnConfirm.setEnabled(true);
                            btnConfirm.setTextColor(getResources().getColor(R.color.white));
                            btnConfirm.setBackgroundResource(R.drawable.button_shape);

                            GetSKUdeatils("manual");
                            return;
                        }

                    } else {
                        //cyclecount.IsEAN = true;
                        lblScannedSku.setText(scannedData);

                        lblQty.setText("1");
                        flag = 1;
                        GetSKUdeatils("auto");
                        etCCQty.setText("1");
                        etCCQty.setEnabled(false);
                        btnConfirm.setEnabled(true);
                        btnConfirm.setTextColor(getResources().getColor(R.color.white));
                        btnConfirm.setBackgroundResource(R.drawable.button_shape);
                        //Confirmbin();

                        return;
                    }


                    //Handling Set Quantity in EAN Barcode
                 /*   if (scannedData.split("[,]").length == 2) {

                        lblScannedSku.setText(scannedData.split("[,]")[0]);
                        if (scannedData.split("[,]")[1] == "1") {

                            lblQty.setText("1");
                            flag = 1;
                            GetSKUdeatils("auto");
                            etCCQty.setText("1");
                            etCCQty.setEnabled(false);
                            btnConfirm.setEnabled(false);
                            btnConfirm.setTextColor(getResources().getColor(R.color.black));
                            btnConfirm.setBackgroundResource(R.drawable.button_hide);
                            if (Isresult) {
                                Confirmbin();
                            }
                        } else {
                            //cyclecount.IsEAN = true;
                            // GetSKUdeatils(lblScanLocation.Text, lblBarcodevalue.Text, Program.Division);
                            lblQty.setText(scannedData.split("[,]")[1]);
                            flag = 0;
                            etCCQty.setEnabled(true);
                            btnConfirm.setEnabled(true);
                            btnConfirm.setTextColor(getResources().getColor(R.color.white));
                            btnConfirm.setBackgroundResource(R.drawable.button_shape);

                            GetSKUdeatils("manual");
                        }

                    } else {
                        //cyclecount.IsEAN = true;
                        lblScannedSku.setText(scannedData);

                        lblQty.setText("1");
                        flag = 1;
                        GetSKUdeatils("manual");
                        etCCQty.setText("1");
                        etCCQty.setEnabled(false);
                        btnConfirm.setEnabled(true);
                        btnConfirm.setTextColor(getResources().getColor(R.color.white));
                        btnConfirm.setBackgroundResource(R.drawable.button_shape);

                    }*/
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0045, getActivity(), getContext(), "Error");
                    etCCQty.setText("");
                    return;
                }


            } else {
                common.showUserDefinedAlertType(errorMessages.EMC_0038, getActivity(), getContext(), "Error");
                lblLocation.setText("");
            }
        }
    }

    public void clearAfterConfirmbin() {
        lblScannedSku.setText("");
        etCCQty.setText("");

        btnConfirm.setEnabled(false);
        btnConfirm.setTextColor(getResources().getColor(R.color.black));
        btnConfirm.setBackgroundResource(R.drawable.button_hide);


    }

    private void getActualAndCCQuantiitesByLocation() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.CycleCount, getContext());
            final CycleCountDTO oCyclecountDTO = new CycleCountDTO();
            oCyclecountDTO.setEANScanned(true);
            oCyclecountDTO.setEANSpecified(true);
            oCyclecountDTO.setMaterialType(materialType);
            oCyclecountDTO.setLocation(lblLocation.getText().toString());
            oCyclecountDTO.setUserId(userId);
            message.setEntityObject(oCyclecountDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.GetActualAndCCQuantiitesByLocation(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetActualAndCCQuantiitesByLocation_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                CycleCountDTO oCycleCountDto = null;

                                for (int i = 0; i < _lInventory.size(); i++) {
                                    oCycleCountDto = new CycleCountDTO(_lInventory.get(i).entrySet());

                                }
                                ProgressDialogUtils.closeProgressDialog();

                                if (oCycleCountDto.getResult().equals("True")) {

                                    DialogUtils.showConfirmDialog(getActivity(), "Confirm Close Bin", "System Qty. " + oCycleCountDto.getLogicalBincount() + ", CC Qty. " + oCycleCountDto.getPhysicalBinCount(), new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    closeBinforCycleCount();
                                                    // clearBin();
                                                    clearAfterConfirmbin();
                                                    break;

                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    //Toast.makeText(getActivity(),"Pressed cancel..!",Toast.LENGTH_LONG).show();

                                                    break;
                                            }

                                        }
                                    });

                                } else {
                                    common.showUserDefinedAlertType(oCycleCountDto.getResult(), getActivity(), getContext(), "Error");
                                    ClearFields();
                                    return;
                                }

                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetActualAndCCQuantiitesByLocation_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetActualAndCCQuantiitesByLocation_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetActualAndCCQuantiitesByLocation_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }

    }

    private void closeBinforCycleCount() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.CycleCount, getContext());
            final CycleCountDTO oCyclecountDTO = new CycleCountDTO();
            oCyclecountDTO.setMaterialType(materialType);
            oCyclecountDTO.setLocation(lblLocation.getText().toString());
            oCyclecountDTO.setEANScanned(true);
            oCyclecountDTO.setEANSpecified(true);
            oCyclecountDTO.setUserId(userId);
            message.setEntityObject(oCyclecountDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.CloseBinforCycleCount(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CloseBinforCycleCount_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                CycleCountDTO cycleCountDTO = null;

                                for (int i = 0; i < _lInventory.size(); i++) {
                                    cycleCountDTO = new CycleCountDTO(_lInventory.get(i).entrySet());

                                }
                                ProgressDialogUtils.closeProgressDialog();

                                if (cycleCountDTO.getResult().equals("Closed successfully")) {
                                    ClearFields();
                                    lblCount.setText("");
                                    lblLocation.setText("");
                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                    ivScanLocation.setImageResource(R.drawable.fullscreen_img);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0044, getActivity(), getContext(), "Success");
                                } else {
                                    //  common.showUserDefinedAlertType(errorMessages.EMC_0045, getActivity(), getContext(), "Error");
                                    ClearFields();
                                    return;
                                }
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CloseBinforCycleCount_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CloseBinforCycleCount_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CloseBinforCycleCount_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }

    }

    private void CheckLocation() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.CycleCount, getContext());
            CycleCountDTO oCyclecountDTO = new CycleCountDTO();
            oCyclecountDTO.setLocation(lblLocation.getText().toString());
            oCyclecountDTO.setEANScanned(true);
            oCyclecountDTO.setEANSpecified(true);

            message.setEntityObject(oCyclecountDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.CheckLocation(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CheckLocation_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                CycleCountDTO dto = null;
                                for (int i = 0; i < _lInventory.size(); i++) {
                                    dto = new CycleCountDTO(_lInventory.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();


                                btnClearBin.setEnabled(true);
                                btnExport.setEnabled(true);

                                cleaBinState = true;
                                exportState = true;

                                if (dto.getResult().equals("Invalid Location")) {
                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanLocation.setImageResource(R.drawable.invalid_cross);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Error");
                                    lblLocation.setText("");

                                    return;
                                } else {
                                    if (!dto.getResult().equals("")) {
                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanLocation.setImageResource(R.drawable.invalid_cross);
                                        common.showUserDefinedAlertType(dto.getResult(), getActivity(), getContext(), "Error");
                                        lblLocation.setText("");
                                        return;
                                    } else {
                                        if (dto.getCCQty() != "0") {
                                            lblCount.setText(dto.getCCQty());
                                            cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanLocation.setImageResource(R.drawable.check);
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CheckLocation_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CheckLocation_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CheckLocation_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }

    }

    private void GetSKUdeatils(final String scan) {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.CycleCount, getContext());
            final CycleCountDTO oCyclecountDTO = new CycleCountDTO();
            oCyclecountDTO.setLocation(lblLocation.getText().toString());
            oCyclecountDTO.setMaterialType(materialType);
            oCyclecountDTO.setBarcode(lblScannedSku.getText().toString());
            message.setEntityObject(oCyclecountDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.GetSKUDetails(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetSKUDetails_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                for (int i = 0; i < _lInventory.size(); i++) {
                                    ccDto = new CycleCountDTO(_lInventory.get(i).entrySet());

                                }
                                ProgressDialogUtils.closeProgressDialog();

                                if (!ccDto.getMaterialCode().equals("")) {
                                    lblSKU.setText(ccDto.getMaterialCode());
                                    lblDesc.setText(ccDto.getMDesc());
                                    Isresult = true;

                                    if (scan.equalsIgnoreCase("auto")) {
                                        Confirmbin();
                                        return;
                                    }

                                    btnConfirm.setEnabled(true);
                                    btnConfirm.setTextColor(getResources().getColor(R.color.white));
                                    btnConfirm.setBackgroundResource(R.drawable.button_shape);
                                    common.showUserDefinedAlertType(errorMessages.EMC_072, getActivity(), getContext(), "Warning");




                                /*    if (Isresult && flag == 1) {

                                    }*/


                                } else {
                                    common.showUserDefinedAlertType(errorMessages.EMC_0045, getActivity(), getContext(), "Error");
                                    ClearFields();
                                    return;
                                }

                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetSKUDetails_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetSKUDetails_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetSKUDetails_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }

    }


    private void Confirmbin() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.CycleCount, getContext());
            CycleCountDTO oCyclecountDTO = new CycleCountDTO();
            oCyclecountDTO.setLocation(lblLocation.getText().toString());
            oCyclecountDTO.setBarcode(lblScannedSku.getText().toString());
            oCyclecountDTO.setMaterialCode(lblSKU.getText().toString());
            oCyclecountDTO.setMDesc(lblDesc.getText().toString());
            oCyclecountDTO.setWMSQty(lblQty.getText().toString());
            oCyclecountDTO.setCCQty(etCCQty.getText().toString());
            oCyclecountDTO.setEANScanned(true);
            oCyclecountDTO.setEANSpecified(true);
            oCyclecountDTO.setUserId(userId);

            if (ccDto != null) {
                ccDto.setLocation(lblLocation.getText().toString());
                ccDto.setBarcode(lblScannedSku.getText().toString());
                ccDto.setMaterialCode(lblSKU.getText().toString());
                ccDto.setMDesc(lblDesc.getText().toString());
                ccDto.setWMSQty(lblQty.getText().toString());
                ccDto.setCCQty(etCCQty.getText().toString());
                ccDto.setUserId(userId);
                ccDto.setEANScanned(true);
                ccDto.setEANSpecified(true);
                message.setEntityObject(ccDto);
            } else {
                message.setEntityObject(oCyclecountDTO);

            }


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.ConfirmCycleCount(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmCycleCount_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                CycleCountDTO dto = null;
                                for (int i = 0; i < _lInventory.size(); i++) {
                                    dto = new CycleCountDTO(_lInventory.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();


                                if (dto.getResult().equals("Confirmed successfully")) {
                                    common.showUserDefinedAlertType(dto.getResult(), getActivity(), getContext(), "Success");

                                    lblCount.setText(dto.getCCQty());
                                    cvScan.setCardBackgroundColor(getResources().getColor(R.color.scanColor));
                                    ivScan.setImageResource(R.drawable.fullscreen_img);

                                    lblScannedSku.setText("");


                                    etCCQty.setText("");
                                    lblDesc.setText("");
                                    lblSKU.setText("");
                                    lblQty.setText("");

                                    btnConfirm.setEnabled(false);
                                    etCCQty.setEnabled(false);

                                    btnConfirm.setTextColor(getResources().getColor(R.color.black));
                                    btnConfirm.setBackgroundResource(R.drawable.button_hide);

                                    clearAfterConfirmbin();

                                } else {
                                    //  common.showUserDefinedAlertType(dto.getResult(), getActivity(), getContext(), "Success");

                                    //ClearFields();
                                }


                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmCycleCount_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmCycleCount_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmCycleCount_04", getActivity());
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_cycle_count));
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