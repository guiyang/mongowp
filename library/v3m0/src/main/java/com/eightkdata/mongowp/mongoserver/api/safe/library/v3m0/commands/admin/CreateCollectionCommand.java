
package com.eightkdata.mongowp.mongoserver.api.safe.library.v3m0.commands.admin;

import com.eightkdata.mongowp.mongoserver.api.safe.impl.AbstractCommand;
import com.eightkdata.mongowp.mongoserver.api.safe.library.v3m0.commands.admin.CreateCollectionCommand.CreateCollectionArgument;
import com.eightkdata.mongowp.mongoserver.api.safe.library.v3m0.pojos.CollectionOptions;
import com.eightkdata.mongowp.mongoserver.api.safe.tools.Empty;
import com.eightkdata.mongowp.mongoserver.api.safe.tools.bson.BsonDocumentBuilder;
import com.eightkdata.mongowp.mongoserver.api.safe.tools.bson.BsonField;
import com.eightkdata.mongowp.mongoserver.api.safe.tools.bson.BsonReaderTool;
import com.eightkdata.mongowp.mongoserver.protocol.exceptions.BadValueException;
import com.eightkdata.mongowp.mongoserver.protocol.exceptions.NoSuchKeyException;
import com.eightkdata.mongowp.mongoserver.protocol.exceptions.TypesMismatchException;
import org.bson.BsonDocument;

/**
 *
 */
public class CreateCollectionCommand extends AbstractCommand<CreateCollectionArgument, Empty> {

    public static final CreateCollectionCommand INSTANCE = new CreateCollectionCommand();

    private CreateCollectionCommand() {
        super("create");
    }

    @Override
    public Class<? extends CreateCollectionArgument> getArgClass() {
        return CreateCollectionArgument.class;
    }

    @Override
    public CreateCollectionArgument unmarshallArg(BsonDocument requestDoc) 
            throws TypesMismatchException, NoSuchKeyException, BadValueException {
        return CreateCollectionArgument.unmarshall(requestDoc);
    }

    @Override
    public BsonDocument marshallArg(CreateCollectionArgument request) {
        return request.marshall();
    }

    @Override
    public Class<? extends Empty> getResultClass() {
        return Empty.class;
    }

    @Override
    public BsonDocument marshallResult(Empty reply) {
        return null;
    }

    @Override
    public Empty unmarshallResult(BsonDocument replyDoc) {
        return Empty.getInstance();
    }

    public static class CreateCollectionArgument {
        private static final BsonField<String> COLLECTION_FIELD = BsonField.create("create");

        private final String collection;
        private final CollectionOptions options;

        public CreateCollectionArgument(String collection, CollectionOptions options) {
            this.collection = collection;
            this.options = options;
        }

        private static CreateCollectionArgument unmarshall(BsonDocument requestDoc)
                throws TypesMismatchException, NoSuchKeyException, BadValueException {
            String collection = BsonReaderTool.getString(requestDoc, COLLECTION_FIELD);
            CollectionOptions options = CollectionOptions.unmarshal(requestDoc);

            return new CreateCollectionArgument(collection, options);
        }

        private BsonDocument marshall() {
            BsonDocumentBuilder builder = new BsonDocumentBuilder();
            builder.append(COLLECTION_FIELD, collection);
            options.marshall(builder);

            return builder.build();
        }

        public String getCollection() {
            return collection;
        }

        public CollectionOptions getOptions() {
            return options;
        }

    }

}