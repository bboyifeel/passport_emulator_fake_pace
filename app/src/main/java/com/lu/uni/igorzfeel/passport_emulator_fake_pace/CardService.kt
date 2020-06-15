package com.lu.uni.igorzfeel.passport_emulator_fake_pace

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.util.*

class CardService: HostApduService() {

    companion object {
        val TAG: String  = "CardService"
    }

    private val OK_CMD = Utils.hexStringToByteArray("9000")
    private val UNKNOWN_CMD = Utils.hexStringToByteArray("0000")


    // ISO-DEP command HEADER for selecting an AID.
    private val SELECT_APDU: ByteArray = Utils.hexStringToByteArray("00A4040C07A0000002471001")

    override fun onDeactivated(reason: Int) { }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {

        if (commandApdu == null)
            return UNKNOWN_CMD

        var response = UNKNOWN_CMD
        updateLog("Received APDU: " + Utils.toHex(commandApdu))

        if (Arrays.equals(SELECT_APDU, commandApdu)) {
            updateLog( "This is a SELECT_APDU")
            response = OK_CMD
        }

        updateLog("Sending: " + Utils.toHex(response))
        return response
    }


    fun updateLog(msg: String) {
        System.out.println(msg)
        Log.i(TAG, msg)
    }

}