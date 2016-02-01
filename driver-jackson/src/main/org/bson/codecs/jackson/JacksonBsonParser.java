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

package org.bson.codecs.jackson;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import org.bson.BSONException;
import org.bson.BsonBinary;
import org.bson.BsonJavaScript;
import org.bson.BsonReader;
import org.bson.BsonSymbol;
import org.bson.BsonType;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Date;

import static java.lang.String.format;

/**
 * Defines a BSON parser to plug in to Jackson
 */
public class JacksonBsonParser extends JsonParser {

    private final BsonReader reader;

    // used as a stack where the last element is the bottom
    private final ArrayDeque<ReaderContext> readerContextDeque = new ArrayDeque<ReaderContext>();
    private JsonToken tokenOfCurValue;
    // token of the value of the current BsonType, could be null if current BsonType isn't of (key, value) pair
    private Object curValue;
    private JsonToken curToken;
    private BsonType curBsonType;

    private String curFieldName;

    /**
     * Creates an instance of this class for the given reader
     *
     * @param reader the BsonReader to use when parsing
     */
    public JacksonBsonParser(final BsonReader reader) {
        this.reader = reader;
        readerContextDeque.offerFirst(ReaderContext.NULL_CONTEXT);
    }

    @Override
    public ObjectCodec getCodec() {
        return null;
    }

    @Override
    public void setCodec(final ObjectCodec objectCodec) {

    }

    @Override
    public Object getCurrentValue() {
        return curValue;
    }

    @Override
    public Version version() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    /**
     * The context structure for {a:[b:{c:1}]} looks like the following at the time we call nextToken()
     * 1.   after we've just read "a", about to read "["
     * (bottom) -> NULL_CONTEXT -> DOCUMENT_CONTEXT -> ARRAY_CONTEXT -> VALUE_CONTEXT -> FIELD_NAME_CONTEXT
     * 2.   after we've just read "[", about to read "b"'s bson type
     * (bottom) -> NULL_CONTEXT -> DOCUMENT_CONTEXT -> ARRAY_CONTEXT -> VALUE_CONTEXT
     * 3.   after we've read b's name, about to read "{"
     * (bottom) -> NULL_CONTEXT -> DOCUMENT_CONTEXT -> ARRAY_CONTEXT -> VALUE_CONTEXT -> FIELD_NAME_CONTEXT
     * <p/>
     * note that 1's field name and 3's field name are different; the latter refers to the name "b", the former refers
     * to "a".
     *
     * @return JsonToken
     * @throws IOException
     */
    @Override
    @SuppressWarnings("CheckStyle")  // method is too long
    public JsonToken nextToken() throws IOException {


        switch (readerContextDeque.peekFirst()) {
            case FIELD_NAME_CONTEXT:
                // here we've already read the field name, so only need to pop the field name context
                curFieldName = null;
                readerContextDeque.pollFirst();
                curToken = tokenOfCurValue;
                return curToken;
            case NULL_CONTEXT:
                // reached the end of the documents, no more tokens available does not throw an error per BsonParser
                return null;
            case ARRAY_CONTEXT:
            case DOCUMENT_CONTEXT:
            case VALUE_CONTEXT:
                curValue = null;
                readerContextDeque.pollFirst();
                break;
            default:
                throw new BSONException(format("Unexpected ContextType %s.", readerContextDeque.peekFirst()));
        }

        curBsonType = reader.readBsonType();

        final ReaderContext curContext = readerContextDeque.peekFirst();

        // context that we're about to be in
        if (curBsonType == BsonType.DOCUMENT) {
            readerContextDeque.offerFirst(ReaderContext.DOCUMENT_CONTEXT);
        }
        if (curBsonType == BsonType.ARRAY) {
            readerContextDeque.offerFirst(ReaderContext.ARRAY_CONTEXT);
        }

        switch (curBsonType) {
            case END_OF_DOCUMENT:
                switch (readerContextDeque.pollFirst()) {
                    case ARRAY_CONTEXT:
                        reader.readEndArray();
                        // not required for documentReader
                        return JsonToken.END_ARRAY;
                    case DOCUMENT_CONTEXT:
                        reader.readEndDocument();
                        return JsonToken.END_OBJECT;
                    default:
                        throw new BSONException("Unexpected Token");
                }
            case DOCUMENT:
            case ARRAY:
            case DOUBLE:
            case STRING:
            case BINARY:
            case OBJECT_ID:
            case BOOLEAN:
            case DATE_TIME:
            case NULL:
            case REGULAR_EXPRESSION:
            case DB_POINTER:
            case INT32:
            case INT64:
            case SYMBOL:
            case JAVASCRIPT:
            case TIMESTAMP:
                readerContextDeque.offerFirst(ReaderContext.VALUE_CONTEXT);
                if (curContext == ReaderContext.DOCUMENT_CONTEXT) {
                    readerContextDeque.offerFirst(ReaderContext.FIELD_NAME_CONTEXT);
                    curToken = JsonToken.FIELD_NAME;
                    curFieldName = reader.readName();
                }
                break;
            default:
                throw new BSONException(format("Unexpected ContextType %s.", curBsonType));
        }

        readBsonValue();

        if (curToken != JsonToken.FIELD_NAME) {
            curToken = tokenOfCurValue;
        }

        return curToken;
    }

