package de.ywegel.svenska.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.LinkedList
import java.util.Queue

object QueueSerializer : KSerializer<Queue<String>> {
    private val listSerializer = ListSerializer(String.serializer())

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "QueueSerializer", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Queue<String> {
        val list = decoder.decodeSerializableValue(listSerializer)
        return LinkedList(list)
    }

    override fun serialize(encoder: Encoder, value: Queue<String>) {
        encoder.encodeSerializableValue(listSerializer, value as LinkedList<String>)
    }
}
