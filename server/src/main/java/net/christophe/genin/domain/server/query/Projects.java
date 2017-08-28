package net.christophe.genin.domain.server.query;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.christophe.genin.domain.server.db.Dbs;
import net.christophe.genin.domain.server.db.Schemas;
import net.christophe.genin.domain.server.json.Jsons;

import java.util.Optional;

public class Projects extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(Projects.class);

    public static final String LIST = Projects.class.getName() + ".list";
    public static final String GET = Projects.class.getName() + ".get";
    public static final String ID = "id";

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(LIST, msg -> {
            final JsonArray arr = Dbs.instance.getCollection(Schemas.Projects.collection())
                    .find().toList()
                    .parallelStream()
                    .map(doc -> {
                        final JsonObject obj = new JsonObject();
                        Optional.ofNullable(doc.get(Schemas.Projects.release.name()))
                                .ifPresent((s) -> obj.put(Schemas.Projects.release.name(), s));
                        Optional.ofNullable(doc.get(Schemas.Projects.snapshot.name()))
                                .ifPresent((s) -> obj.put(Schemas.Projects.snapshot.name(), s));
                        final Dbs.Attributes attributes = new Dbs.Attributes(doc);
                        return obj
                                .put(Schemas.Projects.id.name(), doc.get(Schemas.Projects.id.name()))
                                .put(Schemas.Projects.name.name(), doc.get(Schemas.Projects.name.name()))
                                .put(Schemas.Projects.latestUpdate.name(), doc.get(Schemas.Projects.latestUpdate.name()))
                                .put(Schemas.Projects.tables.name(), attributes.toJsonArray(Schemas.Projects.tables.name()))
                                .put(Schemas.Projects.javaDeps.name(), attributes.toJsonArray(Schemas.Projects.javaDeps.name()));
                    }).collect(Jsons.Collectors.toJsonArray());
            if (logger.isDebugEnabled()) {
                logger.debug("LIST -res :" + arr.encodePrettily());
            }
            msg.reply(arr);
        });
        vertx.eventBus().consumer(GET, (Handler<Message<JsonObject>>) msg -> {
            String id = msg.body().getString(ID, "");
            final JsonArray l = Dbs.instance.getCollection(Schemas.Version.collection(id))
                    .find().toList()
                    .parallelStream()
                    .map(doc -> {
                        final Dbs.Attributes attributes = new Dbs.Attributes(doc);
                        return new JsonObject()
                                .put(Schemas.Version.id.name(), doc.get(Schemas.Version.id.name()))
                                .put(Schemas.Version.name.name(), doc.get(Schemas.Version.name.name()))
                                .put(Schemas.Version.isSnapshot.name(), doc.get(Schemas.Version.isSnapshot.name()))
                                .put(Schemas.Version.latestUpdate.name(), doc.get(Schemas.Version.latestUpdate.name()))
                                .put(Schemas.Version.tables.name(), attributes.toJsonArray(Schemas.Version.tables.name()))
                                .put(Schemas.Version.javaDeps.name(), attributes.toJsonArray(Schemas.Version.javaDeps.name()));
                    }).collect(Jsons.Collectors.toJsonArray());
            if (logger.isDebugEnabled()) {
                logger.debug("GET : " + id + " -res :" + l.encodePrettily());
            }
            msg.reply(l);
        });

        logger.info("started");
    }
}