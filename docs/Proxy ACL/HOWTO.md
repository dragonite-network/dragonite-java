# Creating ACL files for clients

**See "[sample.txt](sample.txt)" for a complete sample**

With ACL files, you can customize the way dragonite-proxy handling different destination addresses. You can create rules telling it which sites should be connected through the remote server, which sites should be connected through local network and which sites should be blocked.

An ACL file consists of two sections, information section and rules section.

## Information section

The information section usually contains information such as title, author and default connection method.

It's a simple `key:value` syntax. All fields are optional and have reasonable default values.

Currently supported key:

    title: Title of this ACL file
    author: Author of this ACL file
    default: The default behavior when there is no rule that matches an address (Default value: proxy)

Example:

    title: CHNRoutes (IPv4/IPv6 Universal)
    author: Toby
    default: proxy

## Rules section

The rules section defines how different addresses are handled.

Syntax: `address-type,address,method`

Example: `ipv4-cidr,103.42.32.0/22,direct`

### Supported address

`domain`: Precisely match a specific domain name (e.g. `apple.com` will matches `apple.com`, but not `www.apple.com`)

`domain-suffix`: Match a suffix for a domain name (e.g. `apple.com` will matches `apple.com` and `*.apple.com`)

`ipv4`: Match an IPv4 address (e.g. `8.8.8.8`)

`ipv4-cidr`: Match a block of IPv4 addresses (e.g. `103.42.32.0/22`)

`ipv6`: Match an IPv6 address (e.g. `2001:4860:4860::8888`)

`ipv6-cidr`: Match a block of IPv6 addresses (e.g. `2401:9600:0000:0000:0000:0000:0000:0000/32`)

### Supported method

`direct`: Directly connect through local network

`proxy`: Connected through the remote server

`reject`: Reject the connection

It should be noted that the domains from proxy requests will be resolved locally (with local DNS servers) and dragonite-proxy will try to match the IP rules with its A/AAAA record.
