package com.inventrax.athome_multiwh.fragments.HH.ECOM;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.inventrax.athome_multiwh.pojos.ItemInfoDTO;
import com.inventrax.athome_multiwh.pojos.VLPDRequestDTO;
import com.inventrax.athome_multiwh.pojos.VLPDResponseDTO;
import com.inventrax.athome_multiwh.pojos.VlpdDto;
import com.inventrax.athome_multiwh.pojos.WMSCoreMessage;
import com.inventrax.athome_multiwh.pojos.WMSExceptionMessage;
import com.inventrax.athome_multiwh.searchableSpinner.SearchableSpinner;
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

public class EcomPickOnDemandFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_ECOM PICK ON DEMAND";
    private View rootView;

    private RelativeLayout rlVLPD, rlPick, rlSelectReason, rlPrint;
    private TextView lblRefNo, lblDock, lblLocation, lblSKU, lblDesc, lblBatch, lblBox, lblReqQty, lblScannedBarcode;
    private CardView cvScanBarcode;
    private ImageView ivScanBarcode;
    private TextInputLayout txtInputLayoutVLPD, txtInputLayoutPallet, txtInputLayoutQty;
    private SearchableSpinner spinnerSelectReason;
    private CustomEditText etVLPD,etQty, etQtyPrint, etPrinterIP;
    private Button btnOk, btnClose, btnSkip, btnExport, btnCloseOne, btnSkipItem, btnCloseTwo, btnPrint, btnClosePrint;
    private String PickQty = null;
    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    private WMSCoreMessage core;
    String userId = null, materialType = null, vlpdId = null, vlpdTypeId = null, SkipReason = null, vlpdNo = null;
    private boolean isRSNScanned, isEANScanned;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;

    ArrayList<String> sloc;
    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;

    ItemInfoDTO vlpdItem = null;
    private boolean isPrintWindowRequired = false;
    private String OLDRSNNumber = "", pickedMMID = null;
    private String ipAddress = null, printerIPAddress = null;
    private boolean IsSkipItem = false, IsRSNScanned = false;

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };


    public EcomPickOnDemandFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.ecom_pickondemand_fragment, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        rlPick = (RelativeLayout) rootView.findViewById(R.id.rlPick);
        rlSelectReason = (RelativeLayout) rootView.findViewById(R.id.rlSelectReason);
        rlVLPD = (RelativeLayout) rootView.findViewById(R.id.rlVLPD);
        rlPrint = (RelativeLayout) rootView.findViewById(R.id.rlPrint);

        lblRefNo = (TextView) rootView.findViewById(R.id.lblRefNo);
        lblDock = (TextView) rootView.findViewById(R.id.lblDock);
        lblLocation = (TextView) rootView.findViewById(R.id.lblLocation);
        lblDesc = (TextView) rootView.findViewById(R.id.lblDesc);
        lblSKU = (TextView) rootView.findViewById(R.id.lblSKU);
        lblBatch = (TextView) rootView.findViewById(R.id.lblBatch);
        lblBox = (TextView) rootView.findViewById(R.id.lblBox);
        lblReqQty = (TextView) rootView.findViewById(R.id.lblReqQty);
        lblScannedBarcode = (TextView) rootView.findViewById(R.id.lblScannedBarcode);

        cvScanBarcode = (CardView) rootView.findViewById(R.id.cvScanBarcode);

        ivScanBarcode = (ImageView) rootView.findViewById(R.id.ivScanBarcode);

        txtInputLayoutPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPallet);
        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);
        txtInputLayoutVLPD = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutVLPD);

        etQty = (CustomEditText) rootView.findViewById(R.id.etQty);
        etVLPD = (CustomEditText) rootView.findViewById(R.id.etVLPD);
        etQtyPrint = (CustomEditText) rootView.findViewById(R.id.etQtyPrint);
        etPrinterIP = (CustomEditText) rootView.findViewById(R.id.etPrinterIP);

        btnClosePrint = (Button) rootView.findViewById(R.id.btnClosePrint);
        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);
        btnCloseTwo = (Button) rootView.findViewById(R.id.btnCloseTwo);
        btnSkip = (Button) rootView.findViewById(R.id.btnSkip);
        btnExport = (Button) rootView.findViewById(R.id.btnExport);
        btnSkipItem = (Button) rootView.findViewById(R.id.btnSkipItem);
        btnOk = (Button) rootView.findViewById(R.id.btnOk);
        btnClose = (Button) rootView.findViewById(R.id.btnClose);
        btnPrint = (Button) rootView.findViewById(R.id.btnPrint);

        etQty.setEnabled(false);

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

        btnCloseOne.setOnClickListener(this);
        btnCloseTwo.setOnClickListener(this);
        btnSkipItem.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
        btnExport.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnPrint.setOnClickListener(this);
        btnClosePrint.setOnClickListener(this);

        sloc = new ArrayList<>();

        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();

        SharedPreferences spPrinterIP = getActivity().getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        ipAddress = spPrinterIP.getString("printerIP", "");

        if (ipAddress != null) {
            etPrinterIP.setText(ipAddress);
        }

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


        //To get skip items
        LoadSkipReason();


        // Getting arguments from Bundle
        if (getArguments() != null) {

            if (materialType.equals("HH")) {

                rlVLPD.setVisibility(View.GONE);
                rlSelectReason.setVisibility(View.GONE);
                rlPick.setVisibility(View.VISIBLE);

                rlVLPD.setVisibility(View.GONE);
                rlSelectReason.setVisibility(View.GONE);
                rlPick.setVisibility(View.VISIBLE);

                vlpdItem = (ItemInfoDTO) getArguments().getSerializable("ItemInfoDto");
                vlpdId = getArguments().getString("vlpdId");
                lblRefNo.setText(getArguments().getString("vlpdNo"));
                lblLocation.setText(getArguments().getString("loc"));
                lblSKU.setText(getArguments().getString("sku"));
                lblDesc.setText(getArguments().getString("desc"));
                lblReqQty.setText(getArguments().getString("reqQty"));
                etQty.setText(getArguments().getString("qty"));
                lblScannedBarcode.setText(getArguments().getString("barcode"));
                lblBox.setText(getArguments().getString("box"));
                lblDock.setText(getArguments().getString("dock"));
                lblBatch.setText(getArguments().getString("batchno"));

                if (!lblScannedBarcode.getText().toString().isEmpty()) {

                    cvScanBarcode.setCardBackgroundColor(getResources().getColor(R.color.white));
                    ivScanBarcode.setImageResource(R.drawable.check);
                }

            }

        }
    }

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnCloseOne:
                if (!lblSKU.getText().toString().isEmpty()) {
                    updateSuggestedStatus();
                }
                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnClose:
                if (!lblSKU.getText().toString().isEmpty()) {
                    updateSuggestedStatus();
                }
                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnOk:
                if (!etVLPD.getText().toString().isEmpty()) {
                    GetVLPDID();
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0039, getActivity(), getContext(), "Warning");
                }
                break;
            case R.id.btnCloseTwo:
                rlSelectReason.setVisibility(View.GONE);
                rlPick.setVisibility(View.VISIBLE);
                rlPrint.setVisibility(View.GONE);
                rlVLPD.setVisibility(View.GONE);
                break;
            case R.id.btnSkip:
                if (lblSKU.getText().toString().isEmpty()) {
                    clearFields();
                    common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Error");
                    return;
                }
                rlSelectReason.setVisibility(View.VISIBLE);
                rlPick.setVisibility(View.GONE);
                rlPrint.setVisibility(View.GONE);
                rlVLPD.setVisibility(View.GONE);
                break;

            case R.id.btnExport:
                if (lblSKU.getText().toString().isEmpty()) {
                    clearFields();
                    common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Error");
                    return;
                }
                goToExport();
                break;

            case R.id.btnSkipItem:
                updateSkipReason();
                break;

            case R.id.btnClosePrint:
                rlPrint.setVisibility(View.GONE);
                rlPick.setVisibility(View.VISIBLE);
                rlSelectReason.setVisibility(View.GONE);
                break;

            default:
                break;
        }
    }


    private void GetVLPDID() {
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
                call = apiService.GetVLPDID(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetVLPDID", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error   ");
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

                                vlpdId = dto.getiD();
                                if (vlpdId.equals("0")) {

                                    common.showUserDefinedAlertType(errorMessages.EMC_034, getActivity(), getContext(), "Warning");
                                    ProgressDialogUtils.closeProgressDialog();
                                    return;
                                } else {
                                    getItemToPick();
                                }

                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetVLPDID", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetVLPDID", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetVLPDID", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }


    }

    public void ClearFields() {

        cvScanBarcode.setCardBackgroundColor(getResources().getColor(R.color.scanColor));
        ivScanBarcode.setImageResource(R.drawable.fullscreen_img);

        etQty.setText("");
        lblScannedBarcode.setText("");

    }


    public void updateSkipReason() {

        if (!SkipReason.equals("Select")) {
            IsSkipItem = true;
           /* pickRequest.userID = Program.Userid;
            pickRequest.userIDSpecified = true;
            vlpdItem.SkipReason = cmbSkipReason.SelectedItem.ToString();
            vlpdItem.RequestType = "SKIP";
            pickRequest.pickerRequestedInfo = vlpdItem;*/
            getItemToPick();

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

    public void goToExport() {

        Bundle bundle = new Bundle();

        bundle.putString("vlpdId", vlpdId);
        bundle.putString("vlpdNo", lblRefNo.getText().toString());
        bundle.putString("loc", lblLocation.getText().toString());
        bundle.putString("sku", lblSKU.getText().toString());
        bundle.putString("desc", lblDesc.getText().toString());
        bundle.putString("reqQty", lblReqQty.getText().toString());
        bundle.putString("qty", etQty.getText().toString());
        bundle.putString("dock", lblDock.getText().toString());
        bundle.putString("barcode", lblScannedBarcode.getText().toString());
        bundle.putString("batchno", lblBatch.getText().toString());
        bundle.putString("box", lblBox.getText().toString());
        bundle.putSerializable("ItemInfoDto", vlpdItem);

        EcomPendingPickOnDemand ecomPendingPickOnDemand = new EcomPendingPickOnDemand();
        ecomPendingPickOnDemand.setArguments(bundle);
        FragmentUtils.replaceFragment(getActivity(), R.id.container_body, ecomPendingPickOnDemand);


    }


    public void getItemToPick() {

        try {
            List<ItemInfoDTO> lstiteminfo = new ArrayList<>();
            ItemInfoDTO oItem = new ItemInfoDTO();
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDRequestDTO, getContext());
            VLPDRequestDTO vlpdRequestDTO = new VLPDRequestDTO();
            vlpdRequestDTO.setUserID(userId);
            vlpdRequestDTO.setVlpdID(vlpdId);
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
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetItemtoPick(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetItemtoPick_01", getActivity());
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
                                List<VLPDResponseDTO> lstDto = new ArrayList<VLPDResponseDTO>();
                                List<String> lstVLPD = new ArrayList<>();
                                VLPDResponseDTO dto = null;
                                for (int i = 0; i < _lVLPD.size(); i++) {
                                    dto = new VLPDResponseDTO(_lVLPD.get(i).entrySet());

                                    lstDto.add(dto);
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                if(dto.getSuggestedItem()!=null) {
                                for (ItemInfoDTO oiteminfo : dto.getSuggestedItem()) {
                                    vlpdItem = oiteminfo;
                                }
                                }
                                else
                                {
                                    ClearFields();
                                    rlVLPD.setVisibility(View.VISIBLE);
                                    rlPick.setVisibility(View.GONE);
                                    rlSelectReason.setVisibility(View.GONE);
                                    rlPrint.setVisibility(View.GONE);
                                    etVLPD.setText("");
                                    ClearUIElemennts();
                                    lblBox.setText("");
                                    lblReqQty.setText("");
                                    common.showUserDefinedAlertType(errorMessages.EMC_0065, getActivity(), getContext(), "Error");
                                    return;
                                }

                                ProgressDialogUtils.closeProgressDialog();

                                if (dto.getPreviousPickedItemResponce() != null) {
                                    if (dto.getPreviousPickedItemResponce().get(0).getMessage() != null) {
                                        if (dto.getPreviousPickedItemResponce().get(0).getStatus() == false) {

                                            common.showUserDefinedAlertType(dto.getPreviousPickedItemResponce().get(0).getMessage(), getActivity(), getContext(), "Error");
                                            return;
                                        }
                                    }
                                }
                                IsSkipItem = false;
                                //vlpdItem =dto.getSuggestedItem();

                                if (vlpdItem != null) {
                                    if (vlpdItem.getMcode() != null && vlpdItem.getMcode() != "") {

                                        UpDateUI(vlpdItem);


                                    } else {

                                        common.showUserDefinedAlertType(errorMessages.EMC_0043.replace("[Reference]", lblRefNo.getText()), getActivity(), getContext(), "Error");
                                        rlPick.setVisibility(View.VISIBLE);
                                        rlPrint.setVisibility(View.GONE);
                                        rlSelectReason.setVisibility(View.GONE);
                                        rlVLPD.setVisibility(View.GONE);
                                        ClearUIElemennts();
                                        ProgressDialogUtils.closeProgressDialog();
                                        return;
                                    }
                                }
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetItemtoPick_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetItemtoPick_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetItemtoPick_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    private void UpDateUI(ItemInfoDTO suggestedItem) {
        rlPick.setVisibility(View.VISIBLE);
        rlSelectReason.setVisibility(View.GONE);
        rlVLPD.setVisibility(View.GONE);
        rlPrint.setVisibility(View.GONE);

        if (suggestedItem != null) {
            ////Fill Outbound Information
            if (suggestedItem.getMcode() != null && suggestedItem.getMcode() != "") {
                vlpdTypeId = suggestedItem.getVlpdTypeId();
                lblSKU.setText(suggestedItem.getMcode());
                lblDesc.setText(suggestedItem.getDescription());
                lblBatch.setText(suggestedItem.getBatchNumber());
                lblLocation.setText(suggestedItem.getLocation());

                etQty.setText(suggestedItem.getReqQuantity().toString());
                lblBox.setText(suggestedItem.getHuNo() + "/" + suggestedItem.getHuSize());
                lblReqQty.setText(suggestedItem.getReqQuantity().toString());
                lblScannedBarcode.setText("");
                etQty.setText("");
                etQty.setEnabled(false);

                lblDock.setText(suggestedItem.getDock());
                lblRefNo.setText(suggestedItem.getRefDoc());

            } else {
                if (!lblSKU.getText().toString().isEmpty()) {
                    //   MessageBox.Show("No item pending to pick with ref: " + lblSKU.getText().toString());
                    ClearUIElemennts();
                    ProgressDialogUtils.closeProgressDialog();
                    common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Warning");
                    //MessageBox.Show("No items available to pick");
                    return;
                }
            }
        } else {
            clearFields();
            ProgressDialogUtils.closeProgressDialog();
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

    public void clearFields() {

        lblRefNo.setText("");
        lblDock.setText("");
        lblSKU.setText("");
        lblDesc.setText("");
        lblLocation.setText("");
        lblBox.setText("");
        lblReqQty.setText("");
        lblBatch.setText("");

        etQty.setText("");
        lblScannedBarcode.setText("");

        cvScanBarcode.setCardBackgroundColor(getResources().getColor(R.color.scanColor));
        ivScanBarcode.setImageResource(R.drawable.fullscreen_img);

    }


    public void ValidateBarcodeAndConfirmPicking() {

        try {

            if (lblSKU.getText().toString().isEmpty()) {
                clearFields();
                common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Error");
                return;
            }

            List<ItemInfoDTO> lstiteminfo = new ArrayList<>();
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDRequestDTO, getContext());
            VLPDRequestDTO vlpdRequestDTO = new VLPDRequestDTO();
            vlpdRequestDTO.setUserID(userId);
            vlpdRequestDTO.setVlpdID(vlpdId);
            vlpdRequestDTO.setUniqueRSN("");
            ItemInfoDTO oIteminfo = new ItemInfoDTO();
            oIteminfo = vlpdItem;
            oIteminfo.setEAN(lblScannedBarcode.getText().toString());
            oIteminfo.setUserScannedRSN("");
            oIteminfo.setPalletNumber("");
            oIteminfo.setUserRequestedQty(etQty.getText().toString());
            oIteminfo.setReqQuantity(lblReqQty.getText().toString());
            PickQty = etQty.getText().toString();
            lstiteminfo.add(oIteminfo);
            vlpdRequestDTO.setPickerRequestedInfo(lstiteminfo);
            message.setEntityObject(vlpdRequestDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

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

                                lblScannedBarcode.setText("");
                                etQty.setText("");

                                cvScanBarcode.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanBarcode.setImageResource(R.drawable.invalid_cross);

                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

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
                                if (dto.getSuggestedItem() != null) {
                                    for (ItemInfoDTO oiteminfo : dto.getSuggestedItem()) {
                                        vlpdItem = oiteminfo;
                                    }
                                }

                                if (dto.getSuggested()) {
                                    clearFields();
                                    lblReqQty.setText("");
                                    common.showUserDefinedAlertType(errorMessages.EMC_0043.replace("[Reference]", lblRefNo.getText()), getActivity(), getContext(), "Error");
                                    return;
                                }

                                if (IsRSNScanned && !(dto.getPreviousPickedItemResponce().get(0).getMessage().equals(etQty.getText().toString()))) {
                                    isPrintWindowRequired = true;
                                    OLDRSNNumber = lblScannedBarcode.getText().toString();
                                    pickedMMID = vlpdItem.getMaterialMasterId();

                                }
                                if (dto.getSuggestedItem() == null) {
                                    if (dto.getPreviousPickedItemResponce() != null) {
                                        if (dto.getPreviousPickedItemResponce().get(0).getMessage() != null) {
                                            if (dto.getPreviousPickedItemResponce().get(0).getStatus() == false) {
                                                common.showUserDefinedAlertType(dto.getPreviousPickedItemResponce().get(0).getMessage(), getActivity(), getContext(), "Error");

                                                etQty.setText("");
                                                etQty.setEnabled(false);

                                                lblScannedBarcode.setText(R.string.scan_barcode);
                                            } else {
                                                etQty.setText(dto.getPreviousPickedItemResponce().get(0).getMessage());
                                                etQty.setEnabled(true);

                                                common.setIsPopupActive(true);

                                                IsRSNScanned = true;
                                                common.showUserDefinedAlertType(errorMessages.EMC_0036, getActivity(), getContext(), "Error");
                                                return;


                                            }
                                        }
                                    } else {
                                        common.showUserDefinedAlertType(errorMessages.EMC_0037, getActivity(), getContext(), "Error");
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
    public void
    ProcessScannedinfo(String scannedData) {

        if (scannedData != null && !common.isPopupActive()) {

            // Checking For EAN Scan
            // restiring user for scanning Pallet And Location
            if (!ScanValidator.IsPalletScanned(scannedData) && !ScanValidator.IsLocationScanned(scannedData)
                    && !ScanValidator.IsRSNScanned(scannedData)) {
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

                ValidateBarcodeAndConfirmPicking();

                return;

            } else {

                lblScannedBarcode.setText("");

                cvScanBarcode.setCardBackgroundColor(getResources().getColor(R.color.white));
                ivScanBarcode.setImageResource(R.drawable.invalid_cross);
                common.showUserDefinedAlertType(errorMessages.EMC_0062, getActivity(), getContext(), "Error");
                return;
            }
        }
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
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_ecom_pickondemand));
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
