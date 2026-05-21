package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.output.TransactionOutput;

public record TransactionResponse(String id, String category, double amount, String description) {
    public static TransactionResponse from(TransactionOutput output) {
        return new TransactionResponse(
                output.id(),
                output.category(),
                output.value(),
                output.description() 
        );
    }
}
