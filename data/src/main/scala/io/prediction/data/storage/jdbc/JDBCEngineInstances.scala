/** Copyright 2015 TappingStone, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package io.prediction.data.storage.jdbc

import grizzled.slf4j.Logging
import io.prediction.data.storage.EngineInstance
import io.prediction.data.storage.EngineInstances
import io.prediction.data.storage.StorageClientConfig
import scalikejdbc._

class JDBCEngineInstances(client: String, config: StorageClientConfig, prefix: String)
  extends EngineInstances with Logging {
  val tableName = JDBCUtils.prefixTableName(prefix, "engineinstances")
  DB autoCommit { implicit session =>
    try {
      sql"""
      create table $tableName (
        id text not null primary key,
        status text not null,
        startTime timestamp not null,
        endTime timestamp not null,
        engineId text not null,
        engineVersion text not null,
        engineVariant text not null,
        engineFactory text not null,
        evaluatorClass text not null,
        batch text not null,
        env text not null,
        sparkConf text not null,
        datasourceParams text not null,
        preparatorParams text not null,
        algorithmsParams text not null,
        servingParams text not null,
        evaluatorParams text not null,
        evaluatorResults text not null,
        evaluatorResultsHTML text not null,
        evaluatorResultsJSON text)""".execute().apply()
    } catch {
      case e: Exception => debug(e.getMessage, e)
    }
  }

  def insert(i: EngineInstance): String = DB localTx { implicit session =>
    try {
      val id = java.util.UUID.randomUUID().toString
      sql"""
      INSERT INTO $tableName VALUES(
        $id,
        ${i.status},
        ${i.startTime},
        ${i.endTime},
        ${i.engineId},
        ${i.engineVersion},
        ${i.engineVariant},
        ${i.engineFactory},
        ${i.evaluatorClass},
        ${i.batch},
        ${JDBCUtils.mapToString(i.env)},
        ${JDBCUtils.mapToString(i.sparkConf)},
        ${i.dataSourceParams},
        ${i.preparatorParams},
        ${i.algorithmsParams},
        ${i.servingParams},
        ${i.evaluatorParams},
        ${i.evaluatorResults},
        ${i.evaluatorResultsHTML},
        ${i.evaluatorResultsJSON})""".update().apply()
      id
    } catch {
      case e: Exception =>
        error(e.getMessage, e)
        ""
    }
  }

  def get(id: String): Option[EngineInstance] = DB localTx { implicit session =>
    try {
      sql"""
      SELECT
        id,
        status,
        startTime,
        endTime,
        engineId,
        engineVersion,
        engineVariant,
        engineFactory,
        evaluatorClass,
        batch,
        env,
        sparkConf,
        datasourceParams,
        preparatorParams,
        algorithmsParams,
        servingParams,
        evaluatorParams,
        evaluatorResults,
        evaluatorResultsHTML,
        evaluatorResultsJSON
      FROM $tableName WHERE id = $id""".map(resultToEngineInstance).
        single().apply()
    } catch {
      case e: Exception =>
        error(e.getMessage, e)
        None
    }
  }

  def getAll(): Seq[EngineInstance] = DB localTx { implicit session =>
    try {
      sql"""
      SELECT
        id,
        status,
        startTime,
        endTime,
        engineId,
        engineVersion,
        engineVariant,
        engineFactory,
        evaluatorClass,
        batch,
        env,
        sparkConf,
        datasourceParams,
        preparatorParams,
        algorithmsParams,
        servingParams,
        evaluatorParams,
        evaluatorResults,
        evaluatorResultsHTML,
        evaluatorResultsJSON
      FROM $tableName""".map(resultToEngineInstance).list().apply()
    } catch {
      case e: Exception =>
        error(e.getMessage, e)
        Seq()
    }
  }

  def getLatestCompleted(
    engineId: String,
    engineVersion: String,
    engineVariant: String): Option[EngineInstance] =
    getCompleted(engineId, engineVersion, engineVariant).headOption

  /** Get all instances that has trained to completion. */
  def getCompleted(
    engineId: String,
    engineVersion: String,
    engineVariant: String): Seq[EngineInstance] = DB localTx { implicit s =>
    try {
      sql"""
      SELECT
        id,
        status,
        startTime,
        endTime,
        engineId,
        engineVersion,
        engineVariant,
        engineFactory,
        evaluatorClass,
        batch,
        env,
        sparkConf,
        datasourceParams,
        preparatorParams,
        algorithmsParams,
        servingParams,
        evaluatorParams,
        evaluatorResults,
        evaluatorResultsHTML,
        evaluatorResultsJSON
      FROM $tableName
      WHERE
        status = 'COMPLETED' AND
        engineId = $engineId AND
        engineVersion = $engineVersion AND
        engineVariant = $engineVariant
      ORDER BY startTime DESC""".
        map(resultToEngineInstance).list().apply()
    } catch {
      case e: Exception =>
        error(e.getMessage, e)
        Seq()
    }
  }

  def getEvalCompleted(): Seq[EngineInstance] = DB localTx { implicit s =>
    try {
      sql"""
      SELECT
        id,
        status,
        startTime,
        endTime,
        engineId,
        engineVersion,
        engineVariant,
        engineFactory,
        evaluatorClass,
        batch,
        env,
        sparkConf,
        datasourceParams,
        preparatorParams,
        algorithmsParams,
        servingParams,
        evaluatorParams,
        evaluatorResults,
        evaluatorResultsHTML,
        evaluatorResultsJSON
      FROM $tableName
      WHERE
        status = 'EVALCOMPLETED'
      ORDER BY startTime DESC""".
        map(resultToEngineInstance).list().apply()
    } catch {
      case e: Exception =>
        error(e.getMessage, e)
        Seq()
    }
  }

  /** Update a EngineInstance. */
  def update(i: EngineInstance): Unit = DB localTx { implicit session =>
    try {
      sql"""
      update $tableName set
        status = ${i.status},
        startTime = ${i.startTime},
        endTime = ${i.endTime},
        engineId = ${i.engineId},
        engineVersion = ${i.engineVersion},
        engineVariant = ${i.engineVariant},
        engineFactory = ${i.engineFactory},
        evaluatorClass = ${i.evaluatorClass},
        batch = ${i.batch},
        env = ${JDBCUtils.mapToString(i.env)},
        sparkConf = ${JDBCUtils.mapToString(i.sparkConf)},
        datasourceParams = ${i.dataSourceParams},
        preparatorParams = ${i.preparatorParams},
        algorithmsParams = ${i.algorithmsParams},
        servingParams = ${i.servingParams},
        evaluatorParams = ${i.evaluatorParams},
        evaluatorResults = ${i.evaluatorResults},
        evaluatorResultsHTML = ${i.evaluatorResultsHTML},
        evaluatorResultsJSON = ${i.evaluatorResultsJSON}
      where id = ${i.id}""".update().apply()
    } catch {
      case e: Exception =>
        error(e.getMessage, e)
        ""
    }
  }

  /** Delete a EngineInstance. */
  def delete(id: String): Unit = DB localTx { implicit session =>
    try {
      sql"DELETE FROM $tableName WHERE id = $id".update().apply()
    } catch {
      case e: Exception =>
        error(e.getMessage, e)
    }
  }

  def resultToEngineInstance(rs: WrappedResultSet): EngineInstance = {
    EngineInstance(
      id = rs.string("id"),
      status = rs.string("status"),
      startTime = rs.jodaDateTime("startTime"),
      endTime = rs.jodaDateTime("endTime"),
      engineId = rs.string("engineId"),
      engineVersion = rs.string("engineVersion"),
      engineVariant = rs.string("engineVariant"),
      engineFactory = rs.string("engineFactory"),
      evaluatorClass = rs.string("evaluatorClass"),
      batch = rs.string("batch"),
      env = JDBCUtils.stringToMap(rs.string("env")),
      sparkConf = JDBCUtils.stringToMap(rs.string("sparkConf")),
      dataSourceParams = rs.string("datasourceParams"),
      preparatorParams = rs.string("preparatorParams"),
      algorithmsParams = rs.string("algorithmsParams"),
      servingParams = rs.string("servingParams"),
      evaluatorParams = rs.string("evaluatorParams"),
      evaluatorResults = rs.string("evaluatorResults"),
      evaluatorResultsHTML = rs.string("evaluatorResultsHTML"),
      evaluatorResultsJSON = rs.string("evaluatorResultsJSON"))
  }
}