/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.operation;

import org.bson.BSONReader;
import org.bson.BSONType;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.Codec;
import org.bson.codecs.Decoder;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.configuration.CodecSource;
import org.bson.codecs.configuration.RootCodecRegistry;
import org.bson.types.BsonDocument;
import org.bson.types.BsonDocumentWrapper;
import org.bson.types.BsonValue;

import java.util.Arrays;

class CommandResultDocumentCodec<T> extends BsonDocumentCodec {
    private final Decoder<T> payloadDecoder;
    private final String fieldContainingPayload;

    CommandResultDocumentCodec(final CodecRegistry registry, final Decoder<T> payloadDecoder, final String fieldContainingPayload) {
        super(registry);
        this.payloadDecoder = payloadDecoder;
        this.fieldContainingPayload = fieldContainingPayload;
    }

    static <P> Codec<BsonDocument> create(final Decoder<P> payloadDecoder, final String fieldContainingPayload) {
        CodecRegistry registry = new RootCodecRegistry(Arrays.<CodecSource>asList(new CommandResultCodecSource<P>(payloadDecoder,
                                                                                                                  fieldContainingPayload)));
        return registry.get(BsonDocument.class);
    }

    @Override
    protected BsonValue readValue(final BSONReader reader) {
        if (reader.getCurrentName().equals(fieldContainingPayload)) {
            if (reader.getCurrentBSONType() == BSONType.DOCUMENT) {
                return new BsonDocumentWrapper<T>(payloadDecoder.decode(reader), null);
            } else if (reader.getCurrentBSONType() == BSONType.ARRAY) {
                return new CommandResultArrayCodec<T>(getCodecRegistry(), payloadDecoder).decode(reader);
            }
        }
        return super.readValue(reader);
    }
}
