# Базовый образ с Java Runtime
FROM eclipse-temurin:17-jre

#ARG CACHE_BUSTER

# Рабочая директория
WORKDIR /app

# Копируем JAR-файл
COPY target target

WORKDIR /app/target
RUN cd /app/target

# Открываем порт
EXPOSE 8010

# Команда запуска
CMD ["java", "-jar", "app.jar"]
