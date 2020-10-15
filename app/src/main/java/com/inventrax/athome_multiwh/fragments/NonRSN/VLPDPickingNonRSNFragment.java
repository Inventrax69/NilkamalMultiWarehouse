package com.inventrax.athome_multiwh.fragments.NonRSN;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
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

public class VLPDPickingNonRSNFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_PICK ON DEMAND HU";
    private View rootView;

    private RelativeLayout rlVLPD, rlPick, rlSelectReason, rlPrint;
    private TextView lblRefNo, lblDock, lblLocation, lblSKU, lblDesc, lblBatch, lblBox, lblReqQty, lblScannedBarcode, lblCaseNo;
    private CardView cvScanPallet, cvScanLocation, cvScanOldRsn, cvScanNewRsn;
    private ImageView ivScanPallet, ivScanLocation, ivScanOldRsn, ivScanNewRsn;
    private TextInputLayout txtInputLayoutVLPD, txtInputLayoutPallet, txtInputLayoutQty;
    private SearchableSpinner spinnerVlpdNo, spinnerSelectReason;
    private CustomEditText etPallet, etQty, etOldRsn, etNewRsn, etQtyPrint, etPrinterIP;
    private Button btnOk, btnClose, btnSkip, btnPick, btnCloseOne, btnSkipItem, btnCloseTwo, btnPrint, btnClosePrint;
    private boolean _isPrintWindowRequired = false;
    private String _oldRSNNumber = null, PickQty = null;
    FragmentUtils fragmentUtils;
    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    private WMSCoreMessage core;
    String userId = null, materialType = null, vlpdId = "", vlpdTypeId = null, SkipReason = null, vlpdNo = "";

    double UserrequestedQty = 0, RequiredQty = 0;
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    String clientId = null;
    ArrayList<String> sloc;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    ToneGenerator toneGenerator;
    private ErrorMessages errorMessages;
    private SoundUtils soundUtils;

    ItemInfoDTO vlpdItem = null;
    private boolean isPrintWindowRequired = false;
    private String OLDRSNNumber = "", pickedMMID = null;
    private String ipAddress = null, printerIPAddress = null;
    private boolean IsSkipItem = false, IsRSNScanned = false, isLocationScanned = false;
    VLPDResponseDTO vlpdresponseobj = null;
    List<VlpdDto> lstVlpdDto = null;
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };


    public VLPDPickingNonRSNFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.hu_fragment_vlpdpick_non_rsn, container, false);
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
        lblCaseNo = (TextView) rootView.findViewById(R.id.lblCaseNo);
        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);

        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        vlpdresponseobj = new VLPDResponseDTO();
        txtInputLayoutPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPallet);
        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);
        txtInputLayoutVLPD = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutVLPD);

        etPallet = (CustomEditText) rootView.findViewById(R.id.etPallet);
        etQty = (CustomEditText) rootView.findViewById(R.id.etQty);
        etOldRsn = (CustomEditText) rootView.findViewById(R.id.etOldRsn);
        etNewRsn = (CustomEditText) rootView.findViewById(R.id.etNewRsn);
        etQtyPrint = (CustomEditText) rootView.findViewById(R.id.etQtyPrint);
        etPrinterIP = (CustomEditText) rootView.findViewById(R.id.etPrinterIP);

        cvScanOldRsn = (CardView) rootView.findViewById(R.id.cvScanOldRsn);
        cvScanNewRsn = (CardView) rootView.findViewById(R.id.cvScanNewRsn);

        ivScanNewRsn = (ImageView) rootView.findViewById(R.id.ivScanNewRsn);
        ivScanOldRsn = (ImageView) rootView.findViewById(R.id.ivScanOldRsn);

        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);
        btnCloseTwo = (Button) rootView.findViewById(R.id.btnCloseTwo);
        btnSkip = (Button) rootView.findViewById(R.id.btnSkip);
        btnPick = (Button) rootView.findViewById(R.id.btnPick);

        btnSkipItem = (Button) rootView.findViewById(R.id.btnSkipItem);
        btnOk = (Button) rootView.findViewById(R.id.btnOk);
        btnClose = (Button) rootView.findViewById(R.id.btnClose);
        btnPrint = (Button) rootView.findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(this);
        btnClosePrint = (Button) rootView.findViewById(R.id.btnClosePrint);
        btnClosePrint.setOnClickListener(this);
        etQty.setEnabled(false);
        etPallet.setEnabled(false);
        spinnerVlpdNo = (SearchableSpinner) rootView.findViewById(R.id.spinnerVlpdNo);
        spinnerVlpdNo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vlpdNo = spinnerVlpdNo.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
        btnPick.setOnClickListener(this);


        btnOk.setOnClickListener(this);
        btnClose.setOnClickListener(this);

        sloc = new ArrayList<>();

        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        soundUtils = new SoundUtils();
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

        getVLPDsForNonRSN();
        //To get skip items
        LoadSkipReason();
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
                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnOk:
                if (!vlpdNo.equalsIgnoreCase("")) {
                    GetVLPDID(vlpdNo);
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_084, getActivity(), getContext(), "Warning");
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
                if (isLocationScanned) {
                    rlSelectReason.setVisibility(View.VISIBLE);
                    rlPick.setVisibility(View.GONE);
                    rlPrint.setVisibility(View.GONE);
                    rlVLPD.setVisibility(View.GONE);
                }
                break;

            case R.id.btnPick:
                common.setIsPopupActive(false);

                if (isLocationScanned) {

                    int reqQty, enteredQty;
                    reqQty = (int) Double.parseDouble(lblReqQty.getText().toString());
                    enteredQty = (int) Double.parseDouble(etQty.getText().toString());

                    if (reqQty >= enteredQty) {
                        ValidateNONRSNSKUAndConfirmPicking();
                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_073, getActivity(), getActivity(), "Error");
                    }

                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getActivity(), "Error");
                }

                /*if (IsRSNScanned && !(vlpdresponseobj.getPreviousPickedItemResponce().get(0).getMessage().equals(etQty.getText().toString()))) {
                    isPrintWindowRequired = true;
                    _oldRSNNumber = lblScannedBarcode.getText().toString();
                    pickedMMID = vlpdItem.getMaterialMasterId();

                }
                ValidateBarcodeAndConfirmPicking();
                _oldRSNNumber = lblScannedBarcode.getText().toString();
                if (_isPrintWindowRequired) {
                    rlPrint.setVisibility(View.VISIBLE);
                    rlPick.setVisibility(View.GONE);
                    rlSelectReason.setVisibility(View.GONE);
                    etOldRsn.setText(_oldRSNNumber);
                    cvScanOldRsn.setCardBackgroundColor(getResources().getColor(R.color.white));
                    ivScanOldRsn.setImageResource(R.drawable.check);
                    GetNewlyGeneratedRSNNumberByRSNNumber();

                    return;
                }
*/
                break;
            case R.id.btnPrint:
                printValidations();
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrintRSNnumber_01", getActivity());
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
                                    cvScanNewRsn.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanNewRsn.setImageResource(R.drawable.warning_img);
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrintRSNnumber_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrintRSNnumber_03", getActivity());
                logException();

            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
        }
    }

    public void goBackToNormalView() {
        rlPick.setVisibility(View.VISIBLE);
        rlPrint.setVisibility(View.GONE);
        IsRSNScanned = false;
        isPrintWindowRequired = false;
        rlSelectReason.setVisibility(View.GONE);
        rlVLPD.setVisibility(View.GONE);
        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
        ivScanPallet.setImageResource(R.drawable.check);


        //loadPalleClearFields();
    }

    public void GetVLPDID(String vlpdNo) {

        for (VlpdDto oVlpdDto : lstVlpdDto) {
            if (oVlpdDto.getvLPDNumber().equals(vlpdNo)) {
                vlpdId = oVlpdDto.getiD();
                getItemToPick();
            }
        }

    }

    public void ClearFields() {

        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanLocation.setImageResource(R.drawable.fullscreen_img);

        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

        etQty.setText("");
        lblScannedBarcode.setText("");
        etPallet.setText("");

    }

    public void updateSkipReason() {

        if (!SkipReason.equals("Select")) {
            IsSkipItem = true;
           /* pickRequest.userID = Program.Userid;
            pickRequest.userIDSpecified = true;
            vlpdItem.SkipReason = cmbSkipReason.SelectedItem.ToString();
            vlpdItem.RequestType = "SKIP";
            pickRequest.pickerRequestedInfo = vlpdItem;*/
            if (isLocationScanned) {

                getItemToPick();

            } else {
                common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getActivity(), "Error");
            }
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

    private void getVLPDsForNonRSN() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDRequestDTO, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setType("1");
            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                call = apiService.GetVLPDsForNonRSN(message);
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
                                List<String> lstvlpdnumbers = new ArrayList<String>();
                                VlpdDto dto = null;

                                lstVlpdDto = new ArrayList<>();

                                for (int i = 0; i < _lstvlpd.size(); i++) {
                                    dto = new VlpdDto(_lstvlpd.get(i).entrySet());
                                    lstvlpdnumbers.add(dto.getvLPDNumber());
                                    lstVlpdDto.add(dto);
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                ArrayAdapter arrayAdapterStoreRefNo = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstvlpdnumbers);
                                spinnerVlpdNo.setAdapter(arrayAdapterStoreRefNo);

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

    public void getItemToPick() {

        try {
            List<ItemInfoDTO> lstiteminfo = new ArrayList<>();
            ItemInfoDTO oItem = new ItemInfoDTO();
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDRequestDTO, getContext());
            VLPDRequestDTO vlpdRequestDTO = new VLPDRequestDTO();
            vlpdRequestDTO.setUserID(userId);
            vlpdRequestDTO.setVlpdID(vlpdId);
            vlpdRequestDTO.setIsRSN("0");
            if (vlpdItem != null) {
                oItem = vlpdItem;
            }
            oItem.setRequestType("PICK");

            if (IsSkipItem) {
                oItem.setRequestType("SKIP");
                oItem.setSkipReason(SkipReason);
                oItem.setUserRequestedQty(etQty.getText().toString());
                oItem.setMcode(lblSKU.getText().toString());
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

                                if (dto.getSuggested()) {

                                    lblBox.setText("");
                                    lblReqQty.setText("");
                                    common.showUserDefinedAlertType("No item pending to pick with ref:" + lblRefNo.getText().toString(), getActivity(), getContext(), "Error");
                                    ClearFields();
                                    ClearUIElemennts();
                                    return;
                                }


                                for (ItemInfoDTO oiteminfo : dto.getSuggestedItem()) {
                                    vlpdItem = oiteminfo;
                                }


                                IsSkipItem = false;
                                //vlpdItem =dto.getSuggestedItem();

                                if (vlpdItem != null) {
                                    if (vlpdItem.getMcode() != null && vlpdItem.getMcode() != "") {

                                        UpDateUI(vlpdItem);

                                      /*  if (isPrintWindowRequired) {
                                            ShowPrintPanel();
                                        }*/
                                    } else {
                                        if (isPrintWindowRequired) {
                                            ShowPrintPanel();
                                        }

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

    private void ShowPrintPanel() {
        rlPick.setVisibility(View.GONE);
        rlPrint.setVisibility(View.VISIBLE);
        rlSelectReason.setVisibility(View.GONE);
        rlVLPD.setVisibility(View.GONE);
        try {
            GetNewlyGeneratedRSNNumberByRSNNumber();
        } catch (Exception EX) {
             //
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetNewlyGeneratedRSNNumberByRSNNumber_01", getActivity());
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
                                    etQtyPrint.setText(PickQty);
                                    etOldRsn.setText(_oldRSNNumber);
                                    cvScanNewRsn.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanNewRsn.setImageResource(R.drawable.check);
                                    cvScanOldRsn.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanOldRsn.setImageResource(R.drawable.check);
                                    // ConfirmBinTransferToPallet();

                                }
                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetNewlyGeneratedRSNNumberByRSNNumber_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetNewlyGeneratedRSNNumberByRSNNumber_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetNewlyGeneratedRSNNumberByRSNNumber_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }

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

        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanLocation.setImageResource(R.drawable.fullscreen_img);

        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);


        etQty.setText("");
        lblScannedBarcode.setText("");
        etPallet.setText("");

        btnPick.setTextColor(getResources().getColor(R.color.black));
        btnPick.setBackgroundResource(R.drawable.button_hide);
        btnPick.setEnabled(false);
    }

    public void ValidateNONRSNSKUAndConfirmPicking() {

        try {

            if (lblSKU.getText().toString().isEmpty()) {
                common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Error");
                btnPick.setEnabled(false);
                return;
            }
            if (!etQty.getText().toString().isEmpty()) {
                UserrequestedQty = Double.parseDouble(etQty.getText().toString());
            }
            RequiredQty = Double.parseDouble(lblReqQty.getText().toString());
            if (UserrequestedQty > RequiredQty) {
                common.showUserDefinedAlertType(errorMessages.EMC_0064, getActivity(), getContext(), "Error");
                return;
            }
            List<ItemInfoDTO> lstiteminfo = new ArrayList<>();
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDRequestDTO, getContext());
            VLPDRequestDTO vlpdRequestDTO = new VLPDRequestDTO();
            vlpdRequestDTO.setUserID(userId);
            vlpdRequestDTO.setVlpdID(vlpdId);
            /*double db = Double.parseDouble(lblReqQty.getText().toString());
            int x = (int) db;
            vlpdRequestDTO.setReqQuantity(15); ///added by hemnath*/

            vlpdRequestDTO.setUniqueRSN(lblScannedBarcode.getText().toString());
            // For Non RSN case it is 0 For RSN picking it 1
            vlpdRequestDTO.setIsRSN("0");
            ItemInfoDTO oIteminfo = new ItemInfoDTO();
            oIteminfo = vlpdItem;

            oIteminfo.setPalletNumber(etPallet.getText().toString());
            oIteminfo.setMcode(lblSKU.getText().toString());
            oIteminfo.setReqQuantity(lblReqQty.getText().toString());
            oIteminfo.setUserScannedRSN(lblScannedBarcode.getText().toString());
            oIteminfo.setUserRequestedQty(etQty.getText().toString());
            oIteminfo.setItem_SerialNumber(lblCaseNo.getText().toString());
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
                call = apiService.ValidateNONRSNSKUAndConfirmPicking(message);
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
                                    vlpdresponseobj = dto;
                                    lstDto.add(dto);
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                if (isPrintWindowRequired) {
                                    ShowPrintPanel();

                                }
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

                                //vlpdItem =dto.getSuggestedItem();
                                ProgressDialogUtils.closeProgressDialog();
                                if (vlpdItem != null) {
                                    if (vlpdItem.getMcode() != null && vlpdItem.getMcode() != "") {

                                        UpDateUI(vlpdItem);
                                        if (isPrintWindowRequired) {
                                            ShowPrintPanel();
                                        }

                                        if (lblRefNo.getText().toString().isEmpty()) {
                                            //MessageBox.Show("No item pending to pick with ref: " + lblRefNumberValue.Text);
                                            ClearUIElemennts();

                                        } else {
                                        }
                                    } else {
                                        common.showUserDefinedAlertType(errorMessages.EMC_0043.replace("[Reference]", lblRefNo.getText()), getActivity(), getContext(), "Error");
                                        return;
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

            if (!ProgressDialogUtils.isProgressActive()) {

                if (ScanValidator.IsLocationScanned(scannedData)) {
                    if (lblLocation.getText().toString().equalsIgnoreCase(scannedData)) {
                        isLocationScanned = true;
                        etQty.setEnabled(true);
                        etQty.requestFocus();

                        cvScanLocation.setCardBackgroundColor(Color.WHITE);
                        ivScanLocation.setImageResource(R.drawable.check);
                    } else {
                        cvScanLocation.setCardBackgroundColor(Color.WHITE);
                        ivScanLocation.setImageResource(R.drawable.invalid_cross);
                        common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getActivity(), "Error");
                    }
                } /*else if (ScanValidator.IsPalletScanned(scannedData)) {
                    *//*if (lblSKU.getText().toString().isEmpty()) {
                        clearFields();
                        common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Error");
                        return;
                    }*//*
                    etPallet.setText(scannedData);

                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                    ivScanPallet.setImageResource(R.drawable.check);

                    return;
                } */else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0045, getActivity(), getContext(), "Error");
                    return;
                }

            } else {
                if (!common.isPopupActive()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_081, getActivity(), getContext(), "Error");

                }
                soundUtils.alertWarning(getActivity(), getContext());

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
            oIteminfo.setPalletNumber(etPallet.getText().toString());
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_nonrsn_vlpd_Picking));
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