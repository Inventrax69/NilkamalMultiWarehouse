package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;

public class UnloadImage {

    @SerializedName("FileName")
    private String fileName;
    @SerializedName("Type")
    private String type;
    @SerializedName("ImageData")
    private String imageData;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }
}
