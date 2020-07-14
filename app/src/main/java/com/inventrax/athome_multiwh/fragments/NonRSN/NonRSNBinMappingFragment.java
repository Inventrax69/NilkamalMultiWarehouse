package com.inventrax.athome_multiwh.fragments.NonRSN;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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
import com.inventrax.athome_multiwh.adapters.PendingBintoBinListAdapter;
import com.inventrax.athome_multiwh.common.Common;
import com.inventrax.athome_multiwh.common.constants.EndpointConstants;
import com.inventrax.athome_multiwh.common.constants.ErrorMessages;
import com.inventrax.athome_multiwh.interfaces.ApiInterface;
import com.inventrax.athome_multiwh.pojos.InternalTransferDTO;
import com.inventrax.athome_multiwh.pojos.InventoryDTO;
import com.inventrax.athome_multiwh.pojos.OutboundDTO;
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

public class NonRSNBinMappingFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {
    private static final String classCode = "API_FRAG_NonRSNBinToBinFragment";
    private View rootView;

    private RelativeLayout rlBinMapping, rlPending;
    private CardView cvScanSourcePallet, cvScanDestPallet, cvScanDestBin;
    private ImageView ivScanSourcePallet, ivScanDestPallet, ivScanDestBin;
    private EditText etSourcePallet, etDestPallet, etDestBin, etCountBinMap;
    private Button btnClearBinMap, btnExportBinMap, btnConfirmBinMap, btnCloseBinMap, btnCancel;
    private RecyclerView rvBintoBinPendingList;
    private LinearLayoutManager linearLayoutManager;

    FragmentUtils fragmentUtils;

    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    String userId = null, materialType = null;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;

    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;


    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public NonRSNBinMappingFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.nonrsn_binmapping_fragment, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");

        rlBinMapping = (RelativeLayout) rootView.findViewById(R.id.rlBinMapping);
        rlPending = (RelativeLayout) rootView.findViewById(R.id.rlPending);

        cvScanSourcePallet = (CardView) rootView.findViewById(R.id.cvScanSourcePallet);
        cvScanDestPallet = (CardView) rootView.findViewById(R.id.cvScanDestPallet);
        cvScanDestBin = (CardView) rootView.findViewById(R.id.cvScanDestBin);

        ivScanSourcePallet = (ImageView) rootView.findViewById(R.id.ivScanSourcePallet);
        ivScanDestPallet = (ImageView) rootView.findViewById(R.id.ivScanDestPallet);
        ivScanDestBin = (ImageView) rootView.findViewById(R.id.ivScanDestBin);

        etSourcePallet = (EditText) rootView.findViewById(R.id.etSourcePallet);
        etDestPallet = (EditText) rootView.findViewById(R.id.etDestPallet);
        etDestBin = (EditText) rootView.findViewById(R.id.etDestBin);
        etCountBinMap = (EditText) rootView.findViewById(R.id.etCountBinMap);

        btnClearBinMap = (Button) rootView.findViewById(R.id.btnClearBinMap);
        btnExportBinMap = (Button) rootView.findViewById(R.id.btnExportBinMap);
        btnConfirmBinMap = (Button) rootView.findViewById(R.id.btnConfirmBinMap);
        btnCloseBinMap = (Button) rootView.findViewById(R.id.btnCloseBinMap);
        btnCancel = (Button) rootView.findViewById(R.id.btnCancel);


        btnClearBinMap.setOnClickListener(this);
        btnExportBinMap.setOnClickListener(this);
        btnConfirmBinMap.setOnClickListener(this);
        btnCloseBinMap.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        rvBintoBinPendingList = (RecyclerView) rootView.findViewById(R.id.rvBintoBinPendingList);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        rvBintoBinPendingList.setLayoutManager(linearLayoutManager);

        rlBinMapping.setVisibility(View.VISIBLE);
        rlPending.setVisibility(View.GONE);

        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();

        common.setIsPopupActive(false);

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

            case R.id.btnCancel:
                rlBinMapping.setVisibility(View.VISIBLE);
                rlPending.setVisibility(View.GONE);

                break;

