# ![dragonite-java](/assets/TextLogo.png)

    MAKE INTERNET GREAT AGAIN

![powered by dragonite](https://img.shields.io/badge/powered%20by-dragonite-yellow.svg)

Current status (2017-8-16):
- [x] **dragonite-sdk** (Protocol library) (v0.3.2 released)
- [x] **dragonite-mux** (Connection multiplexing library) (v0.3.0 released)
- [x] **dragonite-forwarder** (Reverse proxy) (v0.3.0 released)
- [x] **dragonite-proxy** (Advanced encrypted SOCKS5 proxy) (v0.2.0 released)
- [ ] **dragonite-chat** (Simple chat platform) (WIP)

Dragonite is a (yet another, but one of the few that are being actively developed) reliable application level data transport protocol (and implementation) based on UDP.

**It is designed to be fast - even in extremely unstable networks, flexible - with lots of customizable options, and easy to use - implemented in Socket-like APIs.**

## Usage Scenarios

- Transfer data between countries with lossy network connections

- Your application needs a persistent connection to your servers, without interference from NAT gateways

- Or you simply need an UDP protocol to bypass the firewall

- And many more...

## Notice

- Dragonite is TCP-unfriendly and is intentionally designed to be so. Use with caution if TCP-friendliness is important in your network.
