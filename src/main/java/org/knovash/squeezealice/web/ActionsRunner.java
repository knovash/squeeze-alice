package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Tasker;

import java.util.Arrays;
import java.util.List;

// Предполагается, что классы Tasker и LmsPlayers доступны статически или через контекст
// В реальном коде их нужно импортировать и использовать корректные ссылки

@Log4j2
public class ActionsRunner extends ActionsBase {

    // Ссылки на реальные методы – можно передавать через лямбды
    // Здесь для примера используются заглушки – замените на реальные вызовы

    public static ActionsRunner actionsRunner = new ActionsRunner();

    @Override
    protected List<Action> getActions() {
        return Arrays.asList(
                new Action(
                        "ACTION_TASKER_REFRESH",
                        "Обновить таскер",

                        ctx -> {
                            log.info("Выполняется обновление Tasker");
                            //Tasker.postToTaskerRefresh();
                            //Tasker.sendRequestToTaskerRunRefresh();
                            // Например: Tasker.refreshPost();
                            // Для демонстрации просто добавим сообщение в ответ
                            ctx.bodyResponse = "Tasker обновлён";
                        }
                ),
                new Action(
                        "ACTION_REFRESH_LMS_PLAYERS",
                        "Обновить плееры в LMS",
                        ctx -> {
                            // Вызов реального метода lmsplayers.update()
                            // Например: LmsPlayers.getInstance().update();
                            log.info("Выполняется обновление плееров LMS");
                            ctx.bodyResponse = "Плееры LMS обновлены";
                        }
                )
                // Можно добавить другие действия
        );
    }

    // При необходимости можно переопределить заголовки
    @Override
    protected String getPageTitle() {
        return "Панель действий";
    }

    @Override
    protected String getPageHeader() {
        return "Выполнить команду";
    }
}