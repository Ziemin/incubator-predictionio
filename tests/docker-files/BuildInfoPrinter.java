import org.apache.predictionio.core.BuildInfo;
import java.util.Map;
import java.lang.Iterable;

class BuildInfoPrinter {
  public static void main(String[] args) {
    System.out.println("PIO_VERSION=" + BuildInfo.version());
    System.out.println("PIO_SCALA_VERSION=" + BuildInfo.scalaVersion());
    System.out.println("PIO_SBT_VERSION=" + BuildInfo.sbtVersion());
    System.out.println("PIO_SPARK_VERSION=" + BuildInfo.sparkVersion());
    System.out.println("PIO_HADOOP_VERSION=" + BuildInfo.hadoopVersion());
  }
}
