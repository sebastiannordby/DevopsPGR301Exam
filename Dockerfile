# Stage 1: Bygg
# Bruker Maven-bilde for å bygge applikasjonen
FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /app

# Kopier Maven-konfigurasjonsfiler
COPY pom.xml .

# Last ned alle avhengigheter
RUN mvn dependency:go-offline

# Kopier kildekoden til bildet
COPY src /app/src

# Bygg applikasjonen
RUN mvn package -DskipTests

# Stage 2: Kjøring
FROM openjdk:11-jre-slim
WORKDIR /app

# Kopier den bygde applikasjonen fra Stage 1
COPY --from=build /app/target/*.jar app.jar

# Sett standard kommando for containeren
CMD ["java", "-jar", "app.jar"]
