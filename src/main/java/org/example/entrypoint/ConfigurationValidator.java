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
package org.example.entrypoint;

import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.statefun.flink.core.StatefulFunctionsConfig;
import org.apache.flink.statefun.flink.core.exceptions.StatefulFunctionsInvalidConfigException;
import org.apache.flink.statefun.flink.core.message.MessageFactoryType;
import org.apache.flink.util.StringUtils;

public final class ConfigurationValidator {

  private ConfigurationValidator() {}

  static void validate(Configuration configuration) {
    validateCustomPayloadSerializerClassName(configuration);
    validateNoHeapBackedTimers(configuration);
    validateUnalignedCheckpointsDisabled(configuration);
  }

  private static void validateCustomPayloadSerializerClassName(Configuration configuration) {

    MessageFactoryType factoryType =
        configuration.get(StatefulFunctionsConfig.USER_MESSAGE_SERIALIZER);
    String customPayloadSerializerClassName =
        configuration.get(StatefulFunctionsConfig.USER_MESSAGE_CUSTOM_PAYLOAD_SERIALIZER_CLASS);

    if (factoryType == MessageFactoryType.WITH_CUSTOM_PAYLOADS) {
      if (StringUtils.isNullOrWhitespaceOnly(customPayloadSerializerClassName)) {
        throw new StatefulFunctionsInvalidConfigException(
            StatefulFunctionsConfig.USER_MESSAGE_CUSTOM_PAYLOAD_SERIALIZER_CLASS,
            "custom payload serializer class must be supplied with WITH_CUSTOM_PAYLOADS serializer");
      }
    } else {
      if (customPayloadSerializerClassName != null) {
        throw new StatefulFunctionsInvalidConfigException(
            StatefulFunctionsConfig.USER_MESSAGE_CUSTOM_PAYLOAD_SERIALIZER_CLASS,
            "custom payload serializer class may only be supplied with WITH_CUSTOM_PAYLOADS serializer");
      }
    }
  }

  private static final ConfigOption<String> TIMER_SERVICE_FACTORY =
      ConfigOptions.key("state.backend.rocksdb.timer-service.factory")
          .stringType()
          .defaultValue("rocksdb");

  private static final ConfigOption<Boolean> ENABLE_UNALIGNED_CHECKPOINTS =
      ConfigOptions.key("execution.checkpointing.unaligned").booleanType().defaultValue(false);

  private static void validateNoHeapBackedTimers(Configuration configuration) {
    final String timerFactory = configuration.getString(TIMER_SERVICE_FACTORY);
    if (!timerFactory.equalsIgnoreCase("rocksdb")) {
      throw new StatefulFunctionsInvalidConfigException(
          TIMER_SERVICE_FACTORY,
          "StateFun only supports non-heap timers with a rocksdb state backend.");
    }
  }

  private static void validateUnalignedCheckpointsDisabled(Configuration configuration) {
    final boolean unalignedCheckpoints = configuration.getBoolean(ENABLE_UNALIGNED_CHECKPOINTS);
    if (unalignedCheckpoints) {
      throw new StatefulFunctionsInvalidConfigException(
          ENABLE_UNALIGNED_CHECKPOINTS,
          "StateFun currently does not support unaligned checkpointing.");
    }
  }
}
