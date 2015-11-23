package com.robertniu.nfcarse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import android.content.Context;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.MifareUltralight;
import android.util.Log;
import android.util.SparseArray;


/**
 * Provides functions to read/write/analyze a Mifare Classic tag.
 * @author Gerhard Klostermeier
 */
public class MifareUltralightRW {

    private static final String LOG_TAG = MifareUltralightRW.class.getSimpleName();
    /**
     * Placeholder for not found keys.
     */
    public  static final String NO_KEY = "------------";
    /**
     * Placeholder for unreadable blocks.
     */
    public  static final String NO_DATA = "--------------------------------";

    private final Tag mTag;
    //private final MifareClassic mMFC;
    private final MifareUltralight mMFC ;
    private SparseArray<byte[][]> mKeyMap = new SparseArray<byte[][]>();
    private int mKeyMapStatus = 0;
    private int mLastSector = -1;
    private int mFirstSector = 0;
    private ArrayList<byte[]> mKeysWithOrder;

    /**
     * Initialize a Mifare Classic reader for the given tag.
     * @param tag The tag to operate on.
     */
    private MifareUltralightRW(Tag tag) {
        mTag = tag;
        mMFC = MifareUltralight.get(mTag);
    }

    /**
     * Get new instance of {@link MCReader}.
     * If the tag is "null" or if it is not a Mifare Classic tag, "null"
     * will be returned.
     * @param tag The tag to operate on.
     * @return {@link MCReader} object or "null" if tag is "null" or tag is
     * not Mifare Classic.
     */
    public static MifareUltralightRW get(Tag tag) {
    	MifareUltralightRW mcr = null;
        if (tag != null) {
            mcr = new MifareUltralightRW(tag);
            if (mcr.isMifareClassic() == false) {
                return null;
            }
        }
        return mcr;
    }

    /**
     * Read as much as possible from the tag with the given key information.
     * @param keyMap Keys (A and B) mapped to a sector.
     * See {@link #buildNextKeyMapPart()}.
     * @return A Key-Value Pair. Keys are the sector numbers, values
     * are the tag data. This tag data (values) are arrays containing
     * one block per field (index 0-3 or 0-15).
     * If a block is "null" it means that the block couldn't be
     * read with the given key information.<br />
     * On Error "null" will be returned (tag was removed during reading or
     * keyMap is null). If none of the keys in the key map is valid for reading
     * and therefore no sector is read, an empty set (SparseArray.size() == 0)
     * will be returned.
     * @see #buildNextKeyMapPart()
     */
    public SparseArray<String[]> readAsMuchAsPossible(
            SparseArray<byte[][]> keyMap) {
        SparseArray<String[]> ret = null;
        if (keyMap != null && keyMap.size() > 0) {
            ret = new SparseArray<String[]>(keyMap.size());
            // For all entries in map do:
            for (int i = 0; i < keyMap.size(); i++) {
                String[][] results = new String[2][];
                try {
                    if (keyMap.valueAt(i)[0] != null) {
                        // Read with key A.
                        results[0] = readSector(
                                );
                    }
                    if (keyMap.valueAt(i)[1] != null) {
                        // Read with key B.
                        results[1] = readSector(
                                );
                    }
                } catch (TagLostException e) {
                    return null;
                }
                // Merge results.
                if (results[0] != null || results[1] != null) {
                    ret.put(keyMap.keyAt(i), mergeSectorData(
                            results[0], results[1]));
                }
            }
            return ret;
        }
        return ret;
    }


