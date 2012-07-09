package com.example.sasijbeam;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements CreateNdefMessageCallback  {
	
	EditText et1;
	String smsToBeam;
	NfcAdapter nfcAdapter;
	TextView mInfoText;
	Context c;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        c = getBaseContext();
        
        if (Build.VERSION.SDK_INT >= 14) { // Android Beam is only for 4.0+
        	  nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());        
        }
        
        
        // Register callback
        nfcAdapter.setNdefPushMessageCallback(this, this);
        
        et1 = ((EditText) findViewById(R.id.editText1));       
        ((EditText)findViewById(R.id.editText1)).addTextChangedListener(new TextWatcher() {
        	 
            public void afterTextChanged(Editable s) {
            	smsToBeam = et1.getText().toString();
            	// Register callback
                nfcAdapter.setNdefPushMessageCallback(MainActivity.this, MainActivity.this);
            }
         
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}         
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
         
        });
        
        
    }
    
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        //String text = ("Beam me up, Android!\n\n" +
        //        "Beam Time: " + System.currentTimeMillis());
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], smsToBeam.getBytes());
        return new NdefMessage(new NdefRecord[] {record});
    }
        
    
    private void checkAndProcessBeamIntent(Intent intent) {
    	  String action = intent.getAction();
    	  if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)){
    		  Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
    		  // only one message sent during the beam
    		  if (rawMsgs != null && rawMsgs.length > 0) {
    			  NdefMessage msg = (NdefMessage) rawMsgs[0];
    			  // record 0 contains the MIME type, record 1 is the AAR, if present
    			  NdefRecord[] records = msg.getRecords();
    			  if (records != null && records.length > 0) {
    				  //process records[0].getPayload()
    				  showMessageText(records[0]);
    			  }
    		  }
    	  }
    }
    
	/**
	 * Muestra el mensaje
	 * @param record
	 */
	private void showMessageText(NdefRecord record){
	 	   String resultado = getText(record);
	 	   this.showSimpleToast(getApplicationContext(), resultado);
	 }
	
	 /**
     * Obtiene la carga del mensaje
     * @param record
     * @return
     */
	public String getText(NdefRecord record){
	    	byte[] payload = record.getPayload();
	        if(payload == null) return null;
	        try {
	            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
	            int languageCodeLength = payload[0] & 0077;
	            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return null;
	}

    
	@Override 
    protected void onResume() {
    	super.onResume();
    	if (Build.VERSION.SDK_INT >= 14) {
    		if (nfcAdapter != null) {
    			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    			IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
    	        
    	        try{ndef.addDataType("text/plain");}
    	        catch (MalformedMimeTypeException e) {throw new RuntimeException("fail mime type", e);}
    	        IntentFilter[] intentFiltersArray = new IntentFilter[] {ndef };
    	        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
    	    }
    	 }
    }
	
	 @Override protected void onPause() {
		  super.onPause();
		  if (Build.VERSION.SDK_INT >= 14) {
			  if (nfcAdapter != null)
				  nfcAdapter.disableForegroundDispatch(this);
		  }
	}
	
	 @Override
	 protected void onNewIntent(Intent intent) {
		  super.onNewIntent(intent);
		  if (Build.VERSION.SDK_INT >= 14) {
			  checkAndProcessBeamIntent(intent);
		  }
	}
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    public void showSimpleToast(Context c, String sms){
		Toast.makeText(c, sms, Toast.LENGTH_SHORT).show();
	}
    
}//juanjo''ss
