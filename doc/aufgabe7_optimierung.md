# Aufgabe 7: Optimierung der Duplikaterkennung in PPClean

In diesem Dokument wird die theoretische Funktionsweise sowie die praktische Umsetzung der vier Ansätze zur Optimierung der Duplikaterkennung in **PPClean** detailliert beschrieben.

---

## 1. Parametrisierung & Hyperparameter Tuning

### Idee & Konzept
Die Effizienz und Genauigkeit aller Duplikaterkennungs- und Blocking-Verfahren hängen maßgeblich von den gewählten Parametern ab. Durch systematische Feineinstellung (Hyperparameter Tuning) lassen sich Precision, Recall und F1-Score optimal ausbalancieren.

### Theoretische Funktionsweise & Umsetzung
1. **Sorted Neighborhood Method (SNM)**:
   - **Fenstergröße (`windowSize`)**: Ein größeres Fenster (z. B. 15–30 anstelle von 4) erhöht die Chance, echte Duplikate zu finden (höherer Recall), benötigt aber mehr Vergleiche.
   - **Sortierschlüssel (`keyComponents`)**: Der Schlüssel bestimmt die Reihenfolge der Records. Ein zusammengesetzter Schlüssel (z. B. die ersten 3 Zeichen von `name`, `address` und `phone`) gruppiert verwandte Einträge nahe beieinander.

2. **Locality Sensitive Hashing (LSH)**:
   - **Bänder (`numBands` $b$) & MinHashes ($m$)**: Die Wahrscheinlichkeit, dass zwei Datensätze mit Jaccard-Ähnlichkeit $s$ in mindestens einem Bucket landen, folgt der S-Kurve: $P(s) = 1 - (1 - s^{m/b})^b$. Durch Anpassung von $b$ und $m$ lässt sich die Schwelle exakt so einstellen, dass Kandidatenpaare mit $s \ge 0.8$ zuverlässig erfasst werden.

3. **Hybrid-Richtlinien & Gewichtung**:
   - Unterschiedliche Attribute besitzen unterschiedliche Aussagekraft. Beispielsweise bieten Namens- und Telefonattribute eine höhere Trennschärfe als Stadt oder Typ.
   - Einbindung einer gewichteten Ähnlichkeitsfunktion: 
     $$\text{Similarity}(r_1, r_2) = \frac{\sum w_i \cdot \text{sim}_i(v_{1,i}, v_{2,i})}{\sum w_i}$$

---

## 2. Weitere Ähnlichkeitsmaße (Jaro-Winkler, Soundex, Monge-Elkan)

### Idee & Konzept
Standardmaße wie Levenshtein oder Jaccard stoßen bei tippfehleranfälligen Personennamen, lautmalerischen Abweichungen oder verdrehten Wortreihenfolgen an ihre Grenzen. Spezialisierte Maße lösen diese Problemstellungen:

### Theoretische Funktionsweise & Umsetzung
1. **Jaro-Winkler**:
   - **Funktionsweise**: Rechnet die Anzahl übereinstimmender Zeichen und Transpositionen ein, belohnt jedoch gemeinsame Präfixe. Ideal für Tippfehler bei Namen (z. B. "Arnie Morton" vs. "Arnie Morton's").
   - **Umsetzung**: Einbindung der Apache Commons Text Bibliothek (`org.apache.commons.text.similarity.JaroWinklerSimilarity`).
   ```java
   public class JaroWinkler implements StringSimilarity {
       private final JaroWinklerSimilarity jw = new JaroWinklerSimilarity();
       @Override
       public double compare(String x, String y) {
           return jw.apply(x, y);
       }
   }
   ```

2. **Soundex**:
   - **Funktionsweise**: Kodiert Wörter nach ihrem Klang im Englischen in 4-stellige Codes (z. B. "Smith" und "Smyth" $\rightarrow$ `S530`).
   - **Umsetzung**: Nutzung von Apache Commons Codec (`org.apache.commons.codec.language.Soundex`).
   ```java
   public class SoundexSimilarity implements StringSimilarity {
       private final Soundex soundex = new Soundex();
       @Override
       public double compare(String x, String y) {
           try {
               int diff = soundex.difference(x, y); // 0 bis 4
               return diff / 4.0;
           } catch (Exception e) {
               return 0.0;
           }
       }
   }
   ```

