package com.stambul.arbitrageur.arbitrage.graph.processing;

import com.stambul.arbitrageur.arbitrage.cycles.processing.Arbitrage;
import com.stambul.arbitrageur.arbitrage.cycles.processing.CyclesProcessor;
import com.stambul.arbitrageur.arbitrage.cycles.objects.FieldsComparableCycle;
import com.stambul.arbitrageur.arbitrage.cycles.processing.CycleSplitter;
import com.stambul.arbitrageur.arbitrage.cycles.objects.ProfitComparableCycle;
import com.stambul.arbitrageur.arbitrage.graph.interfaces.WeightedDigraph;
import com.stambul.arbitrageur.arbitrage.graph.provider.interfaces.VerticesEncoder;
import com.stambul.library.tools.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphProcessor {
    public interface Processor { void process(int componentId, Arbitrage arbitrage, Set<FieldsComparableCycle> foundCycles); }
    private final Logger logger = Logger.getLogger(this.getClass());
    private final String[] startCurrencySlugs;
    private final String[] startMarketSlugs;
    private final double[] startCurrencyAmounts;
    private final double additionalCommissionFrom, additionalCommissionStep, minProfitAsPercent, maxProfitAsPercent;
    private final int componentMinSize, randomLaunches;

    public GraphProcessor(
            @Value("${arbitrageur.graph.processing.start.currency.slug}") String[] startCurrencySlugs,
            @Value("${arbitrageur.graph.processing.start.currency.market}") String[] startMarketSlugs,
            @Value("${arbitrageur.graph.processing.start.currency.amount}") double[] startCurrencyAmounts,
            @Value("${arbitrageur.graph.processing.additional.commission.from}") double additionalCommissionFrom,
            @Value("${arbitrageur.graph.processing.additional.commission.step}") double additionalCommissionStep,
            @Value("${arbitrageur.graph.processing.random.launches}") int randomLaunches,
            @Value("${arbitrageur.graph.processing.component.min.size}") int componentMinSize,
            @Value("${arbitrageur.graph.processing.profit.percent.min}") double minProfitAsPercent,
            @Value("${arbitrageur.graph.processing.profit.percent.max}") double maxProfitAsPercent
    ) {
        this.startCurrencySlugs = startCurrencySlugs;
        this.startMarketSlugs = startMarketSlugs;
        this.startCurrencyAmounts = startCurrencyAmounts;
        this.additionalCommissionFrom = additionalCommissionFrom;
        this.additionalCommissionStep = additionalCommissionStep;
        this.componentMinSize = componentMinSize;
        this.randomLaunches = randomLaunches;
        this.minProfitAsPercent = minProfitAsPercent;
        this.maxProfitAsPercent = maxProfitAsPercent;

        validateFields();
    }

    public Map<FieldsComparableCycle, ProfitComparableCycle> findAllCycles(
            WeightedDigraph graph,
            VerticesEncoder encoder
    ) {
        CyclesProcessor processor = new CyclesProcessor(minProfitAsPercent, maxProfitAsPercent);
        Connectivity connectivity = new Connectivity(graph, encoder);

        for (var component : connectivity.getAllComponents()) {
            StringBuilder builder = new StringBuilder();
            for (var vertex : component)
                builder.append(encoder.getCode(vertex)).append(" ");
            logger.info(builder);
        }

        process(graph, connectivity, generateStartVertices(encoder), processor::addAll);
        process(graph, connectivity, generateRandomVertices(connectivity, processor), processor::addFoundCycles);

        return processor.getAllCycles();
    }

    private void process(
            WeightedDigraph graph,
            Connectivity connectivity,
            Iterable<Pair<Integer, Double>> startVertices,
            Processor processor
    ) {
        for (var pair : startVertices) {
            int startVertex = pair.getFirst();
            double startCurrencyAmount = pair.getSecond();

            Pair<Arbitrage, Set<FieldsComparableCycle>> result = findCycles(
                    graph, startVertex, startCurrencyAmount, additionalCommissionFrom, additionalCommissionStep
            );

            int componentId = connectivity.getComponentIdByVertex(startVertex);
            Arbitrage arbitrage = result.getFirst();
            Set<FieldsComparableCycle> foundCycles = result.getSecond();

            processor.process(componentId, arbitrage, foundCycles);
        }

    }

    private Iterable<Pair<Integer, Double>> generateStartVertices(VerticesEncoder encoder) {
        List<Pair<Integer, Double>> startVertices = new LinkedList<>();

        for (int i = 0; i < startCurrencySlugs.length; i++) {
            int startVertex = encoder.makeIfNotExistsIndex(startMarketSlugs[i], startCurrencySlugs[i]);
            double startCurrencyAmount = startCurrencyAmounts[i];
            startVertices.add(new Pair<>(startVertex, startCurrencyAmount));
        }

        return startVertices;
    }

    private Iterable<Pair<Integer, Double>> generateRandomVertices(
            Connectivity connectivity,
            CyclesProcessor processor
    ) {
        List<Pair<Integer, Double>> startVertices = new LinkedList<>();

        int maxVertexIdx = connectivity.getSize();
        int minVertexIdx = 0;

        while (startVertices.size() < randomLaunches) {
            int startVertex = minVertexIdx + (int) (Math.random() * (maxVertexIdx - minVertexIdx));
            int component = connectivity.getComponentIdByVertex(startVertex);

            List<Arbitrage> arbitrages = processor.getArbitrages(component);
            if (arbitrages == null || connectivity.getComponentSizeById(component) < componentMinSize)
                continue;

            double startCurrencyAmount = arbitrages.get(0).getDistanceTo(startVertex);
            startVertices.add(new Pair<>(startVertex, startCurrencyAmount));
        }

        return startVertices;
    }

    private Pair<Arbitrage, Set<FieldsComparableCycle>> findCycles(
            WeightedDigraph graph,
            int startVertex,
            double startCurrencyAmount,
            double additionalCommissionFrom,
            double additionalCommissionStep
    ) {
        Set<FieldsComparableCycle> foundCycles = new TreeSet<>();

        double additionalCommission = additionalCommissionFrom;
        Arbitrage arbitrage = new Arbitrage(graph, startVertex, additionalCommission, startCurrencyAmount);
        while (arbitrage.hasCycle()) {
            CycleSplitter splitter = new CycleSplitter(arbitrage.getCycle());
            for (var cycle : splitter.split())
                foundCycles.add(new FieldsComparableCycle(cycle));

            additionalCommission -= additionalCommissionStep;
            arbitrage = new Arbitrage(graph, startVertex, additionalCommission, startCurrencyAmount);
        };

        return new Pair<>(arbitrage, foundCycles);
    }

    private void validateFields() {
        if (startCurrencySlugs.length != startCurrencyAmounts.length) {
            String message = "Size mismatch: startCurrencySlugs=%s, startCurrencyAmounts=%s";
            message = String.format(message, Arrays.toString(startCurrencySlugs), Arrays.toString(startCurrencyAmounts));
            throw new IllegalArgumentException(message);
        }

        if (startCurrencySlugs.length != startMarketSlugs.length) {
            String message = "Size mismatch: startCurrencySlugs=%s, startMarketSlugs=%s";
            message = String.format(message, Arrays.toString(startCurrencySlugs), Arrays.toString(startMarketSlugs));
            throw new IllegalArgumentException(message);
        }
    }
}


