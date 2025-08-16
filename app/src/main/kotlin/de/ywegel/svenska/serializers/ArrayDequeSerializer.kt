package de.ywegel.svenska.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ArrayDequeSerializer : KSerializer<ArrayDeque<String>> {
    private val delegateSerializer = ListSerializer(String.Companion.serializer())

    override val descriptor: SerialDescriptor = delegateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ArrayDeque<String>) {
        delegateSerializer.serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): ArrayDeque<String> {
        return ArrayDeque(delegateSerializer.deserialize(decoder))
    }
}