3. **Monge-Elkan**:
   - **Funktionsweise**: Tokenisiert Strings in Wörter und sucht für jedes Token des ersten Strings das ähnlichste Token im zweiten String. Dadurch werden Wortdreher (z. B. "Hotel Bel-Air" vs. "Bel-Air Hotel") perfekt erkannt.
   - **Formula**:
     $$\text{MongeElkan}(A, B) = \frac{1}{|A|} \sum_{a \in A} \max_{b \in B} \text{sim}(a, b)$$

---

## 3. Transitive Hülle (Transitive Closure)

### Idee & Konzept
Aufgrund der Transitivität der mathematischen Gleichheit gilt: Wenn $(A, B)$ ein Duplikat ist und $(B, C)$ ebenfalls ein Duplikat ist, muss $(A, C)$ logischerweise auch ein Duplikat sein. Fehlende Duplikatpaare lassen sich so ohne erneute Ähnlichkeitsberechnung inferieren.

### Praktische Umsetzung im Code
Die Erkennung wird als Graph-Problem interpretiert:
- **Knoten**: Records
- **Kanten**: Gefundene Duplikate `Duplicate(r1, r2)`
- **Algorithmus**: Ermittlung aller Zusammenhangskomponenten (Connected Components) via Breitensuche (BFS). Jede Komponente der Größe $k$ wird zu einer Clique (vollständiger Teilgraph) aus allen $\binom{k}{2}$ Paaren ausgebaut.

#### Implementierung in `Helper.java`:
```java
public static Set<Duplicate> computeTransitiveClosure(Set<Duplicate> duplicates) {
    Map<Record, Set<Record>> adj = new HashMap<>();
    for (Duplicate d : duplicates) {
        Record r1 = d.getRecord1();
        Record r2 = d.getRecord2();
        adj.computeIfAbsent(r1, k -> new HashSet<>()).add(r2);
        adj.computeIfAbsent(r2, k -> new HashSet<>()).add(r1);
    }

    Set<Duplicate> closure = new HashSet<>();
    Set<Record> visited = new HashSet<>();

    for (Record start : adj.keySet()) {
        if (!visited.contains(start)) {
            List<Record> component = new ArrayList<>();
            Queue<Record> queue = new LinkedList<>();
            queue.add(start);
            visited.add(start);

            while (!queue.isEmpty()) {
                Record curr = queue.poll();
                component.add(curr);
                for (Record neighbor : adj.getOrDefault(curr, Collections.emptySet())) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }

            for (int i = 0; i < component.size(); i++) {
                for (int j = i + 1; j < component.size(); j++) {
                    closure.add(new Duplicate(component.get(i), component.get(j)));
                }
            }
        }
    }
    return closure;
}
```

#### Einbindung in `PPClean.java`:
```java
Set<Duplicate> rawDuplicates = lshDetection.detect(inputTable, nameAttributeEquality);
Set<Duplicate> closureDuplicates = Helper.computeTransitiveClosure(rawDuplicates);
```

---

## 4. Parallelisierung (Multi-Threading & Java Parallel Streams)

### Idee & Konzept
Bei der naiven Duplikaterkennung oder großen Blocking-Fenstern müssen tausende Paarvergleiche `recSim.compare(r1, r2)` durchgeführt werden. Da jeder Paarvergleich unabhängig von anderen Vergleichen ist, eignet sich dieser Schritt hervorragend für Datenparallelismus.

### Theoretische Umsetzung im Code
1. **Parallel Streams**:
   Kandidatenpaare werden in einer Liste `List<RecordPair>` gesammelt und mittels Java Parallel Streams parallel verarbeitet:
   ```java
   public Set<Duplicate> detect(Table table, RecordSimilarity recSim) {
       List<RecordPair> candidatePairs = generateCandidatePairs(table);

       return candidatePairs.parallelStream()
           .filter(pair -> recSim.compare(pair.getRecord1(), pair.getRecord2()) >= threshold)
           .map(pair -> new Duplicate(pair.getRecord1(), pair.getRecord2()))
           .collect(Collectors.toSet());
   }
   ```

2. **ExecutorService / ThreadPool**:
   Verteilung von Datensatz-Blöcken auf eine feste Anzahl von Worker-Threads:
   ```java
   ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
   ```
   Auf modernen Multi-Core-CPUs führt die Parallelisierung zu einer nahezu linearen Beschleunigung (speedup) der Laufzeit.
