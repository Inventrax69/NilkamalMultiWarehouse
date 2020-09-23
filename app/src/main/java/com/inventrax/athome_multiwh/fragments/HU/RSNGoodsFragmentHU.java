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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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


public class RSNGoodsFragmentHU extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener {

    private static final String classCode = "API_FRAG_RSN GOODS HU";
    private View rootView;

    private RelativeLayout rlReceive, rlPrint;
    private TextView lblStoreRefNo, lblInboundQty, lblScannedSku, lblDesc, lblPrintScannedSku, lblPrintSKUDesc;
    private CardView cvScanPallet, cvScanLocation, cvScanSku, cvScanRSN;
    private ImageView ivScanPallet, ivScanLocation, ivScanSku, ivScanRSN;
    private TextInputLayout txtInputLayoutPallet, txtInputLayoutLocation, txtInputLayoutRSN,
            txtInputLayoutLength, txtInputLayoutBreadth, txtInputLayoutHeight, txtInputLayoutWeight, txtInputLayoutBox,
            txtInputLayoutQty, txtInputLayoutVolume, txtInputLayoutTweight, txtInputLayoutCase, txtInputLayoutRSNPrint,
            txtInputLayoutStackCount, txtInputLayoutPrintQty, txtInputLayoutPrinterIP;
    private CustomEditText etPallet, etLocation, etRSN, etLength, etBreadth, etHeight,
            etWeight, etBox, etQty, etVolume, etTweight, etCase,etSloc;

    private EditText etRSNPrint, etStackCount, etPrintQty, etPrinterIP;
    private SearchableSpinner spinnerSelectSloc;
    private Button btnPalletClose, btnConfirmLBH, btnExport, btnPrint, btnClose, btnPrintBarcode, btnClosePrint;
    SoundUtils soundUtils = null;
    FragmentUtils fragmentUtils;
    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    String userId = null;
    String DefaultSloc = null;
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    String storageloc = null, clientId = null, InboundId = null, MaterialMasterId = null, materialType = null;
    ArrayList<String> sloc;
    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private String mCodeWithSupplier = "";


    private boolean isMaxVolumeReached = false;
    private boolean isMaxWeightReached = false;
    private boolean IsPalletLoading = false;
    private boolean IsReceivingBin = false;

    private boolean isRTRBarcodeScanned = false;

    private InboundDTO oInboundDataDTO = null;
    double palletMaxVolume = 0;
    double palletLoadedVolume = 0;
    int palletMaxWeight = 0;
    int palletLoadedWeight = 0;

    public String auditbinLocation = null;

    private String pallet = null, location = null, rsn = null, L = null, B = null, H = null,
            W = null, box = null, qty = null, vol = null, twt = null, caseString = null, sku = null, desc = null, count = null, ipAddress = null, printerIPAddress = null;

    private LinearLayout llRTRBarcodeScan;
    private Button btnSubmit;
    private EditText etHuSize, etHuNum, etQuantity;

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public RSNGoodsFragmentHU() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.hu_fragment_rsn_goods, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        rlReceive = (RelativeLayout) rootView.findViewById(R.id.rlReceive);
        rlPrint = (RelativeLayout) rootView.findViewById(R.id.rlPrint);

        llRTRBarcodeScan = (LinearLayout) rootView.findViewById(R.id.llRTRBarcodeScan);
        llRTRBarcodeScan.setVisibility(View.INVISIBLE);

