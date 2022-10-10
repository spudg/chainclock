package com.spudg.chainclock

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.spudg.chainclock.databinding.ActivityMainBinding
import com.spudg.chainclock.databinding.InfoDialogBinding
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var bindingDialog: InfoDialogBinding

    var refreshAllowed = true

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        val view = bindingMain.root
        setContentView(view)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        actionBar?.hide()

        fetchData()
        updateTV()

        if (bindingMain.heightMain.text === "0") {
            bindingMain.heightMain.text = getString(R.string.tap_msg)
        }

        bindingMain.info.setOnClickListener {
            infoDialog()
        }

        bindingMain.heightMain.setOnClickListener {
            if (refreshAllowed) {
                fetchData()
                updateTV()
                Toast.makeText(this, "Refreshed", Toast.LENGTH_SHORT).show()
                refreshAllowed = false
                refreshTimer()
            } else {
                Toast.makeText(this, "You can only refresh every 10 seconds", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            fetchData()
            updateTV()
        }, 60000)
    }

    private fun refreshTimer() {
        object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                refreshAllowed = true
            }
        }.start()
    }

    private fun fetchData() {

        val url = "https://mempool.space/api/blocks/tip/height"

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ERROR", "Failed to get block height.")
            }

            override fun onResponse(call: Call, response: Response) {
                val heightString = response.body()?.string()
                if (heightString != null) {
                    GlobalHeight.height = heightString
                }
            }
        })
    }

    private fun updateTV() {
        bindingMain.heightMain.text = GlobalHeight.height
    }

    private fun infoDialog() {
        val dialog = Dialog(this, R.style.Theme_Dialog)
        dialog.setCancelable(false)
        bindingDialog = InfoDialogBinding.inflate(layoutInflater)
        val view = bindingDialog.root
        dialog.setContentView(view)

        bindingDialog.twitterLink.setOnClickListener {
            val url =
                "https://twitter.com/coldhardbtc"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        bindingDialog.apiLink.setOnClickListener {
            val url =
                "https://mempool.space"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        bindingDialog.submitButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }
}