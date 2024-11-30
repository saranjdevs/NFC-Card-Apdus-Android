package com.example.apdusendercl;

import android.nfc.tech.IsoDep;

public class NfcAdapterHandler {
    public IsoDep getMyTag() {
        return myTag;
    }

    public void setMyTag(IsoDep myTag) {
        this.myTag = myTag;
    }

    public boolean ismFirstDetected() {
        return mFirstDetected;
    }

    public void setmFirstDetected(boolean mFirstDetected) {
        this.mFirstDetected = mFirstDetected;
    }



    private IsoDep myTag;
    private boolean mFirstDetected = false;
    private static NfcAdapterHandler nfcAdapter;
    private NfcAdapterHandler(){}
    public static NfcAdapterHandler getInstance(){
        if(nfcAdapter==null)
            nfcAdapter=new NfcAdapterHandler();
        return  nfcAdapter;
    }

    public boolean checkNfcConnection() {

        return ((mFirstDetected) && (myTag!=null && myTag.isConnected()));
    }
}
