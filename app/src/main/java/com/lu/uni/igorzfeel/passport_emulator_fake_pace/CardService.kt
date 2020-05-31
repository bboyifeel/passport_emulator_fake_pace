package com.lu.uni.igorzfeel.passport_emulator_fake_pace

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.util.*

class CardService: HostApduService() {

    companion object {
        val TAG: String  = "CardService"
    }

    private val OK_CMD = HexStringToByteArray("9000")
    private val UNKNOWN_CMD = HexStringToByteArray("0000")


    // ISO-DEP command HEADER for selecting an AID.
    private val SELECT_APDU: ByteArray = HexStringToByteArray("00A4040C07A0000002471001")

    override fun onDeactivated(reason: Int) { }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {

        if (commandApdu == null)
            return UNKNOWN_CMD

        var response = UNKNOWN_CMD
        val recApduStr: String = ByteArrayToHexString(commandApdu)
        Log.i(TAG, "Received APDU: $recApduStr")

        if (Arrays.equals(SELECT_APDU, commandApdu)) {
            Log.i(TAG, "Select apdu came around")
            response = OK_CMD
        }

        return response
    }

    fun ByteArrayToHexString(bytes: ByteArray): String {
        val hexArray = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
        )
        val hexChars =
            CharArray(bytes.size * 2) // Each byte has two hex characters (nibbles)
        var v: Int
        for (j in bytes.indices) {
            v = bytes[j].toInt()  and 0xFF // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v ushr 4] // Select hex character from upper nibble
            hexChars[j * 2 + 1] =
                hexArray[v and 0x0F] // Select hex character from lower nibble
        }
        return String(hexChars)
    }

    @Throws(IllegalArgumentException::class)
    fun HexStringToByteArray(s: String): ByteArray {
        val len = s.length
        require(len % 2 != 1) { "Hex string must have even number of characters" }
        val data =
            ByteArray(len / 2) // Allocate 1 byte per 2 hex characters
        var i = 0
        while (i < len) {

            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                    + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

}