FROM maven:3.6-jdk-11 as build
WORKDIR /app
COPY . .
RUN mvn clean package

FROM openjdk:11
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone
WORKDIR /app
COPY --from=build /app/target/sales-product.jar /app/sales-product.jar
CMD java -jar -Xms128M -Xmx256M /app/sales-product.jar
