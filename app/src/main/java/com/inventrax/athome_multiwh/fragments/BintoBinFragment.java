package com.inventrax.athome_multiwh.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.inventrax.athome_multiwh.interfaces.ApiInterface;
import com.inventrax.athome_multiwh.pojos.InboundDTO;
import com.inventrax.athome_multiwh.pojos.InternalTransferDTO;
import com.inventrax.athome_multiwh.pojos.InventoryDTO;
import com.inventrax.athome_multiwh.pojos.WMSCoreMessage;
import com.inventrax.athome_multiwh.pojos.WMSExceptionMessage;
import com.inventrax.athome_multiwh.services.RestService;
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

public class BintoBinFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_BIN TO BIN";
    private View rootView;
    private String ipAddress = null, printerIPAddress = null;
    private RelativeLayout rlSelection, rlLoadPallet, rlBinMapping, rlPrint;
    private RadioGroup radioGroup;
    private RadioButton radioLoadPallet, radioBinMapping;
    private TextView lblScannedSku, lblDesc;
    private CardView cvScanSourceBin, cvScanPallet, cvScanRSN, cvScanSourcePallet, cvScanDestPallet, cvScanDestBin;
    private ImageView ivScanSourceBin, ivScanPallet, ivScanRSN, ivScanSourcePallet, ivScanDestPallet, ivScanDestBin;
    private TextInputLayout txtInputLayoutSourceBin, txtInputLayoutPallet, txtInputLayoutCount, txtInputLayoutQty,
            txtInputLayoutSourcePallet, txtInputLayoutDestPallet, txtInputLayoutCountBinMap, txtInputLayoutDestBin,
            txtInputLayoutOldRsn, txtInputLayoutNewRsn, txtInputLayoutQtyPrint, txtInputLayoutPrinterIP;
    private EditText etSourceBin, etPallet, etCount, etQty, etSourcePallet, etDestPallet, etCountBinMap, etDestBin,
            etOldRsn, etNewRsn, etQtyPrint, etPrinterIP;
    private Button btnConfirmLoadPallet, btnClearLoadPallet, btnExportLoadPallet, btnCloseLoadPallet, btnConfirmBinMap,
            btnClearBinMap, btnExportBinMap, btnCloseBinMap, btnPrint, btnClosePrint;

    private Common common = null;
    SoundUtils soundUtils = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    private boolean _isPrintWindowRequired = false, IsResult = false;
    private String _oldRSNNumber = null;
    private double _availableSetQty = 0;
    private double _partialDispatchQty = 0;
    private String userId = null, stRefNo = null, palletType = null, materialType = null;
    private Boolean IsFromLocationScanned = false, IsFromPalletScanned = false, IsRSNScanned = false, IsEANScanned = false, IsValidLocationorPallet = false, IsPalletScanned = false, Isscannedpalletitem = false, IsToPalletScanned = false;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;

    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    public Bundle bundle;

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public BintoBinFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_bintobin, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        rlSelection = (RelativeLayout) rootView.findViewById(R.id.rlSelection);
        rlPrint = (RelativeLayout) rootView.findViewById(R.id.rlPrint);
        rlLoadPallet = (RelativeLayout) rootView.findViewById(R.id.rlLoadPallet);
        rlBinMapping = (RelativeLayout) rootView.findViewById(R.id.rlBinMapping);

        radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup);


        radioBinMapping = (RadioButton) rootView.findViewById(R.id.radioBinMapping);
        radioLoadPallet = (RadioButton) rootView.findViewById(R.id.radioLoadPallet);

        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);
        lblDesc = (TextView) rootView.findViewById(R.id.lblDesc);

        cvScanSourceBin = (CardView) rootView.findViewById(R.id.cvScanSourceBin);
        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanRSN = (CardView) rootView.findViewById(R.id.cvScanRSN);
        cvScanSourcePallet = (CardView) rootView.findViewById(R.id.cvScanSourcePallet);
        cvScanDestPallet = (CardView) rootView.findViewById(R.id.cvScanDestPallet);
        cvScanDestBin = (CardView) rootView.findViewById(R.id.cvScanDestBin);


        ivScanSourceBin = (ImageView) rootView.findViewById(R.id.ivScanSourceBin);
        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanRSN = (ImageView) rootView.findViewById(R.id.ivScanRSN);
        ivScanSourcePallet = (ImageView) rootView.findViewById(R.id.ivScanSourcePallet);
        ivScanDestPallet = (ImageView) rootView.findViewById(R.id.ivScanDestPallet);
        ivScanDestBin = (ImageView) rootView.findViewById(R.id.ivScanDestBin);


        btnConfirmLoadPallet = (Button) rootView.findViewById(R.id.btnConfirmLoadPallet);
        btnClearLoadPallet = (Button) rootView.findViewById(R.id.btnClearLoadPallet);
        btnExportLoadPallet = (Button) rootView.findViewById(R.id.btnExportLoadPallet);
        btnCloseLoadPallet = (Button) rootView.findViewById(R.id.btnCloseLoadPallet);
        btnConfirmBinMap = (Button) rootView.findViewById(R.id.btnConfirmBinMap);
        btnClearBinMap = (Button) rootView.findViewById(R.id.btnClearBinMap);
        btnExportBinMap = (Button) rootView.findViewById(R.id.btnExportBinMap);
        btnCloseBinMap = (Button) rootView.findViewById(R.id.btnCloseBinMap);
        btnPrint = (Button) rootView.findViewById(R.id.btnPrint);
        btnClosePrint = (Button) rootView.findViewById(R.id.btnClosePrint);

        txtInputLayoutSourceBin = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutSourceBin);
        txtInputLayoutPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPallet);
        txtInputLayoutCount = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutCount);
        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);
        txtInputLayoutSourcePallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutSourcePallet);
        txtInputLayoutDestPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutDestPallet);
        txtInputLayoutCountBinMap = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutCountBinMap);
        txtInputLayoutDestBin = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutDestBin);
        txtInputLayoutOldRsn = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutOldRsn);
        txtInputLayoutNewRsn = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutNewRsn);
        txtInputLayoutQtyPrint = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQtyPrint);
        txtInputLayoutPrinterIP = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPrinterIP);

        etSourceBin = (EditText) rootView.findViewById(R.id.etSourceBin);
        etPallet = (EditText) rootView.findViewById(R.id.etPallet);
        etCount = (EditText) rootView.findViewById(R.id.etCount);
        etQty = (EditText) rootView.findViewById(R.id.etQty);
        etSourcePallet = (EditText) rootView.findViewById(R.id.etSourcePallet);
        etDestPallet = (EditText) rootView.findViewById(R.id.etDestPallet);
        etCountBinMap = (EditText) rootView.findViewById(R.id.etCountBinMap);
        etDestBin = (EditText) rootView.findViewById(R.id.etDestBin);
        etOldRsn = (EditText) rootView.findViewById(R.id.etOldRsn);
        etNewRsn = (EditText) rootView.findViewById(R.id.etNewRsn);
        etQtyPrint = (EditText) rootView.findViewById(R.id.etQtyPrint);
        etPrinterIP = (EditText) rootView.findViewById(R.id.etPrinterIP);

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");
        SharedPreferences spPrinterIP = getActivity().getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        ipAddress = spPrinterIP.getString("printerIP", "");
        if (ipAddress != null) {
            etPrinterIP.setText(ipAddress);
        }
        btnConfirmLoadPallet.setOnClickListener(this);
        btnClearLoadPallet.setOnClickListener(this);
        btnExportLoadPallet.setOnClickListener(this);
        btnCloseLoadPallet.setOnClickListener(this);
        btnConfirmBinMap.setOnClickListener(this);
        btnClearBinMap.setOnClickListener(this);
        btnCloseBinMap.setOnClickListener(this);
        btnPrint.setOnClickListener(this);
        btnExportBinMap.setOnClickListener(this);
        btnClosePrint.setOnClickListener(this);

        btnConfirmLoadPallet.setEnabled(false);
        btnConfirmLoadPallet.setTextColor(getResources().getColor(R.color.black));
        btnConfirmLoadPallet.setBackgroundResource(R.drawable.button_hide);

        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();
        soundUtils = new SoundUtils();

        bundle = new Bundle();

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

        // For Honey well
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


        if (getArguments() == null) {

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @SuppressLint("ResourceType")
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    RadioButton rb = (RadioButton) group.findViewById(checkedId);
                    if (null != rb && checkedId > -1) {

                        if (rb.getText() == getString(R.string.bin_mapping)) {

                            rlLoadPallet.setVisibility(View.GONE);
                            rlBinMapping.setVisibility(View.VISIBLE);

                        } else {
                            rlLoadPallet.setVisibility(View.VISIBLE);
                            rlBinMapping.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }


        if (getArguments() != null) {

            Boolean isLoadPallet = getArguments().getBoolean("isLoadPallet");
            Boolean isBinMapping = getArguments().getBoolean("isBinMapping");

            if (isLoadPallet) {

                radioLoadPallet.setChecked(true);
                radioBinMapping.setChecked(false);

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @SuppressLint("ResourceType")
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton rb = (RadioButton) group.findViewById(checkedId);
                        if (null != rb && checkedId > -1) {

                            if (rb.getText() == getString(R.string.bin_mapping)) {

                                rlLoadPallet.setVisibility(View.GONE);
                                rlBinMapping.setVisibility(View.VISIBLE);

                            } else {
                                rlLoadPallet.setVisibility(View.VISIBLE);
                                rlBinMapping.setVisibility(View.GONE);
                            }
                        }
                    }
                });


                etSourceBin.setText(getArguments().getString("sourceBin"));
                etPallet.setText(getArguments().getString("toPallet"));
                IsEANScanned = getArguments().getBoolean("IsEANScanned");
                IsRSNScanned = getArguments().getBoolean("IsRSNScanned");

                IsFromLocationScanned = getArguments().getBoolean("IsFromLocationScanned");
                IsFromPalletScanned = getArguments().getBoolean("IsFromPalletScanned");
                IsToPalletScanned = getArguments().getBoolean("IsToPalletScanned");

                _availableSetQty = Double.parseDouble(getArguments().getString("avialQty"));
                Boolean isBtnConfirm;
                isBtnConfirm = getArguments().getBoolean("isBtnConfirm");
                if (isBtnConfirm) {

                    btnConfirmLoadPallet.setEnabled(true);
                    btnConfirmLoadPallet.setTextColor(getResources().getColor(R.color.white));
                    btnConfirmLoadPallet.setBackgroundResource(R.drawable.button_shape);

                } else {
                    btnConfirmLoadPallet.setEnabled(false);
                    btnConfirmLoadPallet.setTextColor(getResources().getColor(R.color.black));
                    btnConfirmLoadPallet.setBackgroundResource(R.drawable.button_hide);
                }

                if (materialType.equals("HU")) {
                    lblScannedSku.setText(getArguments().getString("RSN"));
                } else {
                    lblScannedSku.setText(getArguments().getString("EAN"));
                }
                etQty.setText(getArguments().getString("qty"));
                etCount.setText(getArguments().getString("count"));

                _oldRSNNumber = getArguments().getString("RSN");

                if (!etPallet.getText().toString().isEmpty()) {

                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                    ivScanPallet.setImageResource(R.drawable.check);
                }
                if (!etSourceBin.getText().toString().isEmpty()) {

                    cvScanSourceBin.setCardBackgroundColor(getResources().getColor(R.color.white));
                    ivScanSourceBin.setImageResource(R.drawable.check);
                }
                if (!lblScannedSku.getText().toString().isEmpty()) {

                    cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                    ivScanRSN.setImageResource(R.drawable.check);
                }

                if (radioLoadPallet.isChecked()) {
                    rlLoadPallet.setVisibility(View.VISIBLE);
                    rlBinMapping.setVisibility(View.GONE);
                    return;
                } else if (radioBinMapping.isChecked()) {
                    rlLoadPallet.setVisibility(View.GONE);
                    rlBinMapping.setVisibility(View.VISIBLE);
                    return;
                }


                return;
            }

            if (isBinMapping) {

                radioLoadPallet.setChecked(false);
                radioBinMapping.setChecked(true);

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @SuppressLint("ResourceType")
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton rb = (RadioButton) group.findViewById(checkedId);
                        if (null != rb && checkedId > -1) {

                            if (rb.getText() == getString(R.string.bin_mapping)) {

                                rlLoadPallet.setVisibility(View.GONE);
                                rlBinMapping.setVisibility(View.VISIBLE);

                            } else {
                                rlLoadPallet.setVisibility(View.VISIBLE);
                                rlBinMapping.setVisibility(View.GONE);
                            }
                        }
                    }
                });

                etSourcePallet.setText(getArguments().getString("sourcePallet"));
                etDestPallet.setText(getArguments().getString("destPallet"));
                etDestBin.setText(getArguments().getString("destBin"));
                etCountBinMap.setText(getArguments().getString("count"));

                if (!etSourcePallet.getText().toString().isEmpty()) {

                    cvScanSourcePallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                    ivScanSourcePallet.setImageResource(R.drawable.check);
                }
                if (!etDestPallet.getText().toString().isEmpty()) {

                    cvScanDestPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                    ivScanDestPallet.setImageResource(R.drawable.check);
                }
                if (!etDestBin.getText().toString().isEmpty()) {

                    cvScanDestBin.setCardBackgroundColor(getResources().getColor(R.color.white));
                    ivScanDestBin.setImageResource(R.drawable.check);
                }

                if (radioLoadPallet.isChecked()) {
                    rlLoadPallet.setVisibility(View.VISIBLE);
                    rlBinMapping.setVisibility(View.GONE);
                    return;
                } else if (radioBinMapping.isChecked()) {
                    rlLoadPallet.setVisibility(View.GONE);
                    rlBinMapping.setVisibility(View.VISIBLE);
                    return;
                }


                return;
            }
        }

        if (radioLoadPallet.isChecked()) {
            rlLoadPallet.setVisibility(View.VISIBLE);
            rlBinMapping.setVisibility(View.GONE);
            return;
        } else if (radioBinMapping.isChecked()) {
            rlLoadPallet.setVisibility(View.GONE);
            rlBinMapping.setVisibility(View.VISIBLE);
            return;
        }


    }

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnCloseBinMap:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnClosePrint:
                goBackToNormalView();
                break;
            case R.id.btnCloseLoadPallet:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnExportLoadPallet:
                if (!etPallet.getText().toString().isEmpty()) {
                    goToLoadPalletExport();
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Warning");
                }
                break;
            case R.id.btnClearLoadPallet:
                loadPalleClearFields();
                break;
            case R.id.btnConfirmLoadPallet:
                ConfirmBinTransferToPallet();
                if (IsResult) {
                    if ((_availableSetQty != _partialDispatchQty) && IsRSNScanned) {
                        _isPrintWindowRequired = true;
                        _oldRSNNumber = lblScannedSku.getText().toString();
                        if (_isPrintWindowRequired) {
                            rlPrint.setVisibility(View.VISIBLE);
                            rlLoadPallet.setVisibility(View.GONE);
                            rlBinMapping.setVisibility(View.GONE);
                            rlSelection.setVisibility(View.GONE);
                            etOldRsn.setText(_oldRSNNumber);
                            GetNewlyGeneratedRSNNumberByRSNNumber();
                            return;
                        }
                    }
                }


                break;
            case R.id.btnConfirmBinMap:
                MapPalletToLocation();
                break;
            case R.id.btnClearBinMap:
                binMappingClearFields();
                break;
            case R.id.btnPrint:
                printValidations();
                break;

            case R.id.btnExportBinMap:
                if (!etDestPallet.getText().toString().isEmpty()) {
                    goToBinMappingExport();
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Warning");
                }
                break;

            default:
                break;
        }
    }

    public void printValidations() {

        if (etNewRsn.getText().toString().isEmpty()) {
            //common.showUserDefinedAlertType(errorMessages.EMC_0027,getActivity(),getContext(),"Error");
            etNewRsn.setFocusable(true);
            return;
        }
        if (etOldRsn.getText().toString().isEmpty()) {
            //common.showUserDefinedAlertType(errorMessages.EMC_0028,getActivity(),getContext(),"Error");
            etOldRsn.setFocusable(true);
            return;
        }
        try {
            if (ipAddress != null) {
                printerIPAddress = ipAddress;
                //printerIPAddress = "192.168.1.73";
            } else {
                common.showUserDefinedAlertType(errorMessages.EMC_0030, getActivity(), getContext(), "Error");
                return;
            }

            // To initiate printer to Print the new RSN Label
            PrintRSNnumber();
        } catch (Exception ex) {
        }

    }

    public void PrintRSNnumber() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.InternalTransferDTO, getContext());
            InternalTransferDTO internalTransferDTO = new InternalTransferDTO();
            internalTransferDTO.setBarcode(etNewRsn.getText().toString());
            internalTransferDTO.setPrinterIP(etPrinterIP.getText().toString());
            internalTransferDTO.setScannedQty(etQtyPrint.getText().toString());
            message.setEntityObject(internalTransferDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.PrintRSNnumber(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrintRSNnumber", getActivity());
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
                                List<InternalTransferDTO> lstPrint = new ArrayList<InternalTransferDTO>();
                                InternalTransferDTO _oInternalTransferDto = null;
                                for (int i = 0; i < _lPrintList.size(); i++) {

                                    _oInternalTransferDto = new InternalTransferDTO(_lPrintList.get(i).entrySet());
                                    lstPrint.add(_oInternalTransferDto);

                                }
                                ProgressDialogUtils.closeProgressDialog();
                                if (_oInternalTransferDto.getStatus()) {

                                    goBackToNormalView();
                                    common.showUserDefinedAlertType(errorMessages.EMC_0049, getActivity(), getContext(), "Success");
                                    return;
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrintRSNnumber", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrintRSNnumber", getActivity());
                logException();

            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
        }
    }

    public void goBackToNormalView() {
        rlSelection.setVisibility(View.VISIBLE);
        radioLoadPallet.setChecked(true);
        rlPrint.setVisibility(View.GONE);
        rlLoadPallet.setVisibility(View.VISIBLE);
        //loadFormControls();

        binMappingClearFields();
        //loadPalleClearFields();
    }


    public void goToLoadPalletExport() {

        bundle.putBoolean("isLoadPallet", true);
        bundle.putString("sourceBin", etSourceBin.getText().toString());
        bundle.putString("toPallet", etPallet.getText().toString());

        bundle.putString("qty", etQty.getText().toString());
        bundle.putString("count", etCount.getText().toString());
        bundle.putString("avialQty", String.valueOf(_availableSetQty));

        bundle.putString("barcode", etPallet.getText().toString());

        if (materialType.equals("HU")) {
            bundle.putString("RSN", lblScannedSku.getText().toString());
        } else {
            bundle.putString("EAN", lblScannedSku.getText().toString());
        }
        bundle.putBoolean("IsRSNScanned", IsRSNScanned);
        bundle.putBoolean("IsEANScanned", IsEANScanned);

        bundle.putBoolean("IsFromLocationScanned", IsFromLocationScanned);
        bundle.putBoolean("IsFromPalletScanned", IsFromPalletScanned);
        bundle.putBoolean("IsToPalletScanned", IsToPalletScanned);
        if (btnConfirmLoadPallet.isEnabled()) {
            bundle.putBoolean("isBtnConfirm", true);
        } else {
            bundle.putBoolean("isBtnConfirm", false);
        }


        PendingBinToBinFragment pendingBinToBinFragment = new PendingBinToBinFragment();
        pendingBinToBinFragment.setArguments(bundle);
        FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, pendingBinToBinFragment);

    }

    public void goToBinMappingExport() {

        bundle.putBoolean("isBinMapping", true);
        bundle.putString("sourcePallet", etSourcePallet.getText().toString());
        bundle.putString("destPallet", etDestPallet.getText().toString());
        bundle.putString("destBin", etDestBin.getText().toString());
        bundle.putString("count", etCountBinMap.getText().toString());

        bundle.putString("barcode", etDestPallet.getText().toString());

        PendingBinToBinFragment pendingBinToBinFragment = new PendingBinToBinFragment();
        pendingBinToBinFragment.setArguments(bundle);
        FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, pendingBinToBinFragment);

    }


    public void loadPalleClearFields() {

        cvScanSourceBin.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanSourceBin.setImageResource(R.drawable.fullscreen_img);

        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.rsnColor));
        ivScanRSN.setImageResource(R.drawable.fullscreen_img);

        lblScannedSku.setText("");
        lblDesc.setText("");

        etSourceBin.setText("");
        etPallet.setText("");
        etCount.setText("");
        etQty.setText("");

    }

    public void binMappingClearFields() {

        cvScanSourcePallet.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanSourcePallet.setImageResource(R.drawable.fullscreen_img);

        cvScanDestPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanDestPallet.setImageResource(R.drawable.fullscreen_img);

        cvScanDestBin.setCardBackgroundColor(getResources().getColor(R.color.rsnColor));
        ivScanDestBin.setImageResource(R.drawable.fullscreen_img);

        etSourcePallet.setText("");
        etDestPallet.setText("");
        etDestBin.setText("");
        etCountBinMap.setText("");

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
        if (scannedData != null && !common.isPopupActive()) {

            if (!ProgressDialogUtils.isProgressActive()) {


                if (rlLoadPallet.getVisibility() == View.VISIBLE) {

                    processScanForLoadPallet(scannedData);
                } else {

                    processScanForBinMapping(scannedData);
                }

            } else {
                if (!common.isPopupActive()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_081, getActivity(), getContext(), "Error");

                }
                soundUtils.alertWarning(getActivity(), getContext());

            }
        }
    }


    public void processScanForLoadPallet(String scannedData) {

        if (scannedData != null && !common.isPopupActive()) {

            if (!ProgressDialogUtils.isProgressActive()) {

                //From Location / FROM PALLET (Here pallet can also be a source bin ie u can scan a pallet as source bin)
                if (etSourceBin.getText().toString().isEmpty())
                {
                    // for handling location
                    if (ScanValidator.IsLocationScanned(scannedData)) {
                        // if user scanned location with L , removing L and consider it as a Location ( 01A01A0L ==> 01A01A0 )
                        if (scannedData.length() == 8) {
                            IsFromLocationScanned = true;
                            etSourceBin.setText(scannedData.substring(0, 7));
                        } else {
                            IsFromLocationScanned = true;
                            etSourceBin.setText(scannedData);
                        }

                        ValidatePalletOrLocation(etSourceBin.getText().toString(), "LOCATION");
                        return;
                    } else if (ScanValidator.IsPalletScanned(scannedData)) {
                        etSourceBin.setText(scannedData);
                        IsPalletScanned = true;
                        Isscannedpalletitem = true;
                        IsFromPalletScanned = true;
                        IsFromLocationScanned = false;
                        ValidatePalletOrLocation(etSourceBin.getText().toString(), "PALLET");
                        return;
                    } else
                        {
                        common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Error");
                        return;
                    }
                }


                //To Pallet
                if (etPallet.getText().toString().isEmpty()) {
                    if (ScanValidator.IsPalletScanned(scannedData)) {
                        etPallet.setText(scannedData);
                        ValidatePalletOrLocation(etPallet.getText().toString(), "PALLET");
                        IsPalletScanned = false;
                        IsToPalletScanned = true;
                        return;

                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                        return;
                    }

                }

                // FOR SCAN VALIDATION OF ARTICLE BARCODE RSN/EAN
                if (!ScanValidator.IsPalletScanned(scannedData) && !ScanValidator.IsLocationScanned(scannedData))
                {
                    if (ScanValidator.IsRSNScanned(scannedData))
                    {
                        lblScannedSku.setText(scannedData);
                        IsRSNScanned = true;
                        IsEANScanned = false;
                        etQty.setText("0");
                        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanRSN.setImageResource(R.drawable.check);
                        ConfirmBinTransferToPallet();

                        //Handling Set Quantity in EAN Barcode
                    } else if (scannedData.split("[,]").length == 2)
                    {


                        lblScannedSku.setText(scannedData.split("[,]")[0]);
                        etQty.setText(scannedData.split("[,]")[1]);
                        btnConfirmLoadPallet.setEnabled(true);
                        btnConfirmLoadPallet.setTextColor(getResources().getColor(R.color.white));
                        btnConfirmLoadPallet.setBackgroundResource(R.drawable.button_shape);

                        common.showUserDefinedAlertType("Please provide qty.", getActivity(), getContext(), "Warning");


                    } else {
                        lblScannedSku.setText(scannedData);
                        etQty.setText("1");

                        IsRSNScanned = false;
                        IsEANScanned = true;

                        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanRSN.setImageResource(R.drawable.check);
                        ConfirmBinTransferToPallet();
                    }


                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0045, getActivity(), getContext(), "Error");
                    return;
                }

            } else {
                if (!common.isPopupActive()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_081, getActivity(), getContext(), "Error");

                }
                soundUtils.alertWarning(getActivity(), getContext());
            }
        } else

        {
            soundUtils.alertWarning(getActivity(), getContext());
        }

    }

    private void ConfirmBinTransferToPallet() {
        try {

            cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
            ivScanRSN.setImageResource(R.drawable.fullscreen_img);

            _partialDispatchQty = Double.parseDouble(etQty.getText().toString());

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO oInventory = new InventoryDTO();
            oInventory.setFromLocation("");
            oInventory.setFromPallet("");
            oInventory.setDestPallet("");

            if (IsFromLocationScanned)
            {
                oInventory.setFromLocation(etSourceBin.getText().toString());

            }

            if (IsFromPalletScanned)
            {
                oInventory.setFromPallet(etSourceBin.getText().toString());
            }

            if (IsToPalletScanned)
            {
                oInventory.setDestPallet(etPallet.getText().toString());
            }

            if (IsRSNScanned)
            {
                oInventory.setRSNBarcode(lblScannedSku.getText().toString());
                oInventory.setEanBarcode("");
            } else {
                oInventory.setRSNBarcode("");
                oInventory.setEanBarcode(lblScannedSku.getText().toString());
            }


            oInventory.setUserId(userId);
            oInventory.setQuantity(etQty.getText().toString());
            message.setEntityObject(oInventory);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.ConfirmBinTransferToPallet(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmBinTransferToPallet", getActivity());
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
                            if ((core.getType().toString().equals("Exception")))
                            {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++)
                                {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanRSN.setImageResource(R.drawable.invalid_cross);
                                return;

                            } else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lInternalTransfer = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInternalTransfer = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                InternalTransferDTO dto = null;
                                for (int i = 0; i < _lInternalTransfer.size(); i++) {
                                    dto = new InternalTransferDTO(_lInternalTransfer.get(i).entrySet());
                                }


                                if (dto.getStatus()) {

                                    etQty.setText("");
                                    etCount.setText(dto.getScannedQty());
                                    btnConfirmLoadPallet.setEnabled(false);
                                    btnConfirmLoadPallet.setTextColor(getResources().getColor(R.color.black));
                                    btnConfirmLoadPallet.setBackgroundResource(R.drawable.button_hide);
                                    lblScannedSku.setText("");
                                    cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanRSN.setImageResource(R.drawable.check);
                                    //common.showUserDefinedAlertType(errorMessages.EMC_0044, getActivity(), getContext(), "Success");
                                    ProgressDialogUtils.closeProgressDialog();

                                } else {
                                    if (dto.getMessage().equals(""))
                                    {
                                        etQty.setText(dto.getSetQuantity());
                                        _availableSetQty = Double.parseDouble(dto.getSetQuantity());
                                        IsResult = true;
                                        common.showUserDefinedAlertType(errorMessages.EMC_0048, getActivity(), getContext(), "Error");
                                        btnConfirmLoadPallet.setEnabled(true);

                                        btnConfirmLoadPallet.setTextColor(getResources().getColor(R.color.white));
                                        btnConfirmLoadPallet.setBackgroundResource(R.drawable.button_shape);
                                    } else {
                                        common.showUserDefinedAlertType(dto.getMessage(), getActivity(), getContext(), "Error");
                                        etQty.setText("");
                                        lblScannedSku.setText("");
                                    }
                                    etSourcePallet.setText("");
                                    Isscannedpalletitem = false;

                                    ProgressDialogUtils.closeProgressDialog();

                                    return;
                                }
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmBinTransferToPallet", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmBinTransferToPallet", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ConfirmBinTransferToPallet", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }

    }

    public void processScanForBinMapping(String scannedData) {

        if (scannedData != null && !common.isPopupActive()) {

            if (!ProgressDialogUtils.isProgressActive())
            {


                // Checking for Source Pallet
                if (etSourcePallet.getText().toString().isEmpty())
                {
                    if (ScanValidator.IsPalletScanned(scannedData)) {
                        etSourcePallet.setText(scannedData);
                        cvScanSourcePallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanSourcePallet.setImageResource(R.drawable.check);
                        GetTempPalletItemCount();
                        return;
                    }else {
                        common.showUserDefinedAlertType("Please scan source pallet", getActivity(), getContext(), "Error");
                        return;
                    }
                }

                // Checking for Destination Pallet
                if (etDestPallet.getText().toString().isEmpty()) {
                    if (ScanValidator.IsPalletScanned(scannedData)) {
                        etDestPallet.setText(scannedData);
                        GetPalletCurrentLocation();
                        return;
                    }else
                    {
                        common.showUserDefinedAlertType("Please scan dest. pallet", getActivity(), getContext(), "Error");
                        return;
                    }
                }

                // Checking for destination palllet to map
                if (!etDestPallet.getText().toString().isEmpty() && etDestBin.getText().toString().isEmpty())
                {
                    if (ScanValidator.IsLocationScanned(scannedData)) {
                        if (scannedData.length() == 8) {
                            etDestBin.setText(scannedData.substring(0, 7));
                            cvScanDestBin.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanDestBin.setImageResource(R.drawable.check);
                            return;
                        } else {
                            etDestBin.setText(scannedData);
                            cvScanDestBin.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanDestBin.setImageResource(R.drawable.check);
                            return;
                        }

                    }else
                    {
                        common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Error");
                        return;
                    }
                }
            } else {
                if (!common.isPopupActive()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_081, getActivity(), getContext(), "Error");

                }
                soundUtils.alertWarning(getActivity(), getContext());
            }
        }
    }

    private void GetPalletCurrentLocation() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO oInventory = new InventoryDTO();
            oInventory.setPalletNumber(etDestPallet.getText().toString());
            message.setEntityObject(oInventory);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.GetInternaltransferPalletCurrentLocation(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetInternaltransferPalletCurrentLocation", getActivity());
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
                                cvScanDestBin.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanDestBin.setImageResource(R.drawable.invalid_cross);
                                cvScanDestPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanDestPallet.setImageResource(R.drawable.invalid_cross);
                                etDestPallet.setText("");

                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());


                            } else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                if(!((LinkedTreeMap) core.getEntityObject()).get("Message").equals("")){
                                    String destinationLoc = (String) ((LinkedTreeMap) core.getEntityObject()).get("Message");
                                    ProgressDialogUtils.closeProgressDialog();
                                    etDestBin.setText(destinationLoc);
                                    etDestBin.setEnabled(false);

                                    cvScanDestBin.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanDestBin.setImageResource(R.drawable.check);
                                    cvScanDestPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanDestPallet.setImageResource(R.drawable.check);
                                }else {
                                    cvScanDestPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanDestPallet.setImageResource(R.drawable.check);
                                }

                                /*List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                InventoryDTO dto = null;
                                for (int i = 0; i < _lInventory.size(); i++) {
                                    dto = new InventoryDTO(_lInventory.get(i).entrySet());
                                }

                                if (dto.getResult() != null && !dto.getResult().equals("")) {
                                    ProgressDialogUtils.closeProgressDialog();
                                    etDestBin.setText(dto.getResult());
                                    etDestBin.setEnabled(false);

                                    cvScanDestBin.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanDestBin.setImageResource(R.drawable.check);
                                    cvScanDestPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanDestPallet.setImageResource(R.drawable.check);

                                } else {
                                    cvScanDestPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanDestPallet.setImageResource(R.drawable.check);
                                }*/
                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetInternaltransferPalletCurrentLocation", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetInternaltransferPalletCurrentLocation", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetInternaltransferPalletCurrentLocation", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }

    }

    private void GetNewlyGeneratedRSNNumberByRSNNumber() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.InternalTransferDTO, getContext());
            InternalTransferDTO otransfer = new InternalTransferDTO();
            otransfer.setBarcode(_oldRSNNumber);
            message.setEntityObject(otransfer);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.GetNewlyGeneratedRSNNumberByRSNNumber(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetNewlyGeneratedRSNNumberByRSNNumber", getActivity());
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

                                InternalTransferDTO dto = null;
                                for (int i = 0; i < _lInventory.size(); i++) {
                                    dto = new InternalTransferDTO(_lInventory.get(i).entrySet());
                                }

                                if (dto.getMessage() != null) {
                                    ProgressDialogUtils.closeProgressDialog();
                                    etNewRsn.setText(dto.getMessage());
                                    etOldRsn.setText(_oldRSNNumber);
                                    etQtyPrint.setText(etQty.getText().toString());

                                    // ConfirmBinTransferToPallet();

                                }
                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetNewlyGeneratedRSNNumberByRSNNumber", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetNewlyGeneratedRSNNumberByRSNNumber", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetNewlyGeneratedRSNNumberByRSNNumber", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }

    }


    private void GetTempPalletItemCount() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO oInventory = new InventoryDTO();
            oInventory.setPalletNumber(etSourcePallet.getText().toString());
            message.setEntityObject(oInventory);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.GetTempPalletItemCount(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetTempPalletItemCount", getActivity());
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

                                InventoryDTO dto = null;
                                for (int i = 0; i < _lInventory.size(); i++) {
                                    dto = new InventoryDTO(_lInventory.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();

                                if (dto.getResult() != null && !dto.getResult().equals("0")) {

                                    etCountBinMap.setText(dto.getResult());
                                } else {
                                    etSourcePallet.setText("");
                                    cvScanSourcePallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanSourcePallet.setImageResource(R.drawable.invalid_cross);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0031, getActivity(), getContext(), "Error");
                                    return;
                                    // }
                                }
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetTempPalletItemCount", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetTempPalletItemCount", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetTempPalletItemCount", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }

    }


    private void ValidatePalletOrLocation(String scannedData, final String ScannedBarcodetype) {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setMaterialType(materialType);
            inboundDTO.setBarcodeType(ScannedBarcodetype);
            inboundDTO.setScannedInput(scannedData);
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.ValidatePalletOrLocation(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ValidatePalletOrLocation", getActivity());
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
                                IsFromLocationScanned = false;
                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                             /*   if(owmsExceptionMessage.getWMSExceptionCode().equals("WMC_PUT_CNTL_006"))
                                {
                                    rlPalletType.setVisibility(View.VISIBLE);
                                    rlPutaway.setVisibility(View.GONE);
                                    rlStRefSelect.setVisibility(View.GONE);
                                }*/
                            } else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lInbound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInbound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                InboundDTO dto = null;
                                for (int i = 0; i < _lInbound.size(); i++) {
                                    dto = new InboundDTO(_lInbound.get(i).entrySet());
                                }

                                if (dto.getResult().toString().equalsIgnoreCase("1")) {
                                    ProgressDialogUtils.closeProgressDialog();
                                    IsValidLocationorPallet = true;


                                } else {
                                    IsValidLocationorPallet = false;
                                }
                                if (ScannedBarcodetype.equals("LOCATION") || IsPalletScanned) {
                                    cvScanSourceBin.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanSourceBin.setImageResource(R.drawable.check);
                                    if (!IsValidLocationorPallet)
                                    {
                                        common.showUserDefinedAlertType(errorMessages.EMC_0038, getActivity(), getContext(), "Error");
                                        ProgressDialogUtils.closeProgressDialog();
                                        etSourceBin.setText("");
                                        cvScanSourceBin.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSourceBin.setImageResource(R.drawable.warning_img);
                                        return;
                                    } else {
                                        // IsFromLocationScanned = true;

                                        // IsFromPalletScanned = false;

                                    }
                                } else {
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.check);
                                    if (!IsValidLocationorPallet) {
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showUserDefinedAlertType(errorMessages.EMC_0031, getActivity(), getContext(), "Error");
                                        etSourceBin.setText("");
                                    } else {

                                        //IsFromLocationScanned = false;
                                        // IsFromPalletScanned = true;

                                    }
                                }

                            }


                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ValidatePalletOrLocation", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ValidatePalletOrLocation", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ValidatePalletOrLocation", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }

    }


    private void MapPalletToLocation() {
        try {
            if (etSourcePallet.getText().toString().isEmpty()) {
                common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                return;
            }
            if (etDestBin.getText().toString().isEmpty()) {
                common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Error");
                return;
            }
            if (etDestPallet.getText().toString().isEmpty()) {
                common.showUserDefinedAlertType(errorMessages.EMC_0047, getActivity(), getContext(), "Error");
                return;
            }
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO oInventory = new InventoryDTO();
            oInventory.setTempPallet(etSourcePallet.getText().toString());
            oInventory.setDestPallet(etDestPallet.getText().toString());
            oInventory.setDestBin(etDestBin.getText().toString());

            oInventory.setUserId(userId);
            message.setEntityObject(oInventory);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.MapPalletToLocation(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "MapPalletToLocation", getActivity());
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
                                //etDestBin.setText("");
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                            } else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                InventoryDTO dto = null;
                                for (int i = 0; i < _lInventory.size(); i++) {
                                    dto = new InventoryDTO(_lInventory.get(i).entrySet());
                                }


                                if (dto.getResult().equals("1")) {
                                    ProgressDialogUtils.closeProgressDialog();
                                    binMappingClearFields();
                                    common.showUserDefinedAlertType(errorMessages.EMC_0044, getActivity(), getContext(), "Success");
                                }

                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "MapPalletToLocation", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "MapPalletToLocation", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "MapPalletToLocation", getActivity());
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_bintobin));
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