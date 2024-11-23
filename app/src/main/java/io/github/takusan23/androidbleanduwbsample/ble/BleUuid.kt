package io.github.takusan23.androidbleanduwbsample.ble

import java.util.UUID

/** BLE で使う UUID */
object BleUuid {

    /** GATT サービスの UUID */
    val GATT_SERVICE_UUID = UUID.fromString("107c9e9b-bf6d-4b64-ab30-0bd96fdd2537")

    /** GATT キャラクタリスティックの UUID */
    val GATT_CHARACTERISTIC_UUID = UUID.fromString("e42ba363-eeaa-4e46-b7aa-049c19341f24")

}