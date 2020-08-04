create database converter with encoding='UTF8';
create user converter_user with password 'password';
grant all privileges on database converter to converter_user;