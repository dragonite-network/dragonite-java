FROM openjdk:latest

WORKDIR /code

ADD docker-entrypoint.sh ./
ADD dragonite-*/build/distributions/*.tar ./

ENTRYPOINT [ "/bin/bash", "docker-entrypoint.sh" ]
