package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by karthik.m on 06/27/2018.
 */

public class CycleCountDTO implements Serializable{

    @SerializedName("UserId")
    private String userId;

    @SerializedName("Location")
    private String location;

    @SerializedName("BoxQty")
    private String boxQty;

    @SerializedName("IsSatisfiedBoxQty")
    private Boolean isSatisfiedBoxQty;

    @SerializedName("SerialNo")
    private String serialNo;

    @SerializedName("MRP")
    private String MRP;

    @SerializedName("MOP")
    private String MOP;

    @SerializedName("MaterialCode")
    private String materialCode;

    @SerializedName("BatchNo")
    private String batchNo;

    @SerializedName("SelectedCCName")
    private String SelectedCCName;

    @SerializedName("SLOC")
    private String SLOC;

    @SerializedName("SelectedColorCode")
    private String SelectedColorCode;

    @SerializedName("Result")
    private String Result;

    @SerializedName("UserConfirmReDo")
    private Boolean UserConfirmReDo;

    @SerializedName("IsEANScanned")
    private Boolean IsEANScanned;

    @SerializedName("IsEANSpecified")
    private Boolean IsEANSpecified;

    @SerializedName("MaterialType")
    private String MaterialType;

    @SerializedName("MDesc")
    private String MDesc;

    @SerializedName("Barcode")
    private String barcode;

    @SerializedName("WMSQty")
    private String WMSQty;

    @SerializedName("CCQty")
    private String CCQty;

    @SerializedName("MaterialMasterId")
    private String materialMasterId;

    @SerializedName("LogicalBincount")
    private String logicalBincount;

    @SerializedName("PhysicalBinCount")
    private String physicalBinCount;

    @SerializedName("HUNumber")
    private String HUNumber;

    @SerializedName("HUsize")
    private String HUsize;

    public CycleCountDTO()
    {

    }

    public Boolean getUserConfirmReDo() {
        return UserConfirmReDo;
    }

    public void setUserConfirmReDo(Boolean userConfirmReDo) {
        UserConfirmReDo = userConfirmReDo;
    }

    public CycleCountDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {
                case  "UserId" :
                    if(entry.getValue()!=null) {
                        this.setUserId(entry.getValue().toString());
                    }
                    break;

                case  "Location" :
                    if(entry.getValue()!=null) {
                        this.setLocation(entry.getValue().toString());
                    }
                    break;
                case  "BoxQty" :
                    if(entry.getValue()!=null) {
                        this.setBoxQty(entry.getValue().toString());
                    }
                    break;
                case  "IsSatisfiedBoxQty" :
                    if(entry.getValue()!=null) {
                        this.setSatisfiedBoxQty(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case  "SerialNo" :
                    if(entry.getValue()!=null) {
                        this.setSerialNo(entry.getValue().toString());
                    }
                    break;
                case  "MRP" :
                    if(entry.getValue()!=null) {
                        this.setMRP(entry.getValue().toString());
                    }
                    break;
                case  "MOP" :
                    if(entry.getValue()!=null) {
                        this.setMOP(entry.getValue().toString());
                    }
                    break;
                case  "MaterialCode" :
                    if(entry.getValue()!=null) {
                        this.setMaterialCode(entry.getValue().toString());
                    }
                    break;
                case  "BatchNo" :
                    if(entry.getValue()!=null) {
                        this.setBatchNo(entry.getValue().toString());
                    }
                    break;

                case  "SelectedCCName" :
                    if(entry.getValue()!=null) {
                        this.setSelectedCCName(entry.getValue().toString());
                    }
                    break;
                case  "SLOC" :
                    if(entry.getValue()!=null) {
                        this.setSLOC(entry.getValue().toString());
                    }
                    break;
                case  "SelectedColorCode" :
                    if(entry.getValue()!=null) {
                        this.setSelectedColorCode(entry.getValue().toString());
                    }
                    break;
                case  "Result" :
                    if(entry.getValue()!=null) {
                        this.setResult(entry.getValue().toString());

                    }
                    break;


                case "IsEANScanned":
                    if(entry.getValue()!=null)
                    {
                        this.setEANScanned(Boolean.parseBoolean(entry.getValue().toString()));

                    }
                    break;
                case "UserConfirmReDo":
                    if(entry.getValue()!=null)
                    {
                        this.setUserConfirmReDo(Boolean.parseBoolean(entry.getValue().toString()));

                    }
                    break;
                case "IsEANSpecified":
                    if(entry.getValue()!=null)
                    {
                        this.setEANSpecified(Boolean.parseBoolean(entry.getValue().toString()));


                    }
                    break;
                case "MaterialType":
                    if(entry.getValue()!=null)
                    {
                        this.setMaterialType(entry.getValue().toString());

                    }
                    break;
                case "MDesc":
                    if(entry.getValue()!=null)
                    {
                        this.setMDesc(entry.getValue().toString());

                    }
                    break;

                case "Barcode":
                    if(entry.getValue()!=null)
                    {
                        this.setBarcode(entry.getValue().toString());

                    }
                    break;


                case "WMSQty":
                    if(entry.getValue()!=null)
                    {
                        this.setWMSQty(entry.getValue().toString());

                    }
                    break;

                case "CCQty":
                    if(entry.getValue()!=null)
                    {
                        this.setCCQty(entry.getValue().toString());

                    }
                    break;

                case "MaterialMasterId":
                    if(entry.getValue()!=null)
                    {
                        this.setMaterialMasterId(entry.getValue().toString());

                    }
                    break;


                case "LogicalBincount":
                    if(entry.getValue()!=null)
                    {
                        this.setLogicalBincount(entry.getValue().toString());

                    }
                    break;

                case "PhysicalBinCount":
                    if(entry.getValue()!=null)
                    {
                        this.setPhysicalBinCount(entry.getValue().toString());

                    }
                    break;

                case "HUNumber":
                    if(entry.getValue()!=null)
                    {
                        this.setHUNumber(entry.getValue().toString());

                    }
                    break;

                case "HUsize":
                    if(entry.getValue()!=null)
                    {
                        this.setHUsize(entry.getValue().toString());

                    }
                    break;


            }
        }
    }

