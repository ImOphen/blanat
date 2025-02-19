import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {

        record Measurement(String firstPart, String secondPart) {
        }
        ;

        var alltats = new BufferedReader(new FileReader("input.txt"))
                .lines()
                .parallel()
                .collect(Collectors.groupingBy(
                        line -> {
                            int firstSemicolonIndex = line.indexOf(',');
                            int secondSemicolonIndex = line.indexOf(',', firstSemicolonIndex + 1);
                            String firstPart = line.substring(0, firstSemicolonIndex);
                            String secondPart = line.substring(firstSemicolonIndex + 1, secondSemicolonIndex);
                            return new Measurement(firstPart, secondPart);
                        },
                        Collectors.summarizingDouble(line -> Double
                                .parseDouble(line.substring(line.indexOf(',', line.indexOf(',') + 1) + 1)))));

        var result = alltats.entrySet().stream().collect(
                Collectors.groupingBy(
                        entry -> entry.getKey().firstPart,
                        TreeMap::new,
                        Collectors.summarizingDouble(e -> e.getValue().getSum())))
                .entrySet().stream().collect(Collectors.toMap(
                        Entry::getKey,
                        e -> {
                            var stats = e.getValue();
                            return stats.getSum();
                        },
                        (l, r) -> r,
                        TreeMap::new))
                .entrySet().stream()
                .map(entry -> {
                    Double value = entry.getValue();
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), value);
                })
                .min(Comparator.comparingDouble(entry -> entry.getValue()));

        var res = alltats.entrySet().stream().collect(
                Collectors.groupingBy(
                        entry -> entry.getKey().secondPart,
                        TreeMap::new,
                        Collectors.summarizingDouble(e -> e.getValue().getMin())))
                .entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getMin(),
                        (l, r) -> r,
                        TreeMap::new))
                .entrySet().stream().map(entry -> {
                    Double value = entry.getValue();
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), value);
                })
                .sorted(Entry.comparingByValue(Comparator.comparingDouble(entry -> entry.floatValue()))).distinct()
                .limit(5);

        writeToFile(result.get().getKey() + " " + String.format("%.2f",result.get().getValue()).replace(',', '.'), "output.txt");

        res.forEach(entry -> {
            String key = entry.getKey();
            String value =  String.format("%.2f",entry.getValue()).replace(',', '.');
            try {
                writeToFile(key + " " + value, "output.txt");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

    }

    public static void writeToFile(String line, String filePath) throws IOException {
        Files.write(Paths.get(filePath), (line + System.lineSeparator()).getBytes(),
                java.nio.file.StandardOpenOption.APPEND);
    }
}