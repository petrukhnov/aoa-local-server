package com.petrukhnov.prototypes.aoa.localaoaserver.usb

data class UsbDeviceDescription(
        //this server vendor name
        val vendorName: String = "petrukhnovVendorName",
        //this server device name/model
        val name: String = "deviceName",
        val description: String = "device description",
        val version: String = "1.0",
        val serial: String = "1234",
        val url: String = "https://konstantin.petrukhnov.com/aoaserver/"
)
