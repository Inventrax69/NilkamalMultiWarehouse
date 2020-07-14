package com.inventrax.athome_multiwh.fragments.NonRSN;


import android.app.Activity;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.inventrax.athome_multiwh.adapters.ItemListNinToBinNonRSn;
import com.inventrax.athome_multiwh.common.Common;
import com.inventrax.athome_multiwh.common.constants.EndpointConstants;
import com.inventrax.athome_multiwh.common.constants.ErrorMessages;
import com.inventrax.athome_multiwh.fragments.HomeFragment;
import com.inventrax.athome_multiwh.interfaces.ApiInterface;
import com.inventrax.athome_multiwh.pojos.InternalTransferDTO;
import com.inventrax.athome_multiwh.pojos.InventoryDTO;
import com.inventrax.athome_multiwh.pojos.ItemInfoDTO;
import com.inventrax.athome_multiwh.pojos.VLPDRequestDTO;
import com.inventrax.athome_multiwh.pojos.WMSCoreMessage;
import com.inventrax.athome_multiwh.pojos.WMSExceptionMessage;
import com.inventrax.athome_multiwh.services.RestService;
import com.inventrax.athome_multiwh.util.ExceptionLoggerUtils;
import com.inventrax.athome_multiwh.util.ExpandableHeightGridView;
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

public class NonRSNBinToBinFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {
    private static final String classCode = "API_FRAG_NonRSNBinToBinFragment";
    private View rootView;

    private RelativeLayout rlSelection, rlNonRSNSkuList, rlMapToPallet;
    private Button btnLoadPallet, btnBinMap, btnCancel, btnCloseTransferSelection, btnMapItemstoPallet, btnCloseMapPallet;
    private CardView cvScanLocation, cvScanPallet;
    private ImageView ivScanLocation, ivScanPallet;
    private EditText etLocation, etPallet;
    private RecyclerView rvItemsList;
    private LinearLayoutManager linearLayoutManager;
    private ExpandableHeightGridView listViewItems;

    private Boolean IsValidLocationorPallet = false, IsPalletScanned = false;

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

    private int mmId;
    private String location = "";
    private List<ItemInfoDTO> itemInfoDTOList, finalizedList;


    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public NonRSNBinToBinFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_nonrsn_bintobin, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");

        rlSelection = (RelativeLayout) rootView.findViewById(R.id.rlSelection);
        rlNonRSNSkuList = (RelativeLayout) rootView.findViewById(R.id.rlNonRSNSkuList);
        rlMapToPallet = (RelativeLayout) rootView.findViewById(R.id.rlMapToPallet);

        btnLoadPallet = (Button) rootView.findViewById(R.id.btnLoadPallet);
        btnBinMap = (Button) rootView.findViewById(R.id.btnBinMap);
        btnCancel = (Button) rootView.findViewById(R.id.btnCancel);
        btnCloseTransferSelection = (Button) rootView.findViewById(R.id.btnCloseTransferSelection);
        btnMapItemstoPallet = (Button) rootView.findViewById(R.id.btnMapItemstoPallet);
        btnCloseMapPallet = (Button) rootView.findViewById(R.id.btnCloseMapPallet);