    /**
     * Read a as much as possible from a sector with the given key.
     * Best results are gained from a valid key B (except key B is marked as
     * readable in the access conditions).
     * @param sectorIndex Index of the Sector to read. (For Mifare Classic 1K:
     * 0-63)
     * @param key Key for the authentication.
     * @param useAsKeyB If true, key will be treated as key B
     * for authentication.
     * @return Array of blocks (index 0-3 or 0-15). If a block or a key is
     * marked with {@link #NO_DATA} or {@link #NO_KEY}
     * it means that this data could be read or found. On authentication error
     * "null" will be returned.
     * @throws TagLostException When tag is lost.
     * @see #mergeSectorData(String[], String[])
     */
    public String[] readSector() throws TagLostException 
            {
       
        String[] ret = null;
       
		try {
			byte[] payload0 = mMFC.readPages(0);
	        byte[] payload1 = mMFC.readPages(4);
	        byte[] payload2 = mMFC.readPages(8);
	        byte[] payload3 = mMFC.readPages(12);
	        

	        // String readpages= new String(payload,Charset.forName("US-ASCII"));
	         ret[0]= Converter.getHexString(payload0, payload0.length);
	         ret[1]= Converter.getHexString(payload1, payload1.length);
	         ret[2]= Converter.getHexString(payload2, payload2.length);
	         ret[3]= Converter.getHexString(payload3, payload3.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     
        return ret;
    }



   
    /**
     * Merge the result of two {@link #readSector(int, byte[], boolean)}
     * calls on the same sector (with different keys or authentication methods).
     * In this case merging means empty blocks will be overwritten with non
     * empty ones and the keys will be added correctly to the sector trailer.
     * The access conditions will be taken from the first (firstResult)
     * parameter if it is not null.
     * @param firstResult First
     * {@link #readSector(int, byte[], boolean)} result.
     * @param secondResult Second
     * {@link #readSector(int, byte[], boolean)} result.
     * @return Array (sector) as result of merging the given
     * sectors. If a block is {@link #NO_DATA} it
     * means that none of the given sectors contained data from this block.
     * @see #readSector(int, byte[], boolean)
     * @see #authenticate(int, byte[], boolean)
     */
    public String[] mergeSectorData(String[] firstResult,
            String[] secondResult) {
        String[] ret = null;
        if (firstResult != null || secondResult != null) {
            if ((firstResult != null && secondResult != null)
                    && firstResult.length != secondResult.length) {
                return null;
            }
            int length  = (firstResult != null)
                    ? firstResult.length : secondResult.length;
            ArrayList<String> blocks = new ArrayList<String>();
            // Merge data blocks.
            for (int i = 0; i < length -1 ; i++) {
                if (firstResult != null && firstResult[i] != null
                        && !firstResult[i].equals(NO_DATA)) {
                    blocks.add(firstResult[i]);
                } else if (secondResult != null && secondResult[i] != null
                        && !secondResult[i].equals(NO_DATA)) {
                    blocks.add(secondResult[i]);
                } else {
                    // Non of the results got the data for the block.
                    blocks.add(NO_DATA);
                }
            }
            ret = blocks.toArray(new String[blocks.size() + 1]);
            int last = length - 1;
            // Merge sector trailer.
            if (firstResult != null && firstResult[last] != null
                    && !firstResult[last].equals(NO_DATA)) {
                // Take first for sector trailer.
                ret[last] = firstResult[last];
                if (secondResult != null && secondResult[last] != null
                        && !secondResult[last].equals(NO_DATA)) {
                    // Merge key form second result to sector trailer.
                    ret[last] = ret[last].substring(0, 20)
                            + secondResult[last].substring(20);
                }
            } else if (secondResult != null && secondResult[last] != null
                    && !secondResult[last].equals(NO_DATA)) {
                // No first result. Take second result as sector trailer.
                ret[last] = secondResult[last];
            } else {
                // No sector trailer at all.
                ret[last] = NO_DATA;
            }
        }
        return ret;
    }

    
   

  

    /**
     * Get the key map build from {@link #buildNextKeyMapPart()} with
     * the given key file ({@link #setKeyFile(File[], Context)}). If you want a
     * full key map, you have to call {@link #buildNextKeyMapPart()} as
     * often as there are sectors on the tag
     * (See {@link #getSectorCount()}).
     * @return A Key-Value Pair. Keys are the sector numbers,
     * values are the Mifare keys.
     * The Mifare keys are 2D arrays with key type (first dimension, 0-1,
     * 0 = KeyA / 1 = KeyB) and key (second dimension, 0-6). If a key "null"
     * it means that the key A or B (depending of first dimension) could not
     * be found.
     * @see #getSectorCount()
     * @see #buildNextKeyMapPart()
     */
    public SparseArray<byte[][]> getKeyMap() {
        return mKeyMap;
    }

    public boolean isMifareClassic() {
        if (mMFC == null) {
            return false;
        }
        return true;
    }

    public String getType()
    {
    String typeS = ""; 
    switch (mMFC.getType()) {      
    case 1:      
        typeS = "Mifare_ULTRALIGHT ";      
        break;      
    case 2:      
        typeS = "Mifare_ULTRALIGHT_C ";      
        break;       
    case -1:      
        typeS = "TYPE_UNKNOWN";      
        break;      
    }  
    return typeS;
    }


    /**
     * Check if the reader is connected to the tag.
     * @return True if the reader is connected. False otherwise.
     */
    public boolean isConnected() {
        return mMFC.isConnected();
    }

    /**
     * Connect the reader to the tag.
     */
    public void connect() throws IOException {
        try {
            mMFC.connect();
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error while connecting to tag.");
            throw e;
        }
    }

    /**
     * Close the connection between reader an tag.
     */
    public void close() {
        try {
            mMFC.close();
        }
        catch (IOException e) {
            Log.d(LOG_TAG, "Error on closing tag.");
        }
    }
}
