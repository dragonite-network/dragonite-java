# ![dragonite-java](assets/TextLogo.png)

    INTERNET UNLEASHED.

![powered by dragonite](https://img.shields.io/badge/powered%20by-dragonite-yellow.svg)

[![https://t.me/DragoniteProject](https://img.shields.io/badge/Telegram%20Group%20CN-https%3A%2F%2Ft.me%2FDragoniteProject-blue.svg)](https://t.me/DragoniteProject)

中文用户请加入 [Telegram 群组](https://t.me/DragoniteProject)

Dragonite is a reliable application level data transport protocol based on UDP.

It started as a small private utility to transfer data faster over lossy networks, which turns out to be a fairly complete project of a new reliable UDP protocol. I guess dealing with lossy internet connections and the GFW of China really helps with your computer network knowledge. :wink:

It is designed to be fast over lossy & unstable networks, highly customizable with an easy-to-use socket-like API.

## Usage Scenarios

- Transfer data between countries over lossy network connections

- Your application needs a persistent connection to your servers, without interference from NAT gateways

- Or you simply need an UDP protocol to bypass some weird firewall rules

- And many more...

## Projects

Dragonite is not only a reliable UDP library but also a series of actively developed network applications. You can use these applications, or take them as code samples to learn how to integrate Dragonite into your own projects.

### Current status (2017-10-16):
- [x] **dragonite-sdk** (Protocol library) (v0.3.3 released)
- [x] **dragonite-mux** (Connection multiplexing library) (v0.3.0 released)
- [x] **dragonite-forwarder** (TCP over Dragonite relay) (v0.3.0 released)
- [x] **dragonite-proxy** (Advanced encrypted SOCKS5 proxy) (v0.3.0 released)

### [More about: dragonite-forwarder](dragonite-forwarder/README.md)

A TCP (over Dragonite) relay program that can be used to accelerate any TCP connection between your clients and servers.

### [More about: dragonite-proxy](dragonite-proxy/README.md)

Advanced SOCKS5 proxy featuring encryption, traffic obfuscation and a powerful ACL system.

## Benchmarks

![TCP vs Dragonite](benchmarks/TCPvsDragonite.png)

## Notice

- Dragonite is TCP-unfriendly and is intentionally designed to be so. Use with caution if TCP-friendliness is important in your network.

## The Road Ahead

The release of our project is just a beginning. Our plans are as follows, **anyone is welcome to help**!

- We are building a project website to provide download links and user manuals. https://github.com/dragonite-network/dragonite-site (https://dragonite.network/)

- We are translating our documents into multiple languages (Especially Chinese, Russian and Persian)

- A Go port of dragonite-sdk and dragonite-mux is currently under development. Feel free to port this project in any other language you like.

- The ultimate goal is to build a uncensorable, decentralized peer-to-peer proxy network. Developers with experience of these are more than welcome to be our contributors!
