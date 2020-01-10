package com.inventrax.athome_multiwh.fragments.HH;

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
import com.inventrax.athome_multiwh.pojos.ItemInfoDTO;
import com.inventrax.athome_multiwh.pojos.VLPDRequestDTO;
import com.inventrax.athome_multiwh.pojos.VLPDResponseDTO;
import com.inventrax.athome_multiwh.pojos.WMSCoreMessage;
import com.inventrax.athome_multiwh.pojos.WMSExceptionMessage;
import com.inventrax.athome_multiwh.searchableSpinner.SearchableSpinner;
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

public class SkuToSkuFragmentHH extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_006";
    private View rootView;

    private RelativeLayout rlPick, rlSelectReason, rlOBDSuggestion;
    private TextView lblPickRefNo, lblBin, lblScannedBin, lblRefNo, lblDock, lblLocation, lblSKU, lblDesc, lblBatch, lblBox, lblReqQty, lblBalQty, lblScannedBarcode;
    private CardView cvScanBin, cvScanBarcode;
    private ImageView ivScanBin, ivScanBarcode;
    private TextInputLayout txtInputLayoutPallet, txtInputLayoutQty;
    private SearchableSpinner spinnerSelectReason;
    private EditText etQty, etOldRsn, etNewRsn, etQtyPrint, etPrinterIP;
    private Button btnClose, btnSkip, btnExport, btnCloseOne, btnSkipItem, btnCloseTwo;

    FragmentUtils fragmentUtils;
    private Common common = null;

    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    private WMSCoreMessage core;
    String userId = null, materialType = null, vlpdId = null, vlpdTypeId = null, SkipReason = null;
    String sku = null, ref = null, reqQty = null, balQty = null, desc = null, bin = null, vlpdNo = null, skipvlpdId = null;
    private boolean isRSNScanned, isEANScanned;
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    String clientId = null;
    ArrayList<String> sloc;
    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    ToneGenerator toneGenerator;
    private ErrorMessages errorMessages;

    ItemInfoDTO vlpdItem = null;
    private boolean isPrintWindowRequired = false;
    private String OLDRSNNumber = "";
    private boolean IsSkipItem = false, IsPicking = false;

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public SkuToSkuFragmentHH() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.hh_fragment_obd_picking, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        rlOBDSuggestion = (RelativeLayout) rootView.findViewById(R.id.rlOBDSuggestion);
        rlPick = (RelativeLayout) rootView.findViewById(R.id.rlPick);
        rlSelectReason = (RelativeLayout) rootView.findViewById(R.id.rlSelectReason);


        lblPickRefNo = (TextView) rootView.findViewById(R.id.lblPickRefNo);
        lblBin = (TextView) rootView.findViewById(R.id.lblBin);
        lblScannedBin = (TextView) rootView.findViewById(R.id.lblScannedBin);

        lblRefNo = (TextView) rootView.findViewById(R.id.lblRefNo);
        lblDock = (TextView) rootView.findViewById(R.id.lblDock);
        lblLocation = (TextView) rootView.findViewById(R.id.lblLocation);
        lblDesc = (TextView) rootView.findViewById(R.id.lblDesc);
        lblSKU = (TextView) rootView.findViewById(R.id.lblSKU);
        lblBatch = (TextView) rootView.findViewById(R.id.lblBatch);
        lblBox = (TextView) rootView.findViewById(R.id.lblBox);
        lblReqQty = (TextView) rootView.findViewById(R.id.lblReqQty);
        lblBalQty = (TextView) rootView.findViewById(R.id.lblBalQty);
        lblScannedBarcode = (TextView) rootView.findViewById(R.id.lblScannedBarcode);

        cvScanBin = (CardView) rootView.findViewById(R.id.cvScanBin);
        cvScanBarcode = (CardView) rootView.findViewById(R.id.cvScanBarcode);

        ivScanBin = (ImageView) rootView.findViewById(R.id.ivScanBin);
        ivScanBarcode = (ImageView) rootView.findViewById(R.id.ivScanBarcode);

        txtInputLayoutPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPallet);
        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);


        etQty = (EditText) rootView.findViewById(R.id.etQty);

        btnClose = (Button) rootView.findViewById(R.id.btnClose);
        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);
        btnCloseTwo = (Button) rootView.findViewById(R.id.btnCloseTwo);
        btnSkip = (Button) rootView.findViewById(R.id.btnSkip);

        btnExport = (Button) rootView.findViewById(R.id.btnExport);
        btnSkipItem = (Button) rootView.findViewById(R.id.btnSkipItem);


        spinnerSelectReason = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectReason);
        spinnerSelectReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SkipReason = spinnerSelectReason.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");

        btnClose.setOnClickListener(this);
        btnCloseOne.setOnClickListener(this);
        btnCloseTwo.setOnClickListener(this);
        btnSkipItem.setOnClickListener(this);
        btnSkip.setOnClickListener(this);

        btnExport.setOnClickListener(this);


        sloc = new ArrayList<>();
        vlpdItem = new ItemInfoDTO();
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
        // Getting arguments from Bundle
        if (getArguments() != null) {

            if (materialType.equals("HU")) {

                vlpdId = getArguments().getString("vlpdId");
                lblRefNo.setText(getArguments().getString("vlpdNo"));
                lblLocation.setText(getArguments().getString("loc"));
                lblSKU.setText(getArguments().getString("sku"));
                lblDesc.setText(getArguments().getString("desc"));
                lblReqQty.setText(getArguments().getString("reqQty"));
                lblBalQty.setText(getArguments().getString("balQty"));
                etQty.setText(getArguments().getString("qty"));
                lblScannedBarcode.setText(getArguments().getString("barcode"));
                lblBox.setText(getArguments().getString("box"));
                lblDock.setText(getArguments().getString("dock"));
                lblBatch.setText(getArguments().getString("batchno"));
                vlpdItem = (ItemInfoDTO) getArguments().getSerializable("ItemInfoDto");
                lblBin.setText(getArguments().getString("loc"));
                lblPickRefNo.setText(getArguments().getString("vlpdNo"));


            } else if (materialType.equals("HH")) {

                vlpdId = getArguments().getString("vlpdId");
                lblRefNo.setText(getArguments().getString("vlpdNo"));
                lblLocation.setText(getArguments().getString("loc"));
                lblSKU.setText(getArguments().getString("sku"));
                lblDesc.setText(getArguments().getString("desc"));
                lblReqQty.setText(getArguments().getString("reqQty"));
                lblBalQty.setText(getArguments().getString("balQty"));
                etQty.setText(getArguments().getString("qty"));
                lblScannedBarcode.setText(getArguments().getString("barcode"));
                lblBox.setText(getArguments().getString("box"));
                lblDock.setText(getArguments().getString("dock"));
                lblBatch.setText(getArguments().getString("batchno"));
                vlpdItem = (ItemInfoDTO) getArguments().getSerializable("ItemInfoDto");
                lblBin.setText(getArguments().getString("loc"));
                lblPickRefNo.setText(getArguments().getString("vlpdNo"));

                lblScannedBin.setText(getArguments().getString("loc"));

                rlPick.setVisibility(View.VISIBLE);
                rlOBDSuggestion.setVisibility(View.GONE);
                rlSelectReason.setVisibility(View.GONE);

            }
        } else {
            GetAllOpenVLPDList();
        }

        // To get skip list
        LoadSkipReason();
    }

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
                if (!lblSKU.getText().toString().isEmpty()) {
                    updateSuggestedStatus();
                }
                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnCloseOne:
                if (!lblSKU.getText().toString().isEmpty()) {
                    updateSuggestedStatus();
                }
                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnCloseTwo:
               // FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                rlSelectReason.setVisibility(View.GONE);
                rlPick.setVisibility(View.VISIBLE);
                break;
            case R.id.btnSkip:
                if (lblSKU.getText().toString().isEmpty()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Error");
                    ClearFields();
                    return;
                }
                rlSelectReason.setVisibility(View.VISIBLE);
                rlPick.setVisibility(View.GONE);
                break;
            case R.id.btnExport:
                goToExport();
                break;
            case R.id.btnSkipItem:
                updateSkipReason();
                break;

            default:
                break;
        }
    }

    public void ClearFields() {
        cvScanBarcode.setCardBackgroundColor(getResources().getColor(R.color.scanColor));
        ivScanBarcode.setImageResource(R.drawable.fullscreen_img);

        cvScanBin.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanBin.setImageResource(R.drawable.fullscreen_img);


        etQty.setText("");
        lblScannedBarcode.setText("");


    }


    public void updateSuggestedStatus() {

        try {

            List<ItemInfoDTO> lstiteminfo = new ArrayList<>();
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDRequestDTO, getContext());
            VLPDRequestDTO vlpdRequestDTO = new VLPDRequestDTO();
            vlpdRequestDTO.setUserID(userId);
            vlpdRequestDTO.setVlpdID(vlpdId);
            vlpdRequestDTO.setUniqueRSN(lblScannedBarcode.getText().toString());
            ItemInfoDTO oIteminfo = new ItemInfoDTO();
            oIteminfo = vlpdItem;
            oIteminfo.setUserScannedRSN(lblScannedBarcode.getText().toString());
            oIteminfo.setUserRequestedQty(etQty.getText().toString());

            lstiteminfo.add(oIteminfo);
            vlpdRequestDTO.setPickerRequestedInfo(lstiteminfo);
            message.setEntityObject(vlpdRequestDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.UpdateSuggestedStatus(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateSuggestedStatus_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lVLPD = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lVLPD = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                List<VLPDRequestDTO> lstDto = new ArrayList<VLPDRequestDTO>();
                                List<String> lstVLPD = new ArrayList<>();
                                VLPDRequestDTO dto = null;
                                for (int i = 0; i < _lVLPD.size(); i++) {
                                    dto = new VLPDRequestDTO(_lVLPD.get(i).entrySet());
                                    lstDto.add(dto);
                                }

                                ProgressDialogUtils.closeProgressDialog();

                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateSuggestedStatus_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateSuggestedStatus_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateSuggestedStatus_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }

    }


    public void goToExport() {

        Bundle bundle = new Bundle();

        bundle.putString("vlpdId", vlpdId);
        bundle.putString("vlpdNo", lblRefNo.getText().toString());
        bundle.putString("loc", lblLocation.getText().toString());
        bundle.putString("sku", lblSKU.getText().toString());
        bundle.putString("desc", lblDesc.getText().toString());
        bundle.putString("reqQty", lblReqQty.getText().toString());
        bundle.putString("balQty", lblBalQty.getText().toString());
        bundle.putString("qty", etQty.getText().toString());

        bundle.putString("dock", lblDock.getText().toString());
        bundle.putString("barcode", lblScannedBarcode.getText().toString());
        bundle.putString("batchno", lblBatch.getText().toString());
        bundle.putString("box", lblBox.getText().toString());
        bundle.putSerializable("ItemInfoDto", vlpdItem);
        PendingSkuToSkuFragmentHH pendingSkuToSkuFragmentHH = new PendingSkuToSkuFragmentHH();
        pendingSkuToSkuFragmentHH.setArguments(bundle);
        FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, pendingSkuToSkuFragmentHH);
    }

    public void updateSkipReason() {

        if (!SkipReason.equals("Select")) {
            IsSkipItem = true;

            GetAllOpenVLPDList();

        } else {
            common.showUserDefinedAlertType(errorMessages.EMC_0041, getActivity(), getContext(), "Error");
            return;
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
            // Set Max Code 39 barcode length
            properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
            properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);
            // Turn on center decoding
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Enable bad read response
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);
            // Apply the settings
            barcodeReader.setProperties(properties);
        }

    }

    // To get VLPD Id
    private void GetAllOpenVLPDList() {
        try {
            List<ItemInfoDTO> lstiteminfo = new ArrayList<>();
            ItemInfoDTO oItem = new ItemInfoDTO();
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDRequestDTO, getContext());
            VLPDRequestDTO vlpdRequestDTO = new VLPDRequestDTO();
            vlpdRequestDTO.setUserID(userId);
            vlpdRequestDTO.setType("5");
            if (vlpdItem != null) {
                oItem = vlpdItem;
            }
            oItem.setRequestType("PICK");
            if (IsSkipItem) {

                oItem.setRequestType("SKIP");
                oItem.setSkipReason(SkipReason);
                oItem.setUserScannedRSN(lblScannedBarcode.getText().toString());
                lstiteminfo.add(oItem);

            }
            vlpdRequestDTO.setPickerRequestedInfo(lstiteminfo);
            message.setEntityObject(vlpdRequestDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetOpenVLPDListByPriority(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenVLPDListByPriority_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lVLPD = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lVLPD = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                if (_lVLPD.size() > 0) {
                                    List<VLPDResponseDTO> lstDto = new ArrayList<VLPDResponseDTO>();
                                    List<String> lstVLPD = new ArrayList<>();

                                    VLPDResponseDTO dto = null;
                                    for (int i = 0; i < _lVLPD.size(); i++) {
                                        dto = new VLPDResponseDTO(_lVLPD.get(i).entrySet());
                                        lstDto.add(dto);
                                    }
                                    ProgressDialogUtils.closeProgressDialog();
                                    //getItemToPick();
                                    if (dto.getPreviousPickedItemResponce() != null) {
                                        if (dto.getPreviousPickedItemResponce().get(0).getMessage() != null) {
                                            if (dto.getPreviousPickedItemResponce().get(0).getStatus() == false) {

                                                common.showUserDefinedAlertType(dto.getPreviousPickedItemResponce().get(0).getMessage(), getActivity(), getContext(), "Error");
                                                return;
                                            }

                                        }
                                    }
                                    if (dto.getSuggestedItem() != null) {
                                        for (ItemInfoDTO itemInfoDTO : dto.getSuggestedItem()) {
                                            vlpdItem = itemInfoDTO;
                                        }
                                    } else {
                                        vlpdItem = null;
                                    }
                                    if (vlpdItem != null) {
                                        if (vlpdItem.getMcode() != null && vlpdItem.getMcode() != "") {
                                            vlpdId = dto.getVlpdID();
                                            UpDateUI(vlpdItem);
                                        /*if (isPrintWindowRequired) {
                                            ShowPrintPanel(OLDRSNNumber);
                                        }*/
                                        }
                                    } else {

                                        common.showUserDefinedAlertType(errorMessages.EMC_0043.replace("[Reference]", lblRefNo.getText()), getActivity(), getContext(), "Error");
                                        rlPick.setVisibility(View.VISIBLE);

                                        rlSelectReason.setVisibility(View.GONE);
                                        lblReqQty.setText("");
                                        lblBox.setText("");
                                        ClearUIElemennts();
                                        skipvlpdId = "";
                                        IsSkipItem = false;
                                        GetAllOpenVLPDList();
                                        ProgressDialogUtils.closeProgressDialog();
                                        return;
                                    }


                                } else {
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Error");

                                    ClearFields();
                                    return;
                                }
                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenVLPDListByPriority_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenVLPDListByPriority_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenVLPDListByPriority_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    private void UpDateUI(ItemInfoDTO suggestedItem) {
        rlOBDSuggestion.setVisibility(View.VISIBLE);
        rlPick.setVisibility(View.GONE);
        rlSelectReason.setVisibility(View.GONE);
        if (suggestedItem != null) {
            ////Fill Outbound Information
            if (suggestedItem.getMcode() != null && suggestedItem.getMcode() != "") {
                if (suggestedItem.getLocation().toString().equals(lblLocation.getText().toString())) {

                    rlOBDSuggestion.setVisibility(View.GONE);
                    rlPick.setVisibility(View.VISIBLE);
                    rlSelectReason.setVisibility(View.GONE);

                    lblScannedBarcode.setText("");


                    cvScanBarcode.setCardBackgroundColor(getResources().getColor(R.color.scanColor));
                    ivScanBarcode.setImageResource(R.drawable.fullscreen_img);

                } else {
                    rlOBDSuggestion.setVisibility(View.VISIBLE);
                    rlPick.setVisibility(View.GONE);
                    rlSelectReason.setVisibility(View.GONE);

                    lblScannedBin.setText("");
                    cvScanBin.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                    ivScanBin.setImageResource(R.drawable.fullscreen_img);
                }
                lblPickRefNo.setText(suggestedItem.getRefDoc());
                lblBin.setText(suggestedItem.getLocation());
                vlpdTypeId = suggestedItem.getVlpdTypeId();
                lblSKU.setText(suggestedItem.getMcode());
                lblDesc.setText(suggestedItem.getDescription());
                lblBatch.setText(suggestedItem.getBatchNumber());
                lblLocation.setText(suggestedItem.getLocation());
                etQty.setText(suggestedItem.getReqQuantity().toString());
                lblBalQty.setText(suggestedItem.getAvlQuantity().toString());
                lblBox.setText(suggestedItem.getHuNo() + "/" + suggestedItem.getHuSize());
                lblReqQty.setText(suggestedItem.getReqQuantity().toString());
                lblScannedBarcode.setText("");
                etQty.setText("");
                etQty.setEnabled(false);

                if (lblBox.getText().toString().isEmpty() || lblBox.equals("0")) {
                    common.showUserDefinedAlertType(errorMessages.EMC_0063, getActivity(), getContext(), "Error");
                    return;
                }

                lblDock.setText(suggestedItem.getDock());
                lblRefNo.setText(suggestedItem.getRefDoc());
                cvScanBarcode.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                ivScanBarcode.setImageResource(R.drawable.fullscreen_img);
                ProgressDialogUtils.closeProgressDialog();
            } else {
                if (!lblSKU.getText().toString().isEmpty()) {
                    //   MessageBox.Show("No item pending to pick with ref: " + lblSKU.getText().toString());
                    ClearUIElemennts();
                    GetAllOpenVLPDList();
                    common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Warning");
                    //MessageBox.Show("No items available to pick");
                    return;
                }
            }
        } else {
            common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Warning");
            //MessageBox.Show("No items available to pick");
            return;
        }
    }


    private void ClearUIElemennts() {
        lblSKU.setText("");
        lblBatch.setText("");
        lblDesc.setText("");
        lblBatch.setText("");
        lblLocation.setText("");
        lblBalQty.setText("");
        lblRefNo.setText("");
        lblDock.setText("");
        lblScannedBarcode.setText("");
        lblBox.setText("0");
    }


    private void LoadSkipReason() {

        List<String> lstSkipReason = new ArrayList<>();
        lstSkipReason.add("Damage");
        lstSkipReason.add("Not Found");
        ArrayAdapter arrayAdapterStoreRefNo = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstSkipReason);
        spinnerSelectReason.setAdapter(arrayAdapterStoreRefNo);
    }

    public void ValidateBarcodeAndConfirmPicking() {

        try {

            List<ItemInfoDTO> lstiteminfo = new ArrayList<>();
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDRequestDTO, getContext());
            VLPDRequestDTO vlpdRequestDTO = new VLPDRequestDTO();
            vlpdRequestDTO.setUserID(userId);
            vlpdRequestDTO.setVlpdID(vlpdId);
            vlpdRequestDTO.setUniqueRSN(lblScannedBarcode.getText().toString());
            ItemInfoDTO oIteminfo = new ItemInfoDTO();
            oIteminfo = vlpdItem;
            oIteminfo.setEAN(lblScannedBarcode.getText().toString());
            oIteminfo.setUserRequestedQty(etQty.getText().toString());
            oIteminfo.setPalletNumber("");
            oIteminfo.setUserScannedRSN("");
            lstiteminfo.add(oIteminfo);
            vlpdRequestDTO.setPickerRequestedInfo(lstiteminfo);
            message.setEntityObject(vlpdRequestDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.ValidateBarcodeAndConfirmPicking(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ValidateBarcodeAndConfirmPicking_01", getActivity());
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
                                lblScannedBarcode.setText("");
                                etQty.setText("");


                            } else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lVLPD = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lVLPD = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                List<VLPDResponseDTO> lstDto = new ArrayList<VLPDResponseDTO>();
                                List<String> lstVLPD = new ArrayList<>();
                                VLPDResponseDTO dto = null;
                                for (int i = 0; i < _lVLPD.size(); i++) {
                                    dto = new VLPDResponseDTO(_lVLPD.get(i).entrySet());

                                    lstDto.add(dto);
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                if (dto.getSuggested()) {
                                    ClearFields();
                                    ClearUIElemennts();
                                    lblBox.setText("");
                                    lblReqQty.setText("");
                                    common.showUserDefinedAlertType(errorMessages.EMC_0043.replace("[Reference]", lblRefNo.getText()), getActivity(), getContext(), "Error");

                                    return;
                                }
                                if (dto.getSuggestedItem() != null) {
                                    for (ItemInfoDTO oiteminfo : dto.getSuggestedItem()) {
                                        vlpdItem = oiteminfo;
                                    }
                                }

                                if (dto.getSuggestedItem() == null) {
                                    if (dto.getPreviousPickedItemResponce() != null) {
                                        if (dto.getPreviousPickedItemResponce().get(0).getMessage() != null) {
                                            if (dto.getPreviousPickedItemResponce().get(0).getStatus() == false) {
                                                common.showUserDefinedAlertType(dto.getPreviousPickedItemResponce().get(0).getMessage(), getActivity(), getContext(), "Error");
                                                ;
                                                //MessageBox.Show(responce.PreviousPickedItemResponce.Message);
                                                etQty.setText("");
                                                etQty.setEnabled(false);

                                                lblScannedBarcode.setText(R.string.scan_barcode);
                                            } else {

                                                etQty.setText(dto.getPreviousPickedItemResponce().get(0).getMessage());
                                                etQty.setEnabled(true);

                                                common.showUserDefinedAlertType(errorMessages.EMC_0036, getActivity(), getContext(), "Error");
                                                return;


                                            }
                                        }
                                    } else {
                                        common.showUserDefinedAlertType(errorMessages.EMC_0037, getActivity(), getContext(), "Error");
                                        ;
                                        return;
                                    }

                                } else {

                                    if (dto.getPreviousPickedItemResponce() != null) {
                                        if (dto.getPreviousPickedItemResponce().get(0).getMessage() != null) {
                                            if (dto.getPreviousPickedItemResponce().get(0).getStatus() == false) {
                                                common.showUserDefinedAlertType(dto.getPreviousPickedItemResponce().get(0).getMessage(), getActivity(), getContext(), "Error");
                                                return;

                                            } else {

                                                etQty.setEnabled(false);
                                            }
                                        }
                                    }
                                }
                                //vlpdItem =dto.getSuggestedItem();
                                ProgressDialogUtils.closeProgressDialog();
                                if (vlpdItem != null) {
                                    if (vlpdItem.getMcode() != null && vlpdItem.getMcode() != "") {
                                        cvScanBarcode.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanBarcode.setImageResource(R.drawable.check);
                                        UpDateUI(vlpdItem);


                                        if (lblRefNo.getText().toString().isEmpty()) {
                                            //MessageBox.Show("No item pending to pick with ref: " + lblRefNumberValue.Text);
                                            ClearUIElemennts();

                                        } else {
                                        }
                                    }
                                }

                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ValidateBarcodeAndConfirmPicking_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ValidateBarcodeAndConfirmPicking_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "ValidateBarcodeAndConfirmPicking_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {

        if (scannedData != null && !common.isPopupActive()) {

            if (ScanValidator.IsLocationScanned(scannedData)) {

                if (rlPick.getVisibility() == View.VISIBLE) {
                    common.showUserDefinedAlertType(errorMessages.EMC_0062, getActivity(), getContext(), "Error");
                    return;
                }

                if (lblSKU.getText().toString().isEmpty()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Error");
                    ClearFields();
                    return;
                }
                if (lblBin.getText().toString().isEmpty()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_0065, getActivity(), getContext(), "Error");
                    return;
                }

                lblScannedBin.setText(scannedData);

                if (!lblBin.getText().toString().isEmpty()) {

                    if (lblScannedBin.getText().toString().equals(lblBin.getText().toString())) {

                        rlOBDSuggestion.setVisibility(View.GONE);
                        rlPick.setVisibility(View.VISIBLE);         // To navigate to Picking screen
                        return;

                    } else {

                        lblScannedBin.setText("");
                        cvScanBin.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanBin.setImageResource(R.drawable.invalid_cross);

                        common.showUserDefinedAlertType(errorMessages.EMC_0038, getActivity(), getContext(), "Error");
                        return;
                    }
                }
            }


            // Checking For EAN Scan
            else if (!ScanValidator.IsPalletScanned(scannedData) && !ScanValidator.IsLocationScanned(scannedData) && !ScanValidator.IsRSNScanned(scannedData)) {
                if (lblSKU.getText().toString().isEmpty()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Error");
                    ClearFields();
                    return;
                }
                if (lblScannedBin.getText().toString().isEmpty()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Error");
                    return;
                }
                //Handling Set Quantity in EAN Barcode
                if (scannedData.split("[,]").length == 2) {
                    try {
                        int qty = Integer.parseInt(scannedData.split("[,]")[1]);
                        int ReqQty = 0;
                        if (!lblReqQty.getText().toString().isEmpty()) {
                            ReqQty = Integer.parseInt(lblReqQty.getText().toString().split("[.]")[0]);
                        }
                        // IF Scanned quantity is 10 and required quntity is <10
                        if (ReqQty < qty) {
                            common.showUserDefinedAlertType(errorMessages.EMC_0061, getActivity(), getContext(), "Error");
                            return;
                        }
                        etQty.setText(Integer.toString(qty));
                    } catch (Exception ex) {
                        common.showUserDefinedAlertType(errorMessages.EMC_0062, getActivity(), getContext(), "Error");
                        etQty.setText("");
                    }

                } else {
                    etQty.setText("1");
                }

                lblScannedBarcode.setText(scannedData.split("[,]")[0]);

                isRSNScanned = false;
                isEANScanned = true;

                //etEAN.setText(scannedData.split("[,]")[0]);

                ValidateBarcodeAndConfirmPicking();
                return;
            } else {
                if (lblSKU.getText().toString().isEmpty()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Error");
                    ClearFields();
                    return;
                }
                lblScannedBarcode.setText("");
                lblScannedBarcode.setText("");

                cvScanBarcode.setCardBackgroundColor(getResources().getColor(R.color.white));
                ivScanBarcode.setImageResource(R.drawable.invalid_cross);
                common.showUserDefinedAlertType(errorMessages.EMC_0062, getActivity(), getContext(), "Error");
                return;
            }
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_pending_skutosku));
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