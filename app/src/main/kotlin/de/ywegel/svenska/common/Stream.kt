package de.ywegel.svenska.common

import java.util.stream.Stream

fun <T> streamOf(vararg elements: T): Stream<T> = Stream.of<T>(*elements)
