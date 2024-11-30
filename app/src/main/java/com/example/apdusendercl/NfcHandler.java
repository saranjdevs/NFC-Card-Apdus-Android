package com.example.apdusendercl;

import android.content.Intent;

public interface NfcHandler {
    public void nfcStateChanged(boolean state, Intent intent);
}
