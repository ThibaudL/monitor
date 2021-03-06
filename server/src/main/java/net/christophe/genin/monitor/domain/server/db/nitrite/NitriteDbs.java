package net.christophe.genin.monitor.domain.server.db.nitrite;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.tool.ExportOptions;
import org.dizitart.no2.tool.Exporter;
import org.dizitart.no2.tool.Importer;
import rx.Observable;
import rx.Single;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public final class NitriteDbs {

    public static final NitriteDbs instance = new NitriteDbs();

    private Nitrite db;

    public static String newId() {
        return UUID.randomUUID().toString();
    }

    private NitriteDbs() {
    }

    public <T> ObjectRepository<T> repository(Class<T> clazz) {
        return db.getRepository(clazz);
    }


    private static JsonObject toJson(Document doc) {
        return doc.keySet()
                .parallelStream()
                .map(key -> new JsonObject().put(key, doc.get(key)))
                .reduce(JsonObject::mergeIn).orElse(new JsonObject());
    }

    public static JsonArray toArray(List<Document> list) {
        return new JsonArray(list.stream()
                .map(NitriteDbs::toJson)
                .collect(Collectors.toList()));
    }

    public static Single<Integer> removeAll(NitriteCollection collection){
        return Single.fromCallable(() -> {
            Long size = collection.size();
            collection.drop();
            return size.intValue();
        });
    }

    public NitriteDbs build(String dbPath, String user, String pwd) {
        this.db = Nitrite.builder()
                .compressed()
                .filePath(dbPath)
                .openOrCreate(user, pwd);
        this.db.compact();
        return this;
    }


    public NitriteCollection getCollection(String name) {
        return nitrite().getCollection(name);
    }

    public Nitrite nitrite() {
        return Optional.ofNullable(db)
                .orElseThrow(() -> new IllegalStateException("Nitrite not found"));
    }

    public Observable<String> exporter() {
        return Observable.fromCallable(() -> Exporter.of(nitrite()))
                .map(exporter -> {
                    ExportOptions options = new ExportOptions();
                    options.setExportData(true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    exporter.withOptions(options).exportTo(stream);
                    return stream;
                })
                .map(baos -> new String(baos.toByteArray()));
    }

    public boolean importFrom(JsonObject json) {
        String str = json.encode();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes());
        Importer.of(nitrite())
                .importFrom(byteArrayInputStream);
        return true;
    }

    public void close() {
        Optional.ofNullable(db).ifPresent(Nitrite::close);
    }

    public static class Attributes {
        private final Document document;

        public Attributes(Document document) {
            this.document = document;
        }

        @SuppressWarnings("unchecked")
        public <T> List<T> toList(String attr) {
            return Optional.ofNullable(document.get(attr, List.class)).orElse(Collections.emptyList());
        }

        public JsonArray toJsonArray(String attr) {
            return new JsonArray(toList(attr));
        }
    }
}
