package sttp.tapir.serverless.aws.lambda

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.interceptor.decodefailure.{DecodeFailureHandler, DefaultDecodeFailureHandler}
import sttp.tapir.server.interceptor.metrics.MetricsRequestInterceptor
import sttp.tapir.server.tests.{ServerBasicTests, ServerMetricsTest, TestServerInterpreter}
import sttp.tapir.serverless.aws.lambda.AwsLambdaCreateServerStubTest.catsMonadIO
import sttp.tapir.tests.{Port, Test, TestSuite}

class AwsLambdaStubHttpTest extends TestSuite {
  override def tests: Resource[IO, List[Test]] = Resource.eval(
    IO.pure {
      val createTestServer = new AwsLambdaCreateServerStubTest
      new ServerBasicTests(createTestServer, AwsLambdaStubHttpTest.testServerInterpreter)(catsMonadIO).tests() ++
        new ServerMetricsTest(createTestServer).tests()
    }
  )
}

object AwsLambdaStubHttpTest {
  private val testServerInterpreter = new TestServerInterpreter[IO, Any, Route[IO]] {
    override def route[A, U, I, E, O](
        e: ServerEndpoint[A, U, I, E, O, Any, IO],
        decodeFailureHandler: Option[DecodeFailureHandler],
        metricsInterceptor: Option[MetricsRequestInterceptor[IO]]
    ): Route[IO] = {
      val serverOptions: AwsServerOptions[IO] = AwsServerOptions
        .customInterceptors[IO]
        .metricsInterceptor(metricsInterceptor)
        .decodeFailureHandler(decodeFailureHandler.getOrElse(DefaultDecodeFailureHandler.handler))
        .options
        .copy(encodeResponseBody = false)

      AwsCatsEffectServerInterpreter(serverOptions).toRoute(e)
    }

    override def route[A, U, I, E, O](es: List[ServerEndpoint[A, U, I, E, O, Any, IO]]): Route[IO] =
      AwsCatsEffectServerInterpreter[IO]().toRoute(es)

    override def server(routes: NonEmptyList[Route[IO]]): Resource[IO, Port] = ???
  }
}
