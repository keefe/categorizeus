
create table if not exists tags(
	id bigserial primary key, 
	tag varchar(255) not null
);

create table if not exists messages(
	id bigserial primary key,
	body text,
	title text,
	posted_by bigint,
	link text,
  img_width int,
  img_height int,
  thumb_width int,
  thumb_height int,
  thumb_link text
);

create table if not exists users(
	id bigserial primary key,
	username text,
	email text,
	passhash text
);

create table if not exists message_tags(
	message_id bigint,
	tag_id bigint,
	rating real DEFAULT 1.0
);

create table if not exists message_relations(
	id bigserial primary key,
	tag_id bigint,
	message_source_id bigint,
	message_sink_id bigint
);

create table if not exists user_sessions(
	session_uuid varchar(128) primary key,
	user_id bigint
);
