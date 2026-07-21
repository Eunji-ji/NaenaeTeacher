FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends unzip \
    && rm -rf /var/lib/apt/lists/*

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x ./gradlew

# Keep dependency resolution in a cacheable layer.
RUN ./gradlew --no-daemon dependencies >/dev/null

COPY src src
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system naenae \
    && useradd --system --gid naenae --home-dir /app --shell /usr/sbin/nologin naenae

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

RUN mkdir -p /var/lib/naenae-teacher/uploads \
    && chown -R naenae:naenae /app /var/lib/naenae-teacher

USER naenae

ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8081 \
    JAVA_OPTS="-Xms128m -Xmx512m"

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
    CMD curl --fail --silent --show-error http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
