FROM gradle:7.4.0-jdk17

WORKDIR /

COPY / .

RUN gradle installDist

CMD ./gradlew bootRun --args='--spring.profiles.active=prod'