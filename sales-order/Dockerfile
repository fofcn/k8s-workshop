FROM openjdk:11

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone

VOLUME /data

ADD  ./target/sales-order.jar sales-order.jar

CMD java -jar -Xms128M -Xmx256M sales-order.jar
