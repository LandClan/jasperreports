package net.sf.jasperreports.util;

import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to gather reference keys and their associated incremental reference numbers during report fill
 * time. Each reference number is associated to a String key. During collection, key/number pairs are added and the next
 * number is incremented. When finished, the reference data are returned as a JRDataSource or by direct retrieval by
 * reference key.
 */
public class ReferenceCreator {

    private static final String REFERENCE_KEY = "refKey";
    private static final String REFERENCE_TEXT = "refText";
    private static final String REFERENCE_NUMBER = "refNumber";

    private int currentReferenceNumber = 1;

    private final List<Map<String, String>> references = new ArrayList<>();

    /**
     * Returns the reference number for the given key for use in assigning a reference number to a super-script text
     * field. Must be rendered at Report time or at least after the given reference key has been added.
     * @param referenceKey
     * @return
     */
    public String getReferenceNumberForKey(String referenceKey) {
        for (Map<String, String> reference : this.references) {
            if(referenceKey.equals(reference.get(REFERENCE_KEY))) {
                return reference.get(REFERENCE_NUMBER);
            }
        }
        return null;
    }

    /**
     * Returns a {@code JRDataSource} for use as a datasource expression. The datasource contains all the reference
     * entries that have been collected for use in creating a list of references at the end of the report or report
     * section.
     * @return
     */
    public JRRewindableDataSource getDataSource() {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Collection<Map<String, ?>> resultCollection = (Collection) references;
        return new JRMapCollectionDataSource(resultCollection);
    }

    /**
     * Adds the reference text for the given reference key.
     * @param referenceText
     * @param referenceKey
     * @return The reference key.
     */
    public String addReferenceTextForKey(String referenceText, String referenceKey) {
        references.add(createReferenceEntry(referenceText, referenceKey));
        return referenceKey;
    }

    private Map<String, String> createReferenceEntry(String referenceText, String referenceKey) {
        Map<String, String> refEntry = new HashMap<>();
        refEntry.put(REFERENCE_KEY, referenceKey);
        refEntry.put(REFERENCE_TEXT, referenceText);
        refEntry.put(REFERENCE_NUMBER, String.valueOf(currentReferenceNumber++));
        return refEntry;
    }
}