FROM eclipse-temurin:17-jre
ENV TZ=Europe/Oslo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /app
RUN addgroup --gid 1001 --system app && \
  adduser --uid 1001 --system app --gid 1001 && \
  chown -R app:app /app && \
  chmod 770 -R /app
USER app:app
COPY --chown=app:app /target/app.jar app.jar
CMD java -XX:+UseZGC -jar app.jar
