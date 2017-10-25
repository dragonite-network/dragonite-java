FROM openjdk:slim

EXPOSE 5233/udp 5234/udp 1080/tcp

WORKDIR /code

ADD docker-entrypoint.sh ./
ADD dragonite-*/build/distributions/*.tar ./

ENTRYPOINT [ "/bin/bash", "docker-entrypoint.sh" ]
