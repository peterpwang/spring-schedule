# spring-schedule
A web application and a background server for mobile for booking workers schedule.

Technical stacks:
1. Basic CRUD: Spring Data Rest, Hibernate, JPA, Mysql, JSon, ReactJS, HTML5, CSS.
2. Search by name.
3. Spring Security.
4. Support CRSF.
5. Server side validation: Event handler.
6. Client side validation: Html5 validator. 
7. Bootstrap 4, Popper and JQuery.
8. Spring MVC.
9. ReactJS-Router.
10. Support to H2.
11. Test case.
12. Microservices: Eureka, Zuul.
13. Support Docker.


To start the system:
1. In Registry, run: "mvn spring-boot:run". It runs on port 8761.
2. In AuthService, run: "mvn spring-boot:run". It runs on port 9100.
3. In ScheduleService: run: "mvn spring-boot:run". It runs on port 8000.
4. In UserService: run: "mvn spring-boot:run". It runs on port 8100.
5. In Gateway: run: "mvn spring-boot:run". It runs on port 8080.
6. In a browser, visit http://localhost:8080
