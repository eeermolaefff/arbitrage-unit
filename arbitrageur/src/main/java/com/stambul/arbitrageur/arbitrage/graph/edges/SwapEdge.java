package com.stambul.arbitrageur.arbitrage.graph.edges;

import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.BaseEdge;
import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;
import com.stambul.library.database.objects.dto.ContractDTO;
import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.dto.MarketDTO;
import com.stambul.library.database.objects.dto.SwapDTO;

import java.util.Map;
import java.util.TreeMap;

public class SwapEdge extends BaseEdge {
    private SwapDTO swap;
    private ContractDTO contractFrom, contractTo;

    public SwapEdge(
            CurrencyDTO currencyFrom,
            CurrencyDTO currencyTo,
            MarketDTO market,
            SwapDTO swap,
            int vertexFrom,
            int vertexTo
    ) {
        super(currencyFrom, currencyTo, market, vertexFrom, vertexTo);
        init(swap);
    }

    public SwapDTO getSwap() {
        return swap;
    }

    public void update(SwapDTO swap) {
        init(swap);
    }

    @Override
    public Map<String, Object> toJSON(double startCurrencyAmount) {
        Map<String, Object> json = super.toJSON();

        json.put("feePercentage", swap.getFeePercentage());
        json.put("poolHash", swap.getHash());
        json.put("tradingType", swap.getTradingType());
        json.put("updatedAt", swap.getUpdatedAt());

        json.put("blockchainId", contractFrom.getBlockchain().getId());
        json.put("blockchainName", contractFrom.getBlockchain().getName());
        json.put("gas", contractTo.getBlockchain().getGas());

        Map<String, Object> from = (Map<String, Object>) json.get("from");
        Map<String, Object> to = (Map<String, Object>) json.get("to");

        from.put("amount", startCurrencyAmount);
        from.put("address", contractFrom.getAddress());

        to.put("amount", goThrough(startCurrencyAmount));
        to.put("address", contractTo.getAddress());

        return json;
    };

    @Override
    public DirectedEdge copy(int vertexFrom, int vertexTo) {
        return new SwapEdge(currencyFrom, currencyTo, market, swap, vertexFrom, vertexTo);
    }

    @Override
    public DirectedEdge reversed() {
        return new SwapEdge(currencyTo, currencyFrom, market, swap, vertexTo, vertexFrom);
    }

    @Override
    public String getTicker() {
        return swap.getTradingPair().getTicker().getTicker();
    }

    @Override
    public String toString() {
        return String.format(
                "%s: %s %s -> %s (%.5f) [%f, %f]",
                market.getFullName(),
                getTicker(),
                currencyFrom.getSlug(),
                currencyTo.getSlug(),
                weight,
                minAmount,
                maxAmount
        );
    }

    private void init(SwapDTO swap) {
        this.swap = swap;

        percentCommission = (100 - swap.getFeePercentage()) / 100;
        if (swap.getTradingPair().getBaseAssetId().equals(currencyFrom.getId())) {          // base to quote
            weight = swap.getBasePrice();
            contractFrom = swap.getBaseContract();
            contractTo = swap.getQuoteContract();
            maxAmount = swap.getDailyVolumeBase();
        } else if (swap.getTradingPair().getQuoteAssetId().equals(currencyFrom.getId())) {  // quote to base
            weight = swap.getQuotePrice();
            contractFrom = swap.getQuoteContract();
            contractTo = swap.getBaseContract();
            maxAmount = swap.getDailyVolumeQuote();
        } else {
            String message = "Base/quote ids mismatch: fromIdx=%d, toIdx=%d, edge=%s";
            throw new IllegalArgumentException(String.format(message, currencyFrom.getId(), currencyTo.getId(), this));
        }
    }
}
