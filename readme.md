# SqueezeBox control by Alice
Bridge for Logitech Media Server players (SqueezeBox) control via Alice voice assistant.

<a href="https://dzen.ru/a/ZP0AKcRQ-h8hfwhT">Dzen.ru manual</a>

## Basic features
* Alice, turn on the music - turn on the last one that played
* Alice, turn off the music - turn off all players
* Alice, music louder, quieter - volume control
* Alice, turn on the channel - play from favorites
## Technologies
Project is created with:
* Java 11
* Apache Fluent
* Logitech Media Server API
* AlexStar SmartHome
* Localtunnel
## Launch
to run:
```
java -jar squeeze-alice-1.0.jar [LMS ip]
```
if no LMS ip, default use LMS http://localhost:9000/

create access to your computer from the Internetyour url is: https://squeeze.loca.lt

```
lt --port 8002 --subdomain squeeze
```

create rules for virtual players here
https://alexstar.ru/smarthome

```
https://squeeze.loca.lt/cmd?action=turnon&player=homepod
https://squeeze.loca.lt/cmd?action=turnoff&player=homepod
https://squeeze.loca.lt/cmd?action=volume&player=homepod&value={value}
https://squeeze.loca.lt/cmd?action=channel&player=homepod&value={value}
```
### Tasker UI for configuring settings

<a href="https://taskernet.com/shares/?user=AS35m8kJKYp5977YUIkcjNHTVyukgPWCIFiiEwuSPsUglqHm3bv6bL9D5mme1LtPP5KjRMog1V%2BP&id=Task%3AStateCfg">Tasker XML</a>
* volume step
* vloume low
* volume high
* wake delay
## Links
* Alex Star
  https://alexstar.ru/smarthome
* Localtunel
https://localtunnel.github.io/www/
* Logitech Media Server
https://www.mysqueezebox.com/download