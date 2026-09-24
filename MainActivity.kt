package com.bmdhaulage.curtainremote

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.InetSocketAddress
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private lateinit var ipInput: EditText
    private lateinit var portInput: EditText
    private lateinit var status: TextView

    // Commands confirmed during testing.
    private val commands = mapOf(
        "Open ON" to "A00101A2",
        "Open OFF" to "A00100A1",
        "Close ON" to "A00201A3",
        "Close OFF" to "A00200A2"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 28)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "BMD HAULAGE\nCURTAIN REMOTE"
            textSize = 26f
            setTextColor(Color.rgb(10, 52, 105))
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 22)
        }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))

        val connectionTitle = TextView(this).apply {
            text = "TCP CONNECTION"
            textSize = 14f
            setTextColor(Color.DKGRAY)
        }
        root.addView(connectionTitle)

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        ipInput = EditText(this).apply {
            hint = "IP address"
            setText("192.168.1.100")
            singleLine = true
        }
        portInput = EditText(this).apply {
            hint = "Port"
            setText("8080")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            singleLine = true
        }
        row.addView(ipInput, LinearLayout.LayoutParams(0, -2, 2f))
        row.addView(portInput, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(row)

        status = TextView(this).apply {
            text = "Ready"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, 12, 0, 18)
        }
        root.addView(status)

        commands.forEach { (label, hex) ->
            val b = Button(this).apply {
                text = "$label\n$hex"
                textSize = 17f
                setAllCaps(false)
                setOnClickListener { sendHex(hex, label) }
            }
            root.addView(
                b,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    76
                ).apply { bottomMargin = 12 }
            )
        }

        val note = TextView(this).apply {
            text = "One-shot TCP commands • Port 8080 • HEX bytes"
            textSize = 12f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 0)
        }
        root.addView(note, LinearLayout.LayoutParams(-1, -2))

        setContentView(root)
    }

    private fun sendHex(hex: String, label: String) {
        val host = ipInput.text.toString().trim()
        val port = portInput.text.toString().toIntOrNull() 

        if (host.isEmpty() || port == null || port !in 1..65535) {
            status.text = "Enter a valid IP and port."
            return
        }

        val bytes = hexToBytes(hex)
        status.text = "Sending $label…"

        Thread {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, port), 2500)
                    socket.getOutputStream().use { out ->
                        out.write(bytes)
                        out.flush()
                    }
                }

                runOnUiThread {
                    status.text = "$label sent"
                    Toast.makeText(this, "$label sent", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    status.text = "Connection failed: ${e.message ?: "unknown error"}"
                    Toast.makeText(this, "TCP connection failed", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.replace(" ", "")
        require(clean.length % 2 == 0)
        return ByteArray(clean.length / 2) { i ->
            clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}
