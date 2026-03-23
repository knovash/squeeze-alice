package org.knovash.squeezealice.utils;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.yandex.DeviceStateUpdate;
import org.knovash.squeezealice.yandex.Yandex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.knovash.squeezealice.yandex.DeviceStateUpdate.sendBatchStateUpdates;

@Log4j2
public class SchedulerPlayersUpdate {

    // Единый пул потоков для всех задач
    private static final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(1);

    // Текущая задача планировщика (volatile для thread-safety)
    private static volatile ScheduledFuture<?> scheduledTask;

    // Приватный конструктор для предотвращения инстанцирования
    private SchedulerPlayersUpdate() {
    }

    /**
     * Запускает периодический опрос плееров
     *
     * @param periodMinutes интервал обновления в минутах
     */

    public static void startPeriodicUpdate2(int periodMinutes) {
        log.info("Starting players state updates. Interval: {} minutes", periodMinutes);
        cancelExistingTask();

        Runnable updateTask = () -> {
            try {
                log.info("Executing players state update");
                Main.lmsPlayers.updatePlayers();

                // Список для сбора обновлений
                List<DeviceStateUpdate> updates = new ArrayList<>();

                Main.lmsPlayers.players.stream()
                        .filter(player -> player != null && player.room != null)
                        .forEach(player -> {
                            // Получаем ID устройства по комнате
                            String deviceId = Yandex.deviceIdbyRoomName(player.room);
//                            log.info("DEVICE ID: " + deviceId);
                            if (deviceId == null) {
                                log.warn("No device ID for room: {}", player.room);
                                return;
                            }

                            // Обновление on/off (играет или нет)
                            boolean isPlaying = player.playing;
                            updates.add(new DeviceStateUpdate(
                                    deviceId,
                                    "on_off",
                                    "on",
                                    isPlaying
                            ));

                            // Получаем текущую громкость (если плеер подключен)
                            if (player.connected) {
                                player.volumeGet();
                                int volume = Integer.parseInt(player.volume);
                                updates.add(new DeviceStateUpdate(
                                        deviceId,
                                        "range",
                                        "volume",
                                        volume
                                ));
                            }
                        });

                // Отправляем одним пакетом
                sendBatchStateUpdates(updates);

            } catch (Exception e) {
                log.error("Failed to update players state", e);
            }
        };

        scheduledTask = executor.scheduleAtFixedRate(
                updateTask,
                1,
                periodMinutes,
                TimeUnit.MINUTES
        );
    }

    public static void startPeriodicUpdate(int periodMinutes) {
        log.info("Starting players state updates. Interval: {} minutes", periodMinutes);

        cancelExistingTask();

        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("Executing players state update");


                    List<DeviceStateUpdate> updates = new ArrayList<>();

                    Main.lmsPlayers.updatePlayers(); // PlayersUpdateScheduler
                    Main.lmsPlayers.players.stream()
                            .filter(player -> player != null)
                            .filter(player -> player.room != null)
                            .forEach(player ->
                            {
                                player.volume = "0";
                                if (player.connected) player.volumeGet();
                                Yandex.sendDeviceState(player.room, "on_off", "on", String.valueOf(player.playing), null);
                                Yandex.sendDeviceState(player.room, "range", "volume", String.valueOf(player.volume), null);

                            }); // startPeriodicUpdate
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