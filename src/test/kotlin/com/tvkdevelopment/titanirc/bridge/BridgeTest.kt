package com.tvkdevelopment.titanirc.bridge

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformationLink
import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformationMapping
import com.tvkdevelopment.titanirc.niceMockk
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.Test

class BridgeTest {

    private val mockClient1MessageListeners = mutableListOf<BridgeClient.MessageListener>()
    private val mockClient2MessageListeners = mutableListOf<BridgeClient.MessageListener>()
    private val mockClient3MessageListeners = mutableListOf<BridgeClient.MessageListener>()
    private val mockClient1TopicListeners = mutableListOf<BridgeClient.TopicListener>()
    private val mockClient2TopicListeners = mutableListOf<BridgeClient.TopicListener>()
    private val mockClient3TopicListeners = mutableListOf<BridgeClient.TopicListener>()

    private val mockClient1 = mockClient(mockClient1MessageListeners, mockClient1TopicListeners)
    private val mockClient2 = mockClient(mockClient2MessageListeners, mockClient2TopicListeners)
    private val mockClient3 = mockClient(mockClient3MessageListeners, mockClient3TopicListeners)

    private fun mockClient(
        messageListeners: MutableList<BridgeClient.MessageListener>,
        topicListeners: MutableList<BridgeClient.TopicListener>,
    ) =
        niceMockk<BridgeClient> {
            every { addRelayMessageListener(capture(messageListeners)) } just Runs
            every { addTopicListener(capture(topicListeners)) } just Runs
        }

    @Test
    fun testRelayMessage() {
        // GIVEN
        Bridge.connect(
            ChannelMapping(
                ChannelLink(mockClient1 to "a1", mockClient2 to "a2"),
            ),
        )

        // WHEN
        mockClient1MessageListeners.forEach { it.onMessage("a1", "nick", "message") }

        // THEN
        verify(exactly = 0) { mockClient1.relayMessage(any(), any(), any()) }
        verify { mockClient2.relayMessage("a2", "nick", "message") }
    }

    @Test
    fun testRelayMessageToMultipleClients() {
        // GIVEN
        Bridge.connect(
            ChannelMapping(
                ChannelLink(mockClient1 to "a1", mockClient2 to "a2", mockClient3 to "a3"),
            ),
        )

        // WHEN
        mockClient1MessageListeners.forEach { it.onMessage("a1", "nick", "message") }

        // THEN
        verify(exactly = 0) { mockClient1.relayMessage(any(), any(), any()) }
        verify { mockClient2.relayMessage("a2", "nick", "message") }
        verify { mockClient3.relayMessage("a3", "nick", "message") }
    }

    @Test
    fun testRelayMessageToSelectClient() {
        // GIVEN
        Bridge.connect(
            ChannelMapping(
                ChannelLink(mockClient1 to "a1", mockClient2 to "a2", mockClient3 to "a3"),
                ChannelLink(mockClient1 to "b1", mockClient2 to "b2"),
            ),
        )

        // WHEN
        mockClient1MessageListeners.forEach { it.onMessage("b1", "nick", "message") }

        // THEN
        verify(exactly = 0) { mockClient1.relayMessage(any(), any(), any()) }
        verify { mockClient2.relayMessage("b2", "nick", "message") }
        verify(exactly = 0) { mockClient3.relayMessage(any(), any(), any()) }
    }

    @Test
    fun testRelayMessageDecoupledLink() {
        // GIVEN
        Bridge.connect(
            ChannelMapping(
                ChannelLink(mockClient1 to "a1", mockClient2 to "a2", mockClient3 to "a3"),
                ChannelLink(mockClient1 to "b1", mockClient2 to "b2"),
                ChannelLink(mockClient2 to "b2", mockClient3 to "b3"),
            ),
        )

        // WHEN
        mockClient2MessageListeners.forEach { it.onMessage("b2", "nick", "message") }

        // THEN
        verify { mockClient1.relayMessage("b1", "nick", "message") }
        verify(exactly = 0) { mockClient2.relayMessage(any(), any(), any()) }
        verify { mockClient3.relayMessage("b3", "nick", "message") }
    }

    @Test
    fun testRelayMessageDecoupledLinkToSelectClient() {
        // GIVEN
        Bridge.connect(
            ChannelMapping(
                ChannelLink(mockClient1 to "a1", mockClient2 to "a2", mockClient3 to "a3"),
                ChannelLink(mockClient1 to "b1", mockClient2 to "b2"),
                ChannelLink(mockClient2 to "b2", mockClient3 to "b3"),
            ),
        )

        // WHEN
        mockClient1MessageListeners.forEach { it.onMessage("b1", "nick", "message") }

        // THEN
        verify(exactly = 0) { mockClient1.relayMessage(any(), any(), any()) }
        verify { mockClient2.relayMessage("b2", "nick", "message") }
        verify(exactly = 0) { mockClient3.relayMessage(any(), any(), any()) }
    }

    @Test
    fun testTransformation() {
        // GIVEN
        Bridge.connect(
            ChannelMapping(
                ChannelLink(mockClient1 to "a1", mockClient2 to "a2"),
            ),
            MessageTransformationMapping(
                MessageTransformationLink(
                    mockClient1 to mockClient2,
                    listOf(MessageTransformation { "$it transformed" })
                )
            )
        )

        // WHEN
        mockClient1MessageListeners.forEach { it.onMessage("a1", "nick", "message") }

        // THEN
        verify { mockClient2.relayMessage("a2", "nick", "message transformed") }
    }

    @Test
    fun testTransformationNotInverted() {
        // GIVEN
        Bridge.connect(
            ChannelMapping(
                ChannelLink(mockClient1 to "a1", mockClient2 to "a2"),
            ),
            MessageTransformationMapping(
                MessageTransformationLink(
                    mockClient1 to mockClient2,
                    listOf(MessageTransformation { "$it transformed" })
                )
            )
        )

        // WHEN
        mockClient2MessageListeners.forEach { it.onMessage("a2", "nick", "message") }

        // THEN
        verify { mockClient1.relayMessage("a1", "nick", "message") }
    }

    @Test
    fun testMultipleTransformations() {
        // GIVEN
        Bridge.connect(
            ChannelMapping(
                ChannelLink(mockClient1 to "a1", mockClient2 to "a2"),
            ),
            MessageTransformationMapping(
                MessageTransformationLink(
                    mockClient1 to mockClient2,
                    listOf(
                        MessageTransformation { "$it 1" },
                        MessageTransformation { "$it 2" },
                    )
                )
            )
        )

        // WHEN
        mockClient1MessageListeners.forEach { it.onMessage("a1", "nick", "message") }

        // THEN
        verify { mockClient2.relayMessage("a2", "nick", "message 1 2") }
    }

    @Test
    fun testSyncTopic() {
        // GIVEN
        Bridge.connect(
            ChannelMapping(
                ChannelLink(mockClient1 to "a1", mockClient2 to "a2"),
            ),
        )

        // WHEN
        mockClient1TopicListeners.forEach { it.onTopicChanged("a1", "topic") }

        // THEN
        verify(exactly = 0) { mockClient1.setTopic(any(), any()) }
        verify { mockClient2.setTopic("a2", "topic") }
    }
}