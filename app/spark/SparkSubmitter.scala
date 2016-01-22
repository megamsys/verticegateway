package spark

import scalaz._
import Scalaz._
import scalaz.Validation
import scalaz.Validation.FlatMap._
import io.megam.gradle._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import controllers.Constants._
import java.nio.charset.Charset
import controllers.funnel.FunnelErrors._
import org.megam.common.jsonscalaz._

import com.stackmob.newman._
import com.stackmob.newman.response._
import com.stackmob.newman.dsl._
import scala.concurrent.Await
import scala.concurrent._
import scala.concurrent.duration._
import java.net.URL

case class JobResult(message: String, job_id: String, context: String, errorClass: String)
case class JobSubmitted(code: Int, status: String, resultstr: String, result: JobResult)
object cons{
  val WB_SPARKJOBSERVER_INPUT = "WB_SPARKJOBSERVER_INPUT"
}

class SparkSubmitter(ospark: models.analytics.SparkjobsInput) {
  implicit val formats = DefaultFormats

  def build =  YonpiProject(new org.megam.common.git.GitRepo(controllers.Constants.OJA_YONPI_DIR, ospark.source)).buildJar()

  def submit(clean: Boolean = false, email: String, args: Map[String, String]): ValidationNel[Throwable, Option[Tuple2[String,JobSubmitted]]] = {
    for {
      bu <- build
      sa <- new spark.JarUpload(new JarsInput(email, bu.jar.getAbsolutePath, bu.name)).run.successNel
      ss <- new spark.JarSubmit(new JarsInput(email, bu.jar.getAbsolutePath, bu.name, args)).run.successNel
    } yield {
      play.api.Logger.debug("%-20s -->[%s]".format("JAR",  sa))
      play.api.Logger.debug("%-20s -->[%s]".format(ss.get._1, ss.get))
      (ss.get._2, JobSubmittedSerialization.decode(ss.get._1, ss.get._3)).some
    }
  }
  def wbsubmit(clean: Boolean = false, email: String, args: Map[String, String]): ValidationNel[Throwable, Option[Tuple2[String,JobSubmitted]]] = {
      for {
         ss <- new spark.wbSubmit(new JarsInput(email, "", "", args)).run.successNel
    } yield {
      (ss.get._2, JobSubmittedSerialization.decode(ss.get._1, ss.get._3)).some
    }
  }
  def job(job_id: String): ValidationNel[Throwable, Option[String]] = {
    val out = new spark.JobResults(JobsInput(job_id)).run
    play.api.Logger.debug("%-20s -->[%s]".format("JOB",  out))
    out.successNel
  }

}

object SparkSubmitter {
  def apply(a: models.analytics.SparkjobsInput): SparkSubmitter = new SparkSubmitter(a)
  def empty: SparkSubmitter = new SparkSubmitter(new models.analytics.SparkjobsInput("","", models.tosca.KeyValueList.empty))
}
