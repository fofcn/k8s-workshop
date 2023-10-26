FROM maven:3.6-jdk-11 as build
WORKDIR /app
COPY . .
RUN mvn clean package

FROM openjdk:11
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone
VOLUME /app
COPY --from=build /app/target/sales-order-v1.jar /app/sales-order-v1.jar
CMD java -jar -Xms128M -Xmx256M  /app/sales-order-v1.jar
