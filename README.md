# my-notes-rest-manual-auth-client
EN: A Java and Spring Boot project for taking notes using the REST API (only the client are implemented in this application). 

The project implements a REST client that accesses the server API endpoints for the Note entity. At the same time:
1) the application interface provides access via the server API to all data operations (CRUD);  
2) in the application interface in the list of notes, the functions of page-by-page viewing of records and filtering of data are implemented, which work consistently; 
3) in the application interface, the function of uploading a list of notes in the form of a report to a PDF file is implemented (the function uploads data to the report only about those notes that are available in accordance with the filtering and pagination criteria at the time of uploading the report). 

NOTE: In this part, this project is similar to the project from the repository "my-notes-rest-manual-auth" (only in the client part).

In addition, the application has developed authentication and configured security based on Spring Security, which supports: 
1) user authentication in the client part of the application; 
2) transmission of user authentication data to the server in the "Authorization" header in client requests using Http Basic authentication; 
3) transfer to the CSRF server of the token in the header "X-CSRF-TOKEN" in client requests when performing "dangerous" requests that change the state of data. 

P.S.: This version of the application implements only the functions of the client.

/-------------------------------------------------------------------------------------------------------------------------------/

RU: Проект на Java и Spring Boot по учету заметок с использованием REST API (в этом приложении реализован только клиент).

Проект реализует REST клиент, который обращается к конечным точкам API сервера для сущности Note (Заметка). При этом:
1) в интерфейсе приложения обеспечен доступ через API сервера ко всем операциям с данными (CRUD);  
2) в интерфейсе приложения в списке заметок реализованы функции пострачниного просмотра записей и фильтрации данных, которые работают согласованно; 
3) в интерфейсе приложения реализована функция выгрузки списка заметок в виде отчета во веншний файл формата PDF (функция выгружает в отчет данные только о тех заметках, которые доступны в соответствии с критериями фильтрации и постраничного просмотра на момент выгрузки отчета). 

ПРИМЕЧАНИЕ: В этой части данный проект аналогичен проекту из репозитория "my-notes-rest-manual-auth" (только в части клиента).

Кроме того, в приложении разработана аутентификация и настроена безопасность на основе Spring Security, которая поддерживает: 
1) аутентификацию пользователя в клиентской части приложения; 
2) передачу на сервер аутентификационных данных пользователя в заголовке "Authorization" в запросах клиента с помощью Http Basic аутентификации; 
3) передачу на сервер CSRF токена в заголовке "X-CSRF-TOKEN" в запросах клиента при выполнении "опасных" запросов, изменяющих состояние данных. 

P.S.: Данная версия приложения реализует только функции клиента.
