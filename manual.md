Consola H2

Acceso a http://localhost:8080/h2-console.

Puedes usar estas credenciales:

Driver: org.h2.Driver
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (vacío)


Ejemplo de application.yml con dos perfiles

Cómo activar cada perfil
Por defecto, el perfil activo es dev .

Para correr con prod, puedes pasar el parámetro al arrancar:

bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod

o

bash
java -jar loanpayment.jar --spring.profiles.active=prod
👉 Con esto ya tienes un entorno de desarrollo más flexible y un entorno de producción más seguro y controlado.