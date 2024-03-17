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


УСТАНОВКА

1. установить Backend на OrangePi

- установить Armbian
- запустить first_run.sh

2. создать два диалога - Навык Алисы и Умный дом https://dialogs.yandex.ru/developer

Название - Squeezebox LMS
Backend - Endpoint URL https://unicorn-***-***.ngrok-free.app
Тип доступа - Приватный
Связка аккаунтов
URL авторизации https://unicorn-***-***.ngrok-free.app/auth
URL для получения токена https://unicorn-***-***.ngrok-free.app/token
URL для обновления токена https://unicorn-***-***.ngrok-free.app/token
Идентификатор группы действий 12345

Имя навыка - раз два
Backend - Webhook URL https://unicorn-***-***.ngrok-free.app/alice/
Тип доступа - Приватный
Связка аккаунтов - ненужна

## ИСПОЛЬЗОВАНИЕ

### Комманды напрямую Алисе для устройств "Музыка" (стандартные для медиа устройств в приложении Алиса)

**Алиса, включи музыку** - включит музыку:
- продолжит играть если было на паузе
- если плейлист пуст включит последнее игравшее
- если играет другая колонка или группа подключит к ней
если плеер не играл более 15 минут, перед проигрыванием музыки, на плеер отправится тишина для включения колонки,
задержка 10сек., потом установлена громкость из пресета для этого времени.
пресет задаеться для каждой колонки в настройках, в виде час:громкость
по умолчанию для всех - 7:10,8:15,20:10,22:5,0:5

**Алиса, выключи музыку** - выключит музыку на колонке в комнате

* Алиса, выключи музыку везде - выключит музыку на всех колонках
* 
* Алиса, музыку громче(тише) - увеличит(уменьшит) громкость
* 
Алиса, музыку громче(тише) на {цифра} - увеличит(уменьшит) громкость на {цифра}

Алиса, громкость музыки {цифра} - установит громкость плеера на {цифра}

Алиса, включи канал {цифра} - включит канал {цифра} - соответствующую позицию из избранного в LMS
Алиса, переключи канал - включит следующий канал - следующую позицию из избранного в LMS

### Комманды через навык

Алиса, скажи {навык}, это комната {название комнаты}
- свяжет название комнаты с app_id колонки Алиса для работы с навыком

Алиса, скажи {навык}, включи избранное(канал){название}
- найдет в избранном LMS примерно подходящую по расстоянию Левинштейна закладку
- запустит эту закладку на плеере в LMS

Алиса, скажи {навык}, дальше (следующий)
- переключит на следующий трек в плейлисте LMS

Алиса, скажи {навык}, какая громкость
- ответ: сейчас на {название в LMS} громкость {значение}

Алиса, скажи {навык}, что играет
- ответ: сейчас на {название в LMS} играет <отдельно> {название} громкость {значение}

Алиса, скажи {навык}, включи {исполнитель}
- найдет на Spotify плейлист This Is {исполнитель}
- запустит этот плейлист на плеере в LMS

Алиса, скажи {навык}, переключи Spotify
- переключит воспроизведение из Spotify на плеер LMS в комнате

Алиса, скажи {навык}, выбери колонку {название в LMS}
- привяжет плеер LMS к комнате с Алисой

Алиса, скажи {навык}, отдельно
- плеер отвяжется от группы и будет играть отдельно

Алиса, скажи {навык}, вместе
- плеер синхронизируется с остальными

### Комманды через API для запуска через Tasker из виджетов или кнопок пульта твбокса

* https://{ngrok tunnel to home server}/cmd?action=toggle_music&player=homepod
* https://{ngrok tunnel to home server}/cmd?action=turn_on_music&player=homepod
* https://{ngrok tunnel to home server}/cmd?action=turn_off_music&player=homepod
* https://{ngrok tunnel to home server}/cmd?action=turn_off_speaker&player=homepod
* https://{ngrok tunnel to home server}/cmd?action=volume&player=homepod&value={value}
* https://{ngrok tunnel to home server}/cmd?action=channel&player=homepod&value={value}
* https://{ngrok tunnel to home server}/cmd?action=turn_on_spotify&player=homepod
* https://{ngrok tunnel to home server}/cmd?action=log