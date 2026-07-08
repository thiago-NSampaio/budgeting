package dio.budgeting.application.output;

import java.util.List;

import dio.budgeting.domain.Expense;
import dio.budgeting.domain.Income;
import dio.budgeting.domain.balance.Balance;

import java.util.stream.Collectors;

public record DashboardOutput(
        Balance balance,
        Income income,
        Expense expense,
        BudgetLimitOutput budget,
        GoalOutput goal,
        List<TransactionOutput> lastTransactions,
        String aiInsight
) {

    public DashboardOutput withInsight(String insight) {
        return new DashboardOutput(
                balance,
                income,
                expense,
                budget,
                goal,
                lastTransactions,
                insight
        );
    }

    public String toAiSummary() {

        String transactions = lastTransactions.isEmpty()
                ? "Nenhuma transação registrada."
                : lastTransactions.stream()
                        .map(t -> String.format(
                                "- %s | Categoria: %s | Valor: R$ %.2f",
                                t.description(),
                                t.category(),
                                t.value() / 100.0))
                        .collect(Collectors.joining("\n"));

        double budgetUsed = budget.limitAmount() == 0
                ? 0
                : (expense.amount() * 100.0) / budget.limitAmount();

        double goalProgress = goal.targetAmount() == 0
                ? 0
                : (goal.currentAmount() * 100.0) / goal.targetAmount();

        return """
                Resumo financeiro do usuário

                Saldo atual: R$ %.2f
                Receitas: R$ %.2f
                Despesas: R$ %.2f

                Orçamento mensal
                - Mês: %s
                - Limite: R$ %.2f
                - Utilizado: %.1f%%

                Meta financeira
                - Objetivo: %s
                - Guardado: R$ %.2f
                - Meta: R$ %.2f
                - Progresso: %.1f%%
                - Prazo: %s

                Últimas transações
                %s
                """.formatted(
                balance.amount() / 100.0,
                income.amount() / 100.0,
                expense.amount() / 100.0,
                budget.month(),
                budget.limitAmount() / 100.0,
                budgetUsed,
                goal.title(),
                goal.currentAmount() / 100.0,
                goal.targetAmount() / 100.0,
                goalProgress,
                goal.deadline(),
                transactions
        );
    }
}