    void readBsonValue() {
        switch (curBsonType) {
            case DOUBLE:
                curValue = reader.readDouble();
                tokenOfCurValue = JsonToken.VALUE_NUMBER_FLOAT;
                break;
            case STRING:
                curValue = reader.readString();
                tokenOfCurValue = JsonToken.VALUE_STRING;
                break;
            case DOCUMENT:
                tokenOfCurValue = JsonToken.START_OBJECT;
                reader.readStartDocument();
                break;
            case ARRAY:
                tokenOfCurValue = JsonToken.START_ARRAY;
                reader.readStartArray();
                break;
            case BINARY:
                curValue = reader.readBinaryData();
                tokenOfCurValue = JsonToken.VALUE_EMBEDDED_OBJECT;
                break;
            case UNDEFINED:
                reader.readUndefined();
                //TODO: throw error here or sth?
                break;
            case OBJECT_ID:
                curValue = reader.readObjectId();
                tokenOfCurValue = JsonToken.VALUE_EMBEDDED_OBJECT;
                break;
            case BOOLEAN:
                curValue = reader.readBoolean();
                tokenOfCurValue = ((Boolean) curValue) ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE;
                break;
            case DATE_TIME:
                curValue = new Date(reader.readDateTime());
                tokenOfCurValue = JsonToken.VALUE_EMBEDDED_OBJECT;
                break;
            case NULL:
                curValue = null;
                tokenOfCurValue = JsonToken.VALUE_NULL;
                break;
            case REGULAR_EXPRESSION:
                curValue = reader.readRegularExpression().getPattern();
                tokenOfCurValue = JsonToken.VALUE_EMBEDDED_OBJECT;
                break;
            case DB_POINTER:
//                final BsonDbPointer bsonDbPointer = reader.readDBPointer();
//                final Map<String, Object> pointer = new LinkedHashMap<String, Object>();
//                pointer.put("$ns", bsonDbPointer.getNamespace());
//                pointer.put("$id", bsonDbPointer.getId());
                curValue = reader.readDBPointer();
                tokenOfCurValue = JsonToken.VALUE_EMBEDDED_OBJECT;
                break;
            case JAVASCRIPT:
                curValue = new BsonJavaScript(reader.readJavaScript());
                tokenOfCurValue = JsonToken.VALUE_EMBEDDED_OBJECT;
                break;
            case SYMBOL:
                curValue = new BsonSymbol(reader.readSymbol());
                tokenOfCurValue = JsonToken.VALUE_EMBEDDED_OBJECT;
                break;
            case JAVASCRIPT_WITH_SCOPE:
                curValue = reader.readJavaScriptWithScope();
                tokenOfCurValue = JsonToken.VALUE_EMBEDDED_OBJECT;
                break;
            case INT32:
                curValue = reader.readInt32();
                tokenOfCurValue = JsonToken.VALUE_NUMBER_INT;
                break;
            case INT64:
                curValue = reader.readInt64();
                tokenOfCurValue = JsonToken.VALUE_NUMBER_INT;
                break;
            case TIMESTAMP:
                //TODO: figure out if need to convert timestamp to some other format
                curValue = reader.readTimestamp();
                tokenOfCurValue = JsonToken.VALUE_EMBEDDED_OBJECT;
                break;
            case MIN_KEY:
                // TODO: how should min and max key be tested
                curValue = "MinKey";
                tokenOfCurValue = JsonToken.VALUE_STRING;
                break;
            case MAX_KEY:
                curValue = "MaxKey";
                tokenOfCurValue = JsonToken.VALUE_STRING;
                break;
            default:
                throw new UnsupportedOperationException("Can not process unknown BSON type: " + curBsonType);
        }
    }

    @Override
    public JsonToken nextValue() throws IOException {
        nextToken();
        return tokenOfCurValue;
    }

