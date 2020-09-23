package com.inventrax.athome_multiwh.fragments.NonRSN;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.inventrax.athome_multiwh.adapters.SKUListAdapterNonRSNLoading;
import com.inventrax.athome_multiwh.common.Common;
import com.inventrax.athome_multiwh.common.constants.EndpointConstants;
import com.inventrax.athome_multiwh.common.constants.ErrorMessages;
import com.inventrax.athome_multiwh.fragments.HomeFragment;
import com.inventrax.athome_multiwh.interfaces.ApiInterface;
import com.inventrax.athome_multiwh.pojos.InboundDTO;
import com.inventrax.athome_multiwh.pojos.ItemInfoDTO;
import com.inventrax.athome_multiwh.pojos.StorageLocationDTO;
import com.inventrax.athome_multiwh.pojos.VLPDLoadingDTO;
import com.inventrax.athome_multiwh.pojos.VlpdDto;
import com.inventrax.athome_multiwh.pojos.WMSCoreMessage;
import com.inventrax.athome_multiwh.pojos.WMSExceptionMessage;
import com.inventrax.athome_multiwh.searchableSpinner.SearchableSpinner;
import com.inventrax.athome_multiwh.services.RestService;
import com.inventrax.athome_multiwh.util.DialogUtils;
import com.inventrax.athome_multiwh.util.ExceptionLoggerUtils;
import com.inventrax.athome_multiwh.util.FragmentUtils;
import com.inventrax.athome_multiwh.util.NetworkUtils;
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


public class NewVLPDLoadingNonRSNFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_VLPDLoadingNonRSNFragment";
    private View rootView;
    private RelativeLayout rlVLPDNoSelect, rlNonRsnSku;
    private SearchableSpinner spinnerLoadRefNo;
    private Button btnGo, btnCancel,btnSubmit,btnClear,btnCloseTwo;
    private Gson gson;
    private WMSCoreMessage core;
    List<InboundDTO> lstInbound = null;
    private Common common;
    String userId = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    String clientId = "", loadRefNo = "", materialType = "", vlpdId = "";
    private List<VlpdDto> lstVLPDDTO;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    List<VlpdDto> lstVlpdDto = null;
    int mmId;
    TextView lblVLPDNumber,lblScannedItem,lblPickQty,lblLoadQty,lblDesc;
    EditText etQty;

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (NetworkUtils.isInternetAvailable(getContext())) {
            rootView = inflater.inflate(R.layout.hu_fragment_vlpdloading_non_rsn_new, container, false);
            loadFormControls();
        } else {
            DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
            // soundUtils.alertSuccess(LoginActivity.this,getBaseContext());
            return rootView;
        }
        return rootView;
    }

    public void loadFormControls() {

        if (NetworkUtils.isInternetAvailable(getContext())) {

            rlVLPDNoSelect = (RelativeLayout) rootView.findViewById(R.id.rlVLPDNoSelect);
            rlNonRsnSku = (RelativeLayout) rootView.findViewById(R.id.rlNonRsnSku);

            lblVLPDNumber = (TextView) rootView.findViewById(R.id.lblVLPDNumber);
            lblScannedItem = (TextView) rootView.findViewById(R.id.lblScannedItem);
            lblPickQty = (TextView) rootView.findViewById(R.id.lblPickQty);
            lblLoadQty = (TextView) rootView.findViewById(R.id.lblLoadQty);
            lblDesc = (TextView) rootView.findViewById(R.id.lblDesc);

            etQty = (EditText) rootView.findViewById(R.id.etQty);

            rlVLPDNoSelect.setVisibility(View.VISIBLE);
            rlNonRsnSku.setVisibility(View.GONE);

            spinnerLoadRefNo = (SearchableSpinner) rootView.findViewById(R.id.spinnerLoadRefNo);
            spinnerLoadRefNo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    loadRefNo = spinnerLoadRefNo.getSelectedItem().toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }

            });

            btnGo = (Button) rootView.findViewById(R.id.btnGo);
            btnCancel = (Button) rootView.findViewById(R.id.btnCancel);
            btnSubmit = (Button) rootView.findViewById(R.id.btnSubmit);
            btnClear = (Button) rootView.findViewById(R.id.btnClear);
            btnCloseTwo = (Button) rootView.findViewById(R.id.btnCloseTwo);

            btnGo.setOnClickListener(this);
            btnCancel.setOnClickListener(this);
            btnSubmit.setOnClickListener(this);
            btnClear.setOnClickListener(this);
            btnCloseTwo.setOnClickListener(this);

            gson = new GsonBuilder().create();
            core = new WMSCoreMessage();

            SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
            userId = sp.getString("RefUserId", "");
            materialType = sp.getString("division", "");

            common = new Common();
            exceptionLoggerUtils = new ExceptionLoggerUtils();
            errorMessages = new ErrorMessages();
            lstInbound = new ArrayList<InboundDTO>();
            clientId = null;

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

            GetNonRSNOpenRefNumberList();

        } else {
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0025);
            // soundUtils.alertSuccess(LoginActivity.this,getBaseContext());
            return;
        }



    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnGo:
                if (!loadRefNo.equalsIgnoreCase("Select") && !loadRefNo.isEmpty()) {
                    for (VlpdDto oVlpdDto : lstVlpdDto) {
                        if (oVlpdDto.getvLPDNumber().equals(loadRefNo)) {
                            vlpdId = oVlpdDto.getiD();
                        }
                    }
                    rlVLPDNoSelect.setVisibility(View.GONE);
                    rlNonRsnSku.setVisibility(View.VISIBLE);
                    lblVLPDNumber.setText(loadRefNo);
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0042, getActivity(), getActivity(), "Error");
                }
                break;

            case R.id.btnCancel:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;

            case R.id.btnCloseTwo:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;

            case R.id.btnClear:
                ClearUI();
                break;

            case R.id.btnSubmit:

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

                break;

            default:
                break;
        }

    }

    private void ClearUI() {
        lblScannedItem.setText("");
        lblDesc.setText("");
        lblPickQty.setText("");
        lblLoadQty.setText("");
        etQty.setText("");
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

    public void ProcessScannedinfo(String scannedData) {

        if (scannedData != null && !common.isPopupActive()) {

                if(rlNonRsnSku.getVisibility()==View.VISIBLE){
                    getNONRSNSKULoadingInfo(scannedData);
/*                    if(ScanValidator.IsRTRBarcodeScanned(scannedData)){

                    }else{
                        common.showUserDefinedAlertType("Invalid barcode", getActivity(), getContext(), "Warning");
                    }*/

                }

        } else {
            common.showUserDefinedAlertType(errorMessages.EMC_0051, getActivity(), getContext(), "Error");
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

    private void GetNonRSNOpenRefNumberList() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setiD(userId);
            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                call = apiService.GetNonRSNOpenRefNumberList(message);
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
                                spinnerLoadRefNo.setAdapter(arrayAdapterStoreRefNo);

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

                            // if any Exception throws
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    ProgressDialogUtils.closeProgressDialog();
                                    return;
                                }
                            } else {
                                ProgressDialogUtils.closeProgressDialog();
                                LinkedTreeMap<String, String> _lResultvalue = new LinkedTreeMap<String, String>();
                                _lResultvalue = (LinkedTreeMap<String, String>) core.getEntityObject();
                                for (Map.Entry<String, String> entry : _lResultvalue.entrySet()) {
                                    if (entry.getKey().equals("Result")) {
                                        String Result = entry.getValue();
                                        if (Result.equals("0")) {
                                            return;
                                        } else {
                                            exceptionLoggerUtils.deleteFile(getActivity());
                                            ProgressDialogUtils.closeProgressDialog();
                                            return;
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {

                            /*try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002",getContext());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            logException();*/

                            ProgressDialogUtils.closeProgressDialog();
                            //Log.d("Message", core.getEntityObject().toString());
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
                // Toast.makeText(LoginActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_vlpd_loading));
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

}

