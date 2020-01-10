package com.inventrax.athome_multiwh.fragments.HU;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.ToneGenerator;
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
import com.inventrax.athome_multiwh.pojos.InternalTransferDTO;
import com.inventrax.athome_multiwh.pojos.VlpdDto;
import com.inventrax.athome_multiwh.pojos.WMSCoreMessage;
import com.inventrax.athome_multiwh.pojos.WMSExceptionMessage;
import com.inventrax.athome_multiwh.services.RestService;
import com.inventrax.athome_multiwh.util.CustomEditText;
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

public class MapPalletDockLoc extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_MAP PALLET TO DOCK";
    private View rootView;

    private RelativeLayout rlVLPDEnter, rlMap;
    private TextView lblSuggestedDoc;
    private CardView cvScanDockLocation, cvScanPallet;
    private ImageView ivScanDockLocation, ivScanPallet;
    private TextInputLayout txtInputLayoutPallet, txtInputLayoutVLPD, txtInputLayoutDock;
    private CustomEditText etPallet, etVLPD, etDock;
    private Button btnOk, btnClear, btnExport, btnClose;

    FragmentUtils fragmentUtils;
    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    String userId = null;
    private SoundUtils soundUtils;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    String storageloc = null, clientId = null;
    ArrayList<String> sloc;
    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    ToneGenerator toneGenerator;
    private ErrorMessages errorMessages;

    String vlpdDockLocation = null;

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };


    public MapPalletDockLoc() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.hu_map_pallet_doclocation, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        rlVLPDEnter = (RelativeLayout) rootView.findViewById(R.id.rlVLPDEnter);
        rlMap = (RelativeLayout) rootView.findViewById(R.id.rlMap);

        lblSuggestedDoc = (TextView) rootView.findViewById(R.id.lblSuggestedDoc);

        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanDockLocation = (CardView) rootView.findViewById(R.id.cvScanDockLocation);

        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanDockLocation = (ImageView) rootView.findViewById(R.id.ivScanDockLocation);

        txtInputLayoutPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPallet);
        txtInputLayoutVLPD = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutVLPD);
        txtInputLayoutDock = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutDock);

        etPallet = (CustomEditText) rootView.findViewById(R.id.etPallet);
        etVLPD = (CustomEditText) rootView.findViewById(R.id.etVLPD);
        etDock = (CustomEditText) rootView.findViewById(R.id.etDock);

        btnClose = (Button) rootView.findViewById(R.id.btnClose);
        btnOk = (Button) rootView.findViewById(R.id.btnOk);
        btnClear = (Button) rootView.findViewById(R.id.btnClear);
        btnExport = (Button) rootView.findViewById(R.id.btnExport);

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");

        btnClose.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnExport.setOnClickListener(this);

        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();
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

        rlMap.setVisibility(View.GONE);

        txtInputLayoutVLPD.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                } else {
                    if (!etVLPD.getText().toString().isEmpty()) {

                        GetVLPDDockLocation();
                    }
                }
            }
        });

        if (getArguments() != null) {

            etVLPD.setText(getArguments().getString("vlpdNo"));
            lblSuggestedDoc.setText(getArguments().getString("SuggestedDoc"));
            etDock.setText(getArguments().getString("Dock"));
            etPallet.setText(getArguments().getString("pallet"));

            rlMap.setVisibility(View.VISIBLE);

            if (!etPallet.getText().toString().isEmpty()) {
                cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                ivScanPallet.setImageResource(R.drawable.check);
            }
            if (!etDock.getText().toString().isEmpty()) {

                lblSuggestedDoc.setText(etDock.getText().toString());

                cvScanDockLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                ivScanDockLocation.setImageResource(R.drawable.check);
            }
        }

    }

    private void GetVLPDDockLocation() {


        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setvLPDNumber(etVLPD.getText().toString());
            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);
            try {
                call = apiService.GetVLPDDockLocation(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetVLPDDockLocation_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lGetLocation = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lGetLocation = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                VlpdDto dto = null;
                                for (int i = 0; i < _lGetLocation.size(); i++) {
                                    dto = new VlpdDto(_lGetLocation.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();

                                if (dto.getResult() != null && dto.getResult() != "") {
                                    if (dto.getResult().toString().length() != 7) {

                                        common.showUserDefinedAlertType(errorMessages.EMC_034, getActivity(), getContext(), "Error");
                                        return;
                                    } else {
                                        vlpdDockLocation = dto.getResult().toString();
                                        lblSuggestedDoc.setText(dto.getResult().toString());
                                        rlMap.setVisibility(View.VISIBLE);
                                    }

                                } else {
                                    common.showUserDefinedAlertType(errorMessages.EMC_034, getActivity(), getContext(), "Error");
                                }
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetVLPDDockLocation_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                //e.printStackTrace();
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetVLPDDockLocation_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetVLPDDockLocation_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }


    }

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnOk:

                if (!etVLPD.getText().toString().isEmpty()) {

                    GetVLPDDockLocation();
                    ClearFields();

                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0039, getActivity(), getContext(), "Error");
                }
                break;
            case R.id.btnClear:

                ClearFields();
                rlMap.setVisibility(View.GONE);
                break;


            case R.id.btnExport:

                if (!etVLPD.getText().toString().isEmpty()) {
                    goToExport();
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0039, getActivity(), getContext(), "Error");
                }

                break;


            default:
                break;
        }
    }


    public void goToExport() {
        Bundle bundle = new Bundle();
        bundle.putString("vlpdNo", etVLPD.getText().toString());
        bundle.putString("Dock", etDock.getText().toString());
        bundle.putString("pallet", etPallet.getText().toString());
        bundle.putString("SuggestedDoc", lblSuggestedDoc.getText().toString());

        PendingMapPalletToDocListFragment pendingMapPalletToDocListFragment = new PendingMapPalletToDocListFragment();
        pendingMapPalletToDocListFragment.setArguments(bundle);
        FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, pendingMapPalletToDocListFragment);

    }

    public void ClearFields() {
        cvScanDockLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanDockLocation.setImageResource(R.drawable.fullscreen_img);

        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);


        etDock.setText("");
        etPallet.setText("");
        //etVLPD.setText("");
        lblSuggestedDoc.setText("");

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
            properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED,true);
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

                if (ScanValidator.IsLocationScanned(scannedData)) {

                    // if user scanned location with L , removing L and consider it as a Location ( 01A01A0L ==> 01A01A0 )
                    if (scannedData.length() == 8) {
                        if (vlpdDockLocation != scannedData.substring(0, 7)) {
                            common.showUserDefinedAlertType(errorMessages.EMC_035, getActivity(), getContext(), "Error");
                            etDock.setText("");
                            return;
                        } else {
                            etDock.setText(scannedData.substring(0, 7));
                            cvScanDockLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanDockLocation.setImageResource(R.drawable.check);
                        }
                    } else {
                        if (!lblSuggestedDoc.getText().toString().equals(scannedData)) {
                            common.showUserDefinedAlertType(errorMessages.EMC_035, getActivity(), getContext(), "Error");

                            return;
                        } else {
                            etDock.setText(scannedData.substring(0, 7));
                            cvScanDockLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanDockLocation.setImageResource(R.drawable.check);
                        }
                    }

                } else if (!etDock.getText().toString().isEmpty()) {
                    if (scanValidator.IsPalletScanned(scannedData.trim())) {
                        if (txtInputLayoutVLPD.getVisibility() == View.VISIBLE) {
                            etPallet.setText(scannedData);
                            ValidatePalletAtVLPDDock(scannedData);
                            return;
                        }
                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0017, getActivity(), getContext(), "Error");
                    }
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Error");
                }
            }else {
                if(!common.isPopupActive())
                {
                    common.showUserDefinedAlertType(errorMessages.EMC_081, getActivity(), getContext(), "Error");

                }
                soundUtils.alertWarning(getActivity(),getContext());
            }
        }

    }

    private void ValidatePalletAtVLPDDock(String scannedData) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setvLPDNumber(etVLPD.getText().toString());
            vlpdDto.setPickedPalletNumber(etPallet.getText().toString());
            vlpdDto.setLocation(etDock.getText().toString());
            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);
            try {
                call = apiService.ValidatePalletAtVLPDDock(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ValidatePalletAtVLPDDock_01", getActivity());
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

                                cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanPallet.setImageResource(R.drawable.invalid_cross);

                                etPallet.setText("");

                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lGetPalletValidation = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lGetPalletValidation = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                InternalTransferDTO dto = null;
                                for (int i = 0; i < _lGetPalletValidation.size(); i++) {
                                    dto = new InternalTransferDTO(_lGetPalletValidation.get(i).entrySet());
                                }


                                if (dto.getStatus()) {

                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.check);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0044, getActivity(), getContext(), "Success");
                                    ProgressDialogUtils.closeProgressDialog();
                                    return;
                                }
                                ProgressDialogUtils.closeProgressDialog();
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ValidatePalletAtVLPDDock_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ValidatePalletAtVLPDDock_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ValidatePalletAtVLPDDock_04", getActivity());
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_map_pallet_dock));
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