    public String getSLOC() {
        return SLOC;
    }

    public void setSLOC(String SLOC) {
        this.SLOC = SLOC;
    }

    public String getLogicalBincount() {
        return logicalBincount;
    }

    public void setLogicalBincount(String logicalBincount) {
        this.logicalBincount = logicalBincount;
    }

    public String getPhysicalBinCount() {
        return physicalBinCount;
    }

    public void setPhysicalBinCount(String physicalBinCount) {
        this.physicalBinCount = physicalBinCount;
    }

    public String getMDesc() {
        return MDesc;
    }

    public void setMDesc(String MDesc) {
        this.MDesc = MDesc;
    }

    public String getMaterialType() {
        return MaterialType;
    }

    public void setMaterialType(String materialType) {
        MaterialType = materialType;
    }

    public Boolean getEANScanned() {
        return IsEANScanned;
    }

    public void setEANScanned(Boolean EANScanned) {
        IsEANScanned = EANScanned;
    }

    public Boolean getEANSpecified() {
        return IsEANSpecified;
    }

    public void setEANSpecified(Boolean EANSpecified) {
        IsEANSpecified = EANSpecified;
    }



    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }


    public String getSelectedColorCode() {
        return SelectedColorCode;

    }

    public void setSelectedColorCode(String selectedColorCode) {
        SelectedColorCode = selectedColorCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }



    public Boolean getSatisfiedBoxQty() {
        return isSatisfiedBoxQty;
    }

    public void setSatisfiedBoxQty(Boolean satisfiedBoxQty) {
        isSatisfiedBoxQty = satisfiedBoxQty;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getMRP() {
        return MRP;
    }

    public void setMRP(String MRP) {
        this.MRP = MRP;
    }

    public String getMOP() {
        return MOP;
    }

    public void setMOP(String MOP) {
        this.MOP = MOP;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getSelectedCCName() {
        return SelectedCCName;
    }

    public void setSelectedCCName(String selectedCCName) {
        SelectedCCName = selectedCCName;
    }




    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getWMSQty() {
        return WMSQty;
    }

    public void setWMSQty(String WMSQty) {
        this.WMSQty = WMSQty;
    }

    public String getCCQty() {
        return CCQty;
    }

    public void setCCQty(String CCQty) {
        this.CCQty = CCQty;
    }

    public String getMaterialMasterId() {
        return materialMasterId;
    }

    public void setMaterialMasterId(String materialMasterId) {
        this.materialMasterId = materialMasterId;
    }

    public String getHUNumber() {
        return HUNumber;
    }

    public void setHUNumber(String HUNumber) {
        this.HUNumber = HUNumber;
    }

    public String getHUsize() {
        return HUsize;
    }

    public void setHUsize(String HUsize) {
        this.HUsize = HUsize;
    }

    public String getBoxQty() {
        return boxQty;
    }

    public void setBoxQty(String boxQty) {
        this.boxQty = boxQty;
    }
}