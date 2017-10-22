# ![dragonite-java](assets/TextLogo.png)

    INTERNET UNLEASHED.

![powered by dragonite](https://img.shields.io/badge/powered%20by-dragonite-yellow.svg)

[![https://t.me/DragoniteProject](https://img.shields.io/badge/Telegram%20Group%20CN-https%3A%2F%2Ft.me%2FDragoniteProject-blue.svg)](https://t.me/DragoniteProject)

中文用户请加入 [Telegram 群组](https://t.me/DragoniteProject)

Dragonite 是一个我自己开发了接近一年的一系列项目。

其中包括

- dragonite-sdk 作为核心的一个可靠 UDP 协议实现库

- dragonite-mux 一个简单的连接复用库

- **dragonite-forwarder 基于前两者开发的 TCP 双边加速程序**

- **dragonite-proxy 一个支持类似 Surge 语法规则、UDP 转发的加密 SOCKS5 代理**

目前这四个项目都已经完全可用稳定。

这个最早来自我在中国时写来用于加速跨境文件传输的一个小工具，后来逐渐把其中可靠 UDP 传输协议实现分离出来作为类库，又基于此开发了这些项目。

Dragonite 协议本身主要特性是能在高延迟、高丢包但实际最大带宽已知的 "long fat pipes" 网络中以最大速率较为稳定地传输数据。

## 项目状态

Dragonite 有基于本协议的一系列网络应用，目前状态如下

### 版本状态 (2017-10-21):
- [x] **dragonite-sdk** (协议类库) (v0.3.3 已发布)
- [x] **dragonite-mux** (连接复用库) (v0.3.0 已发布)
- [x] **dragonite-forwarder** (TCP双边加速器) (v0.3.1 已发布)
- [x] **dragonite-proxy** (加密SOCKS5代理系统) (v0.3.1 已发布)

[更新日志](CHANGELOG.md)

### [查看详情: dragonite-forwarder](dragonite-forwarder/README.md)

一个可以用来加速任何TCP连接的转发器。

### [查看详情: dragonite-proxy](dragonite-proxy/README.md)

一个带有强大规则系统的加密SOCKS5代理。

## 对比测试

![TCP vs Dragonite](benchmarks/TCPvsDragonite.png)

## 注意

- Dragonite 并不 TCP 友好。满速使用 Dragonite 协议可能导致网络中其他 TCP 连接的速度急剧下降。请酌情使用。

## 愿景

本项目只是刚刚开始，未来的计划如下，**欢迎任何人加入开发**！

- 我们正在建立一个项目网站，用于提供下载与帮助文档 https://github.com/dragonite-network/dragonite-site (https://dragonite.network/)

- 我们正在翻译这些文档到多国语言 (优先中文，俄文与波斯文)

- 一个Go语言的移植项目已经在进行。欢迎移植到其他任何语言！

- 最终的目标是构建一个去中心化的代理网络，欢迎有这方面经验的开发者赐教！
