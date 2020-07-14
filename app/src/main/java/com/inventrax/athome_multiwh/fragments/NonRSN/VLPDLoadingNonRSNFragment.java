package com.inventrax.athome_multiwh.fragments.NonRSN;

import android.content.BroadcastReceiver;
import android.content.Context;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class VLPDLoadingNonRSNFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_VLPDLoadingNonRSNFragment";
    private View rootView;

    private RelativeLayout rlVLPDNoSelect, rlLoading, rlSelectSKU;
    private SearchableSpinner spinnerLoadRefNo;
    private Button btnGo, btnCancel, btnCloseLoading, btnConfirm, btnCloseItemSelection;
    private TextView tvLoadRefNum, tvSelectedGrpVlpdNo;
    private RecyclerView rvItemsList;
    LinearLayoutManager linearLayoutManager;

    private Gson gson;
    private WMSCoreMessage core;
    List<List<StorageLocationDTO>> sloc;
    List<String> lstStorageloc;
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
    int mmId ;

    TableLayout tl;

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
            rootView = inflater.inflate(R.layout.hu_fragment_vlpdloading_non_rsn, container, false);
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
            rlSelectSKU = (RelativeLayout) rootView.findViewById(R.id.rlSelectSKU);
            rlLoading = (RelativeLayout) rootView.findViewById(R.id.rlLoading);

            tvLoadRefNum = (TextView) rootView.findViewById(R.id.tvLoadRefNum);
            tvSelectedGrpVlpdNo = (TextView) rootView.findViewById(R.id.tvSelectedGrpVlpdNo);

            rvItemsList = (RecyclerView) rootView.findViewById(R.id.rvItemsList);
            linearLayoutManager = new LinearLayoutManager(getActivity());
            rvItemsList.setLayoutManager(linearLayoutManager);

            rlVLPDNoSelect.setVisibility(View.VISIBLE);
            rlLoading.setVisibility(View.GONE);

            spinnerLoadRefNo = (SearchableSpinner) rootView.findViewById(R.id.spinnerLoadRefNo);
            spinnerLoadRefNo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    loadRefNo = spinnerLoadRefNo.getSelectedItem().toString();

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }

            });

            btnGo = (Button) rootView.findViewById(R.id.btnGo);
            btnGo.setOnClickListener(this);

            btnCancel = (Button) rootView.findViewById(R.id.btnCancel);
            btnCancel.setOnClickListener(this);

            btnCloseLoading = (Button) rootView.findViewById(R.id.btnCloseLoading);
            btnCloseLoading.setOnClickListener(this);

            btnCloseItemSelection = (Button) rootView.findViewById(R.id.btnCloseItemSelection);
            btnCloseItemSelection.setOnClickListener(this);

            btnConfirm = (Button) rootView.findViewById(R.id.btnConfirm);
            btnConfirm.setOnClickListener(this);
            btnConfirm.setEnabled(false);

            tl = (TableLayout) rootView.findViewById(R.id.DetailsTable);

            gson = new GsonBuilder().create();
            core = new WMSCoreMessage();
            sloc = new ArrayList<>();
            lstVLPDDTO = new ArrayList<>();
            lstStorageloc = new ArrayList<String>();

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


        } else {
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0025);
            // soundUtils.alertSuccess(LoginActivity.this,getBaseContext());
            return;
        }

        GetNonRSNOpenRefNumberList();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnGo:

                if (!loadRefNo.equalsIgnoreCase("Select")) {

                    GetVLPDID(loadRefNo);
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0042, getActivity(), getActivity(), "Error");
                }

                break;
            case R.id.btnCancel:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;

            case R.id.btnCloseLoading:
                rlSelectSKU.setVisibility(View.VISIBLE);
                rlLoading.setVisibility(View.GONE);
                rlVLPDNoSelect.setVisibility(View.GONE);

                tl.removeAllViews();

                GetNonRSNVLPDSKUList();

                break;

            case R.id.btnCloseItemSelection:
                rlSelectSKU.setVisibility(View.GONE);
                rlLoading.setVisibility(View.GONE);
                rlVLPDNoSelect.setVisibility(View.VISIBLE);


                break;


            case R.id.btnConfirm:
                confirmNonRSNVLPDLoading();
                break;


            default:
                break;
        }

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


    }

    private void GetNonRSNOpenRefNumberList() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setiD(userId);
            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

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

    public void GetVLPDID(String loadRefNo) {

        for (VlpdDto oVlpdDto : lstVlpdDto) {
            if (oVlpdDto.getvLPDNumber().equals(loadRefNo)) {
                vlpdId = oVlpdDto.getiD();

                GetNonRSNVLPDSKUList();


            }
        }

    }

    private void GetNonRSNVLPDSKUList() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setvLPDNumber(loadRefNo);
            vlpdDto.setiD(vlpdId);
            vlpdDto.setType("0");

            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);
            try {
                call = apiService.GetNonRSNVLPDSKUList(message);
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
                                rvItemsList.setAdapter(null);
                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                            } else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lResponse = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lResponse = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                ItemInfoDTO dto = null;
                                final List<ItemInfoDTO> itemDto = new ArrayList<>();
                                for (int i = 0; i < _lResponse.size(); i++) {
                                    dto = new ItemInfoDTO(_lResponse.get(i).entrySet());
                                    itemDto.add(dto);
                                }

                                rlVLPDNoSelect.setVisibility(View.GONE);
                                rlSelectSKU.setVisibility(View.VISIBLE);

                                tvSelectedGrpVlpdNo.setText(loadRefNo);

                                rvItemsList.setAdapter(null);

                                SKUListAdapterNonRSNLoading skuListAdapterNonRSNLoading = new SKUListAdapterNonRSNLoading(getActivity(), itemDto, new SKUListAdapterNonRSNLoading.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(int pos) {
                                        mmId = Integer.parseInt(itemDto.get(pos).getMaterialMasterId());
                                        getNonRSNVLPDSKUPendingDetails();
                                    }
                                });
                                rvItemsList.setAdapter(skuListAdapterNonRSNLoading);

                                ProgressDialogUtils.closeProgressDialog();

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

    private void getNonRSNVLPDSKUPendingDetails() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.ItemInfoDTO, getContext());

            ItemInfoDTO infoDTO = new ItemInfoDTO();
            infoDTO.setMaterialMasterId(String.valueOf(mmId));
            infoDTO.setVlpdAssgnmentId(vlpdId);

            message.setEntityObject(infoDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);
            try {
                call = apiService.GetNonRSNVLPDSKUPendingDetails(message);
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

                                ItemInfoDTO dto = null;
                                final List<ItemInfoDTO> itemDto = new ArrayList<>();
                                for (int i = 0; i < _lResponse.size(); i++) {
                                    dto = new ItemInfoDTO(_lResponse.get(i).entrySet());
                                    itemDto.add(dto);
                                }

                                btnConfirm.setEnabled(true);

                                rlVLPDNoSelect.setVisibility(View.GONE);
                                rlSelectSKU.setVisibility(View.GONE);
                                rlLoading.setVisibility(View.VISIBLE);

                                tvLoadRefNum.setText(loadRefNo);
                                addHeaders();
                                addData(itemDto);

                                ProgressDialogUtils.closeProgressDialog();

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

    private void confirmNonRSNVLPDLoading() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.ItemInfoDTO, getContext());

            ItemInfoDTO infoDTO = new ItemInfoDTO();
            infoDTO.setMaterialMasterId(String.valueOf(mmId));
            infoDTO.setVlpdAssgnmentId(vlpdId);

            message.setEntityObject(infoDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);
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

                                    ProgressDialogUtils.closeProgressDialog();
                                    rlLoading.setVisibility(View.GONE);
                                    rlSelectSKU.setVisibility(View.VISIBLE);
                                    tl.removeAllViews();

                                    GetNonRSNVLPDSKUList();
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


    private TextView getTextView(String title, int color, int typeface, int bgColor) {
        TextView tv = new TextView(getContext());
        tv.setText(title);
        tv.setTextColor(color);
        tv.setPadding(10, 10, 10, 10);
        tv.setTypeface(Typeface.DEFAULT, typeface);
        tv.setBackgroundColor(bgColor);
        tv.setLayoutParams(getLayoutParams());
        tv.setOnClickListener(this);
        return tv;
    }

    @NonNull
    private TableRow.LayoutParams getLayoutParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(2, 0, 0, 2);
        return params;
    }

    @NonNull
    private TableLayout.LayoutParams getTblLayoutParams() {
        return new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
    }

    /**
     * This function add the headers to the table
     **/
    public void addHeaders() {

        TableRow tr = new TableRow(getContext());

        tr.setLayoutParams(getLayoutParams());

        tr.addView(getTextView("SKU", Color.WHITE, Typeface.BOLD, R.color.parentColor));
        tr.addView(getTextView("Desc.", Color.WHITE, Typeface.BOLD, R.color.parentColor));
        tr.addView(getTextView("Batch", Color.WHITE, Typeface.BOLD, R.color.parentColor));
        tr.addView(getTextView("HU", Color.WHITE, Typeface.BOLD, R.color.parentColor));
        tr.addView(getTextView("Qty.", Color.WHITE, Typeface.BOLD, R.color.parentColor));


        tl.addView(tr, getTblLayoutParams());
    }

    public void addData(List<ItemInfoDTO> skuInfo) {

        TableLayout tl = (TableLayout) rootView.findViewById(R.id.DetailsTable);
        for (ItemInfoDTO data : skuInfo) {
            TableRow tr = new TableRow(getContext());
            tr.setLayoutParams(getLayoutParams());

            tr.addView(getTextView(data.getMcode(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.white)));
            tr.addView(getTextView(data.getDescription().toString(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.white)));
            tr.addView(getTextView(data.getBatchNumber(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.white)));
            tr.addView(getTextView(data.getHuNo()+ "/" + data.getHuSize(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.white)));
            tr.addView(getTextView(data.getPickedQuantity(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.white)));


            tl.addView(tr, getTblLayoutParams());
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
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

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

