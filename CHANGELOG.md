# Changelog

## dragonite-sdk

### 0.1.0
The initial release.

### 0.1.1
Fix a thread deadlock problem

### 0.1.2

Add slow retransmission mode for packets that lost too many times

## dragonite-mux

### 0.1.0
The initial release.

### 0.2.0
New flow control mechanism (Buffer size limitations, Pause & Continue frames)

## dragonite-forwarder

### 0.1.0
The initial release.

### 0.1.1
Added a new speed limit option for server (-l)

### 0.1.2
Auto reconnect for broken underlying dragonite socket connections

Set default logging level to INFO, use --debug to enable debug mode