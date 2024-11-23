package io.github.takusan23.androidbleanduwbsample.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** BLE ペリフェラル側のコード */
object BlePeripheral {

    /**
     * ペリフェラル側に必要な GATT サーバーとアドバタイジングを開始する。
     * コルーチンをキャンセルすると終了する。
     *
     * @param context [Context]
     * @param onCharacteristicReadRequest セントラルからキャラクタリスティックに対して read 要求された時
     * @param onCharacteristicWriteRequest セントラルからキャラクタリスティックに対して write 要求された時
     */
    suspend fun startPeripheralAndAdvertising(
        context: Context,
        onCharacteristicReadRequest: () -> ByteArray,
        onCharacteristicWriteRequest: (ByteArray) -> Unit
    ) {
        coroutineScope {
            launch {
                suspendGattServer(context, onCharacteristicReadRequest, onCharacteristicWriteRequest)
            }
            launch {
                suspendAdvertisement(context)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun suspendGattServer(
        context: Context,
        onCharacteristicReadRequest: () -> ByteArray,
        onCharacteristicWriteRequest: (ByteArray) -> Unit
    ) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        var bleGattServer: BluetoothGattServer? = null
        bleGattServer = bluetoothManager.openGattServer(context, object : BluetoothGattServerCallback() {
            // readCharacteristic が要求されたら呼ばれる
            // セントラルへ送信する
            override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
                val sendByteArray = onCharacteristicReadRequest()
                // オフセットを考慮する
                // TODO バイト数スキップするのが面倒で ByteArrayInputStream 使ってるけど多分オーバースペック
                val sendOffsetByteArray = sendByteArray.inputStream().apply { skip(offset.toLong()) }.readBytes()
                bleGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, sendOffsetByteArray)
            }

            // writeCharacteristic が要求されたら呼ばれる
            // セントラルから受信する
            override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
                value ?: return
                onCharacteristicWriteRequest(value)
                bleGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
            }
        })

        //サービスとキャラクタリスティックを作る
        val gattService = BluetoothGattService(BleUuid.GATT_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val gattCharacteristics = BluetoothGattCharacteristic(
            BleUuid.GATT_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        // サービスに Characteristic を入れる
        gattService.addCharacteristic(gattCharacteristics)
        // GATT サーバーにサービスを追加
        bleGattServer?.addService(gattService)

        // キャンセルしたら終了
        try {
            awaitCancellation()
        } finally {
            bleGattServer?.close()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun suspendAdvertisement(context: Context) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothLeAdvertiser = bluetoothManager.adapter.bluetoothLeAdvertiser

        // アドバタイジング。これがないと見つけてもらえない
        val advertiseSettings = AdvertiseSettings.Builder().apply {
            setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            setTimeout(0)
        }.build()
        val advertiseData = AdvertiseData.Builder().apply {
            addServiceUuid(ParcelUuid(BleUuid.GATT_SERVICE_UUID))
        }.build()
        // アドバタイジング開始
        val advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)
            }

            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
            }
        }
        bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)

        // キャンセルしたら終了
        try {
            awaitCancellation()
        } finally {
            bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
        }
    }

}