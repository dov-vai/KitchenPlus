# KitchenPlus
Kitchen furniture store

# Mail configuration

You may need to set the following values in **application.properties** if you want to send mails:
```yaml
custom.mail.host=
custom.mail.port=
custom.mail.username=
custom.mail.password=
```

# Running

Uses Java 21.

In the project root directory:

UNIX:
```sh
./gradlew bootRun
```
Windows:
```ps
./gradlew.bat bootRun
```

# Development

- [Thymeleaf](https://www.thymeleaf.org/) is used for the HTML templating.

- [JPA](https://spring.io/projects/spring-data-jpa) for the database.

- [Spring MVC](https://docs.spring.io/spring-boot/how-to/spring-mvc.html)
