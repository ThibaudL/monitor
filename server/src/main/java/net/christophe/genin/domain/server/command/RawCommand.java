package net.christophe.genin.domain.server.command;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.christophe.genin.domain.server.db.Schemas;
import net.christophe.genin.domain.server.db.nitrite.Dbs;
import net.christophe.genin.domain.server.model.Raw;
import org.dizitart.no2.NitriteCollection;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.stream.Collectors;

public class RawCommand extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(RawCommand.class);

    public static final String SAVING = RawCommand.class.getName() + ".saving";
    public static final String CLEAR_CALCULATE_DATA = RawCommand.class.getName() + ".clear.calculate.data";


    @Override
    public void start() {
        logger.info("start RawCommand Verticle");
        vertx.eventBus().<JsonObject>consumer(SAVING, rc -> {
            final JsonObject body = rc.body();
            Raw.save(body)
                    .subscribe(
                            rc::reply,
                            err -> {
                                logger.error("Error in saving events", err);
                                rc.fail(500, "Error in saving events");
                            }
                    );
        });
        vertx.eventBus().consumer(CLEAR_CALCULATE_DATA, rc -> {
            //TODO
            /*
            Observable<String> listIdVersions = Observable.from(Dbs.instance.getCollection(Schemas.Projects.collection())
                    .find()
                    .toList()
                    .stream()
                    .map(d -> d.get(Schemas.Projects.id.name(), String.class))
                    .map(Schemas.Version::collection)
                    .collect(Collectors.toList()));

            Observable<String> others = Observable.just(Schemas.Apis.collection(), Schemas.Projects.collection(), Schemas.Tables.collection());

            Observable.concat(listIdVersions, others)
                    .observeOn(Schedulers.io())
                    .map(idCollection -> {
                        NitriteCollection collection = Dbs.instance.getCollection(idCollection);
                        long size = collection.size();
                        collection.drop();
                        return new JsonObject().put("collectionName", idCollection).put("size", size);
                    })
                    .reduce(new JsonArray(), JsonArray::add)
                    .subscribe(
                            rc::reply,
                            err -> {
                                logger.error("Error in clearing in calculating data", err);
                                rc.fail(500, "Error in clearing in calculating data");
                            });
                            */
        });

    }
}
