package com.petrukhnov.prototypes.aoa.localaoaserver.usb

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import javax.usb.*
import javax.usb.event.UsbDeviceDataEvent
import javax.usb.event.UsbDeviceErrorEvent
import javax.usb.event.UsbDeviceEvent
import javax.usb.event.UsbDeviceListener
import kotlin.concurrent.thread

@Service
class UsbDetector {
    private val logger = KotlinLogging.logger {}

    private var deviceFound = false
    private lateinit var device: UsbDevice
    private var aoaDevice: UsbDevice? = null

    private lateinit var writePipe: UsbPipe
    private lateinit var writeEndpoint: UsbEndpoint
    private lateinit var readPipe: UsbPipe
    private lateinit var readEndpoint: UsbEndpoint

    // content: vendor/product
    private val excludeList: ArrayList<Pair<Short,Short>> = arrayListOf()

    @PostConstruct
    fun startDeviceSearch() {
        logger.debug { "startDeviceSearch() " }
        thread {
            logger.debug { "thread " }
            while (!deviceFound) {
                deviceSearch()
                Thread.sleep(1000)
            }
        }
    }

    fun connectAoa(device: UsbDevice) {
        logger.debug { "device found, $device . Connecting to AOA... " }
        AoaConnector.sendUsbInformation(device)

        //actual aoa connection
        var retryCount = 0
        while (true) {
            Thread.sleep(1000)
            retryCount++
            val aoaDevice = getAoaConnectedDevice()
            if (aoaDevice != null) {
                aoaDevice.addUsbDeviceListener(object: UsbDeviceListener {
                    override fun dataEventOccurred(event: UsbDeviceDataEvent?) {
                        logger.debug { "data: ${event?.data}" }
                    }

                    override fun usbDeviceDetached(event: UsbDeviceEvent?) {
                        logger.debug { "detached: ${event?.usbDevice}" }
                    }

                    override fun errorEventOccurred(event: UsbDeviceErrorEvent?) {
                        logger.debug { "error: ${event?.usbException}" }
                    }
                })

                //AOA phone configured, stop connecting. All data flow will be handled with event listener specified above.
                break // while
            }
        }

        sendAoaModeToClient()

    }

    fun sendAoaModeToClient() {
        var configuration = aoaDevice!!.activeUsbConfiguration
        var iface = configuration.getUsbInterface(0x2D00.toByte())
        if (iface != null) {
            //aoa mode active
            logger.debug { "aoa mode active" }
        } else {
            var iface = configuration.getUsbInterface(0x2D01.toByte())
            if (iface != null) {
                //aoa-adb mode active
                logger.debug { "aoa-adb mode active" }
            } else {
                //unsupported mode
                logger.debug { "unsupportedMode" }
            }
        }
        if(!iface.isClaimed) {
            iface.claim { true }
        }


        readEndpoint = iface.usbEndpoints.get(1) as UsbEndpoint
        readPipe = readEndpoint.usbPipe
        if (!readPipe.isOpen) {
            readPipe.open()
        }

        //todo listen data coming over AOA
        listenAoaData()

        writeEndpoint = iface.usbEndpoints.get(0) as UsbEndpoint
        writePipe = writeEndpoint.usbPipe
        if (!writePipe.isOpen) {
            writePipe.open()
        }

        //fixme send pingSrv
        logger.debug { "sending pingSrv" }
        writePipe.asyncSubmit("pingSrv".toByteArray())

    }

    fun deviceSearch() {
        logger.debug { "deviceSearch()" }
        val usbHub = UsbHostManager.getUsbServices().rootUsbHub

        var aoaDevice  = getAoaConnectedDevice()
        if (aoaDevice == null) {
            var deviceFound: UsbDevice? = null
            while (deviceFound == null) {
                deviceFound = findDevice(usbHub)
                Thread.sleep(1000)
            }
            device = deviceFound
            connectAoa(device)
        } else {
            //already connected
            logger.debug { "already connected" }
        }
        deviceFound = true

    }

    fun getAoaConnectedDevice(): UsbDevice? {
        logger.debug { "getAoaConnectedDevice()" }
        val usbHub = UsbHostManager.getUsbServices().rootUsbHub
        var aoaDevice  = findDevice(usbHub)

        if (aoaDevice != null) {
            this.aoaDevice = aoaDevice
            return aoaDevice
        }
        return null
    }

    fun findDevice(usbHub: UsbHub): UsbDevice? {
        logger.debug { "findDevice()" }
        var usbdevice: UsbDevice?
        for (device in usbHub.attachedUsbDevices as List<UsbDevice?>) {
            logger.debug { "findDevice() for $device" }
            val descriptor = device!!.usbDeviceDescriptor

            val deviceIds = Pair(descriptor.idVendor(), descriptor.idProduct())
            if(!device.isUsbHub && !excludeList.contains(deviceIds)) {

                try {
                    val manufString = device.getManufacturerString()
                    val prodString = device.getProductString()
                    logger.debug { "manuf: $manufString, prod: $prodString" }

                    if(manufString == null || prodString == null) {
                        excludeList.add(deviceIds)
                    } else {
                        return device
                    }

                } catch (e: UsbPlatformException) {
                    logger.debug { "exception: ${e.errorCode}, exclude device: $deviceIds" }
                    excludeList.add(deviceIds)
                }
            }

            if(device.isUsbHub) {
                usbdevice = findDevice(device as UsbHub)
                if (usbdevice != null) {
                    return usbdevice
                }
            }
        }

        return null
    }

    fun listenAoaData() {
        thread {
            while (true) {
                // 16Kb is optimal size usually
                var readBuffer = ByteArray(16*1024)
                var bytesCount = readPipe.syncSubmit(readBuffer)
                val msgStr = String(readBuffer, 0, bytesCount)

                logger.debug { "msg: $msgStr" }
                //todo send message to listener
            }
        }
    }


}