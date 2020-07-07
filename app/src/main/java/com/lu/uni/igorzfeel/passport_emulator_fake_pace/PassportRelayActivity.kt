package com.lu.uni.igorzfeel.passport_emulator_fake_pace

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.nfc.cardemulation.HostApduService
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_passport_relay.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket


class PassportRelayActivity : AppCompatActivity() {

    companion object {
        val TAG: String = "PassportRelayActivity"
        const val MESSAGE_READ = 1
        var server: Server? = null
        var client: Client? = null
        lateinit var sendReceive: SendReceive
        var response = "0000"
    }




    private var mBinder: Messenger? = null

    fun sendResponseApdu(responseApdu: ByteArray?) {
        val responseMsg: Message =
            Message.obtain(null, CardService.MSG_RESPONSE_APDU)
        val dataBundle = Bundle()
        dataBundle.putByteArray(CardService.KEY_DATA, responseApdu)
        responseMsg.data = dataBundle
        try {
            mBinder?.send(responseMsg)
        } catch (e: RemoteException) {
            updateError("Local messenger has died.")
        }
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
            updateLog("ServiceConnection: connected to service.")
            mBinder = Messenger(iBinder)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            updateLog("ServiceConnection: disconnected from service.")
            mBinder = null
        }
    }

    override fun onPause() {
        super.onPause()
        if(mBinder != null) {
            unbindService(serviceConnection)
        }
    }

    override fun onResume() {
        super.onResume()
        startService()
    }


    private fun startService() {
        val intent = Intent(this, CardService::class.java)
        startService(intent)
        bindService()
    }


    private fun bindService() {
        val intent = Intent(this, CardService::class.java)
        bindService(intent,  serviceConnection, Context.BIND_AUTO_CREATE)
    }



    private var status: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passport_relay)

        status = intent.getStringExtra("connectionStatus")
        updateLog(status)

        try {

            if (status == WifiConnectionActivity.CLIENT)
                initializeClient()
            else if (status == WifiConnectionActivity.SERVER)
                initializeServer()

        } catch(e: Exception) {
            updateError(e.toString())
        }


        btnSend.setOnClickListener {
            sendReceive.sendMessage("This is a test message")
        }
    }

    private fun initializeServer() {
        server = Server()
        server!!.start()

        updateLog("Connected as $status")
    }

    private fun initializeClient() {
        client = Client(WifiConnectionActivity.groupOwnerAddress)
        client!!.start()

        updateLog("Connected as $status")
    }


    private fun updateLog(msg: String) {
        Log.i(TAG, msg)
        runOnUiThread {textView.append(msg + "\n") }
    }


    private fun updateError(msg: String) {
        Log.e(TAG, msg)
        runOnUiThread {textView.append("[ERROR] " + msg + "\n") }
    }


    private var handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            MESSAGE_READ -> {
                val buffer = msg.obj as ByteArray
                val msgString = String(buffer, 0, msg.arg1)
                updateLog(msgString)

                sendResponseApdu(Utils.hexStringToByteArray(msgString))
            }
        }
        true
    })


    inner class SendReceive(private val socket: Socket?) : Thread() {

        private lateinit var inputStream: InputStream
        private lateinit var outputStream: OutputStream


        override fun run() {
            updateLog("sendReceive has been initialized and started")
            try {
                inputStream = socket!!.getInputStream()
                outputStream = socket.getOutputStream()

                val buffer = ByteArray(1024)
                var bytes: Int
                while (true) {
                    try {
                        bytes = inputStream.read(buffer)

                        if (bytes == -1) {
                            break;
                        }

                        handler.obtainMessage(WifiConnectionActivity.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget()

                    } catch (e: IOException) {
                        updateError(e.toString())
                    }
                }
            } catch (e: IOException) {
                updateError(e.toString())
            } finally {
                try {
                    socket?.close();
                } catch (e: IOException) {
                    updateError(e.toString())
                }
            }
        }


        fun sendMessage(msg: String) {
            write(msg.toByteArray())
        }


        fun write(bytes: ByteArray?) {
            try {
                outputStream.write(bytes)
            } catch (e: Exception) {
                updateError(e.toString())
            }
        }
    }


    inner class Client(hostAddr: InetAddress) : Thread() {
        val serverPort = 9999
        var socket: Socket = Socket()


        private var hostAddr: String = hostAddr.hostAddress


        override fun run() {
            try {
                socket.bind(null)
                socket.connect(InetSocketAddress(hostAddr, serverPort), 5000)
                sendReceive = SendReceive(socket)
                sendReceive.start()
            } catch (e: IOException) {
                updateError(e.toString())
                try {
                    socket.close()
                } catch (e: IOException) {
                    updateError(e.toString())
                }
                return
            }
        }
    }


    inner class Server : Thread() {
        val serverPort = 9999
        var socket: ServerSocket? = null


        override fun run() {
            openServerSocket()

            while (true) {
                try {
                    sendReceive = SendReceive(socket!!.accept())
                    sendReceive.start()
                } catch (e: IOException) {
                    try {
                        if (socket != null && !socket!!.isClosed)
                            socket!!.close()
                    } catch (ioe: IOException) {
                    }
                    updateError(e.toString())
                    break
                }
            }

        }


        private fun openServerSocket() {
            try {
                socket = ServerSocket(serverPort)
            } catch (e: IOException) {
                updateError(e.toString())
            }
        }
    }
}

