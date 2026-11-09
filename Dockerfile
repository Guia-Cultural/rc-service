
# ==========================
# Stage 1: Build
# ==========================
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Copia os arquivos do Maven
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Baixa as dependências
RUN chmod +x mvnw
RUN ./mvnw -B -ntp dependency:go-offline

# Copia o código-fonte
COPY src/ src/

# Compila e gera o JAR
RUN ./mvnw -B -ntp package -DskipTests

# ==========================
# Stage 2: Runner
# ==========================
FROM eclipse-temurin:25-jre-alpine AS runner

WORKDIR /app

# Cria usuário sem privilégios de root
RUN addgroup -S app && adduser -S app -G app

# Copia apenas o JAR compilado
COPY --from=builder --chown=app:app \
    /app/target/rc-service-0.0.1-SNAPSHOT.jar app.jar

USER app

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
