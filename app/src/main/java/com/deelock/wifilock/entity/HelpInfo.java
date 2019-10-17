package com.deelock.wifilock.entity;

/**
 * Created by forgive for on 2018\6\8 0008.
 */
public class HelpInfo {

    private String helpNumber;
    private String helpName;
    private String noticeName;

    public void setHelpName(String helpName) {
        this.helpName = helpName;
    }

    public String getHelpName() {
        return helpName;
    }

    public void setHelpNumber(String helpNumber) {
        this.helpNumber = helpNumber;
    }

    public String getHelpNumber() {
        return helpNumber;
    }

    public void setNoticeName(String noticeName) {
        this.noticeName = noticeName;
    }

    public String getNoticeName() {
        return noticeName;
    }
}
