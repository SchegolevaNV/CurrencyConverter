Как пользоваться конвертером валют

1) Сначала необходимо создать базу. Скрипт лежит в папке /resources/postgresql.sql

Его содержимое:
---
create database converter with encoding='UTF8';
create user converter_user with password 'password';
grant all privileges on database converter to converter_user;
----

2) Сами таблицы создаёт liquibase. Скрипты лежат в папке /resources/db.changelog/..

                                        Методы API

1) Чтобы залогиниться для использования конвертера воспользуйтесь командой: (POST) api/v1/auth/login

Данные принимаются в JSON формате (на данный момент в базе создан один тестовый пользователь):
{
  "login": "petr",
  "password": "password"
}

2) (GET) api/v1/converter/convert - запуск самого конвертера валют

Входные данные:
currencyFrom - н-р: RUB;
currencyTo - н-р: USD;
exchangeSum - н-р: 1000.99 или 1000

3) (GET) api/v1/converter/history - получение всей истории конвертаций залогиненного пользователя

Входные данные offset и limit (или без них, тогда по-умолчанию будут параметры 0 и 10)

4) (GET) api/v1/converter/history/date - получение истории конвертаций на заданную дату для залогиненного пользователя

Входные данные offset и limit (или без них, тогда по-умолчанию будут параметры 0 и 10) и дата в формате: 2020-08-04