    @Override
    public JsonParser skipChildren() throws IOException {
        return null;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public JsonToken getCurrentToken() {
        if (readerContextDeque.peekFirst() == ReaderContext.NULL_CONTEXT) {
            // TODO: nextToken() should return start_object if called before readStartDocument()
            // but there's no way to determine if NULL_CONTEXT is before or after root document is read
            // need to introduce state?
            reader.readStartDocument();
            readerContextDeque.offerFirst(ReaderContext.DOCUMENT_CONTEXT);
            curToken = JsonToken.START_OBJECT;
        }

        return curToken;
    }

    @Override
    public int getCurrentTokenId() {
        final JsonToken t = curToken;
        return (t == null) ? JsonTokenId.ID_NO_TOKEN : t.id();
    }

    @Override
    public boolean hasCurrentToken() {
        return curToken != null;
    }

    @Override
    public boolean hasTokenId(final int id) {
        return getCurrentTokenId() == id;
    }

    @Override
    public boolean hasToken(final JsonToken t) {
        return getCurrentToken() == t;
    }

    @Override
    public String getCurrentName() throws IOException {
        return curFieldName;
    }

    @Override
    public JsonStreamContext getParsingContext() {
        return null;
    }

    @Override
    public JsonLocation getTokenLocation() {
        return null;
    }

    @Override
    public JsonLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void clearCurrentToken() {
        curToken = null;
    }

    @Override
    public JsonToken getLastClearedToken() {
        return curToken;
    }

    @Override
    public void overrideCurrentName(final String s) {
        curFieldName = s;
    }

    @Override
    public String getText() throws IOException {
        if (readerContextDeque.peekFirst() == ReaderContext.FIELD_NAME_CONTEXT) {
            return String.valueOf(curFieldName);
        } else if (readerContextDeque.peekFirst() == ReaderContext.VALUE_CONTEXT) {
            return String.valueOf(curValue);
        }
        return null;
    }

    @Override
    public char[] getTextCharacters() throws IOException {
        return ((String) curValue).toCharArray();
    }

    @Override
    public int getTextLength() throws IOException {
        return getText().length();
    }

    @Override
    public int getTextOffset() throws IOException {
        return 0;
    }

    @Override
    public boolean hasTextCharacters() {
        return false;
    }

    @Override
    public Number getNumberValue() throws IOException {
        return (Number) curValue;
    }

    @Override
    public NumberType getNumberType() throws IOException {
        //TODO: figure out when this gets called
        return null;
    }

    @Override
    public byte getByteValue() throws IOException {
        return 0;
    }

    @Override
    public short getShortValue() throws IOException {
        return (Short) curValue;
    }

    @Override
    public int getIntValue() throws IOException {
        return (Integer) curValue;
    }

    @Override
    public long getLongValue() throws IOException {
        return (Long) curValue;
    }

    @Override
    public BigInteger getBigIntegerValue() throws IOException {
        return new BigInteger(String.valueOf(curValue));
    }

    @Override
    public float getFloatValue() throws IOException {
        return ((Double) curValue).floatValue();
    }

    @Override
    public double getDoubleValue() throws IOException {
        return (Double) curValue;
    }

    @Override
    public BigDecimal getDecimalValue() throws IOException {
        return new BigDecimal(String.valueOf(curValue));
    }

    @Override
    public boolean getBooleanValue() throws IOException {
        return (Boolean) curValue;
    }

    @Override
    public Object getEmbeddedObject() throws IOException {
        return curValue;
    }

    @Override
    public byte[] getBinaryValue(final Base64Variant base64Variant) throws IOException {
        return ((BsonBinary) curValue).getData();
    }

    @Override
    public String getValueAsString(final String s) throws IOException {
        return (String) curValue;
    }

    @Override
    public ObjectId getObjectId() {
        return (ObjectId) curValue;
    }

    /**
     * @return the current bson type
     */
    public BsonType getCurBsonType() {
        return curBsonType;
    }

/*
    public Date getDateValue() {
        return (Date) curValue;
    }

    public BsonJavaScript getJavascriptValue() {
        return (BsonJavaScript) curValue;
    }

    public BsonTimestamp getTimestampValue() {
        return (BsonTimestamp) curValue;
    }
*/

    enum ReaderContext {
        DOCUMENT_CONTEXT,
        ARRAY_CONTEXT,
        NULL_CONTEXT,
        // have not started reading
        FIELD_NAME_CONTEXT,
        // currentToken is a field name, so next token is going to be a value
        VALUE_CONTEXT // currentToken is the value of a field
    }
}
