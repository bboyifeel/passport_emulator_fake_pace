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
        val PACE0_CAPDU     = Utils.hexStringToByteArray("0022C1A412800A04007F00070202040204830102840110")
        val PACE0_RAPDU     = Utils.hexStringToByteArray("9000")
        val PACE1_CAPDU     = Utils.hexStringToByteArray("10860000027C0000")
        val PACE1_RAPDU     = Utils.hexStringToByteArray("7C228020C6E82CE4A0F80BAB907A95E573090DAA94BF4227FAB66DD4C46E6154E28992659000")
        val PACE2_CAPDU     = Utils.hexStringToByteArray("10860000657C63816104")
        val PACE2_RAPDU     = Utils.hexStringToByteArray("7C638261046D756A258E35BE0A4ED2C03FAA6EC4C814D6B35922B94B1DD755A4F86E50B3DA3B7F3BACD7B64B7312F537335E3089D6260248E2D21B45E1D143441FA6B94500F10CAAE948C002C68DDE4545236E2B15349C50DD1BAC40B0FC72BECE3277A1889000")
        val PACE3_CAPDU     = Utils.hexStringToByteArray("10860000657C63836104")
        val PACE3_RAPDU     = Utils.hexStringToByteArray("7C6384610407DCE7549D3057CDEED8FF8E46B0C286768D79AE949B2D49AC7B55981F3D5711E693BBCD0C6EC65DA3716DEA394AFA323854691E3EC50E2D514B108C6057D13F6FD916765889B6D534A916AB943719B3956E7A66FE7EC6A350FAC5B663F5F2569000")
        val PACE4_CAPDU     = Utils.hexStringToByteArray("008600000C7C0A8508")
        val PACE4_RAPDU     = Utils.hexStringToByteArray("7C0A860859088653F6DC213F9000")
    }

    override fun onDeactivated(reason: Int) { }


    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {

        if (commandApdu == null)
            return UNKNOWN_RAPDU

        var response = UNKNOWN_RAPDU
        updateLog("Received APDU: " + Utils.toHex(commandApdu))

        if (Arrays.equals(SELECT_CAPDU, commandApdu)) {
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
        else if (Arrays.equals(PACE0_CAPDU, commandApdu)) {
            response = PACE0_RAPDU
        }
        else if (Arrays.equals(PACE1_CAPDU, commandApdu)) {
            response = PACE1_RAPDU
        }

        if (response != UNKNOWN_RAPDU) {
            return response
        }

        val first10Bytes = commandApdu.take(10).toByteArray()
        updateLog("First 10 bytes: " + Utils.toHex(first10Bytes))

        if (Arrays.equals(PACE2_CAPDU, first10Bytes )) {
            response = PACE2_RAPDU
        }
        else if (Arrays.equals(PACE3_CAPDU, commandApdu.take(10).toByteArray())) {
            response = PACE3_RAPDU
        }
        else if (Arrays.equals(PACE4_CAPDU, first10Bytes.take(9).toByteArray())) {
            response = PACE4_RAPDU
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
//
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
