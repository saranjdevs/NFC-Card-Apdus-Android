package com.example.apdusendercl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;

public class NfcBoradCastReceiver extends BroadcastReceiver {
    private NfcHandler nfcHandler;
    public NfcBoradCastReceiver(NfcHandler nfcHandler){
        this.nfcHandler=nfcHandler;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                final int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE,
                        NfcAdapter.STATE_OFF);
                switch (state) {
                    case NfcAdapter.STATE_OFF:
                 //   case NfcAdapter.STATE_TURNING_OFF:
                        nfcHandler.nfcStateChanged(true,intent);
                        break;
                    case NfcAdapter.STATE_ON:
                   // case NfcAdapter.STATE_TURNING_ON:
                        nfcHandler.nfcStateChanged(true,intent);
                        break;
                }
            }


    }
}
