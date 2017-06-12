# Changelog

## dragonite-sdk

### 0.1.0
The initial release.

### 0.1.1
Fix a thread deadlock problem

### 0.1.2
Add slow retransmission mode for packets that lost too many times

### 0.2.0
Add HTTP(JSON) statistics interface

Fix redundant retransmissions caused by delayed ACKs

### 0.2.1
Enable Cross-Origin Resource Sharing (CORS) for HTTP(JSON) statistics interface

## dragonite-mux

### 0.1.0
The initial release.

### 0.2.0
New flow control mechanism (Buffer size limitations, Pause & Continue frames)

## dragonite-forwarder

### 0.1.0
The initial release.

### 0.1.1
Add a new speed limit option for server (-l)

### 0.1.2
Auto reconnect for broken underlying dragonite socket connections

Set default logging level to INFO, use --debug to enable debug mode

### 0.1.3
Add an option for enabling HTTP(JSON) statistics interface of underlying dragonite socket connections

### 0.1.4
GUI Web Panel for HTTP(JSON) statistics interface

--web-panel & --web-panel-public options