FROM flink:latest
WORKDIR /
COPY target/debs-2020-challenge-1.0.0.jar /Query1.jar
COPY custom-conf.yaml /flink-conf.yaml
CMD ["flink", "run", "Query1.jar"]