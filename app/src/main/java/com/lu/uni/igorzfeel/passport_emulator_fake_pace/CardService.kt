package com.lu.uni.igorzfeel.passport_emulator_relay

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.lang.reflect.GenericArrayType
import java.util.*


class CardService: HostApduService() {

    companion object {
        val TAG: String  = "CardService"

        val OK_RAPDU        = Utils.hexStringToByteArray("9000")
        val UNKNOWN_RAPDU   = Utils.hexStringToByteArray("0000")
        val SELECT_CAPDU    = Utils.hexStringToByteArray("00A4040C07A0000002471001")
        val CA1_CAPDU       = Utils.hexStringToByteArray("00A4020C02011C")


        val GET_CHALLENGE               = Utils.hexStringToByteArray("00840000")
        val EXTERNAL_AUTHENTICATE     = Utils.hexStringToByteArray("00820000")

        val SELECT_APPLET_SUCCESS = Utils.hexStringToByteArray("0BE95354D509E58A9000")

        val KEY_DATA = "data"
        val MSG_RESPONSE_APDU = 1
        // 1 - PACE, 2 - BAC
        var PROTOCOL = 1
    }




    override fun onDeactivated(reason: Int) { }


    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray? {
        if (PROTOCOL == 1) {
            return relayPACE(commandApdu)
        }

        return relayBAC(commandApdu)
    }

    private fun relayPACE(commandApdu: ByteArray?): ByteArray? {
        if (commandApdu == null)
            return UNKNOWN_RAPDU

        var response = UNKNOWN_RAPDU
        updateLog("capdu: " + Utils.toHex(commandApdu))

        if (Arrays.equals(SELECT_CAPDU, commandApdu) || Arrays.equals(CA1_CAPDU, commandApdu)) {
            response = OK_RAPDU
        } else {
            updateLog("[->]")
            PassportRelayActivity.sendReceive.sendMessage(Utils.toHex(commandApdu))
            return null
        }

        updateLog("rapdu: " + Utils.toHex(response))
        return response
    }


    private fun relayBAC(commandApdu: ByteArray?): ByteArray? {
        updateLog("HANDLING with bac")

        if (commandApdu == null)
            return UNKNOWN_RAPDU

        var response = UNKNOWN_RAPDU
        updateLog("capdu: " + Utils.toHex(commandApdu))

        if (Arrays.equals(SELECT_CAPDU, commandApdu)) {
            response = OK_RAPDU
        } else if (Arrays.equals(GET_CHALLENGE, commandApdu.take(4).toByteArray())
            || Arrays.equals(EXTERNAL_AUTHENTICATE, commandApdu.take(4).toByteArray())
            || Arrays.equals(SELECT_APPLET_SUCCESS, commandApdu)) {

            updateLog("[->]")
            PassportRelayActivity.sendReceive.sendMessage(Utils.toHex(commandApdu))
            return null
        } else {
            response = UNKNOWN_RAPDU
        }

        updateLog("rapdu: " + Utils.toHex(response))
        return response
    }


    fun updateLog(msg: String) {
        Log.i(TAG, msg)
    }


    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }



}


//// PACE msg flow
//
//[ReadBinaryAPDUSender][sendSelectFile] Sending: 00A4020C02011C
//[ReadBinaryAPDUSender][sendSelectFile] Received: 9000
//
//
//[ReadBinaryAPDUSender][sendReadBinary] Sending: 00B0000008
//[ReadBinaryAPDUSender][sendReadBinary] Received: 31143012060A04009000
//
//[ReadBinaryAPDUSender][sendReadBinary] Sending: 00B000080E
//[ReadBinaryAPDUSender][sendReadBinary] Received: 7F000702020402040201020201109000
//
//
//[PACEAPDUSender][sendMSESetATMutualAuth] Sending: 0022C1A412800A04007F00070202040204830102840110
//[PACEAPDUSender][sendMSESetATMutualAuth] Respond: 9000
//
//[PACEAPDUSender][sendMSESetATMutualAuth] Sending: 10860000027C0000
//[PACEAPDUSender][sendMSESetATMutualAuth] Respond: 7C228020.C6E82CE4A0F80BAB907A95E573090DAA94BF4227FAB66DD4C46E6154E2899265.9000
//
//[PACEProtocol][doPACEStep1] The nonce is: CCBC7821ACCAF0DD456C943B8F051AD140C7398A84C39CAF381672D055A8965D
//                                                  10860000657C63816104.417C5A685B16535F31292B67D8A336189967C4EE57C771F7A4102405ABA61D99584CBA5890EC7B65983740E688754AC235ED7EC79BA33FEC61B412BC6C3C2B8C79E2426A9DAA1CB977728A06C57E5DEA58383840EA4B235D264F4590923167D000
//[PACEAPDUSender][sendMSESetATMutualAuth] Sending: 10860000657C63816104.14328F0AD433938C4220799E1EE8610DEE735E6A7B4D484C0A8AEBB3994841F4ABE7EA822D5F3C765184400771A534CF6EB616F1AC51381D0F00E335E97D827409A78BCD083D95A7B2D4CB9CFA3B501787820E5D3D0766E90891B03F10BFE6.D700
//[PACEAPDUSender][sendMSESetATMutualAuth] Respond: 7C63826104.6D756A258E35BE0A4ED2C03FAA6EC4C814D6B35922B94B1DD755A4F86E50B3DA3B7F3BACD7B64B7312F537335E3089D6260248E2D21B45E1D143441FA6B94500F10CAAE948C002C68DDE4545236E2B15349C50DD1BAC40B0FC72BECE3277A188.9000
//
//
//[PACEAPDUSender][sendMSESetATMutualAuth] Sending: 10860000657C63836104.2838BF25565D3B8D92F6F7F27B2EFF14BD9E4B405F59B09D8A8B0CB7C3BDB171A156F272475736E345961E33972BB04B1A9517105C9F8F7436B6BA7AFFDA6CED7531D403968818193CE92A6AF01068432E7BB0DC755731DEBEF01294058EE78900
//[PACEAPDUSender][sendMSESetATMutualAuth] Respond: 7C63846104.07DCE7549D3057CDEED8FF8E46B0C286768D79AE949B2D49AC7B55981F3D5711E693BBCD0C6EC65DA3716DEA394AFA323854691E3EC50E2D514B108C6057D13F6FD916765889B6D534A916AB943719B3956E7A66FE7EC6A350FAC5B663F5F256.9000
//
//[PACEAPDUSender][sendMSESetATMutualAuth] Sending: 008600000C7C0A8508.15AF50DE1BAE174300
//[PACEAPDUSender][sendMSESetATMutualAuth] Respond: 7C0A8608.59088653F6DC213F.9000
//
//if failed the last respond is 6300


//reader side first capdu should be:

//var command: ByteArray = HexStringToByteArray("00A4040C07A0000002471001")
//updateLog("Sending " + "00A4040C07A0000002471001")
//var result = isoDep.transceive(command)
//updateLog(ByteArrayToHexString(result))
