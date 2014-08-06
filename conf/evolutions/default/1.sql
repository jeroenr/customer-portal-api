# --- !Ups

CREATE TABLE user (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    login_name varchar(255) NOT NULL,
    password_value varchar(255) NOT NULL,
    first_name varchar(255) NOT NULL,
    last_name varchar(255) NOT NULL,
    created_at timestamp,
    PRIMARY KEY (id)
);

CREATE TABLE customer (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    login_name varchar(255) NOT NULL,
    password_value varchar(255) NOT NULL,
    created_at timestamp,
    PRIMARY KEY (id)
);

CREATE TABLE keypair ( 
    id bigint(20) NOT NULL AUTO_INCREMENT,
    customer_id bigint(20) NOT NULL,
    auth_key varchar(255) NOT NULL,
    auth_secret varchar(255) NOT NULL,
    created_at timestamp,
    FOREIGN KEY (customer_id) REFERENCES customer(id),
    PRIMARY KEY (id)
);

CREATE TABLE key_register (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    value varchar(255) NOT NULL,
    PRIMARY KEY (id)
); 

INSERT INTO customer (name, login_name, password_value)
VALUES ('admin','adm','$2a$10$spALFU/13QVNIh1AmNUA0uYwagKVL5OqmmqgsqLhoUFcqOCdIInzm');

# --- !Downs
 
DROP TABLE user;
DROP TABLE customer;
DROP TABLE key_register;
