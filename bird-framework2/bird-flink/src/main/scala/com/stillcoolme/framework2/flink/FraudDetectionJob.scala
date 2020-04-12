/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stillcoolme.framework2.flink

import org.apache.flink.streaming.api.scala._
import org.apache.flink.walkthrough.common.sink.AlertSink
import org.apache.flink.walkthrough.common.entity.Alert
import org.apache.flink.walkthrough.common.entity.Transaction
import org.apache.flink.walkthrough.common.source.TransactionSource

/**
  * Skeleton code for the DataStream code walkthrough
  * 官方的 DataStream API 的 诈骗检测作业 例子
  */
object FraudDetectionJob {

  @throws[Exception]
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    // Creating a Source
    val transactions: DataStream[Transaction] = env
      .addSource(new TransactionSource)
      .name("transactions")
    // Partitioning Events & Detecting Fraud 诈骗检测
    val alerts: DataStream[Alert] = transactions
      .keyBy(transaction => transaction.getAccountId)
      .process(new FraudDetector)
      .name("fraud-detector")
    // Outputting Results
    alerts
      .addSink(new AlertSink)
      .name("send-alerts")

    env.execute("Fraud Detection")
  }
}
