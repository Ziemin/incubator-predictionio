name := "zeppelin-predictionio"

libraryDependencies ++= Seq(
  "org.slf4j"           % "slf4j-log4j12"  % "1.7.18" % "provided",
  "org.apache.zeppelin" % "zeppelin-interpreter" % "0.6.1" % "provided",
  "org.apache.zeppelin" %% "zeppelin-spark" % "0.6.1" % "provided")

parallelExecution in Test := false

excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter { _.data.getName match {
    case "asm-3.1.jar" => true
    case "commons-beanutils-1.7.0.jar" => true
    case "reflectasm-1.10.1.jar" => true
    case "commons-beanutils-core-1.8.0.jar" => true
    case "kryo-3.0.3.jar" => true
    case "slf4j-log4j12-1.7.5.jar" => true
    case _ => false
  }}
}

assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("org.objenesis.**" -> "shadeio.@1").inLibrary("com.esotericsoftware.kryo" % "kryo" % "2.21").inProject,
  ShadeRule.rename("com.esotericsoftware.reflectasm.**" -> "shadeio.@1").inLibrary("com.esotericsoftware.kryo" % "kryo" % "2.21").inProject,
  ShadeRule.rename("com.esotericsoftware.minlog.**" -> "shadeio.@1").inLibrary("com.esotericsoftware.kryo" % "kryo" % "2.21").inProject
)

// skip test in assembly
test in assembly := {}

outputPath in assembly := baseDirectory.value.getAbsoluteFile.getParentFile.getParentFile /
  "assembly" / ("pio-zeppelin-" + version.value + ".jar")

cleanFiles <+= baseDirectory { base => base.getParentFile / "assembly" }
