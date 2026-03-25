package org.knovash.squeezealice.utils;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.yandex.Yandex;
import org.knovash.squeezealice.yandex.YandexUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Log4j2
public class SchedulerYandexGetNewDevice {

    private static final AtomicBoolean cycleStarted = new AtomicBoolean(false);
    private static ScheduledExecutorService scheduler;

    public static synchronized void requestYandexForDevices(int delay, int attempts) {
        if (cycleStarted.get()) {
            log.debug("Yandex device cycle already running, skipping");
            return;
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        scheduler = Executors.newScheduledThreadPool(1);
        AtomicInteger counter = new AtomicInteger(0);
        cycleStarted.set(true);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                // Запоминаем размер ДО обновления
                int sizeBefore = SmartHome.devices.size();

                CompletableFuture.runAsync(() -> {
                    log.info("YANDEX DEVICES (до): {}",
                            SmartHome.devices.stream()
                                    .filter(Objects::nonNull)
                                    .map(device -> device.id)
                                    .collect(Collectors.toList()));

                    List<YandexUtils.MusicDevice> yandexInfoDevices = Yandex.devicesGetFromYandexInfo();
                    Yandex.createDevicesFromYandexDevices(yandexInfoDevices);

                    log.info("YANDEX DEVICES (после): {}",
                            SmartHome.devices.stream()
                                    .filter(Objects::nonNull)
                                    .map(device -> device.id)
                                    .collect(Collectors.toList()));
                }).whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Ошибка при обработке устройств Яндекса", throwable);
                    } else {
                        // Проверяем, увеличилось ли количество устройств
                        int sizeAfter = SmartHome.devices.size();
                        if (sizeAfter > sizeBefore) {
                            log.info("Количество устройств увеличилось с {} до {}, завершаем цикл досрочно",
                                    sizeBefore, sizeAfter);
                            cycleStarted.set(false);
                            scheduler.shutdown();
                            return; // Не планируем следующий запуск
                        }
                    }

                    // Старая логика: планируем следующее выполнение, если не исчерпаны попытки
                    if (counter.incrementAndGet() < attempts) {
                        scheduler.schedule(this, delay, TimeUnit.SECONDS);
                    } else {
                        cycleStarted.set(false);
                        scheduler.shutdown();
                        log.info("Завершён цикл запросов к Яндексу ({} повторов)", attempts);
                    }
                });
            }
        };

        scheduler.schedule(task, 0, TimeUnit.SECONDS);
    }
}