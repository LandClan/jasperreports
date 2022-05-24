package net.sf.jasperreports.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

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
    private static final String STYLED_REFERENCE_NUMBER_FORMAT =
            " <sup><style forecolor='blue' isUnderline='true'>%s</style></sup>";

    private int currentReferenceNumber = 1;

    private final List<Map<String, String>> references = new ArrayList<>();
    private final Set<String> keysAdded = new HashSet<>();

    /**
     * Returns the reference number for the given key for use in assigning a reference number to a super-script text
     * field. Must be rendered at Report time or at least after the given reference key has been added.
     * @param referenceKey
     * @return
     * @throws JRException If the given key is not found (ensures robust report configuration).
     */
    private String getReferenceNumberForKey(String referenceKey) throws JRException {
        for (Map<String, String> reference : this.references) {
            if (referenceKey.equals(reference.get(REFERENCE_KEY))) {
                return reference.get(REFERENCE_NUMBER);
            }
        }
        throw new JRException("Reference key \"" + referenceKey + "\" not found! Potential mis-match of key names, or" +
                "a call to getReferenceNumberForKey() when the text was empty - if the latter, try using " +
                "getStyledReferenceNumberTextForKey() instead.");
    }

    public String createReferenceNumberAnchorForKey(String referenceKey) throws JRException {

        if (!keysAdded.contains(referenceKey)) {
            return null;
        }

        return createReferenceNumberAnchor(getReferenceNumberForKey(referenceKey));
    }

    public String createReferenceNumberAnchor(String value) throws JRException {

        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // Additional validation to align the method name with it's counterpart "...ForKey".
            throw new JRException("Value is not numeric!");
        }

        return "ref_" + value;
    }

    /**
     * Returns the reference number for the given key as a string of super-script styled XML to be used in a text
     * element. The text element must have the attribute {@code markup="styled"}. If the reference key is not in the
     * list of references, for example due to the provided text being empty, then an empty string is returned.
     * @param referenceKey
     * @return
     * @throws JRException
     */
    public String getStyledReferenceNumberTextForKey(String referenceKey) throws JRException {

        if (!keysAdded.contains(referenceKey)) {
            return "";
        }

        String referenceNumberStr = getReferenceNumberForKey(referenceKey);
        return String.format(STYLED_REFERENCE_NUMBER_FORMAT, referenceNumberStr);
    }

    /**
     * Whether there are any references. Can be used to determine whether to display a references detail section.
     * @return
     */
    public boolean hasReferences() {
        return !references.isEmpty();
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
     * Adds the reference text for the given reference key. This should be called before any attempt is made to get the
     * reference number or text for the given reference key.
     * @param referenceText
     * @param referenceKey
     * @return The reference key.
     * @throws JRException If text has already been added for the given key (ensures robust report configuration).
     */
    public String addReferenceTextForKey(String referenceText, String referenceKey) throws JRException {

        if (keysAdded.contains(referenceKey)) {
            throw new JRException("Reference text was already added for key \"" + referenceKey + "\"!");
        }

        if (referenceText != null && !referenceText.isEmpty()) {
            references.add(createReferenceEntry(referenceText, referenceKey));
            keysAdded.add(referenceKey);
        }

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
