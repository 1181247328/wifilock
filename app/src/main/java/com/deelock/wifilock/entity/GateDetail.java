package com.deelock.wifilock.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class GateDetail {

    /**
     * subList : [{"devId":"C000C43B00158D000131","isOnline":1,"power":"64","devName":"智能门磁","timeBind":1536308099}]
     * gtwId : 0001000084F3EB5DDA09
     */

    private String gtwId;
    private List<SubListBean> subList;

    public String getGtwId() {
        return gtwId;
    }

    public void setGtwId(String gtwId) {
        this.gtwId = gtwId;
    }

    public List<SubListBean> getSubList() {
        return subList;
    }

    public void setSubList(List<SubListBean> subList) {
        this.subList = subList;
    }

    public static class SubListBean implements Parcelable {
        /**
         * devId : C000C43B00158D000131
         * isOnline : 1
         * power : 64
         * devName : 智能门磁
         * timeBind : 1536308099
         */

        private String devId;
        private int isOnline;
        private String power;
        private String devName;
        private int timeBind;

        public String getDevId() {
            return devId;
        }

        public void setDevId(String devId) {
            this.devId = devId;
        }

        public int getIsOnline() {
            return isOnline;
        }

        public void setIsOnline(int isOnline) {
            this.isOnline = isOnline;
        }

        public String getPower() {
            return power;
        }

        public void setPower(String power) {
            this.power = power;
        }

        public String getDevName() {
            return devName;
        }

        public void setDevName(String devName) {
            this.devName = devName;
        }

        public int getTimeBind() {
            return timeBind;
        }

        public void setTimeBind(int timeBind) {
            this.timeBind = timeBind;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.devId);
            dest.writeInt(this.isOnline);
            dest.writeString(this.power);
            dest.writeString(this.devName);
            dest.writeInt(this.timeBind);
        }

        public SubListBean() {
        }

        protected SubListBean(Parcel in) {
            this.devId = in.readString();
            this.isOnline = in.readInt();
            this.power = in.readString();
            this.devName = in.readString();
            this.timeBind = in.readInt();
        }

        public static final Parcelable.Creator<SubListBean> CREATOR = new Parcelable.Creator<SubListBean>() {
            @Override
            public SubListBean createFromParcel(Parcel source) {
                return new SubListBean(source);
            }

            @Override
            public SubListBean[] newArray(int size) {
                return new SubListBean[size];
            }
        };
    }
}
