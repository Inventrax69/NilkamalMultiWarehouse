package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VLPDRequestDTO {

    @SerializedName("VlpdID")
    private String vlpdID;
    @SerializedName("userID")
    private String userID;
    @SerializedName("pickerRequestedInfo")
    private List<ItemInfoDTO> pickerRequestedInfo ;
    @SerializedName("Type")
    private String type;
    @SerializedName("ScannedInput")
    private String ScannedInput;
    @SerializedName("UniqueRSN")
    private String uniqueRSN;
    @SerializedName("IsNew")
    private String IsNew;
    @SerializedName("IsRSN")
    private String IsRSN;

   /* @SerializedName("ReqQuantity")
    private int reqQuantity;*/
    @SerializedName("InputType")
    private List<ItemInfoDTO> inputType ;

    public VLPDRequestDTO(){

    }

    public VLPDRequestDTO(Set<? extends Map.Entry<?, ?>> entries)
    {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "VlpdID":
                    if(entry.getValue()!=null) {
                        this.setVlpdID(entry.getValue().toString());
                    }
                    break;
                case "userID":
                    if(entry.getValue()!=null) {
                        this.setUserID(entry.getValue().toString());
                    }
                    break;
                case "pickerRequestedInfo":
                    if(entry.getValue()!=null) {
                        List<LinkedTreeMap<?,?>> pickerRequestedInfo=(List<LinkedTreeMap<?,?>>)entry.getValue();
                        List<ItemInfoDTO> lstPickerInfo=new ArrayList<ItemInfoDTO>();

                        for(int i=0;i<pickerRequestedInfo.size();i++)
                        {
                            ItemInfoDTO dto=new ItemInfoDTO(pickerRequestedInfo.get(i).entrySet());
                            lstPickerInfo.add(dto);

                        }
                        this.setPickerRequestedInfo(lstPickerInfo);
                    }
                    break;
                case "Type":
                    if(entry.getValue()!=null) {
                        this.setType(entry.getValue().toString());
                    }
                    break;
                case "ScannedInput":
                    if(entry.getValue()!=null) {
                        this.setScannedInput(entry.getValue().toString());
                    }
                    break;
                case "UniqueRSN":
                    if(entry.getValue()!=null) {
                        this.setUniqueRSN(entry.getValue().toString());
                    }
                    break;
                case "IsNew":
                    if(entry.getValue()!=null) {
                        this.setIsNew(entry.getValue().toString());
                    }
                    break;
                case "InputType":
                    if(entry.getValue()!=null) {
                        List<LinkedTreeMap<?,?>> inputTypeList=(List<LinkedTreeMap<?,?>>)entry.getValue();
                        List<ItemInfoDTO> listinputType=new ArrayList<ItemInfoDTO>();
                        for(int i=0;i<inputTypeList.size();i++)
                        {
                            ItemInfoDTO dto=new ItemInfoDTO(inputTypeList.get(i).entrySet());
                            listinputType.add(dto);

                        }
                        this.setInputType(listinputType);
                    }
                    break;
             /*   case "ReqQuantity":
                    if(entry.getValue()!=null) {
                        this.setReqQuantity(Integer.parseInt(entry.getValue().toString()));
                    }
                    break;*/

            }
        }
    }

/*
    public int getReqQuantity() {
        return reqQuantity;
    }

    public void setReqQuantity(int reqQuantity) {
        this.reqQuantity = reqQuantity;
    }*/

    public String getVlpdID() {
        return vlpdID;
    }

    public void setVlpdID(String vlpdID) {
        this.vlpdID = vlpdID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public List<ItemInfoDTO> getPickerRequestedInfo() {
        return pickerRequestedInfo;
    }

    public void setPickerRequestedInfo(List<ItemInfoDTO> pickerRequestedInfo) {
        this.pickerRequestedInfo = pickerRequestedInfo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScannedInput() {
        return ScannedInput;
    }

    public void setScannedInput(String scannedInput) {
        ScannedInput = scannedInput;
    }

    public String getUniqueRSN() {
        return uniqueRSN;
    }

    public void setUniqueRSN(String uniqueRSN) {
        this.uniqueRSN = uniqueRSN;
    }

    public String getIsNew() {
        return IsNew;
    }

    public void setIsNew(String isNew) {
        IsNew = isNew;
    }

    public List<ItemInfoDTO> getInputType() {
        return inputType;
    }

    public void setInputType(List<ItemInfoDTO> inputType) {
        this.inputType = inputType;
    }

    public String getIsRSN() {
        return IsRSN;
    }

    public void setIsRSN(String isRSN) {
        IsRSN = isRSN;
    }
}
