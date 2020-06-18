package com.lu.uni.igorzfeel.passport_emulator_fake_pace

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.util.*

class CardService: HostApduService() {

    companion object {
        val TAG: String  = "CardService"

        val OK_RAPDU        = Utils.hexStringToByteArray("9000")
        val FAILED_RAPDU    = Utils.hexStringToByteArray("6F00")
        val UNKNOWN_RAPDU   = Utils.hexStringToByteArray("0000")
        val SELECT_CAPDU    = Utils.hexStringToByteArray("00A4040C07A0000002471001")
        val CA1_CAPDU       = Utils.hexStringToByteArray("00A4020C02011C")
        val CA2_CAPDU       = Utils.hexStringToByteArray("00B0000008")
        val CA2_RAPDU       = Utils.hexStringToByteArray("31143012060A04009000")
        val CA3_CAPDU       = Utils.hexStringToByteArray("00B000080E")
        val CA3_RAPDU       = Utils.hexStringToByteArray("7F000702020402040201020201109000")
    }


    override fun onDeactivated(reason: Int) { }


    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {

        if (commandApdu == null)
            return UNKNOWN_RAPDU

        var response = FAILED_RAPDU
        updateLog("Received APDU: " + Utils.toHex(commandApdu))

        if (Arrays.equals(SELECT_CAPDU, commandApdu)) {
            updateLog( "This is a SELECT_APDU")
            response = OK_RAPDU
        }
        else if (Arrays.equals(CA1_CAPDU, commandApdu)) {
            response = OK_RAPDU
        }
        else if (Arrays.equals(CA2_CAPDU, commandApdu)) {
            response = CA2_RAPDU
        }
        else if (Arrays.equals(CA3_CAPDU, commandApdu)) {
            response = CA3_RAPDU
        }
        else {
            response = UNKNOWN_RAPDU
        }

        updateLog("Sending: " + Utils.toHex(response))
        return response
    }


    fun updateLog(msg: String) {
//        System.out.println(msg)
        Log.i(TAG, msg)
    }

}


//// msg flow
//[ReadBinaryAPDUSender][sendSelectFile] Sending: 00A4020C02011C
//[ReadBinaryAPDUSender][sendSelectFile] Received: 9000
//[ReadBinaryAPDUSender][sendReadBinary] Sending: 00B0000008
//[ReadBinaryAPDUSender][sendReadBinary] Received: 31143012060A04009000
//[ReadBinaryAPDUSender][sendReadBinary] Sending: 00B000080E
//[ReadBinaryAPDUSender][sendReadBinary] Received: 7F000702020402040201020201109000