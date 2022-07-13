# ![CS50x Iran](./assets/logo_v1.svg) Telegram Bot
![RELEASE](https://img.shields.io/badge/RELEASE-v2.0.0-green)
![Tech](https://img.shields.io/badge/TECH-Kotlin,%20Spring%20Boot-orange)

### Navigation
- [How To Deploy Project](#deploy)
- [Our Contact Information](#contact)

### Deploy
You can deploy project using docker and docker-compose!<br>

First create a `docker-compose.yaml`
```yaml
version: '3.9'
services:
  cs50_bot:
    restart: on-failure
    build: ./cs50_bot
    ports:
      - '8080:8080'
```
After that, clone the project using git
```shell
git clone https://github.com/YasTechOrg/cs50_bot
```
Finally, you can build and deploy project using docker compose<br>
Don't forget to change `{TOKEN}` with your own telegram bot token!
```shell
docker-compose up -d --build --build-arg TOKEN="{TOKEN}"
```
### Contact
- Hossein Araghi
    - Email : hoseinaraghi84@gmail.com
    - Website : [hossara.com](https://hossara.com)
    - Instagram : @hossara.dev | @hossara.pv
    - LinkedIn : @dev-haraghi