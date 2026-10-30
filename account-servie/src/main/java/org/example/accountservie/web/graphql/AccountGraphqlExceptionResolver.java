package org.example.accountservie.web.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.example.accountservie.service.*;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class AccountGraphqlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable exception, DataFetchingEnvironment environment) {
        if (exception instanceof AccountNotFoundException) {
            return error(exception, environment, ErrorType.NOT_FOUND);
        }
        if (exception instanceof InvalidAmountException
                || exception instanceof InvalidAccountDataException
                || exception instanceof AccountBlockedException
                || exception instanceof InsufficientBalanceException) {
            return error(exception, environment, ErrorType.BAD_REQUEST);
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
