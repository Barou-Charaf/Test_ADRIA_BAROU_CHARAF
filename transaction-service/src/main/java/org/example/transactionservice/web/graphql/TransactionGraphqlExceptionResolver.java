package org.example.transactionservice.web.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.example.transactionservice.client.RemoteAccountOperationException;
import org.example.transactionservice.service.*;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class TransactionGraphqlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable exception, DataFetchingEnvironment environment) {
        if (exception instanceof TransactionNotFoundException || exception instanceof AccountNotFoundException) {
            return error(exception, environment, ErrorType.NOT_FOUND);
        }
        if (exception instanceof InvalidTransactionException
                || exception instanceof CurrencyMismatchException
                || exception instanceof BlockedAccountException
                || exception instanceof InsufficientBalanceException
                || exception instanceof RemoteAccountOperationException) {
            return error(exception, environment, ErrorType.BAD_REQUEST);
        }
        if (exception instanceof AccountServiceUnavailableException
                || exception instanceof TransactionProcessingException
                || exception instanceof CompensationFailedException) {
            return error(exception, environment, ErrorType.INTERNAL_ERROR);
        }
        return null;
    }

    private GraphQLError error(Throwable exception, DataFetchingEnvironment environment, ErrorType errorType) {
        return GraphqlErrorBuilder.newError(environment)
                .errorType(errorType)
                .message(exception.getMessage())
                .build();
    }
}
