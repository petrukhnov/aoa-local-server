# aoa-local-server

sample of local/aoa server

## Related projects

https://github.com/petrukhnov/aoa-local-server


## How AOA works


AOA replace TCP/IP part of OSI stack. It provides 2 byte buffers (rx/tx).

Connection sequence (simplified):

1. Server scan for USB connections (ignoring hubs and unknown devises).
2. USB wire connection established.
3. Server detects devise with name, and name is matching: petrukhnovVendorName / deviceName
4. Server send request to initialize AOA with values from UsbDeviceDescription
5. Android device receive request. If app not installed/registered, it shows provided information.
6. As app already installed and could handle AOA device (PC with running this server) it continue handshake.
7. When AOA connection established server sends "pingSrv" and android device respond with "pongAndroid" which could be seen in the logs.

## How to try at home

1. Download both projects (+ AndroidStudio, Idea, Java, etc)
2. Start server, connect phone via usb
3. Observe message on phone that devise is not supported
4. Stop server
5. Install app on the phone, start it
6. disconnect USB cable
7. Start server locally
8. Connect USB
9. observe ping/pong (onece) in logs

