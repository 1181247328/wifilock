package com.deelock.wifilock.entity;

public class ZigBeeDetail {


    /**
     * devObj : {"devId":"C000C43B00158D000131","alertType":"11","gtwId":"000100005CCF7F6204B5","signalStrength":"64","isCall":1,"isOnline":1,"isRent":0,"devName":"zigbee门磁","mac":"C4-3B-00-15-8D-00--01-31","timeBind":1542781117,"isProsHelp":0,"version2":"EC","version1":"F5","timeRegister":0}
     */

    private DevObjBean devObj;

    public DevObjBean getDevObj() {
        return devObj;
    }

    public void setDevObj(DevObjBean devObj) {
        this.devObj = devObj;
    }

    public static class DevObjBean {
        /**
         * devId : C000C43B00158D000131
         * alertType : 11
         * gtwId : 000100005CCF7F6204B5
         * signalStrength : 64
         * isCall : 1
         * isOnline : 1
         * isRent : 0
         * devName : zigbee门磁
         * mac : C4-3B-00-15-8D-00--01-31
         * timeBind : 1542781117
         * isProsHelp : 0
         * version2 : EC
         * version1 : F5
         * timeRegister : 0
         */

        private String devId;
        private String alertType;
        private String gtwId;
        private String signalStrength;
        private int isCall;
        private int isOnline;
        private int isRent;
        private String devName;
        private String mac;
        private int timeBind;
        private int isProsHelp;
        private String version2;
        private String version1;
        private int timeRegister;

        public String getDevId() {
            return devId;
        }

        public void setDevId(String devId) {
            this.devId = devId;
        }

        public String getAlertType() {
            return alertType;
        }

        public void setAlertType(String alertType) {
            this.alertType = alertType;
        }

        public String getGtwId() {
            return gtwId;
        }

        public void setGtwId(String gtwId) {
            this.gtwId = gtwId;
        }

        public String getSignalStrength() {
            return signalStrength;
        }

        public void setSignalStrength(String signalStrength) {
            this.signalStrength = signalStrength;
        }

        public int getIsCall() {
            return isCall;
        }

        public void setIsCall(int isCall) {
            this.isCall = isCall;
        }

        public int getIsOnline() {
            return isOnline;
        }

        public void setIsOnline(int isOnline) {
            this.isOnline = isOnline;
        }

        public int getIsRent() {
            return isRent;
        }

        public void setIsRent(int isRent) {
            this.isRent = isRent;
        }

        public String getDevName() {
            return devName;
        }

        public void setDevName(String devName) {
            this.devName = devName;
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
    }
}