            case R.id.btnCloseBinMap:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new NonRSNBinToBinFragment());
                break;


            case R.id.btnConfirmBinMap:
                MapPalletToLocation();
                break;
            case R.id.btnClearBinMap:
                binMappingClearFields();
                break;

            case R.id.btnExportBinMap:
                if (!etDestPallet.getText().toString().isEmpty()) {
                    GetInternaltransferInformation();
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                }
                break;

            default:
                break;
        }
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

    //01A01B2
    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {

        if (rlBinMapping.getVisibility() == View.VISIBLE) {

            if (!ProgressDialogUtils.isProgressActive()) {

                // Checking for Source Pallet
                if (etSourcePallet.getText().toString().isEmpty()) {
                    if (ScanValidator.IsPalletScanned(scannedData)) {
                        etSourcePallet.setText(scannedData);
                        cvScanSourcePallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanSourcePallet.setImageResource(R.drawable.check);
                        GetTempPalletItemCount();
                        return;
                    } else {
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
                    } else {
                        common.showUserDefinedAlertType("Please scan dest. pallet", getActivity(), getContext(), "Error");
                        return;
                    }
                }

                // Checking for destination palllet to map
                if (!etDestPallet.getText().toString().isEmpty() && etDestBin.getText().toString().isEmpty()) {
                    if (ScanValidator.IsLocationScanned(scannedData)) {
                        if (scannedData.length() == 8) {
                            etDestBin.setText(scannedData);
                            cvScanDestBin.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanDestBin.setImageResource(R.drawable.check);
                            return;
                        } else {
                            etDestBin.setText(scannedData);
                            cvScanDestBin.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanDestBin.setImageResource(R.drawable.check);
                            return;
                        }

                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Error");
                        return;
                    }
                }
            }
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

                                if (!((LinkedTreeMap) core.getEntityObject()).get("Message").equals("")) {
                                    String destinationLoc = (String) ((LinkedTreeMap) core.getEntityObject()).get("Message");
                                    ProgressDialogUtils.closeProgressDialog();
                                    etDestBin.setText(destinationLoc);
                                    etDestBin.setEnabled(false);

                                    cvScanDestBin.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanDestBin.setImageResource(R.drawable.check);
                                    cvScanDestPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanDestPallet.setImageResource(R.drawable.check);
                                } else {
                                    cvScanDestPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanDestPallet.setImageResource(R.drawable.check);
                                }

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

    public void binMappingClearFields() {

        cvScanSourcePallet.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanSourcePallet.setImageResource(R.drawable.fullscreen_img);

        cvScanDestPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanDestPallet.setImageResource(R.drawable.fullscreen_img);

        cvScanDestBin.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanDestBin.setImageResource(R.drawable.fullscreen_img);

        etSourcePallet.setText("");
        etDestPallet.setText("");
        etDestBin.setText("");
        etCountBinMap.setText("");

    }

    public void GetInternaltransferInformation() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.InternalTransferDTO, getContext());
            InternalTransferDTO internalTransferDTO = new InternalTransferDTO();
            internalTransferDTO.setBarcodeType("PALLET");
            internalTransferDTO.setBarcode(etDestPallet.getText().toString());
            message.setEntityObject(internalTransferDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetInternaltransferInformation(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetInternaltransferInformation", getActivity());
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
                            if (core.getType() != null) {
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

                                    List<LinkedTreeMap<?, ?>> _lPendingBintoBin = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lPendingBintoBin = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    List<OutboundDTO> listPendingBintoBin = new ArrayList<OutboundDTO>();

                                    for (int i = 0; i < _lPendingBintoBin.size(); i++) {
                                        OutboundDTO oOutbound = new OutboundDTO(_lPendingBintoBin.get(i).entrySet());
                                        listPendingBintoBin.add(oOutbound);

                                    }
                                    // Setting Values to the view
                                    PendingBintoBinListAdapter pendingBintoBinListAdapter = new PendingBintoBinListAdapter(getActivity(), listPendingBintoBin);
                                    rvBintoBinPendingList.setAdapter(pendingBintoBinListAdapter);
                                    ProgressDialogUtils.closeProgressDialog();

                                }
                            } else {
                                ProgressDialogUtils.closeProgressDialog();

                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetInternaltransferInformation", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetInternaltransferInformation", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetInternaltransferInformation", getActivity());
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

