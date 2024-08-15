package com.stambul.scanner.jobs.parsers.results;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.tools.IterableTools;
import com.stambul.library.tools.Pair;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import java.util.*;

public class ParsingResults<D extends DTO<D>> implements ResolvableTypeProvider {
    private final Map<Integer, List<D>> parsingResultsMap = new TreeMap<>();
    private final Map<Integer, Map<String, Exception>> exceptionsMap = new TreeMap<>();
    private final String parserName;
    private final Class<D> dataClass;

    public ParsingResults(Class<D> dataClass, String parserName) {
        this.dataClass = dataClass;
        this.parserName = parserName;
    }

    public void addAll(Collection<? extends D> result, int marketId) {
        parsingResultsMap.computeIfAbsent(marketId, k -> new LinkedList<>()).addAll(result);
    }

    public void add(D result, int marketId) {
        parsingResultsMap.computeIfAbsent(marketId, k -> new LinkedList<>()).add(result);
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                this.getClass(), ResolvableType.forClass(dataClass)
        );
    }

    public Class<D> getDataClass() {
        return dataClass;
    }

    public Iterable<Integer> getMarketIds() {
        return parsingResultsMap.keySet();
    }

    public Iterable<D> getParsingResults(int marketId) {
        return parsingResultsMap.get(marketId);
    }

    public void block(String ticker, Exception blockedResult, int marketId) {
        exceptionsMap.computeIfAbsent(marketId, k -> new HashMap<>()).put(ticker, blockedResult);
    }

    public void blockAll(Iterable<Pair<String, Exception>> blockedResults, int marketId) {
        if (IterableTools.size(blockedResults) == 0)
            return;
        Map<String, Exception> exceptions = exceptionsMap.computeIfAbsent(marketId, k -> new HashMap<>());
        blockedResults.forEach(pair -> exceptions.put(pair.getFirst(), pair.getSecond()));
    }

    public boolean isBlacklistEmpty() {
        return exceptionsMap.isEmpty();
    }

    public Iterable<Pair<String, Exception>> getBlacklist(int marketId) {
        List<Pair<String, Exception>> blacklist = new LinkedList<>();

        Map<String, Exception> blockedResults = exceptionsMap.get(marketId);
        if (blockedResults != null) {
            for (String ticker : blockedResults.keySet())
                blacklist.add(new Pair<>(ticker, blockedResults.get(ticker)));
        }

        return blacklist;
    }

    public boolean isEmpty() {
        return parsingResultsMap.isEmpty();
    }

    public String getParserName() {
        return parserName;
    }

    @Override
    public String toString() {
        String format = getClass().getSimpleName() + "[marketName=%s, exceptions=%s, parsingResults=%s]";
        return String.format(format, parserName, exceptionsMap, parsingResultsMap);
    }
}
