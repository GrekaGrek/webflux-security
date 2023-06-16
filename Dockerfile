FROM eclipse-temurin:17-jdk
COPY build/libs/webflux-security-1.0.0.jar webflux-security.jar
ENTRYPOINT [ "java", "-jar", "webflux-security.jar" ]