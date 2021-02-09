FROM maven:3-openjdk-15-slim AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn clean package --no-transfer-progress -DskipTests

FROM openjdk:15-slim
ENV TZ=Europe/Oslo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /app
RUN addgroup --gid 1001 --system app && \
  adduser --uid 1001 --system app --gid 1001 && \
  chown -R app:app /app && \
  chmod 770 /app \
  chmod 775 /app/store
USER app:app
COPY --chown=app:app --from=build /app/target/app.jar ./
CMD java -jar app.jar
WORKDIR /