package dio.budgeting.application.input;

import org.springframework.ai.tool.annotation.ToolParam;

import dio.budgeting.domain.Category;

public record PersistTransactionInput(
    @ToolParam(description = "Descrição do gasto, compra ou recebimento") String description,
    @ToolParam(description = "valor do gasto, compra ou recebimento em centavos") Long amount, 
    @ToolParam(description = "Categoria de uma transação") Category category
) {
}
