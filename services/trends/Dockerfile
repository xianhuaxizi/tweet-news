FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD target/trends-1.0.0.jar  app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE  -jar /app.jar" ]
