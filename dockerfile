# Базовый образ с Java Runtime
FROM eclipse-temurin:17-jre

ARG CACHE_BUSTER

# Рабочая директория
WORKDIR /app

# RUN ls
# RUN ls /
# RUN ls /app


# Копируем JAR-файл
COPY target target

#ARG CACHE_BUSTER

# RUN ls /app
# RUN ls /app/target
WORKDIR /app/target
RUN cd /app/target

# Открываем порт
EXPOSE 8010

# Команда запуска
CMD ["java", "-jar", "app.jar"]
