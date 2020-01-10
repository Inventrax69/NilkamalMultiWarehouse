package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class InternalTransferDTO {

    @SerializedName("ScannedQty")
    private String scannedQty;
    @SerializedName("SetQuantity")
    private String setQuantity;
    @SerializedName("Message")
    private String message;
    @SerializedName("Status")
    private Boolean status;
    @SerializedName("Barcode")
    private String barcode;
    @SerializedName("BarcodeType")
    private String barcodeType;
    @SerializedName("PrinterIP")
    private String printerIP;
    public InternalTransferDTO() {

    }

    public InternalTransferDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "ScannedQty":
                    if (entry.getValue() != null) {
                        this.setScannedQty(entry.getValue().toString());
                    }
                    break;
                case "SetQuantity":
                    if (entry.getValue() != null) {
                        this.setSetQuantity(entry.getValue().toString());
                    }
                    break;
                case "Message":
                    if (entry.getValue() != null) {
                        this.setMessage(entry.getValue().toString());
                    }
                    break;
                case "Status":
                    if (entry.getValue() != null) {
                        this.setStatus(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "Barcode":
                    if (entry.getValue() != null) {
                        this.setBarcode(entry.getValue().toString());
                    }
                    break;
                case "BarcodeType":
                    if (entry.getValue() != null) {
                        this.setBarcodeType(entry.getValue().toString());
                    }
                    break;
                case "PrinterIP":
                    if (entry.getValue() != null) {
                        this.setPrinterIP(entry.getValue().toString());
                    }
                    break;

            }
        }
    }

    public String getPrinterIP() {
        return printerIP;
    }

    public void setPrinterIP(String printerIP) {
        this.printerIP = printerIP;
    }

    public String getScannedQty() {
        return scannedQty;
    }

    public void setScannedQty(String scannedQty) {
        this.scannedQty = scannedQty;
    }

    public String getSetQuantity() {
        return setQuantity;
    }

    public void setSetQuantity(String setQuantity) {
        this.setQuantity = setQuantity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
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
}
