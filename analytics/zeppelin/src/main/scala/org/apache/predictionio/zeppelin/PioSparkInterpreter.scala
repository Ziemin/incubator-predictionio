/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.predictionio.zeppelin

import java.util.Properties
import org.slf4j.LoggerFactory

import org.apache.zeppelin.interpreter.Interpreter
import org.apache.zeppelin.interpreter.InterpreterResult
import org.apache.zeppelin.interpreter.InterpreterContext
import org.apache.zeppelin.interpreter.WrappedInterpreter
import org.apache.zeppelin.interpreter.Interpreter.FormType
import org.apache.zeppelin.scheduler.Scheduler
import org.apache.zeppelin.spark.SparkInterpreter

class PioSparkInterpreter(props: Properties) extends Interpreter(props) {

  private val log = LoggerFactory.getLogger(classOf[PioSparkInterpreter])
  private val pioContext = PioZeppelinContext()

  override def open(): Unit = {
    val sparkInterpreter = getSparkInterpreter
    sparkInterpreter.open()

    val sparkInterpreterLock = classOf[SparkInterpreter].getDeclaredField("sharedInterpreterLock")
    sparkInterpreterLock.setAccessible(true)
    sparkInterpreter.synchronized {
      sparkInterpreterLock.get(sparkInterpreter).synchronized {

        val interpretMethod = classOf[SparkInterpreter].getDeclaredMethod("interpret", classOf[String])
        interpretMethod.setAccessible(true)
        interpretMethod.invoke(
          sparkInterpreter,
          "@transient val _pioBinds = new collection.mutable.HashMap[String, Any]()")
        val pioBinds = sparkInterpreter.getLastObject.asInstanceOf[collection.mutable.HashMap[String, Any]];
        pioBinds("pio") = pioContext
        interpretMethod.invoke(
          sparkInterpreter,
          "import org.apache.predictionio.zeppelin.PioZeppelinContext")
        interpretMethod.invoke(
          sparkInterpreter,
          "@transient val pio = _pioBinds(\"pio\").asInstanceOf[PioZeppelinContext]")
      }
    }
  }


  override def close(): Unit = getSparkInterpreter.close()

   /**
   * Run code and return result, in synchronous way.
   *
   * @param st statements to run
   * @param context
   * @return
   */
  override def interpret(st: String, context: InterpreterContext): InterpreterResult = {
    val sparkInterpreter = getSparkInterpreter
    sparkInterpreter.interpret(st, context)
  }

  /**
   * Optionally implement the canceling routine to abort interpret() method
   *
   * @param context
   */
  override def cancel(context: InterpreterContext): Unit =
    getSparkInterpreter.cancel(context)

  /**
   * Dynamic form handling
   * see http://zeppelin.apache.org/docs/dynamicform.html
   *
   * @return FormType.SIMPLE enables simple pattern replacement (eg. Hello ${name=world}),
   *         FormType.NATIVE handles form in API
   */
  override def getFormType(): FormType = FormType.SIMPLE

  /**
   * get interpret() method running process in percentage.
   *
   * @param context
   * @return number between 0-100
   */
  override def getProgress(context: InterpreterContext): Int =
    getSparkInterpreter.getProgress(context)

  override def getScheduler(): Scheduler =
    getSparkInterpreter.getScheduler

  private def getSparkInterpreter(): SparkInterpreter = {
    var p = getInterpreterInTheSameSessionByClassName(classOf[SparkInterpreter].getName())
    if (p == null) {
      return null;
    }

    while (p.isInstanceOf[WrappedInterpreter]) {
      p = p.asInstanceOf[WrappedInterpreter].getInnerInterpreter()
    }
    if (p != null) {
      p.asInstanceOf[SparkInterpreter]
    } else {
      null
    }
  }
}
