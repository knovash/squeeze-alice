# SqueezeBox control by Alice
Bridge for Logitech Media Server players (SqueezeBox) control via Alice voice assistant.
* <a href="https://dzen.ru/a/ZP0AKcRQ-h8hfwhT">Dzen.ru manual</a>
* <a href="https://[www.youtube.com/watch?v=1xTXht4AsQk](~~~~)">YouTube demo</a>
## Basic LMS features
* Alice, turn on the music - turn on the last one that played
* Alice, turn off the music - turn off all players
* Alice, music louder, quieter - volume control
* Alice, turn on the channel - play from favorites
* Alice, turn on Spotify - transfer from Spoty to LMS
## Basic Spotify features
* search [artist] in Spotify and run playlist "This Is [artist]"
* next track
* what's playing?
* what's the volume?
## Technologies
Project is created with:
* Java
* Server Sun HttpServer
* Client Apache Fluent
* JSON parser Jackson
* Args parser Apache Commons CLI
* Logitech Media Server API
* Spotify API
* Localtunnel, Serveo
## Launch
to run:
```
java -jar squeeze-alice-1.0.jar [-lmsip 192.168.1.52 -lmsport 9000 -port 8010 -context cmd]
```
if no args, default use LMS http://localhost:9000/ 
this server http://localhost:8010/
if not LMS not on localhost, try auto search LMS ip in network

create access to your computer from the Internet

```
lt --port 8010 --subdomain squeeze
```
example request is: http://192.168.1.52:8010/cmd?action=state

### Tasker UI for configuring settings

<a href="https://taskernet.com/shares/?user=AS35m8kJKYp5977YUIkcjNHTVyukgPWCIFiiEwuSPsUglqHm3bv6bL9D5mme1LtPP5KjRMog1V%2BP&id=Task%3AStateCfg">Tasker XML</a>
* volume step
* vloume low
* volume high
* wake delay
## Links
* Localtunel
https://localtunnel.github.io/www/
* Logitech Media Server
https://lms-community.github.io/lms-server-repository/


## УСТАНОВКА

* установить Armbian на OrangePi
* запустить first_run.sh скрипт выполнит:
* установка Java на OrangePi
* установка Ngrok на OrangePi
* копирование файлов на OrangePi
* запуск сервиса туннеля
* запуск сервиса сервера


создать два диалога - Навык Алисы и Умный дом https://dialogs.yandex.ru/developer

* Название - Squeezebox LMS
* Backend - Endpoint URL https://unicorn-***-***.ngrok-free.app
* Тип доступа - Приватный
* Связка аккаунтов
* URL авторизации https://unicorn-***-***.ngrok-free.app/auth
* URL для получения токена https://unicorn-***-***.ngrok-free.app/token
* URL для обновления токена https://unicorn-***-***.ngrok-free.app/token
* Идентификатор группы действий 12345

* Имя навыка - раз два
* Backend - Webhook URL https://unicorn-***-***.ngrok-free.app/alice/
* Тип доступа - Приватный
* Связка аккаунтов - ненужна

## ИСПОЛЬЗОВАНИЕ

### Комманды напрямую Алисе для устройств "Музыка" (стандартные для медиа устройств в приложении Алиса)

* **Алиса, включи музыку** - _включит музыку:
продолжит играть если было на паузе, 
если плейлист пуст включит последнее игравшее, 
если играет другая колонка или группа подключит к ней, 
если плеер не играл более 15 минут, перед проигрыванием музыки, на плеер отправится тишина для включения колонки, 
задержка 10сек., потом установлена громкость из пресета для этого времени. 
пресет задаеться для каждой колонки в настройках, в виде час:громкость. 
по умолчанию для всех - 7:10,8:15,20:10,22:5,0:5_

* **Алиса, выключи музыку** - _выключит музыку на колонке в комнате_ 
* **Алиса, выключи музыку везде** - _выключит музыку на всех колонках_
* **Алиса, музыку громче(тише)** - _увеличит(уменьшит) громкость_
* **Алиса, музыку громче(тише) на {цифра}** - _увеличит(уменьшит) громкость на {цифра}_
* **Алиса, громкость музыки {цифра}** - _установит громкость плеера на {цифра}_
* **Алиса, включи канал {цифра}** - _включит канал {цифра} - соответствующую позицию из избранного в LMS_
* **Алиса, переключи канал** - _включит следующий канал - следующую позицию из избранного в LMS_

### Комманды через навык
* **Алиса, скажи {навык}, это комната {название комнаты}** - _свяжет название комнаты с app_id колонки Алиса для работы с навыком_
* **Алиса, скажи {навык}, выбери колонку {название в LMS}** - _привяжет плеер LMS к комнате с Алисой_
* **Алиса, скажи {навык}, включи избранное(канал) {название}** - _найдет в избранном LMS примерно подходящую по расстоянию Левинштейна закладку, запустит эту закладку на плеере в LMS_
* **Алиса, скажи {навык}, дальше (следующий)** - _переключит на следующий трек в плейлисте LMS_
* **Алиса, скажи {навык}, какая громкость** - _ответ: сейчас на {название в LMS} громкость {значение}_
* **Алиса, скажи {навык}, что играет** - _ответ: сейчас на {название в LMS} играет <отдельно> {название} громкость {значение}_
* **Алиса, скажи {навык}, включи {исполнитель}** - _найдет в Spotify плейлист This Is {исполнитель}, запустит этот плейлист на плеере в LMS_
* **Алиса, скажи {навык}, переключи Spotify** - _переключит воспроизведение из Spotify на плеер LMS в комнате и другие играющине не отдельно_
* **Алиса, скажи {навык}, отдельно** - _плеер отвяжется от группы и будет играть отдельно_
* **Алиса, скажи {навык}, вместе** - _плеер синхронизируется с остальными_

### Комманды через API для запуска через Tasker из виджетов или кнопок пульта твбокса
* https://{ngrok tunnel to home server}/cmd?**action**=toggle_music&**player**={player name lms}
* https://{ngrok tunnel to home server}/cmd?**action**=toggle_music_all&**player**={player name lms}
* https://{ngrok tunnel to home server}/cmd?**action**=turn_on_music&**player**={player name lms}
* https://{ngrok tunnel to home server}/cmd?**action**=turn_off_music&**player**={player name lms}
* https://{ngrok tunnel to home server}/cmd?**action**=volume&**player**={player name lms}&**value**={value}
* https://{ngrok tunnel to home server}/cmd?**action**=channel&**player**={player name lms}&**value**={value}
* https://{ngrok tunnel to home server}/cmd?**action**=transfer&**player**={player name lms}
* https://{ngrok tunnel to home server}/cmd?**action**=alone_on&**player**={player name lms}
* https://{ngrok tunnel to home server}/cmd?**action**=separate_on&**player**={player name lms}
* https://{ngrok tunnel to home server}/cmd?**action**=separate_alone_off&**player**={player name lms}
* https://{ngrok tunnel to home server}/cmd?**action**=log