        btnLoadPallet.setOnClickListener(this);
        btnBinMap.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnCloseTransferSelection.setOnClickListener(this);
        btnMapItemstoPallet.setOnClickListener(this);
        btnCloseMapPallet.setOnClickListener(this);


        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);

        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);

        etLocation = (EditText) rootView.findViewById(R.id.etLocation);
        etPallet = (EditText) rootView.findViewById(R.id.etPallet);

        rvItemsList = (RecyclerView) rootView.findViewById(R.id.rvItemsList);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        rvItemsList.setLayoutManager(linearLayoutManager);

        listViewItems = (ExpandableHeightGridView) rootView.findViewById(R.id.listViewItems);

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
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;

            case R.id.btnBinMap:
                FragmentUtils.addFragment(getActivity(), R.id.container_body, new NonRSNBinMappingFragment());
                break;

            case R.id.btnLoadPallet:
                rlSelection.setVisibility(View.GONE);
                rlMapToPallet.setVisibility(View.GONE);
                rlNonRSNSkuList.setVisibility(View.VISIBLE);
                break;
            case R.id.btnCloseTransferSelection:
                rlSelection.setVisibility(View.VISIBLE);
                rlMapToPallet.setVisibility(View.GONE);
                rlNonRSNSkuList.setVisibility(View.GONE);
                rvItemsList.setAdapter(null);
                etLocation.setText("");
                etPallet.setText("");
                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                ivScanLocation.setImageResource(R.drawable.fullscreen_img);
                ivScanPallet.setImageResource(R.drawable.fullscreen_img);
                break;


            case R.id.btnCloseMapPallet:
                rlSelection.setVisibility(View.GONE);
                rlMapToPallet.setVisibility(View.VISIBLE);
                rlNonRSNSkuList.setVisibility(View.GONE);
                break;

            case R.id.btnMapItemstoPallet:

                List<ItemInfoDTO> dtoList = itemInfoDTOList;
                finalizedList = new ArrayList<>();
                int child = 0;
                child = listViewItems.getAdapter().getCount();

                for (int i = 0; i < child; i++) {

                    if (itemInfoDTOList.get(i).getPickedQuantity() != null) {
                        finalizedList.add(itemInfoDTOList.get(i));
                    }

                }

                if (finalizedList.size() > 0)
                    ConfirmNonRSNBinToBinInternalTransfer();
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

        if (rlNonRSNSkuList.getVisibility() == View.VISIBLE) {

            if (scannedData != null && !common.isPopupActive()) {

                if (!ProgressDialogUtils.isProgressActive()) {

                    if (ScanValidator.IsLocationScanned(scannedData)) {

                        etLocation.setText(scannedData);
                        location = scannedData;
                        GetNONRSNBinLocationSKUs(etLocation.getText().toString());

                    } else if (ScanValidator.IsPalletScanned(scannedData)) {
                        if (!etLocation.getText().toString().isEmpty()) {
                            IsPalletScanned = true;
                            etPallet.setText(scannedData);

                        } else {
                            common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getActivity(), "Error");
                        }
                    }

                } else {
                    if (!common.isPopupActive()) {
                        common.showUserDefinedAlertType(errorMessages.EMC_081, getActivity(), getContext(), "Error");
                    }

                }
            }

        }

    }

    public void GetNONRSNBinLocationSKUs(String location) {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.InventoryDTO, getContext());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setUserId(userId);
            inventoryDTO.setFromLocation(location);
            message.setEntityObject(inventoryDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetNONRSNBinLocationSKUs(message);
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
                                    etLocation.setText("");
                                    ProgressDialogUtils.closeProgressDialog();
                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanLocation.setImageResource(R.drawable.warning_img);
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                } else {
                                    core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                    List<LinkedTreeMap<?, ?>> _lResponse = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lResponse = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanLocation.setImageResource(R.drawable.check);

                                    ItemInfoDTO dto = null;
                                    final List<ItemInfoDTO> itemDto = new ArrayList<>();
                                    for (int i = 0; i < _lResponse.size(); i++) {
                                        dto = new ItemInfoDTO(_lResponse.get(i).entrySet());
                                        itemDto.add(dto);
                                    }


                                    rvItemsList.setAdapter(null);


                                    ItemListNinToBinNonRSn adp = new ItemListNinToBinNonRSn(getActivity(), itemDto, new ItemListNinToBinNonRSn.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(int pos) {
                                            mmId = Integer.parseInt(itemDto.get(pos).getMaterialMasterId());
                                            if (IsPalletScanned)
                                                GetNONRSNBinLocationSKUDetails();
                                            else
                                                common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getActivity(), "Error");

                                        }
                                    });
                                    rvItemsList.setAdapter(adp);

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


    private void GetNONRSNBinLocationSKUDetails() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.InventoryDTO, getContext());

            InventoryDTO iDto = new InventoryDTO();
            iDto.setMaterialMasterID(mmId);
            iDto.setFromLocation(location);

            message.setEntityObject(iDto);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);
            try {
                call = apiService.GetNONRSNBinLocationSKUDetails(message);
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

                                itemInfoDTOList = new ArrayList<>();
                                itemInfoDTOList.clear();
                                ItemInfoDTO dto = null;
                                final List<ItemInfoDTO> itemDto = new ArrayList<>();
                                for (int i = 0; i < _lResponse.size(); i++) {
                                    dto = new ItemInfoDTO(_lResponse.get(i).entrySet());
                                    itemDto.add(dto);

                                }
                                itemInfoDTOList = itemDto;
                                rlSelection.setVisibility(View.GONE);
                                rlNonRSNSkuList.setVisibility(View.GONE);
                                rlMapToPallet.setVisibility(View.VISIBLE);

                                listViewItems.setExpanded(true);
                                listViewItems.setAdapter(null);
                                ListAdapter adapter = new ListAdapter(getActivity(), itemDto);
                                listViewItems.setAdapter(adapter);


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

    private void ConfirmNonRSNBinToBinInternalTransfer() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDRequestDTO, getContext());

            VLPDRequestDTO vlpdRequestDTO = new VLPDRequestDTO();
            vlpdRequestDTO.setIsRSN("0");
            vlpdRequestDTO.setScannedInput(etPallet.getText().toString());
            vlpdRequestDTO.setPickerRequestedInfo(finalizedList);


            message.setEntityObject(vlpdRequestDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);
            try {
                call = apiService.ConfirmNonRSNBinToBinInternalTransfer(message);
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

                                List<LinkedTreeMap<?, ?>> _lInternalTransfer = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInternalTransfer = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                InternalTransferDTO dto = null;
                                for (int i = 0; i < _lInternalTransfer.size(); i++) {
                                    dto = new InternalTransferDTO(_lInternalTransfer.get(i).entrySet());
                                }


                                if (dto.getStatus()) {

                                    GetNONRSNBinLocationSKUDetails();

                                }

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

class ListAdapter extends BaseAdapter {

    // HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

    Context context;
    List<ItemInfoDTO> objects;

    public ListAdapter(Activity context, List<ItemInfoDTO> objects) {
        this.context = context;
        this.objects = objects;

    }


    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {

        return objects.indexOf(getItem(position));
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    int pos;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_list_nonrsn_bintobin, parent, false);
            holder = new ViewHolder();
            holder.txtMCode = (TextView) convertView.findViewById(R.id.txtMCode);
            holder.txtDesc = (TextView) convertView.findViewById(R.id.txtDesc);
            holder.txtHu = (TextView) convertView.findViewById(R.id.txtHu);
            holder.txtQty = (TextView) convertView.findViewById(R.id.txtQty);
            holder.txtBatch = (TextView) convertView.findViewById(R.id.txtBatch);
            holder.txtSiteCode = (TextView) convertView.findViewById(R.id.txtSiteCode);
            holder.txtPallet = (TextView) convertView.findViewById(R.id.txtPallet);
            holder.etQty = (EditText) convertView.findViewById(R.id.etQty);
            holder.cbSelect = (CheckBox) convertView.findViewById(R.id.cbSelect);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtMCode.setText(objects.get(position).getMcode());
        holder.txtDesc.setText(objects.get(position).getDescription());
        holder.txtQty.setText(objects.get(position).getAvlQuantity());
        holder.txtBatch.setText(objects.get(position).getBatchNumber());
        holder.txtSiteCode.setText("Site: " + objects.get(position).getSiteCode());
        holder.txtPallet.setText("Pallet: " + objects.get(position).getPalletNumber());
        holder.txtHu.setText("Hu: " + objects.get(position).getHuNo() + "/" + objects.get(position).getHuSize());

        holder.cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CheckBox cb = (CheckBox) buttonView;

                if (isChecked) {

                    objects.get(position).setResult("1");


                } else {
                    objects.get(position).setResult("0");

                }

            }
        });

        holder.etQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    pos = position;
            }
        });

        holder.etQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (pos == position)
                    objects.get(pos).setPickedQuantity(s.toString());
                //objects.get(pos).setResult("1");
            }
        });

        return convertView;
    }


    public class ViewHolder {
        TextView txtMCode, txtDesc, txtHu, txtQty, txtBatch, txtSiteCode, txtPallet;
        EditText etQty;
        CheckBox cbSelect;

    }
}
