create unique index messagetag on message_tags(message_id, tag_id);
create index related_messages on message_relations(tag_id, message_id);
create index user_messages on messages(posted_by);
create unique index taglookup on tags(tag);
