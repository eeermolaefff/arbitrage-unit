package com.stambul.arbitrageur.arbitrage.graph.edges;

import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.BaseEdge;
import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;
import com.stambul.library.database.objects.dto.*;

import java.util.Map;

public class TransferEdge extends BaseEdge {
    private TransferDTO transfer;
    private boolean withdraw;

    public TransferEdge(
            CurrencyDTO currency,
            MarketDTO market,
            TransferDTO transfer,
            int vertexFrom,
            int vertexTo,
            boolean withdraw
    ) {
        super(currency, currency, market, vertexFrom, vertexTo);
        init(transfer, withdraw);
    }

    public TransferEdge(
            CurrencyDTO currencyFrom,
            CurrencyDTO currencyTo,
            MarketDTO market,
            TransferDTO transfer,
            int vertexFrom,
            int vertexTo,
            boolean withdraw
    ) {
        super(currencyFrom, currencyTo, market, vertexFrom, vertexTo);
        init(transfer, withdraw);
    }

    public TransferDTO getTransfer() {
        return transfer;
    }

    public void update(TransferDTO transfer, boolean withdraw) {
        init(transfer, withdraw);
    }

    @Override
    public Map<String, Object> toJSON(double startCurrencyAmount) {
        Map<String, Object> json = super.toJSON();

        json.put("blockchain", transfer.getBlockchain().getName());
        json.put("gas", transfer.getBlockchain().getGas());
        json.put("withdraw", withdraw);
        json.put("withdrawEnable", transfer.getWithdrawEnable());
        json.put("depositEnable", transfer.getDepositEnable());
        json.put("withdrawalStaticCommission", transfer.getWithdrawalStaticCommission());
        json.put("depositStaticCommission", transfer.getDepositStaticCommission());
        json.put("withdrawalPercentCommission", transfer.getWithdrawalPercentCommission());
        json.put("depositPercentCommission", transfer.getDepositPercentCommission());
        json.put("updatedAt", transfer.getUpdatedAt());

        Map<String, Object> from = (Map<String, Object>) json.get("from");
        Map<String, Object> to = (Map<String, Object>) json.get("to");

        from.put("amount", startCurrencyAmount);
        to.put("amount", goThrough(startCurrencyAmount));

        return json;
    };

    @Override
    public DirectedEdge copy(int vertexFrom, int vertexTo) {
        return new TransferEdge(currencyFrom, currencyTo, market, transfer, vertexFrom, vertexTo, withdraw);
    }

    @Override
    public DirectedEdge reversed() {
        return new TransferEdge(currencyTo, currencyFrom, market, transfer, vertexTo, vertexFrom, !withdraw);
    }

    @Override
    public String getTicker() {
        return String.format("%s (%s)", transfer.getRelation().getTicker().getTicker(), transfer.getBlockchain().getName());
    }

    @Override
    public String toString() {
        String type, from, to;
        if (withdraw) {
            type = "Withdraw";
            from = market.getFullName();
            to = transfer.getBlockchain().getName();
        } else {
            type = "Deposit";
            from = transfer.getBlockchain().getName();
            to = market.getFullName();
        }

        return String.format(
                "%s: %s %s -> %s %s (%.5f) [%f, %f]",
                type,
                from,
                currencyFrom.getSlug(),
                to,
                currencyTo.getSlug(),
                weight,
                minAmount,
                maxAmount
        );
    }

    private void init(TransferDTO transfer, boolean withdraw) {
        this.transfer = transfer;
        this.withdraw = withdraw;

        weight = 1;
        if (withdraw) {
            percentCommission = (100 - transfer.getWithdrawalPercentCommission()) / 100;
            staticCommission = transfer.getWithdrawalStaticCommission();
        }
        else {
            percentCommission = (100 - transfer.getDepositPercentCommission()) / 100;
            staticCommission = transfer.getDepositStaticCommission();
        }
    }
}
