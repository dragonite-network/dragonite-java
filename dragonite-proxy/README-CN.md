# dragonite-proxy

![powered by dragonite](https://img.shields.io/badge/powered%20by-dragonite-yellow.svg)

一个基于 dragonite 协议，带有强大规则系统的加密SOCKS5代理。

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

## 配置样例

**注意: 不推荐使用命令行参数作为配置，不方便保存与分享！请查看 [JSON 方式](#json) below!**

假设你有一台服务器 **example.com**，可以使用如下命令

    ./dragonite-proxy -s -k uMadBro -p 27000 -l 100

将服务端开在 UDP 27000 端口，限制每个客户端最大速度为 100Mbps，用密码 `uMadBro` 加密。

对于客户端，

    ./dragonite-proxy -a example.com -p 27000 -k uMadBro -d 100 -u 20 -r acl.txt

会连接到 **example.com:27000**，设置本地网络连接速度为 100Mbps 下行，20Mbps 上行。使用密码 `uMadBro`，加载分流规则文件 `acl.txt`

**[如何编写规则文件](https://github.com/dragonite-network/dragonite-proxy-ACLs)**

**客户端会默认在1080端口提供一个完整的支持TCP与UDP转发的SOCKS5代理**

## JSON

你可以用 JSON 配置文件代替参数，

    ./dragonite-proxy -c Japan1.json

许多字段是可选的，详情见下面

### 服务端 JSON 配置

最简单的服务端配置只需要两个字段：

    {
      "server": true,
      "password": "blahblah"
    }

所有支持的字段：

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

### 客户端 JSON 配置

最简单的客户端配置只需要四个字段：

    {
      "addr": "example.com",
      "password": "blahblah",
      "up": 20,
      "down": 100
    }

所有支持的字段：

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

## 关于加密

所有流量都用 `AES-128-CBC` 加密，密钥从 `PBKDF2WithHmacSHA1` 生成。协议本身也进行了加密，因此错误的密码会生成无法加密的无效数据包，被静默丢弃。因此密码错误通常只能看到类似“无法连接”的错误提示。

## Precautions

## 注意事项

Dragonite Proxy 默认的 MTU (即每个 UDP 包的最大大小) 是 1300。如果你需要进行修改（请先确认你了解这是什么），请保证客户端与服务端的数值一致。

window size multiplier 选项类似于发送方的“激进程度”。如果 Proxy 由于自动 window 大小过小限制了速度，可以尝试逐步提高这个倍数。
