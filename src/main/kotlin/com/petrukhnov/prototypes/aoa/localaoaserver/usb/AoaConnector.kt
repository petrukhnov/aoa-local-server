package com.petrukhnov.prototypes.aoa.localaoaserver.usb

import javax.usb.UsbConst
import javax.usb.UsbDevice
import kotlin.experimental.or

object AoaConnector {

    private val defaultDeviceDescription = UsbDeviceDescription()

    fun sendUsbInformation(device: UsbDevice) {

        //this usb device identification

        var irp = device.createUsbControlIrp((UsbConst.REQUESTTYPE_DIRECTION_IN or UsbConst.REQUESTTYPE_TYPE_VENDOR), 51, 0 ,0)
        irp.data = byteArrayOf(0,0)
        device.syncSubmit(irp)

        irp = device.createUsbControlIrp((UsbConst.REQUESTTYPE_DIRECTION_OUT or UsbConst.REQUESTTYPE_TYPE_VENDOR), 52, 0, 0)
        irp.data = defaultDeviceDescription.vendorName.toByteArray()
        device.syncSubmit(irp)

        irp = device.createUsbControlIrp((UsbConst.REQUESTTYPE_DIRECTION_OUT or UsbConst.REQUESTTYPE_TYPE_VENDOR), 52, 0, 1)
        irp.data = defaultDeviceDescription.name.toByteArray()
        device.syncSubmit(irp)

        irp = device.createUsbControlIrp((UsbConst.REQUESTTYPE_DIRECTION_OUT or UsbConst.REQUESTTYPE_TYPE_VENDOR), 52, 0, 2)
        irp.data = defaultDeviceDescription.description.toByteArray()
        device.syncSubmit(irp)

        irp = device.createUsbControlIrp((UsbConst.REQUESTTYPE_DIRECTION_OUT or UsbConst.REQUESTTYPE_TYPE_VENDOR), 52, 0, 3)
        irp.data = defaultDeviceDescription.version.toByteArray()
        device.syncSubmit(irp)

        irp = device.createUsbControlIrp((UsbConst.REQUESTTYPE_DIRECTION_OUT or UsbConst.REQUESTTYPE_TYPE_VENDOR), 52, 0, 4)
        irp.data = defaultDeviceDescription.url.toByteArray()
        device.syncSubmit(irp)

        //serial?
        irp = device.createUsbControlIrp((UsbConst.REQUESTTYPE_DIRECTION_OUT or UsbConst.REQUESTTYPE_TYPE_VENDOR), 52, 0, 5)
        irp.data = defaultDeviceDescription.serial.toByteArray()
        device.syncSubmit(irp)

        irp = device.createUsbControlIrp((UsbConst.REQUESTTYPE_DIRECTION_OUT or UsbConst.REQUESTTYPE_TYPE_VENDOR), 53, 0, 0)
        irp.data = ByteArray(0)
        device.syncSubmit(irp)


    }
}