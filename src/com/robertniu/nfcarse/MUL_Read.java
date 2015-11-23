package com.robertniu.nfcarse;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



public class MUL_Read extends BasicActivity {

	
    LinearLayout mLayout;
    TextView mErrorMessage;
    int mMFCSupport;
    private final Handler mHandler = new Handler();
    //
    
	private String GpxFileNameHead = "/MifareUL_";
	private String GpxFileNameEnd = ".xml";
	private String GpxFileName = "";
	private String [] getData=new String[]{"","","",""};

    /**
     * Calls {@link #updateTagInfos(Tag)} (and initialize some member
     * variables).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mul_read);
        ///
        
      //文件名定义
    	Time t=new Time();
    	t.setToNow(); 
    	GpxFileName=GpxFileNameHead+t.year+"_"+(t.month+1)+"_"
    	+t.monthDay+"_"+t.hour+"_"+t.minute+"_"+t.second+GpxFileNameEnd;
    	////

        mLayout = (LinearLayout) findViewById(R.id.linearLayoutTagInfoTool);
        mErrorMessage = (TextView) findViewById(
                R.id.textTagInfoToolErrorMessage);
        updateTagInfos(Common.getTag());
        
    }

    /**
     * Calls {@link Common#treatAsNewTag(Intent, android.content.Context)} and
     * then calls {@link #updateTagInfos(Tag)}
     */
    @Override
    public void onNewIntent(Intent intent) {
        Common.treatAsNewTag(intent, this);
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            updateTagInfos(Common.getTag());
        }
    }

    /**
     * Show a dialog with further information.
     * @param view The View object that triggered the method
     * (in this case the read more button).
     */
    public void onReadMore(View view) {
    	
    	 /////
        WriteXML.WriteGpxFile("##########################\n",GpxFileName);
    	WriteXML.WriteGpxFile("初始卡数据:\n",GpxFileName);
    	WriteXML.WriteGpxFile("Pg00-03:\n"+ getData[0] + "\n",GpxFileName);
    	WriteXML.WriteGpxFile("Pg04-07:\n"+ getData[1] + "\n",GpxFileName);
    	WriteXML.WriteGpxFile("Pg08-11:\n"+ getData[2] + "\n",GpxFileName);
    	WriteXML.WriteGpxFile("Pg12-15:\n"+ getData[3] + "\n",GpxFileName);
    	WriteXML.WriteGpxFile("##########################\n",GpxFileName);

        // 
        Toast.makeText(this,R.string.info_write_log_success, Toast.LENGTH_LONG).show();
        Toast.makeText(this,"文件名:"+GpxFileName, Toast.LENGTH_LONG).show();
        /////
      
    }
    
    private String TransBintoHuman(String Bin,int startID,int lg)
    {
    	
    	//十六进制转二进制，再截取从第startID开始的lg长度 
	    String myMoney1=(Converter.HexString2BinaryString(Bin)).substring(startID,startID+lg);
	    //二进制字符串转十进制字符串
        String myMoney2=Integer.valueOf(myMoney1,2).toString();
        
        
    	return myMoney2;
    	
    }

 
    /**
     * Update and display the tag information.
     * If there is no Mifare Classic support, a warning will be shown.
     * @param tag A Tag from an NFC Intent.
     */
    private void updateTagInfos(Tag tag) {

        if (tag != null) {
            // Check for Mifare Classic support.
            mMFCSupport = Common.checkMifareClassicSupport(tag, this);
            

            mLayout.removeAllViews();
            // Display generic info.
            // Create views and add them to the layout.
            TextView headerGenericInfo = new TextView(this);
            headerGenericInfo.setText(Common.colorString(
                    getString(R.string.text_generic_info),
                    getResources().getColor(R.color.blue)));
            headerGenericInfo.setBackgroundColor(
                    getResources().getColor(R.color.dark_gray));
            headerGenericInfo.setTextAppearance(this,
                    android.R.style.TextAppearance_Large);
            headerGenericInfo.setGravity(Gravity.CENTER_HORIZONTAL);
            int pad = Common.dpToPx(5); // 5dp to px.
            headerGenericInfo.setPadding(pad, pad, pad, pad);
            mLayout.addView(headerGenericInfo);
            TextView genericInfo = new TextView(this);
            genericInfo.setPadding(pad, pad, pad, pad);
            genericInfo.setTextAppearance(this,
                    android.R.style.TextAppearance_Medium);
            mLayout.addView(genericInfo);
            
            ////CartBin:
            // Get generic info and set these as text.
            //String CartBin = Common.byte2HexString(tag.getId());
            
            String CartBin = ""; 
            
            try {
            MifareUltralight mifarelt =MifareUltralight.get(tag);
            mifarelt.connect();
            
		
            switch (mifarelt.getType()) 
            {      
            case 1:      
                CartBin = "Mifare_ULTRALIGHT ";      
                break;      
            case 2:      
                CartBin = "Mifare_ULTRALIGHT_C ";      
                break;       
            case -1:      
                CartBin = "TYPE_UNKNOWN";      
                break;      
           


			} 
            
           
            byte[] payload0 = mifarelt.readPages(0);
            byte[] payload1 = mifarelt.readPages(4);
            byte[] payload2 = mifarelt.readPages(8);
            byte[] payload3 = mifarelt.readPages(12);
		    getData[0]= Converter.getHexString(payload0, payload0.length);
		    getData[1]= Converter.getHexString(payload1, payload1.length);
		    getData[2]= Converter.getHexString(payload2, payload2.length);
		    getData[3]= Converter.getHexString(payload3, payload3.length);
				
		    //payload1.length=16;

            CartBin += getData[0]+"\n"+getData[1]+"\n"+getData[2]+"\n"+getData[3];

           
			} 
            
            catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            /*
            int CartBinLen = tag.getId().length;
            CartBin += " (" + CartBinLen + " byte";
            if (CartBinLen == 7) {
                CartBin += ", CL2";
            } else if (CartBinLen == 10) {
                CartBin += ", CL3";
            }
            CartBin += ")";
            */
            
            ////tagType
            NfcA nfca = NfcA.get(tag);
            // Swap ATQA to match the common order like shown here:
            // http://nfc-tools.org/index.php?title=ISO14443A
            byte[] atqaBytes = nfca.getAtqa();
            atqaBytes = new byte[] {atqaBytes[1], atqaBytes[0]};
            String atqa = Common.byte2HexString(atqaBytes);
            String sak = Common.byte2HexString(new byte[] {(byte)nfca.getSak()});
            String ats = "-";
            IsoDep iso = IsoDep.get(tag);
            if (iso != null ) {
                ats = Common.byte2HexString(iso.getHistoricalBytes());
            }
            // Identify tag type.
            int tagTypeResourceID = getTagIdentifier(atqa, sak, ats);
            String tagType = "";
            if (tagTypeResourceID == R.string.tag_unknown && mMFCSupport > -2) {
                tagType = getString(R.string.tag_unknown_mf_classic);
            } else {
                tagType = getString(tagTypeResourceID);
            }
            
            ////
            //purchaseValue	174	17 
            String PartTwo_a=TransBintoHuman(getData[0]+getData[1]+getData[2]+getData[3],174,17);
            //加入小数点
            String PartTwo_b=(new StringBuffer(PartTwo_a).insert((PartTwo_a.length()-2),".")).toString();
            String PartTwo=PartTwo_b+"元";
            
            //cardBaseDateTime	143	9
            //startDateTime	268	22
            //validityDuration	212	7
            String PartThree_Base=TransBintoHuman(getData[0]+getData[1]+getData[2]+getData[3],143,9);
            String PartThree_AddMin=TransBintoHuman(getData[0]+getData[1]+getData[2]+getData[3],268,22);
            int year=(Integer.valueOf(PartThree_Base))/12+2006;
            int month=(Integer.valueOf(PartThree_Base))%12+1;
            int day=(Integer.valueOf(PartThree_AddMin))/1440;
            int hour=((Integer.valueOf(PartThree_AddMin))%1440)/60;
            int min=((Integer.valueOf(PartThree_AddMin))%1440)%60;
            String PartThree_Dura=TransBintoHuman(getData[0]+getData[1]+getData[2]+getData[3],212,7);
            
            //lifecycleCount	130	10
            String PartFour=TransBintoHuman(getData[0]+getData[1]+getData[2]+getData[3],130,10);
            //lastLocation	298	12
            //validityOrigin	345	12
            
            String PartFive_Last=TransBintoHuman(getData[0]+getData[1]+getData[2]+getData[3],298,12);
            String PartFive_Origin=TransBintoHuman(getData[0]+getData[1]+getData[2]+getData[3],345,12);
           
            ////
            //serialNumber	96	32
            String PartSix=TransBintoHuman(getData[0]+getData[1]+getData[2]+getData[3],96,32);
            
            int hc = getResources().getColor(R.color.light_green);
            genericInfo.setText(TextUtils.concat(
            		//第一部分：卡类型和数据
                    Common.colorString(getString(R.string.text_uid) + ":", hc),
                    "\n", CartBin, "\n",
                    
                    //第二部分：卡内余额    purchaseValue	174	17
                    Common.colorString(getString(R.string.text_rf_tech) + ":", hc),
                    // Tech is always ISO 14443a due to NFC Intet filter.
                    // "\n", getString(R.string.text_rf_tech_14a), "\n",
                    "\n", PartTwo, "\n",
                    
                    //第三部分：startDateTime	268	22
                    Common.colorString(getString(R.string.text_atqa) + ":", hc),
                    //"\n", atqa, "\n",
                    "\n", 
                    "cardBaseDateTime[月]:"+PartThree_Base+
                    "\nstartDateTime[分钟]:"+PartThree_AddMin+
                    "\nvalidityDuration[有效期]:"+PartThree_Dura+
                    "\n"+year+"年"+month+"月+"+day+"天"+ hour+"小时"+min+"分钟"
                    , "\n",
                    
                    //第四部分：lifecycleCount	130	10
                    Common.colorString(getString(R.string.text_sak) + ":", hc),
                    //"\n", sak, "\n",
                    "\n", PartFour, "\n",
                    
                    //第五部分：lastLocation	298	12  
                    //validityOrigin	337	12
                    Common.colorString(getString(R.string.text_ats) + ":", hc),
                    //"\n", ats, "\n",
                    "\n", PartFive_Last+"\nvalidityOrigin:"+PartFive_Origin, "\n",
                    
                    //第六部分：serialNumber	96	32
                    Common.colorString(getString(R.string.text_tag_type_and_manuf) + ":", hc),
                    "\n", PartSix));

            // Add message that the tag type might be wrong.
            /*
            if (tagTypeResourceID != R.string.tag_unknown) {
                TextView tagTypeInfo = new TextView(this);
                tagTypeInfo.setPadding(pad, 0, pad, pad);
                tagTypeInfo.setText(
                        "(" + getString(R.string.text_tag_type_guess) + ")");
                mLayout.addView(tagTypeInfo);
            }
            */
            
            LinearLayout layout = (LinearLayout) findViewById(
                    R.id.linearLayoutTagInfoToolSupport);
            
            
            ////
            // Check for Mifare Classic support.
            if (mMFCSupport == 0) {
                // Display Mifare Classic info.
                // Create views and add them to the layout.
                TextView headerMifareInfo = new TextView(this);
                headerMifareInfo.setText(Common.colorString(
                        getString(R.string.text_mf_info),
                        getResources().getColor(R.color.blue)));
                headerMifareInfo.setBackgroundColor(
                        getResources().getColor(R.color.dark_gray));
                headerMifareInfo.setTextAppearance(
                        this, android.R.style.TextAppearance_Large);
                headerMifareInfo.setGravity(Gravity.CENTER_HORIZONTAL);
                headerMifareInfo.setPadding(pad, pad, pad, pad);
                mLayout.addView(headerMifareInfo);
                TextView mifareInfo = new TextView(this);
                mifareInfo.setPadding(pad, pad, pad, pad);
                mifareInfo.setTextAppearance(this,
                        android.R.style.TextAppearance_Medium);
                mLayout.addView(mifareInfo);

                // Get Mifare info and set these as text.
                MifareClassic mfc = MifareClassic.get(tag);
                String size = "" + mfc.getSize();
                String sectorCount = "" + mfc.getSectorCount();
                String blockCount = "" + mfc.getBlockCount();
                mifareInfo.setText(TextUtils.concat(
                        Common.colorString(getString(
                                R.string.text_mem_size) + ":", hc),
                        "\n", size, " byte\n",
                        Common.colorString(getString(
                                R.string.text_block_size) + ":", hc),
                        // Block size is always 16 byte on Mifare Classic Tags.
                        "\n", "" + MifareClassic.BLOCK_SIZE, " byte\n",
                        Common.colorString(getString(
                                R.string.text_sector_count) + ":", hc),
                        "\n", sectorCount, "\n",
                        Common.colorString(getString(
                                R.string.text_block_count) + ":", hc),
                        "\n", blockCount));
                layout.setVisibility(View.GONE);
            } else if (mMFCSupport == -1) {
                // No Mifare Classic Support (due to the device hardware).
                // Set error message.
                mErrorMessage.setText(R.string.text_no_mfc_support_device);
                layout.setVisibility(View.VISIBLE);
            } else if (mMFCSupport == -2) {
                // The tag does not support Mifare Classic.
                // Set error message.
                mErrorMessage.setText(R.string.text_no_mfc_support_tag);
                layout.setVisibility(View.VISIBLE);
            }
        } else {
            // There is no Tag.
            TextView text = new TextView(this);
            int pad = Common.dpToPx(5);
            text.setPadding(pad, pad, 0, 0);
            text.setTextAppearance(this, android.R.style.TextAppearance_Large);
            text.setText(getString(R.string.text_no_tag));
            mLayout.removeAllViews();
            mLayout.addView(text);
            Toast.makeText(this, R.string.info_no_tag_found,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get (determine) the tag type resource ID from ATQA + SAK + ATS.
     * If no resource is found check for the tag type only on ATQA + SAK
     * (and then on ATQA only).
     * @param atqa The ATQA from the tag.
     * @param sak The SAK from the tag.
     * @param ats The ATS from the tag.
     * @return The resource ID.
     */
    private int getTagIdentifier(String atqa, String sak, String ats) {
        String prefix = "tag_";
        ats = ats.replace("-", "");

        // First check on ATQA + SAK + ATS.
        int ret = getResources().getIdentifier(
                prefix + atqa + sak + ats, "string", getPackageName());

        if (ret == 0) {
            // Check on ATQA + SAK.
            ret = getResources().getIdentifier(
                    prefix + atqa + sak, "string", getPackageName());
        }

        if (ret == 0) {
            // Check on ATQA.
            ret = getResources().getIdentifier(
                    prefix + atqa, "string", getPackageName());
        }

        if (ret == 0) {
            // No match found return "Unknown".
            return R.string.tag_unknown;
        }
        return ret;
    }
    
    
}
