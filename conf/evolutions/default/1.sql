# --- !Ups

CREATE TABLE user (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    login_name varchar(255) NOT NULL,
    password_value varchar(255) NOT NULL,
    first_name varchar(255) NOT NULL,
    last_name varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE customer (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    auth_key varchar(255) NOT NULL,
    auth_secret varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE key (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    value varchar(255) NOT NULL,
    PRIMARY KEY (id)
) 

# --- !Downs
 
DROP TABLE user;
DROP TABLE customer;
