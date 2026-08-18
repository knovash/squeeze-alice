package org.knovash.squeezealice.player;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
@Log4j2
public class AsyncExecutor {

    private static final long DEFAULT_TIMEOUT = 1;
    private static final TimeUnit DEFAULT_UNIT = TimeUnit.SECONDS;
    private static final String DEFAULT_TIMEOUT_MESSAGE = "Не успела выполнить команду";
    private static final String DEFAULT_ERROR_MESSAGE = "Ошибка выполнения команды";

    /**
     * Универсальный метод для асинхронного выполнения задачи с таймаутом.
     *
     * @param task     задача, возвращающая String (например, () -> ActionsSync.volumeLimitSet(player, command))
     * @param timeout  время ожидания
     * @param unit     единица времени
     * @return результат задачи или сообщение об ошибке/таймауте
     */
    public static String executeWithTimeout(Supplier<String> task, long timeout, TimeUnit unit) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(task);
        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            //future.cancel(true); // попытка прервать выполнение (не гарантирует остановку)
            return DEFAULT_TIMEOUT_MESSAGE;
        } catch (Exception e) {
            // Логируем исключение для диагностики (замените на ваш логгер)
            // log.error("Ошибка при выполнении асинхронной задачи", e);
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    // Перегрузка с таймаутом по умолчанию (1 секунда)
    public static String executeWithTimeout(Supplier<String> task) {
        return executeWithTimeout(task, DEFAULT_TIMEOUT, DEFAULT_UNIT);
    }
}