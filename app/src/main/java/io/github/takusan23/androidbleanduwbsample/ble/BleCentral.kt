package io.github.takusan23.androidbleanduwbsample.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** BLE セントラル側のコード */
class BleCentral(private val context: Context) {

    /** [readCharacteristic]等で使いたいので */
    private val _bluetoothGatt = MutableStateFlow<BluetoothGatt?>(null)

    /** コールバックの返り値をコルーチン側から受け取りたいので */
    private val _characteristicReadChannel = Channel<ByteArray>()

    /** BLE 通信をし、GATT サーバーへ接続しサービスを探す */
    @SuppressLint("MissingPermission")
    suspend fun connectGattServer() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        // BluetoothDevice が見つかるまで一時停止
        val bluetoothDevice: BluetoothDevice? = suspendCoroutine { continuation ->
            val bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner
            val bleScanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    // 見つけたら返して、スキャンも終了させる
                    continuation.resume(result?.device)
                    bluetoothLeScanner.stopScan(this)
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    continuation.resume(null)
                }
            }

            // GATT サーバーのサービス UUID を指定して検索を始める
            val scanFilter = ScanFilter.Builder().apply {
                setServiceUuid(ParcelUuid(BleUuid.GATT_SERVICE_UUID))
            }.build()
            bluetoothLeScanner.startScan(
                listOf(scanFilter),
                ScanSettings.Builder().build(),
                bleScanCallback
            )
        }

        // BLE デバイスを見つけたら、GATT サーバーへ接続
        bluetoothDevice?.connectGatt(context, false, object : BluetoothGattCallback() {

            // ペリフェラル側との接続
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                when (newState) {
                    // 接続できたらサービスを探す
                    BluetoothProfile.STATE_CONNECTED -> gatt?.discoverServices()
                    // なくなった
                    BluetoothProfile.STATE_DISCONNECTED -> _bluetoothGatt.value = null
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                // サービスが見つかったら GATT サーバーに対して操作ができるはず
                // サービスとキャラクタリスティックを探して、read する
                // キャラクタリスティック操作ができたら flow に入れる
                _bluetoothGatt.value = gatt
            }

            // onCharacteristicReadRequest で送られてきたデータを受け取る
            override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
                super.onCharacteristicRead(gatt, characteristic, value, status)
                _characteristicReadChannel.trySend(value)
            }
        })

        // GATT サーバーへ接続できるまで一時停止する
        _bluetoothGatt.first { it != null }
    }

    /** 終了時に呼ぶ */
    @SuppressLint("MissingPermission")
    fun destroy() {
        _bluetoothGatt.value?.close()
        _bluetoothGatt.value = null
    }

    /** キャラクタリスティックから読み出す */
    @SuppressLint("MissingPermission")
    suspend fun readCharacteristic(): ByteArray {
        // GATT サーバーとの接続を待つ
        val gatt = _bluetoothGatt.filterNotNull().first()
        // GATT サーバーへ狙ったサービス内にあるキャラクタリスティックへ read を試みる
        val findService = gatt.services?.first { it.uuid == BleUuid.GATT_SERVICE_UUID }
        val findCharacteristic = findService?.characteristics?.first { it.uuid == BleUuid.GATT_CHARACTERISTIC_UUID }
        // 結果は onCharacteristicRead で
        gatt.readCharacteristic(findCharacteristic)
        return _characteristicReadChannel.receive()
    }

    /** キャラクタリスティックへ書き込む */
    @SuppressLint("MissingPermission")
    suspend fun writeCharacteristic(sendData: ByteArray) {
        // GATT サーバーとの接続を待つ
        val gatt = _bluetoothGatt.filterNotNull().first()
        // GATT サーバーへ狙ったサービス内にあるキャラクタリスティックへ write を試みる
        val findService = gatt.services?.first { it.uuid == BleUuid.GATT_SERVICE_UUID } ?: return
        val findCharacteristic = findService.characteristics?.first { it.uuid == BleUuid.GATT_CHARACTERISTIC_UUID } ?: return
        // 結果は onCharacteristicWriteRequest で
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(findCharacteristic, sendData, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        } else {
            // TODO 下位バージョン対応するなら。UWB 対応デバイスが、TIRAMISU より前に存在するかを考えるとめんどい
        }
    }

}