import io.javalin.Javalin
import io.javalin.http.sse.SseClient
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.vertx.core.json.JsonObject
import io.vertx.pgclient.PgConnectOptions
import io.vertx.rxjava3.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

fun main() {
    startServer()
//    test()
}

fun test() {
    // connect to the database
    val db = getDatabaseConnection()
    getStream(1377561600, "pir", db).subscribe {
        println("it = ${it}")
    }
    Thread.sleep(100000)
}

fun startServer() {
    // setup server
    val app = Javalin.create().start(7000)

    // connect to the database
    val db = getDatabaseConnection()

    // hello world path
    app.get("/") { ctx -> ctx.result("Hello World") }

    // the tables we want to stream
    val tables = arrayOf("humidity", "pir", "temperature")

    // figure out the first timestamp in the database
    val firstTimestamp = getFirstTimestamp(db).blockingGet() + 60
    // we will use this offset throughout the server's life
    val serverStart = System.currentTimeMillis() / 1000

    // sse path
    val clients = ConcurrentLinkedQueue<SseClient>()
    app.sse("/sse") { client ->
        // handle the client setup
        println("client = ${client.ctx.req.pathInfo}")
        clients.add(client)

        // each client should get the same view on the data, regardless of when they connected
        // the start of the stream should thus be dependent on the current time
        var streamStart = (System.currentTimeMillis() / 1000) - serverStart + firstTimestamp

        // does the client override the firsttimestamp?
        client.ctx.queryParam("start")?.let { userStart ->
            streamStart = userStart.toLong()
        }

        println("This server 'starts' on timestamp $streamStart. This means that the first event returned will correspond to this timestamp. After waiting an hour, you will see events 1 hour in the future, based on the same timestamp. Pass query parameter 'start' to override this behaviour and start where you want.")

        // start sending data to the client
        val disposables = tables.map { table ->
            getStream(streamStart, table, db)
                .doOnError {
                    println("observable errors = ${it}")
                }
                .subscribe {
                    client.sendEvent("measurement", it.encode())
                }
        }

        // clean up after client leaves
        client.onClose {
            println("closing client")
            disposables.forEach { it.dispose() }
            clients.remove(client)
        }
    }
}

fun getFirstTimestamp(db: PgPool): Maybe<Long> {
    return db
        .rxWithConnection { conn ->
            conn.query("select min(timestamp) timestamp from pir").rxExecute().toMaybe()
                .map {
                    val integer = it.first().getLong("timestamp")
                    println("it = ${integer}")
                    integer
                }
        }
}

fun getStream(startTime: Long, table: String, db: PgPool): Flowable<JsonObject> {
    // figure out the 'start' of the dataset
    val period: Long = 2
    return Flowable
        .interval(0, period, TimeUnit.SECONDS)
        .flatMap { counter ->
            db.rxWithConnection { conn ->
                val query = """select timestamp, measurement, room.name room
                            from $table inner join room using (idroom)
                            where timestamp >= ${startTime + (counter * period)} and timestamp < ${startTime + ((counter + 1) * period)} 
                            order by timestamp"""
                conn.query(query).rxExecute().toMaybe()
            }
                .flatMapObservable { Observable.fromIterable(it.asIterable()) }
                .map { row ->
                    JsonObject()
                        .put("measurement", table)
                        .put("timestamp", row.getInteger("timestamp"))
                        .put("value", row.getFloat("measurement"))
                        .put("room", row.getString("room"))
                }
                .doOnNext { println("producing json = ${it.encode()}") }
                .toFlowable(BackpressureStrategy.DROP)
        }
}

fun getDatabaseConnection(): PgPool {
    // for these credentials, check out the docker-compose.yml file that starts up the database itself
    val connectOptions = PgConnectOptions()
        .setPort(5490)
        .setHost("localhost")
        .setDatabase("postgres")
        .setUser("postgres")
        .setPassword("neebai9izooHio4athie6ahj0haiph")

    // Pool options
    val poolOptions = PoolOptions()
        .setMaxSize(5)

    return PgPool.pool(connectOptions, poolOptions)
}

