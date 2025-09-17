package de.hamburg.university.agent.guardrails;

import io.quarkiverse.langchain4j.guardrails.OutputTokenAccumulator;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ParagraphAccumulator implements OutputTokenAccumulator {

    // Emits chunks when a paragraph boundary is detected:
    //  - blank line (\n\n),
    //  - or a new heading "## " on a new line,
    //  - or end of stream (flush remainder).
    @Override
    public Multi<String> accumulate(Multi<String> tokens) {
        return Multi.createFrom().emitter(em -> {
            final StringBuilder buf = new StringBuilder();

            tokens.subscribe().with(
                    chunk -> {
                        buf.append(chunk);
                        String s = buf.toString();

                        int emitUpto = findBoundaryIndex(s);
                        while (emitUpto >= 0) {
                            String toEmit = s.substring(0, emitUpto).trim();
                            if (!toEmit.isEmpty()) em.emit(toEmit);
                            s = s.substring(emitUpto);
                            emitUpto = findBoundaryIndex(s);
                        }
                        buf.setLength(0);
                        buf.append(s);
                    },
                    em::fail,
                    () -> {
                        String tail = buf.toString().trim();
                        if (!tail.isEmpty()) em.emit(tail);
                        em.complete();
                    }
            );
        });
    }

    private int findBoundaryIndex(String s) {
        // paragraph break
        int bi = s.indexOf("\n\n");
        // heading start on new line
        int hi = -1;
        int idx = s.indexOf("\n## ");
        if (idx >= 0) hi = idx + 1; // keep the "## " for the next chunk

        if (bi >= 0 && hi >= 0) return Math.min(bi + 2, hi);
        if (bi >= 0) return bi + 2;
        if (hi >= 0) return hi;
        return -1;
    }
}
