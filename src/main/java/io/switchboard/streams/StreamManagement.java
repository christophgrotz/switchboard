package io.switchboard.streams;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.mongodb.*;
import io.switchboard.streams.domain.Stream;
import io.switchboard.streams.messages.*;

import java.net.UnknownHostException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Christoph Grotz on 09.12.14.
 */
public class StreamManagement extends AbstractActor {

  private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private MongoClient client;

  private StreamManagement(String clientUri) throws UnknownHostException {
    client = new MongoClient(new MongoClientURI(clientUri));
    DB switchboard = client.getDB("switchboard");
    DBCollection streams = switchboard.getCollection("streams");

    receive(ReceiveBuilder
      .match(RetrieveStreams.class, message -> {
        log.info("retrieve streams {}", message);
        sender().tell(streams.find().toArray().stream().map(dbObject -> new Stream().setId(dbObject.get("id").toString()).setName(dbObject.get("name").toString())).collect(Collectors.toList()), self());
      })
      .match(RetrieveStream.class, message -> {
        log.info("retrieve stream {}", message);
        DBObject dbObject = streams.findOne(new BasicDBObject("id", message.getId()));
        sender().tell(new Stream().setId(dbObject.get("id").toString()).setName(dbObject.get("name").toString()), self());
      })
      .match(DeleteStream.class, message -> {
        log.info("delete stream {}", message);
        DBObject dbObject = streams.findOne(new BasicDBObject("id", message.getId()));
        Stream stream = new Stream().setId(dbObject.get("id").toString()).setName(dbObject.get("name").toString());
        streams.remove(new BasicDBObject("id", message.getId()));
        sender().tell(stream, self());
      })
      .match(UpdateStream.class, message -> {
        log.info("update stream {}", message);
        Stream stream = message.getStream();
        stream.setId(message.getStreamId());
        streams.update(new BasicDBObject("id", message.getStreamId()), new BasicDBObject("id", message.getStreamId()).append("name", message.getStream().getName()));
        sender().tell(stream, self());
      })
      .match(CreateStream.class, message -> {
        log.info("create stream {}", message);
        String id = UUID.randomUUID().toString();

        Stream stream = message.getStream();
        stream.setId(id);
        streams.insert(new BasicDBObject("id", id).append("name", message.getStream().getName()));
        sender().tell(stream, self());
      })
      .build());
  }

  public static RetrieveStreams retrieve() {
    return new RetrieveStreams();
  }

  public static RetrieveStream retrieve(String id) {
    return new RetrieveStream(id);
  }

  public static DeleteStream delete(String id) {
    return new DeleteStream(id);
  }

  public static CreateStream create(Stream stream) {
    return new CreateStream(stream);
  }

  public static UpdateStream update(String streamId, Stream stream) {
    return new UpdateStream(streamId, stream);
  }


}
