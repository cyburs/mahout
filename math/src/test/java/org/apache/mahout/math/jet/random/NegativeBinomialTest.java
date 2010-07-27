package org.apache.mahout.math.jet.random;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import org.apache.mahout.math.jet.random.engine.MersenneTwister;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

public class NegativeBinomialTest {
  private Splitter onComma = Splitter.on(",").trimResults();
  private static final int N = 10000;

  @Test
  public void testDistributionFunctions() throws IOException {
    InputSupplier<InputStreamReader> input = Resources.newReaderSupplier(Resources.getResource("negative-binomial-test-data.csv"), Charsets.UTF_8);
    boolean header = true;
    for (String line : CharStreams.readLines(input)) {
      if (header) {
        // skip
        header = false;
      } else {
        Iterable<String> values = onComma.split(line);
        int k = Integer.parseInt(Iterables.get(values, 0));
        double p = Double.parseDouble(Iterables.get(values, 1));
        int r = Integer.parseInt(Iterables.get(values, 2));
        double density = Double.parseDouble(Iterables.get(values, 3));
        double cume = Double.parseDouble(Iterables.get(values, 4));
        NegativeBinomial nb = new NegativeBinomial(r, p, new MersenneTwister());
        assertEquals("cumulative " + k + "," + p + "," + r, cume, nb.cdf(k), cume * 1e-05);
        assertEquals("density " + k + "," + p + "," + r, density, nb.pdf(k), density * 1e-05);
      }
    }
  }

  @Test
  public void sample() {
    for (double p : new double[]{0.1, 0.2, 0.5, 0.9}) {
      for (int r = 1; r < 5; r++) {
        NegativeBinomial nb = new NegativeBinomial(r, p, new MersenneTwister(1));
        int[] counts = new int[N];
        for (int i = 0; i < N; i++) {
          int k = nb.nextInt();
          if (k < counts.length) {
            counts[k]++;
          }
        }

        // probably should do a chi^2 or LLR test here especially since we know the PDF
        for (int k = 0; k < counts.length; k++) {
          assertEquals(String.format("r=%d,p=%.3f,k=%d,count=%d,pdf=%.3f", r, p, k, counts[k], nb.pdf(k)), N * nb.pdf(k), counts[k], Math.max(3, 4 * Math.sqrt(N * nb.pdf(k) * (1 - nb.pdf(k)))));
        }
      }
    }
  }
}
