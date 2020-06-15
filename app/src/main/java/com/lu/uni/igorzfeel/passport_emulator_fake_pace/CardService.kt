package com.lu.uni.igorzfeel.passport_emulator_fake_pace

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.util.*

class CardService: HostApduService() {

    companion object {
        val TAG: String  = "CardService"

        val OK_CAPDU        = Utils.hexStringToByteArray("9000")
        val FAILED_CAPDU    = Utils.hexStringToByteArray("6F00")
        val UNKNOWN_CAPDU   = Utils.hexStringToByteArray("0000")
        val SELECT_CAPDU    = Utils.hexStringToByteArray("00A4040C07A0000002471001")
    }


    override fun onDeactivated(reason: Int) { }


    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {

        if (commandApdu == null)
            return UNKNOWN_CAPDU

        var response = FAILED_CAPDU
        updateLog("Received APDU: " + Utils.toHex(commandApdu))

        if (Arrays.equals(SELECT_CAPDU, commandApdu)) {
            updateLog( "This is a SELECT_APDU")
            response = OK_CAPDU
        }
        else {
            response = UNKNOWN_CAPDU
        }

        updateLog("Sending: " + Utils.toHex(response))
        return response
    }


    fun updateLog(msg: String) {
        System.out.println(msg)
        Log.i(TAG, msg)
    }

}