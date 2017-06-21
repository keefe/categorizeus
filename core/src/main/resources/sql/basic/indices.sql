create unique index messagetag on message_tags(message_id, tag_id);
create index related_messages_source on message_relations(tag_id, message_source_id);
create index related_messages_sink on message_relations(tag_id, message_sink_id);
create index user_messages on messages(posted_by);
create unique index taglookup on tags(tag);
