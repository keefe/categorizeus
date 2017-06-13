insert into users(username, email, passhash) values ('keefe', 'keefe@categorize.us', '9ab3b090385ee96f8844969a7ee4e6a8c881cc4fb453562706db6a0506f559dd');
insert into users(username, email, passhash) values ('keefe1', 'keefe+1@categorize.us', '9ab3b090385ee96f8844969a7ee4e6a8c881cc4fb453562706db6a0506f559dd');
insert into users(username, email, passhash) values ('keefe2', 'keefe+2@categorize.us', '9ab3b090385ee96f8844969a7ee4e6a8c881cc4fb453562706db6a0506f559dd');
insert into users(username, email, passhash) values ('keefe3', 'keefe+3@categorize.us', '9ab3b090385ee96f8844969a7ee4e6a8c881cc4fb453562706db6a0506f559dd');
insert into users(username, email, passhash) values ('keefe4', 'keefe+4@categorize.us', '9ab3b090385ee96f8844969a7ee4e6a8c881cc4fb453562706db6a0506f559dd');
insert into users(username, email, passhash) values ('you', 'amyourweapon@categorize.us', '9ab3b090385ee96f8844969a7ee4e6a8c881cc4fb453562706db6a0506f559dd');


insert into tags(tag) values ('tag1');
insert into tags(tag) values ('tag2');
insert into tags(tag) values ('tag3');
insert into tags(tag) values ('tag4');
insert into tags(tag) values ('tag5');
insert into tags(tag) values ('repliesTo');
insert into tags(tag) values ('top');


insert into messages(body, title, posted_by) values ('Here is the body1', 'Thread:Here is the title1', 1);/*1*/
insert into messages(body, title, posted_by) values ('Here is the body2', 'Here is the title2', 2);/*2*/
insert into messages(body, title, posted_by) values ('Here is the body3', 'Here is the title3', 3);/*3*/
insert into messages(body, title, posted_by) values ('Here is the body4', 'Here is the title4', 4);/*4*/
insert into messages(body, title, posted_by) values ('Here is the body5', 'Here is the title5', 3);/*5*/
insert into messages(body, title, posted_by) values ('Reply One', 'Reply One Title', 3);/*6*/
insert into message_relations(tag_id, message_source_id, message_sink_id) values (6, 6,1);
insert into messages(body, title, posted_by) values ('Reply to Reply One 1', 'Reply To Reply One 1 Title', 3);/*7*/
insert into message_relations(tag_id, message_source_id, message_sink_id) values (6, 7,6);
insert into messages(body, title, posted_by) values ('Reply to Reply One 2', 'Reply To Reply One 2 Title', 3);/*8*/
insert into message_relations(tag_id, message_source_id, message_sink_id) values (6, 8,6);
insert into messages(body, title, posted_by) values ('Reply to Reply One 3', 'Reply To Reply One 3 Title', 3);/*9*/
insert into message_relations(tag_id, message_source_id, message_sink_id) values (6, 9,6);
insert into messages(body, title, posted_by) values ('Reply to (Reply to Reply One 1) 1', 'Reply* Title', 3);/*10*/
insert into message_relations(tag_id, message_source_id, message_sink_id) values (6, 10,7);
insert into messages(body, title, posted_by) values ('Reply to (Reply to Reply One 1) 2', 'Reply* Title', 3);/*11*/
insert into message_relations(tag_id, message_source_id, message_sink_id) values (6, 11,7);
insert into messages(body, title, posted_by) values ('Reply to (Reply to (Reply to Reply One 1) 2) 1', 'Reply* Title', 3);/*12*/
insert into message_relations(tag_id, message_source_id, message_sink_id) values (6, 12,11);


insert into message_tags(message_id, tag_id) values (1, 7);
insert into message_tags(message_id, tag_id) values (2, 7);
insert into message_tags(message_id, tag_id) values (3, 7);
insert into message_tags(message_id, tag_id) values (4, 7);
insert into message_tags(message_id, tag_id) values (5, 7);

insert into messages(body, title, posted_by) values ('Here is the body6', 'Here is the title6', 3);/*5*/
insert into messages(body, title, posted_by) values ('Here is the body7', 'Here is the title7', 3);/*5*/
insert into messages(body, title, posted_by) values ('Here is the body8', 'Here is the title8', 3);/*5*/
insert into messages(body, title, posted_by) values ('Here is the body9', 'Here is the title9', 3);/*5*/
insert into messages(body, title, posted_by) values ('Here is the body10', 'Here is the title10', 3);/*5*/
insert into messages(body, title, posted_by) values ('Here is the body11', 'Here is the title11', 3);/*5*/
insert into messages(body, title, posted_by) values ('Here is the body12', 'Here is the title12', 3);/*5*/
insert into messages(body, title, posted_by) values ('Here is the body13', 'Here is the title13', 3);/*5*/
insert into messages(body, title, posted_by) values ('Here is the body14', 'Here is the title14', 3);/*5*/
insert into messages(body, title, posted_by) values ('Here is the body15', 'Here is the title15', 3);/*5*/
insert into message_tags(message_id, tag_id) values (13, 7);
insert into message_tags(message_id, tag_id) values (14, 7);
insert into message_tags(message_id, tag_id) values (15, 7);
insert into message_tags(message_id, tag_id) values (16, 7);
insert into message_tags(message_id, tag_id) values (17, 7);
insert into message_tags(message_id, tag_id) values (18, 7);
insert into message_tags(message_id, tag_id) values (19, 7);
insert into message_tags(message_id, tag_id) values (20, 7);
insert into message_tags(message_id, tag_id) values (21, 7);
insert into message_tags(message_id, tag_id) values (22, 7);




insert into message_tags(message_id, tag_id) values (1, 1);
insert into message_tags(message_id, tag_id) values (6, 1);
insert into message_tags(message_id, tag_id) values (1, 2);
insert into message_tags(message_id, tag_id) values (1, 3);
insert into message_tags(message_id, tag_id) values (2, 1);
insert into message_tags(message_id, tag_id) values (2, 2);

