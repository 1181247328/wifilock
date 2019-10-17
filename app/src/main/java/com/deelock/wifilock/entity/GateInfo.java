package com.deelock.wifilock.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class GateInfo {


    /**
     * gtwObj : {"devId":"0001000084F3EB5DDA09","devType":"000","signalStrength":"64","isOnline":1,"devName":"Wifi智能网关","ssid":"11","mac":"84F3EB5DDA09","timeBind":1536557640,"isProsHelp":0,"version2":"25","version3":"10","version1":"F3","timeRegister":1536055183,"power":"64"}
     * uid : 180760
     */

    private GtwObjBean gtwObj;
    private String uid;

    public GtwObjBean getGtwObj() {
        return gtwObj;
    }

    public void setGtwObj(GtwObjBean gtwObj) {
        this.gtwObj = gtwObj;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public static class GtwObjBean implements Parcelable {
        /**
         * devId : 0001000084F3EB5DDA09
         * devType : 000
         * signalStrength : 64
         * isOnline : 1
         * devName : Wifi智能网关
         * ssid : 11
         * mac : 84F3EB5DDA09
         * timeBind : 1536557640
         * isProsHelp : 0
         * version2 : 25
         * version3 : 10
         * version1 : F3
         * timeRegister : 1536055183
         * power : 64
         */

        private String devId;
        private String devType;
        private String signalStrength;
        private int isOnline;
        private String devName;
        private String ssid;
        private String mac;
        private int timeBind;
        private int isProsHelp;
        private String version2;
        private String version3;
        private String version1;
        private int timeRegister;
        private String power;

        public String getDevId() {
            return devId;
        }

        public void setDevId(String devId) {
            this.devId = devId;
        }

        public String getDevType() {
            return devType;
        }

        public void setDevType(String devType) {
            this.devType = devType;
        }

        public String getSignalStrength() {
            return signalStrength;
        }

        public void setSignalStrength(String signalStrength) {
            this.signalStrength = signalStrength;
        }

        public int getIsOnline() {
            return isOnline;
        }

        public void setIsOnline(int isOnline) {
            this.isOnline = isOnline;
        }

        public String getDevName() {
            return devName;
        }

        public void setDevName(String devName) {
            this.devName = devName;
        }

        public String getSsid() {
            return ssid;
        }

        public void setSsid(String ssid) {
            this.ssid = ssid;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public int getTimeBind() {
            return timeBind;
        }

        public void setTimeBind(int timeBind) {
            this.timeBind = timeBind;
        }

        public int getIsProsHelp() {
            return isProsHelp;
        }

        public void setIsProsHelp(int isProsHelp) {
            this.isProsHelp = isProsHelp;
        }

        public String getVersion2() {
            return version2;
        }

        public void setVersion2(String version2) {
            this.version2 = version2;
        }

        public String getVersion3() {
            return version3;
        }

        public void setVersion3(String version3) {
            this.version3 = version3;
        }

        public String getVersion1() {
            return version1;
        }

        public void setVersion1(String version1) {
            this.version1 = version1;
        }

        public int getTimeRegister() {
            return timeRegister;
        }

        public void setTimeRegister(int timeRegister) {
            this.timeRegister = timeRegister;
        }

        public String getPower() {
            return power;
        }

        public void setPower(String power) {
            this.power = power;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.devId);
            dest.writeString(this.devType);
            dest.writeString(this.signalStrength);
            dest.writeInt(this.isOnline);
            dest.writeString(this.devName);
            dest.writeString(this.ssid);
            dest.writeString(this.mac);
            dest.writeInt(this.timeBind);
            dest.writeInt(this.isProsHelp);
            dest.writeString(this.version2);
            dest.writeString(this.version3);
            dest.writeString(this.version1);
            dest.writeInt(this.timeRegister);
            dest.writeString(this.power);
        }

        public GtwObjBean() {
        }

        protected GtwObjBean(Parcel in) {
            this.devId = in.readString();
            this.devType = in.readString();
            this.signalStrength = in.readString();
            this.isOnline = in.readInt();
            this.devName = in.readString();
            this.ssid = in.readString();
            this.mac = in.readString();
            this.timeBind = in.readInt();
            this.isProsHelp = in.readInt();
            this.version2 = in.readString();
            this.version3 = in.readString();
            this.version1 = in.readString();
            this.timeRegister = in.readInt();
            this.power = in.readString();
        }

        public static final Parcelable.Creator<GtwObjBean> CREATOR = new Parcelable.Creator<GtwObjBean>() {
            @Override
            public GtwObjBean createFromParcel(Parcel source) {
                return new GtwObjBean(source);
            }

            @Override
            public GtwObjBean[] newArray(int size) {
                return new GtwObjBean[size];
            }
        };
    }
}
