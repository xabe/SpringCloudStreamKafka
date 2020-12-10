package com.xabe.spring.cloud.stream.consumer.infrastructure.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.xabe.avro.v1.*;
import java.io.ByteArrayOutputStream;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.jupiter.api.Test;

public class AvroTest {

  @Test
  public void avro() throws Exception {

    final Car car = Car.newBuilder().setId("id").setName("name").build();
    final CarCreated carCreated = CarCreated.newBuilder().setSentAt(System.currentTimeMillis()).setCar(car).build();

    final MessageEnvelope messageEnvelope =
        MessageEnvelope.newBuilder().setMetadata(this.setMetadata()).setPayload(carCreated).build();

    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    final DatumWriter<MessageEnvelope> writer = new SpecificDatumWriter<>(MessageEnvelope.getClassSchema());
    final Encoder encoder = EncoderFactory.get().binaryEncoder(os, null);
    writer.write(messageEnvelope, encoder);
    encoder.flush();
    os.close();
    final byte[] data = os.toByteArray();

    final DatumReader<MessageEnvelope> reader = new SpecificDatumReader<>(MessageEnvelope.getClassSchema());
    final Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
    final MessageEnvelope result = reader.read(null, decoder);

    assertThat(result, is(notNullValue()));
  }

  private Metadata setMetadata() {
    return Metadata.newBuilder().setDomain("abc").setName("name").setAction("action").setVersion("version").setTimestamp("12345").build();
  }
}
