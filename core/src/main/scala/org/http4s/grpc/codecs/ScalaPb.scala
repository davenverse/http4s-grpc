package org.http4s.grpc.codecs

import com.google.protobuf.ByteString
import scalapb.GeneratedMessage
import scalapb.GeneratedMessageCompanion
import scalapb.TypeMapper
import scodec.Attempt
import scodec.Codec
import scodec.DecodeResult
import scodec.Decoder
import scodec.Encoder
import scodec.bits.BitVector
import scodec.bits.ByteVector

// Should this be its own subproject?
object ScalaPb {

  private def encoderForGenerated[A <: GeneratedMessage](companion: GeneratedMessageCompanion[A]): Encoder[A] = {
    Encoder[A]((a: A) => Attempt.successful(ByteVector.view(companion.toByteArray(a)).bits))
  }

  private def decoderForGenerated[A <: GeneratedMessage](companion: GeneratedMessageCompanion[A]): Decoder[A] = {
    Decoder[A]((b: BitVector) =>
      Attempt.fromTry(companion.validate(b.bytes.toArrayUnsafe))
        .map(a => DecodeResult(a, BitVector.empty))
    )
  }

  def codecForGenerated[A <: GeneratedMessage](companion: GeneratedMessageCompanion[A]): Codec[A] = {
    Codec[A](encoderForGenerated(companion), decoderForGenerated(companion))
  }

  implicit def byteVectorTypeMapper: TypeMapper[ByteString, ByteVector] =
    new TypeMapper[ByteString, ByteVector] {
      def toCustom(bs: ByteString) = ByteVector.view(bs.toByteArray())
      def toBase(bv: ByteVector) = ByteString.copyFrom(bv.toArrayUnsafe)
    }
}
