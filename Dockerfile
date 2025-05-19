# Dockerfile com Amazon Corretto 21
FROM amazoncorretto:21

# Define o diretorio de trabalho dentro do conteiner
WORKDIR /app

# Copia o JAR para dentro do conteiner
COPY target/pedido-0.0.1-SNAPSHOT.jar app.jar

# Expoe a porta usada pelo Spring Boot
EXPOSE 8080

# Comando para rodar a aplicacao
ENTRYPOINT ["java", "-jar", "app.jar"]