        lblStoreRefNo = (TextView) rootView.findViewById(R.id.lblStoreRefNo);
        lblInboundQty = (TextView) rootView.findViewById(R.id.lblInboundQty);
        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);
        lblDesc = (TextView) rootView.findViewById(R.id.lblDesc);
        lblPrintScannedSku = (TextView) rootView.findViewById(R.id.lblPrintScannedSku);
        lblPrintSKUDesc = (TextView) rootView.findViewById(R.id.lblPrintSKUDesc);

        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScanSku = (CardView) rootView.findViewById(R.id.cvScanSku);
        cvScanRSN = (CardView) rootView.findViewById(R.id.cvScanRSN);

        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScanSku = (ImageView) rootView.findViewById(R.id.ivScanSku);
        ivScanRSN = (ImageView) rootView.findViewById(R.id.ivScanRSN);

        txtInputLayoutPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPallet);
        txtInputLayoutLocation = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutLocation);
        txtInputLayoutRSN = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutRSN);
        txtInputLayoutLength = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutLength);
        txtInputLayoutBreadth = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutBreadth);
        txtInputLayoutHeight = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutHeight);
        txtInputLayoutWeight = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutWeight);
        txtInputLayoutBox = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutBox);
        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);
        txtInputLayoutVolume = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutVolume);
        txtInputLayoutTweight = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutTweight);
        txtInputLayoutCase = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutCase);
        txtInputLayoutRSNPrint = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutRSNPrint);
        txtInputLayoutStackCount = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutStackCount);
        txtInputLayoutPrintQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPrintQty);
        txtInputLayoutPrinterIP = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPrinterIP);

        etPallet = (CustomEditText) rootView.findViewById(R.id.etPallet);
        etPallet.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //SAVE THE DATA

                } else {
                    if (!etPallet.getText().toString().isEmpty()) {
                        GetPalletinformation();
                    }

                    //GetPalletInfo();
                }

            }
        });
        etLocation = (CustomEditText) rootView.findViewById(R.id.etLocation);
        etRSN = (CustomEditText) rootView.findViewById(R.id.etRSN);
        etLength = (CustomEditText) rootView.findViewById(R.id.etLength);
        etBreadth = (CustomEditText) rootView.findViewById(R.id.etBreadth);
        etHeight = (CustomEditText) rootView.findViewById(R.id.etHeight);
        etWeight = (CustomEditText) rootView.findViewById(R.id.etWeight);
        etBox = (CustomEditText) rootView.findViewById(R.id.etBox);
        etQty = (CustomEditText) rootView.findViewById(R.id.etQty);
        etVolume = (CustomEditText) rootView.findViewById(R.id.etVolume);
        etTweight = (CustomEditText) rootView.findViewById(R.id.etTweight);
        etCase = (CustomEditText) rootView.findViewById(R.id.etCase);
        etSloc = (CustomEditText) rootView.findViewById(R.id.etSloc);

        etHuSize = (CustomEditText) rootView.findViewById(R.id.etHuSize);
        etHuNum = (CustomEditText) rootView.findViewById(R.id.etHuNum);
        etQuantity = (CustomEditText) rootView.findViewById(R.id.etQuantity);

        etRSNPrint = (EditText) rootView.findViewById(R.id.etRSNPrint);
        etStackCount = (EditText) rootView.findViewById(R.id.etStackCount);
        etPrintQty = (EditText) rootView.findViewById(R.id.etPrintQty);
        etPrinterIP = (EditText) rootView.findViewById(R.id.etPrinterIP);


        etRSNPrint.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //SAVE THE DATA

                } else {
                    getPrintDetails();
                }

            }
        });


        spinnerSelectSloc = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectSloc);
        spinnerSelectSloc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                storageloc = spinnerSelectSloc.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnClose = (Button) rootView.findViewById(R.id.btnClose);
        btnPalletClose = (Button) rootView.findViewById(R.id.btnPalletClose);
        btnPrint = (Button) rootView.findViewById(R.id.btnPrint);
        btnConfirmLBH = (Button) rootView.findViewById(R.id.btnConfirmLBH);
        btnExport = (Button) rootView.findViewById(R.id.btnExport);
        btnPrintBarcode = (Button) rootView.findViewById(R.id.btnPrintBarcode);
        btnClosePrint = (Button) rootView.findViewById(R.id.btnClosePrint);
        btnSubmit = (Button) rootView.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(this);

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");

        SharedPreferences spPrinterIP = getActivity().getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        ipAddress = spPrinterIP.getString("printerIP", "");

        btnClose.setOnClickListener(this);
        btnConfirmLBH.setOnClickListener(this);
        btnPalletClose.setOnClickListener(this);
        btnPrint.setOnClickListener(this);
        btnExport.setOnClickListener(this);
        btnPrintBarcode.setOnClickListener(this);
        btnClosePrint.setOnClickListener(this);

        btnConfirmLBH.setTextColor(getResources().getColor(R.color.black));
        btnConfirmLBH.setBackgroundResource(R.drawable.button_hide);

        sloc = new ArrayList<>();

        if (getArguments() != null) {
            etPallet.setEnabled(getArguments().getBoolean("IsPalletEnabled"));
            etLocation.setEnabled(getArguments().getBoolean("IsLocationEnabled"));
            IsReceivingBin = getArguments().getBoolean("IsReceivingBin");

            if (IsReceivingBin) {
                etPallet.requestFocus();
            }

            lblStoreRefNo.setText(getArguments().getString("StoreRefNo"));
            clientId = getArguments().getString("ClientId");
            InboundId = getArguments().getString("InboundId");
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
            count = getArguments().getString("count");


            etPallet.setText(pallet);
            etLocation.setText(location);
            etRSN.setText(rsn);
            etLength.setText(L);
            etBreadth.setText(B);
            etHeight.setText(H);
            etWeight.setText(W);
            etVolume.setText(vol);
            etBox.setText(box);
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
            if (!etRSN.getText().toString().isEmpty()) {

                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                ivScanSku.setImageResource(R.drawable.check);
            }


        }

        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();
        oInboundDataDTO = new InboundDTO();
        soundUtils = new SoundUtils();


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


        /*GetStorageLocations();*/

    }

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnClose:
                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnPalletClose:
                ClosePallet();
                break;
            case R.id.btnPrint:
                if (!lblStoreRefNo.getText().toString().isEmpty()) {
                    etPrinterIP.setText(ipAddress);
                    rlReceive.setVisibility(View.GONE);
                    rlPrint.setVisibility(View.VISIBLE);
                }

                break;
            case R.id.btnExport:
                goToPendingInboundList();
                break;
            case R.id.btnConfirmLBH:

                if (isRTRBarcodeScanned)
                    UpdateSKULBH();
                else
                    UpdateLBH();
                break;

            case R.id.btnSubmit:

                if(!etHuNum.getText().toString().equalsIgnoreCase("") || !etHuNum.getText().toString().equalsIgnoreCase("0")){
                    if( !etHuNum.getText().toString().isEmpty() && Integer.parseInt(etHuNum.getText().toString())!=0 && Integer.parseInt(etHuSize.getText().toString()) >= Integer.parseInt(etHuNum.getText().toString())){
                        if(!etQuantity.getText().toString().isEmpty() && Integer.parseInt(etQuantity.getText().toString())!=0 ){
                            insertSKUDeatilsWithSupplierInvoice();
                        }else {
                            common.showUserDefinedAlertType("Please enter quantity.",getActivity(),getActivity(),"Error");
                        }
                    }else{
                        common.showUserDefinedAlertType("Please enter valid HU number.",getActivity(),getActivity(),"Error");
                    }
                }else {
                    common.showUserDefinedAlertType("Please enter HU number.",getActivity(),getActivity(),"Error");
                }

                break;


            case R.id.btnPrintBarcode:

                printValidation();

                break;

            case R.id.btnClosePrint:

                rlPrint.setVisibility(View.GONE);
                rlReceive.setVisibility(View.VISIBLE);

                break;

            default:
                break;
        }
    }

    public void goToPendingInboundList() {

        Bundle bundle = new Bundle();

        bundle.putString("StoreRefNo", lblStoreRefNo.getText().toString());
        bundle.putString("ClientId", clientId);
        bundle.putString("InboundId", InboundId);


        bundle.putString("pallet", etPallet.getText().toString());
        bundle.putString("location", etLocation.getText().toString());
        bundle.putString("rsn", etRSN.getText().toString());
        bundle.putString("L", etLength.getText().toString());
        bundle.putString("B", etBreadth.getText().toString());
        bundle.putString("H", etHeight.getText().toString());
        bundle.putString("V", etVolume.getText().toString());
        bundle.putString("W", etWeight.getText().toString());
        bundle.putString("box", etBox.getText().toString());
        bundle.putString("qty", etQty.getText().toString());
        bundle.putString("twt", etTweight.getText().toString());
        bundle.putString("caseString", etCase.getText().toString());
        bundle.putString("sku", lblScannedSku.getText().toString());
        bundle.putString("desc", lblDesc.getText().toString());
        bundle.putString("count", lblInboundQty.getText().toString());
        bundle.putBoolean("IsLocationEnabled", etLocation.isEnabled());
        bundle.putBoolean("IsPalletEnabled", etPallet.isEnabled());
        bundle.putBoolean("IsReceivingBin", IsReceivingBin);


        PendingInboundListFragment pendingInboundListFragment = new PendingInboundListFragment();
        pendingInboundListFragment.setArguments(bundle);
        FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, pendingInboundListFragment);
    }


    public void ClearFields() {
        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanLocation.setImageResource(R.drawable.fullscreen_img);

        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanSku.setImageResource(R.drawable.fullscreen_img);

        etTweight.setText("");
        etVolume.setText("");
        etQty.setText("");
        etBreadth.setText("");
        etHeight.setText("");
        etLength.setText("");
        etWeight.setText("");
        etBox.setText("");
        etRSN.setText("");
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
            properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);
            // Apply the settings
            barcodeReader.setProperties(properties);
        }

    }


    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {

        if (!ProgressDialogUtils.isProgressActive()) {

            if (isMaxVolumeReached) {
                common.showUserDefinedAlertType("Pallet Volume :" + palletMaxVolume + " -- Loaded Volumne : " + palletLoadedVolume, getActivity(), getContext(), "Warning");
                return;
            }
            if (isMaxWeightReached) {
                common.showUserDefinedAlertType("Pallet Weight :" + palletMaxWeight + " -- Loaded Weight : " + palletLoadedWeight, getActivity(), getContext(), "Warning");
                return;
            }

            if (scannedData != null && !common.isPopupActive()) {

                // checking for location scan
                if (etLocation.getText().toString().isEmpty()) {

                    if (scanValidator.IsLocationScanned(scannedData)) {
                        etLocation.setText(scannedData.substring(0, 7));
                        GetLocationType();
                        return;
                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Error");
                        return;
                    }
                }

                // Check for Pallet if in case scanned location is receiving bin zone
                if (etPallet.isEnabled()) {
                    if (etPallet.getText().toString().isEmpty()) {
                        if (scanValidator.IsPalletScanned(scannedData)) {
                            etPallet.requestFocus();
                            etPallet.setText(scannedData);
                            etLocation.requestFocus();
                            return;
                        } else {
                            common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                            return;
                        }
                    }
                }

                // Checking For RSN Scan
                if (ScanValidator.IsRSNScanned(scannedData)) {

                    txtInputLayoutRSN.setHint(getString(R.string.hintRSN));
                    llRTRBarcodeScan.setVisibility(View.INVISIBLE);
                    btnSubmit.setVisibility(View.GONE);
                    btnExport.setVisibility(View.VISIBLE);
                    btnConfirmLBH.setVisibility(View.INVISIBLE);
                    etRSN.setText(scannedData);
                    ConfirmReciptOnScan();
                    return;

/*                    if (btnConfirmLBH.isEnabled()) {
                        common.showUserDefinedAlertType(errorMessages.EMC_0008, getActivity(), getContext(), "Warning");
                        btnConfirmLBH.setTextColor(getResources().getColor(R.color.white));
                        btnConfirmLBH.setBackgroundResource(R.drawable.button_shape);
                        etWeight.setEnabled(true);
                        etLength.setEnabled(true);
                        etHeight.setEnabled(true);
                        etBreadth.setEnabled(true);
                        return;

                    } else {
                        etRSN.setText(scannedData);
                        ConfirmReciptOnScan();
                        return;
                    }*/
                } /*else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Error");
                    return;
                }*/

                if (ScanValidator.IsRTRBarcodeScanned(scannedData)) {

                    isRTRBarcodeScanned = true;

                    txtInputLayoutRSN.setHint("Barcode");
                    btnExport.setVisibility(View.GONE);
                    btnSubmit.setVisibility(View.INVISIBLE);
                    llRTRBarcodeScan.setVisibility(View.VISIBLE);
                    btnConfirmLBH.setVisibility(View.INVISIBLE);
                    mCodeWithSupplier = scannedData;
                    etRSN.setText(scannedData.split("_")[0]);
                    etHuNum.setText(scannedData.split("_")[2]);
                    getSKUDeatilsWithSupplierInvoice();

/*                    if (btnConfirmLBH.isEnabled()) {
                        common.showUserDefinedAlertType(errorMessages.EMC_0008, getActivity(), getContext(), "Warning");
                        btnConfirmLBH.setTextColor(getResources().getColor(R.color.white));
                        btnConfirmLBH.setBackgroundResource(R.drawable.button_shape);
                        etWeight.setEnabled(true);
                        etLength.setEnabled(true);
                        etHeight.setEnabled(true);
                        etBreadth.setEnabled(true);

                    } else {
                        mCodeWithSupplier = scannedData;
                        etRSN.setText(scannedData.split("_")[0]);
                        getSKUDeatilsWithSupplierInvoice();

                    }*/

                    return;

                }

                if (ScanValidator.IsBatchRSN(scannedData)) {
                    PrintEcomLabelsForFurniture(scannedData);
                    return;
                } else {
                    soundUtils.alertWarning(getActivity(), getContext());
                }
            } else {
                soundUtils.alertWarning(getActivity(), getContext());

            }


        } else {
            if (!common.isPopupActive()) {
                common.showUserDefinedAlertType(errorMessages.EMC_081, getActivity(), getContext(), "Error");

            }
            soundUtils.alertWarning(getActivity(), getContext());

        }

    }


    public void GetPalletinformation() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setClientID(clientId);
            inboundDTO.setPalletNo(etPallet.getText().toString());
            inboundDTO.setLocation(etLocation.getText().toString());
            inboundDTO.setInboundID(InboundId);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

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
                                ProgressDialogUtils.closeProgressDialog();

                                if (owmsExceptionMessage.getWMSExceptionCode().equals("WMC_PUT_CNTL_004")) {
                                    auditbinLocation = owmsExceptionMessage.getWMSMessage().split("[{}]")[1].toString();
                                    /*
                                     * if Pallet Existed Logically in Some location and physically empty, confirming user to
                                     * move the Stock to Audit Bin if USer say Yes Call MoveStockToAuditBin service to Move the stock to audit bin zone so that
                                     * this pallet can be used further
                                     */
                                    DialogUtils.showConfirmDialog(getActivity(), "Confirm Remove",
                                            "Pallet is not empty, Do you want to move stock to Audit Bin ?", new DialogInterface.OnClickListener() {

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
                                    lblInboundQty.setText(oPalletInfo.getNoOfBoxesLoaded());
                                    etVolume.setText(oPalletInfo.getLoadedVolume());
                                    etWeight.setText(oPalletInfo.getLoadedWeight());
                                    InboundDTO oInbound = new InboundDTO();
                                    oInbound.setPalletInfoDTO(lstPalletdto);
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.check);
                                    ValidateWeightAndVolume(oInbound);

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
                        etPallet.setText("");
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


    public void ClosePallet() {

        if (etPallet.getText().toString().isEmpty()) {
            spinnerSelectSloc.setEnabled(true);
            etLocation.setText("");
            cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
            ivScanLocation.setImageResource(R.drawable.fullscreen_img);
        }

        ClearUI();

        etBox.setText("");
        etPallet.setText("");
        etPallet.setEnabled(true);
        etPallet.requestFocus();

        etVolume.setText("");
        etWeight.setText("");
        etTweight.setText("");

        btnConfirmLBH.setEnabled(false);

        etQty.setText("");
        lblInboundQty.setText("");
        isMaxVolumeReached = false;
        isMaxVolumeReached = false;
        palletMaxWeight = 0;
        palletMaxVolume = 0;
        palletLoadedVolume = 0;
        palletLoadedWeight = 0;


        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanSku.setImageResource(R.drawable.fullscreen_img);

        btnConfirmLBH.setTextColor(getResources().getColor(R.color.black));
        btnConfirmLBH.setBackgroundResource(R.drawable.button_hide);


    }

    public void getPrintDetails() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUniqueRSN(etRSNPrint.getText().toString());
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetRSNInfo(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetRSNInfo_01", getActivity());
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
                        core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                        if (core != null) {
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanRSN.setImageResource(R.drawable.warning_img);
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    return;
                                }
                            } else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                                List<LinkedTreeMap<?, ?>> _lPrintList = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lPrintList = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                List<InboundDTO> lstInventory = new ArrayList<InboundDTO>();
                                InboundDTO inboundDTO = null;


                                for (int i = 0; i < _lPrintList.size(); i++) {

                                    inboundDTO = new InboundDTO(_lPrintList.get(i).entrySet());
                                    lstInventory.add(inboundDTO);

                                }

                                ProgressDialogUtils.closeProgressDialog();

                                if (inboundDTO != null) {
                                    if (inboundDTO.getHUsize() != null) {
                                        if (inboundDTO.getHUsize().equals("1")) {
                                            if (!etRSNPrint.getText().toString().isEmpty()) {
                                                cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                                ivScanRSN.setImageResource(R.drawable.check);
                                            }

                                            lblPrintScannedSku.setText(inboundDTO.getMaterialCode());
                                            lblPrintSKUDesc.setText(inboundDTO.getmDesc());
                                        } else {

                                            etRSNPrint.setFocusable(true);
                                            etRSNPrint.setText("");

                                            common.showUserDefinedAlertType(errorMessages.EMC_0045, getActivity(), getContext(), "Warning");

                                            return;
                                        }

                                    } else {
                                        if (etRSNPrint.getText().toString() != "") {
                                            common.showUserDefinedAlertType(errorMessages.EMC_0045, getActivity(), getContext(), "Warning");

                                            cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanRSN.setImageResource(R.drawable.check);

                                            etRSNPrint.setFocusable(true);
                                            etRSNPrint.setText("");

                                            return;
                                        }
                                    }
                                }
                            }
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
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetRSNInfo_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetRSNInfo_03", getActivity());
                logException();

            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
        }
    }

    public void printValidation() {

        if (etRSNPrint.getText().toString().isEmpty()) {
            common.showUserDefinedAlertType(errorMessages.EMC_0027, getActivity(), getContext(), "Error");
            etRSN.setFocusable(true);
            return;
        }

        if (etStackCount.getText().toString().isEmpty()) {
            common.showUserDefinedAlertType(errorMessages.EMC_0028, getActivity(), getContext(), "Error");
            etStackCount.setFocusable(true);
            return;
        }

        if (etPrintQty.getText().toString().isEmpty()) {
            common.showUserDefinedAlertType(errorMessages.EMC_0029, getActivity(), getContext(), "Error");
            etPrintQty.setFocusable(true);
            return;
        }

        try {

            if (ipAddress != null) {
                //PrinterIPAddress = ipAddress;
                printerIPAddress = ipAddress;
            } else {
                common.showUserDefinedAlertType(errorMessages.EMC_0030, getActivity(), getContext(), "Error");
                return;
            }
            printModule();
        } catch (Exception ex) {
        }
    }

    public void printModule() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUniqueRSN(etRSNPrint.getText().toString());
            inboundDTO.setStackCount(etStackCount.getText().toString());
            inboundDTO.setPrinyQty(etPrintQty.getText().toString());
            inboundDTO.setIpAddress(etPrinterIP.getText().toString());
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.PrintMouldedFurnitureLable(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrintMouldedFurnitureLable_01", getActivity());
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
                        core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                        if (core != null) {
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanRSN.setImageResource(R.drawable.warning_img);
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    return;
                                }
                            } else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                                List<LinkedTreeMap<?, ?>> _lPrintList = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lPrintList = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                List<InboundDTO> lstInventory = new ArrayList<InboundDTO>();
                                InboundDTO inboundDTO = null;

                                for (int i = 0; i < _lPrintList.size(); i++) {
                                    inboundDTO = new InboundDTO(_lPrintList.get(i).entrySet());
                                    lstInventory.add(inboundDTO);
                                }

                                ProgressDialogUtils.closeProgressDialog();
                                common.showUserDefinedAlertType(errorMessages.EMC_0049, getActivity(), getContext(), "Success");


                            }
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
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrintMouldedFurnitureLable_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrintMouldedFurnitureLable_03", getActivity());
                logException();

            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
        }
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
            inboundDTO.setUniqueRSN(etRSN.getText().toString());
            inboundDTO.setInboundID(oInboundDataDTO.getInboundID());
            inboundDTO.setHUNumber(etBox.getText().toString().split("[/]")[0]);
            inboundDTO.setHUsize(etBox.getText().toString().split("[/]")[1]);
            inboundDTO.setBoxQuantity(oInboundDataDTO.getBoxQuantity());
            inboundDTO.setMaterialMasterId(oInboundDataDTO.getMaterialMasterId());
            inboundDTO.setMaterialType(materialType);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

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
                                    btnConfirmLBH.setEnabled(false);
                                    btnConfirmLBH.setTextColor(getResources().getColor(R.color.black));
                                    btnConfirmLBH.setBackgroundResource(R.drawable.button_hide);
                                    ValidateWeightAndVolume(oInboundDataDTO);

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

    public void UpdateSKULBH() {

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
            inboundDTO.setMaterialCode(mCodeWithSupplier);
            inboundDTO.setInboundID(oInboundDataDTO.getInboundID());
            inboundDTO.setHUNumber(etBox.getText().toString().split("[/]")[0]);
            inboundDTO.setHUsize(etBox.getText().toString().split("[/]")[1]);
            inboundDTO.setBoxQuantity(oInboundDataDTO.getBoxQuantity());
            inboundDTO.setMaterialMasterId(oInboundDataDTO.getMaterialMasterId());
            inboundDTO.setMaterialType(materialType);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.UpdateSKULBH(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateSKULBH_01", getActivity());
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
                                    btnConfirmLBH.setEnabled(false);
                                    btnConfirmLBH.setTextColor(getResources().getColor(R.color.black));
                                    btnConfirmLBH.setBackgroundResource(R.drawable.button_hide);
                                    ValidateWeightAndVolume(oInboundDataDTO);

                                }

                            }


                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateSKULBH_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateSKULBH_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateSKULBH_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    public void GetStorageLocations() {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setClientID(clientId);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

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
                                        DefaultSloc = lstDto.get(i).getSLOCcode();

                                    }
                                }

                                ProgressDialogUtils.closeProgressDialog();
                                ArrayAdapter arrayAdapterSLoc = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstStorageLoc);
                                spinnerSelectSloc.setAdapter(arrayAdapterSLoc);
                                int spinnerPosition = arrayAdapterSLoc.getPosition(DefaultSloc);
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

    public void ValidateWeightAndVolume(InboundDTO oInboundDTO) {

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

    public void ConfirmReciptOnScan() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setClientID(clientId);
            inboundDTO.setLocation(etLocation.getText().toString());
            inboundDTO.setUniqueRSN(etRSN.getText().toString());
            inboundDTO.setPalletNo(etPallet.getText().toString());
           // inboundDTO.setSelectedStorageLocation(storageloc);
            inboundDTO.setSelectedStorageLocation("");
            inboundDTO.setStoreRefNo(lblStoreRefNo.getText().toString());
            inboundDTO.setItemSerialNo(etCase.getText().toString());
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.ConfirmReceiptOnUniqueRSNScan(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmReceiptOnUniqueRSNScan_01", getActivity());
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
                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanSku.setImageResource(R.drawable.warning_img);
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
                                etBox.setText(oInboundData.getHUNumber() + "/" + oInboundData.getHUsize());
                                etQty.setText(oInboundData.getBoxQuantity());
                                etLength.setText(oInboundData.getDimensionsDTO().get(0).getLength());
                                etBreadth.setText(oInboundData.getDimensionsDTO().get(0).getBreadth());
                                etHeight.setText(oInboundData.getDimensionsDTO().get(0).getHeight());
                                etWeight.setText(oInboundData.getDimensionsDTO().get(0).getWeight());
                                lblInboundQty.setText(oInboundData.getPalletInfoDTO().get(0).getNoOfBoxesLoaded());
                                etVolume.setText(oInboundData.getPalletInfoDTO().get(0).getLoadedVolume());
                                etTweight.setText(oInboundData.getPalletInfoDTO().get(0).getLoadedWeight());
                                MaterialMasterId = oInboundData.getMaterialMasterId();
                                etCase.setText(oInboundData.getSerialNo());
                                etSloc.setText(oInboundData.getSelectedStorageLocation());

                                if (oInboundData.getDimensionsDTO().get(0).getLength().equals("0") || oInboundData.getDimensionsDTO().get(0).getLength().equalsIgnoreCase("0.00")) {
                                    etLength.setEnabled(true);
                                    etBreadth.setEnabled(true);
                                    etHeight.setEnabled(true);
                                    etWeight.setEnabled(true);
                                    btnConfirmLBH.setEnabled(true);
                                    btnConfirmLBH.setTextColor(getResources().getColor(R.color.white));
                                    btnConfirmLBH.setBackgroundResource(R.drawable.button_shape);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0008_01, getActivity(), getContext(), "Warning");
                                } else {
                                    etLength.setEnabled(false);
                                    etBreadth.setEnabled(false);
                                    etHeight.setEnabled(false);
                                    etWeight.setEnabled(false);
                                    btnConfirmLBH.setEnabled(false);
                                    btnConfirmLBH.setTextColor(getResources().getColor(R.color.black));
                                    btnConfirmLBH.setBackgroundResource(R.drawable.button_hide);

                                    try {
                                        ValidateWeightAndVolume(oInboundData);
                                    } catch (Exception ex) {
                                        isMaxVolumeReached = false;
                                        isMaxWeightReached = false;
                                    }
                                }


                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanSku.setImageResource(R.drawable.check);
                                ProgressDialogUtils.closeProgressDialog();

                            }


                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmReceiptOnUniqueRSNScan_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmReceiptOnUniqueRSNScan_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmReceiptOnUniqueRSNScan_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    public void insertSKUDeatilsWithSupplierInvoice() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO = oInboundDataDTO;
            inboundDTO.setUserId(userId);
            inboundDTO.setClientID(clientId);
            inboundDTO.setLocation(etLocation.getText().toString());
            inboundDTO.setMaterialCode(mCodeWithSupplier);
            inboundDTO.setPalletNo(etPallet.getText().toString());
           // inboundDTO.setSelectedStorageLocation(storageloc);
            inboundDTO.setSelectedStorageLocation(etSloc.getText().toString());
            inboundDTO.setStoreRefNo(lblStoreRefNo.getText().toString());
            inboundDTO.setItemSerialNo(etCase.getText().toString());
            inboundDTO.setHUNumber(etHuNum.getText().toString());
            inboundDTO.setReceivedQty("1");
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.insertSKUDeatilsWithSupplierInvoice(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "insertSKUDeatilsWithSupplierInvoice_01", getActivity());
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
                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanSku.setImageResource(R.drawable.warning_img);
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
                                etBox.setText(oInboundData.getHUNumber() + "/" + oInboundData.getHUsize());
                                etQty.setText(oInboundData.getBoxQuantity());
                                etLength.setText(oInboundData.getDimensionsDTO().get(0).getLength());
                                etBreadth.setText(oInboundData.getDimensionsDTO().get(0).getBreadth());
                                etHeight.setText(oInboundData.getDimensionsDTO().get(0).getHeight());
                                etWeight.setText(oInboundData.getDimensionsDTO().get(0).getWeight());
                                lblInboundQty.setText(oInboundData.getPalletInfoDTO().get(0).getNoOfBoxesLoaded());
                                etVolume.setText(oInboundData.getPalletInfoDTO().get(0).getLoadedVolume());
                                etTweight.setText(oInboundData.getPalletInfoDTO().get(0).getLoadedWeight());
                                MaterialMasterId = oInboundData.getMaterialMasterId();
                                etCase.setText(oInboundData.getSerialNo());
                                etHuNum.setText(oInboundData.getHUNumber());
                                etHuSize.setText(oInboundData.getHUsize());
                                etSloc.setText(oInboundData.getSelectedStorageLocation());

                                if (oInboundData.getDimensionsDTO().get(0).getLength().equals("0") || oInboundData.getDimensionsDTO().get(0).getLength().equalsIgnoreCase("0.00")) {
                                    etLength.setEnabled(true);
                                    etBreadth.setEnabled(true);
                                    etHeight.setEnabled(true);
                                    etWeight.setEnabled(true);
                                    btnConfirmLBH.setEnabled(true);
                                    btnConfirmLBH.setTextColor(getResources().getColor(R.color.white));
                                    btnConfirmLBH.setBackgroundResource(R.drawable.button_shape);
                                } else {
                                    etLength.setEnabled(false);
                                    etBreadth.setEnabled(false);
                                    etHeight.setEnabled(false);
                                    etWeight.setEnabled(false);
                                    btnConfirmLBH.setEnabled(false);
                                    btnConfirmLBH.setTextColor(getResources().getColor(R.color.black));
                                    btnConfirmLBH.setBackgroundResource(R.drawable.button_hide);
                                    try {
                                        ValidateWeightAndVolume(oInboundData);
                                    } catch (Exception ex) {
                                        isMaxVolumeReached = false;
                                        isMaxWeightReached = false;
                                    }
                                }

                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanSku.setImageResource(R.drawable.check);
                                ProgressDialogUtils.closeProgressDialog();

                            }


                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "insertSKUDeatilsWithSupplierInvoice_02", getActivity());
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
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "insertSKUDeatilsWithSupplierInvoice_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "insertSKUDeatilsWithSupplierInvoice_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    public void getSKUDeatilsWithSupplierInvoice() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setClientID(clientId);
            inboundDTO.setLocation(etLocation.getText().toString());
            inboundDTO.setPalletNo(etPallet.getText().toString());
            //inboundDTO.setSelectedStorageLocation(storageloc);
            inboundDTO.setSelectedStorageLocation("");
            inboundDTO.setStoreRefNo(lblStoreRefNo.getText().toString());
            inboundDTO.setItemSerialNo(etCase.getText().toString());
            inboundDTO.setMaterialCode(mCodeWithSupplier);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.getSKUDeatilsWithSupplierInvoice(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "getSKUDeatilsWithSupplierInvoice_01", getActivity());
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

                            Log.v("SKUDeatils",new Gson().toJson(core));

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());


                                }
                                ProgressDialogUtils.closeProgressDialog();


                                txtInputLayoutRSN.setHint(getString(R.string.hintRSN));
                                llRTRBarcodeScan.setVisibility(View.INVISIBLE);
                                btnSubmit.setVisibility(View.GONE);
                                btnExport.setVisibility(View.VISIBLE);

                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanSku.setImageResource(R.drawable.warning_img);
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

                                lblScannedSku.setText("SKU: " + oInboundData.getMaterialCode().split("_")[0] + "|" + "Batch: " + oInboundData.getBatchNo());
                                lblDesc.setText("Desc. : " + oInboundData.getmDesc());
                                etBox.setText(oInboundData.getHUNumber() + "/" + oInboundData.getHUsize());
                                etQty.setText(oInboundData.getBoxQuantity());
                                etLength.setText(oInboundData.getDimensionsDTO().get(0).getLength());
                                etBreadth.setText(oInboundData.getDimensionsDTO().get(0).getBreadth());
                                etHeight.setText(oInboundData.getDimensionsDTO().get(0).getHeight());
                                etWeight.setText(oInboundData.getDimensionsDTO().get(0).getWeight());
                                MaterialMasterId = oInboundData.getMaterialMasterId();
                                etCase.setText(oInboundData.getSerialNo());
                                etHuSize.setText(oInboundData.getHUsize());
                               // etHuNum.setText(oInboundData.getHUNumber());
                                ProgressDialogUtils.closeProgressDialog();
                                if (oInboundData.getDimensionsDTO().get(0).getLength().equals("0") || oInboundData.getDimensionsDTO().get(0).getLength().equalsIgnoreCase("0.00")) {
                                    etLength.setEnabled(true);
                                    etBreadth.setEnabled(true);
                                    etHeight.setEnabled(true);
                                    etWeight.setEnabled(true);
                                    btnConfirmLBH.setEnabled(true);
                                    btnConfirmLBH.setTextColor(getResources().getColor(R.color.white));
                                    btnConfirmLBH.setBackgroundResource(R.drawable.button_shape);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0008_01, getActivity(), getContext(), "Warning");
                                } else {
                                    etLength.setEnabled(false);
                                    etBreadth.setEnabled(false);
                                    etHeight.setEnabled(false);
                                    etWeight.setEnabled(false);
                                    btnConfirmLBH.setEnabled(false);
                                    btnConfirmLBH.setTextColor(getResources().getColor(R.color.black));
                                    btnConfirmLBH.setBackgroundResource(R.drawable.button_hide);
                                    insertSKUDeatilsWithSupplierInvoice();
                                }

                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanSku.setImageResource(R.drawable.check);


                            }


                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "getSKUDeatilsWithSupplierInvoice_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "getSKUDeatilsWithSupplierInvoice_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "getSKUDeatilsWithSupplierInvoice_04", getActivity());
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
                            ProgressDialogUtils.closeProgressDialog();
                            if (response.body() != null) {

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
                                        common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Error");
                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanLocation.setImageResource(R.drawable.warning_img);
                                        etLocation.setText("");
                                        etPallet.setEnabled(false);
                                        ProgressDialogUtils.closeProgressDialog();
                                        return;
                                    }
                                    if (oInventoryDTO.getLocationTypeID().equals("7")) {
                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanLocation.setImageResource(R.drawable.check);
                                        IsPalletLoading = true;
                                        etPallet.setEnabled(true);
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


                                    }
                                    ProgressDialogUtils.closeProgressDialog();
                                }
                            } else {
                                etLocation.setText("");
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

    private void PrintEcomLabelsForFurniture(String scannedData) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setSKU(scannedData.split("[,]")[0]);
            inboundDTO.setStoreRefNo(lblStoreRefNo.getText().toString());
            inboundDTO.setIpAddress(ipAddress);
            if (scannedData.split("[,]")[2].split("[.]").length == 3) {
                inboundDTO.setBoxQuantity("1");
            } else {
                inboundDTO.setBoxQuantity(scannedData.split("[,]")[2]);
            }
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.PrintEcomLabelsForFurniture(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
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

                                InboundDTO inboundDTO1 = null;
                                for (int i = 0; i < _lInbound.size(); i++) {
                                    inboundDTO1 = new InboundDTO(_lInbound.get(i).entrySet());
                                }

                                if (inboundDTO1.getResult().equals("Success")) {
                                    // TODO Nothing
                                } else {
                                    common.showUserDefinedAlertType("Print Failed", getActivity(), getContext(), "Error");
                                }

                                ProgressDialogUtils.closeProgressDialog();

                            }

                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
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
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_04", getActivity());
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
        etBox.setText("");
        lblDesc.setText("");
        etLength.setText("");
        etBreadth.setText("");
        etHeight.setText("");
        etWeight.setText("");
        etRSN.setText("");
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
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

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
                                    common.showUserDefinedAlertType(errorMessages.EMC_0067, getActivity(), getContext(), "Success");
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_rsngooods));
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

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}