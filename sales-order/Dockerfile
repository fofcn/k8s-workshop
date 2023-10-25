FROM maven:3.6-jdk-8 as build
WORKDIR /app
COPY . .
RUN mvn clean package

FROM openjdk:11
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone
VOLUME /app
COPY --from=build /app/target/sales-order.jar /app/sales-order.jar
CMD java -jar -Xms128M -Xmx256M sales-order.jar
