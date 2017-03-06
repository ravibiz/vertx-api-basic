DROP SCHEMA IF EXISTS appdb;
create SCHEMA appdb;
USE appdb;

DROP TABLE IF EXISTS messages; 
create table messages (
	id integer NOT NULL AUTO_INCREMENT,
	messageCode varchar(64),
	message text,
	primary key (id),
	unique key i_messageCode (messageCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Server side messages';

DROP TABLE IF EXISTS test; 
create table test (
	id integer NOT NULL AUTO_INCREMENT,
	name varchar(64),
	age int,
	touchtime timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	primary key (id),
	UNIQUE KEY unique_fbid (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='app test table';