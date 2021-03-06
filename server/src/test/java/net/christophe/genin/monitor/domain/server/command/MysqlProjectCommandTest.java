package net.christophe.genin.monitor.domain.server.command;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;
import net.christophe.genin.monitor.domain.server.Database;
import net.christophe.genin.monitor.domain.server.base.DbWithSchemaTest;
import net.christophe.genin.monitor.domain.server.ReadJsonFiles;
import net.christophe.genin.monitor.domain.server.base.DbTest;
import net.christophe.genin.monitor.domain.server.base.NitriteDBManagemementTest;
import net.christophe.genin.monitor.domain.server.db.mysql.Mysqls;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MysqlProjectCommandTest extends DbWithSchemaTest implements ReadJsonFiles {

    private static DeploymentOptions options;
    private JsonObject data;


    Vertx vertx;

    @BeforeClass
    public static void first() throws Exception {
        options = new NitriteDBManagemementTest(MysqlProjectCommandTest.class).deleteAndGetOption();
    }

    @Before
    public void before(TestContext context) throws Exception {

        vertx = Vertx.vertx();
        Async async = context.async();
        vertx.deployVerticle(Database.class.getName(), options, callbackForSchemaCreation(vertx, context, async));

        data = load("/datas/projects-1.json");
    }



    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void should_create_project_if_update_time_is_after_0(TestContext context) {
        Async async = context.async(3);

        ProjectCommandTest.MockRaw raw = new ProjectCommandTest.MockRaw(context, async, 500, data);
        ProjectCommandTest.create_project_if_update_time_is_after_0(context, async, raw);
    }

    @Test
    public void should_create_project_if_update_time_is_before_0(TestContext context) {
        ProjectCommandTest.create_project_if_update_time_is_before_0(context);
    }

}
