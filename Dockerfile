FROM amazoncorretto:21-al2023-headless AS fetcher

ADD target /target

RUN mv target/*-shaded.jar /target/application.jar


FROM amazoncorretto:21-al2023-headless

EXPOSE 8080

COPY --from=fetcher /target/application.jar /

WORKDIR /
CMD ["java", "-jar", "application.jar"]