# dragonite-forwarder

![powered by dragonite](https://img.shields.io/badge/powered%20by-dragonite-yellow.svg)

一个可以用来加速任何TCP连接的双向转发加速工具。

![Diagram](diagram.png)

    usage: dragonite-forwarder
     -a,--address <address>                     Remote server address for
                                                client / Bind address for
                                                server
     -d,--download-mbps <mbps>                  Download Mbps for client
        --debug                                 Set the logging level to DEBUG
     -f,--forwarding-port <port>                Local port for client /
                                                Forwarding port for server
     -h,--help                                  Help message
     -l,--limit-mbps <mbps>                     Max Mbps per client for server
     -m,--mtu <size>                            MTU of underlying Dragonite
                                                sockets
     -p,--port <port>                           Remote server port for client
                                                / Bind port for server
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

## 配置样例

假如你已经有一个 **开在8080端口的代理服务器**，使用命令

    ./dragonite-forwarder -s -f 8080

对于客户端，命令

    ./dragonite-forwarder -a example.com -f 8080 -d 100 -u 10

会连接到 **example.com** 上的 forwarder，告诉服务器你网络目前是 100Mbps 下行，10Mbps 上行。

如果一切正常，**你现在客户端的本地8080端口就转发到服务端的8080端口了**。

让你的程序连接到代理服务器 **localhost:8080** 即可使用通过 forwarder 加速后的代理连接。

其他高级参数请见上面的参数表。

## 注意事项

Dragonite Forwarder 默认的 MTU (即每个 UDP 包的最大大小) 是 1300。如果你需要进行修改（请先确认你了解这是什么），请保证客户端与服务端的数值一致。

window size multiplier 选项类似于发送方的“激进程度”。如果 Forwarder 由于自动 window 大小过小限制了速度，可以尝试逐步提高这个倍数。