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
* play [artist] - searh Spotify, get playlist "This Is [artist]" and play in LMS 
## Technologies
Project is created with:
* Java
* Server Sun HttpServer
* Client Apache Fluent
* JSON parser Jackson
* Args parser Apache Commons CLI
* Logitech Media Server API
* AlexStar SmartHome
* Localtunnel
## Launch
to run:
```
java -jar squeeze-alice-1.0.jar [-lmsip 192.168.1.52 -lmsport 9000 -port 8010 -context cmd]
```
if no args, default use LMS http://localhost:9000/ 
this server http://localhost:8010/cmd

create access to your computer from the Internet

```
lt --port 8010 --subdomain squeeze
```
example request is: http://192.168.1.52:8010/cmd?action=state

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