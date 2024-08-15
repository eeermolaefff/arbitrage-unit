package com.stambul.arbitrageur.arbitrage.cycles.processing;

import com.stambul.arbitrageur.arbitrage.cycles.objects.ConnectedCycle;
import com.stambul.arbitrageur.arbitrage.cycles.objects.FieldsComparableCycle;
import com.stambul.arbitrageur.arbitrage.cycles.objects.ProfitComparableCycle;
import com.stambul.arbitrageur.arbitrage.graph.edges.OrderbookEdge;
import com.stambul.arbitrageur.arbitrage.graph.edges.SwapEdge;
import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;
import com.stambul.arbitrageur.services.DatabaseService;
import com.stambul.library.database.objects.dto.OrderbookDTO;
import com.stambul.library.database.objects.dto.SwapDTO;
import com.stambul.library.database.objects.interfaces.Identifiable;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CycleStorage {
    protected final Logger logger = Logger.getLogger(this.getClass());
    private final Set<Integer> deletedIds = new TreeSet<>();
    private final Set<Integer> activeIds = new TreeSet<>();
    private final Map<FieldsComparableCycle, ProfitComparableCycle> fieldProfitMap = new TreeMap<>();
    private final DatabaseService databaseService;
    private final Random random = new Random();
    private final double minProfitAsPercent, maxProfitAsPercent;

    public CycleStorage(
            DatabaseService databaseService,
            @Value("${arbitrageur.graph.processing.profit.percent.min}") double minProfitAsPercent,
            @Value("${arbitrageur.graph.processing.profit.percent.max}") double maxProfitAsPercent
    ) {
        this.databaseService = databaseService;
        this.minProfitAsPercent = minProfitAsPercent;
        this.maxProfitAsPercent = maxProfitAsPercent;
    }

    public synchronized void addAll(Map<FieldsComparableCycle, ProfitComparableCycle> foundCycles) {
        for (var pair : foundCycles.entrySet()) {
            FieldsComparableCycle fieldCycle = pair.getKey();
            ProfitComparableCycle profitCycle = pair.getValue();
            addCycle(fieldCycle, profitCycle);
        }
    }

    public synchronized Iterable<ProfitComparableCycle> getAll() {
        return sorted(fieldProfitMap.values());
    }

    public synchronized void updateAll() {
        Map<String, Set<Integer>> tradingPairIdMap = getTradingPairIdMap();
        Map<Integer, OrderbookDTO> orderbookMap = databaseService.getOrderbookMap(
                tradingPairIdMap.get(OrderbookDTO.class.getName())
        );
        Map<Integer, SwapDTO> swapMap = databaseService.getSwapMap(
                tradingPairIdMap.get(SwapDTO.class.getName())
        );

        List<FieldsComparableCycle> cyclesToDelete = new LinkedList<>();
        for (var pair : fieldProfitMap.entrySet()) {
            FieldsComparableCycle fieldCycle = pair.getKey();
            ProfitComparableCycle profitCycle = pair.getValue();

            if (!updateCycles(fieldCycle, profitCycle, orderbookMap, swapMap))
                cyclesToDelete.add(fieldCycle);
        }
        cyclesToDelete.forEach(this::deleteCycle);
    }

    private void addCycle(FieldsComparableCycle fieldCycle, ProfitComparableCycle profitCycle) {
        ProfitComparableCycle existedCycle = fieldProfitMap.get(fieldCycle);
        int id = (existedCycle == null) ? generateId() : existedCycle.getId();

        fieldCycle.setId(id);
        profitCycle.setId(id);
        fieldProfitMap.put(fieldCycle, profitCycle);
    }

    private void deleteCycle(FieldsComparableCycle cycle) {
        int id = cycle.getId();
        activeIds.remove(id);
        deletedIds.add(id);
        fieldProfitMap.remove(cycle);
    }

    private int generateId() {
        int id = random.nextInt();
        while (activeIds.contains(id) || deletedIds.contains(id))
            id = random.nextInt();
        activeIds.add(id);
        return id;
    }

    private boolean updateCycles(
            FieldsComparableCycle fieldCycle,
            ProfitComparableCycle profitCycle,
            Map<Integer, OrderbookDTO> orderbookMap,
            Map<Integer, SwapDTO> swapMap
    ) {
        List<DirectedEdge> fieldCycleEdges = fieldCycle.getCycle();
        List<DirectedEdge> profitCycleEdges = profitCycle.getCycle();

        if (!updateEdges(fieldCycleEdges, orderbookMap, swapMap))
            return false;
        if (!updateEdges(profitCycleEdges, orderbookMap, swapMap))
            return false;

        double newProfit = profitCycle.updateProfit();
        return newProfit >= minProfitAsPercent && newProfit <= maxProfitAsPercent;
    }

    private boolean updateEdges(
            List<DirectedEdge> edges,
            Map<Integer, OrderbookDTO> orderbookMap,
            Map<Integer, SwapDTO> swapMap
    ) {
        for (DirectedEdge edge : edges)
            if (!updateEdge(edge, orderbookMap, swapMap))
                return false;
        return true;
    }

    private boolean updateEdge(
            DirectedEdge edge,
            Map<Integer, OrderbookDTO> orderbookMap,
            Map<Integer, SwapDTO> swapMap
    ) {
        if (edge instanceof OrderbookEdge orderbook) {
            int id = orderbook.getOrderbook().getId();
            OrderbookDTO newOrderbook = orderbookMap.get(id);
            if (newOrderbook == null)
                return false;
            orderbook.update(newOrderbook);
            return true;
        }

        if (edge instanceof SwapEdge swap) {
            int id = swap.getSwap().getId();
            SwapDTO newSwap = swapMap.get(id);
            if (newSwap == null)
                return false;
            swap.update(newSwap);
            return true;
        }

        throw new IllegalArgumentException("Unknown edge class: edge=" + edge);
    }

    private Map<String, Set<Integer>> getTradingPairIdMap() {
        Map<String, Set<Integer>> result = new HashMap<>();

        for (ProfitComparableCycle cycle : fieldProfitMap.values()) {
            putTradingPairIdsToMap(cycle.getCycle(), result);
            if (cycle instanceof ConnectedCycle connected)
                for (var connection : connected.getAllConnections())
                    putTradingPairIdsToMap(connection, result);
        }

        return result;
    }

    private void putTradingPairIdsToMap(Iterable<DirectedEdge> edges, Map<String, Set<Integer>> map) {
        for (DirectedEdge edge : edges) {

            Identifiable tradingPair = null;
            if (edge instanceof OrderbookEdge orderbookEdge) {
                tradingPair = orderbookEdge.getOrderbook();
            } else if (edge instanceof SwapEdge swapEdge) {
                tradingPair = swapEdge.getSwap();
            }

            if (tradingPair == null) {
                String message = "Trading pair is null: edge=" + edge;
                throw new IllegalArgumentException(message);
            }
            if (tradingPair.getId() == null) {
                String message = "Trading pair id null: edge=%s, tradingPair=%s";
                throw new IllegalArgumentException(String.format(message, edge, tradingPair));
            }

            map.computeIfAbsent(tradingPair.getClass().getName(), x -> new TreeSet<>()).add(tradingPair.getId());
        }
    }

    private Iterable<ProfitComparableCycle> sorted(Iterable<ProfitComparableCycle> cycles) {
        PriorityQueue<ProfitComparableCycle> pq = new PriorityQueue<>();
        for (var pair : cycles)
            pq.add(pair);

        LinkedList<ProfitComparableCycle> sortedCycles = new LinkedList<>();
        while (!pq.isEmpty())
            sortedCycles.addFirst(pq.poll());
        return sortedCycles;
    }
}
