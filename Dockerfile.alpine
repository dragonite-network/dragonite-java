FROM openjdk:alpine

EXPOSE 5233/udp 5234/udp 1080/tcp

WORKDIR /code

ADD docker-entrypoint.sh ./
ADD dragonite-*/build/distributions/*.tar ./

ENTRYPOINT [ "/bin/sh", "docker-entrypoint.sh" ]
