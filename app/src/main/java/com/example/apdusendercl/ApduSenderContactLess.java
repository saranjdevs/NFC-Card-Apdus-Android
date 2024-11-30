

package com.example.apdusendercl;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApduSenderContactLess extends Activity implements OnClickListener,NfcHandler, CheckBox.OnCheckedChangeListener {

    static byte[] byteAPDU = null;
    static byte[] respAPDU = null;
    static HexadecimalKbd mHexKbd;

    private static CheckBox mCheckRaw;
    private static CheckBox mCheckResp;
    private static CheckBox mCheckScript;

    private Button mSendAPDUButton;
    private Button mClearLogButton;
    private Button mSetNFCButton;
    private Button mPasteButton;
    private Button exitButton;

    static ImageView icoNfc;
    static ImageView icoCard;

    static TextView TextNfc;
    static TextView TextCard;

    static TextView txtCLA;
    static TextView txtINS;
    static TextView txtP1;
    static TextView txtP2;
    static TextView txtLc;
    static TextView txtDataIn;
    static TextView txtLe;

    static EditText editCLA;
    static EditText editINS;
    static EditText editP1;
    static EditText editP2;
    static EditText editLc;
    static EditText editDataIn;
    static EditText editLe;

    static TextView txtLog;

    private Spinner mCommandsSpinner;

    private NfcAdapter mAdapter = null;
    private NfcBoradCastReceiver nfcBoradCastReceiver;
    private PendingIntent mPendingIntent;
    private String[][] mTechLists;
    private IntentFilter[] mFilters;

    boolean mShowAtr = false;
    NfcAdapter adapter;// = NfcAdapter.getDefaultAdapter(this);
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove the broadcast listener
        this.unregisterReceiver(nfcBoradCastReceiver);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHexKbd = new HexadecimalKbd(this, R.id.keyboardview, R.xml.hexkbd);
        mHexKbd.registerEditText(R.id.editCLA);
        mHexKbd.registerEditText(R.id.editINS);
        mHexKbd.registerEditText(R.id.editP1);
        mHexKbd.registerEditText(R.id.editP2);
        mHexKbd.registerEditText(R.id.editLc);
        mHexKbd.registerEditText(R.id.editDataIn);
        mHexKbd.registerEditText(R.id.editLe);

        txtLog = findViewById(R.id.textLog);
        icoNfc = findViewById(R.id.imageNfc);
        icoNfc.setImageResource(R.drawable.nfc_red);
        icoCard = findViewById(R.id.imageCard);
        icoCard.setImageResource(R.drawable.card_red);
        TextNfc = findViewById(R.id.textNfc);
        TextCard = findViewById(R.id.textCard);

        txtCLA = findViewById(R.id.textCLA);
        txtINS = findViewById(R.id.textINS);
        txtP1 = findViewById(R.id.textP1);
        txtP2 = findViewById(R.id.textP2);
        txtLc = findViewById(R.id.textLc);
        txtDataIn = findViewById(R.id.textDataIn);
        txtLe = findViewById(R.id.textLe);

        editCLA = findViewById(R.id.editCLA);
        editINS = findViewById(R.id.editINS);
        editP1 = findViewById(R.id.editP1);
        editP2 = findViewById(R.id.editP2);
        editLc = findViewById(R.id.editLc);
        editDataIn = findViewById(R.id.editDataIn);
        editLe = findViewById(R.id.editLe);
        findViewById(R.id.scriptLayout).setVisibility(View.GONE);
        mSendAPDUButton = findViewById(R.id.button_SendApdu);
        mSendAPDUButton.setOnClickListener(this::onClick);
        mClearLogButton = findViewById(R.id.button_ClearLog);
        mClearLogButton.setOnClickListener(this::onClick);
        mSetNFCButton = findViewById(R.id.button_SetNFC);
        mSetNFCButton.setOnClickListener(this::onClick);
        mPasteButton = findViewById(R.id.button_Paste);
        mPasteButton.setOnClickListener(this::onClick);

        mCheckRaw = findViewById(R.id.check_box_raw);
        mCheckRaw.setOnCheckedChangeListener(this::onCheckedChanged);
        mCheckResp = findViewById(R.id.check_box_resp);
        mCheckResp.setOnCheckedChangeListener(this::onCheckedChanged);

        //  findViewById(R.id.button_script).setOnClickListener(this::onClick);
        final String[] commadsTableNames = {
                "Built-in APDUs...",
                "SELECT PSE",
                "SELECT PPSE",
                "SELECT VISA AID",
                "SELECT VISA ELECTRON AID",
                "SELECT MASTERCARD AID",
                "SELECT AMEX AID",
                "SELECT DINERS/DISCOVER AID",
                "SELECT INTERAC AID",
                "SELECT CUP AID",
                "READ RECORD SFI:01 R:01",
                "READ RECORD SFI:01 R:02",
                "READ RECORD SFI:02 R:01",
                "READ RECORD SFI:02 R:02",
                "GET ATC",
                "GET LAST ONLINE ATC",
                "GET PIN TRY COUNTER"
        };
        ArrayAdapter<String> commadsTable = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, commadsTableNames);
        mCommandsSpinner = (Spinner) findViewById(R.id.APDU_spinner_table);
        mCommandsSpinner.setAdapter(commadsTable);
        mCommandsSpinner.setSelection(0);
        mCommandsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                int CommandAPDU = mCommandsSpinner.getSelectedItemPosition();
                switch (CommandAPDU) {
                    case 0:

                        mCheckRaw.setChecked(false);
                        editCLA.setText("");
                        editINS.setText("");
                        editP1.setText("");
                        editP2.setText("");
                        editLc.setText("");
                        editLe.setText("");
                        editDataIn.setText("");

                        txtCLA.setEnabled(true);
                        txtINS.setEnabled(true);
                        txtP1.setEnabled(true);
                        txtP2.setEnabled(true);
                        txtLc.setEnabled(true);
                        txtDataIn.setEnabled(true);
                        txtLe.setEnabled(true);
                        txtLe.setEnabled(true);
                        editCLA.setEnabled(true);
                        editINS.setEnabled(true);
                        editP1.setEnabled(true);
                        editP2.setEnabled(true);
                        editLc.setEnabled(true);
                        editDataIn.setEnabled(true);
                        txtDataIn.setText("Data:");
                        editLe.setEnabled(true);
                        editCLA.requestFocus();

                        break;
                    case 1: //SELECT PSE
                        vSetBuiltinCommand();
                        editDataIn.setText("00A404000E315041592E5359532E4444463031");
                        HideKbd();
                        vShowGeneralMesg("Payment System Environment");
                        break;
                    case 2: //SELECT PPSE
                        vSetBuiltinCommand();
                        editDataIn.setText("00A404000E325041592E5359532E4444463031");
                        HideKbd();
                        vShowGeneralMesg("Proximity Payment System Environment");
                        break;
                    case 3: //SELECT VISA AID
                        vSetBuiltinCommand();
                        editDataIn.setText("00A4040007A0000000031010");
                        HideKbd();
                        vShowGeneralMesg("Visa credit or debit");
                        break;
                    case 4: //SELECT VISA ELECTRON AID
                        vSetBuiltinCommand();
                        editDataIn.setText("00A4040007A0000000032010");
                        HideKbd();
                        vShowGeneralMesg("Visa Electron");
                        break;
                    case 5: //SELECT MASTERCARD AID
                        vSetBuiltinCommand();
                        editDataIn.setText("00A4040007A0000000041010");
                        HideKbd();
                        vShowGeneralMesg("MasterCard credit or debit");
                        break;
                    case 6: //SELECT AMEX AID
                        vSetBuiltinCommand();
                        editDataIn.setText("00A4040006A00000002501");
                        HideKbd();
                        vShowGeneralMesg("American Express");
                        break;
                    case 7: //SELECT DINERS/DISCOVER AID
                        vSetBuiltinCommand();
                        editDataIn.setText("00A4040007A0000001523010");
                        HideKbd();
                        vShowGeneralMesg("Diners Club/Discover");
                        break;
                    case 8: //SELECT INTERAC AID
                        vSetBuiltinCommand();
                        editDataIn.setText("00A4040007A0000002771010");
                        HideKbd();
                        vShowGeneralMesg("Interac Debit card");
                        break;
                    case 9: //SELECT CUP AID
                        vSetBuiltinCommand();
                        editDataIn.setText("00A4040008A000000333010101");
                        HideKbd();
                        vShowGeneralMesg("UnionPay Debit");
                        break;
                    case 10: //READRECORD SFI:01 R:01
                        vSetBuiltinCommand();
                        editDataIn.setText("00B2010C00");
                        HideKbd();
                        vShowGeneralMesg("SFI:01 R:01");
                        break;
                    case 11: //READRECORD SFI:01 R:02
                        vSetBuiltinCommand();
                        editDataIn.setText("00B2020C00");
                        HideKbd();
                        vShowGeneralMesg("SFI:01 R:02");
                        break;
                    case 12: //READRECORD SFI:02 R:01
                        vSetBuiltinCommand();
                        editDataIn.setText("00B2011400");
                        HideKbd();
                        vShowGeneralMesg("SFI:02 R:01");
                        break;
                    case 13: //READRECORD SFI:02 R:02
                        vSetBuiltinCommand();
                        editDataIn.setText("00B2021400");
                        HideKbd();
                        vShowGeneralMesg("SFI:02 R:02");
                        break;
                    case 14: //GET ATC
                        vSetBuiltinCommand();
                        editDataIn.setText("80CA9F3600");
                        HideKbd();
                        vShowGeneralMesg("Get Tag 9F36");
                        break;
                    case 15: //GET LAST ONLINE ATC
                        vSetBuiltinCommand();
                        editDataIn.setText("80CA9F1300");
                        HideKbd();
                        vShowGeneralMesg("Get Tag 9F13");
                        break;
                    case 16: //GET PIN TRY COUNTER
                        vSetBuiltinCommand();
                        editDataIn.setText("80CA9F1700");
                        HideKbd();
                        vShowGeneralMesg("Get Tag 9F17");
                        break;


                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                return;
            }
        });


        mSendAPDUButton.setEnabled(false);
        mClearLogButton.setEnabled(true);
        mSetNFCButton.setEnabled(true);
        mCommandsSpinner.setEnabled(true);

        editCLA.setText("");
        editINS.setText("");
        editP1.setText("");
        editP2.setText("");
        editLc.setText("");
        editLe.setText("");
        editDataIn.setText("");

        txtCLA.setEnabled(true);
        txtINS.setEnabled(true);
        txtP1.setEnabled(true);
        txtP2.setEnabled(true);
        txtLc.setEnabled(true);
        txtDataIn.setEnabled(true);
        txtLe.setEnabled(true);
        txtLe.setEnabled(true);
        editCLA.setEnabled(true);
        editINS.setEnabled(true);
        editP1.setEnabled(true);
        editP2.setEnabled(true);
        editLc.setEnabled(true);
        editDataIn.setEnabled(true);
        txtDataIn.setText("Data:");
        editLe.setEnabled(true);
        editCLA.requestFocus();

        resolveIntent(getIntent());

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            mPendingIntent = PendingIntent.getActivity
                    (this, 0, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            mPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        }
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[]{ndef,};
        mTechLists = new String[][]{new String[]{IsoDep.class.getName()}};
    }

    private void registerNfcBroadcastRecevier() {
        nfcBoradCastReceiver = new NfcBoradCastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        this.registerReceiver(nfcBoradCastReceiver, filter);
    }

    private void enableManualApdus() {
        editCLA.setText("");
        editINS.setText("");
        editP1.setText("");
        editP2.setText("");
        editLc.setText("");
        editLe.setText("");
        editDataIn.setText("");

        txtCLA.setEnabled(true);
        txtINS.setEnabled(true);
        txtP1.setEnabled(true);
        txtP2.setEnabled(true);
        txtLc.setEnabled(true);
        txtDataIn.setEnabled(true);
        txtLe.setEnabled(true);
        txtLe.setEnabled(true);
        editCLA.setEnabled(true);
        editINS.setEnabled(true);
        editP1.setEnabled(true);
        editP2.setEnabled(true);
        editLc.setEnabled(true);
        editDataIn.setEnabled(true);
        txtDataIn.setText("Data:");
        editLe.setEnabled(true);
        mCommandsSpinner.setSelection(0);
        editCLA.requestFocus();
    }

    private void disableManualAPDUS() {
        editCLA.setText("");
        editINS.setText("");
        editP1.setText("");
        editP2.setText("");
        editLc.setText("");
        editLe.setText("");
        editDataIn.setText("");

        txtCLA.setEnabled(false);
        txtINS.setEnabled(false);
        txtP1.setEnabled(false);
        txtP2.setEnabled(false);
        txtLc.setEnabled(false);
        txtLe.setEnabled(false);
        editCLA.setEnabled(false);
        editINS.setEnabled(false);
        editP1.setEnabled(false);
        editP2.setEnabled(false);
        editLc.setEnabled(false);
        editLe.setEnabled(false);

    }

    private void enableDataIn() {
        editDataIn.setEnabled(true);
        txtDataIn.setEnabled(true);
        editDataIn.requestFocus();
        txtDataIn.setText("APDU:");
    }

    private void disableDataIn() {
        editDataIn.setEnabled(false);
        txtDataIn.setEnabled(false);
    }


    @Override
    public void onResume() {
        super.onResume();
        clearReadResponseBuffer();
        // Check for available NFC Adapter
        nfcSettings();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void nfcSettings() {

        if (mAdapter != null && mAdapter.isEnabled()) {
            registerNfcBroadcastRecevier();
        }
        if (NfcAdapterHandler.getInstance().checkNfcConnection()) {
            if (mShowAtr == true) {
                icoCard.setImageResource(R.drawable.card_green);
            } else {
                icoCard.setImageResource(R.drawable.card_green);
            }
        } else {
            icoCard.setImageResource(R.drawable.card_red);
        }
        if ((mAdapter == null) || (!mAdapter.isEnabled())) {
            if (mAdapter == null) {
                clearlog();
                TextCard.setText("NO CARD");
                mSendAPDUButton.setEnabled(false);
                mSetNFCButton.setEnabled(false);
                editCLA.setText("");
                editINS.setText("");
                editP1.setText("");
                editP2.setText("");
                editLc.setText("");
                editLe.setText("");
                editDataIn.setText("");
                editCLA.requestFocus();
                print("Please enable NFC of Device....!!!");

            } else if (mAdapter.isEnabled()) {
                clearlog();
                TextNfc.setText("READER ON");
            } else {
                clearlog();
                TextCard.setText("NO CARD");
                editCLA.setText("");
                editINS.setText("");
                editP1.setText("");
                editP2.setText("");
                editLc.setText("");
                editLe.setText("");
                editDataIn.setText("");
                editCLA.requestFocus();
                mSendAPDUButton.setEnabled(false);
                print("Please enable NFC of Device....!!!");
                mSetNFCButton.setEnabled(true);
                icoNfc.setImageResource(R.drawable.nfc_red);
                TextNfc.setText("READER OFF");
            }
        }
        if (mAdapter != null) {
            if (mAdapter.isEnabled()) {
                clearlog();
                TextNfc.setText("READER ON");
                icoNfc.setImageResource(R.drawable.nfc_green);
            }
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        } else {
            clearlog();
            icoNfc.setImageResource(R.drawable.nfc_red);
            TextNfc.setText("READER OFF");
            TextCard.setText("NO CARD");
            mSendAPDUButton.setEnabled(false);
            mSetNFCButton.setEnabled(false);
            editCLA.setText("");
            editINS.setText("");
            editP1.setText("");
            editP2.setText("");
            editLc.setText("");
            editLe.setText("");
            editDataIn.setText("");
            editCLA.requestFocus();
            print("Please enable NFC of Device....!!!");
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        clearReadResponseBuffer();
       if (NfcAdapterHandler.getInstance().checkNfcConnection()) {
            if (mShowAtr == true) {
                icoCard.setImageResource(R.drawable.card_green);
            } else {
                icoCard.setImageResource(R.drawable.card_green);
            }
        } else {
            icoCard.setImageResource(R.drawable.card_red);
        }
        mAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (mHexKbd.isCustomKeyboardVisible()) mHexKbd.hideCustomKeyboard();
        else this.finish();
    }

    public static void HideKbd() {
        if (mHexKbd.isCustomKeyboardVisible()) mHexKbd.hideCustomKeyboard();
    }

    private static void clearlog() {
        txtLog.setText("");
    }

    private static void print(String s) {
        txtLog.append(s);
        txtLog.append("\r\n");
        return;
    }


    private void resolveIntent(Intent intent) {

        Log.i("NFC", "resloveIntent Entry>>>" );
        String action = intent.getAction();
        clearlog();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            final Tag t = (Tag) tag;
            String[] techList = t.getTechList();
            for (String tech : techList) {
                if (tech.equals("android.nfc.tech.IsoDep"))
                {
                    IsoDep icoDepTag = IsoDep.get(t);
                    NfcAdapterHandler.getInstance().setMyTag(icoDepTag);
                    NfcAdapterHandler.getInstance().setmFirstDetected(true);
                    Log.i("NFC", "Tag Tech ISO DEP Found: " + tech );
                }
            }
            if (NfcAdapterHandler.getInstance().getMyTag()!=null &&
                    !NfcAdapterHandler.getInstance().getMyTag().isConnected()) {
                try {
                    NfcAdapterHandler.getInstance().getMyTag().connect();
                    NfcAdapterHandler.getInstance().getMyTag().setTimeout(5000);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            if (NfcAdapterHandler.getInstance().getMyTag()!=null &&
                    NfcAdapterHandler.getInstance().getMyTag().isConnected()) {
                if (mShowAtr == true) {
                    icoCard.setImageResource(R.drawable.card_green);
                } else {
                    icoCard.setImageResource(R.drawable.card_green);
                }
                vShowCardRemovalInfo();
                String szATR = null;
                try {
                    mShowAtr = true;
                    szATR = " 3B " + Util.getATRLeString(NfcAdapterHandler.getInstance().getMyTag().getHistoricalBytes()) + "80 01 " +
                            Util.getHexString(NfcAdapterHandler.getInstance().getMyTag().getHistoricalBytes()) + "" +
                            Util.getATRXorString(NfcAdapterHandler.getInstance().getMyTag().getHistoricalBytes());
                } catch (Exception e) {
                    mShowAtr = false;
                    szATR = "CARD PRESENT";
                }
                TextCard.setText(szATR);

                mSendAPDUButton.setEnabled(true);
                clearlog();
                txtCLA.setEnabled(true);
                txtINS.setEnabled(true);
                txtP1.setEnabled(true);
                txtP2.setEnabled(true);
                txtLc.setEnabled(true);
                txtDataIn.setEnabled(true);
                txtLe.setEnabled(true);

                editCLA.setEnabled(true);
                editINS.setEnabled(true);
                editP1.setEnabled(true);
                editP2.setEnabled(true);
                editLc.setEnabled(true);
                editDataIn.setEnabled(true);
                editLe.setEnabled(true);
                editCLA.setText("");
                editINS.setText("");
                editP1.setText("");
                editP2.setText("");
                editLc.setText("");
                editLe.setText("");
                editDataIn.setText("");
                mCheckRaw.setChecked(false);
                mCheckResp.setChecked(false);
                editCLA.requestFocus();
            } else {
                icoCard.setImageResource(R.drawable.card_red);
            }
        }
        if (NfcAdapterHandler.getInstance().checkNfcConnection()) {
            if (mShowAtr == true) {
                icoCard.setImageResource(R.drawable.card_green);
            } else {
                icoCard.setImageResource(R.drawable.card_green);
            }
        } else {
            icoCard.setImageResource(R.drawable.card_red);
        }
        Log.i("NFC", "resloveIntent Exit>>>" );
    }

    private void vSetBuiltinCommand() {
        clearlog();

        editCLA.setText("");
        editINS.setText("");
        editP1.setText("");
        editP2.setText("");
        editLc.setText("");
        editLe.setText("");
        editDataIn.setText("");

        txtCLA.setEnabled(false);
        txtINS.setEnabled(false);
        txtP1.setEnabled(false);
        txtP2.setEnabled(false);
        txtLc.setEnabled(false);
        txtLe.setEnabled(false);
        editCLA.setEnabled(false);
        editINS.setEnabled(false);
        editP1.setEnabled(false);
        editP2.setEnabled(false);
        editLc.setEnabled(false);
        editLe.setEnabled(false);
        editDataIn.setEnabled(true);
        txtDataIn.setEnabled(true);
        txtDataIn.setText("APDU:");
        mCheckRaw.setChecked(true);

        return;
    }

    private static void vShowResponseInterpretation(byte[] data) {

        print("");
        print("====================================");
        print("RESPONSE INTERPRETATION:");

        if (data.length > 2) {
            byte[] sw12 = new byte[2];
            System.arraycopy(data, data.length - 2, sw12, 0, 2);
            byte[] payload = Arrays.copyOf(data, (data.length) - 2);
            try {
                print("SW1-SW2 " + Util.getHexString(sw12) + RetStatusWord.getSWDescription(Util.szByteHex2String(sw12[0]) + Util.szByteHex2String(sw12[1])));
            } catch (Exception e) {
                e.printStackTrace();
            }
            print(EmvInterpreter.ShowEMV_Interpretation(payload));

        } else if (data.length == 2) {
            byte[] sw12 = new byte[2];
            System.arraycopy(data, data.length - 2, sw12, 0, 2);
            try {
                print("SW1-SW2 " + Util.getHexString(sw12));
                print(RetStatusWord.getSWDescription(Util.szByteHex2String(sw12[0]) + Util.szByteHex2String(sw12[1])));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        print("====================================");
        return;
    }

    private void vShowCardRemovalInfo() {
        Context context = getApplicationContext();
        CharSequence text = "Card Removal will NOT be detected";
        int duration = Toast.LENGTH_LONG;
        HideKbd();
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void vShowGeneralMesg(String szText) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, szText, duration);
        toast.show();
    }

    private void vShowErrorVaules() {
        Context context = getApplicationContext();
        CharSequence text = "C-APDU values ERROR";
        int duration = Toast.LENGTH_LONG;
        HideKbd();
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_SendApdu:
                sendApduHandler();
                break;
            case R.id.button_ClearLog:
                clearLogHandler();
                break;
            case R.id.button_SetNFC:
                nfcSetHandler();
                break;

            case R.id.button_Paste:
                pasteHandler();
                break;

        }

    }

    private void pasteHandler() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        String ClipBoardData = clipboard.getText().toString().toUpperCase();

        if (ClipBoardData.length() > 254) {
            vShowGeneralMesg("Max Length to Paste is 254 chars !");
        } else if (ClipBoardData.length() >= 8) {
            if ((ClipBoardData.length() % 2) != 0) {
                vShowGeneralMesg("String Length must be Even !");
            }
            if (!ClipBoardData.matches("^[0-9A-F]+$")) {
                clearlog();
                print(ClipBoardData);
                vShowGeneralMesg("String should be '0'-'9' or 'A'-'F'");
            } else {
                vSetBuiltinCommand();
                editDataIn.setText(ClipBoardData);
                HideKbd();
                vShowGeneralMesg("Data Pasted Successfully");
            }
        } else {
            vShowGeneralMesg("Length must be greater than 8 chars !");
        }
    }

    private void nfcSetHandler() {
        if (NfcAdapterHandler.getInstance().checkNfcConnection()) {
            if (mShowAtr == true) {
                icoCard.setImageResource(R.drawable.card_green);
            } else {
                icoCard.setImageResource(R.drawable.card_green);
            }
        } else {
            icoCard.setImageResource(R.drawable.card_red);
            clearlog();
            TextCard.setText("PLEASE TAP CARD");
          /*  editCLA.setText("");
            editINS.setText("");
            editP1.setText("");
            editP2.setText("");
            editLc.setText("");
            editLe.setText("");
            editDataIn.setText("");*/
            editCLA.requestFocus();
            mSendAPDUButton.setEnabled(false);
        }
        clearlog();
        startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);
    }

    private void clearLogHandler() {
        if (NfcAdapterHandler.getInstance().checkNfcConnection()) {
            if (mShowAtr == true) {
                icoCard.setImageResource(R.drawable.card_green);
            } else {
                icoCard.setImageResource(R.drawable.card_green);
            }
        } else {
            icoCard.setImageResource(R.drawable.card_red);
            TextCard.setText("PLEASE TAP CARD");
            editCLA.setText("");
            editINS.setText("");
            editP1.setText("");
            editP2.setText("");
            editLc.setText("");
            editLe.setText("");
            editDataIn.setText("");
            editCLA.requestFocus();
            mSendAPDUButton.setEnabled(false);
        }
        clearlog();
    }

    private void sendApduHandler() {
        if (NfcAdapterHandler.getInstance().checkNfcConnection()) {
            if (mShowAtr == true) {
                icoCard.setImageResource(R.drawable.card_green);
            } else {
                icoCard.setImageResource(R.drawable.card_green);
            }
            clearlog();
            HideKbd();
            String StringCLA = editCLA.getText().toString();
            String StringINS = editINS.getText().toString();
            String StringP1 = editP1.getText().toString();
            String StringP2 = editP2.getText().toString();
            String StringLc = editLc.getText().toString();
            String StringDataIn = editDataIn.getText().toString();
            String StringLe = editLe.getText().toString();

            if(!mCheckRaw.isChecked()){
            if (
                    ((StringCLA.length() == 0) ||
                            (StringINS.length() == 0) ||
                            (StringP1.length() == 0) ||
                            (StringP2.length() == 0) ||
                            ((StringDataIn.length() % 2) != 0))) {
                vShowErrorVaules();
                return;
            }
            if (!StringLc.contentEquals("")) {
                if (StringDataIn.length() != (((int) Long.parseLong(StringLc, 16)) * 2)) {
                    vShowErrorVaules();
                    return;
                }
            }
            if (StringLe.length() == 1) {
                StringLe = "0" + StringLe;
                editLe.setText(StringLe);
            }
            if (StringLc.length() == 1) {
                StringLc = "0" + StringLc;
                editLc.setText(StringLc);
            }
            if (StringP2.length() == 1) {
                StringP2 = "0" + StringP2;
                editP2.setText(StringP2);
            }
            if (StringP1.length() == 1) {
                StringP1 = "0" + StringP1;
                editP1.setText(StringP1);
            }
            if (StringINS.length() == 1) {
                StringINS = "0" + StringINS;
                editINS.setText(StringINS);
            }
            if (StringCLA.length() == 1) {
                StringCLA = "0" + StringCLA;
                editCLA.setText(StringCLA);
            }
        }

            String StringAPDU = "";
            if (mCheckRaw.isChecked()) {
                StringAPDU = editDataIn.getText().toString();
                if (((StringAPDU.length() % 2) != 0) || (StringAPDU.length() < 1)) {
                    vShowErrorVaules();
                    return;
                }
            } else {
                StringAPDU = StringCLA + StringINS + StringP1 + StringP2 + StringLc + StringDataIn + StringLe;
            }

            if (!bSendAPDU(StringAPDU)) {
                vShowErrorVaules();
            }
            if (mCheckResp.isChecked()) {
                try
                {
                    vShowResponseInterpretation(respAPDU);
                }
                catch (Exception e)
                {
                    clearlog();
                    print("Response is not TLV format !!!");
                }
            } else {
               // icoCard.setImageResource(R.drawable.card_red);
                clearlog();
                TextCard.setText("PLEASE TAP CARD");
                editCLA.setText("");
                editINS.setText("");
                editP1.setText("");
                editP2.setText("");
                editLc.setText("");
                editLe.setText("");
                editDataIn.setText("");
                editCLA.requestFocus();
                mSendAPDUButton.setEnabled(false);
            }
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.check_box_raw:
                rawCheckBoxHanlder(isChecked);
                break;
            case R.id.check_box_resp:
                respCheckBoxHandler(isChecked);
                break;

        }
    }


    private void respCheckBoxHandler(boolean isChecked) {
        if (NfcAdapterHandler.getInstance().checkNfcConnection()) ;
        else {
            icoCard.setImageResource(R.drawable.card_red);
            clearlog();
            mSendAPDUButton.setEnabled(false);
            TextCard.setText("PLEASE TAP CARD");
            return;
        }
        if ((byteAPDU == null) || (respAPDU == null)) {
            return;
        }
        if (isChecked) {
            clearlog();
            print("***COMMAND APDU***");
            print("");
            try {
                print("IFD - " + Util.getHexString(byteAPDU));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                print("");
                print("ICC - " + Util.getHexString(respAPDU));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mCheckResp.isChecked()) {
                try {
                    vShowResponseInterpretation(respAPDU);
                } catch (Exception e) {
                    clearlog();
                    print("Response is not TLV format !!!");
                }
            }

        } else {
            clearlog();
            print("***COMMAND APDU***");
            print("");
            try {
                print("IFD - " + Util.getHexString(byteAPDU));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                print("");
                print("ICC - " + Util.getHexString(respAPDU));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void rawCheckBoxHanlder(boolean isChecked) {
        if (NfcAdapterHandler.getInstance().checkNfcConnection()) ;
        else {
            icoCard.setImageResource(R.drawable.card_red);
            clearlog();
            mSendAPDUButton.setEnabled(false);
            TextCard.setText("PLEASE TAP CARD");
        }
        if (isChecked) {
            disableManualAPDUS();
            enableDataIn();
        } else {
            enableManualApdus();
        }
    }

    private static byte[] transceives(byte[] data) {
        byte[] ra = null;
        try {
            print("***COMMAND APDU***");
            print("");
            print("IFD - " + Util.getHexString(data));
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        try {
            ra = NfcAdapterHandler.getInstance().getMyTag().transceive(data);
        } catch (IOException e) {

            print("************************************");
            print("         NO CARD RESPONSE");
            print("************************************");

        }
        try {
            print("");
            print("ICC - " + Util.getHexString(ra));
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return (ra);
    }


    private static byte[] atohex(String data) {
        String hexchars = "0123456789abcdef";

        data = data.replaceAll(" ", "").toLowerCase();
        if (data == null) {
            return null;
        }
        byte[] hex = new byte[data.length() / 2];

        for (int ii = 0; ii < data.length(); ii += 2) {
            int i1 = hexchars.indexOf(data.charAt(ii));
            int i2 = hexchars.indexOf(data.charAt(ii + 1));
            hex[ii / 2] = (byte) ((i1 << 4) | i2);
        }
        return hex;
    }

    public void clearReadResponseBuffer() {
        byteAPDU = null;
        respAPDU = null;

    }

    public boolean bSendAPDU(String StringAPDU) {
        byteAPDU = atohex(StringAPDU);
        respAPDU = transceives(byteAPDU);
        return true;
    }

    @Override
    public void nfcStateChanged(boolean state,Intent intent) {
        if(state){
           nfcSettings();
        }

    }
}

