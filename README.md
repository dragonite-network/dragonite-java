# ![dragonite-java](assets/TextLogo.png)

    MAKE INTERNET GREAT AGAIN

![powered by dragonite](https://img.shields.io/badge/powered%20by-dragonite-yellow.svg)

Dragonite is a reliable application level data transport protocol based on UDP.

It started as a small private utility to transfer data faster over lossy networks, which turns out to be a fairly complete project of a new reliable UDP protocol. (I guess dealing with lossy internet connections and the GFW of China really helps with your computer network knowledge :wink:)

It is designed to be fast over lossy & unstable networks, highly customizable, provides an easy-to-use socket-like API.

## Usage Scenarios

- Transfer data between countries over lossy network connections

- Your application needs a persistent connection to your servers, without interference from NAT gateways

- Or you simply need an UDP protocol to bypass some weird firewall rules

- And many more...

## Projects

Dragonite is not only a reliable UDP library but also a series of actively developed network applications. You can use these applications, or take them as code samples to learn how to integrate Dragonite into your own projects.

### Current status (2017-8-27):
- [x] **dragonite-sdk** (Protocol library) (v0.3.2 released)
- [x] **dragonite-mux** (Connection multiplexing library) (v0.3.0 released)
- [x] **dragonite-forwarder** (TCP over Dragonite relay) (v0.3.0 released)
- [x] **dragonite-proxy** (Advanced encrypted SOCKS5 proxy) (v0.2.1 released)

### [More about: dragonite-forwarder](dragonite-forwarder/README.md)

A TCP (over Dragonite) relay program that can be used to accelerate any TCP connection between your clients and servers.

### [More about: dragonite-proxy](dragonite-proxy/README.md)

Advanced SOCKS5 proxy featuring encryption, traffic obfuscation and a powerful ACL system.

## Notice

- Dragonite is TCP-unfriendly and is intentionally designed to be so. Use with caution if TCP-friendliness is important in your network.
