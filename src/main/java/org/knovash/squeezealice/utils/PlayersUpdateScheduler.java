package org.knovash.squeezealice.utils;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.yandex.Yandex;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Log4j2
public class PlayersUpdateScheduler {

    // Единый пул потоков для всех задач
    private static final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(1);

    // Текущая задача планировщика (volatile для thread-safety)
    private static volatile ScheduledFuture<?> scheduledTask;

    // Приватный конструктор для предотвращения инстанцирования
    private PlayersUpdateScheduler() {
    }

    /**
     * Запускает периодический опрос плееров
     *
     * @param periodMinutes интервал обновления в минутах
     */
    public static void startPeriodicUpdate(int periodMinutes) {
        log.info("Starting players state updates. Interval: {} minutes", periodMinutes);

        cancelExistingTask();

        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                try {
                    log.debug("Executing players state update");
                    Main.lmsPlayers.updateLmsPlayers(); // PlayersUpdateScheduler
                    Main.lmsPlayers.players.stream()
                            .filter(player -> player != null)
                            .filter(player -> player.deviceId != null)
                            .forEach(player ->
                                    Yandex.sendDeviceState(player.deviceId, "on_off", "on", String.valueOf(player.playing), null)); // startPeriodicUpdate
                } catch (Exception e) {
                    log.error("Failed to update players state", e);
                }
            }
        };

        scheduledTask = executor.scheduleAtFixedRate(
                updateTask,
                1,      // Начальная задержка
                periodMinutes,
                TimeUnit.MINUTES
        );
    }

    /**
     * Останавливает выполнение периодических обновлений
     */
    public static void stopPeriodicUpdate() {
        log.info("Stopping players state updates");
        cancelExistingTask();
    }

    /**
     * Освобождает ресурсы планировщика
     */
    public static void shutdown() {
        log.info("Shutting down scheduler");
        cancelExistingTask();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static void cancelExistingTask() {
        if (scheduledTask != null && !scheduledTask.isDone()) {
            log.debug("Cancelling existing task");
            scheduledTask.cancel(false);
        }
    }
}