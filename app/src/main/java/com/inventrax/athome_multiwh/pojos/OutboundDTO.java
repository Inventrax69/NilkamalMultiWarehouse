package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class OutboundDTO {

    @SerializedName("OBDNumber")
    private String OBDNumber;
    @SerializedName("SKU")
    private String SKU;
    @SerializedName("MDescription")
    private String MDescription;
    @SerializedName("RequiredQty")
    private String requiredQty;
    @SerializedName("PendigQty")
    private String pendigQty;
    @SerializedName("EAN")
    private String EAN;
    @SerializedName("MRP")
    private String MRP;
    @SerializedName("UserId")
    private String userId;
    @SerializedName("IsNew")
    private String isNew ;
    @SerializedName("ReturnValue")
    private String returnValue;
    @SerializedName("OutboundNumber")
    private String outboundNumber;
    @SerializedName("EANNumber")
    private String EANNumber;
    @SerializedName("Quntity")
    private Double quantity;
    @SerializedName("BoxNO")
    private String BoxNO;
    @SerializedName("Description")
    private String description;
    @SerializedName("TotalScannedQty")
    private String totalScannedQty ;
    @SerializedName("SKUReqQty")
    private String SKUReqQty;
    @SerializedName("SKUPendingQty")
    private String SKUPendingQty;
    @SerializedName("RequestedBy")
    private Integer requestedBy;
    @SerializedName("ScanQuantity")
    private String scanQuantity;
    @SerializedName("SKUInfo")
    private String SKUInfo;
    @SerializedName("Barcode")
    private String barcode;
    @SerializedName("BarcodeType")
    private String barcodeType ;
    @SerializedName("BatchNo")
    private String batchNo;
    @SerializedName("Location")
    private String location;
    @SerializedName("AvailQuantity")
    private String availQuantity;
    @SerializedName("HUNumber")
    private String HUNumber;
    @SerializedName("HUsize")
    private String HUsize;
    @SerializedName("Sloc")
    private String sLoc;
    @SerializedName("IpAddress")
    private String ipAddress;
    @SerializedName("Result")
    private String result;
    @SerializedName("VlpdNumber")
    private String VlpdNumber;

    public  OutboundDTO()
    { }


    public OutboundDTO(Set<? extends Map.Entry<?, ?>> entries)
    {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "OBDNumber":
                    if(entry.getValue()!=null) {
                        this.setOBDNumber(entry.getValue().toString());
                    }
                    break;
                case "SKU":
                    if(entry.getValue()!=null) {
                        this.setSKU(entry.getValue().toString());
                    }
                    break;
                case "MDescription":
                    if(entry.getValue()!=null) {
                        this.setMDescription(entry.getValue().toString());
                    }
                    break;
                case "RequiredQty":
                    if(entry.getValue()!=null) {
                        this.setRequiredQty(entry.getValue().toString());
                    }
                    break;
                case "PendigQty":
                    if(entry.getValue()!=null) {
                        this.setSKUPendingQty(entry.getValue().toString());
                    }
                    break;
                case "EAN":
                    if(entry.getValue()!=null) {
                        this.setEAN(entry.getValue().toString());
                    }
                    break;
                case "MRP":
                    if(entry.getValue()!=null) {
                        this.setMRP(entry.getValue().toString());
                    }
                    break;
                case "UserId":
                    if(entry.getValue()!=null) {
                        this.setUserId(entry.getValue().toString());
                    }
                    break;

                case   "IsNew":
                    if(entry.getValue()!=null) {
                        this.setIsNew(entry.getValue().toString());
                    }
                    break;
                case   "ReturnValue":
                    if(entry.getValue()!=null) {
                        this.setReturnValue(entry.getValue().toString());
                    }
                    break;
                case   "OutboundNumber":
                    if(entry.getValue()!=null) {
                        this.setOutboundNumber(entry.getValue().toString());
                    }
                    break;
                case   "EANNumber":
                    if(entry.getValue()!=null) {
                        this.setEANNumber(entry.getValue().toString());
                    }
                    break;
                case   "Quntity":
                    if(entry.getValue()!=null) {
                        this.setQuantity(Double.parseDouble(entry.getValue().toString()));
                    }
                    break;
                case   "BoxNO":
                    if(entry.getValue()!=null) {
                        this.setBoxNO(entry.getValue().toString());
                    }
                    break;
                case   "Description":
                    if(entry.getValue()!=null) {
                        this.setDescription(entry.getValue().toString());
                    }
                    break;

                case "TotalScannedQty":
                    if(entry.getValue()!=null) {
                        this.setTotalScannedQty(entry.getValue().toString());
                    }
                    break;

                case "SKUReqQty":
                    if(entry.getValue()!=null) {
                        this.setSKUReqQty(entry.getValue().toString());
                    }

                    break;
                case   "SKUPendingQty":
                    if(entry.getValue()!=null) {
                        this.setSKUPendingQty(entry.getValue().toString());
                    }
                    break;
                case   "RequestedBy":
                    if(entry.getValue()!=null) {
                        this.setRequestedBy(Integer.parseInt(entry.getValue().toString()));
                    }
                    break;
                case   "ScanQuantity":
                    if(entry.getValue()!=null) {
                        this.setScanQuantity(entry.getValue().toString());
                    }
                    break;
                case   "SKUInfo":
                    if(entry.getValue()!=null) {
                        this.setSKUInfo(entry.getValue().toString());
                    }
                    break;
                case   "Barcode":
                    if(entry.getValue()!=null) {
                        this.setBarcode(entry.getValue().toString());
                    }
                    break;
                case   "BarcodeType":
                    if(entry.getValue()!=null) {
                        this.setBarcodeType(entry.getValue().toString());
                    }
                case   "BatchNo":
                    if(entry.getValue()!=null) {
                        this.setBatchNo(entry.getValue().toString());
                    }
                    break;
                case   "Location":
                    if(entry.getValue()!=null) {
                        this.setLocation(entry.getValue().toString());
                    }
                    break;
                case   "AvailQuantity":
                    if(entry.getValue()!=null) {
                        this.setAvailQuantity(entry.getValue().toString());
                    }
                case   "HUNumber":
                    if(entry.getValue()!=null) {
                        this.setHUNumber(entry.getValue().toString());
                    }
                    break;

                case   "HUsize":
                    if(entry.getValue()!=null) {
                        this.setHUsize((entry.getValue().toString()));
                    }
                    break;

                case   "Sloc":
                    if(entry.getValue()!=null) {
                        this.setsLoc(entry.getValue().toString());
                    }
                    break;

                case   "Result":
                    if(entry.getValue()!=null) {
                        this.setResult(entry.getValue().toString());
                    }
                    break;
                case   "IpAddress":
                    if(entry.getValue()!=null) {
                        this.setIpAddress(entry.getValue().toString());
                    }
                    break;
                case   "VlpdNumber":
                    if(entry.getValue()!=null) {
                        this.setVlpdNumber(entry.getValue().toString());
                    }
                    break;

            }
        }
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getOBDNumber() {
        return OBDNumber;
    }

    public void setOBDNumber(String OBDNumber) {
        this.OBDNumber = OBDNumber;
    }

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public String getMDescription() {
        return MDescription;
    }

    public void setMDescription(String MDescription) {
        this.MDescription = MDescription;
    }

    public String getRequiredQty() {
        return requiredQty;
    }

    public void setRequiredQty(String requiredQty) {
        this.requiredQty = requiredQty;
    }

    public String getPendigQty() {
        return pendigQty;
    }

    public void setPendigQty(String pendigQty) {
        this.pendigQty = pendigQty;
    }

    public String getEAN() {
        return EAN;
    }

    public void setEAN(String EAN) {
        this.EAN = EAN;
    }

    public String getMRP() {
        return MRP;
    }

    public void setMRP(String MRP) {
        this.MRP = MRP;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIsNew() {
        return isNew;
    }

    public void setIsNew(String isNew) {
        this.isNew = isNew;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    public String getOutboundNumber() {
        return outboundNumber;
    }

    public void setOutboundNumber(String outboundNumber) {
        this.outboundNumber = outboundNumber;
    }

    public String getEANNumber() {
        return EANNumber;
    }

    public void setEANNumber(String EANNumber) {
        this.EANNumber = EANNumber;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getBoxNO() {
        return BoxNO;
    }

    public void setBoxNO(String boxNO) {
        BoxNO = boxNO;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTotalScannedQty() {
        return totalScannedQty;
    }

    public void setTotalScannedQty(String totalScannedQty) {
        this.totalScannedQty = totalScannedQty;
    }

    public String getSKUReqQty() {
        return SKUReqQty;
    }

    public void setSKUReqQty(String SKUReqQty) {
        this.SKUReqQty = SKUReqQty;
    }

    public String getSKUPendingQty() {
        return SKUPendingQty;
    }

    public void setSKUPendingQty(String SKUPendingQty) {
        this.SKUPendingQty = SKUPendingQty;
    }

    public Integer getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Integer requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getScanQuantity() {
        return scanQuantity;
    }

    public void setScanQuantity(String scanQuantity) {
        this.scanQuantity = scanQuantity;
    }

    public String getSKUInfo() {
        return SKUInfo;
    }

    public void setSKUInfo(String SKUInfo) {
        this.SKUInfo = SKUInfo;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBarcodeType() {
        return barcodeType;
    }

    public void setBarcodeType(String barcodeType) {
        this.barcodeType = barcodeType;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAvailQuantity() {
        return availQuantity;
    }

    public void setAvailQuantity(String availQuantity) {
        this.availQuantity = availQuantity;
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

    public String getsLoc() {
        return sLoc;
    }

    public void setsLoc(String sLoc) {
        this.sLoc = sLoc;
    }


    public String getVlpdNumber() {
        return VlpdNumber;
    }

    public void setVlpdNumber(String vlpdNumber) {
        VlpdNumber = vlpdNumber;
    }
}