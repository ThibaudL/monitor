package net.christophe.genin.monitor.domain.server.command;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;
import net.christophe.genin.monitor.domain.server.Database;
import net.christophe.genin.monitor.domain.server.ReadJsonFiles;
import net.christophe.genin.monitor.domain.server.command.util.RawsTest;
import net.christophe.genin.monitor.domain.server.model.Raw;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@RunWith(VertxUnitRunner.class)
public class RawCommandTest implements ReadJsonFiles {

    private JsonObject data;
    public static final String PATH_DB = "target/testRawCommandTest.db";
    Vertx vertx;


    @Before
    public void before(TestContext context) throws Exception {
        Files.deleteIfExists(Paths.get(new File(PATH_DB).toURI()));
        JsonObject config = new JsonObject().put("nitritedb", new JsonObject().put("path", PATH_DB));
        DeploymentOptions options = new DeploymentOptions()
                .setConfig(config);

        vertx = Vertx.vertx();
        Async async = context.async(2);
        vertx.deployVerticle(Database.class.getName(), options, (result) -> {
            context.assertTrue(result.succeeded());
            async.countDown();
        });
        vertx.deployVerticle(RawCommand.class.getName(), options, (r) -> {
            context.assertTrue(r.succeeded());
            async.countDown();
        });


        data = load("/datas/projects-1.json");

    }


    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void should_save_raw_datas(TestContext context) {
        Async async = context.async(2);
        vertx.eventBus().<Long>send(RawCommand.SAVING, data, msg -> {
            context.assertTrue(msg.succeeded());
            Long id = msg.result().body();
            context.assertTrue(id > 1L);
            async.complete();

            Raw.findAll().subscribe(r -> {

                context.assertEquals("societe-service-impl", r.artifactId());
                context.assertEquals(Treatments.PROJECTS, r.state());
                context.assertTrue(r.update() > 1L);
                context.assertEquals(data, r.json());
                async.countDown();
            });
        });
    }
}
