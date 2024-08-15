package com.stambul.arbitrageur.arbitrage.graph.provider;

import com.stambul.arbitrageur.arbitrage.graph.edges.OrderbookEdge;
import com.stambul.arbitrageur.arbitrage.graph.edges.TransferEdge;
import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;
import com.stambul.arbitrageur.arbitrage.graph.interfaces.ArrayBasedDigraph;
import com.stambul.arbitrageur.arbitrage.graph.interfaces.WeightedDigraph;
import com.stambul.arbitrageur.arbitrage.graph.edges.SwapEdge;
import com.stambul.arbitrageur.arbitrage.graph.provider.interfaces.GraphProvider;
import com.stambul.arbitrageur.arbitrage.graph.provider.interfaces.VerticesEncoder;
import com.stambul.arbitrageur.services.DatabaseService;
import com.stambul.library.database.objects.dto.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ArrayBasedDigraphProvider implements GraphProvider {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final VerticesEncoder encoder = new AddressBasedEncoder();
    private final DatabaseService databaseService;
    private final double minTvlUsd;
    private final double minLiquidity;
    private Map<Integer, MarketDTO> markets;
    private Map<Integer, CurrencyDTO> currencies;
    private List<TransferDTO> transfers;
    private List<OrderbookDTO> orderbooks;
    private List<SwapDTO> swaps;

    @Autowired
    public ArrayBasedDigraphProvider(
            DatabaseService databaseService,
            @Value("${provider.swaps.limits.tvl.usd.min}") double minTvlUsd,
            @Value("${provider.swaps.limits.liquidity.min}") double minLiquidity
    ) {
        this.databaseService = databaseService;
        this.minTvlUsd = minTvlUsd;
        this.minLiquidity = minLiquidity;
    }

    @Override
    public WeightedDigraph makeGraph() {
        extractFieldsFromDatabase();
        List<DirectedEdge> edges = makeEdgesList();

        WeightedDigraph graph = new ArrayBasedDigraph(encoder.size());
        graph.addAll(edges);
        return graph;
    }

    @Override
    public VerticesEncoder getEncoder() {
        return encoder;
    }

    private List<DirectedEdge> makeEdgesList() {
        List<DirectedEdge> edges = new LinkedList<>();

        insertOrderbookEdges(edges);
        insertSwapEdges(edges);
        insertTransferEdges(edges);

        return edges;
    }

    private void insertOrderbookEdges(List<DirectedEdge> edges) {
        for (OrderbookDTO orderbook : orderbooks) {
            TradingPairDTO tradingPair = orderbook.getTradingPair();
            CurrencyDTO currencyFrom = currencies.get(tradingPair.getBaseAssetId());
            CurrencyDTO currencyTo = currencies.get(tradingPair.getQuoteAssetId());
            MarketDTO market = markets.get(tradingPair.getMarketId());
            int vertexFrom = encoder.makeIfNotExistsIndex(market.getSlug(), currencyFrom.getSlug());
            int vertexTo = encoder.makeIfNotExistsIndex(market.getSlug(), currencyTo.getSlug());

            edges.add(new OrderbookEdge(currencyFrom, currencyTo, market, orderbook, vertexFrom, vertexTo));
            edges.add(new OrderbookEdge(currencyTo, currencyFrom, market, orderbook, vertexTo, vertexFrom));
        }
    }

    private void insertSwapEdges(List<DirectedEdge> edges) {
        for (SwapDTO swap : swaps) {
            if (swap.getTvlUsd() < minTvlUsd || swap.getLiquidity() < minLiquidity)
                continue;

            TradingPairDTO tradingPair = swap.getTradingPair();
            CurrencyDTO currencyFrom = currencies.get(tradingPair.getBaseAssetId());
            CurrencyDTO currencyTo = currencies.get(tradingPair.getQuoteAssetId());
            MarketDTO market = markets.get(tradingPair.getMarketId());
            ContractDTO contractFrom = swap.getBaseContract();
            ContractDTO contractTo = swap.getQuoteContract();
            int vertexFrom = encoder.makeIfNotExistsIndex(contractFrom.getBlockchain().getName(), currencyFrom.getSlug());
            int vertexTo = encoder.makeIfNotExistsIndex(contractTo.getBlockchain().getName(), currencyTo.getSlug());

            //TODO make code by address instead of currency slug (for coins that are not in the CoinMarketCap)

            edges.add(new SwapEdge(currencyFrom, currencyTo, market, swap, vertexFrom, vertexTo));
            edges.add(new SwapEdge(currencyTo, currencyFrom, market, swap, vertexTo, vertexFrom));
        }
    }

    private void insertTransferEdges(List<DirectedEdge> edges) {
        for (TransferDTO transfer : transfers) {
            MarketDTO market = markets.get(transfer.getRelation().getMarketId());
            CurrencyDTO currency = currencies.get(transfer.getRelation().getCurrencyId());

            Integer vertexMarket = encoder.getIndex(market.getSlug(), currency.getSlug());
            Integer vertexChain = encoder.getIndex(transfer.getBlockchain().getName(), currency.getSlug());

            if (vertexMarket == null || vertexChain == null)
                continue;

            //TODO make code by address instead of currency slug (for coins that are not in the CoinMarketCap)

            if (transfer.getWithdrawEnable())
                edges.add(new TransferEdge(currency, market, transfer, vertexMarket, vertexChain, true));
            if (transfer.getDepositEnable())
                edges.add(new TransferEdge(currency, market, transfer, vertexChain, vertexMarket, false));
        }

    }

    private void extractFieldsFromDatabase() {
        orderbooks = databaseService.getAllOrderbooks();
        swaps = databaseService.getAllSwaps();
        transfers = databaseService.getAllTransfers();

        Set<Integer> marketIds = new TreeSet<>();
        Set<Integer> currencyIds = new TreeSet<>();
        for (OrderbookDTO orderbook : orderbooks) {
            TradingPairDTO tradingPair = orderbook.getTradingPair();
            marketIds.add(tradingPair.getMarketId());
            currencyIds.add(tradingPair.getBaseAssetId());
            currencyIds.add(tradingPair.getQuoteAssetId());
        }
        for (SwapDTO swap : swaps) {
            TradingPairDTO tradingPair = swap.getTradingPair();
            marketIds.add(tradingPair.getMarketId());
            currencyIds.add(tradingPair.getBaseAssetId());
            currencyIds.add(tradingPair.getQuoteAssetId());
        }
        for (TransferDTO transfer : transfers) {
            RelationDTO relation = transfer.getRelation();
            marketIds.add(relation.getMarketId());
            currencyIds.add(relation.getCurrencyId());
        }

        //TODO extract contracts for transfers (for coins that are not in the CoinMarketCap)

        markets = databaseService.getMarketMapById(marketIds);
        currencies = databaseService.getCurrencyMapById(currencyIds);
    }
}
