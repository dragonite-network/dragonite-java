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

**NOTICE: Using command line arguments for configurations is deprecated and inconvenient to save and share them! we recommend using JSON files, please see [JSON section](#json-configuration) below!**

Assume that we have a server **example.com**, we could use command

    ./dragonite-proxy -s -k uMadBro -p 27000 -l 100

to have it listening on UDP port 27000, limiting the maximum speed of each client to 100 Mbps, using encryption key `uMadBro`.

For clients,

    ./dragonite-proxy -a example.com -p 27000 -k uMadBro -d 100 -u 20 -r acl.txt

will connect to **example.com:27000**, telling the server our maximum download speed is 100 Mbps, upload speed is 20 Mbps, using encryption key `uMadBro`, and `acl.txt` as the access control rules.

**[How to write an ACL file](https://github.com/dragonite-network/dragonite-proxy-ACLs)**

**The client will bind to local TCP port 1080 by default, providing a SOCKS5 proxy that supports CONNECT & UDP ASSOCIATE.**

## JSON configuration

You can save the configuration as JSON files and use them like

    ./dragonite-proxy -c Japan1.json

Many fields are optional, just like the arguments above.

### Server JSON configuration

The simplest configuration requires only two fields.

    {
      "server": true,
      "password": "blahblah"
    }

All supported fields are:

    {
      "server": true,
      "addr": "example.com",
      "port": 9299,
      "password": "blahblah",
      "limit": 100,
      "welcome": "GTFO of my server!",
      "loopback": false,
      "mtu": 1300,
      "multiplier": 4,
      "webpanel": true,
      "paneladdr": "127.0.0.1",
      "panelport": 8088
    }

### Client JSON configuration

The simplest configuration requires only 4 fields.

    {
      "addr": "example.com",
      "password": "blahblah",
      "up": 20,
      "down": 100
    }

All supported fields are:

    {
      "server": false,
      "addr": "example.com",
      "port": 9299,
      "socks5port": 1081,
      "password": "blahblah",
      "up": 20,
      "down": 100,
      "acl": "chn.txt",
      "mtu": 1300,
      "multiplier": 4,
      "webpanel": true,
      "paneladdr": "127.0.0.1",
      "panelport": 8088
    }

## About encryption

All traffic is encrypted with `AES-128-CBC` using key derived from `PBKDF2WithHmacSHA1`. The underlying Dragonite socket protocol itself is also encrypted. Wrong passwords will generate invalid packets that can't be decrypted. Invalid packets will be silently discarded, with no error info other than "connection failed".

## Precautions

Dragonite Proxy has a default MTU (maximum transmission unit, sets an upper bound on the size of UDP packets) of 1300. The receiver's buffer size is also based on the value of this option. If you need to modify this value, make sure that the clients and servers have the same MTU value.

The window size multiplier option is like the "aggressiveness" of the sender. If Dragonite Proxy is not fully utilizing the bandwidth, try to increase this value step by step.
