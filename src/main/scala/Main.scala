import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import sttp.capabilities.zio.ZioStreams
import sttp.model.{Header, StatusCode}
import sttp.tapir.server.vertx.zio.VertxZioServerInterpreter
import sttp.tapir.server.vertx.zio.VertxZioServerInterpreter.VertxFutureToRIO
import sttp.tapir.ztapir._
import sttp.tapir.{CodecFormat, endpoint, statusCode, streamBinaryBody}
import zio.stream.ZStream
import zio.{ZIO, ZIOAppDefault}

import scala.concurrent.duration.DurationInt

object Main extends ZIOAppDefault {
  override def run = {
    val downloadEndpoint =
      endpoint
        .in("download")
        .get
        .out(
          streamBinaryBody(ZioStreams)(CodecFormat.OctetStream()).toEndpointIO
            .and(statusCode(StatusCode.Ok)
            .and(header(Header.contentLength(50))))
        )
        .zServerLogic { _ =>
          ZIO.succeed(ZStream.never.interruptAfter(zio.Duration.fromScala(1.second)))
        }

    val downloadFailEndpoint =
      endpoint
        .in("download-fail")
        .get
        .out(
          streamBinaryBody(ZioStreams)(CodecFormat.OctetStream()).toEndpointIO
            .and(statusCode(StatusCode.Ok)
              .and(header(Header.contentLength(50))))
        )
        .zServerLogic { _ =>
          ZIO.succeed(ZStream.fail(new RuntimeException("Fail stream")))
        }


    val testEndpoint = endpoint.get.in("test").out(stringBody).zServerLogic(_ => ZIO.succeed("Server works"))

    ZIO.scoped(
      ZIO
        .acquireRelease(
          ZIO.attempt {
            val vertx = Vertx.vertx()
            val server = vertx.createHttpServer()
            val router = Router.router(vertx)
            val interpreter = VertxZioServerInterpreter[Any]()
            interpreter.route(downloadEndpoint)(zio.Runtime.default)(router)
            interpreter.route(downloadFailEndpoint)(zio.Runtime.default)(router)
            interpreter.route(testEndpoint)(zio.Runtime.default)(router)
            server.requestHandler(router).listen(8080)
          }.flatMap(_.asRIO)
        ) { server =>
          ZIO.attempt(server.close()).flatMap(_.asRIO).orDie
        } *> ZIO.succeed(println("Server started on 8080")) *> ZIO.never
    )
  }
}
