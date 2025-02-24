/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the
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

package software.amazon.kinesis.common;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.processor.MultiStreamTracker;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;
import software.amazon.kinesis.processor.SingleStreamTracker;
import software.amazon.kinesis.processor.StreamTracker;
import software.amazon.kinesis.utils.MockObjectHelper;

@RunWith(MockitoJUnitRunner.class)
public class ConfigsBuilderTest {

    private final KinesisAsyncClient mockKinesisClient = MockObjectHelper.createKinesisClient();

    @Mock
    private DynamoDbAsyncClient mockDynamoClient;

    @Mock
    private CloudWatchAsyncClient mockCloudWatchClient;

    @Mock
    private ShardRecordProcessorFactory mockShardProcessorFactory;

    private static final String APPLICATION_NAME = ConfigsBuilderTest.class.getSimpleName();
    private static final String WORKER_IDENTIFIER = "worker-id";

    @Test
    public void testTrackerConstruction() {
        final String streamName = "single-stream";
        final ConfigsBuilder configByName = createConfig(streamName);
        final ConfigsBuilder configBySingleTracker = createConfig(new SingleStreamTracker(streamName));

        for (final ConfigsBuilder cb : Arrays.asList(configByName, configBySingleTracker)) {
            assertEquals(Optional.empty(), cb.appStreamTracker().left());
            assertEquals(streamName, cb.appStreamTracker().right().get());
            assertEquals(streamName, cb.streamTracker().streamConfigList().get(0).streamIdentifier().streamName());
            assertFalse(cb.streamTracker().isMultiStream());
        }

        final StreamTracker mockMultiStreamTracker = mock(MultiStreamTracker.class);
        final ConfigsBuilder configByMultiTracker = createConfig(mockMultiStreamTracker);
        assertEquals(Optional.empty(), configByMultiTracker.appStreamTracker().right());
        assertEquals(mockMultiStreamTracker, configByMultiTracker.appStreamTracker().left().get());
        assertEquals(mockMultiStreamTracker, configByMultiTracker.streamTracker());
    }

    private ConfigsBuilder createConfig(String streamName) {
        // intentional invocation of constructor where streamName is a String
        return new ConfigsBuilder(streamName, APPLICATION_NAME, mockKinesisClient, mockDynamoClient,
                mockCloudWatchClient, WORKER_IDENTIFIER, mockShardProcessorFactory);
    }

    private ConfigsBuilder createConfig(StreamTracker streamTracker) {
        return new ConfigsBuilder(streamTracker, APPLICATION_NAME, mockKinesisClient, mockDynamoClient,
                mockCloudWatchClient, WORKER_IDENTIFIER, mockShardProcessorFactory);
    }

}