package nu.mine.mosher.rdf;

import org.eclipse.rdf4j.model.impl.*;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.*;

import java.io.*;

import static org.eclipse.rdf4j.model.util.Namespaces.*;
import static org.eclipse.rdf4j.rio.helpers.BasicParserSettings.*;
import static org.eclipse.rdf4j.rio.helpers.BasicWriterSettings.*;
import static org.eclipse.rdf4j.rio.helpers.RDFaParserSettings.*;

public class ToRdf {
    public static void main(final String... args) throws IOException {
        final var filepath = args[0];

        final var format =
          Rio.getParserFormatForFileName(filepath).orElse(RDFFormat.RDFXML);

        final var parser = Rio.createParser(format);
        parser.getParserConfig().set(RDFA_COMPATIBILITY, RDFaVersion.RDFA_1_1);
        parser.getParserConfig().set(VOCAB_EXPANSION_ENABLED, true);
        parser.getParserConfig().set(NAMESPACES, DEFAULT_RDF4J);

        final var model = new LinkedHashModel();
//        DEFAULT_RDF4J.forEach(model::setNamespace);
        parser.setRDFHandler(new StatementCollector(model));

        try (final InputStream in = new BufferedInputStream(new FileInputStream(filepath))) {
            parser.parse(in, "file://"+filepath);
        }

//        model.getNamespaces().forEach(n -> System.out.println("DETECTED NAMESPACE: "+n.getName()));

        try (final OutputStream out = new FileOutputStream(FileDescriptor.out)) {
            final var writer = Rio.createWriter(RDFFormat.TURTLE, out);
            writer.getWriterConfig().set(PRETTY_PRINT, true);
            writer.getWriterConfig().set(INLINE_BLANK_NODES, false);
            writer.startRDF();
            DEFAULT_RDF4J.forEach(n -> writer.handleNamespace(n.getPrefix(), n.getName()));
            for (final var st : model) {
                writer.handleStatement(st);
            }
            writer.endRDF();
        }
        System.out.flush();
        System.err.flush();
    }
}
