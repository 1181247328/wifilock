package com.deelock.wifilock.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binChuan on 2017\9\22 0022.
 */

public class UserManage {

    private String pid;

    @SerializedName("pwdList")
    private List<UserPassword> passwords;

    @SerializedName("userFprintList")
    private List<UserFPrint> fPrints;

    private List<Auth> authList;

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public void setPasswords(List<UserPassword> passwords) {
        this.passwords = passwords;
    }

    public List<UserPassword> getPasswords() {
        if (passwords == null){
            passwords = new ArrayList<>();
        }
        return passwords;
    }

    public void setfPrints(List<UserFPrint> fPrints) {
        this.fPrints = fPrints;
    }

    public List<UserFPrint> getfPrints() {
        if (fPrints == null){
            fPrints = new ArrayList<>();
        }
        return fPrints;
    }

    public List<Auth> getAuthList() {
        return authList;
    }

    public void setAuthList(List<Auth> authList) {
        this.authList = authList;
    }

    public class Auth{

        /**
         * timeCreate : 1546510073
         * isBtopen : 1
         * pid : EF1DF11AB2C44FD4B7EA25C18144B708
         * state : 1
         * isFprint : 1
         * authUid : 181004
         * isPwd : 1
         */

        private int timeCreate;
        private int isBtopen;
        private String pid;
        private int state;
        private int isFprint;
        private int authUid;
        private int isPwd;

        public int getTimeCreate() {
            return timeCreate;
        }

        public void setTimeCreate(int timeCreate) {
            this.timeCreate = timeCreate;
        }

        public int getIsBtopen() {
            return isBtopen;
        }

        public void setIsBtopen(int isBtopen) {
            this.isBtopen = isBtopen;
        }

        public String getPid() {
            return pid;
        }

        public void setPid(String pid) {
            this.pid = pid;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getIsFprint() {
            return isFprint;
        }

        public void setIsFprint(int isFprint) {
            this.isFprint = isFprint;
        }

        public int getAuthUid() {
            return authUid;
        }

        public void setAuthUid(int authUid) {
            this.authUid = authUid;
        }

        public int getIsPwd() {
            return isPwd;
        }

        public void setIsPwd(int isPwd) {
            this.isPwd = isPwd;
        }
    }


}
