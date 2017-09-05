# dragonite-proxy

![powered by dragonite](https://img.shields.io/badge/powered%20by-dragonite-yellow.svg)

Advanced SOCKS5 proxy featuring encryption, traffic obfuscation and a powerful ACL system.

    usage: dragonite-proxy
     -a,--address <address>                     Remote server address for
                                                client / Bind address for
                                                server
        --allow-loopback                        Allow clients to access the
                                                local loopback address of
                                                server
     -c,--config <path>                         JSON config file
     -d,--download-mbps <mbps>                  Download Mbps for client
        --debug                                 Set the logging level to DEBUG
     -h,--help                                  Help message
     -k,--password <xxx>                        Encryption password for both
                                                client and server
     -l,--limit-mbps <mbps>                     Max Mbps per client for server
     -m,--mtu <size>                            MTU of underlying Dragonite
                                                sockets
        --obfs                                  Enable XBC Obfuscator of
                                                underlying Dragonite sockets
                                                for both client and server
     -p,--port <port>                           Remote server port for client
                                                / Bind port for server
     -r,--acl <path>                            ACL file for client
     -s,--server-mode                           Enable server mode
     -u,--upload-mbps <mbps>                    Upload Mbps for client
     -w,--welcome <msg>                         Welcome message of server
        --web-panel                             Enable Web Panel of underlying
                                                Dragonite sockets (Bind to
                                                loopback interface)
        --web-panel-public                      Enable Web Panel of underlying
                                                Dragonite sockets (Bind to all
                                                interfaces)
        --window-size-multiplier <multiplier>   Send window size multiplier of
                                                underlying Dragonite sockets
                                                (1-10)
     -x,--socks5-port <port>                    Local SOCKS5 proxy port for
                                                client

## Sample configuration

Assume that we have a server **example.com**, we could use command

    ./dragonite-proxy -s -k uMadBro -p 27000 -l 100 --obfs

to have it listening on UDP port 27000, limiting the maximum speed of each client to 100 Mbps, using encryption key `uMadBro`, with traffic obfuscation turned on.

For clients,

    ./dragonite-proxy -a example.com -p 27000 -k uMadBro -d 100 -u 20 --obfs -r acl.txt

will connect to **example.com:27000**, telling the server our maximum download speed is 100 Mbps, upload speed is 20 Mbps, using encryption key `uMadBro`, with traffic obfuscation turned on, and using `acl.txt` as the access control rules.

[How to write an ACL file](ACL.md)

**The client will bind to local TCP port 1080 by default, providing a SOCKS5 proxy that supports CONNECT & UDP ASSOCIATE.**

## JSON configuration

You can also save the configuration as JSON files and use them like

    ./dragonite-proxy -c Japan1.json

Many fields are optional, just like the arguments above.

### Server JSON configuration

    {
      "server": true,
      "addr": "example.com",
      "port": 9299,                               //OPTIONAL
      "password": "blahblah",
      "limit": 100,                               //OPTIONAL
      "welcome": "GTFO of my server!",            //OPTIONAL
      "loopback": false,                          //OPTIONAL
      "mtu": 1300,                                //OPTIONAL
      "multiplier": 4,                            //OPTIONAL
      "webpanel": true,                           //OPTIONAL
      "paneladdr": "127.0.0.1",                   //OPTIONAL
      "panelport": 8088,                          //OPTIONAL
      "obfs": true                                //OPTIONAL
    }

### Client JSON configuration

    {
      "server": false,                            //OPTIONAL
      "addr": "example.com",
      "port": 9299,                               //OPTIONAL
      "socks5port": 1081,                         //OPTIONAL
      "password": "blahblah",
      "up": 20,
      "down": 100,
      "acl": "chn.txt",                           //OPTIONAL
      "mtu": 1300,                                //OPTIONAL
      "multiplier": 4,                            //OPTIONAL
      "webpanel": true,                           //OPTIONAL
      "paneladdr": "127.0.0.1",                   //OPTIONAL
      "panelport": 8088,                          //OPTIONAL
      "obfs": true                                //OPTIONAL
    }

## About encryption

All proxy traffic is encrypted with `AES-128-CFB8` using key derived from `PBKDF2WithHmacSHA1`. The Dragonite socket protocol itself is not encrypted by default, but if you want to avoid any potential DPI detection, the `--obfs` option can be used to enable the CRXObfuscator.

## Precautions

Dragonite Proxy has a default MTU (maximum transmission unit, sets an upper bound on the size of UDP packets) of 1300. The receiver's buffer size is also based on the value of this option. If you need to modify this value, make sure that the clients and servers have the same MTU value.

The window size multiplier option is like the "aggressiveness" of the sender. If Dragonite Proxy is not fully utilizing the bandwidth, try to increase this value step by step.
