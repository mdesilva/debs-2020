FROM flink:latest
WORKDIR /
COPY target/debs-2020-challenge-1.0.0.jar /Query1.jar
COPY run.sh /run.sh
CMD ["./run.sh"]
