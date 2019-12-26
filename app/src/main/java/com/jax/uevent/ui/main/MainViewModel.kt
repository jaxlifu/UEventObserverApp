package com.jax.uevent.ui.main

import android.os.UEventObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jax.uevent.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class MainViewModel : ViewModel() {

    private val _switchOneStatus = MutableLiveData("off")
    private val _switchTwoStatus = MutableLiveData("off")
    private val _gpioSwitch = MutableLiveData("off")
    private val _uEventMessage = MutableLiveData("")
    val switchOneStatus: LiveData<String> = _switchOneStatus
    val switchTwoStatus: LiveData<String> = _switchTwoStatus
    val gpioSwitch: LiveData<String> = _gpioSwitch
    val uEventMessage: LiveData<String> = _uEventMessage


    fun startObserverUEvent() {
        uEventObserver.startObserving(DISK_MATCH)
        uEventObserver.startObserving(GPIO_MATCH)
    }

    private val uEventObserver = object : UEventObserver() {
        override fun onUEvent(uEvnet: UEvent?) {
            if (uEvnet == null) return

            val name: String? = uEvnet.get(GPIO_NAME)
            val status: String? =
                uEvnet.get(GPIO_STATUS) ?: name?.split("${GPIO_STATUS}=")?.lastOrNull()
            Timber.d("onUEvent date: 2019-12-25 name: $name status: $status")
            viewModelScope.launch(Dispatchers.Main) {
                _uEventMessage.value = uEvnet.toString()
                if (name != null) {
                    updateSwitchStatus(name, status.toString())
                }
            }
        }
    }

    private fun updateSwitchStatus(name: String, status: String) {
        when {
            name.contains(SWITCH_ONE) -> {
                _switchOneStatus.value = status
            }
            name.contains(SWITCH_TWO) -> {
                _switchTwoStatus.value = status
            }
        }
    }


    fun onGpioClick() {
        FileUtils.write2File(File(GPIO_PATH), if (getGpioValue() == "1") "0" else "1")
        viewModelScope.launch(Dispatchers.Main) {
            delay(300L)
            _gpioSwitch.value = if (getGpioValue() == "1") "on" else "off"
        }
    }

    private fun getGpioValue() = FileUtils.readFromFile(File(GPIO_PATH)).toString()

    override fun onCleared() {
        super.onCleared()
        uEventObserver.stopObserving()

    }

    //remove@/devices/platform/fe320000.dwmmc/mmc_host/mmc0/mmc0:1234/block/mmcblk0 ACTION=remove DEVPATH=/devices/platform/fe320000.dwmmc/mmc_host/mmc0/mmc0:1234/block/mmcblk0 SUBSYSTEM=block MAJOR=179 MINOR=0 DEVNAME=mmcblk0 DEVTYPE=disk NPARTS=0 SEQNUM=2722
    //{SUBSYSTEM=block, MAJOR=179, NPARTS=0, SEQNUM=2762, ACTION=remove, DEVNAME=mmcblk0, DEVTYPE=disk, MINOR=0, DEVPATH=/devices/platform/fe320000.dwmmc/mmc_host/mmc0/mmc0:1234/block/mmcblk0}
    companion object {
        const val GPIO_PATH = "/sys/class/gpio/gpio119/value"
        const val GPIO_NAME = "GPIO_NAME"
        const val GPIO_STATUS = "GPIO_STATE"
        const val SWITCH_ONE = "gpio3-c4"
        const val SWITCH_TWO = "gpio3-c5"
        const val GPIO_MATCH = "DEVPATH=/devices/virtual/gpio-detection"
        const val DISK_MATCH = "DEVTYPE=disk"
    }

}
