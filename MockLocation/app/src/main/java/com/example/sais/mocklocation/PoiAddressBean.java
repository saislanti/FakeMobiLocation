package com.example.sais.mocklocation;

import java.io.Serializable;

/**
 * Created by ywq on 2017-11-03.
 */
public class PoiAddressBean implements Serializable{

    private String longitude;
    private String latitude;
    private String text;
    public String detailAddress; //详细地址（搜索关键字）
    public String province;
    public String city;
    public String district;

    public PoiAddressBean(String longitude, String latitude, String text, String detailAddress, String province, String city, String district) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.text = text;
        this.detailAddress = detailAddress;
        this.province = province;
        this.city = city;
        this.district = district;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getText() {
        return text;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }
}
