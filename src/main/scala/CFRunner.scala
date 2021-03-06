import io.circe.Printer
import io.circe.syntax._
import typeformation.cf.{Encoding, Template}
import typeformation.cf.Encoding._

import scala.compat.java8.FutureConverters._
import software.amazon.awssdk.services.cloudformation.CloudFormationAsyncClient
import software.amazon.awssdk.services.cloudformation.model.{CreateStackRequest, Parameter}

import scala.concurrent.{ExecutionContext, Future}

class CFRunner {
  private val client = CloudFormationAsyncClient.create()

  def createStack(stackName: String, template: Template, params: Map[String, String])
                 (implicit ec: ExecutionContext): Future[Unit] = {

    val parameters = params.map { case (k, v) =>
      Parameter.builder().parameterKey(k).parameterValue(v).build()
    }.toSeq

    val templateBody = template.asJson.pretty(Printer.spaces2.copy(dropNullKeys = true))

    val req =
      CreateStackRequest.builder()
        .stackName(stackName)
        .parameters(parameters: _*)
        .templateBody(templateBody)
        .disableRollback(true)
        .timeoutInMinutes(3)
        .build()

    client.createStack(req).toScala.map(resp =>
      println(resp)
    ).onComplete { _ =>
      client.close()
    }
  }
}
