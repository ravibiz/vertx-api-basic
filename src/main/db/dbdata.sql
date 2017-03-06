USE appdb;

truncate table messages;
insert into messages ( messageCode, message ) values
('INTERNAL_ERROR','Oh no, we ran into a technical error. Please try again in a few minutes or contact support.');
