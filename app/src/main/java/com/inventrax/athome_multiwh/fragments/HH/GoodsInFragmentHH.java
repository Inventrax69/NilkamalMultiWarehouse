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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import com.inventrax.athome_multiwh.fragments.PendingInboundListFragment;
import com.inventrax.athome_multiwh.interfaces.ApiInterface;
import com.inventrax.athome_multiwh.pojos.DimensionsDTO;
import com.inventrax.athome_multiwh.pojos.ExecutionResponseDTO;
import com.inventrax.athome_multiwh.pojos.InboundDTO;
import com.inventrax.athome_multiwh.pojos.InventoryDTO;
import com.inventrax.athome_multiwh.pojos.PalletInfoDTO;
import com.inventrax.athome_multiwh.pojos.StorageLocationDTO;
import com.inventrax.athome_multiwh.pojos.WMSCoreMessage;
import com.inventrax.athome_multiwh.pojos.WMSExceptionMessage;
import com.inventrax.athome_multiwh.searchableSpinner.SearchableSpinner;
import com.inventrax.athome_multiwh.services.RestService;
import com.inventrax.athome_multiwh.util.CustomEditText;
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

public class GoodsInFragmentHH extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_GOODS-IN HH";
    private View rootView;

    private TextView lblStoreRefNo, lblInboundQty, lblScannedSku, lblDesc;
    private CardView cvScanPallet, cvScanLocation, cvScanEAN;
    private ImageView ivScanPallet, ivScanLocation, ivScanEAN;
    private TextInputLayout txtInputLayoutPallet, txtInputLayoutLocation, txtInputLayoutEAN, txtInputLayoutLength, txtInputLayoutBreadth, txtInputLayoutHeight, txtInputLayoutWeight, txtInputLayoutQty, txtInputLayoutVolume, txtInputLayoutTweight, txtInputLayoutCase;
    private CustomEditText etPallet, etLocation, etEAN, etLength, etBreadth, etHeight, etWeight, etQty, etVolume, etTweight, etCase;
    private SearchableSpinner spinnerSelectSloc;
    private Button btnPalletClose, btnConfirmHHLBH, btnExport, btnClose;

    FragmentUtils fragmentUtils;
    SoundUtils soundUtils=null;
    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    String userId = null, inboundId = null;
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    String storageloc = null, clientId = null, defaultSloc = null, materialType = null, InboundId = null;
    ArrayList<String> sloc;
    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;

    private boolean IsReceivingBin = false;
    private boolean isMaxVolumeReached = false;
    private boolean isMaxWeightReached = false;
    private boolean IsPalletLoading = false;
    double palletMaxVolume = 0;
    double palletLoadedVolume = 0;
    int palletMaxWeight = 0;
    int palletLoadedWeight = 0;
    private InboundDTO oInboundDataDTO = null;
    private String auditbinLocation = null;
    private String pallet = null, location = null, ean = null, L = null, B = null, H = null, W = null, box = null, qty = null, vol = null, twt = null, caseString = null, sku = null, desc = null, count = null;


    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public GoodsInFragmentHH() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.hh_fragment_goodsin, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {
        soundUtils=new SoundUtils();
        lblStoreRefNo = (TextView) rootView.findViewById(R.id.lblStoreRefNo);
        lblInboundQty = (TextView) rootView.findViewById(R.id.lblInboundQty);
        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);
        lblDesc = (TextView) rootView.findViewById(R.id.lblDesc);

        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScanEAN = (CardView) rootView.findViewById(R.id.cvScanEAN);

        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScanEAN = (ImageView) rootView.findViewById(R.id.ivScanEAN);

        txtInputLayoutPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPallet);
        txtInputLayoutLocation = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutLocation);
        txtInputLayoutEAN = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutEAN);
        txtInputLayoutLength = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutLength);
        txtInputLayoutBreadth = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutBreadth);
        txtInputLayoutHeight = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutHeight);
        txtInputLayoutWeight = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutWeight);
        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);
        txtInputLayoutVolume = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutVolume);
        txtInputLayoutTweight = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutTweight);
        txtInputLayoutCase = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutCase);

        etPallet = (CustomEditText) rootView.findViewById(R.id.etPallet);
        etLocation = (CustomEditText) rootView.findViewById(R.id.etLocation);
        etEAN = (CustomEditText) rootView.findViewById(R.id.etEAN);
        etLength = (CustomEditText) rootView.findViewById(R.id.etLength);
        etBreadth = (CustomEditText) rootView.findViewById(R.id.etBreadth);
        etHeight = (CustomEditText) rootView.findViewById(R.id.etHeight);
        etWeight = (CustomEditText) rootView.findViewById(R.id.etWeight);
        etQty = (CustomEditText) rootView.findViewById(R.id.etQty);
        etVolume = (CustomEditText) rootView.findViewById(R.id.etVolume);
        etTweight = (CustomEditText) rootView.findViewById(R.id.etTweight);
        etCase = (CustomEditText) rootView.findViewById(R.id.etCase);

        spinnerSelectSloc = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectSloc);
        spinnerSelectSloc.setOnItemSelectedListener(this);

        btnClose = (Button) rootView.findViewById(R.id.btnClose);
        btnPalletClose = (Button) rootView.findViewById(R.id.btnPalletClose);
        btnConfirmHHLBH = (Button) rootView.findViewById(R.id.btnConfirmHHLBH);
        btnExport = (Button) rootView.findViewById(R.id.btnExport);

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");
        if (getArguments() != null) {


            etPallet.setEnabled(getArguments().getBoolean("IsPalletEnabled"));
            etLocation.setEnabled(getArguments().getBoolean("IsLocationEnabled"));
            IsReceivingBin= getArguments().getBoolean("IsReceivingBin") ;

            if(IsReceivingBin){
                etPallet.requestFocus();
            }



            lblStoreRefNo.setText(getArguments().getString("StoreRefNo"));
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
            count = getArguments().getString("count");

            etPallet.setText(pallet);
            etLocation.setText(location);
            etEAN.setText(ean);
            etLength.setText(L);
            etBreadth.setText(B);
            etHeight.setText(H);
            etWeight.setText(W);
            etVolume.setText(vol);
            etQty.setText(qty);
            etTweight.setText(twt);
            etCase.setText(caseString);
            lblScannedSku.setText(sku);
            lblDesc.setText(desc);
            lblInboundQty.setText(count);
            if (!etPallet.getText().toString().isEmpty()) {

                cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                ivScanPallet.setImageResource(R.drawable.check);
            }

            if (!etLocation.getText().toString().isEmpty()) {

                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                ivScanLocation.setImageResource(R.drawable.check);
            }
            if (!etEAN.getText().toString().isEmpty()) {

                cvScanEAN.setCardBackgroundColor(getResources().getColor(R.color.white));
                ivScanEAN.setImageResource(R.drawable.check);
            }
        }
        btnClose.setOnClickListener(this);
        btnConfirmHHLBH.setOnClickListener(this);
        btnPalletClose.setOnClickListener(this);
        btnExport.setOnClickListener(this);
        btnConfirmHHLBH.setTextColor(getResources().getColor(R.color.black));
        btnConfirmHHLBH.setBackgroundResource(R.drawable.button_hide);
        sloc = new ArrayList<>();


        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();

        // To get Storage Locations
        getStorageLocations();


        etPallet.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //SAVE THE DATA

                } else {


                    if(!etPallet.getText().toString().isEmpty())
                    {
                        getPalletInfo();
                    }

                }

            }
        });

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

    }

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnClose:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnPalletClose:
                ClearFields();
                closePallet();
                break;
            case R.id.btnExport:
                goToPendingInboundList();
                break;
            case R.id.btnConfirmHHLBH:
                UpdateLBH();
            default:
                break;
        }
    }

    public void moveStockToAuditBin() {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setClientID(clientId);
            inboundDTO.setMaterialType("Location");
            inboundDTO.setMaterialCode(auditbinLocation);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.MoveStockToAuditBin(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "MoveStockToAuditBin_01", getActivity());
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
                                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanLocation.setImageResource(R.drawable.warning_img);
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lLocationtype = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lLocationtype = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();


                                ExecutionResponseDTO oResponseDTO = null;
                                for (int i = 0; i < _lLocationtype.size(); i++) {

                                    oResponseDTO = new ExecutionResponseDTO(_lLocationtype.get(i).entrySet());


                                }

                                ProgressDialogUtils.closeProgressDialog();

                                if (!oResponseDTO.getStatus()) {
                                    common.showUserDefinedAlertType(oResponseDTO.getMessage(), getActivity(), getContext(), "Error");
                                    return;
                                } else {
                                    common.showUserDefinedAlertType(errorMessages.EMC_0067, getActivity(), getContext(), "Error");
                                    return;
                                }

                            }


                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "MoveStockToAuditBin_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "MoveStockToAuditBin_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "MoveStockToAuditBin_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    // To send the bundled details to the Export fragment
    public void goToPendingInboundList() {
        Bundle bundle = new Bundle();
        bundle.putString("StoreRefNo", lblStoreRefNo.getText().toString());
        bundle.putString("ClientId", clientId);
        bundle.putString("InboundId", inboundId);

        bundle.putString("pallet", etPallet.getText().toString());
        bundle.putString("location", etLocation.getText().toString());
        bundle.putString("ean", etEAN.getText().toString());
        bundle.putString("L", etLength.getText().toString());
        bundle.putString("B", etBreadth.getText().toString());
        bundle.putString("H", etHeight.getText().toString());
        bundle.putString("V", etVolume.getText().toString());
        bundle.putString("W", etWeight.getText().toString());
        bundle.putString("qty", etQty.getText().toString());
        bundle.putString("twt", etTweight.getText().toString());
        bundle.putString("caseString", etCase.getText().toString());
        bundle.putString("sku", lblScannedSku.getText().toString());
        bundle.putString("desc", lblDesc.getText().toString());
        bundle.putString("count", lblInboundQty.getText().toString());

        bundle.putBoolean("IsLocationEnabled",etLocation.isEnabled());
        bundle.putBoolean("IsPalletEnabled",etPallet.isEnabled());
        bundle.putBoolean("IsReceivingBin",IsReceivingBin);


        PendingInboundListFragment pendingInboundListFragment = new PendingInboundListFragment();
        pendingInboundListFragment.setArguments(bundle);
        FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, pendingInboundListFragment);
    }

    public void ClearFields() {
        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanLocation.setImageResource(R.drawable.fullscreen_img);

        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

        cvScanEAN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanEAN.setImageResource(R.drawable.fullscreen_img);

        etTweight.setText("");
        etVolume.setText("");
        etQty.setText("");
        etBreadth.setText("");
        etHeight.setText("");
        etLength.setText("");
        etWeight.setText("");
        etEAN.setText("");
        etLocation.setText("");
        etPallet.setText("");

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
    public void ProcessScannedinfo(String scannedData)
    {


        if(!ProgressDialogUtils.isProgressActive())
        {
            if (isMaxVolumeReached) {
                common.showUserDefinedAlertType("Pallet Volume :" + palletMaxVolume + " -- Loaded Volumne : " + palletLoadedVolume, getActivity(), getContext(), "Warning");
                return;
            }
            if (isMaxWeightReached) {
                common.showUserDefinedAlertType("Pallet Weight :" + palletMaxWeight + " -- Loaded Weight : " + palletLoadedWeight, getActivity(), getContext(), "Warning");
                return;
            }

            if (scannedData != null && !common.isPopupActive())
            {

                // checking for location scan
                if (etLocation.getText().toString().isEmpty())
                {

                    if (scanValidator.IsLocationScanned(scannedData))
                    {
                        etLocation.setText(scannedData.substring(0, 7));
                        GetLocationType();

                        return;
                    }else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Error");
                        return;
                    }
                }


                // Check for Pallet if in case scanned location is receiving bin zone
                if (etPallet.isEnabled() && etPallet.getText().toString().isEmpty()) {
                    if (scanValidator.IsPalletScanned(scannedData)) {

                        etPallet.setText(scannedData);
                        etPallet.setFocusable(false);
                        return;
                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                        return;
                    }
                }



                // Checking For EAN Scan
                if (btnConfirmHHLBH.isEnabled())
                {
                    common.showUserDefinedAlertType(errorMessages.EMC_0032, getActivity(), getContext(), "Error");
                    btnConfirmHHLBH.setTextColor(getResources().getColor(R.color.white));
                    btnConfirmHHLBH.setBackgroundResource(R.drawable.button_shape);
                    return;

                } else {
                    etEAN.setText(scannedData);
                    confirmReciptOnScan();
                    return;

                }

            }


        }else {
            if(!common.isPopupActive())
            {
                common.showUserDefinedAlertType(errorMessages.EMC_081, getActivity(), getContext(), "Error");

            }
            soundUtils.alertWarning(getActivity(),getContext());
        }




       /* if (scannedData != null && !common.isPopupActive())
        {

            if (scanValidator.IsLocationScanned(scannedData)) {

                etLocation.setText(scannedData.substring(0, 7));
                GetLocationType();
                return;

            } else if (etPallet.isEnabled() && etPallet.getText().toString().isEmpty()) {
                if (scanValidator.IsPalletScanned(scannedData)) {
                    etPallet.setFocusable(true);
                    etPallet.setText(scannedData);
                    getPalletInfo();
                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                    ivScanPallet.setImageResource(R.drawable.check);
                    //btnExport.Focus();
                    // CBLocations.Focus();

                    return;
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                }
            } else {
                if (IsPalletLoading) {
                    if (etPallet.getText().toString().isEmpty()) {
                        common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                        return;
                    }
                }

                if (!etLocation.getText().toString().isEmpty()) {
                    if (btnConfirmHHLBH.isEnabled()) {
                        common.showUserDefinedAlertType(errorMessages.EMC_0032, getActivity(), getContext(), "Error");
                        btnConfirmHHLBH.setTextColor(getResources().getColor(R.color.white));
                        btnConfirmHHLBH.setBackgroundResource(R.drawable.button_shape);
                        return;

                    } else {
                        etEAN.setText(scannedData);
                        confirmReciptOnScan();
                        return;

                    }
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Error");
                    return;
                }
            }
        }*/
    }


    //To get Storage Locations
    public void getStorageLocations() {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setClientID(clientId);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetClientbasedStorageLocations(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetClientbasedStorageLocations_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lSLoc = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lSLoc = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();


                                List<StorageLocationDTO> lstDto = new ArrayList<>();
                                List<String> lstStorageLoc = new ArrayList<>();

                                for (int i = 0; i < _lSLoc.size(); i++) {

                                    StorageLocationDTO oStorageLoc = new StorageLocationDTO(_lSLoc.get(i).entrySet());
                                    lstDto.add(oStorageLoc);

                                }
                                for (int i = 0; i < lstDto.size(); i++) {
                                    lstStorageLoc.add(lstDto.get(i).getSLOCcode());
                                    if (lstDto.get(i).getIsDefault().equals("True")) {
                                        defaultSloc = lstDto.get(i).getSLOCcode();

                                    }
                                }

                                ProgressDialogUtils.closeProgressDialog();
                                ArrayAdapter arrayAdapterSLoc = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstStorageLoc);
                                spinnerSelectSloc.setAdapter(arrayAdapterSLoc);
                                int spinnerPosition = arrayAdapterSLoc.getPosition(defaultSloc);
                                //set the default according to value
                                spinnerSelectSloc.setSelection(spinnerPosition);
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetClientbasedStorageLocations_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetClientbasedStorageLocations_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetClientbasedStorageLocations_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    public void GetLocationType() {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setClientID(clientId);
            inboundDTO.setMaterialType(materialType);
            inboundDTO.setLocation(etLocation.getText().toString());
            inboundDTO.setInboundID(InboundId);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetLocationType(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetLocationType_01", getActivity());
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
                                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanLocation.setImageResource(R.drawable.warning_img);
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lLocationtype = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lLocationtype = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();


                                List<InventoryDTO> lstDto = new ArrayList<>();
                                InventoryDTO oInventoryDTO = null;
                                for (int i = 0; i < _lLocationtype.size(); i++) {

                                    oInventoryDTO = new InventoryDTO(_lLocationtype.get(i).entrySet());
                                    lstDto.add(oInventoryDTO);

                                }
                                if (oInventoryDTO.getLocationTypeID().equals("0")) {
                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanLocation.setImageResource(R.drawable.warning_img);
                                    etLocation.setText("");
                                    common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Error");
                                    ProgressDialogUtils.closeProgressDialog();
                                    return;
                                }
                                if (oInventoryDTO.getLocationTypeID().equals("7")) {
                                    IsPalletLoading = true;
                                    etPallet.setEnabled(true);
                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanLocation.setImageResource(R.drawable.check);
                                    IsReceivingBin = true;
                                    etLocation.setEnabled(false);
                                    etPallet.requestFocus();
                                    ProgressDialogUtils.closeProgressDialog();
                                    return;
                                } else {
                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanLocation.setImageResource(R.drawable.check);
                                    IsPalletLoading = false;
                                    etPallet.setEnabled(false);
                                    IsReceivingBin = false;
                                    ProgressDialogUtils.closeProgressDialog();
                                }
                                ProgressDialogUtils.closeProgressDialog();
                            }


                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetLocationType_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetLocationType_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetLocationType_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    public void confirmReciptOnScan() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setClientID(clientId);
            inboundDTO.setLocation(etLocation.getText().toString());
            inboundDTO.setEANNumber(etEAN.getText().toString());
            inboundDTO.setPalletNo(etPallet.getText().toString());
            inboundDTO.setSelectedStorageLocation(storageloc);
            inboundDTO.setStoreRefNo(lblStoreRefNo.getText().toString());
            inboundDTO.setItemSerialNo(etCase.getText().toString());
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.ConfirmHHReceiptONEANScan(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmHHReceiptONEANScan_01", getActivity());
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
                                etEAN.setText("");
                                cvScanEAN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanEAN.setImageResource(R.drawable.warning_img);
                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lstInbounddata = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstInbounddata = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                InboundDTO oInboundData = null;
                                for (int i = 0; i < _lstInbounddata.size(); i++) {

                                    oInboundData = new InboundDTO(_lstInbounddata.get(i).entrySet());
                                    oInboundDataDTO = oInboundData;
                                }
                                lblScannedSku.setText("SKU: " + oInboundData.getMaterialCode() + "|" + "Batch: " + oInboundData.getBatchNo());
                                lblDesc.setText("Desc. : " + oInboundData.getmDesc());
                                etQty.setText(oInboundData.getBoxQuantity());
                                etLength.setText(oInboundData.getDimensionsDTO().get(0).getLength());
                                etBreadth.setText(oInboundData.getDimensionsDTO().get(0).getBreadth());
                                etHeight.setText(oInboundData.getDimensionsDTO().get(0).getHeight());
                                etWeight.setText(oInboundData.getDimensionsDTO().get(0).getWeight());
                                lblInboundQty.setText(oInboundData.getPalletInfoDTO().get(0).getNoOfBoxesLoaded());
                                etVolume.setText(oInboundData.getPalletInfoDTO().get(0).getLoadedVolume());
                                etTweight.setText(oInboundData.getPalletInfoDTO().get(0).getLoadedWeight());
                                etCase.setText(oInboundData.getSerialNo());

                                if (oInboundData.getDimensionsDTO().get(0).getLength().equals("0")) {
                                    etLength.setEnabled(true);
                                    etBreadth.setEnabled(true);
                                    etHeight.setEnabled(true);
                                    etWeight.setEnabled(true);
                                    btnConfirmHHLBH.setEnabled(true);
                                    btnConfirmHHLBH.setTextColor(getResources().getColor(R.color.white));
                                    btnConfirmHHLBH.setBackgroundResource(R.drawable.button_shape);
                                    ProgressDialogUtils.closeProgressDialog();
                                    return;
                                } else

                                {
                                    etLength.setEnabled(false);
                                    etBreadth.setEnabled(false);
                                    etHeight.setEnabled(false);
                                    etWeight.setEnabled(false);
                                    btnConfirmHHLBH.setEnabled(false);
                                    btnConfirmHHLBH.setTextColor(getResources().getColor(R.color.black));
                                    btnConfirmHHLBH.setBackgroundResource(R.drawable.button_hide);

                                    try {
                                        validateWeightAndVolume(oInboundData);
                                    } catch (Exception ex) {
                                        isMaxVolumeReached = false;
                                        isMaxWeightReached = false;
                                    }


                                }

                                cvScanEAN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanEAN.setImageResource(R.drawable.check);
                                ProgressDialogUtils.closeProgressDialog();

                            }


                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmHHReceiptONEANScan_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmHHReceiptONEANScan_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmHHReceiptONEANScan_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    public void validateWeightAndVolume(InboundDTO oInboundDTO) {

        try {


            palletMaxVolume = Double.parseDouble(oInboundDTO.getPalletInfoDTO().get(0).getPalletVolume());
            palletMaxWeight = Integer.parseInt(oInboundDTO.getPalletInfoDTO().get(0).getPalletMaxWeight());
            palletLoadedVolume = Double.parseDouble(oInboundDTO.getPalletInfoDTO().get(0).getLoadedVolume());
            palletLoadedWeight = Integer.parseInt(oInboundDTO.getPalletInfoDTO().get(0).getLoadedWeight());
            etVolume.setText(oInboundDTO.getPalletInfoDTO().get(0).getLoadedVolume());
            etTweight.setText(oInboundDTO.getPalletInfoDTO().get(0).getLoadedWeight());

            if ((palletLoadedVolume) != 0 && (palletLoadedWeight) != 0) {
                if (palletLoadedVolume >= (palletMaxVolume)) {
                    isMaxVolumeReached = true;
                }


                if ((palletLoadedVolume) >= (palletMaxWeight)) {
                    isMaxWeightReached = true;
                }
            }
        } catch (Exception ex) {

        }
    }


    public void getPalletInfo() {
        if (etLocation.getText().toString().isEmpty()) {
            common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Error");
            return;
        }
        if (!etPallet.getText().toString().isEmpty()) {
            getPalletinformation();
        }
    }


    public void getPalletinformation() {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setClientID(clientId);
            inboundDTO.setPalletNo(etPallet.getText().toString());
            inboundDTO.setLocation(etLocation.getText().toString());
            inboundDTO.setInboundID(inboundId);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetPalletCurrentLocation(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetPalletCurrentLocation_01", getActivity());
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
                                if (owmsExceptionMessage.getWMSExceptionCode().equals("WMC_PUT_CNTL_004")) {
                                    auditbinLocation = owmsExceptionMessage.getWMSMessage().split("[{}]")[1].toString();
                                    /*
                                     * if Pallet Existed Logically in Some location and physically empty, confirming user to
                                     * move the Stock to Audit Bin if USer say Yes Call MoveStockToAuditBin service to Move the stock to audit bin zone so that
                                     * this pallet can be used further
                                     */
                                    DialogUtils.showConfirmDialog(getActivity(), "Confirm Remove", "Pallet is not empty, Do you want to move stock to Audit Bin ?", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:

                                                    moveStockToAuditBin();
                                                    break;

                                                case DialogInterface.BUTTON_NEGATIVE:

                                                    break;
                                            }

                                        }
                                    });

                                } else {
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                                }
                                ProgressDialogUtils.closeProgressDialog();
                                etPallet.setEnabled(true);
                                etPallet.setText("");
                                cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanPallet.setImageResource(R.drawable.warning_img);

                            } else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lPalletinfo = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lPalletinfo = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                PalletInfoDTO oPalletInfo = null;
                                List<PalletInfoDTO> lstPalletdto = new ArrayList<PalletInfoDTO>();

                                for (int i = 0; i < _lPalletinfo.size(); i++) {

                                    oPalletInfo = new PalletInfoDTO(_lPalletinfo.get(i).entrySet());
                                    lstPalletdto.add(oPalletInfo);
                                }

                                if (oPalletInfo != null) {

                                    etPallet.setEnabled(false);
                                    spinnerSelectSloc.setEnabled(false);
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.check);
                                    lblInboundQty.setText(oPalletInfo.getNoOfBoxesLoaded());
                                    etVolume.setText(oPalletInfo.getLoadedVolume());
                                    etWeight.setText(oPalletInfo.getLoadedWeight());
                                    InboundDTO oInbound = new InboundDTO();
                                    oInbound.setPalletInfoDTO(lstPalletdto);
                                    validateWeightAndVolume(oInbound);

                                }
                                ProgressDialogUtils.closeProgressDialog();

                            }


                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetPalletCurrentLocation_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetPalletCurrentLocation_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetPalletCurrentLocation_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    public void closePallet() {
        if (etPallet.getText().equals("")) {
            // etLocation.setText("");
            spinnerSelectSloc.setEnabled(true);
            common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
            return;

        }

        ClearUI();
        //etLocation.setText("");
        etVolume.setText("");
        etWeight.setText("");
        etTweight.setText("");
        etPallet.setText("");
        etQty.setText("");
        lblInboundQty.setText("");
        isMaxVolumeReached = false;
        isMaxVolumeReached = false;
        palletMaxWeight = 0;
        palletMaxVolume = 0;
        palletLoadedVolume = 0;
        palletLoadedWeight = 0;
        etPallet.setEnabled(false);
        btnConfirmHHLBH.setEnabled(false);
        btnConfirmHHLBH.setTextColor(getResources().getColor(R.color.black));
        btnConfirmHHLBH.setBackgroundResource(R.drawable.button_hide);
        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);
        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanLocation.setImageResource(R.drawable.fullscreen_img);
        cvScanEAN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanEAN.setImageResource(R.drawable.fullscreen_img);
        common.showUserDefinedAlertType(errorMessages.EMC_0035, getActivity(), getContext(), "Success");
    }


    public void UpdateLBH() {

        try {

            if (etLength.getText().toString().equals("") || etLength.getText().toString().equals("0")) {
                common.showUserDefinedAlertType(errorMessages.EMC_0010, getActivity(), getContext(), "Error");
                // txtLength.BackColor = Color.Yellow;
                return;
            }
            if (etBreadth.getText().toString().equals("") || etBreadth.getText().toString().equals("0")) {
                common.showUserDefinedAlertType(errorMessages.EMC_0011, getActivity(), getContext(), "Error");
                // txtBreadth.BackColor = Color.Yellow;
                return;
            }
            if (etHeight.getText().toString().equals("") || etHeight.getText().toString().equals("0")) {
                common.showUserDefinedAlertType(errorMessages.EMC_0012, getActivity(), getContext(), "Error");
                // txtHeight.BackColor = Color.Yellow;
                return;
            }
            if (etWeight.getText().toString().equals("") || etWeight.getText().toString().equals("0")) {
                common.showUserDefinedAlertType(errorMessages.EMC_0013, getActivity(), getContext(), "Error");
                // txtWeight.BackColor = Color.Yellow;
                return;
            }
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            List<DimensionsDTO> lstdimensions = new ArrayList<>();
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setClientID(clientId);
            inboundDTO.setStoreRefNo(lblStoreRefNo.getText().toString());
            DimensionsDTO odimensions = new DimensionsDTO();
            odimensions.setLength(etLength.getText().toString());
            odimensions.setBreadth(etBreadth.getText().toString());
            odimensions.setHeight(etHeight.getText().toString());
            odimensions.setWeight(etWeight.getText().toString());
            lstdimensions.add(odimensions);
            inboundDTO.setDimensionsDTO(lstdimensions);
            inboundDTO.setEANNumber(etEAN.getText().toString());
            inboundDTO.setInboundID(oInboundDataDTO.getInboundID());
            inboundDTO.setBoxQuantity(oInboundDataDTO.getBoxQuantity());
            inboundDTO.setMaterialMasterId(oInboundDataDTO.getMaterialMasterId());
            inboundDTO.setMaterialType(materialType);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.UpdateLBH(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateLBH_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lDimensions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lDimensions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<InboundDTO> lstDto = new ArrayList<>();

                                if (_lDimensions != null) {
                                    InboundDTO oInboundDTO = null;
                                    for (int i = 0; i < _lDimensions.size(); i++) {

                                        oInboundDTO = new InboundDTO(_lDimensions.get(i).entrySet());
                                        lstDto.add(oInboundDTO);

                                    }

                                    common.showUserDefinedAlertType(errorMessages.EMC_0014, getActivity(), getContext(), "Success");
                                    ProgressDialogUtils.closeProgressDialog();
                                    etLength.setEnabled(false);
                                    etBreadth.setEnabled(false);
                                    etHeight.setEnabled(false);
                                    etWeight.setEnabled(false);
                                    btnConfirmHHLBH.setEnabled(false);
                                    btnConfirmHHLBH.setTextColor(getResources().getColor(R.color.black));
                                    btnConfirmHHLBH.setBackgroundResource(R.drawable.button_hide);
                                    validateWeightAndVolume(oInboundDataDTO);

                                }

                            }


                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateLBH_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateLBH_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateLBH_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    //Clearing UI Elements
    private void ClearUI() {
        lblScannedSku.setText("");
        lblDesc.setText("");
        etLength.setText("");
        etBreadth.setText("");
        etHeight.setText("");
        etWeight.setText("");
        etEAN.setText("");
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_goodsIn));
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        storageloc = spinnerSelectSloc.getSelectedItem().toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}