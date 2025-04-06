package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

import static org.knovash.squeezealice.web.PageIndex.pageOuter;

@Log4j2
public class PageManual {

    public static Context action(Context context) {
        context.bodyResponse = page();
        context.code = 200;
        return context;
    }

    public static String page() {
        log.info("PAGE LMS START");
        String pageInner =

                PageIndex.statusBar() +

                        "<p>" +
                        "запустить LMS\n" + "<br>" +
                        "запустит SA\n" + "<br>" +
                        "открыть SA http://localhost:8010\n" + "<br>" +
                        "LMS 192.168.1.110\n" + "<br>" +
                        "LMS плееры [HomePod2, HomePod, Homepod1, Radiotechnika, Mi Box]\n" + "<br>" +
                        "SA плееры не подключены в УДЯ\n" + "<br>" +
                        "Алиса в комнатах []\n" + "<br>" +
                        "УДЯ все комнаты []\n" + "<br>" +
                        "УДЯ нет плееров\n" + "<br>" +
                        "\n" + "<br>" +
                        "Для плееров невозможно выбрать комнаты\n" + "<br>" +
                        "\n" + "<br>" +
                        "открыть на планшети приложение УДЯ\n" + "<br>" +
                        "Добавить устройство, найти производителя Squeezebox LMS\n" + "<br>" +
                        "Привязать к Яндексу\n" + "<br>" +
                        "Подтверждение доступа - нажать Привязать\n" + "<br>" +
                        "\n" + "<br>" +
                        "Обновить список устройст - неполучилось (потому что устройств еще нет)\n" + "<br>" +
                        "\n" + "<br>" +
                        "Перейти на страницу SA\n" + "<br>" +
                        "SA для плееров LMS не выбраны комнаты УДЯ. Перейдите в настройку плееров и выберите комныты для плееров\n" + "<br>" +
                        "Перейдите в настройку плееров и выберите комнаты УДЯ для плееров LMS\n" + "<br>" +
                        "\n" + "<br>" +
                        "Можно нажать Информация-обновить - \n" + "<br>" +
                        "SA подключено 4 плееров [HomePod2 в Душ, HomePod в Спальня, Homepod1 в Гостиная, Radiotechnika в Веранда]\n" + "<br>" +
                        "В навыке появились устройства умного дома \"Музыка\"\n" + "<br>" +
                        "Теперь их можно найти в УДЯ.\n" + "<br>" +
                        "В приложении УДЯ в навыке Squeezebox LMS нажать Обновить список устройств\n" + "<br>" +
                        "Новые устройства успешно добавлены!\n" + "<br>" +
                        "список устройств\n" + "<br>" +
                        "Ура теперь все готово!\n" + "<br>" +
                        "\n" + "<br>" +
                        "\n" +
                        "" +
                        "</p>" +

                        "<br>";
        String page = pageOuter(pageInner, "Настройка LMS", "Настройка LMS");
        return page;
    }
}

