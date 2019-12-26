package com.jax.uevent

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jax.uevent.ui.main.MainFragment
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        requestPermission()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }

    private fun requestPermission() {
        if (!checkPermissions(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION)
        }
    }

    private fun checkPermissions(array: Array<String>) = array.all {
        ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            permissions.zip(grantResults.toTypedArray()).map {
                Timber.d("onRequestPermissionsResult date: 2019-12-25 $it")
            }
        }
    }

    companion object {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        const val REQUEST_CODE_PERMISSION = 0x01
    }
}
